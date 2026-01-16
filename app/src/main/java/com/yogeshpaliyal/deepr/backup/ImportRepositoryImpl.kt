package com.yogeshpaliyal.deepr.backup

import android.content.Context
import android.net.Uri
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.backup.importer.BookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.ChromeBookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.CsvBookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.MozillaBookmarkImporter
import com.yogeshpaliyal.deepr.util.RequestResult

class ImportRepositoryImpl(
    context: Context,
    deeprQueries: DeeprQueries,
) : ImportRepository {
    private val csvImporter = CsvBookmarkImporter(context, deeprQueries)
    private val chromeImporter = ChromeBookmarkImporter(context, deeprQueries)
    private val mozillaImporter = MozillaBookmarkImporter(context, deeprQueries)

    override suspend fun importFromCsv(
        uri: Uri,
        profileId: Long,
    ): RequestResult<ImportResult> = csvImporter.import(uri, profileId)

    override fun getAvailableImporters(): List<BookmarkImporter> =
        listOf(
            csvImporter,
            chromeImporter,
            mozillaImporter,
        )

    override suspend fun importBookmarks(
        uri: Uri,
        importer: BookmarkImporter,
        profileId: Long,
    ): RequestResult<ImportResult> = importer.import(uri, profileId)
}
