package com.yogeshpaliyal.deepr.backup

import com.opencsv.CSVWriter
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.util.Constants
import java.io.OutputStream

class CsvWriter {
    fun writeToCsv(
        outputStream: OutputStream,
        profiles: List<Profile>,
        links: List<GetLinksForBackup>,
    ) {
        outputStream.bufferedWriter().use { writer ->
            CSVWriter(writer).use { csvWriter ->
                // Section 1: Profiles (To ensure even empty ones are exported)
                csvWriter.writeNext(arrayOf("SECTION", "PROFILES"))
                csvWriter.writeNext(
                    arrayOf(
                        "ProfileName",
                        "Priority",
                        "ThemeMode",
                        "ColorTheme",
                    ),
                )
                profiles.forEach { profile ->
                    csvWriter.writeNext(
                        arrayOf(
                            profile.name,
                            profile.priority.toString(),
                            profile.themeMode,
                            profile.colorTheme,
                        ),
                    )
                }

                // Section 2: Links
                csvWriter.writeNext(arrayOf("SECTION", "LINKS"))
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
                links.forEach { item ->
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
