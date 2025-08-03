package com.yogeshpaliyal.deepr.backup

import com.yogeshpaliyal.deepr.util.RequestResult

interface ExportRepository {
    suspend fun exportToCsv(): RequestResult<String>
}