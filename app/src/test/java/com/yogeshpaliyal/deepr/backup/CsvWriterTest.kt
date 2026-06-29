package com.yogeshpaliyal.deepr.backup

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CsvWriterTest {
    private val csvWriter = CsvWriter()

    private fun createTestProfile(
        id: Long = 1L,
        name: String = "Default",
        createdAt: String = "2024-01-01",
        themeMode: String = "system",
        colorTheme: String = "dynamic",
        priority: Long = 0L,
        isPrivate: Long = 0L,
    ): Profile =
        Profile(
            id = id,
            name = name,
            createdAt = createdAt,
            themeMode = themeMode,
            colorTheme = colorTheme,
            priority = priority,
            isPrivate = isPrivate,
        )

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
    fun writeToCsv_withoutSettings_writesOnlyProfilesAndLinks() {
        val profiles = listOf(createTestProfile())
        val links = listOf(createTestLink())
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, profiles, links)

        val rows = readCsvRows(outputStream.toByteArray())

        // Rows:
        // 0: SECTION, PROFILES
        // 1: ProfileName, Priority, ThemeMode, ColorTheme
        // 2: Default, 0, system, dynamic
        // 3: SECTION, LINKS
        // 4: Headers
        // 5: https://example.com, ...
        assertEquals(6, rows.size)
        assertEquals("SECTION", rows[0][0])
        assertEquals("PROFILES", rows[0][1])
        assertEquals("SECTION", rows[3][0])
        assertEquals("LINKS", rows[3][1])
        assertEquals("https://example.com", rows[5][0])
    }

    @Test
    fun writeToCsv_withSettings_appendsSettingsSection() {
        val profiles = listOf(createTestProfile())
        val links = listOf(createTestLink())
        val settings =
            mapOf(
                Constants.Settings.SORTING_ORDER to "createdAt_DESC",
                Constants.Settings.VIEW_TYPE to "0",
            )
        val outputStream = ByteArrayOutputStream()

        csvWriter.writeToCsv(outputStream, profiles, links, settings)

        val rows = readCsvRows(outputStream.toByteArray())

        // Rows:
        // 0-5: profiles and links (6 rows)
        // 6: SECTION, SETTINGS
        // 7: SettingKey, SettingValue
        // 8: sortingOrder, createdAt_DESC
        // 9: viewType, 0
        assertEquals(10, rows.size)

        assertEquals("SECTION", rows[6][0])
        assertEquals("SETTINGS", rows[6][1])
        assertEquals("SettingKey", rows[7][0])
        assertEquals("SettingValue", rows[7][1])

        val parsedSettings = mutableMapOf<String, String>()
        parsedSettings[rows[8][0]] = rows[8][1]
        parsedSettings[rows[9][0]] = rows[9][1]

        assertEquals("createdAt_DESC", parsedSettings[Constants.Settings.SORTING_ORDER])
        assertEquals("0", parsedSettings[Constants.Settings.VIEW_TYPE])
    }
}
