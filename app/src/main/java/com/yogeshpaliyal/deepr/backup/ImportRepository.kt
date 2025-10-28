package com.yogeshpaliyal.deepr.backup

import android.net.Uri
import com.yogeshpaliyal.deepr.backup.importer.BookmarkImporter
import com.yogeshpaliyal.deepr.util.RequestResult

interface ImportRepository {
    suspend fun importFromCsv(uri: Uri): RequestResult<ImportResult>

    /**
     * Get all available bookmark importers.
     *
     * @return A list of [BookmarkImporter] instances
     */
    fun getAvailableImporters(): List<BookmarkImporter>

    /**
     * Import bookmarks using a specific importer.
     *
     * @param uri The URI of the file to import from
     * @param importer The [BookmarkImporter] to use for importing
     * @return A [RequestResult] containing the [ImportResult] or an error
     */
    suspend fun importBookmarks(
        uri: Uri,
        importer: BookmarkImporter,
    ): RequestResult<ImportResult>
}
