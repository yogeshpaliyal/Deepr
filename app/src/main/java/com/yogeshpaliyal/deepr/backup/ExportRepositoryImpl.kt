package com.yogeshpaliyal.deepr.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.yogeshpaliyal.deepr.DeeprQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : ExportRepository {

    override suspend fun exportToCsv(): String {
        val count = deeprQueries.countDeepr().executeAsOne()
        if (count == 0L) {
            return "No data found in the database to export."
        }
        val dataToExportInCsvFormat = deeprQueries.listDeeprAsc().executeAsList().map {
            CsvSchema(
                id = it.id,
                link = it.link,
                createdAt = it.createdAt,
                openedCount = it.openedCount
            )
        }
        if (dataToExportInCsvFormat.isEmpty()) {
            return "No data available to export after mapping."
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "deepr_export_$timeStamp.csv"

        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/Deepr"
                    )
                }

                val resolver = context.contentResolver
                val uri =
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        writeCsvData(outputStream, dataToExportInCsvFormat)
                    }
                    "Successfully exported to ${Environment.DIRECTORY_DOWNLOADS}/Deepr/$fileName"
                } else {
                    "Failed to create CSV file."
                }
            } else {
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOWNLOADS}/Deepr")

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    writeCsvData(outputStream, dataToExportInCsvFormat)
                }
                "Successfully exported to ${downloadsDir}/${file.absolutePath}"
            }
        }
    }

    private fun writeCsvData(outputStream: OutputStream, data: List<CsvSchema>) {
        outputStream.bufferedWriter().use { writer ->
            // Write Header
            writer.write("Id,Link,CreatedAt,OpenedCount\n")
            // Write Data
            data.forEach { item ->
                val row = "${item.id},${item.link},${item.createdAt},${item.openedCount}\n"
                writer.write(row)
            }
        }
    }
}