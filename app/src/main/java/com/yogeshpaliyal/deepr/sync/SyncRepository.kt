package com.yogeshpaliyal.deepr.sync

import com.yogeshpaliyal.deepr.util.RequestResult

interface SyncRepository {
    suspend fun syncToMarkdown(): RequestResult<String>

    suspend fun validateMarkdownFile(filePath: String): RequestResult<Boolean>
}
