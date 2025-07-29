package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

enum class SortOrder {
    ASC, DESC, OPENED_ASC, OPENED_DESC
}

class AccountViewModel(private val deeprQueries: DeeprQueries) : ViewModel(), KoinComponent {

    private val preferenceDataStore: AppPreferenceDataStore = get()
    private val searchQuery = MutableStateFlow("")
    private val sortOrder: Flow<SortOrder> =
        preferenceDataStore.getSortingOrder.map { sortOrderName ->
            SortOrder.valueOf(sortOrderName)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<Deepr>> =
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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferenceDataStore.setSortingOrder(order.name)
        }
    }

    fun insertAccount(link: String, executed: Boolean) {
        viewModelScope.launch {
            deeprQueries.insertDeepr(link = link, if (executed) 1 else 0)
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

    fun updateDeeplink(id: Long, newLink: String) {
        viewModelScope.launch {
            deeprQueries.updateDeeplink(newLink, id)
        }
    }
}