package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class LinksDataRepositoryImpl(
    private val deeprQueries: DeeprQueries,
) : LinksDataRepository {
    override fun getAllLinks(): List<GetLinksAndTags> =
        deeprQueries
            .getLinksAndTags(
                "",
                "",
                "",
                -1L,
                -1L,
                "",
                "",
                0L,
                "DESC",
                "createdAt",
                "DESC",
                "createdAt",
            ).executeAsList()

    override fun getFilteredLinks(
        searchQuery: String,
        isFavourite: Long?,
        tagIds: List<Long>,
        sortOrder: String,
        sortBy: String,
    ): List<GetLinksAndTags> {
        val favouriteFlag = isFavourite ?: -1L
        val tagIdsString = tagIds.joinToString(",")
        val tagCount = if (tagIds.isEmpty()) 0L else tagIds.size.toLong()

        return deeprQueries
            .getLinksAndTags(
                searchQuery,
                searchQuery,
                searchQuery,
                favouriteFlag,
                favouriteFlag,
                tagIdsString,
                tagIdsString,
                tagCount,
                sortOrder,
                sortBy,
                sortOrder,
                sortBy,
            ).executeAsList()
    }

    override fun getAllTags(): List<Tags> = deeprQueries.getAllTags().executeAsList()

    override fun getTagUsageCount(tagId: Long): Int =
        deeprQueries
            .getLinksAndTags(
                "",
                "",
                "",
                -1L,
                -1L,
                tagId.toString(),
                tagId.toString(),
                1L,
                "DESC",
                "createdAt",
                "DESC",
                "createdAt",
            ).executeAsList()
            .size

    override fun getTagsForLink(linkId: Long): List<Tags> = deeprQueries.getTagsForLink(linkId).executeAsList()

    override fun insertLink(
        link: String,
        name: String,
        notes: String,
        thumbnail: String,
        tags: List<Tags>,
    ): Long {
        deeprQueries.transaction {
            deeprQueries.insertDeepr(
                link = link,
                name = name,
                openedCount = 0,
                notes = notes,
                thumbnail = thumbnail,
            )

            val linkId = deeprQueries.lastInsertRowId().executeAsOne()

            tags.forEach { tag ->
                deeprQueries.insertTag(tag.name)
                val insertedTag = deeprQueries.getTagByName(tag.name).executeAsOne()
                deeprQueries.addTagToLink(linkId, insertedTag.id)
            }
        }
        return deeprQueries.lastInsertRowId().executeAsOne()
    }

    override fun updateLink(
        id: Long,
        link: String,
        name: String,
        notes: String,
        thumbnail: String,
    ) {
        deeprQueries.updateDeeplink(
            link = link,
            name = name,
            notes = notes,
            thumbnail = thumbnail,
            id = id,
        )
    }

    override fun deleteLink(id: Long) {
        deeprQueries.deleteDeeprById(id)
    }

    override fun toggleFavourite(id: Long) {
        deeprQueries.toggleFavourite(id)
    }

    override fun setFavourite(
        id: Long,
        isFavourite: Boolean,
    ) {
        deeprQueries.setFavourite(if (isFavourite) 1L else 0L, id)
    }

    override fun incrementOpenedCount(id: Long) {
        deeprQueries.incrementOpenedCount(id)
    }

    override fun getLinkByUrl(url: String): com.yogeshpaliyal.deepr.Deepr? = deeprQueries.getDeeprByLink(url).executeAsOneOrNull()

    override fun getLinkCount(): Long = deeprQueries.countOfLinks().executeAsOne()

    override fun getFavouriteLinkCount(): Long = deeprQueries.countOfFavouriteLinks().executeAsOne()

    override fun importLink(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
        isFavourite: Long,
        createdAt: String,
        tags: List<String>,
    ): Long {
        return deeprQueries.transactionWithResult {
            // Check if link already exists
            if (deeprQueries.getDeeprByLink(link).executeAsOneOrNull() != null) {
                return@transactionWithResult -1L
            }

            deeprQueries.importDeepr(
                link = link,
                name = name,
                openedCount = openedCount,
                notes = notes,
                thumbnail = thumbnail,
                isFavourite = isFavourite,
                createdAt = createdAt,
            )

            val linkId = deeprQueries.lastInsertRowId().executeAsOne()

            tags.forEach { tagName ->
                deeprQueries.insertTag(name = tagName)
                val tag = deeprQueries.getTagByName(tagName).executeAsOne()
                deeprQueries.addTagToLink(linkId = linkId, tagId = tag.id)
            }

            linkId
        }
    }

    override fun insertOrGetTag(name: String): Tags {
        deeprQueries.insertTag(name)
        return deeprQueries.getTagByName(name).executeAsOne()
    }

    override fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        deeprQueries.addTagToLink(linkId, tagId)
    }

    override fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {
        deeprQueries.removeTagFromLink(linkId, tagId)
    }

    override fun deleteTag(tagId: Long) {
        deeprQueries.deleteTag(tagId)
    }

    override fun updateTag(
        tagId: Long,
        name: String,
    ) {
        deeprQueries.updateTag(name, tagId)
    }

    override fun getTagByName(name: String): Tags? = deeprQueries.getTagByName(name).executeAsOneOrNull()

    override fun getTagsForLinkWithUsageCount(linkId: Long): List<Pair<Tags, Long>> {
        val tags = deeprQueries.getTagsForLink(linkId).executeAsList()
        return tags.map { tag ->
            val usageCount = deeprQueries.hasTagLinks(tag.id).executeAsOne()
            Pair(tag, usageCount)
        }
    }

    override fun deleteLinkRelations(linkId: Long) {
        deeprQueries.deleteLinkRelations(linkId)
    }

    override fun deleteTagRelations(tagId: Long) {
        deeprQueries.deleteTagRelations(tagId)
    }

    override fun insertDeeprOpenLog(deeplinkId: Long) {
        deeprQueries.insertDeeprOpenLog(deeplinkId)
    }

    override fun resetOpenedCount(id: Long) {
        deeprQueries.resetOpenedCount(id)
    }

    override fun observeAllTags(): Flow<List<Tags>> = deeprQueries.getAllTags().asFlow().mapToList(Dispatchers.IO)

    override fun observeAllTagsWithCount(): Flow<List<GetAllTagsWithCount>> =
        deeprQueries.getAllTagsWithCount().asFlow().mapToList(Dispatchers.IO)

    override fun observeLinkCount(): Flow<Long?> = deeprQueries.countOfLinks().asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun observeFavouriteLinkCount(): Flow<Long?> = deeprQueries.countOfFavouriteLinks().asFlow().mapToOneOrNull(Dispatchers.IO)
}
