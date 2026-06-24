package com.yogeshpaliyal.deepr.util

object Constants {
    object Header {
        const val LINK = "Link"
        const val OPENED_COUNT = "OpenedCount"
        const val NAME = "Name"
        const val CREATED_AT = "CreatedAt"
        const val NOTES = "Notes"
        const val TAGS = "Tags"
        const val THUMBNAIL = "Thumbnail"
        const val IS_FAVOURITE = "isFavourite"
        const val PROFILE_NAME = "profileName"
    }

    /**
     * Marker and keys used to embed app settings in CSV export files.
     * Settings rows use a blank Link column for backward compatibility
     * (old importers skip rows with blank links).
     */
    object Settings {
        const val MARKER = "__DEEPR_SETTING__"
        const val SORTING_ORDER = "sortingOrder"
        const val VIEW_TYPE = "viewType"
        const val USE_LINK_BASED_ICONS = "useLinkBasedIcons"
        const val DEFAULT_PAGE_FAVOURITES = "defaultPageFavourites"
        const val IS_THUMBNAIL_ENABLE = "isThumbnailEnable"
        const val THEME_MODE = "themeMode"
        const val SHOW_OPEN_COUNTER = "showOpenCounter"
        const val CLIPBOARD_LINK_DETECTION_ENABLED = "clipboardLinkDetectionEnabled"
    }
}
