package com.yogeshpaliyal.deepr.backup

import android.net.Uri
import com.yogeshpaliyal.deepr.util.RequestResult

data class ExportResult(
    val message: String,
    val uri: Uri?,
)

interface ExportRepository {
    suspend fun exportToCsv(): RequestResult<ExportResult>
}
