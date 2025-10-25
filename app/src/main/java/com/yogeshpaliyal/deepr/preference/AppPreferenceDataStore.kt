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
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val AUTO_BACKUP_LOCATION = stringPreferencesKey("auto_backup_location")
        private val AUTO_BACKUP_INTERVAL = longPreferencesKey("auto_backup_interval")
        private val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        private val DEFAULT_PAGE_FAVOURITES = booleanPreferencesKey("default_page_favourites")
        private val IS_THUMBNAIL_ENABLE = booleanPreferencesKey("is_thumbnail_enable")
        private val SERVER_PORT = stringPreferencesKey("server_port")
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

    val getLanguageCode: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[LANGUAGE_CODE] ?: "" // Default to system language
        }

    val getAutoBackupEnabled: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[AUTO_BACKUP_ENABLED] ?: false // Default to disabled
        }

    val getAutoBackupLocation: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[AUTO_BACKUP_LOCATION] ?: "" // Default to empty path
        }

    val getLastBackupTime: Flow<Long> =
        context.appDataStore.data.map { preferences ->
            preferences[LAST_BACKUP_TIME] ?: 0L // Default to 0 (never backed up)
        }

    val getDefaultPageFavourites: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[DEFAULT_PAGE_FAVOURITES] ?: false // Default to All (-1)
        }

    val isThumbnailEnable: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[IS_THUMBNAIL_ENABLE] ?: false
        }

    val getServerPort: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[SERVER_PORT] ?: "" // Default to empty string
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

    suspend fun setLanguageCode(code: String) {
        context.appDataStore.edit { prefs ->
            prefs[LANGUAGE_CODE] = code
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setAutoBackupLocation(location: String) {
        context.appDataStore.edit { prefs ->
            prefs[AUTO_BACKUP_LOCATION] = location
        }
    }

    suspend fun setAutoBackupInterval(interval: Long) {
        context.appDataStore.edit { prefs ->
            prefs[AUTO_BACKUP_INTERVAL] = interval
        }
    }

    suspend fun setLastBackupTime(timestamp: Long) {
        context.appDataStore.edit { prefs ->
            prefs[LAST_BACKUP_TIME] = timestamp
        }
    }

    suspend fun setDefaultPageFavourites(favourites: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[DEFAULT_PAGE_FAVOURITES] = favourites
        }
    }

    suspend fun setThumbnailEnable(thumbnail: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[IS_THUMBNAIL_ENABLE] = thumbnail
        }
    }

    suspend fun setServerPort(port: String) {
        context.appDataStore.edit { prefs ->
            prefs[SERVER_PORT] = port
        }
    }
}
