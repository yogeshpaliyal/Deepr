package com.yogeshpaliyal.deepr.backup

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvException
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.util.Constants
import com.yogeshpaliyal.deepr.util.RequestResult
import java.io.IOException

class ImportRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : ImportRepository {
    override suspend fun importFromCsv(uri: Uri): RequestResult<ImportResult> {
        var updatedCount = 0
        var skippedCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.reader().use { reader ->
                    val csvReader =
                        CSVReaderBuilder(reader)
                            .build()

                    // verify header first
                    val header = csvReader.readNext()
                    if (header == null ||
                        header.size < 4 ||
                        header[0] != Constants.Header.LINK ||
                        header[1] != Constants.Header.CREATED_AT ||
                        header[2] != Constants.Header.OPENED_COUNT ||
                        header[3] != Constants.Header.NAME
                    ) {
                        return RequestResult.Error("Invalid CSV header")
                    }

                    csvReader.forEach { row ->
                        if (row.size >= 4) {
                            val link = row[0]
                            val openedCount = row[2].toLongOrNull() ?: 0L
                            // Name is everything from index 3 onwards, joined if split across columns
                            val name = row.drop(3).joinToString(",")
                            val existing = deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                            if (link.isNotBlank() && existing == null) {
                                updatedCount++
                                deeprQueries.transaction {
                                    deeprQueries.insertDeepr(
                                        link = link,
                                        openedCount = openedCount,
                                        name = name,
                                    )
                                }
                            } else {
                                skippedCount++
                            }
                        } else {
                            skippedCount++
                        }
                    }
                }
            }

            return RequestResult.Success(ImportResult(updatedCount, skippedCount))
        } catch (e: IOException) {
            return RequestResult.Error("Error reading file: ${e.message}")
        } catch (e: CsvException) {
            return RequestResult.Error("Error parsing CSV file: ${e.message}")
        } catch (e: Exception) {
            return RequestResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
}
