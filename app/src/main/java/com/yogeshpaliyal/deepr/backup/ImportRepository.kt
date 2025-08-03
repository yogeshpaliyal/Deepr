package com.yogeshpaliyal.deepr.backup

import android.net.Uri

interface ImportRepository {
    suspend fun importFromCsv(uri: Uri): ImportResult
}