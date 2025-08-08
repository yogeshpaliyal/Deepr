package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.R

fun openDeeplink(
    context: Context,
    link: String,
): Boolean {
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

fun getShortcutAppIcon(
    context: Context,
    link: String,
    useLinkBasedIcon: Boolean,
): IconCompat {
    if (!useLinkBasedIcon) {
        // If link-based icons are not used, return the default app icon
        return IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        val appIconDrawable = context.packageManager.getActivityIcon(intent)

        // Convert the Drawable to a Bitmap
        val bitmap =
            createBitmap(appIconDrawable.intrinsicWidth, appIconDrawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        appIconDrawable.setBounds(0, 0, canvas.width, canvas.height)
        appIconDrawable.draw(canvas)
        // Create and return the IconCompat object
        return IconCompat.createWithBitmap(bitmap)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        // If the icon cannot be found, return a default icon
        return IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    }
}

fun isValidDeeplink(link: String): Boolean {
    if (link.isBlank()) return false
    return try {
        val uri = link.toUri()
        uri.scheme != null && uri.scheme!!.isNotBlank()
    } catch (_: Exception) {
        false
    }
}
