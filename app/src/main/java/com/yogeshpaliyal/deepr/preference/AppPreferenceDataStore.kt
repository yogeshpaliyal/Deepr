package com.yogeshpaliyal.deepr.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yogeshpaliyal.deepr.viewmodel.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data_store")

class AppPreferenceDataStore(
    private val context: Context,
) {
    companion object {
        private val SORTING_ORDER = stringPreferencesKey("sorting_order")
    }

    val getSortingOrder: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[SORTING_ORDER] ?: SortOrder.DESC.name
        }

    suspend fun setSortingOrder(order: String) {
        context.appDataStore.edit { prefs ->
            prefs[SORTING_ORDER] = order
        }
    }
}
