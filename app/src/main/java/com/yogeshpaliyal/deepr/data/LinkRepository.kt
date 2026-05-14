package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.Query
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.Tags

/**
 * Interface defining the data operations for links, profiles, and tags.
 */
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

    /**
     * Retrieves all profiles.
     * @return A [Query] for a list of [Profile].
     */
    fun getAllProfiles(): Query<Profile>

    /**
     * Retrieves a profile by its ID.
     * @param id The profile ID.
     * @return The [Profile] if found, null otherwise.
     */
    suspend fun getProfileById(id: Long): Profile?

    /**
     * Retrieves a profile by its name.
     * @param name The profile name.
     * @return The [Profile] if found, null otherwise.
     */
    suspend fun getProfileByName(name: String): Profile?

    /**
     * Updates an existing profile's details.
     * @param name The new name.
     * @param themeMode The new theme mode.
     * @param colorTheme The new color theme.
     * @param id The ID of the profile to update.
     */
    suspend fun updateProfile(
        name: String,
        themeMode: String,
        colorTheme: String,
        id: Long,
    )

    /**
     * Updates the priority of a given profile.
     * @param id The profile ID.
     * @param priority The new priority to set.
     */
    suspend fun updateProfilePriority(
        id: Long,
        priority: Long,
    )

    /**
     * Deletes a profile by its ID.
     * @param id The ID of the profile to delete.
     */
    suspend fun deleteProfile(id: Long)

    /**
     * Counts the total number of profiles.
     * @return A [Query] for the count.
     */
    fun countProfiles(): Query<Long>

    // Tag operations

    /**
     * Retrieves all tags.
     * @return A [Query] for a list of [Tags].
     */
    fun getAllTags(): Query<Tags>

    /**
     * Retrieves all tags with the count of associated links for a specific profile.
     * @param profileId The ID of the profile.
     * @return A [Query] for a list of [GetAllTagsWithCount].
     */
    fun getAllTagsWithCount(profileId: Long): Query<GetAllTagsWithCount>

    /**
     * Retrieves a tag by its name.
     * @param tagName The name of the tag.
     * @return The [Tags] if found, null otherwise.
     */
    suspend fun getTagByName(tagName: String): Tags?

    /**
     * Inserts a new tag.
     * @param tagName The name of the tag to insert.
     */
    suspend fun insertTag(tagName: String)

    /**
     * Updates an existing tag's name.
     * @param name The new tag name.
     * @param id The ID of the tag to update.
     */
    suspend fun updateTag(
        name: String,
        id: Long,
    )

    /**
     * Deletes a tag by its ID.
     * @param id The ID of the tag to delete.
     */
    suspend fun deleteTag(id: Long)

    /**
     * Deletes all relations for a specific tag.
     * @param id The tag ID.
     */
    suspend fun deleteTagRelations(id: Long)

    // Link-Tag operations

    /**
     * Associates a tag with a link.
     * @param linkId The link ID.
     * @param tagId The tag ID.
     */
    suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    )

    /**
     * Removes an association between a tag and a link.
     * @param linkId The link ID.
     * @param tagId The tag ID.
     */
    suspend fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    )

    /**
     * Retrieves all tags associated with a specific link.
     * @param linkId The ID of the link.
     * @return A list of [Tags].
     */
    suspend fun getTagsForLink(linkId: Long): List<Tags>

    /**
     * Checks if a tag is associated with any links.
     * @param tagId The ID of the tag.
     * @return The number of links associated with the tag.
     */
    suspend fun hasTagLinks(tagId: Long): Long

    /**
     * Deletes all tag relations for a specific link.
     * @param linkId The link ID.
     */
    suspend fun deleteLinkRelations(linkId: Long)

    // Link operations

    /**
     * Retrieves links and their associated tags for a specific profile,
     * with support for searching, filtering by favorites, filtering by tags, and sorting.
     * This version uses positional parameters to match the SQL query contract.
     *
     * @param profileId The ID of the profile to fetch links for.
     * @param searchQuery1 Part of the search query (for link field).
     * @param searchQuery2 Part of the search query (for name field).
     * @param searchQuery3 Part of the search query (for notes field).
     * @param favouriteFilter1 Flag to include all links (-1) or filter by favorite status.
     * @param favouriteFilter2 The favorite status to filter by (if filter is active).
     * @param tagIdsString1 Parameter for the tag filter logic.
     * @param tagIdsString2 Parameter for the tag filter logic.
     * @param sortType1 The sort direction ('ASC' or 'DESC').
     * @param sortField1 The field to sort by.
     * @param sortType2 Secondary sort direction for fallback.
     * @param sortField2 Secondary field to sort by for fallback.
     * @return A [Query] object for [GetLinksAndTags].
     */
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

    /**
     * Counts the total number of links for a specific profile.
     * @param profileId The ID of the profile.
     * @return A [Query] for the count.
     */
    fun countOfLinks(profileId: Long): Query<Long>

    /**
     * Counts the number of favourite links for a specific profile.
     * @param profileId The ID of the profile.
     * @return A [Query] for the count.
     */
    fun countOfFavouriteLinks(profileId: Long): Query<Long>

    /**
     * Inserts a new link (deepr).
     * @param link The URL.
     * @param name The display name.
     * @param openedCount Initial number of times opened.
     * @param notes Optional notes.
     * @param thumbnail Optional thumbnail URL.
     * @param profileId The associated profile ID.
     */
    suspend fun insertDeepr(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
        profileId: Long,
    )

    /**
     * Retrieves the row ID of the last inserted record.
     * @return The last insert row ID, or null if not available.
     */
    suspend fun lastInsertRowId(): Long?

    /**
     * Updates an existing link's details.
     * @param newLink The new URL.
     * @param newName The new display name.
     * @param notes The new notes.
     * @param thumbnail The new thumbnail URL.
     * @param profileId The associated profile ID.
     * @param id The ID of the link to update.
     */
    suspend fun updateDeeplink(
        newLink: String,
        newName: String,
        notes: String,
        thumbnail: String,
        profileId: Long,
        id: Long,
    )

    /**
     * Deletes a link by its ID.
     * @param id The ID of the link to delete.
     */
    suspend fun deleteDeeprById(id: Long)

    /**
     * Increments the opened count for a specific link.
     * @param id The link ID.
     */
    suspend fun incrementOpenedCount(id: Long)

    /**
     * Resets the opened count for a specific link to zero.
     * @param id The link ID.
     */
    suspend fun resetOpenedCount(id: Long)

    /**
     * Toggles the favourite status of a link.
     * @param id The link ID.
     */
    suspend fun toggleFavourite(id: Long)

    /**
     * Inserts a log entry for whenever a link is opened.
     * @param id The link ID.
     */
    suspend fun insertDeeprOpenLog(id: Long)
}
