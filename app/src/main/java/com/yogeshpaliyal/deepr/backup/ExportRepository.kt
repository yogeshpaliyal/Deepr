package com.yogeshpaliyal.deepr.backup

import android.net.Uri
import com.yogeshpaliyal.deepr.util.RequestResult

interface ExportRepository {
    suspend fun exportToCsv(uri: Uri? = null): RequestResult<String>
    suspend fun exportToHtml(uri: Uri? = null): RequestResult<String>
}
