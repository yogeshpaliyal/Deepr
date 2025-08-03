package com.yogeshpaliyal.deepr.backup

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvException
import com.yogeshpaliyal.deepr.DeeprQueries
import java.io.IOException

class ImportRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries
) : ImportRepository {

    override suspend fun importFromCsv(uri: Uri): ImportResult {
        val newRecords = mutableListOf<CsvSchema>()
        var skippedCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.reader().use { reader ->
                    val csvReader = CSVReaderBuilder(reader)
                        .withSkipLines(1)
                        .build()

                    csvReader.forEach { row ->
                        if (row.size >= 4) {
                            val id = row[0]
                            val link = row[1]
                            val createdAt = row[2].toLongOrNull() ?: System.currentTimeMillis()
                            val openedCount = row[3].toLongOrNull() ?: 0L

                            val existing = deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                            if (link.isNotBlank() && existing == null) {
                                newRecords.add(
                                    CsvSchema(
                                        id.toLong(),
                                        link,
                                        createdAt.toString(),
                                        openedCount
                                    )
                                )
                            } else {
                                skippedCount++
                            }
                        } else {
                            skippedCount++
                        }
                    }
                }
            }

            if (newRecords.isNotEmpty()) {
                deeprQueries.transaction {
                    newRecords.forEach { record ->
                        deeprQueries.insertDeepr(
                            link = record.link,
                            openedCount = record.openedCount
                        )
                    }
                }
            }

            return ImportResult(newRecords.size, skippedCount)

        } catch (e: IOException) {
            return ImportResult(0, 0, "Error reading file: ${e.message}")
        } catch (e: CsvException) {
            return ImportResult(0, 0, "Error parsing CSV file: ${e.message}")
        } catch (e: Exception) {
            return ImportResult(0, 0, "An unexpected error occurred: ${e.message}")
        }
    }
}