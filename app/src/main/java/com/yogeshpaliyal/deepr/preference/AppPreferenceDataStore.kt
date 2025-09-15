package com.yogeshpaliyal.deepr.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yogeshpaliyal.deepr.viewmodel.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data_store")

class AppPreferenceDataStore(
    private val context: Context,
) {
    companion object {
        private val SORTING_ORDER = stringPreferencesKey("sorting_order")
        private val USE_LINK_BASED_ICONS = booleanPreferencesKey("use_link_based_icons")
        private val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        private val SYNC_FILE_PATH = stringPreferencesKey("sync_file_path")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val getSortingOrder: Flow<@SortType String> =
        context.appDataStore.data.map { preferences ->
            preferences[SORTING_ORDER] ?: SortType.SORT_CREATED_BY_DESC
        }

    val getUseLinkBasedIcons: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[USE_LINK_BASED_ICONS] ?: true // Default to link-based icons
        }

    val getSyncEnabled: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[SYNC_ENABLED] ?: false // Default to disabled
        }

    val getSyncFilePath: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[SYNC_FILE_PATH] ?: "" // Default to empty path
        }

    val getLastSyncTime: Flow<Long> =
        context.appDataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIME] ?: 0L // Default to 0 (never synced)
        }

    suspend fun setSortingOrder(order: @SortType String) {
        context.appDataStore.edit { prefs ->
            prefs[SORTING_ORDER] = order
        }
    }

    suspend fun setUseLinkBasedIcons(useLink: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[USE_LINK_BASED_ICONS] = useLink
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[SYNC_ENABLED] = enabled
        }
    }

    suspend fun setSyncFilePath(path: String) {
        context.appDataStore.edit { prefs ->
            prefs[SYNC_FILE_PATH] = path
        }
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        context.appDataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = timestamp
        }
    }
}
