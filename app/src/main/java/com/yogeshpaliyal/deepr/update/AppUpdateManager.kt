package com.yogeshpaliyal.deepr.update

import android.app.Activity

/**
 * Checks for and installs newer versions of the app.
 *
 * Each distribution channel has its own implementation, chosen per flavor by
 * [AppUpdateManagerFactory]: Play Store builds use the Google Play In-App Updates API,
 * while GitHub / F-Droid builds check the GitHub releases of the project.
 */
interface AppUpdateManager {
    suspend fun checkForUpdate(activity: Activity): UpdateCheckResult

    /**
     * Starts installing [update]. [onProgress] receives download progress between 0 and 1
     * (or null when the size is unknown) for implementations that download the update themselves.
     */
    suspend fun startUpdate(
        activity: Activity,
        update: AvailableUpdate,
        onProgress: (Float?) -> Unit = {},
    )

    /** Called when the host activity resumes, so an interrupted update can be continued. */
    suspend fun onResume(activity: Activity) {}
}

data class AvailableUpdate(
    /** Stable id of the offered version, used to remember a dismissed update banner. */
    val versionKey: String,
    val versionName: String?,
    val releaseNotes: String? = null,
    val downloadUrl: String? = null,
    val releasePageUrl: String? = null,
)

sealed interface UpdateCheckResult {
    data class Available(
        val update: AvailableUpdate,
    ) : UpdateCheckResult

    data object UpToDate : UpdateCheckResult

    data class Error(
        val throwable: Throwable,
    ) : UpdateCheckResult
}
