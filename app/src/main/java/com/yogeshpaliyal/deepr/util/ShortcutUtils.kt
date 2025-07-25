package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.R

fun createShortcut(context: Context, deepr: Deepr, shortcutName: String) {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        val shortcutInfo = ShortcutInfoCompat.Builder(context, "deepr_${deepr.id}")
            .setShortLabel(shortcutName)
            .setLongLabel(shortcutName)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(Intent(Intent.ACTION_VIEW, deepr.link.toUri()).apply {

            })
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
