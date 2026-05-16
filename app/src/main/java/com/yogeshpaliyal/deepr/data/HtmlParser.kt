package com.yogeshpaliyal.deepr.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlParser {
    // Get Open Graph content (og:title, og:image)
    private fun getOgContent(
        doc: Document,
        property: String,
    ): String? {
        // Try both property and name attributes for compatibility
        return doc.selectFirst("meta[property=$property]")?.attr("content")
            ?: doc.selectFirst("meta[name=$property]")?.attr("content")
    }

    // Get title and image from Open Graph, or fallback as described
    fun getTitleAndImageFromHtml(
        html: String,
        url: String? = null,
    ): LinkInfo {
        val doc = if (url != null) Jsoup.parse(html, url) else Jsoup.parse(html)

        // 1. Try <title> first (often cleaner on Reddit/social)
        val rawDocTitle = doc.title().takeIf { it.isNotBlank() }
        val docTitle = rawDocTitle?.let { cleanTitle(it) }

        // 2. Try og:title and twitter:title from <meta>
        val ogTitle = (getOgContent(doc, "og:title") ?: getOgContent(doc, "twitter:title"))?.let { cleanTitle(it) }
        val ogDescription = getOgContent(doc, "og:description")
            ?: getOgContent(doc, "twitter:description")
            ?: getOgContent(doc, "description")
        val ogImage = getOgContent(doc, "og:image") ?: getOgContent(doc, "twitter:image")

        // 3. Final choice for title
        val title = docTitle ?: ogTitle ?: run {
            val headingTags = listOf("h1", "h2", "h3", "h4", "h5", "h6")
            headingTags.firstNotNullOfOrNull { tag ->
                doc.selectFirst(tag)?.text()?.takeIf { it.isNotBlank() }
            }
        }

        // 4. Fallback for image: first img in document
        val fallbackImage =
            if (ogImage.isNullOrBlank()) {
                doc.selectFirst("img")?.absUrl("src")?.takeIf { it.isNotBlank() }
            } else {
                ogImage
            }

        return LinkInfo(title, ogDescription, fallbackImage)
    }

    private fun cleanTitle(title: String): String {
        val prefixes = listOf("From the Android community on Reddit: ", "From the community on Reddit: ")
        var cleaned = title
        for (prefix in prefixes) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length)
            }
        }

        val suffixes = listOf(" : r/Android", " : Reddit", " - Reddit", " | Reddit")
        for (suffix in suffixes) {
            if (cleaned.endsWith(suffix, ignoreCase = true)) {
                cleaned = cleaned.substring(0, cleaned.length - suffix.length)
            }
        }
        return cleaned.trim()
    }
}

data class LinkInfo(
    val title: String?,
    val description: String?,
    val image: String?,
)
