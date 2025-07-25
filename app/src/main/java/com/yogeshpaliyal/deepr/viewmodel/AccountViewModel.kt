package com.yogeshpaliyal.deepr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(private val deeprQueries: DeeprQueries) : ViewModel() {

    val accounts: StateFlow<List<Deepr>> =
        deeprQueries.listDeepr().asFlow().mapToList(viewModelScope.coroutineContext)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertAccount(link: String) {
        viewModelScope.launch {
            deeprQueries.insertDeepr(link = link)
        }
    }
}