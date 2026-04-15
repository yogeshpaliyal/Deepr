package com.yogeshpaliyal.deepr.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AutoBackupWorker(
    val context: Context,
    val deeprQueries: DeeprQueries,
    val preferenceDataStore: AppPreferenceDataStore,
) {
    private val csvWriter by lazy {
        CsvWriter()
    }

    private suspend fun collectSettings(): Map<String, String> {
        val settings = mutableMapOf<String, String>()
        settings[Constants.Settings.SORTING_ORDER] = preferenceDataStore.getSortingOrder.first()
        settings[Constants.Settings.VIEW_TYPE] = preferenceDataStore.viewType.first().toString()
        settings[Constants.Settings.USE_LINK_BASED_ICONS] = preferenceDataStore.getUseLinkBasedIcons.first().toString()
        settings[Constants.Settings.DEFAULT_PAGE_FAVOURITES] = preferenceDataStore.getDefaultPageFavourites.first().toString()
        settings[Constants.Settings.IS_THUMBNAIL_ENABLE] = preferenceDataStore.isThumbnailEnable.first().toString()
        settings[Constants.Settings.THEME_MODE] = preferenceDataStore.getThemeMode.first()
        settings[Constants.Settings.SHOW_OPEN_COUNTER] = preferenceDataStore.getShowOpenCounter.first().toString()
        settings[Constants.Settings.CLIPBOARD_LINK_DETECTION_ENABLED] = preferenceDataStore.getClipboardLinkDetectionEnabled.first().toString()
        return settings
    }

    suspend fun doWork() {
        return withContext(Dispatchers.IO) {
            try {
                val enabled = preferenceDataStore.getAutoBackupEnabled.first()
                if (!enabled) {
                    return@withContext
                }

                val location = preferenceDataStore.getAutoBackupLocation.first()
                if (location.isEmpty()) {
                    return@withContext
                }

                val count = deeprQueries.countDeepr().executeAsOne()
                if (count == 0L) {
                    return@withContext
                }

                val dataToExport = deeprQueries.getLinksForBackup().executeAsList()
                if (dataToExport.isEmpty()) {
                    return@withContext
                }

                if (!location.startsWith("content://")) {
                    return@withContext
                }

                val settings = collectSettings()
                val success = saveToSelectedLocation(location = location, data = dataToExport, settings = settings)

                if (success) {
                    // Record backup time on successful completion
                    preferenceDataStore.setLastBackupTime(System.currentTimeMillis())
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun saveToSelectedLocation(
        location: String,
        fileName: String = "deepr_backup.csv",
        data: List<GetLinksForBackup>,
        settings: Map<String, String> = emptyMap(),
    ): Boolean =
        try {
            // For content:// URIs from document picker, create a new document in that folder
            val locationUri = location.toUri()
            val directory = DocumentFile.fromTreeUri(context, locationUri)
            var docFile = directory?.findFile(fileName)
            if (docFile == null) {
                docFile =
                    DocumentFile.fromTreeUri(context, locationUri)?.createFile(
                        "text/csv",
                        fileName,
                    )
            }

            if (docFile != null) {
                context.contentResolver
                    .openOutputStream(docFile.uri, "wt")
                    ?.use { outputStream ->
                        csvWriter.writeToCsv(outputStream, data, settings)
                    }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
}
