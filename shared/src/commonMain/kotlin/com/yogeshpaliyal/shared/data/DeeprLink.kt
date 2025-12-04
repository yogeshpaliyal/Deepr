package com.yogeshpaliyal.shared.data

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
)
