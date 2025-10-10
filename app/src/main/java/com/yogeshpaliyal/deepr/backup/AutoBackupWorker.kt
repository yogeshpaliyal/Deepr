package com.yogeshpaliyal.deepr.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val deeprQueries: DeeprQueries by inject()
    private val preferenceDataStore: AppPreferenceDataStore by inject()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val enabled = preferenceDataStore.getAutoBackupEnabled.first()
                if (!enabled) {
                    return@withContext Result.success()
                }

                val location = preferenceDataStore.getAutoBackupLocation.first()
                if (location.isEmpty()) {
                    return@withContext Result.failure()
                }

                val count = deeprQueries.countDeepr().executeAsOne()
                if (count == 0L) {
                    return@withContext Result.success()
                }

                val dataToExport = deeprQueries.listDeeprAsc().executeAsList()
                if (dataToExport.isEmpty()) {
                    return@withContext Result.success()
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "deepr_backup_$timeStamp.csv"

                val success =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveToMediaStore(fileName, dataToExport)
                    } else {
                        saveToExternalStorage(location, fileName, dataToExport)
                    }

                if (success) {
                    // Record backup time on successful completion
                    preferenceDataStore.setLastBackupTime(System.currentTimeMillis())
                    Result.success()
                } else {
                    Result.failure()
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun saveToMediaStore(
        fileName: String,
        data: List<com.yogeshpaliyal.deepr.Deepr>,
    ): Boolean {
        return try {
            val contentValues =
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/Deepr/AutoBackup",
                    )
                }

            val resolver = applicationContext.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    writeCsvData(outputStream, data)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun saveToExternalStorage(
        location: String,
        fileName: String,
        data: List<com.yogeshpaliyal.deepr.Deepr>,
    ): Boolean {
        return try {
            // If location is a URI, use ContentResolver
            if (location.startsWith("content://")) {
                applicationContext.contentResolver.openOutputStream(location.toUri(), "wt")
                    ?.use { outputStream ->
                        writeCsvData(outputStream, data)
                    }
                true
            } else {
                // Fallback to file path
                val downloadsDir = File(location)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    writeCsvData(outputStream, data)
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun writeCsvData(
        outputStream: OutputStream,
        data: List<com.yogeshpaliyal.deepr.Deepr>,
    ) {
        outputStream.bufferedWriter().use { writer ->
            // Write Header
            writer.write(
                "${Constants.Header.LINK},${Constants.Header.CREATED_AT},${Constants.Header.OPENED_COUNT},${Constants.Header.NAME}\n",
            )
            // Write Data
            data.forEach { item ->
                val row = "${item.link},${item.createdAt},${item.openedCount},${item.name}\n"
                writer.write(row)
            }
        }
    }
}
