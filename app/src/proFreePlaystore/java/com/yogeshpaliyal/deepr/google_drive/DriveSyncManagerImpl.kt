package com.yogeshpaliyal.deepr.google_drive

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.DeeprQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.google.api.services.drive.model.File as DriveFile

private const val APP_NAME = "Deepr"
private const val BACKUP_FILE_NAME = "deepr_backup.json"
private const val BACKUP_VERSION = 1
private const val TAG = "DriveSyncManagerImpl"

class DriveSyncManagerImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : DriveSyncManager {
    override val isAvailable: Boolean = true

    override fun isDriveAuthenticated(): Boolean = getSignedInAccount() != null

    override fun getSignInIntent(): Intent = getSignInClient().signInIntent

    override fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed", e)
        }
    }

    override fun signOut() {
        getSignInClient().signOut()
    }

    override suspend fun backupToDrive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isDriveAuthenticated()) {
                    Log.e(TAG, "Backup failed: No Drive permissions")
                    return@withContext false
                }

                val driveService = getGoogleDrive() ?: return@withContext false

                updateBackupFile(driveService)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                false
            }
        }
    }

    override suspend fun restoreFromDrive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isDriveAuthenticated()) {
                    Log.e(TAG, "Restore failed: No Drive permissions")
                    return@withContext false
                }

                val driveService = getGoogleDrive() ?: return@withContext false

                val file = getDriveBackupFileInfo(driveService)

                if (file?.id != null) {
                    @OptIn(ExperimentalSerializationApi::class)
                    val backedUpData =
                        Json.decodeFromStream<BackupData>(
                            driveService.files().get(file.id).executeMediaAsInputStream(),
                        )

                    // Clear existing data
                    deeprQueries.transaction {
                        deeprQueries.deleteAllProfiles()
                        deeprQueries.deleteAllTags()
                    }

                    // Restore profiles
                    backedUpData.profiles.forEach { profileBackup ->
                        deeprQueries.insertProfile(profileBackup.name)
                    }

                    val profileNameToIdMap =
                        deeprQueries.getAllProfiles().executeAsList().associate { it.name to it.id }

                    // Restore links
                    backedUpData.links.forEach { linkBackup ->
                        val profileId = profileNameToIdMap[linkBackup.profileName]

                        if (profileId != null) {
                            deeprQueries.importDeepr(
                                link = linkBackup.link,
                                name = linkBackup.name,
                                createdAt = linkBackup.createdAt,
                                openedCount = linkBackup.openedCount,
                                isFavourite = if (linkBackup.isFavourite) 1L else 0L,
                                notes = linkBackup.notes,
                                thumbnail = linkBackup.thumbnail,
                                profileId = profileId,
                            )
                            val linkId = deeprQueries.lastInsertRowId().executeAsOne()

                            linkBackup.tags.forEach { tagName ->
                                deeprQueries.insertTag(tagName)
                                val tagId = deeprQueries.getTagByName(tagName).executeAsOneOrNull()?.id
                                if (tagId != null) {
                                    deeprQueries.addTagToLink(linkId, tagId)
                                }
                            }
                        }
                    }
                    true
                } else {
                    Log.w(TAG, "No backup file found for restore")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                false
            }
        }
    }

    override suspend fun getBackupFileInfo(): BackupStatus? {
        return withContext(Dispatchers.IO) {
            try {
                if (!isDriveAuthenticated()) {
                    Log.w(TAG, "Cannot get backup info: No Drive permissions")
                    return@withContext null
                }

                val driveService = getGoogleDrive() ?: return@withContext null
                val driveFile = getDriveBackupFileInfo(driveService)
                val lastBackupDate = driveFile?.modifiedTime?.value?.let { Instant.ofEpochMilli(it) }
                BackupStatus(hasBackup = driveFile != null, lastBackupDate = lastBackupDate)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get backup file info", e)
                null
            }
        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val signInOptions =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    private fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    private fun getGoogleDrive(): Drive? {
        val account = getSignedInAccount() ?: return null
        val credential =
            GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE),
            )
        credential.selectedAccount = account.account
        return Drive
            .Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                setHttpTimeout(credential),
            ).setApplicationName(APP_NAME)
            .build()
    }

    private fun setHttpTimeout(requestInitializer: HttpRequestInitializer): HttpRequestInitializer =
        HttpRequestInitializer { httpRequest ->
            requestInitializer.initialize(httpRequest)
            httpRequest.setConnectTimeout(3 * 60000)
            httpRequest.setReadTimeout(3 * 60000)
        }

    private suspend fun updateBackupFile(driveService: Drive) {
        val backupFile = createBackupFile()
        val driveFile = DriveFile()
        driveFile.name = BACKUP_FILE_NAME
        val fileContent = FileContent("application/json", backupFile)

        val existingFile = getDriveBackupFileInfo(driveService)
        if (existingFile != null && existingFile.id != null) {
            driveService.files().update(existingFile.id, driveFile, fileContent).execute()
            Log.d(TAG, "Backup updated successfully")
        } else {
            driveService
                .files()
                .create(driveFile, fileContent)
                .setFields("id")
                .execute()
            Log.d(TAG, "Backup created successfully")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createBackupFile(): File {
        val profiles =
            deeprQueries.getAllProfiles().executeAsList().map {
                ProfileBackup(it.name, it.createdAt)
            }
        val links =
            deeprQueries.getLinksForBackup().executeAsList().map {
                LinkBackup(
                    link = it.link,
                    name = it.name,
                    createdAt = it.createdAt,
                    openedCount = it.openedCount,
                    isFavourite = it.isFavourite == 1L,
                    notes = it.notes,
                    thumbnail = it.thumbnail,
                    profileName = it.profileName,
                    lastOpenedAt = it.lastOpenedAt,
                    tags = it.tags?.split(", ")?.filter { tag -> tag.isNotBlank() } ?: emptyList(),
                )
            }

        val file = File(context.cacheDir, BACKUP_FILE_NAME)
        val backupData =
            BackupData(
                version = BACKUP_VERSION,
                appVersionCode = BuildConfig.VERSION_CODE,
                backupDate =
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now()),
                profiles = profiles,
                links = links,
            )

        Json.encodeToStream(backupData, file.outputStream())
        return file
    }

    private suspend fun getDriveBackupFileInfo(service: Drive): DriveFile? =
        withContext(Dispatchers.IO) {
            val result =
                service
                    .files()
                    .list()
                    .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name, modifiedTime, size, createdTime)")
                    .setOrderBy("modifiedTime desc")
                    .execute()
            result.files?.firstOrNull()
        }
}

@Serializable
data class BackupData(
    val version: Int,
    val appVersionCode: Int,
    val backupDate: String,
    val profiles: List<ProfileBackup>,
    val links: List<LinkBackup>,
)

@Serializable
data class ProfileBackup(
    val name: String,
    val createdAt: String,
)

@Serializable
data class LinkBackup(
    val link: String,
    val name: String,
    val createdAt: String,
    val openedCount: Long,
    val isFavourite: Boolean,
    val notes: String,
    val thumbnail: String,
    val profileName: String,
    val lastOpenedAt: String?,
    val tags: List<String>,
)
