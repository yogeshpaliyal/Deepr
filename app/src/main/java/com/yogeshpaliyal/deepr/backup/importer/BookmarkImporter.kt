package com.yogeshpaliyal.deepr.backup.importer

import android.net.Uri
import com.yogeshpaliyal.deepr.backup.ImportResult
import com.yogeshpaliyal.deepr.util.RequestResult

/**
 * Base interface for importing bookmarks from various sources.
 * This interface can be extended to support different import formats
 * such as CSV, HTML, Chrome bookmarks, Mozilla bookmarks, etc.
 */
interface BookmarkImporter {
    /**
     * Import bookmarks from the given URI.
     *
     * @param uri The URI of the file to import from
     * @param profileId The ID of the profile to import bookmarks into
     * @return A [RequestResult] containing the [ImportResult] or an error
     */
    suspend fun import(uri: Uri): RequestResult<ImportResult>

    /**
     * Get the display name of this importer.
     *
     * @return A human-readable name for this importer (e.g., "CSV", "HTML", "Chrome Bookmarks")
     */
    fun getDisplayName(): String

    /**
     * Get the supported MIME types for this importer.
     *
     * @return An array of MIME types that this importer can handle
     */
    fun getSupportedMimeTypes(): Array<String>
}
