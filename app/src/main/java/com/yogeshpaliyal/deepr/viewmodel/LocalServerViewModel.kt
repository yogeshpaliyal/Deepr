package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.deepr.server.LocalServerRepository
import kotlinx.coroutines.launch

class LocalServerViewModel(
    private val localServerRepository: LocalServerRepository,
) : ViewModel() {
    val isRunning = localServerRepository.isRunning
    val serverUrl = localServerRepository.serverUrl

    fun startServer() {
        viewModelScope.launch {
            localServerRepository.startServer()
        }
    }

    fun stopServer() {
        viewModelScope.launch {
            localServerRepository.stopServer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            localServerRepository.stopServer()
        }
    }
}
