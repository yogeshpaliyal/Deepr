package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.Deepr

fun createShortcut(
    context: Context,
    deepr: Deepr,
    shortcutName: String,
    alreadyExists: Boolean,
    useLinkBasedIcon: Boolean,
) {
    if (isShortcutSupported(context)) {
        val shortcutInfo =
            ShortcutInfoCompat
                .Builder(context, "deepr_${deepr.id}")
                .setShortLabel(shortcutName)
                .setLongLabel(shortcutName)
                .setIcon(getShortcutAppIcon(context, deepr.link, useLinkBasedIcon))
                .setIntent(
                    Intent(Intent.ACTION_VIEW, deepr.link.toUri()),
                ).build()
        if (alreadyExists) {
            // If the shortcut already exists, we update it
            ShortcutManagerCompat.updateShortcuts(context, listOf(shortcutInfo))
        } else {
            // Otherwise, we request to pin the new shortcut
            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        }
    }
}

fun isShortcutSupported(context: Context): Boolean {
    // Check if the device supports pinned shortcuts
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        ShortcutManagerCompat.isRequestPinShortcutSupported(context)
}

/**
 * Checks if a shortcut already exists for the given deeplink
 */
fun getShortcut(
    context: Context,
    deeprId: Long,
): ShortcutInfo? {
    // On Android 10+ (API 29+), we can check for pinned shortcuts
    if (isShortcutSupported(context)) {
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as android.content.pm.ShortcutManager?

        // Get all pinned shortcuts if available
        val shortcuts = shortcutManager?.pinnedShortcuts

        // Check if our shortcut exists
        return shortcuts?.find { it.id == "deepr_$deeprId" }
    }

    // For older Android versions, we can't reliably check if a shortcut exists
    // So we'll return false by default
    return null
}

/**
 * Checks if a shortcut already exists for the given deeplink
 */
fun hasShortcut(
    context: Context,
    deeprId: Long,
): Boolean = getShortcut(context, deeprId) != null
