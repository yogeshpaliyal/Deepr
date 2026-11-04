package com.yogeshpaliyal.deepr.update

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory as PlayAppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo

/**
 * Uses the Google Play In-App Updates API with the immediate (full screen) flow:
 * Play downloads and installs the update and restarts the app.
 */
class PlayStoreAppUpdateManager(
    context: Context,
) : AppUpdateManager {
    private val playUpdateManager = PlayAppUpdateManagerFactory.create(context.applicationContext)

    override suspend fun checkForUpdate(activity: Activity): UpdateCheckResult =
        try {
            val info = playUpdateManager.requestAppUpdateInfo()
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                UpdateCheckResult.Available(AvailableUpdate(versionName = null))
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e)
        }

    override suspend fun startUpdate(
        activity: Activity,
        update: AvailableUpdate,
        onProgress: (Float?) -> Unit,
    ) {
        // An AppUpdateInfo can only start one flow, so always request a fresh one.
        val info = playUpdateManager.requestAppUpdateInfo()
        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
            info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            playUpdateManager.startUpdateFlow(
                info,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
            )
        }
    }

    override suspend fun onResume(activity: Activity) {
        // Resume an immediate update the user left before it finished.
        try {
            val info = playUpdateManager.requestAppUpdateInfo()
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                playUpdateManager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                )
            }
        } catch (_: Exception) {
            // Not installed from Play, or Play is unavailable.
        }
    }
}
