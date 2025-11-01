package com.yogeshpaliyal.deepr.server

import kotlinx.coroutines.flow.StateFlow

interface LocalServerRepository {
    val isRunning: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>
    val serverPort: StateFlow<Int>
    val qrCodeData: StateFlow<String?>

    suspend fun startServer(port: Int)

    fun stopServer()

    suspend fun setServerPort(port: Int)

    suspend fun fetchAndImportFromSender(qrTransferInfo: QRTransferInfo): Result<Unit>
}
