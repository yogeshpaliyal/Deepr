package com.yogeshpaliyal.deepr.sync

import android.content.Context
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val preferenceDataStore: AppPreferenceDataStore,
) : SyncRepository {
    override suspend fun syncToMarkdown(): RequestResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val syncEnabled = preferenceDataStore.getSyncEnabled.first()
                if (!syncEnabled) {
                    return@withContext RequestResult.Error(context.getString(R.string.sync_disabled))
                }

                val filePath = preferenceDataStore.getSyncFilePath.first()
                if (filePath.isEmpty()) {
                    return@withContext RequestResult.Error(context.getString(R.string.sync_file_not_selected))
                }

                val res =
                    context.contentResolver.openOutputStream(filePath.toUri(), "wt")?.use {
                        val count = deeprQueries.countDeepr().executeAsOne()
                        if (count == 0L) {
                            return@withContext RequestResult.Error(context.getString(R.string.no_data_to_export))
                        }

                        val dataToSync = deeprQueries.listDeeprAsc().executeAsList()
                        if (dataToSync.isEmpty()) {
                            return@withContext RequestResult.Error(context.getString(R.string.no_data_available_export))
                        }

                        writeMarkdownData(it, dataToSync)
                        // Record sync time on successful completion
                        recordSyncTime()
                        RequestResult.Success(
                            context.getString(
                                R.string.sync_success,
                                filePath,
                            ),
                        )
                    }
                res ?: RequestResult.Error(context.getString(R.string.sync_failed, ""))
            } catch (e: Exception) {
                RequestResult.Error(context.getString(R.string.sync_failed, e.message))
            }
        }
    }

    override suspend fun validateMarkdownFile(filePath: String): RequestResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (filePath.isEmpty()) {
                    return@withContext RequestResult.Error(context.getString(R.string.no_file_path_provided))
                }

                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext RequestResult.Success(true) // File doesn't exist yet, which is valid
                }

                val content = file.readText()
                val isValid = isValidMarkdownTable(content)

                if (isValid) {
                    RequestResult.Success(true)
                } else {
                    RequestResult.Error(context.getString(R.string.invalid_markdown_format))
                }
            } catch (e: Exception) {
                RequestResult.Error(context.getString(R.string.file_validation_error, e.message))
            }
        }
    }

    override suspend fun recordSyncTime() {
        val currentTime = System.currentTimeMillis()
        preferenceDataStore.setLastSyncTime(currentTime)
    }

    private fun writeMarkdownData(
        file: OutputStream,
        data: List<Deepr>,
    ) {
        file.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                // Write header comment with sync time
                val syncTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                writer.write("<!-- Deepr Sync File - Do not modify the table structure -->\n")
                writer.write("<!-- Last Synced: $syncTime -->\n")
                writer.write("# Deeplinks\n\n")
                writer.write("**Last Sync:** $syncTime\n\n")
                writer.write("**Warning:** Please maintain the markdown table format when editing this file.\n\n")

                // Write markdown table header
                writer.write("| Name | Link | Created At | Opened Count |\n")
                writer.write("|------|------|------------|--------------|\n")

                // Write data rows
                data.forEach { item ->
                    val escapedName = item.name.replace("|", "\\|").replace("\n", " ")
                    val escapedLink = item.link.replace("|", "\\|").replace("\n", " ")
                    val row = "| $escapedName | $escapedLink | ${item.createdAt} | ${item.openedCount} |\n"
                    writer.write(row)
                }

                writer.write("\n<!-- End of Deepr Sync Data -->\n")
            }
        }
    }

    private fun isValidMarkdownTable(content: String): Boolean {
        if (content.isEmpty()) return true // Empty file is valid

        val lines = content.lines()
        var foundHeader = false
        var foundSeparator = false

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("| Name | Link | Created At | Opened Count |")) {
                foundHeader = true
            } else if (foundHeader && trimmedLine.startsWith("|------|------|------------|--------------|")) {
                foundSeparator = true
                break
            }
        }

        return foundHeader && foundSeparator
    }
}
