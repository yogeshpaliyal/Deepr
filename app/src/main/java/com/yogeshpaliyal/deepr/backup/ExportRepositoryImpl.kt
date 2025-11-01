package com.yogeshpaliyal.deepr.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : ExportRepository {
    private val csvWriter by lazy {
        CsvWriter()
    }

    override suspend fun exportToCsv(uri: Uri?): RequestResult<String> {
        val count = deeprQueries.countDeepr().executeAsOne()
        if (count == 0L) {
            return RequestResult.Error(context.getString(R.string.no_data_to_export))
        }
        val dataToExportInCsvFormat = deeprQueries.listDeeprWithTagsAsc().executeAsList()
        if (dataToExportInCsvFormat.isEmpty()) {
            return RequestResult.Error(context.getString(R.string.no_data_available_export))
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "deepr_export_$timeStamp.csv"

        return withContext(Dispatchers.IO) {
            // If URI is provided, export to that location
            if (uri != null) {
                return@withContext try {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                        csvWriter.writeToCsv(outputStream, dataToExportInCsvFormat)
                    }
                    RequestResult.Success(
                        context.getString(
                            R.string.export_success,
                            uri.toString(),
                        ),
                    )
                } catch (_: Exception) {
                    RequestResult.Error(context.getString(R.string.export_failed))
                }
            }

            // Default behavior: export to Downloads/Deepr folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues =
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOWNLOADS}/Deepr",
                        )
                    }

                val resolver = context.contentResolver
                val defaultUri =
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (defaultUri != null) {
                    resolver.openOutputStream(defaultUri)?.use { outputStream ->
                        csvWriter.writeToCsv(outputStream, dataToExportInCsvFormat)
                    }
                    RequestResult.Success(
                        context.getString(
                            R.string.export_success,
                            "${Environment.DIRECTORY_DOWNLOADS}/Deepr/$fileName",
                        ),
                    )
                } else {
                    RequestResult.Error(context.getString(R.string.export_failed))
                }
            } else {
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOWNLOADS}/Deepr")

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    csvWriter.writeToCsv(outputStream, dataToExportInCsvFormat)
                }
                RequestResult.Success(context.getString(R.string.export_success, file.absolutePath))
            }
        }
    }
}
