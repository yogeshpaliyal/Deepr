package com.yogeshpaliyal.deepr.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.play.core.appupdate.AppUpdateManagerFactory as PlayAppUpdateManagerFactory

/**
 * Uses the Google Play In-App Updates API with the immediate (full screen) flow:
 * Play downloads and installs the update and restarts the app. When Play reports an
 * update but does not allow the immediate flow, the Play Store listing is opened instead.
 */
class PlayStoreAppUpdateManager(
    context: Context,
) : AppUpdateManager {
    private val playUpdateManager = PlayAppUpdateManagerFactory.create(context.applicationContext)

    override suspend fun checkForUpdate(activity: Activity): UpdateCheckResult =
        try {
            val info = playUpdateManager.requestAppUpdateInfo()
            when (info.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE,
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
                -> UpdateCheckResult.Available(
                    AvailableUpdate(
                        versionKey = info.availableVersionCode().toString(),
                        versionName = null,
                    ),
                )

                else -> UpdateCheckResult.UpToDate
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
        val canRunImmediate =
            info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ||
                (
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                )
        if (!canRunImmediate) {
            openPlayStoreListing(activity)
            return
        }
        when (startImmediateFlow(activity, info).await()) {
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> throw IOException("In-app update failed")
            // RESULT_OK: Play restarts the app. RESULT_CANCELED: the user backed out.
            else -> Unit
        }
    }

    override suspend fun onResume(activity: Activity) {
        // Resume an immediate update the user left before it finished.
        try {
            val info = playUpdateManager.requestAppUpdateInfo()
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateFlow(activity, info)
            }
        } catch (_: Exception) {
            // Not installed from Play, or Play is unavailable.
        }
    }

    private fun startImmediateFlow(
        activity: Activity,
        info: AppUpdateInfo,
    ): Task<Int> =
        playUpdateManager.startUpdateFlow(
            info,
            activity,
            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
        )

    private fun openPlayStoreListing(activity: Activity) {
        val packageName = activity.packageName
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
        } catch (_: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri(),
                ),
            )
        }
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { continuation.resume(it) }
            addOnFailureListener { continuation.resumeWithException(it) }
        }
}
