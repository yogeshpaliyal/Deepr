package com.yogeshpaliyal.deepr.server

import kotlinx.coroutines.flow.StateFlow

interface TransferLinkLocalServerRepository {
    val isRunning: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>
    val qrCodeData: StateFlow<String?>

    suspend fun startServer()

    suspend fun stopServer()
}
