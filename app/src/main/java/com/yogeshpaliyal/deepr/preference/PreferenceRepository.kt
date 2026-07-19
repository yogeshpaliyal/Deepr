package com.yogeshpaliyal.deepr.preference

import com.yogeshpaliyal.deepr.ui.screens.home.ViewType
import com.yogeshpaliyal.deepr.viewmodel.SortType
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val getSortingOrder: Flow<@SortType String>
    val viewType: Flow<@ViewType Int>
    val getUseLinkBasedIcons: Flow<Boolean>
    val getSyncEnabled: Flow<Boolean>
    val getSyncFilePath: Flow<String>
    val getLastSyncTime: Flow<Long>
    val getLanguageCode: Flow<String>
    val getAutoBackupEnabled: Flow<Boolean>
    val getAutoBackupLocation: Flow<String>
    val getLastBackupTime: Flow<Long>
    val getDefaultPageFavourites: Flow<Boolean>
    val isThumbnailEnable: Flow<Boolean>
    val getServerPort: Flow<String>
    val getThemeMode: Flow<String>
    val getShowOpenCounter: Flow<Boolean>
    val getSelectedProfileId: Flow<Long>
    val getSilentSaveProfileId: Flow<Long>
    val getGoogleDriveAutoBackupEnabled: Flow<Boolean>
    val getClipboardLinkDetectionEnabled: Flow<Boolean>

    suspend fun setSortingOrder(order: @SortType String)

    suspend fun setUseLinkBasedIcons(useLink: Boolean)

    suspend fun setSyncEnabled(enabled: Boolean)

    suspend fun setViewType(viewType: @ViewType Int)

    suspend fun setSyncFilePath(path: String)

    suspend fun setLastSyncTime(timestamp: Long)

    suspend fun setLanguageCode(code: String)

    suspend fun setAutoBackupEnabled(enabled: Boolean)

    suspend fun setAutoBackupLocation(location: String)

    suspend fun setAutoBackupInterval(interval: Long)

    suspend fun setLastBackupTime(timestamp: Long)

    suspend fun setDefaultPageFavourites(favourites: Boolean)

    suspend fun setThumbnailEnable(thumbnail: Boolean)

    suspend fun setServerPort(port: String)

    suspend fun setThemeMode(mode: String)

    suspend fun setShowOpenCounter(show: Boolean)

    suspend fun setSelectedProfileId(profileId: Long)

    suspend fun setSilentSaveProfileId(profileId: Long)

    suspend fun setGoogleDriveAutoBackupEnabled(enabled: Boolean)

    suspend fun setClipboardLinkDetectionEnabled(enabled: Boolean)
}
