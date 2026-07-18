package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import android.net.Uri
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.preference.PreferenceRepository
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.flow.firstOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

/**
 * Base class for importing bookmarks from HTML files.
 * This can be extended to support browser-specific HTML formats (Chrome, Firefox, etc.)
 */
abstract class HtmlBookmarkImporter(
    protected val context: Context,
    protected val linkRepository: LinkRepository,
    protected val preferenceRepository: PreferenceRepository,
) : BookmarkImporter {
    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
        var importedCount = 0
        var skippedCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = Jsoup.parse(inputStream, "UTF-8", "")
                val bookmarks = extractBookmarks(document)
                val profileId = preferenceRepository.getSelectedProfileId.firstOrNull() ?: 1L

                val validBookmarks = bookmarks.filter { it.url.isNotBlank() }
                skippedCount = bookmarks.size - validBookmarks.size

                val items =
                    validBookmarks.map { bookmark ->
                        LinkRepository.NewLinkWithTags(
                            link = bookmark.url,
                            name = bookmark.title,
                            notes = bookmark.folder ?: "",
                            profileId = profileId,
                            tagNames = bookmark.tags ?: emptyList(),
                        )
                    }
                val results = linkRepository.insertLinksWithTags(items)
                importedCount = results.count { it != null }
                skippedCount += results.count { it == null }
            }

            return RequestResult.Success(ImportResult(importedCount, skippedCount))
        } catch (e: IOException) {
            return RequestResult.Error("Error reading file: ${e.message}")
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Extract bookmarks from the HTML document.
     * Subclasses can override this to handle browser-specific formats.
     */
    protected open suspend fun extractBookmarks(document: Document): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val links = document.select("a[href]")

        links.forEach { link ->
            val url = link.attr("href")
            val title = link.text()
            val folder = extractFolder(link)

            if (url.isNotBlank()) {
                bookmarks.add(
                    Bookmark(
                        url = url,
                        title = title.ifBlank { url },
                        folder = folder,
                    ),
                )
            }
        }

        return bookmarks
    }

    /**
     * Extract the folder path for a bookmark.
     * Can be overridden by subclasses for browser-specific folder extraction.
     */
    protected open fun extractFolder(element: Element): String? {
        var current = element.parent()
        val folders = mutableListOf<String>()

        while (current != null) {
            if (current.tagName() == "dt") {
                val h3 = current.previousElementSibling()
                if (h3?.tagName() == "h3") {
                    folders.add(h3.text())
                }
            }
            current = current.parent()
        }

        return if (folders.isNotEmpty()) folders.reversed().joinToString(" / ") else null
    }

    override fun getSupportedMimeTypes(): Array<String> =
        arrayOf(
            "text/html",
            "application/xhtml+xml",
        )

    /**
     * Data class representing a bookmark.
     */
    protected data class Bookmark(
        val url: String,
        val title: String,
        val folder: String? = null,
        val tags: List<String>? = null,
        val profileId: Long? = null,
    )
}
