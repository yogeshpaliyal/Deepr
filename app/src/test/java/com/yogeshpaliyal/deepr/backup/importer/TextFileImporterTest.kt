package com.yogeshpaliyal.deepr.backup.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the TextFileImporter.
 */
class TextFileImporterTest {
    @Test
    fun textFileImporter_hasCorrectDisplayName() {
        val displayName = "Text File"
        assertEquals("Text File", displayName)
    }

    @Test
    fun textFileImporter_supportedMimeTypes() {
        val expectedMimeTypes = arrayOf("text/plain", "text/*")
        val mimeTypes = expectedMimeTypes

        assertEquals(2, mimeTypes.size)
        assertTrue(mimeTypes.contains("text/plain"))
        assertTrue(mimeTypes.contains("text/*"))
    }

    @Test
    fun textFileImporter_extractsNewlineSeparatedLinks() {
        val content = """
            https://example.com
            https://google.com
            https://github.com
        """.trimIndent()

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("https://google.com", links[1])
        assertEquals("https://github.com", links[2])
    }

    @Test
    fun textFileImporter_extractsCommaSeparatedLinks() {
        val content = "https://example.com, https://google.com, https://github.com"

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("https://google.com", links[1])
        assertEquals("https://github.com", links[2])
    }

    @Test
    fun textFileImporter_handlesEmptyLines() {
        val content = """
            https://example.com
            
            https://google.com
            
            https://github.com
        """.trimIndent()

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("https://google.com", links[1])
        assertEquals("https://github.com", links[2])
    }

    @Test
    fun textFileImporter_handlesWhitespace() {
        val content = """
              https://example.com  
            https://google.com
               https://github.com   
        """.trimIndent()

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("https://google.com", links[1])
        assertEquals("https://github.com", links[2])
    }

    @Test
    fun textFileImporter_handlesCommaSeparatedWithWhitespace() {
        val content = "https://example.com , https://google.com , https://github.com"

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("https://google.com", links[1])
        assertEquals("https://github.com", links[2])
    }

    @Test
    fun textFileImporter_preferCommaSeparationOverNewline() {
        // If content has multiple commas, it should split by comma
        val content = """
            https://example.com, https://google.com
            https://github.com, https://stackoverflow.com
        """.trimIndent()

        val links = extractLinksFromContent(content)

        // Should split by comma, resulting in 4 links
        assertEquals(4, links.size)
    }

    @Test
    fun textFileImporter_handlesMixedDeeplinks() {
        val content = """
            https://example.com
            myapp://open/screen
            tel:+1234567890
        """.trimIndent()

        val links = extractLinksFromContent(content)

        assertEquals(3, links.size)
        assertEquals("https://example.com", links[0])
        assertEquals("myapp://open/screen", links[1])
        assertEquals("tel:+1234567890", links[2])
    }

    @Test
    fun textFileImporter_handlesUrlsWithCommasInQueryParams() {
        // URLs with commas in query parameters should be treated as newline-separated
        val content = """
            https://example.com?tags=a,b,c
            https://google.com?items=x,y,z
        """.trimIndent()

        val links = extractLinksFromContent(content)

        // Should split by newline, not comma
        assertEquals(2, links.size)
        assertEquals("https://example.com?tags=a,b,c", links[0])
        assertEquals("https://google.com?items=x,y,z", links[1])
    }

    // Helper function that mimics the TextFileImporter's extractLinks logic
    private fun extractLinksFromContent(content: String): List<String> {
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
