package com.yogeshpaliyal.deepr.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.deepr.preference.PreferenceRepository
import com.yogeshpaliyal.deepr.update.AppUpdateManager
import com.yogeshpaliyal.deepr.update.AvailableUpdate
import com.yogeshpaliyal.deepr.update.InstallPermissionRequiredException
import com.yogeshpaliyal.deepr.update.UpdateCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Idle)
    val state: StateFlow<AppUpdateUiState> = _state.asStateFlow()

    private val _bannerUpdate = MutableStateFlow<AvailableUpdate?>(null)

    /** Update shown in the home screen banner, unless the user dismissed that version. */
    val bannerUpdate: StateFlow<AvailableUpdate?> = _bannerUpdate.asStateFlow()
    private var bannerCheckStarted = false
    private var updateJob: Job? = null

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

    /** Silent check for the home screen banner; runs once per view model. */
    fun checkForBannerUpdate(activity: Activity) {
        if (bannerCheckStarted) return
        bannerCheckStarted = true
        viewModelScope.launch {
            val result = appUpdateManager.checkForUpdate(activity)
            if (result is UpdateCheckResult.Available) {
                val dismissed = preferenceRepository.getDismissedUpdateVersion.first()
                if (result.update.versionKey != dismissed) {
                    _bannerUpdate.value = result.update
                }
            }
        }
    }

    fun dismissBanner() {
        val update = _bannerUpdate.value ?: return
        _bannerUpdate.value = null
        viewModelScope.launch {
            preferenceRepository.setDismissedUpdateVersion(update.versionKey)
        }
    }

    /** Opens the update dialog for the version shown in the banner. */
    fun showBannerUpdate() {
        val update = _bannerUpdate.value ?: return
        if (_state.value == AppUpdateUiState.Idle) {
            _state.value = AppUpdateUiState.Available(update)
        }
    }

    fun startUpdate(
        activity: Activity,
        update: AvailableUpdate,
    ) {
        _state.value = AppUpdateUiState.Downloading(null)
        updateJob?.cancel()
        updateJob =
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

    /** Stops an in-progress download. */
    fun cancelUpdate() {
        updateJob?.cancel()
        updateJob = null
        _state.value = AppUpdateUiState.Idle
    }

    fun dismiss() {
        if (_state.value is AppUpdateUiState.Available) {
            _state.value = AppUpdateUiState.Idle
        }
    }
}
