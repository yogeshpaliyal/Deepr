package com.yogeshpaliyal.deepr

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

/**
 * Test for CSV export/import functionality, specifically handling commas in names
 */
class CsvExportImportTest {
    @Test
    fun csvWriter_handlesCommasInValues() {
        // Test that CSVWriter properly escapes commas
        val stringWriter = StringWriter()
        val csvWriter = CSVWriter(stringWriter)

        csvWriter.writeNext(arrayOf("Link", "CreatedAt", "OpenedCount", "Name"))
        csvWriter.writeNext(arrayOf("https://example.com", "1234567890", "5", "Test, with comma"))
        csvWriter.writeNext(arrayOf("https://test.com", "9876543210", "10", "Normal name"))
        csvWriter.close()

        val output = stringWriter.toString()

        // Verify that the name with comma is properly quoted
        assert(output.contains("\"Test, with comma\""))

        // Now test that CSVReader can parse it back correctly
        val csvReader = CSVReader(StringReader(output))
        val rows = csvReader.readAll()

        assertEquals(3, rows.size) // Header + 2 data rows
        assertEquals("Name", rows[0][3])
        assertEquals("Test, with comma", rows[1][3])
        assertEquals("Normal name", rows[2][3])
    }

    @Test
    fun csvWriter_handlesQuotesInValues() {
        // Test that CSVWriter properly escapes quotes
        val stringWriter = StringWriter()
        val csvWriter = CSVWriter(stringWriter)

        csvWriter.writeNext(arrayOf("Link", "CreatedAt", "OpenedCount", "Name"))
        csvWriter.writeNext(arrayOf("https://example.com", "1234567890", "5", "Test \"quoted\" name"))
        csvWriter.close()

        val output = stringWriter.toString()

        // Now test that CSVReader can parse it back correctly
        val csvReader = CSVReader(StringReader(output))
        val rows = csvReader.readAll()

        assertEquals(2, rows.size) // Header + 1 data row
        assertEquals("Test \"quoted\" name", rows[1][3])
    }

    @Test
    fun csvWriter_handlesNewlinesInValues() {
        // Test that CSVWriter properly escapes newlines
        val stringWriter = StringWriter()
        val csvWriter = CSVWriter(stringWriter)

        csvWriter.writeNext(arrayOf("Link", "CreatedAt", "OpenedCount", "Name"))
        csvWriter.writeNext(arrayOf("https://example.com", "1234567890", "5", "Test\nwith newline"))
        csvWriter.close()

        val output = stringWriter.toString()

        // Now test that CSVReader can parse it back correctly
        val csvReader = CSVReader(StringReader(output))
        val rows = csvReader.readAll()

        assertEquals(2, rows.size) // Header + 1 data row
        assertEquals("Test\nwith newline", rows[1][3])
    }

    @Test
    fun csvReader_handlesMultipleCommasInName() {
        // Test CSV data with multiple commas in the name field
        val csvData = """Link,CreatedAt,OpenedCount,Name
https://example.com,1234567890,5,"First, Second, Third"
https://test.com,9876543210,10,Simple
"""

        val csvReader = CSVReader(StringReader(csvData))
        val rows = csvReader.readAll()

        assertEquals(3, rows.size) // Header + 2 data rows
        assertEquals("Name", rows[0][3])
        assertEquals("First, Second, Third", rows[1][3])
        assertEquals("Simple", rows[2][3])
    }

    @Test
    fun importLogic_joinsColumnsCorrectly() {
        // Test the join logic used in import when name might be split
        val row1 = arrayOf("https://example.com", "1234567890", "5", "Name with comma")
        val name1 = row1.drop(3).joinToString(",")
        assertEquals("Name with comma", name1)

        // Test with properly escaped CSV (name in single column)
        val row2 = arrayOf("https://example.com", "1234567890", "5", "First, Second, Third")
        val name2 = row2.drop(3).joinToString(",")
        assertEquals("First, Second, Third", name2)

        // Test edge case with old-style split data (shouldn't happen with new export, but handles legacy)
        val row3 = arrayOf("https://example.com", "1234567890", "5", "First", " Second", " Third")
        val name3 = row3.drop(3).joinToString(",")
        assertEquals("First, Second, Third", name3)
    }
}
