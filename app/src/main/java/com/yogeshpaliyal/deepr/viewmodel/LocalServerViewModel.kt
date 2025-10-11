package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import com.yogeshpaliyal.deepr.server.LocalServerRepository

class LocalServerViewModel(
    private val localServerRepository: LocalServerRepository,
) : ViewModel() {
    val isRunning = localServerRepository.isRunning
    val serverUrl = localServerRepository.serverUrl
}
