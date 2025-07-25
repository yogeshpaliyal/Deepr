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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(private val deeprQueries: DeeprQueries) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val accounts: StateFlow<List<Deepr>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            deeprQueries.listDeepr().asFlow().mapToList(viewModelScope.coroutineContext)
        } else {
            deeprQueries.searchDeepr(query).asFlow().mapToList(viewModelScope.coroutineContext)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        searchQuery.value = query
    }

    fun insertAccount(link: String) {
        viewModelScope.launch {
            deeprQueries.insertDeepr(link = link)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            deeprQueries.deleteDeeprById(id)
        }
    }
}