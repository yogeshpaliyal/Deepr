package com.yogeshpaliyal.deepr.data

import com.yogeshpaliyal.deepr.GetLinksAndTags
import kotlinx.serialization.Serializable

@Serializable
data class DeeprLink(
    val id: Long,
    val link: String,
    val name: String,
    val createdAt: String,
    val openedCount: Long,
    val isFavourite: Long,
    val notes: String,
    val thumbnail: String,
    val lastOpenedAt: String?,
    val tagsNames: String?,
    val tagsIds: String?,
    val tags: List<String> = emptyList(),
) {
    constructor(dbObj: GetLinksAndTags) : this(
        id = dbObj.id,
        link = dbObj.link,
        name = dbObj.name,
        createdAt = dbObj.createdAt,
        openedCount = dbObj.openedCount,
        isFavourite = dbObj.isFavourite,
        notes = dbObj.notes,
        thumbnail = dbObj.thumbnail,
        lastOpenedAt = dbObj.lastOpenedAt,
        tagsNames = dbObj.tagsNames,
        tagsIds = dbObj.tagsIds,
        tags = dbObj.tagsNames?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
    )
}
