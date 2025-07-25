package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openDeeplink(context: Context, link: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        // Optionally, show a toast or a dialog to the user that the link is invalid
        false
    }
}
