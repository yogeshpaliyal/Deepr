package com.yogeshpaliyal.deepr.viewmodel

import android.net.Uri
import androidx.annotation.StringDef
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

    // State for tag filter
    private val _selectedTagFilter = MutableStateFlow<Tags?>(null)
    val selectedTagFilter: StateFlow<Tags?> = _selectedTagFilter

    // Set tag filter
    fun setTagFilter(tag: Tags?) {
        _selectedTagFilter.value = tag
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
    fun addTagToLink(
        linkId: Long,
        tagId: Long,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.addTagToLink(linkId, tagId)
        }
    }

    // Add tag by name (creates tag if it doesn't exist)
    fun addTagToLinkByName(
        linkId: Long,
        tagName: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
            _selectedTagFilter.value = tag
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
        combine(searchQuery, sortOrder, selectedTagFilter) { query, sorting, tag ->
            Triple(query, sorting, tag)
        }.flatMapLatest { combined ->
            val sorting = combined.second.split("_")
            val sortField = sorting.getOrNull(0) ?: "createdAt"
            val sortType = sorting.getOrNull(1) ?: "DESC"
            deeprQueries
                .getLinksAndTags(
                    combined.first,
                    combined.first,
                    combined.third?.id?.toString() ?: "",
                    combined.third?.id,
                    sortType,
                    sortField,
                    sortType,
                    sortField,
                ).asFlow()
                .mapToList(viewModelScope.coroutineContext)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun search(query: String) {
        searchQuery.value = query
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
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.insertDeepr(link = link, name, if (executed) 1 else 0)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.deleteDeeprById(id)
            deeprQueries.deleteLinkRelations(id)
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
        }
    }

    fun updateDeeplink(
        id: Long,
        newLink: String,
        newName: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            deeprQueries.updateDeeplink(newLink, newName, id)
        }
    }

    fun exportCsvData() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = exportRepository.exportToCsv()
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
