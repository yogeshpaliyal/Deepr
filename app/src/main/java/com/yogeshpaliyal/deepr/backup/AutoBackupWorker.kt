package com.yogeshpaliyal.deepr.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.preference.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AutoBackupWorker(
    val context: Context,
    val linkRepository: LinkRepository,
    val preferenceRepository: PreferenceRepository,
) {
    private val csvWriter by lazy {
        CsvWriter()
    }

    suspend fun doWork() {
        return withContext(Dispatchers.IO) {
            try {
                val enabled = preferenceRepository.getAutoBackupEnabled.first()
                if (!enabled) {
                    return@withContext
                }

                val location = preferenceRepository.getAutoBackupLocation.first()
                if (location.isEmpty()) {
                    return@withContext
                }

                val count = linkRepository.countAllLinks()
                if (count == 0L) {
                    return@withContext
                }

                val dataToExport = linkRepository.getLinksForBackup()
                if (dataToExport.isEmpty()) {
                    return@withContext
                }

                if (!location.startsWith("content://")) {
                    return@withContext
                }

                val success = saveToSelectedLocation(location = location, data = dataToExport)

                if (success) {
                    // Record backup time on successful completion
                    preferenceRepository.setLastBackupTime(System.currentTimeMillis())
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun saveToSelectedLocation(
        location: String,
        fileName: String = "deepr_backup.csv",
        data: List<GetLinksForBackup>,
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
                        csvWriter.writeToCsv(outputStream, data)
                    }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
}
