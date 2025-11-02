package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import android.net.Uri
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.backup.LinkImportCandidate
import com.yogeshpaliyal.deepr.util.RequestResult
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import java.io.IOException

/**
 * Importer for text files containing links.
 * Supports both comma-separated and newline-separated links.
 */
class TextFileImporter(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : BookmarkImporter {
    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
        var updatedCount = 0
        var skippedCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.reader().use { reader ->
                    val content = reader.readText()
                    val links = extractLinks(content)

                    links.forEach { link ->
                        val trimmedLink = link.trim()
                        if (trimmedLink.isNotBlank() && isValidDeeplink(trimmedLink)) {
                            val existing = deeprQueries.getDeeprByLink(trimmedLink).executeAsOneOrNull()
                            if (existing == null) {
                                updatedCount++
                                deeprQueries.insertDeepr(
                                    link = trimmedLink,
                                    name = "",
                                    openedCount = 0L,
                                    notes = "",
                                    thumbnail = "",
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

            return RequestResult.Success(ImportResult(updatedCount, skippedCount))
        } catch (e: IOException) {
            return RequestResult.Error("Error reading file: ${e.message}")
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Parse the text file and extract link candidates for preview.
     */
    suspend fun parseForPreview(uri: Uri): RequestResult<List<LinkImportCandidate>> {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.reader().use { reader ->
                    val content = reader.readText()
                    val links = extractLinks(content)

                    val candidates = links.mapNotNull { link ->
                        val trimmedLink = link.trim()
                        if (trimmedLink.isNotBlank()) {
                            val isValid = isValidDeeplink(trimmedLink)
                            val isDuplicate = if (isValid) {
                                deeprQueries.getDeeprByLink(trimmedLink).executeAsOneOrNull() != null
                            } else {
                                false
                            }
                            LinkImportCandidate(
                                link = trimmedLink,
                                isValid = isValid,
                                isDuplicate = isDuplicate,
                                isSelected = isValid && !isDuplicate, // Only select valid, non-duplicate links by default
                            )
                        } else {
                            null
                        }
                    }

                    return RequestResult.Success(candidates)
                }
            }

            return RequestResult.Error("Unable to open file")
        } catch (e: IOException) {
            return RequestResult.Error("Error reading file: ${e.message}")
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Import selected links from the preview.
     */
    suspend fun importSelected(links: List<String>): RequestResult<ImportResult> {
        var updatedCount = 0
        var skippedCount = 0

        try {
            links.forEach { link ->
                val trimmedLink = link.trim()
                if (trimmedLink.isNotBlank() && isValidDeeplink(trimmedLink)) {
                    val existing = deeprQueries.getDeeprByLink(trimmedLink).executeAsOneOrNull()
                    if (existing == null) {
                        updatedCount++
                        deeprQueries.insertDeepr(
                            link = trimmedLink,
                            name = "",
                            openedCount = 0L,
                            notes = "",
                            thumbnail = "",
                        )
                    } else {
                        skippedCount++
                    }
                } else {
                    skippedCount++
                }
            }

            return RequestResult.Success(ImportResult(updatedCount, skippedCount))
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    override fun getDisplayName(): String = "Text File"

    override fun getSupportedMimeTypes(): Array<String> =
        arrayOf(
            "text/plain",
            "text/*",
        )

    /**
     * Extract links from text content.
     * Supports both comma-separated and newline-separated links.
     */
    private fun extractLinks(content: String): List<String> {
        val links = mutableListOf<String>()

        // Split by commas to check if comma-separated format is used
        val commaSeparated = content.split(",").map { it.trim() }

        // Check if comma separation produces valid results
        // We consider it comma-separated if:
        // 1. We have multiple items after split
        // 2. Most items look like they could be links (contain :// or .)
        val isCommaSeparated =
            commaSeparated.size > 1 &&
                commaSeparated.count { it.contains("://") || (it.contains(".") && !it.contains("\n")) } >= commaSeparated.size * 0.5

        if (isCommaSeparated) {
            links.addAll(commaSeparated)
        } else {
            // Otherwise, split by newlines
            links.addAll(content.split("\n").map { it.trim() })
        }

        return links.filter { it.isNotBlank() }
    }
}
