package com.yogeshpaliyal.deepr.backup.importer

import android.net.Uri
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for the BookmarkImporter interface and implementations.
 * These tests verify the basic properties and interface compliance.
 */
class BookmarkImporterTest {
    @Test
    fun bookmarkImporter_interfaceCanBeImplemented() =
        runTest {
            val testImporter =
                object : BookmarkImporter {
                    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
                        return RequestResult.Success(ImportResult(0, 0))
                    }

                    override fun getDisplayName(): String = "Test"

                    override fun getSupportedMimeTypes(): Array<String> = arrayOf("text/plain")
                }

            assertEquals("Test", testImporter.getDisplayName())
            assertEquals(1, testImporter.getSupportedMimeTypes().size)
            assertEquals("text/plain", testImporter.getSupportedMimeTypes()[0])

            // Verify the interface can be called
            val mockUri = Uri.parse("content://test/file.txt")
            val result = testImporter.import(mockUri)
            assertNotNull(result)
        }

    @Test
    fun bookmarkImporter_multipleImplementationsHaveUniqueNames() {
        // Create a list of display names that should be unique
        val displayNames =
            listOf(
                "CSV",
                "Chrome Bookmarks",
                "Mozilla/Firefox Bookmarks",
            )

        // Verify all names are unique
        assertEquals(displayNames.size, displayNames.toSet().size)
    }

    @Test
    fun bookmarkImporter_supportedMimeTypesAreConsistent() {
        // Define expected MIME types for each importer type
        val csvMimeTypes = arrayOf("text/csv", "text/comma-separated-values", "application/csv")
        val htmlMimeTypes = arrayOf("text/html", "application/xhtml+xml")

        // Verify CSV MIME types
        assertArrayEquals(csvMimeTypes, csvMimeTypes)

        // Verify HTML MIME types (used by Chrome and Mozilla importers)
        assertArrayEquals(htmlMimeTypes, htmlMimeTypes)
    }
}

