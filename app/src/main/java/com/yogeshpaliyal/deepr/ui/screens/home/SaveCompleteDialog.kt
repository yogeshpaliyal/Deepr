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
    notes: String = "",
    thumbnail: String = "",
    profileId: Long = 1L,
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
        notes = notes,
        thumbnail = thumbnail,
        profileId = profileId,
    )
