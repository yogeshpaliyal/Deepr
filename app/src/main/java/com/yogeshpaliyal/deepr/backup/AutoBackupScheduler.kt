package com.yogeshpaliyal.deepr.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AutoBackupScheduler {
    private const val WORK_NAME = "auto_backup_work"

    fun scheduleBackup(
        context: Context,
        intervalMillis: Long,
    ) {
        val intervalMinutes = TimeUnit.MILLISECONDS.toMinutes(intervalMillis)
        
        // WorkManager requires minimum 15 minutes for periodic work
        val adjustedInterval = if (intervalMinutes < 15) 15 else intervalMinutes

        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

        val backupWorkRequest =
            PeriodicWorkRequestBuilder<AutoBackupWorker>(
                adjustedInterval,
                TimeUnit.MINUTES,
            ).setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                backupWorkRequest,
            )
    }

    fun cancelBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
