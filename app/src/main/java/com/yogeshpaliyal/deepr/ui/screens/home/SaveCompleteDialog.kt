package com.yogeshpaliyal.deepr.ui.screens.home

import com.yogeshpaliyal.deepr.GetLinksAndTags

data class SaveDialogInfo(
    val deepr: GetLinksAndTags,
    val executeAfterSave: Boolean,
)

fun createDeeprObject(
    name: String = "",
    link: String = "",
    openedCount: Long = 0,
): GetLinksAndTags =
    GetLinksAndTags(
        id = 0,
        name = name,
        link = link,
        createdAt = "",
        openedCount = openedCount,
        tagsNames = "",
        tagsIds = "",
        lastOpenedAt = "",
        isFavourite = 0,
    )
