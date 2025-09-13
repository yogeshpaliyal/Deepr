package com.yogeshpaliyal.deepr.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.sync.SyncRepository
import com.yogeshpaliyal.deepr.util.RequestResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

enum class SortOrder {
    ASC,
    DESC,
    OPENED_ASC,
    OPENED_DESC,
}

class AccountViewModel(
    private val deeprQueries: DeeprQueries,
    private val exportRepository: ExportRepository,
    private val importRepository: ImportRepository,
    private val syncRepository: SyncRepository,
) : ViewModel(),
    KoinComponent {
    private val preferenceDataStore: AppPreferenceDataStore = get()
    private val searchQuery = MutableStateFlow("")
    private val sortOrder: Flow<SortOrder> =
        preferenceDataStore.getSortingOrder.map { sortOrderName ->
            SortOrder.valueOf(sortOrderName)
        }

    private val exportResultChannel = Channel<String>()
    val exportResultFlow = exportResultChannel.receiveAsFlow()

    private val importResultChannel = Channel<String>()
    val importResultFlow = importResultChannel.receiveAsFlow()

    private val syncResultChannel = Channel<String>()
    val syncResultFlow = syncResultChannel.receiveAsFlow()

    private val syncValidationChannel = Channel<String>()
    val syncValidationFlow = syncValidationChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<Deepr>?> =
        combine(searchQuery, sortOrder) { query, order ->
            Pair(query, order)
        }.flatMapLatest { (query, order) ->
            if (query.isBlank()) {
                when (order) {
                    SortOrder.ASC -> deeprQueries.listDeeprAsc()
                    SortOrder.DESC -> deeprQueries.listDeeprDesc()
                    SortOrder.OPENED_ASC -> deeprQueries.listDeeprByOpenedCountAsc()
                    SortOrder.OPENED_DESC -> deeprQueries.listDeeprByOpenedCountDesc()
                }.asFlow().mapToList(viewModelScope.coroutineContext)
            } else {
                when (order) {
                    SortOrder.ASC -> deeprQueries.searchDeeprAsc(query)
                    SortOrder.DESC -> deeprQueries.searchDeeprDesc(query)
                    SortOrder.OPENED_ASC -> deeprQueries.searchDeeprByOpenedCountAsc(query)
                    SortOrder.OPENED_DESC -> deeprQueries.searchDeeprByOpenedCountDesc(query)
                }.asFlow().mapToList(viewModelScope.coroutineContext)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun search(query: String) {
        searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferenceDataStore.setSortingOrder(order.name)
        }
    }

    fun insertAccount(
        link: String,
        name: String,
        executed: Boolean,
    ) {
        viewModelScope.launch {
            deeprQueries.insertDeepr(link = link, name, if (executed) 1 else 0)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            deeprQueries.deleteDeeprById(id)
        }
    }

    fun incrementOpenedCount(id: Long) {
        viewModelScope.launch {
            deeprQueries.incrementOpenedCount(id)
        }
    }

    fun updateDeeplink(
        id: Long,
        newLink: String,
        newName: String,
    ) {
        viewModelScope.launch {
            deeprQueries.updateDeeplink(newLink, newName, id)
        }
    }

    fun exportCsvData() {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceDataStore.setSyncEnabled(enabled)
        }
    }

    fun setSyncFilePath(path: String) {
        viewModelScope.launch {
            preferenceDataStore.setSyncFilePath(path)
            // Validate the file after setting the path
            validateSyncFile(path)
        }
    }

    fun syncToMarkdown() {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
