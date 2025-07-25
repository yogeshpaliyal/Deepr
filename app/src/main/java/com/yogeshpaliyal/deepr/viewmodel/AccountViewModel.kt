package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    ASC, DESC
}

class AccountViewModel(private val deeprQueries: DeeprQueries) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.DESC)

    val accounts: StateFlow<List<Deepr>> =
        combine(searchQuery, sortOrder) { query, order ->
            Pair(query, order)
        }.flatMapLatest { (query, order) ->
            if (query.isBlank()) {
                when (order) {
                    SortOrder.ASC -> deeprQueries.listDeeprAsc()
                    SortOrder.DESC -> deeprQueries.listDeeprDesc()
                }.asFlow().mapToList(viewModelScope.coroutineContext)
            } else {
                when (order) {
                    SortOrder.ASC -> deeprQueries.searchDeeprAsc(query)
                    SortOrder.DESC -> deeprQueries.searchDeeprDesc(query)
                }.asFlow().mapToList(viewModelScope.coroutineContext)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
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
}