package com.yogeshpaliyal.deepr.backup

import android.content.Context
import android.net.Uri
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.backup.importer.BookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.ChromeBookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.CsvBookmarkImporter
import com.yogeshpaliyal.deepr.backup.importer.MozillaBookmarkImporter
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.RequestResult

class ImportRepositoryImpl(
    context: Context,
    deeprQueries: DeeprQueries,
    appPreferenceDataStore: AppPreferenceDataStore,
) : ImportRepository {
    private val csvImporter = CsvBookmarkImporter(context, deeprQueries)
    private val chromeImporter = ChromeBookmarkImporter(context, deeprQueries, appPreferenceDataStore)
    private val mozillaImporter = MozillaBookmarkImporter(context, deeprQueries, appPreferenceDataStore)

    override suspend fun importFromCsv(uri: Uri): RequestResult<ImportResult> = csvImporter.import(uri)

    override fun getAvailableImporters(): List<BookmarkImporter> =
        listOf(
            csvImporter,
            chromeImporter,
            mozillaImporter,
        )

    override suspend fun importBookmarks(
        uri: Uri,
        importer: BookmarkImporter,
    ): RequestResult<ImportResult> = importer.import(uri)
}
