package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.coroutines.asFlow
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.server.CountData
import com.yogeshpaliyal.deepr.server.CountType
import com.yogeshpaliyal.deepr.server.DeeprTag
import com.yogeshpaliyal.deepr.server.LinksListData
import com.yogeshpaliyal.deepr.server.TagsListData
import com.yogeshpaliyal.deepr.viewmodel.SortType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DataProvider(
    private val deeprQueries: DeeprQueries,
) {
    fun getLinks(
        coroutineScope: CoroutineScope,
        searchQuery: Flow<String>,
        sortOrder: Flow<@SortType String>,
        selectedTagFilter: Flow<List<DeeprTag>>,
        favouriteFilter: Flow<Int>,
    ): StateFlow<LinksListData?> =
        combine(
            searchQuery,
            sortOrder,
            selectedTagFilter,
            favouriteFilter,
        ) { query, sorting, tags, favourite ->
            listOf(query, sorting, tags, favourite)
        }.flatMapLatest { combined ->
            val query = combined[0] as String
            val sorting = (combined[1] as String).split("_")
            val tags = combined[2] as List<DeeprTag>
            val favourite = combined[3] as Int
            val sortField = sorting.getOrNull(0) ?: "createdAt"
            val sortType = sorting.getOrNull(1) ?: "DESC"

            // Prepare tag filter parameters
            val tagIdsString =
                if (tags.isEmpty()) "" else tags.joinToString(",") { it.id.toString() }

            deeprQueries
                .getLinksAndTags(
                    query,
                    query,
                    query,
                    favourite.toLong(),
                    favourite.toLong(),
                    tagIdsString,
                    tagIdsString,
                    sortType,
                    sortField,
                    sortType,
                    sortField,
                ).asFlow()
                .map {
                    val dbList = it.executeAsList()
                    val mappedList = dbList.map { dbObj -> DeeprLink(dbObj) }
                    LinksListData(mappedList)
                }
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)

    fun getAllTags(coroutineScope: CoroutineScope): StateFlow<TagsListData?> =
        deeprQueries
            .DeeprTag()
            .asFlow()
            .map {
                val list =
                    it.executeAsList().map { dto -> DeeprTag(dto.id, dto.name, dto.linkCount) }
                TagsListData(list)
            }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)

    fun countOfLinks(coroutineScope: CoroutineScope): StateFlow<CountData?> {
        return deeprQueries
            .countOfLinks()
            .asFlow()
            .map {
                val data = it.executeAsOneOrNull()
                CountData(data, CountType.TOTAL)
            }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)
    }


    fun countOfFavouritesLinks(coroutineScope: CoroutineScope): StateFlow<CountData?> {
        return deeprQueries
            .countOfFavouriteLinks()
            .asFlow()
            .map {
                val data = it.executeAsOneOrNull()
                CountData(data, CountType.FAVOURITE)
            }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)
    }


    fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {

        deeprQueries.removeTagFromLink(linkId, tagId)

    }

    fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        deeprQueries.addTagToLink(linkId, tagId)
    }


    fun addTagToLinkByName(
        linkId: Long,
        tagName: String,
    ) {
        // Create the tag if it doesn't exist
        deeprQueries.insertTag(tagName)

        // Get the tag ID
        val tag = deeprQueries.getTagByName(tagName).executeAsOneOrNull()

        if (tag != null) {
            // Add the tag to the link
            deeprQueries.addTagToLink(linkId, tag.id)
        }
    }


    fun getTagByName(tagName: String): DeeprTag? {
        val dbTag = deeprQueries.getTagByName(tagName).executeAsOneOrNull()
        if (dbTag != null) {
            return DeeprTag(dbTag.id, dbTag.name, dbTag.linkCount)
        } else {
            return null
        }
    }


    fun modifyTagsForLink(
        linkId: Long,
        tagsList: List<DeeprTag>,
    ) {

        // Then add selected tags
        tagsList.forEach { tag ->
            if (tag.id > 0) {
                // Existing tag
                addTagToLink(linkId, tag.id)
            } else {
                // New tag
                addTagToLinkByName(linkId, tag.name)
            }
        }

    }

    fun insertLink(
        link: String,
        name: String,
        tagsList: List<DeeprTag>? = null,
        notes: String = "",
        thumbnail: String = "",
        openedCount: Long = 0,
        isFavourite: Long = 0,
        createdAt: String? = null,
    ): Long? {
        if (createdAt == null) {
            deeprQueries.insertDeepr(
                link = link,
                name,
                openedCount,
                notes,
                thumbnail,
                isFavourite
            )
        } else {
            deeprQueries.importDeepr(
                link = link,
                name,
                openedCount,
                notes,
                thumbnail,
                isFavourite,
                createdAt
            )
        }
        val linkId = deeprQueries.lastInsertRowId().executeAsOneOrNull()
        linkId?.let { linkId ->
            tagsList?.let {
                modifyTagsForLink(linkId, tagsList)
            }
        }
        return linkId
    }


    fun deleteAccount(id: Long) {

        val tagsToDelete = mutableListOf<Long>()

        deeprQueries.getTagsForLink(id).executeAsList().forEach { tag ->
            val linkCount = deeprQueries.hasTagLinks(tag.id).executeAsOne()
            if (linkCount == 1L) {
                tagsToDelete.add(tag.id)
            }
        }

        deeprQueries.deleteDeeprById(id)
        deeprQueries.deleteLinkRelations(id)
        tagsToDelete.forEach { tagId ->
            deeprQueries.deleteTag(tagId)
        }


    }

    fun deleteTag(id: Long) {
        deeprQueries.deleteTag(id)
        deeprQueries.deleteTagRelations(id)
    }

    fun updateTag(tag: DeeprTag) {

        deeprQueries.updateTag(tag.name, tag.id)

    }

    fun incrementOpenedCount(id: Long) {

        deeprQueries.incrementOpenedCount(id)
        deeprQueries.insertDeeprOpenLog(id)

    }

    fun resetOpenedCount(id: Long) {
        deeprQueries.resetOpenedCount(id)
    }


    fun toggleFavourite(id: Long) {

        deeprQueries.toggleFavourite(id)

    }

    fun updateDeeplink(
        id: Long,
        newLink: String,
        newName: String,
        tagsList: List<DeeprTag>,
        notes: String = "",
        thumbnail: String = "",
    ) {
        deeprQueries.updateDeeplink(newLink, newName, notes, thumbnail, id)
        modifyTagsForLink(id, tagsList)
    }

    fun isLinkExist(link: String): Boolean {
        val dbObj = deeprQueries.isLinkExists(link).executeAsOneOrNull()
        return (dbObj ?: 0L) > 0L
    }

    fun insertTag(name: String) {
        deeprQueries.insertTag(name)
    }


    fun runInDbTransaction(block: () -> Unit) {
        deeprQueries.transaction {
            block()
        }
    }


}
