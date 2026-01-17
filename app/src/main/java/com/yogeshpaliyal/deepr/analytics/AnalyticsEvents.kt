package com.yogeshpaliyal.deepr.analytics

object AnalyticsEvents {
    // Link events
    const val ADD_LINK = "add_link"
    const val EDIT_LINK = "edit_link"
    const val DELETE_LINK = "delete_link"
    const val OPEN_LINK = "open_link"
    const val COPY_LINK = "copy_link"
    const val SHARE_LINK = "share_link"
    const val TOGGLE_FAVOURITE = "toggle_favourite"
    const val RESET_COUNTER = "reset_counter"

    // Navigation events
    const val NAVIGATE_SETTINGS = "navigate_settings"
    const val NAVIGATE_ABOUT = "navigate_about"
    const val NAVIGATE_LOCAL_SERVER = "navigate_local_server"

    // Server events
    const val START_LOCAL_SERVER = "start_local_server"
    const val STOP_LOCAL_SERVER = "stop_local_server"

    // Sorting events
    const val CHANGE_SORT_ORDER = "change_sort_order"

    // Tag events
    const val SELECT_TAG_FILTER = "select_tag_filter"
    const val CLEAR_TAG_FILTER = "clear_tag_filter"

    // Search events
    const val SEARCH_LINKS = "search_links"

    // Filter events
    const val FILTER_FAVOURITES = "filter_favourites"

    // Import/Export events
    const val EXPORT_CSV = "export_csv"
    const val IMPORT_CSV = "import_csv"

    // Shortcut events
    const val CREATE_SHORTCUT = "create_shortcut"

    // QR Code events
    const val SCAN_QR_CODE = "scan_qr_code"
    const val SHOW_QR_CODE = "show_qr_code"

    // Menu events
    const val ITEM_MENU_EDIT = "item_menu_edit"
    const val ITEM_MENU_DELETE = "item_menu_delete"
    const val ITEM_MENU_SHARE = "item_menu_share"
    const val ITEM_MENU_COPY = "item_menu_copy"
    const val ITEM_MENU_SHORTCUT = "item_menu_shortcut"
    const val ITEM_MENU_QR_CODE = "item_menu_qr_code"
    const val ITEM_MENU_RESET_COUNTER = "item_menu_reset_counter"
    const val ITEM_MENU_FAVOURITE = "item_menu_favourite"

    // Profile events
    const val CREATE_PROFILE = "create_profile"
    const val SWITCH_PROFILE = "switch_profile"
    const val DELETE_PROFILE = "delete_profile"
    const val EDIT_PROFILE = "edit_profile"
}

object AnalyticsParams {
    // Common parameters
    const val LINK_ID = "link_id"
    const val LINK_URL = "link_url"
    const val LINK_NAME = "link_name"
    const val TAG_ID = "tag_id"
    const val TAG_NAME = "tag_name"
    const val SORT_ORDER = "sort_order"
    const val IS_FAVOURITE = "is_favourite"
    const val COUNT = "count"
    const val SERVER_PORT = "server_port"
    const val HAS_THUMBNAIL = "has_thumbnail"
    const val SEARCH_QUERY = "search_query"
}

object AnalyticsUserProperties {
    const val TOTAL_LINKS = "total_links"
    const val TOTAL_TAGS = "total_tags"
    const val THUMBNAIL_ENABLED = "thumbnail_enabled"
}
