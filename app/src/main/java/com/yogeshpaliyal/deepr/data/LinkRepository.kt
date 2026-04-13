package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.Query
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.Tags

interface LinkRepository {
    // Profile operations
    suspend fun insertProfile(name: String, priority: Long = 0)

    fun getAllProfiles(): Query<Profile>

    suspend fun getProfileById(id: Long): Profile?

    suspend fun getProfileByName(name: String): Profile?

    suspend fun updateProfile(
        name: String,
        themeMode: String,
        colorTheme: String,
        id: Long,
    )

    suspend fun updateProfilePriority(id: Long, priority: Long)

    suspend fun getMaxPriority(): Long

    suspend fun deleteProfile(id: Long)

    fun countProfiles(): Query<Long>

    // Tag operations
    fun getAllTags(): Query<Tags>

    fun getAllTagsWithCount(profileId: Long): Query<GetAllTagsWithCount>

    suspend fun getTagByName(tagName: String): Tags?

    suspend fun insertTag(tagName: String)

    suspend fun updateTag(
        name: String,
        id: Long,
    )

    suspend fun deleteTag(id: Long)

    suspend fun deleteTagRelations(id: Long)

    // Link-Tag operations
    suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    )

    suspend fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    )

    suspend fun getTagsForLink(linkId: Long): List<Tags>

    suspend fun hasTagLinks(tagId: Long): Long

    suspend fun deleteLinkRelations(linkId: Long)

    // Link operations
    fun getLinksAndTags(
        profileId: Long,
        searchQuery1: String,
        searchQuery2: String,
        searchQuery3: String,
        searchQuery4: String,
        favouriteFilter1: Long,
        favouriteFilter2: Long,
        tagIdsString1: String,
        tagIdsString2: String,
        sortType1: String,
        sortField1: String,
        sortType2: String,
        sortField2: String,
    ): Query<GetLinksAndTags>

    fun countOfLinks(profileId: Long): Query<Long>

    fun countOfFavouriteLinks(profileId: Long): Query<Long>

    suspend fun insertDeepr(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
        profileId: Long,
    )

    suspend fun lastInsertRowId(): Long?

    suspend fun updateDeeplink(
        newLink: String,
        newName: String,
        notes: String,
        thumbnail: String,
        profileId: Long,
        id: Long,
    )

    suspend fun deleteDeeprById(id: Long)

    suspend fun incrementOpenedCount(id: Long)

    suspend fun resetOpenedCount(id: Long)

    suspend fun toggleFavourite(id: Long)

    suspend fun insertDeeprOpenLog(id: Long)
}
