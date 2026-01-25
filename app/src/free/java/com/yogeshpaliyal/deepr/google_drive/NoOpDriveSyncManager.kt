package com.yogeshpaliyal.deepr.google_drive

import android.content.Intent

/**
 * No-op implementation of DriveSyncManager for free flavor.
 * Google Drive features are not available in this build variant.
 */
class NoOpDriveSyncManager : DriveSyncManager {
    override val isAvailable: Boolean = false

    override fun isDriveAuthenticated(): Boolean = false

    override fun getSignInIntent(): Intent? = null

    override fun handleSignInResult(data: Intent?) {
        // No-op
    }

    override fun signOut() {
        // No-op
    }

    override suspend fun backupToDrive(): Boolean = false

    override suspend fun restoreFromDrive(): Boolean = false

    override suspend fun getBackupFileInfo(): BackupStatus? = null
}
