package com.yogeshpaliyal.deepr.backup

import com.opencsv.CSVWriter
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.util.Constants
import java.io.OutputStream

class CsvWriter {
    fun writeToCsv(
        outputStream: OutputStream,
        data: List<GetLinksForBackup>,
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
                        Constants.Header.NOTES,
                        Constants.Header.TAGS,
                        Constants.Header.THUMBNAIL,
                        Constants.Header.IS_FAVOURITE,
                        Constants.Header.PROFILE_NAME,
                    ),
                )
                // Write Data
                data.forEach { item ->
                    csvWriter.writeNext(
                        arrayOf(
                            item.link,
                            item.createdAt,
                            item.openedCount.toString(),
                            item.name,
                            item.notes,
                            item.tags ?: "",
                            item.thumbnail,
                            item.isFavourite.toString(),
                            item.profileName,
                        ),
                    )
                }
            }
        }
    }
}
