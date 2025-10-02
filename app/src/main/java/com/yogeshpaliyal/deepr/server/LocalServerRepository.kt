package com.yogeshpaliyal.deepr.server

import kotlinx.coroutines.flow.StateFlow

interface LocalServerRepository {
    val isRunning: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>

    suspend fun startServer()

    suspend fun stopServer()
}
