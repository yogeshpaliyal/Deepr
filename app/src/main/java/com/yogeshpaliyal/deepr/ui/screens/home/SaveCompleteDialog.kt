package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.yogeshpaliyal.deepr.Deepr

data class SaveDialogInfo(
    val deepr: Deepr,
    val executeAfterSave: Boolean,
)

fun createDeeprObject(
    name: String = "",
    link: String = "",
    openedCount: Long = 0,
): Deepr =
    Deepr(
        id = 0,
        name = name,
        link = link,
        createdAt = "",
        openedCount = openedCount,
    )
