package com.yogeshpaliyal.deepr.backup

import android.net.Uri
import com.yogeshpaliyal.deepr.util.RequestResult

interface ImportRepository {
    suspend fun importFromCsv(uri: Uri): RequestResult<ImportResult>
}