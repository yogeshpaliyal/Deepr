package com.yogeshpaliyal.deepr.backup

import androidx.annotation.Keep

@Keep
data class ExportCsv(
    val id: Long,
    val link: String,
    val createdAt: String,
    val openedCount: Long,
)
