package com.yogeshpaliyal.deepr.viewmodel

import android.net.Uri
import androidx.annotation.StringDef
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.backup.AutoBackupWorker
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.data.LinkInfo
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.sync.SyncRepository
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.TYPE,
)
@StringDef(
    value = [
        SortType.SORT_CREATED_BY_ASC,
        SortType.SORT_CREATED_BY_DESC,
        SortType.SORT_OPENED_ASC,
        SortType.SORT_OPENED_DESC,
        SortType.SORT_NAME_ASC,
        SortType.SORT_NAME_DESC,
        SortType.SORT_LINK_ASC,
        SortType.SORT_LINK_DESC,
    ],
)
annotation class SortType {
    companion object {
        const val SORT_CREATED_BY_ASC = "createdAt_ASC"
        const val SORT_CREATED_BY_DESC = "createdAt_DESC"
        const val SORT_OPENED_ASC = "openedCount_ASC"
        const val SORT_OPENED_DESC = "openedCount_DESC"
        const val SORT_NAME_ASC = "name_ASC"
        const val SORT_NAME_DESC = "name_DESC"
        const val SORT_LINK_ASC = "link_ASC"
        const val SORT_LINK_DESC = "link_DESC"
    }
}

class AccountViewModel(
    private val deeprQueries: DeeprQueries,
    private val exportRepository: ExportRepository,
    private val importRepository: ImportRepository,
    private val syncRepository: SyncRepository,
    private val networkRepository: NetworkRepository,
    private val autoBackupWorker: AutoBackupWorker,
) : ViewModel(),
    KoinComponent {
    private val preferenceDataStore: AppPreferenceDataStore = get()
    private val searchQuery = MutableStateFlow("")

    // State for tags
    val allTags: StateFlow<List<Tags>> =
        deeprQueries
            .getAllTags()
            .asFlow()
            .mapToList(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())

    val allTagsWithCount: StateFlow<List<GetAllTagsWithCount>> =
        deeprQueries
            .getAllTagsWithCount()
            .asFlow()
            .mapToList(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())

    val countOfLinks: StateFlow<Long?> =
        deeprQueries
            .countOfLinks()
            .asFlow()
            .mapToOneOrNull(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val countOfFavouriteLinks: StateFlow<Long?> =
        deeprQueries
            .countOfFavouriteLinks()
            .asFlow()
            .mapToOneOrNull(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val sortOrder: Flow<@SortType String> =
        preferenceDataStore.getSortingOrder

    private val exportResultChannel = Channel<String>()
    val exportResultFlow = exportResultChannel.receiveAsFlow()

    private val importResultChannel = Channel<String>()
    val importResultFlow = importResultChannel.receiveAsFlow()

    private val syncResultChannel = Channel<String>()
    val syncResultFlow = syncResultChannel.receiveAsFlow()

    private val syncValidationChannel = Channel<String>()
    val syncValidationFlow = syncValidationChannel.receiveAsFlow()

    // State for tag filter - now supports multiple tags
    private val _selectedTagFilter = MutableStateFlow<List<Tags>>(emptyList())
    val selectedTagFilter: StateFlow<List<Tags>> = _selectedTagFilter

    // State for favourite filter (-1 = All, 0 = Not Favourite, 1 = Favourite)
    private val defaultPageFavourites: Flow<Boolean> = preferenceDataStore.getDefaultPageFavourites
    private val _favouriteFilter = MutableStateFlow(-1)
    val favouriteFilter: StateFlow<Int> = _favouriteFilter

    init {
        viewModelScope.launch(Dispatchers.IO) {
            defaultPageFavourites.collect { isFavouritesDefault ->
                _favouriteFilter.update { if (isFavouritesDefault) 1 else -1 }
            }
        }
    }

    // Set tag filter - toggle tag in the list
    fun setTagFilter(tag: Tags?) {
        if (tag == null) {
            _selectedTagFilter.update { emptyList() }
        } else {
            _selectedTagFilter.update { currentList ->
                if (currentList.any { it.id == tag.id }) {
                    // Remove tag if already selected
                    currentList.filter { it.id != tag.id }
                } else {
                    // Add tag if not selected
                    currentList + tag
                }
            }
        }
    }

    // Clear all tag filters
    fun clearTagFilters() {
        _selectedTagFilter.update { emptyList() }
    }

    // Set favourite filter
    fun setFavouriteFilter(filter: Int) {
        _favouriteFilter.update { filter }
    }

    // Remove tag from link
    fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.removeTagFromLink(linkId, tagId)
        }
    }

    // Add tag to link
    private suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            deeprQueries.addTagToLink(linkId, tagId)
        }
    }

    // Add tag by name (creates tag if it doesn't exist)
    private suspend fun addTagToLinkByName(
        linkId: Long,
        tagName: String,
    ) {
        withContext(Dispatchers.IO) {
            // Create the tag if it doesn't exist
            deeprQueries.insertTag(tagName)

            // Get the tag ID
            val tag = deeprQueries.getTagByName(tagName).executeAsOneOrNull()

            if (tag != null) {
                // Add the tag to the link
                deeprQueries.addTagToLink(linkId, tag.id)
            }
        }
    }

    fun setSelectedTagByName(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tag = deeprQueries.getTagByName(tagName).executeAsOneOrNull()
            if (tag != null) {
                setTagFilter(tag)
            }
        }
    }

    fun fetchMetaData(
        link: String,
        onLinkMetaDataFound: (LinkInfo?) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepository.getLinkInfo(link).getOrNull().let {
                withContext(Dispatchers.Main) {
                    onLinkMetaDataFound(it)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<GetLinksAndTags>?> =
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
            val tags = combined[2] as List<Tags>
            val favourite = combined[3] as Int
            val sortField = sorting.getOrNull(0) ?: "createdAt"
            val sortType = sorting.getOrNull(1) ?: "DESC"

            // Prepare tag filter parameters
            val tagIdsString = if (tags.isEmpty()) "" else tags.joinToString(",") { it.id.toString() }
            val tagCount = tags.size.toLong()

            deeprQueries
                .getLinksAndTags(
                    query,
                    query,
                    query,
                    favourite.toLong(),
                    favourite.toLong(),
                    tagIdsString,
                    tagIdsString,
                    tagCount,
                    sortType,
                    sortField,
                    sortType,
                    sortField,
                ).asFlow()
                .mapToList(viewModelScope.coroutineContext)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun search(query: String) {
        searchQuery.update { query }
    }

    fun setSortOrder(type: @SortType String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSortingOrder(type)
        }
    }

    fun insertAccount(
        link: String,
        name: String,
        executed: Boolean,
        tagsList: List<Tags>,
        notes: String = "",
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.insertDeepr(link = link, name, if (executed) 1 else 0, notes)
            deeprQueries.lastInsertRowId().executeAsOneOrNull()?.let {
                modifyTagsForLink(it, tagsList)
            }
            syncToMarkdown()
        }
    }

    suspend fun modifyTagsForLink(
        linkId: Long,
        tagsList: List<Tags>,
    ) {
        withContext(Dispatchers.IO) {
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
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
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
    }

    fun deleteTag(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.deleteTag(id)
            deeprQueries.deleteTagRelations(id)
        }
    }

    suspend fun updateTag(tag: Tags) {
        withContext(Dispatchers.IO) {
            deeprQueries.updateTag(tag.name, tag.id)
        }
    }

    fun incrementOpenedCount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.incrementOpenedCount(id)
            deeprQueries.insertDeeprOpenLog(id)
        }
    }

    fun resetOpenedCount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.resetOpenedCount(id)
        }
    }

    fun toggleFavourite(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.toggleFavourite(id)
        }
    }

    fun updateDeeplink(
        id: Long,
        newLink: String,
        newName: String,
        tagsList: List<Tags>,
        notes: String = "",
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.updateDeeplink(newLink, newName, notes, id)
            modifyTagsForLink(id, tagsList)
            syncToMarkdown()
        }
    }

    fun exportCsvData(uri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = exportRepository.exportToCsv(uri)
            when (result) {
                is RequestResult.Success -> {
                    exportResultChannel.send("Export completed: ${result.data}")
                }

                is RequestResult.Error -> {
                    exportResultChannel.send("Export failed: ${result.message}")
                }
            }
        }
    }

    fun importCsvData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            importResultChannel.send("Importing, please wait...")
            val result = importRepository.importFromCsv(uri)

            when (result) {
                is RequestResult.Success -> {
                    importResultChannel.send(
                        "Import complete! Added: ${result.data.importedCount}, Skipped (duplicates): ${result.data.skippedCount}",
                    )
                }

                is RequestResult.Error -> {
                    importResultChannel.send("Import failed: ${result.message}")
                }
            }
        }
    }

    // Shortcut icon preference methods
    val useLinkBasedIcons =
        preferenceDataStore.getUseLinkBasedIcons
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setUseLinkBasedIcons(useLink: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setUseLinkBasedIcons(useLink)
        }
    }

    // Language preference methods
    val languageCode =
        preferenceDataStore.getLanguageCode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setLanguageCode(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setLanguageCode(code)
        }
    }

    // Default page preference methods
    val defaultPageFavouritesEnabled =
        preferenceDataStore.getDefaultPageFavourites
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDefaultPageFavourites(favourites: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setDefaultPageFavourites(favourites)
        }
    }

    // Auto backup preference methods
    val autoBackupEnabled =
        preferenceDataStore.getAutoBackupEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoBackupLocation =
        preferenceDataStore.getAutoBackupLocation
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val lastBackupTime =
        preferenceDataStore.getLastBackupTime
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setAutoBackupEnabled(enabled)
        }
    }

    fun setAutoBackupLocation(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setAutoBackupLocation(location)
        }
    }

    fun setAutoBackupInterval(interval: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setAutoBackupInterval(interval)
        }
    }

    // Sync preference methods
    val syncEnabled =
        preferenceDataStore.getSyncEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val syncFilePath =
        preferenceDataStore.getSyncFilePath
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val lastSyncTime =
        preferenceDataStore.getLastSyncTime
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSyncEnabled(enabled)
        }
    }

    fun setSyncFilePath(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSyncFilePath(path)
            // Validate the file after setting the path
            validateSyncFile(path)
        }
    }

    fun syncToMarkdown() {
        viewModelScope.launch(Dispatchers.IO) {
            autoBackupWorker.doWork()
            val isEnabled = preferenceDataStore.getSyncEnabled.first()
            if (!isEnabled) {
                return@launch
            }
            val result = syncRepository.syncToMarkdown()
            when (result) {
                is RequestResult.Success -> {
                    syncResultChannel.send(result.data)
                }

                is RequestResult.Error -> {
                    syncResultChannel.send(result.message)
                }
            }
        }
    }

    fun validateSyncFile(filePath: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val pathToValidate = filePath.ifEmpty { syncFilePath.value }
            val result = syncRepository.validateMarkdownFile(pathToValidate)
            when (result) {
                is RequestResult.Success -> {
                    if (result.data) {
                        syncValidationChannel.send("valid")
                    } else {
                        syncValidationChannel.send("invalid")
                    }
                }

                is RequestResult.Error -> {
                    syncValidationChannel.send("error: ${result.message}")
                }
            }
        }
    }
}
