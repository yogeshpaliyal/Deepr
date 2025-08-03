package com.yogeshpaliyal.deepr.backup

import androidx.annotation.Keep

@Keep
data class ImportResult(
    val importedCount: Int,
    val skippedCount: Int,
    val errorMessage: String? = null,
)