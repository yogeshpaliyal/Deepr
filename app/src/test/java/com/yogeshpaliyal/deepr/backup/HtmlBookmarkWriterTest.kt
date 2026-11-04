package com.yogeshpaliyal.deepr.backup

import com.yogeshpaliyal.deepr.GetLinksForBackup
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlBookmarkWriterTest {
    private val writer = HtmlBookmarkWriter()

    private fun link(
        id: Long,
        link: String,
        name: String = "",
        notes: String = "",
        tags: String? = null,
        profileName: String = "Default",
        createdAt: String = "2024-01-02 03:04:05",
        lastOpenedAt: String? = null,
    ) = GetLinksForBackup(
        id = id,
        link = link,
        name = name,
        createdAt = createdAt,
        openedCount = 0,
        isFavourite = 0,
        notes = notes,
        thumbnail = "",
        profileId = 1,
        profileName = profileName,
        lastOpenedAt = lastOpenedAt,
        tags = tags,
    )

    @Test
    fun build_writesNetscapeHeader() {
        val html = writer.build(listOf(link(1, "https://example.com")))
        assertTrue(html.startsWith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
    }

    @Test
    fun build_groupsByProfileAndKeepsMetadata() {
        val html =
            writer.build(
                listOf(
                    link(1, "https://a.com", name = "A", tags = "news, tech", notes = "note a"),
                    link(2, "app://deep/link", profileName = "Work", lastOpenedAt = "2024-01-02 03:04:06"),
                ),
            )
        val doc = Jsoup.parse(html)

        val folders = doc.select("dt > h3").map { it.text() }
        assertEquals(listOf("Default", "Work"), folders)

        val anchors = doc.select("dt > a[href]")
        assertEquals(2, anchors.size)
        assertEquals("https://a.com", anchors[0].attr("href"))
        assertEquals("A", anchors[0].text())
        assertEquals("news,tech", anchors[0].attr("tags"))
        assertEquals("1704164645", anchors[0].attr("add_date"))
        assertTrue(html.contains("<DD>note a"))

        // Unnamed links fall back to the URL as the title
        assertEquals("app://deep/link", anchors[1].text())
        assertEquals("1704164646", anchors[1].attr("last_visit"))
        assertFalse(anchors[1].hasAttr("tags"))
    }

    @Test
    fun build_escapesHtml() {
        val html =
            writer.build(
                listOf(link(1, "https://a.com/?q=1&r=\"2\"", name = "<b>Bold</b>")),
            )
        assertTrue(html.contains("HREF=\"https://a.com/?q=1&amp;r=&quot;2&quot;\""))
        assertTrue(html.contains("&lt;b&gt;Bold&lt;/b&gt;"))

        val anchor = Jsoup.parse(html).selectFirst("a[href]")!!
        assertEquals("https://a.com/?q=1&r=\"2\"", anchor.attr("href"))
        assertEquals("<b>Bold</b>", anchor.text())
    }
}
