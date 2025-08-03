package com.yogeshpaliyal.deepr.backup

interface ExportRepository {
    suspend fun exportToCsv(): String
}