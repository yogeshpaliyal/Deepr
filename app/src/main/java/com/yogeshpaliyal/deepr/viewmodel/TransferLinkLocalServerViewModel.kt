package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import com.yogeshpaliyal.deepr.server.TransferLinkLocalServerRepository

class TransferLinkLocalServerViewModel(
    transferLinkLocalServerRepository: TransferLinkLocalServerRepository,
) : ViewModel() {
    val isRunning = transferLinkLocalServerRepository.isRunning
    val serverUrl = transferLinkLocalServerRepository.serverUrl
    val qrCodeData = transferLinkLocalServerRepository.qrCodeData
}
