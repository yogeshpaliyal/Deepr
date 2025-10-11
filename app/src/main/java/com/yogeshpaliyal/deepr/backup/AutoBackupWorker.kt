package com.yogeshpaliyal.deepr.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
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

                val dataToExport = deeprQueries.listDeeprAsc().executeAsList()
                if (dataToExport.isEmpty()) {
                    return@withContext
                }

                if (!location.startsWith("content://")) {
                    return@withContext
                }

                val fileName = "deepr_backup.csv"

                val success = saveToSelectedLocation(location, fileName, dataToExport)

                if (success) {
                    // Record backup time on successful completion
                    preferenceDataStore.setLastBackupTime(System.currentTimeMillis())
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun saveToSelectedLocation(
        location: String,
        fileName: String,
        data: List<com.yogeshpaliyal.deepr.Deepr>,
    ): Boolean =
        try {
            // For content:// URIs from document picker, create a new document in that folder
            val locationUri = location.toUri()
            val documentFile =
                DocumentFile.fromTreeUri(
                    context,
                    locationUri,
                )

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
                        csvWriter.writeToCsv(outputStream, data)
                    }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
}
