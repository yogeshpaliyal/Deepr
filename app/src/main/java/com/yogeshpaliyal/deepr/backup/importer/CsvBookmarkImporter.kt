package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import android.net.Uri
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvException
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.Constants
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Importer for CSV files containing bookmark data.
 */
class CsvBookmarkImporter(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val appPreferenceDataStore: AppPreferenceDataStore,
) : BookmarkImporter {
    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
        var updatedCount = 0
        var skippedCount = 0

        try {
            val defaultProfileId = appPreferenceDataStore.getSelectedProfileId.first()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                inputStream.reader().use { reader ->
                    val customParser =
                        CSVParserBuilder()
                            .build()
                    val csvReader =
                        CSVReaderBuilder(reader)
                            .withCSVParser(customParser)
                            .build()

                    val allRows = csvReader.readAll()
                    var currentSection = ""

                    deeprQueries.transaction {
                        allRows.forEach { row ->
                            if (row.size >= 2 && row[0] == "SECTION") {
                                currentSection = row[1]
                                return@forEach
                            }

                            when (currentSection) {
                                "PROFILES" -> {
                                    if (row.size >= 2 && row[0] != "ProfileName") {
                                        val profileName = row[0]
                                        val priority = row[1].toLongOrNull() ?: 0L
                                        val themeMode = row.getOrNull(2) ?: "system"
                                        val colorTheme = row.getOrNull(3) ?: "dynamic"

                                        val existing = deeprQueries.getProfileByName(profileName).executeAsOneOrNull()
                                        if (existing == null) {
                                            deeprQueries.insertProfileWithPriority(profileName, priority)
                                            val profileId = deeprQueries.lastInsertRowId().executeAsOne()
                                            deeprQueries.updateProfile(profileName, themeMode, colorTheme, profileId)
                                        }
                                    }
                                }

                                "LINKS" -> {
                                    if (row.size >= 3 && row[0] != Constants.Header.LINK) {
                                        val link = row[0]
                                        val createdAt = row[1]
                                        val openedCount = row[2].toLongOrNull() ?: 0L
                                        val name = row.getOrNull(3) ?: ""
                                        val notes = row.getOrNull(4) ?: ""
                                        val tagsString = row.getOrNull(5) ?: ""
                                        val thumbnail = row.getOrNull(6) ?: ""
                                        val isFavourite = row.getOrNull(7)?.toLongOrNull() ?: 0
                                        val profileName = row.getOrNull(8)?.trim()?.takeIf { it.isNotBlank() }

                                        val existing = deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                                        if (link.isNotBlank() && existing == null) {
                                            updatedCount++
                                            val profileID =
                                                profileName?.let {
                                                    val profile = deeprQueries.getProfileByName(it).executeAsOneOrNull()
                                                    if (profile == null) {
                                                        deeprQueries.insertProfileAutoPriority(it)
                                                        deeprQueries.lastInsertRowId().executeAsOneOrNull()
                                                    } else {
                                                        profile.id
                                                    }
                                                } ?: defaultProfileId
                                            deeprQueries.importDeepr(
                                                link = link,
                                                openedCount = openedCount,
                                                name = name,
                                                notes = notes,
                                                thumbnail = thumbnail,
                                                isFavourite = isFavourite,
                                                createdAt = createdAt,
                                                profileId = profileID,
                                            )
                                            val linkId = deeprQueries.lastInsertRowId().executeAsOne()

                                            // Import tags if present
                                            if (tagsString.isNotBlank()) {
                                                val tagNames =
                                                    tagsString
                                                        .split(",")
                                                        .map { it.trim() }
                                                        .filter { it.isNotEmpty() }
                                                tagNames.forEach { tagName ->
                                                    // Insert tag if it doesn't exist
                                                    deeprQueries.insertTag(tagName)
                                                    // Get tag ID and link it
                                                    val tag =
                                                        deeprQueries
                                                            .getTagByName(tagName)
                                                            .executeAsOneOrNull()
                                                    if (tag != null) {
                                                        deeprQueries.addTagToLink(linkId, tag.id)
                                                    }
                                                }
                                            }
                                        } else if (link.isNotBlank()) {
                                            skippedCount++
                                        }
                                    }
                                }
                                // Fallback for old format (without SECTION)
                                "" -> {
                                    if (row.size >= 3 && row[0] != Constants.Header.LINK) {
                                        val link = row[0]
                                        val createdAt = row[1]
                                        val openedCount = row[2].toLongOrNull() ?: 0L
                                        val name = row.getOrNull(3) ?: ""
                                        val notes = row.getOrNull(4) ?: ""
                                        val tagsString = row.getOrNull(5) ?: ""
                                        val thumbnail = row.getOrNull(6) ?: ""
                                        val isFavourite = row.getOrNull(7)?.toLongOrNull() ?: 0
                                        val profileName = row.getOrNull(8)?.trim()?.takeIf { it.isNotBlank() }
                                        val profilePriority = row.getOrNull(9)?.toLongOrNull()

                                        val existing = deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                                        if (link.isNotBlank() && existing == null) {
                                            updatedCount++
                                            val profileID =
                                                profileName?.let {
                                                    val profile = deeprQueries.getProfileByName(it).executeAsOneOrNull()
                                                    if (profile == null) {
                                                        if (profilePriority != null) {
                                                            deeprQueries.insertProfileWithPriority(it, profilePriority)
                                                        } else {
                                                            deeprQueries.insertProfileAutoPriority(it)
                                                        }
                                                        deeprQueries.lastInsertRowId().executeAsOneOrNull()
                                                    } else {
                                                        profile.id
                                                    }
                                                } ?: defaultProfileId
                                            deeprQueries.importDeepr(
                                                link = link,
                                                openedCount = openedCount,
                                                name = name,
                                                notes = notes,
                                                thumbnail = thumbnail,
                                                isFavourite = isFavourite,
                                                createdAt = createdAt,
                                                profileId = profileID,
                                            )
                                            val linkId = deeprQueries.lastInsertRowId().executeAsOne()

                                            // Import tags if present
                                            if (tagsString.isNotBlank()) {
                                                val tagNames =
                                                    tagsString
                                                        .split(",")
                                                        .map { it.trim() }
                                                        .filter { it.isNotEmpty() }
                                                tagNames.forEach { tagName ->
                                                    deeprQueries.insertTag(tagName)
                                                    val tag = deeprQueries.getTagByName(tagName).executeAsOneOrNull()
                                                    if (tag != null) {
                                                        deeprQueries.addTagToLink(linkId, tag.id)
                                                    }
                                                }
                                            }
                                        } else if (link.isNotBlank()) {
                                            skippedCount++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
