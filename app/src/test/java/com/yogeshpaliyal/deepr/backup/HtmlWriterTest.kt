package com.yogeshpaliyal.deepr.backup

import com.yogeshpaliyal.deepr.ListDeeprWithTagsAsc
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

/**
 * Unit tests for HtmlWriter class
 */
class HtmlWriterTest {
    @Test
    fun htmlWriter_generatesValidHtmlStructure() {
        // Given
        val htmlWriter = HtmlWriter()
        val testData = listOf(
            createTestLink(
                link = "https://example.com",
                name = "Example Site",
                createdAt = "2024-01-01 10:00:00",
                tagsNames = "work,important"
            )
        )
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, testData)
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should contain DOCTYPE", result.contains("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue("Should contain HTML title", result.contains("<TITLE>Bookmarks</TITLE>"))
        assertTrue("Should contain H1 header", result.contains("<H1>Bookmarks</H1>"))
        assertTrue("Should contain link", result.contains("https://example.com"))
        assertTrue("Should contain link title", result.contains("Example Site"))
    }

    @Test
    fun htmlWriter_handlesEmptyData() {
        // Given
        val htmlWriter = HtmlWriter()
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, emptyList())
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should contain DOCTYPE even with empty data", result.contains("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue("Should contain closing tags", result.contains("</DL>"))
    }

    @Test
    fun htmlWriter_groupsLinksByTags() {
        // Given
        val htmlWriter = HtmlWriter()
        val testData = listOf(
            createTestLink(
                link = "https://work.com",
                name = "Work Site",
                tagsNames = "work"
            ),
            createTestLink(
                link = "https://personal.com",
                name = "Personal Site",
                tagsNames = "personal"
            )
        )
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, testData)
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should contain work tag folder", result.contains("<H3>work</H3>"))
        assertTrue("Should contain personal tag folder", result.contains("<H3>personal</H3>"))
    }

    @Test
    fun htmlWriter_escapesHtmlCharacters() {
        // Given
        val htmlWriter = HtmlWriter()
        val testData = listOf(
            createTestLink(
                link = "https://example.com?param=value&other=test",
                name = "Site with <special> & \"characters\"",
                notes = "Notes with <html> tags & \"quotes\""
            )
        )
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, testData)
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should escape ampersands in URL", result.contains("&amp;"))
        assertTrue("Should escape < character", result.contains("&lt;"))
        assertTrue("Should escape > character", result.contains("&gt;"))
        assertTrue("Should escape quotes", result.contains("&quot;"))
    }

    @Test
    fun htmlWriter_includesNotesAsDescription() {
        // Given
        val htmlWriter = HtmlWriter()
        val testData = listOf(
            createTestLink(
                link = "https://example.com",
                name = "Example",
                notes = "This is a test note"
            )
        )
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, testData)
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should include notes as DD element", result.contains("<DD>"))
        assertTrue("Should contain note content", result.contains("This is a test note"))
    }

    @Test
    fun htmlWriter_handlesFavouriteLinks() {
        // Given
        val htmlWriter = HtmlWriter()
        val testData = listOf(
            createTestLink(
                link = "https://favourite.com",
                name = "Favourite Site",
                isFavourite = true
            )
        )
        val outputStream = ByteArrayOutputStream()

        // When
        htmlWriter.writeToHtml(outputStream, testData)
        val result = outputStream.toString("UTF-8")

        // Then
        assertTrue("Should mark favourite with icon", result.contains("ICON=\"★\""))
    }

    private fun createTestLink(
        link: String,
        name: String = link,
        createdAt: String = "2024-01-01 10:00:00",
        openedCount: Long = 0,
        notes: String? = null,
        tagsNames: String? = null,
        thumbnail: String? = null,
        isFavourite: Boolean = false
    ): ListDeeprWithTagsAsc {
        return ListDeeprWithTagsAsc(
            id = 1L,
            link = link,
            name = name,
            createdAt = createdAt,
            openedCount = openedCount,
            notes = notes,
            tagsNames = tagsNames,
            thumbnail = thumbnail,
            isFavourite = isFavourite,
            lastOpened = null,
            profileId = 1L
        )
    }
}
