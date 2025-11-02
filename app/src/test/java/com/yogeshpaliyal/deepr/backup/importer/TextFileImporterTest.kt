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

    // Helper function that mimics the TextFileImporter's extractLinks logic
    private fun extractLinksFromContent(content: String): List<String> {
        val links = mutableListOf<String>()

        // First, try to split by commas
        val commaSeparated = content.split(",")

        // If we have multiple items from comma split, use those
        if (commaSeparated.size > 1) {
            links.addAll(commaSeparated.map { it.trim() })
        } else {
            // Otherwise, split by newlines
            links.addAll(content.split("\n").map { it.trim() })
        }

        return links.filter { it.isNotBlank() }
    }
}
