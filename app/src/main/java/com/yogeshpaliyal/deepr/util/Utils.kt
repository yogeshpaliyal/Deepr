package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.core.net.toUri

fun openDeeplink(context: Context, link: String): Boolean {
    if (!isValidDeeplink(link)) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        // Optionally, show a toast or a dialog to the user that the link is invalid
        false
    }
}

fun isValidDeeplink(link: String): Boolean {
    if (link.isBlank()) return false
    return try {
        val uri = link.toUri()
        uri.scheme != null
    } catch (e: Exception) {
        false
    }
}
