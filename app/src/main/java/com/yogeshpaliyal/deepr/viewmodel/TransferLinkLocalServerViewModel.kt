package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.server.LocalServerRepository
import com.yogeshpaliyal.deepr.server.QRTransferInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TransferLinkLocalServerViewModel(
    private val localServerRepository: LocalServerRepository,
) : ViewModel() {
    val isRunning = localServerRepository.isTransferLinkServerRunning
    val serverUrl = localServerRepository.transferLinkServerUrl
    val qrCodeData = localServerRepository.qrCodeData

    private val transferResultChannel = Channel<String>()
    val transferResultFlow = transferResultChannel.receiveAsFlow()

    fun import(data: String) {
        viewModelScope.launch {
            try {
                val qrInfo = Json.decodeFromString<QRTransferInfo>(data)
                val currentVersion = BuildConfig.VERSION_NAME
                if (qrInfo.appVersion != currentVersion) {
                    transferResultChannel.send("Version mismatch. Sender: ${qrInfo.appVersion}, Receiver: $currentVersion")
                    return@launch
                }

                val result = localServerRepository.fetchAndImportFromSender(qrInfo)

                result
                    .onSuccess {
                        transferResultChannel.send("Import Successful")
                    }.onFailure { error ->
                        transferResultChannel.send(error.message ?: "Unknown error occurred")
                    }
            } catch (e: Exception) {
                transferResultChannel.send("Failed to parse QR code data: ${e.message}")
            }
        }
    }
}
