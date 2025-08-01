package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

fun openDeeplink(context: Context, link: String): Boolean {
    if (!isValidDeeplink(link)) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Invalid deeplink: $link", Toast.LENGTH_SHORT).show()
        // Optionally, show a toast or a dialog to the user that the link is invalid
        false
    }
}

fun isValidDeeplink(link: String): Boolean {
    if (link.isBlank()) return false
    return try {
        link.toUri()
        return true
    } catch (e: Exception) {
        false
    }
}
