package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import kotlinx.coroutines.flow.first
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Importer for Mozilla Firefox bookmarks HTML format.
 */
class MozillaBookmarkImporter(
    context: Context,
    deeprQueries: DeeprQueries,
    appPreferenceDataStore: AppPreferenceDataStore,
) : HtmlBookmarkImporter(context, deeprQueries, appPreferenceDataStore) {
    override fun getDisplayName(): String = "Mozilla/Firefox Bookmarks"

    override suspend fun extractBookmarks(document: Document): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()

        // Firefox bookmarks use <a> tags
        val links = document.select("a[href]")

        links.forEach { link ->
            val url = link.attr("href")
            val title = link.text()
            val folder = extractMozillaFolder(link)

            // Firefox-specific attributes
            val addDate = link.attr("add_date")
            val lastModified = link.attr("last_modified")
            val shortcutUrl = link.attr("shortcuturl")
            val tags = link.attr("tags")
            val profileId = appPreferenceDataStore.getSelectedProfileId.first()
            if (url.isNotBlank() && !url.startsWith("place:")) {
                val tagList =
                    if (tags.isNotBlank()) {
                        tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        null
                    }

                bookmarks.add(
                    Bookmark(
                        url = url,
                        title = title.ifBlank { url },
                        folder = folder,
                        tags = tagList,
                        profileId = profileId,
                    ),
                )
            }
        }

        return bookmarks
    }

    private fun extractMozillaFolder(element: Element): String? {
        val folders = mutableListOf<String>()
        var current = element.parent()

        while (current != null) {
            // In Firefox HTML export, folders are <h3> elements
            if (current.tagName() == "dl") {
                val h3 = current.previousElementSibling()
                if (h3?.tagName() == "h3") {
                    val folderName = h3.text()
                    // Skip special Firefox folders
                    if (folderName !in listOf("", "Bookmarks Menu", "Bookmarks Toolbar", "Other Bookmarks")) {
                        folders.add(folderName)
                    }
                }
            }
            current = current.parent()
        }

        return if (folders.isNotEmpty()) folders.reversed().joinToString(" / ") else null
    }
}
