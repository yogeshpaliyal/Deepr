package com.yogeshpaliyal.deepr.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.deepr.update.AppUpdateManager
import com.yogeshpaliyal.deepr.update.AvailableUpdate
import com.yogeshpaliyal.deepr.update.InstallPermissionRequiredException
import com.yogeshpaliyal.deepr.update.UpdateCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface AppUpdateUiState {
    data object Idle : AppUpdateUiState

    data object Checking : AppUpdateUiState

    data class Available(
        val update: AvailableUpdate,
    ) : AppUpdateUiState

    data class Downloading(
        val progress: Float?,
    ) : AppUpdateUiState
}

enum class AppUpdateMessage {
    UP_TO_DATE,
    CHECK_FAILED,
    UPDATE_FAILED,
    INSTALL_PERMISSION_REQUIRED,
}

class AppUpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
) : ViewModel() {
    private val _state = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Idle)
    val state: StateFlow<AppUpdateUiState> = _state.asStateFlow()

    private val _messages = Channel<AppUpdateMessage>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun checkForUpdate(activity: Activity) {
        if (_state.value != AppUpdateUiState.Idle) return
        _state.value = AppUpdateUiState.Checking
        viewModelScope.launch {
            when (val result = appUpdateManager.checkForUpdate(activity)) {
                is UpdateCheckResult.Available -> {
                    _state.value = AppUpdateUiState.Available(result.update)
                }

                UpdateCheckResult.UpToDate -> {
                    _state.value = AppUpdateUiState.Idle
                    _messages.send(AppUpdateMessage.UP_TO_DATE)
                }

                is UpdateCheckResult.Error -> {
                    _state.value = AppUpdateUiState.Idle
                    _messages.send(AppUpdateMessage.CHECK_FAILED)
                }
            }
        }
    }

    fun startUpdate(
        activity: Activity,
        update: AvailableUpdate,
    ) {
        _state.value = AppUpdateUiState.Downloading(null)
        viewModelScope.launch {
            try {
                appUpdateManager.startUpdate(activity, update) { progress ->
                    _state.value = AppUpdateUiState.Downloading(progress)
                }
                _state.value = AppUpdateUiState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (_: InstallPermissionRequiredException) {
                // Keep the dialog so the user can tap update again after granting permission.
                _state.value = AppUpdateUiState.Available(update)
                _messages.send(AppUpdateMessage.INSTALL_PERMISSION_REQUIRED)
            } catch (_: Exception) {
                _state.value = AppUpdateUiState.Idle
                _messages.send(AppUpdateMessage.UPDATE_FAILED)
            }
        }
    }

    fun dismiss() {
        if (_state.value is AppUpdateUiState.Available) {
            _state.value = AppUpdateUiState.Idle
        }
    }
}
