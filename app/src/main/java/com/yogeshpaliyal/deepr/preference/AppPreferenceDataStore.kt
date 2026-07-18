package com.yogeshpaliyal.deepr.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yogeshpaliyal.deepr.ui.screens.home.ViewType
import com.yogeshpaliyal.deepr.viewmodel.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
        private val VIEW_TYPE = intPreferencesKey("view_type")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val SHOW_OPEN_COUNTER = booleanPreferencesKey("show_open_counter")
        private val SELECTED_PROFILE_ID = longPreferencesKey("selected_profile_id")
        private val SILENT_SAVE_PROFILE_ID = longPreferencesKey("silent_save_profile_id")
        private val GOOGLE_DRIVE_AUTO_BACKUP_ENABLED =
            booleanPreferencesKey("google_drive_auto_backup_enabled")
        private val CLIPBOARD_LINK_DETECTION_ENABLED =
            booleanPreferencesKey("clipboard_link_detection_enabled")
    }

    val getSortingOrder: Flow<@SortType String> =
        context.appDataStore.data.map { preferences ->
            preferences[SORTING_ORDER] ?: SortType.SORT_CREATED_BY_DESC
        }

    val viewType: Flow<@ViewType Int> =
        context.appDataStore.data.map { preferences ->
            preferences[VIEW_TYPE] ?: ViewType.LIST
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

    val getThemeMode: Flow<String> =
        context.appDataStore.data.map { preferences ->
            preferences[THEME_MODE] ?: "system" // Default to system theme
        }

    val getShowOpenCounter: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[SHOW_OPEN_COUNTER] ?: true // Default to showing counter
        }

    val getSelectedProfileId: Flow<Long> =
        context.appDataStore.data.map { preferences ->
            preferences[SELECTED_PROFILE_ID] ?: 1L // Default to profile ID 1
        }

    val getSilentSaveProfileId: Flow<Long> =
        context.appDataStore.data.map { preferences ->
            preferences[SILENT_SAVE_PROFILE_ID] ?: getSelectedProfileId.firstOrNull() ?: 1L // Default to profile ID 1
        }

    val getGoogleDriveAutoBackupEnabled: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[GOOGLE_DRIVE_AUTO_BACKUP_ENABLED] ?: false // Default to disabled
        }

    val getClipboardLinkDetectionEnabled: Flow<Boolean> =
        context.appDataStore.data.map { preferences ->
            preferences[CLIPBOARD_LINK_DETECTION_ENABLED] ?: true // Default to enabled
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

    suspend fun setViewType(viewType: @ViewType Int) {
        context.appDataStore.edit { prefs ->
            prefs[VIEW_TYPE] = viewType
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

    suspend fun setThemeMode(mode: String) {
        context.appDataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun setShowOpenCounter(show: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[SHOW_OPEN_COUNTER] = show
        }
    }

    suspend fun setSelectedProfileId(profileId: Long) {
        context.appDataStore.edit { prefs ->
            prefs[SELECTED_PROFILE_ID] = profileId
        }
    }

    suspend fun setSilentSaveProfileId(profileId: Long) {
        context.appDataStore.edit { prefs ->
            prefs[SILENT_SAVE_PROFILE_ID] = profileId
        }
    }

    suspend fun setGoogleDriveAutoBackupEnabled(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[GOOGLE_DRIVE_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setClipboardLinkDetectionEnabled(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[CLIPBOARD_LINK_DETECTION_ENABLED] = enabled
        }
    }
}
