package com.yogeshpaliyal.deepr.data

import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.shared.data.DeeprLink

fun GetLinksAndTags.toDeeprLink(): DeeprLink {
    return DeeprLink(
        id = this.id,
        link = this.link,
        name = this.name,
        createdAt = this.createdAt,
        openedCount = this.openedCount,
        isFavourite = this.isFavourite,
        notes = this.notes,
        thumbnail = this.thumbnail,
        lastOpenedAt = this.lastOpenedAt,
        tagsNames = this.tagsNames,
        tagsIds = this.tagsIds,
        tags = this.tagsNames?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
    )
}