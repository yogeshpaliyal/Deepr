package com.yogeshpaliyal.deepr.viewmodel

import android.net.Uri
import androidx.annotation.StringDef
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.backup.AutoBackupWorker
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.data.LinkInfo
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.sync.SyncRepository
import com.yogeshpaliyal.deepr.ui.screens.home.ViewType
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
import kotlinx.coroutines.flow.map
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
    private val linkRepository: LinkRepository,
    private val exportRepository: ExportRepository,
    private val importRepository: ImportRepository,
    private val syncRepository: SyncRepository,
    private val networkRepository: NetworkRepository,
    private val autoBackupWorker: AutoBackupWorker,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(),
    KoinComponent {
    private val preferenceDataStore: AppPreferenceDataStore = get()
    private val reviewManager: com.yogeshpaliyal.deepr.review.ReviewManager = get()
    private val searchQuery = MutableStateFlow("")

    private val _showProfilesGrid = MutableStateFlow(false)
    val showProfilesGrid: StateFlow<Boolean> = _showProfilesGrid

    fun setShowProfilesGrid(show: Boolean) {
        _showProfilesGrid.value = show
    }

    // Profile state
    val allProfiles: StateFlow<List<com.yogeshpaliyal.deepr.Profile>> =
        linkRepository
            .getAllProfiles()
            .asFlow()
            .mapToList(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())

    private val selectedProfileId: Flow<Long> = preferenceDataStore.getSelectedProfileId
    val currentProfile: StateFlow<com.yogeshpaliyal.deepr.Profile?> =
        combine(allProfiles, selectedProfileId) { profiles, profileId ->
            profiles.firstOrNull { it.id == profileId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Get current profile's theme mode
    val currentProfileTheme: StateFlow<String> =
        currentProfile
            .map { profile ->
                profile?.themeMode ?: "system"
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    // Get current profile's color theme
    val currentProfileColorTheme: StateFlow<String> =
        currentProfile
            .map { profile ->
                profile?.colorTheme ?: "dynamic"
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "dynamic")

    // Initialize default profile if none exists
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val profileCount =
                linkRepository
                    .countProfiles()
                    .asFlow()
                    .mapToOneOrNull(viewModelScope.coroutineContext)
                    .first()
            if (profileCount == 0L) {
                // Create default profile if none exists
                linkRepository.insertProfile("Default", 0)
            } else {
                // Normalize priorities for existing profiles if needed
                normalizePriorities()
            }
        }
    }

    private suspend fun normalizePriorities() {
        val profiles = linkRepository.getAllProfiles().executeAsList()
        profiles.forEachIndexed { index, profile ->
            if (profile.priority != index.toLong()) {
                linkRepository.updateProfilePriority(profile.id, index.toLong())
            }
        }
    }

    // State for tags - now scoped to current profile
    val allTags: StateFlow<List<Tags>> =
        linkRepository
            .getAllTags()
            .asFlow()
            .mapToList(
                viewModelScope.coroutineContext,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTagsWithCount: StateFlow<List<GetAllTagsWithCount>> =
        combine(selectedProfileId, searchQuery) { profileId, query ->
            Pair(profileId, query)
        }.flatMapLatest { (profileId, query) ->
            linkRepository
                .getAllTagsWithCount(profileId)
                .asFlow()
                .mapToList(
                    viewModelScope.coroutineContext,
                ).map { tags ->
                    if (query.isBlank()) {
                        tags
                    } else {
                        tags.filter { it.name.contains(query, ignoreCase = true) }
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val countOfLinks: StateFlow<Long?> =
        selectedProfileId
            .flatMapLatest { profileId ->
                linkRepository
                    .countOfLinks(profileId)
                    .asFlow()
                    .mapToOneOrNull(
                        viewModelScope.coroutineContext,
                    )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val countOfFavouriteLinks: StateFlow<Long?> =
        selectedProfileId
            .flatMapLatest { profileId ->
                linkRepository
                    .countOfFavouriteLinks(profileId)
                    .asFlow()
                    .mapToOneOrNull(
                        viewModelScope.coroutineContext,
                    )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

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

        // Track user properties for total links and tags
        viewModelScope.launch {
            countOfLinks.collect { count ->
                count?.let {
                    analyticsManager.setUserProperty(
                        com.yogeshpaliyal.deepr.analytics.AnalyticsUserProperties.TOTAL_LINKS,
                        it.toString(),
                    )
                }
            }
        }

        viewModelScope.launch {
            allTags.collect { tags ->
                analyticsManager.setUserProperty(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsUserProperties.TOTAL_TAGS,
                    tags.size.toString(),
                )
            }
        }

        viewModelScope.launch {
            preferenceDataStore.isThumbnailEnable.collect { enabled ->
                analyticsManager.setUserProperty(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsUserProperties.THUMBNAIL_ENABLED,
                    enabled.toString(),
                )
            }
        }
    }

    // Set tag filter - toggle tag in the list
    fun setTagFilter(tag: Tags?) {
        if (tag == null) {
            _selectedTagFilter.update { emptyList() }
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.CLEAR_TAG_FILTER,
            )
        } else {
            _selectedTagFilter.update { currentList ->
                if (currentList.any { it.id == tag.id }) {
                    // Remove tag if already selected
                    // Note: We don't reset favourite filter here because:
                    // 1. Other tags might still be selected
                    // 2. The user should explicitly choose All/Favourites to change filter mode
                    currentList.filter { it.id != tag.id }
                } else {
                    // Add tag if not selected
                    // Reset favourite filter to show all links with this tag, not just favourites
                    _favouriteFilter.update { -1 }
                    currentList + tag
                }
            }
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.SELECT_TAG_FILTER,
                mapOf(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsParams.TAG_ID to tag.id,
                    com.yogeshpaliyal.deepr.analytics.AnalyticsParams.TAG_NAME to tag.name,
                ),
            )
        }
    }

    // Clear all tag filters
    fun clearTagFilters() {
        _selectedTagFilter.update { emptyList() }
    }

    // Set favourite filter
    fun setFavouriteFilter(filter: Int) {
        _favouriteFilter.update { filter }
        analyticsManager.logEvent(
            com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.FILTER_FAVOURITES,
            mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.IS_FAVOURITE to (filter == 1)),
        )
    }

    // Remove tag from link
    fun removeTagFromLink(
        linkId: Long,
        tagId: Long,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.removeTagFromLink(linkId, tagId)
        }
    }

    // Add tag to link
    private suspend fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        withContext(Dispatchers.IO) {
            linkRepository.addTagToLink(linkId, tagId)
        }
    }

    // Add tag by name (creates tag if it doesn't exist)
    private suspend fun addTagToLinkByName(
        linkId: Long,
        tagName: String,
    ) {
        withContext(Dispatchers.IO) {
            // Create the tag if it doesn't exist
            linkRepository.insertTag(tagName)

            // Get the tag ID
            val tag = linkRepository.getTagByName(tagName)

            if (tag != null) {
                // Add the tag to the link
                linkRepository.addTagToLink(linkId, tag.id)
            }
        }
    }

    fun setSelectedTagByName(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tag = linkRepository.getTagByName(tagName)
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
            selectedProfileId,
        ) { query, sorting, tags, favourite, profileId ->
            listOf(query, sorting, tags, favourite, profileId)
        }.flatMapLatest { combined ->
            val query = combined[0] as String
            val sorting = (combined[1] as String).split("_")
            val tags = combined[2] as List<Tags>
            val favourite = combined[3] as Int
            val profileId = combined[4] as Long
            val sortField = sorting.getOrNull(0) ?: "createdAt"
            val sortType = sorting.getOrNull(1) ?: "DESC"

            // Prepare tag filter parameters
            val tagIdsString =
                if (tags.isEmpty()) "" else tags.joinToString(",") { it.id.toString() }
            val tagCount = tags.size.toLong()

            linkRepository
                .getLinksAndTags(
                    profileId,
                    query,
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
                .mapToList(viewModelScope.coroutineContext)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun search(query: String) {
        searchQuery.update { query }
        if (query.isNotEmpty()) {
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.SEARCH_LINKS,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.SEARCH_QUERY to query),
            )
        }
    }

    fun setSortOrder(type: @SortType String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSortingOrder(type)
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.CHANGE_SORT_ORDER,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.SORT_ORDER to type),
            )
        }
    }

    fun insertAccount(
        link: String,
        name: String,
        executed: Boolean,
        tagsList: List<Tags>,
        notes: String = "",
        thumbnail: String = "",
        profileId: Long? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetProfileId = profileId ?: selectedProfileId.first()
            linkRepository.insertDeepr(link = link, name, if (executed) 1 else 0, notes, thumbnail, targetProfileId)
            linkRepository.lastInsertRowId()?.let {
                modifyTagsForLink(it, tagsList)
                analyticsManager.logEvent(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ADD_LINK,
                    mapOf(
                        com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to it,
                        com.yogeshpaliyal.deepr.analytics.AnalyticsParams.HAS_THUMBNAIL to thumbnail.isNotEmpty(),
                    ),
                )
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

            linkRepository.getTagsForLink(id).forEach { tag ->
                val linkCount = linkRepository.hasTagLinks(tag.id)
                if (linkCount == 1L) {
                    tagsToDelete.add(tag.id)
                }
            }

            linkRepository.deleteDeeprById(id)
            linkRepository.deleteLinkRelations(id)
            tagsToDelete.forEach { tagId ->
                linkRepository.deleteTag(tagId)
            }

            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.DELETE_LINK,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to id),
            )
        }
    }

    fun deleteTag(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.deleteTag(id)
            linkRepository.deleteTagRelations(id)
        }
    }

    suspend fun updateTag(tag: Tags) {
        withContext(Dispatchers.IO) {
            linkRepository.updateTag(tag.name, tag.id)
        }
    }

    fun incrementOpenedCount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.incrementOpenedCount(id)
            linkRepository.insertDeeprOpenLog(id)
        }
    }

    fun resetOpenedCount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.resetOpenedCount(id)
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.RESET_COUNTER,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to id),
            )
        }
    }

    fun toggleFavourite(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            linkRepository.toggleFavourite(id)
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.TOGGLE_FAVOURITE,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to id),
            )
        }
    }

    fun updateDeeplink(
        id: Long,
        newLink: String,
        newName: String,
        tagsList: List<Tags>,
        notes: String = "",
        thumbnail: String = "",
        profileId: Long? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetProfileId = profileId ?: selectedProfileId.first()
            linkRepository.updateDeeplink(newLink, newName, notes, thumbnail, targetProfileId, id)
            modifyTagsForLink(id, tagsList)
            syncToMarkdown()
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.EDIT_LINK,
                mapOf(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to id,
                    com.yogeshpaliyal.deepr.analytics.AnalyticsParams.HAS_THUMBNAIL to thumbnail.isNotEmpty(),
                ),
            )
        }
    }

    fun exportCsvData(uri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = exportRepository.exportToCsv(uri)) {
                is RequestResult.Success -> {
                    exportResultChannel.send("Export completed: ${result.data}")
                    analyticsManager.logEvent(
                        com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.EXPORT_CSV,
                    )
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
            when (val result = importRepository.importFromCsv(uri)) {
                is RequestResult.Success -> {
                    importResultChannel.send(
                        "Import complete! Added: ${result.data.importedCount}, Skipped (duplicates): ${result.data.skippedCount}",
                    )
                    analyticsManager.logEvent(
                        com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.IMPORT_CSV,
                        mapOf(
                            "imported_count" to result.data.importedCount,
                            "skipped_count" to result.data.skippedCount,
                        ),
                    )
                }

                is RequestResult.Error -> {
                    importResultChannel.send("Import failed: ${result.message}")
                }
            }
        }
    }

    fun importBookmarks(
        uri: Uri,
        importer: com.yogeshpaliyal.deepr.backup.importer.BookmarkImporter,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            importResultChannel.send("Importing ${importer.getDisplayName()}, please wait...")
            when (val result = importRepository.importBookmarks(uri, importer)) {
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

    fun getAvailableImporters() = importRepository.getAvailableImporters()

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

    val isThumbnailEnable =
        preferenceDataStore.isThumbnailEnable
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDefaultPageFavourites(favourites: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setDefaultPageFavourites(favourites)
        }
    }

    fun setIsThumbnailEnable(thumbnail: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setThumbnailEnable(thumbnail)
        }
    }

    // Theme preference methods
    val themeMode =
        preferenceDataStore.getThemeMode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setThemeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setThemeMode(mode)
        }
    }

    // Show open counter preference methods
    val showOpenCounter =
        preferenceDataStore.getShowOpenCounter
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setShowOpenCounter(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setShowOpenCounter(show)
        }
    }

    // Google Drive auto backup preference methods
    val googleDriveAutoBackupEnabled =
        preferenceDataStore.getGoogleDriveAutoBackupEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setGoogleDriveAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setGoogleDriveAutoBackupEnabled(enabled)
        }
    }

    // Clipboard link detection preference methods
    val clipboardLinkDetectionEnabled =
        preferenceDataStore.getClipboardLinkDetectionEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setClipboardLinkDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setClipboardLinkDetectionEnabled(enabled)
        }
    }

    // Auto backup preference methods
    val autoBackupEnabled =
        preferenceDataStore.getAutoBackupEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoBackupLocation =
        preferenceDataStore.getAutoBackupLocation
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val viewType =
        preferenceDataStore.viewType
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewType.LIST)

    val lastBackupTime =
        preferenceDataStore.getLastBackupTime
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setAutoBackupEnabled(enabled)
        }
    }

    fun setViewType(viewType: @ViewType Int) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setViewType(viewType)
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
            when (val result = syncRepository.syncToMarkdown()) {
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
            when (val result = syncRepository.validateMarkdownFile(pathToValidate)) {
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

    fun requestReview(activity: android.app.Activity) {
        reviewManager.requestReview(activity)
    }

    // Profile management methods
    fun setSelectedProfile(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSelectedProfileId(profileId)
            // Clear tag filter when switching profiles
            _selectedTagFilter.update { emptyList() }
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.SWITCH_PROFILE,
                mapOf("profile_id" to profileId),
            )
        }
    }

    fun setSilentSaveProfile(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceDataStore.setSilentSaveProfileId(profileId)
        }
    }

    val silentSaveProfileId =
        preferenceDataStore.getSilentSaveProfileId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    fun insertProfile(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxPriority = linkRepository.getMaxPriority()
            linkRepository.insertProfile(name, maxPriority + 1)
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.CREATE_PROFILE,
            )
        }
    }

    fun moveProfileUp(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // First, ensure all profiles have unique, sequential priorities
            normalizePriorities()
            
            val profiles = linkRepository.getAllProfiles().executeAsList()
            val index = profiles.indexOfFirst { it.id == profileId }
            if (index > 0) {
                val currentProfile = profiles[index]
                val previousProfile = profiles[index - 1]

                val tempPriority = currentProfile.priority
                linkRepository.updateProfilePriority(currentProfile.id, previousProfile.priority)
                linkRepository.updateProfilePriority(previousProfile.id, tempPriority)
            }
        }
    }

    fun moveProfileDown(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // First, ensure all profiles have unique, sequential priorities
            normalizePriorities()
            
            val profiles = linkRepository.getAllProfiles().executeAsList()
            val index = profiles.indexOfFirst { it.id == profileId }
            if (index != -1 && index < profiles.size - 1) {
                val currentProfile = profiles[index]
                val nextProfile = profiles[index + 1]

                val tempPriority = currentProfile.priority
                linkRepository.updateProfilePriority(currentProfile.id, nextProfile.priority)
                linkRepository.updateProfilePriority(nextProfile.id, tempPriority)
            }
        }
    }

    suspend fun updateProfile(
        id: Long,
        name: String,
        themeMode: String,
        colorTheme: String,
    ) {
        withContext(Dispatchers.IO) {
            linkRepository.updateProfile(name, themeMode, colorTheme, id)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Don't allow deleting if it's the only profile
            val profileCount =
                linkRepository
                    .countProfiles()
                    .asFlow()
                    .mapToOneOrNull(viewModelScope.coroutineContext)
                    .first()
            if (profileCount != null && profileCount <= 1L) {
                return@launch
            }

            // If deleting the currently selected profile, switch to the first available profile
            val currentProfileId = selectedProfileId.first()
            if (currentProfileId == id) {
                val profiles = allProfiles.first()
                val nextProfile = profiles.firstOrNull { it.id != id }
                if (nextProfile != null) {
                    setSelectedProfile(nextProfile.id)
                }
            }

            linkRepository.deleteProfile(id)
            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.DELETE_PROFILE,
                mapOf("profile_id" to id),
            )
        }
    }
}
