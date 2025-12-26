package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.Query
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LinkRepositoryImpl(
    private val deeprQueries: DeeprQueries,
) : LinkRepository {
    // Tag operations
    override fun getAllTags(): Query<Tags> = deeprQueries.getAllTags()

    override fun getAllTagsWithCount(): Query<GetAllTagsWithCount> = deeprQueries.getAllTagsWithCount()

    override suspend fun getTagByName(tagName: String): Tags? =
        withContext(Dispatchers.IO) {
            deeprQueries.getTagByName(tagName).executeAsOneOrNull()
        }

    override suspend fun insertTag(tagName: String) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertTag(tagName)
        }
    }

    override suspend fun updateTag(
        name: String,
        id: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateTag(name, id)
        }
    }

    override suspend fun deleteTag(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteTag(id)
        }
    }

    override suspend fun deleteTagRelations(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteTagRelations(id)
        }
    }

    // Link-Tag operations
    override suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.addTagToLink(linkId, tagId)
        }
    }

    override suspend fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.removeTagFromLink(linkId, tagId)
        }
    }

    override suspend fun getTagsForLink(linkId: Long): List<Tags> =
        withContext(Dispatchers.IO) {
            deeprQueries.getTagsForLink(linkId).executeAsList()
        }

    override suspend fun hasTagLinks(tagId: Long): Long =
        withContext(Dispatchers.IO) {
            deeprQueries.hasTagLinks(tagId).executeAsOne()
        }

    override suspend fun deleteLinkRelations(linkId: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteLinkRelations(linkId)
        }
    }

    // Link operations
    override fun getLinksAndTags(
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
    ): Query<GetLinksAndTags> =
        deeprQueries.getLinksAndTags(
            searchQuery1,
            searchQuery2,
            searchQuery3,
            favouriteFilter1,
            favouriteFilter2,
            tagIdsString1,
            tagIdsString2,
            sortType1,
            sortField1,
            sortType2,
            sortField2,
        )

    override fun countOfLinks(): Query<Long> = deeprQueries.countOfLinks()

    override fun countOfFavouriteLinks(): Query<Long> = deeprQueries.countOfFavouriteLinks()

    override suspend fun insertDeepr(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertDeepr(link, name, openedCount, notes, thumbnail)
        }
    }

    override suspend fun lastInsertRowId(): Long? =
        withContext(Dispatchers.IO) {
            deeprQueries.lastInsertRowId().executeAsOneOrNull()
        }

    override suspend fun updateDeeplink(
        newLink: String,
        newName: String,
        notes: String,
        thumbnail: String,
        id: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateDeeplink(newLink, newName, notes, thumbnail, id)
        }
    }

    override suspend fun deleteDeeprById(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteDeeprById(id)
        }
    }

    override suspend fun incrementOpenedCount(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.incrementOpenedCount(id)
        }
    }

    override suspend fun resetOpenedCount(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.resetOpenedCount(id)
        }
    }

    override suspend fun toggleFavourite(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.toggleFavourite(id)
        }
    }

    override suspend fun insertDeeprOpenLog(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertDeeprOpenLog(id)
        }
    }
}
