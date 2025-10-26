package com.yogeshpaliyal.deepr.server

import kotlinx.coroutines.flow.StateFlow

interface LocalServerRepository {
    val isRunning: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>
    val isTransferLinkServerRunning: StateFlow<Boolean>
    val transferLinkServerUrl: StateFlow<String?>
    val qrCodeData: StateFlow<String?>

    suspend fun startServer(port: Int)

    suspend fun stopServer()

    suspend fun fetchAndImportFromSender(qrTransferInfo: QRTransferInfo): Result<Unit>
}
