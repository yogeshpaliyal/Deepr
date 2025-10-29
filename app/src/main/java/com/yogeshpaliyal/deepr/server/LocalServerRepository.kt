package com.yogeshpaliyal.deepr.server

import kotlinx.coroutines.flow.StateFlow

interface LocalServerRepository {
    val isRunning: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>
    val serverPort: StateFlow<Int>
    val isTransferLinkServerRunning: StateFlow<Boolean>
    val transferLinkServerUrl: StateFlow<String?>
    val qrCodeData: StateFlow<String?>

    suspend fun startServer(port: Int)

    suspend fun stopServer()

    suspend fun setServerPort(port: Int)

    suspend fun fetchAndImportFromSender(qrTransferInfo: QRTransferInfo): Result<Unit>
}
