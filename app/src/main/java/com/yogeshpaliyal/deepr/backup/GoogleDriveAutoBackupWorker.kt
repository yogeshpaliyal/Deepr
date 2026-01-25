package com.yogeshpaliyal.deepr.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yogeshpaliyal.deepr.gdrive.DriveSyncManager
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for Google Drive auto backup.
 * This worker is triggered when data changes (links, profiles, tags)
 * and performs a backup to Google Drive if auto backup is enabled.
 */
class GoogleDriveAutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams),
    KoinComponent {
    private val preferenceDataStore: AppPreferenceDataStore by inject()
    private val driveSyncManager: DriveSyncManager by inject()

    override suspend fun doWork(): Result {
        return try {
            // Check if auto backup is enabled
            val autoBackupEnabled = preferenceDataStore.getGoogleDriveAutoBackupEnabled.first()
            if (!autoBackupEnabled) {
                return Result.success()
            }

            // Check if user is authenticated with Google Drive
            if (!driveSyncManager.isAvailable || !driveSyncManager.isDriveAuthenticated()) {
                return Result.success()
            }

            // Perform backup
            val success = driveSyncManager.backupToDrive()
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "google_drive_auto_backup"
        private const val DEBOUNCE_DELAY_SECONDS = 30L

        /**
         * Schedule a debounced auto backup.
         * Uses REPLACE policy to ensure only the latest request is executed,
         * with a delay to batch multiple rapid changes together.
         */
        fun scheduleBackup(context: Context) {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<GoogleDriveAutoBackupWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(DEBOUNCE_DELAY_SECONDS, TimeUnit.SECONDS)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }

        /**
         * Cancel any pending auto backup work.
         */
        fun cancelBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
