package com.yogeshpaliyal.deepr.backup.importer

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import kotlinx.coroutines.flow.singleOrNull
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Importer for Chrome/Chromium browser bookmarks HTML format.
 */
class ChromeBookmarkImporter(
    context: Context,
    deeprQueries: DeeprQueries,
    appPreferenceDataStore: AppPreferenceDataStore,
) : HtmlBookmarkImporter(context, deeprQueries, appPreferenceDataStore) {
    override fun getDisplayName(): String = "Chrome Bookmarks"

    override suspend fun extractBookmarks(document: Document): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()

        // Chrome bookmarks use <a> tags inside <dt> elements
        val links = document.select("dt > a[href]")
        val profileId = appPreferenceDataStore.getSelectedProfileId.singleOrNull() ?: 1L
        links.forEach { link ->
            val url = link.attr("href")
            val title = link.text()
            val folder = extractChromeFolder(link)
            val addDate = link.attr("add_date")
            val tags = link.attr("tags")

            if (url.isNotBlank()) {
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

    private fun extractChromeFolder(element: Element): String? {
        val folders = mutableListOf<String>()
        var current = element.parent()

        while (current != null) {
            // Look for <h3> tags which represent folder names in Chrome bookmarks
            val h3 = current.selectFirst("h3")
            if (h3 != null && current.tagName() == "dt") {
                folders.add(h3.text())
            }

            // Move up the tree
            current = current.parent()

            // Stop at the root bookmark folder
            if (current?.tagName() == "dl" && current.parent()?.tagName() == "html") {
                break
            }
        }

        return if (folders.isNotEmpty()) folders.reversed().joinToString(" / ") else null
    }
}
