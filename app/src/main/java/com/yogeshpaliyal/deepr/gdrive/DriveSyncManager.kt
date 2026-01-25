package com.yogeshpaliyal.deepr.gdrive

import android.content.Intent

/**
 * Interface for Google Drive sync functionality.
 * This allows for different implementations based on build variant.
 */
interface DriveSyncManager {
    /**
     * Whether Google Drive features are available in this build variant.
     */
    val isAvailable: Boolean

    /**
     * Check if the user has authenticated with Google Drive.
     */
    fun isDriveAuthenticated(): Boolean

    /**
     * Get the sign-in intent for Google authentication.
     */
    fun getSignInIntent(): Intent?

    /**
     * Handle the result of sign-in.
     */
    fun handleSignInResult(data: Intent?)

    /**
     * Sign out from Google Drive.
     */
    fun signOut()

    /**
     * Backup data to Google Drive.
     */
    suspend fun backupToDrive(): Boolean

    /**
     * Restore data from Google Drive.
     */
    suspend fun restoreFromDrive(): Boolean

    /**
     * Get information about the backup file.
     */
    suspend fun getBackupFileInfo(): BackupStatus?
}

data class BackupStatus(
    val hasBackup: Boolean = false,
    val lastBackupDate: Long? = null,
)
