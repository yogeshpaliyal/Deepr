package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.annotation.StringRes
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R

/**
 * Actions that can be bound to tapping or long pressing a link entry.
 *
 * [key] is persisted in preferences, so it must never change for an existing entry.
 */
enum class LinkAction(
    val key: String,
    @StringRes val label: Int,
) {
    OPEN("open", R.string.link_action_open),
    OPEN_WITH("open_with", R.string.open_with),
    COPY("copy", R.string.copy_link),
    SHARE("share", R.string.share_link),
    EDIT("edit", R.string.edit),
    SHOW_QR_CODE("show_qr_code", R.string.show_qr_code),
    TOGGLE_FAVOURITE("toggle_favourite", R.string.link_action_toggle_favourite),
    MORE_OPTIONS("more_options", R.string.more_options),
    ;

    fun toMenuItem(item: GetLinksAndTags): MenuItem =
        when (this) {
            OPEN -> MenuItem.Click(item)
            OPEN_WITH -> MenuItem.OpenWith(item)
            COPY -> MenuItem.Copy(item)
            SHARE -> MenuItem.Share(item)
            EDIT -> MenuItem.Edit(item)
            SHOW_QR_CODE -> MenuItem.ShowQrCode(item)
            TOGGLE_FAVOURITE -> MenuItem.FavouriteClick(item)
            MORE_OPTIONS -> MenuItem.MoreOptionsBottomSheet(item)
        }

    companion object {
        val DEFAULT_TAP = OPEN
        val DEFAULT_LONG_PRESS = COPY

        fun fromKey(
            key: String?,
            default: LinkAction,
        ): LinkAction = entries.firstOrNull { it.key == key } ?: default
    }
}
