package com.yogeshpaliyal.deepr.data

import android.content.Context
import app.cash.sqldelight.Query
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.backup.GoogleDriveAutoBackupWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LinkRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : LinkRepository {
    /**
     * Schedules auto backup after data modification.
     */
    private fun scheduleAutoBackup() {
        GoogleDriveAutoBackupWorker.scheduleBackup(context)
    }

    // Profile operations
    override suspend fun insertProfile(name: String, priority: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertProfile(name, priority)
        }
        scheduleAutoBackup()
    }

    override fun getAllProfiles(): Query<Profile> = deeprQueries.getAllProfiles()

    override suspend fun getProfileById(id: Long): Profile? =
        withContext(Dispatchers.IO) {
            deeprQueries.getProfileById(id).executeAsOneOrNull()
        }

    override suspend fun getProfileByName(name: String): Profile? =
        withContext(Dispatchers.IO) {
            deeprQueries.getProfileByName(name).executeAsOneOrNull()
        }

    override suspend fun updateProfile(
        name: String,
        themeMode: String,
        colorTheme: String,
        id: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateProfile(name, themeMode, colorTheme, id)
        }
        scheduleAutoBackup()
    }

    override suspend fun updateProfilePriority(id: Long, priority: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateProfilePriority(priority, id)
        }
        scheduleAutoBackup()
    }

    override suspend fun getMaxPriority(): Long =
        withContext(Dispatchers.IO) {
            deeprQueries.maxPriority().executeAsOneOrNull()?.MAX ?: 0L
        }

    override suspend fun deleteProfile(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteProfile(id)
        }
        scheduleAutoBackup()
    }

    override fun countProfiles(): Query<Long> = deeprQueries.countProfiles()

    // Tag operations
    override fun getAllTags(): Query<Tags> = deeprQueries.getAllTags()

    override fun getAllTagsWithCount(profileId: Long): Query<GetAllTagsWithCount> = deeprQueries.getAllTagsWithCount(profileId)

    override suspend fun getTagByName(tagName: String): Tags? =
        withContext(Dispatchers.IO) {
            deeprQueries.getTagByName(tagName).executeAsOneOrNull()
        }

    override suspend fun insertTag(tagName: String) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertTag(tagName)
        }
        scheduleAutoBackup()
    }

    override suspend fun updateTag(
        name: String,
        id: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateTag(name, id)
        }
        scheduleAutoBackup()
    }

    override suspend fun deleteTag(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteTag(id)
        }
        scheduleAutoBackup()
    }

    override suspend fun deleteTagRelations(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteTagRelations(id)
        }
        scheduleAutoBackup()
    }

    // Link-Tag operations
    override suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.addTagToLink(linkId, tagId)
        }
        scheduleAutoBackup()
    }

    override suspend fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.removeTagFromLink(linkId, tagId)
        }
        scheduleAutoBackup()
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
        scheduleAutoBackup()
    }

    // Link operations
    override fun getLinksAndTags(
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
    ): Query<GetLinksAndTags> =
        deeprQueries.getLinksAndTags(
            profileId,
            searchQuery1,
            searchQuery2,
            searchQuery3,
            searchQuery4,
            favouriteFilter1,
            favouriteFilter2,
            tagIdsString1,
            tagIdsString2,
            sortType1,
            sortField1,
            sortType2,
            sortField2,
        )

    override fun countOfLinks(profileId: Long): Query<Long> = deeprQueries.countOfLinks(profileId)

    override fun countOfFavouriteLinks(profileId: Long): Query<Long> = deeprQueries.countOfFavouriteLinks(profileId)

    override suspend fun insertDeepr(
        link: String,
        name: String,
        openedCount: Long,
        notes: String,
        thumbnail: String,
        profileId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertDeepr(link, name, openedCount, notes, thumbnail, profileId)
        }
        scheduleAutoBackup()
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
        profileId: Long,
        id: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateDeeplink(newLink, newName, notes, thumbnail, profileId, id)
        }
        scheduleAutoBackup()
    }

    override suspend fun deleteDeeprById(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.deleteDeeprById(id)
        }
        scheduleAutoBackup()
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
        scheduleAutoBackup()
    }

    override suspend fun insertDeeprOpenLog(id: Long) {
        withContext(Dispatchers.IO) {
            deeprQueries.insertDeeprOpenLog(id)
        }
    }
}
