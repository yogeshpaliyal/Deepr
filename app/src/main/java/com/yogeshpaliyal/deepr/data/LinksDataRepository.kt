package com.yogeshpaliyal.deepr.data

import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import kotlinx.coroutines.flow.Flow

interface LinksDataRepository {
    /**
     * Get all links with their tags
     */
    fun getAllLinks(): List<GetLinksAndTags>

    /**
     * Get links filtered by search query, favorite status, and tags
     */
    fun getFilteredLinks(
        searchQuery: String = "",
        isFavourite: Long? = null,
        tagIds: List<Long> = emptyList(),
        sortOrder: String = "DESC",
        sortBy: String = "createdAt",
    ): List<GetLinksAndTags>

    /**
     * Get all tags with their usage count
     */
    fun getAllTags(): List<Tags>

    /**
     * Get tag usage count
     */
    fun getTagUsageCount(tagId: Long): Int

    /**
     * Get tags for a specific link
     */
    fun getTagsForLink(linkId: Long): List<Tags>

    /**
     * Add a new link
     */
    fun insertLink(
        link: String,
        name: String,
        notes: String = "",
        thumbnail: String = "",
        tags: List<Tags> = emptyList(),
    ): Long

    /**
     * Update an existing link
     */
    fun updateLink(
        id: Long,
        link: String,
        name: String,
        notes: String,
        thumbnail: String,
    )

    /**
     * Delete a link
     */
    fun deleteLink(id: Long)

    /**
     * Toggle favorite status
     */
    fun toggleFavourite(id: Long)

    /**
     * Set favorite status
     */
    fun setFavourite(
        id: Long,
        isFavourite: Boolean,
    )

    /**
     * Increment opened count
     */
    fun incrementOpenedCount(id: Long)

    /**
     * Get link by URL
     */
    fun getLinkByUrl(url: String): com.yogeshpaliyal.deepr.Deepr?

    /**
     * Get link count
     */
    fun getLinkCount(): Long

    /**
     * Get favorite link count
     */
    fun getFavouriteLinkCount(): Long

    /**
     * Import link with all metadata
     */
    fun importLink(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
        isFavourite: Long,
        createdAt: String,
        tags: List<String>,
    ): Long

    /**
     * Insert or get tag by name
     */
    fun insertOrGetTag(name: String): Tags

    /**
     * Add tag to link
     */
    fun addTagToLink(
        linkId: Long,
        tagId: Long,
    )

    /**
     * Remove tag from link
     */
    fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    )

    /**
     * Delete tag
     */
    fun deleteTag(tagId: Long)

    /**
     * Update tag name
     */
    fun updateTag(
        tagId: Long,
        name: String,
    )

    /**
     * Get tag by name
     */
    fun getTagByName(name: String): Tags?

    /**
     * Get tags for a link with usage count check
     */
    fun getTagsForLinkWithUsageCount(linkId: Long): List<Pair<Tags, Long>>

    /**
     * Delete all link-tag relations for a link
     */
    fun deleteLinkRelations(linkId: Long)

    /**
     * Delete all tag relations
     */
    fun deleteTagRelations(tagId: Long)

    /**
     * Insert deep link open log
     */
    fun insertDeeprOpenLog(deeplinkId: Long)

    /**
     * Reset opened count
     */
    fun resetOpenedCount(id: Long)

    /**
     * Observe all tags as Flow
     */
    fun observeAllTags(): Flow<List<Tags>>

    /**
     * Observe tags with count as Flow
     */
    fun observeAllTagsWithCount(): Flow<List<GetAllTagsWithCount>>

    /**
     * Observe link count as Flow
     */
    fun observeLinkCount(): Flow<Long?>

    /**
     * Observe favourite link count as Flow
     */
    fun observeFavouriteLinkCount(): Flow<Long?>
}
