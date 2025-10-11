package com.yogeshpaliyal.deepr.backup

import com.opencsv.CSVWriter
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.util.Constants
import java.io.OutputStream

class CsvWriter {
    fun writeToCsv(
        outputStream: OutputStream,
        data: List<Deepr>,
    ) {
        outputStream.bufferedWriter().use { writer ->
            // Write Header
            CSVWriter(writer).use { csvWriter ->
                // Write Header
                csvWriter.writeNext(
                    arrayOf(
                        Constants.Header.LINK,
                        Constants.Header.CREATED_AT,
                        Constants.Header.OPENED_COUNT,
                        Constants.Header.NAME,
                    ),
                )
                // Write Data
                data.forEach { item ->
                    csvWriter.writeNext(
                        arrayOf(
                            item.link,
                            item.createdAt.toString(),
                            item.openedCount.toString(),
                            item.name,
                        ),
                    )
                }
            }
        }
    }
}
