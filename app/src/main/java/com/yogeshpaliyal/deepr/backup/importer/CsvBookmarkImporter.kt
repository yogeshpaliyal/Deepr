package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import android.net.Uri
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvException
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.preference.PreferenceRepository
import com.yogeshpaliyal.deepr.util.Constants
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Importer for CSV files containing bookmark data.
 */
class CsvBookmarkImporter(
    private val context: Context,
    private val linkRepository: LinkRepository,
    private val preferenceRepository: PreferenceRepository,
) : BookmarkImporter {
    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
        var skippedCount = 0

        try {
            val defaultProfileId = preferenceRepository.getSelectedProfileId.first()
            val items = mutableListOf<LinkRepository.NewLinkWithTags>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.reader().use { reader ->
                    val customParser =
                        CSVParserBuilder()
                            .build()
                    val csvReader =
                        CSVReaderBuilder(reader)
                            .withCSVParser(customParser)
                            .build()

                    // verify header first
                    val header = csvReader.readNext()
                    if (header == null ||
                        header.size < 3 ||
                        header[0] != Constants.Header.LINK ||
                        header[1] != Constants.Header.CREATED_AT ||
                        header[2] != Constants.Header.OPENED_COUNT
                    ) {
                        return RequestResult.Error("Invalid CSV header")
                    }

                    csvReader.forEach { row ->
                        if (row.size >= 3) {
                            val link = row[0]
                            val createdAt = row[1]
                            val openedCount = row[2].toLongOrNull() ?: 0L
                            val name = row.getOrNull(3) ?: ""
                            val notes = row.getOrNull(4) ?: ""
                            val tagsString = row.getOrNull(5) ?: ""
                            val thumbnail = row.getOrNull(6) ?: ""
                            val isFavourite = row.getOrNull(7)?.toLongOrNull() ?: 0
                            val profileName = row.getOrNull(8)?.trim()?.takeIf { it.isNotBlank() }

                            if (link.isNotBlank()) {
                                val profileId = profileName?.let { linkRepository.getOrCreateProfileByName(it) } ?: defaultProfileId
                                val tagNames =
                                    tagsString
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }

                                items.add(
                                    LinkRepository.NewLinkWithTags(
                                        link = link,
                                        name = name,
                                        notes = notes,
                                        thumbnail = thumbnail,
                                        openedCount = openedCount,
                                        isFavourite = isFavourite,
                                        createdAt = createdAt,
                                        profileId = profileId,
                                        tagNames = tagNames,
                                    ),
                                )
                            } else {
                                skippedCount++
                            }
                        } else {
                            skippedCount++
                        }
                    }
                }
            }

            val results = linkRepository.insertLinksWithTags(items)
            val updatedCount = results.count { it != null }
            skippedCount += results.count { it == null }

            return RequestResult.Success(ImportResult(updatedCount, skippedCount))
        } catch (e: IOException) {
            return RequestResult.Error("Error reading file: ${e.message}")
        } catch (e: CsvException) {
            return RequestResult.Error("Error parsing CSV file: ${e.message}")
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    override fun getDisplayName(): String = "CSV"

    override fun getSupportedMimeTypes(): Array<String> =
        arrayOf(
            "text/csv",
            "text/comma-separated-values",
            "application/csv",
        )
}
