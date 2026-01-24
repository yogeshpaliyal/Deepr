package com.yogeshpaliyal.deepr.google_drive

import android.content.Context
import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
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

private const val BACKUP_FILE_NAME = "deepr_backup.json"
private const val BACKUP_VERSION = 1
private const val TAG = "DriveSyncService"

class DriveSyncService(
    val context: Context,
    val deeprQueries: DeeprQueries,
) {
    fun checkDrivePermissions(): Boolean = GoogleDriveHelper.isDriveAuthenticated(context)

    suspend fun backupToDrive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!checkDrivePermissions()) {
                    Log.e(TAG, "Backup failed: No Drive permissions")
                    return@withContext false
                }

                val driveService = context.getGoogleDrive() ?: return@withContext false

                updateBackupFile(driveService)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                false
            }
        }
    }

    private suspend fun updateBackupFile(driveService: Drive) {
        val backupFile = createBackupFile()
        val driveFile = DriveFile()
        driveFile.name = BACKUP_FILE_NAME
        val fileContent = FileContent("application/json", backupFile)

        val existingFile = getBackupFileInfo(driveService)
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
                    tags = it.tags?.split(", ")?.filter { it.isNotBlank() } ?: emptyList(),
                )
            }

        val file = File(context.cacheDir, BACKUP_FILE_NAME)
        val backupData =
            BackupData(
                version = BACKUP_VERSION,
                appVersionCode = BuildConfig.VERSION_CODE,
                backupDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.now()),
                profiles = profiles,
                links = links,
            )

        Json.encodeToStream(backupData, file.outputStream())
        return file
    }

    suspend fun getBackupFileInfo(service: Drive? = null): DriveFile? {
        return withContext(Dispatchers.IO) {
            try {
                if (!checkDrivePermissions()) {
                    Log.w(TAG, "Cannot get backup info: No Drive permissions")
                    return@withContext null
                }

                val driveService = service ?: context.getGoogleDrive() ?: return@withContext null
                val result =
                    driveService
                        .files()
                        .list()
                        .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                        .setSpaces("drive")
                        .setFields("files(id, name, modifiedTime, size, createdTime)")
                        .setOrderBy("modifiedTime desc")
                        .execute()
                result.files?.firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get backup file info", e)
                null
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restoreFromDrive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!checkDrivePermissions()) {
                    Log.e(TAG, "Restore failed: No Drive permissions")
                    return@withContext false
                }

                val driveService = context.getGoogleDrive() ?: return@withContext false

                val file = getBackupFileInfo(driveService)

                if (file?.id != null) {
                    val backedUpData =
                        Json.decodeFromStream<BackupData>(
                            driveService.files().get(file.id).executeMediaAsInputStream(),
                        )

                    // Clear existing data
                    deeprQueries.transaction {
                        // Cascading deletes will handle dependent data
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
                    false // File not found
                }
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                false
            }
        }
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

data class BackupStatus(
    val hasBackup: Boolean = false,
    val lastBackupDate: Instant? = null,
)
