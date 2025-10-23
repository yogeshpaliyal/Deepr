package com.yogeshpaliyal.deepr.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.server.QRTransferInfo
import com.yogeshpaliyal.deepr.server.TransferLinkLocalServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TransferLinkLocalServerViewModel(
    private val transferLinkLocalServerRepository: TransferLinkLocalServerRepository,
) : ViewModel() {
    val isRunning = transferLinkLocalServerRepository.isRunning
    val serverUrl = transferLinkLocalServerRepository.serverUrl
    val qrCodeData = transferLinkLocalServerRepository.qrCodeData

    private val _importState = MutableStateFlow<String?>(null)
    val importState = _importState.asStateFlow()

    fun import(data: String) {
        Log.d("Anas", data)
        viewModelScope.launch {
            try {
                val qrInfo = Json.decodeFromString<QRTransferInfo>(data)
                val currentVersion = BuildConfig.VERSION_NAME
                if (qrInfo.appVersion != currentVersion) {
                    _importState.value =
                        "Version mismatch. Sender: ${qrInfo.appVersion}, Receiver: $currentVersion"
                    return@launch
                }

                val result = transferLinkLocalServerRepository.fetchAndImportFromSender(qrInfo)

                result
                    .onSuccess {
                        _importState.value = "Import Successful"
                    }.onFailure { error ->
                        _importState.value = error.message ?: "Unknown error occurred"
                    }
            } catch (e: Exception) {
                Log.e("Anas", "Failed to parse QR code data: ${e.message}")
            }
        }
    }
}
