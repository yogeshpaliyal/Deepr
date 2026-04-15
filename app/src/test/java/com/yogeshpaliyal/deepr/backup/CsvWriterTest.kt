package com.yogeshpaliyal.deepr.backup

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CsvWriterTest {
    private val csvWriter = CsvWriter()

    private fun createTestLink(
        link: String = "https://example.com",
        createdAt: String = "2024-01-01",
        openedCount: Long = 5,
        name: String = "Example",
        notes: String = "test notes",
        tags: String? = "tag1,tag2",
        thumbnail: String = "",
        isFavourite: Long = 0,
        profileName: String = "Default",
    ): GetLinksForBackup =
        GetLinksForBackup(
            link = link,
            createdAt = createdAt,
            openedCount = openedCount,
            name = name,
            notes = notes,
            tags = tags,
            thumbnail = thumbnail,
            isFavourite = isFavourite,
            profileName = profileName,
        )

    private fun readCsvRows(bytes: ByteArray): List<Array<String>> {
        val reader =
            CSVReaderBuilder(ByteArrayInputStream(bytes).reader())
                .withCSVParser(CSVParserBuilder().build())
                .build()
        return reader.readAll()
    }

    @Test
    fun writeToCsv_withoutSettings_writesOnlyHeaderAndData() {
        val data = listOf(createTestLink())
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, data)

        val rows = readCsvRows(outputStream.toByteArray())
        // Header + 1 data row
        assertEquals(2, rows.size)
        assertEquals(Constants.Header.LINK, rows[0][0])
        assertEquals("https://example.com", rows[1][0])
    }

    @Test
    fun writeToCsv_withSettings_appendsSettingsRows() {
        val data = listOf(createTestLink())
        val settings =
            mapOf(
                Constants.Settings.SORTING_ORDER to "createdAt_DESC",
                Constants.Settings.VIEW_TYPE to "0",
                Constants.Settings.THEME_MODE to "dark",
            )
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, data, settings)

        val rows = readCsvRows(outputStream.toByteArray())
        // Header + 1 data row + 3 settings rows
        assertEquals(5, rows.size)

        // Verify settings rows have blank link and marker
        val settingsRows = rows.drop(2) // skip header and data
        settingsRows.forEach { row ->
            assertTrue("Settings row link column should be blank", row[0].isBlank())
            assertEquals(Constants.Settings.MARKER, row[1])
        }
    }

    @Test
    fun writeToCsv_settingsRowsContainCorrectKeyValuePairs() {
        val data = listOf(createTestLink())
        val settings =
            mapOf(
                Constants.Settings.SORTING_ORDER to "createdAt_DESC",
                Constants.Settings.SHOW_OPEN_COUNTER to "true",
            )
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, data, settings)

        val rows = readCsvRows(outputStream.toByteArray())
        val settingsRows = rows.drop(2)

        val parsedSettings = mutableMapOf<String, String>()
        settingsRows.forEach { row ->
            parsedSettings[row[2]] = row[3]
        }

        assertEquals("createdAt_DESC", parsedSettings[Constants.Settings.SORTING_ORDER])
        assertEquals("true", parsedSettings[Constants.Settings.SHOW_OPEN_COUNTER])
    }

    @Test
    fun writeToCsv_emptySettings_noExtraRows() {
        val data = listOf(createTestLink())
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, data, emptyMap())

        val rows = readCsvRows(outputStream.toByteArray())
        // Header + 1 data row only
        assertEquals(2, rows.size)
    }

    @Test
    fun writeToCsv_settingsRowsAreBackwardCompatible() {
        // Simulates how an old importer would handle settings rows:
        // it would skip them because the link column is blank
        val data = listOf(createTestLink())
        val settings =
            mapOf(
                Constants.Settings.VIEW_TYPE to "1",
            )
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, data, settings)

        val rows = readCsvRows(outputStream.toByteArray())
        val settingsRow = rows.last()

        // Old importer checks: if (link.isNotBlank() && existing == null)
        // Since link is blank, old importer would skip this row
        assertTrue("Link column must be blank for backward compatibility", settingsRow[0].isBlank())
    }
}
