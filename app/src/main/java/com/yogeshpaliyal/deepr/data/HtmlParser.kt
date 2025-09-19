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
    fun getTitleAndImageFromHtml(html: String): LinkInfo {
        val doc = Jsoup.parse(html)

        // 1. Try og:title and og:image from <meta>
        val ogTitle = getOgContent(doc, "og:title")
        val ogDescription = getOgContent(doc, "og:description")
        val ogImage = getOgContent(doc, "og:image")

        // 2. Fallback for title: biggest heading in document
        val headingTags = listOf("h1", "h2", "h3", "h4", "h5", "h6")
        val fallbackTitle =
            if (ogTitle.isNullOrBlank()) {
                headingTags.firstNotNullOfOrNull { tag ->
                    doc.selectFirst(tag)?.text()?.takeIf { it.isNotBlank() }
                }
            } else {
                ogTitle
            }

        // 3. Fallback for image: first img in document
        val fallbackImage =
            if (ogImage.isNullOrBlank()) {
                doc.selectFirst("img")?.absUrl("src")?.takeIf { it.isNotBlank() }
            } else {
                ogImage
            }

        return LinkInfo(fallbackTitle, ogDescription, fallbackImage)
    }
}

data class LinkInfo(
    val title: String?,
    val description: String?,
    val image: String?,
)
