package com.yogeshpaliyal.deepr.ui.screens.home

import com.yogeshpaliyal.shared.data.DeeprLink

data class SaveDialogInfo(
    val deepr: DeeprLink,
    val executeAfterSave: Boolean,
)

fun createDeeprObject(
    name: String = "",
    link: String = "",
    openedCount: Long = 0,
    notes: String = "",
    thumbnail: String = "",
): DeeprLink =
    DeeprLink(
        id = 0,
        name = name,
        link = link,
        createdAt = "",
        openedCount = openedCount,
        tagsNames = "",
        tagsIds = "",
        lastOpenedAt = "",
        isFavourite = 0,
        notes = notes,
        thumbnail = thumbnail,
    )
