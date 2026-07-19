package com.yogeshpaliyal.deepr.data

import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.GetLinksForBackup
import com.yogeshpaliyal.deepr.ListDeeprWithTagsAsc
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.Tags
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    // Profile operations

    /**
     * Inserts a new profile.
     * @param name The name of the profile.
     * @param priority The priority. If null, calculates the next available priority.
     */
    suspend fun insertProfile(
        name: String,
        priority: Long? = null,
    )

    fun getAllProfiles(): Flow<List<Profile>>

    suspend fun getProfileById(id: Long): Profile?

    suspend fun getProfileByName(name: String): Profile?

    suspend fun updateProfile(
        name: String,
        themeMode: String,
        colorTheme: String,
        id: Long,
    )

    /**
     * Updates the priority of a given profile.
     * @param id The profile ID
     * @param priority The new priority to set
     */
    suspend fun updateProfilePriority(
        id: Long,
        priority: Long,
    )

    suspend fun deleteProfile(id: Long)

    suspend fun countProfiles(): Long

    // Tag operations
    fun getAllTags(): Flow<List<Tags>>

    fun getAllTagsWithCount(profileId: Long): Flow<List<GetAllTagsWithCount>>

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
        favouriteFilter1: Long,
        favouriteFilter2: Long,
        tagIdsString1: String,
        tagIdsString2: String,
        sortType1: String,
        sortField1: String,
        sortType2: String,
        sortField2: String,
    ): Flow<List<GetLinksAndTags>>

    fun countOfLinks(profileId: Long): Flow<Long>

    fun countOfFavouriteLinks(profileId: Long): Flow<Long>

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

    /** Deletes a link and removes any tags that were only used by that link. */
    suspend fun deleteLinkAndOrphanedTags(id: Long)

    /**
     * Sets the exact set of tags for a link: removes tags no longer present in [tagNames],
     * adds the rest (creating any tag that doesn't exist yet).
     */
    suspend fun setTagsForLink(
        linkId: Long,
        tagNames: List<String>,
    )

    suspend fun incrementOpenedCount(id: Long)

    suspend fun resetOpenedCount(id: Long)

    suspend fun toggleFavourite(id: Long)

    suspend fun insertDeeprOpenLog(id: Long)

    // One-shot reads and bulk operations used outside live UI state

    suspend fun getDeeprByLink(link: String): Deepr?

    suspend fun getDeeprById(id: Long): Deepr?

    suspend fun countAllLinks(): Long

    suspend fun getLinksForBackup(): List<GetLinksForBackup>

    suspend fun getLinksForMarkdownSync(): List<ListDeeprWithTagsAsc>

    suspend fun getAllProfilesOnce(): List<Profile>

    suspend fun getOrCreateProfileByName(name: String): Long

    /** Deletes all profiles and tags (cascades to links/link-tags). Used for full-restore flows. */
    suspend fun clearAllData()

    data class NewLinkWithTags(
        val link: String,
        val name: String = "",
        val notes: String = "",
        val thumbnail: String = "",
        val openedCount: Long = 0,
        val isFavourite: Long = 0,
        // null -> DB default (CURRENT_TIMESTAMP)
        val createdAt: String? = null,
        val profileId: Long,
        val tagNames: List<String> = emptyList(),
    )

    /**
     * Inserts each item unless a Deepr with the same `link` already exists.
     * The whole batch runs in a single DB transaction.
     * @return the inserted id per item, or null if that item was skipped (duplicate link).
     */
    suspend fun insertLinksWithTags(items: List<NewLinkWithTags>): List<Long?>
}
