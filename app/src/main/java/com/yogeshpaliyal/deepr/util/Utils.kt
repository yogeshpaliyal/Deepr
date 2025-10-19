package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.GetLinkById
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R

fun openDeeplink(
    context: Context,
    link: String,
): Boolean {
    if (!isValidDeeplink(link)) return false
    val normalizedLink = normalizeLink(link)
    return try {
        val intent = Intent(Intent.ACTION_VIEW, normalizedLink.toUri())
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast
            .makeText(
                context,
                context.getString(R.string.invalid_deeplink_toast, normalizedLink),
                Toast.LENGTH_SHORT,
            ).show()
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
        val normalizedLink = normalizeLink(link)
        val intent = Intent(Intent.ACTION_VIEW, normalizedLink.toUri())
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

fun normalizeLink(link: String): String {
    if (link.isBlank()) return link

    val trimmedLink = link.trim()

    // Check if the link already has a scheme
    if (trimmedLink.contains("://")) {
        return trimmedLink
    }

    // If it looks like a URL (contains a dot and doesn't start with a scheme),
    // prepend https://
    if (trimmedLink.contains(".")) {
        return "https://$trimmedLink"
    }

    // Return as-is for other cases (like custom schemes without ://)
    return trimmedLink
}

fun isValidDeeplink(link: String): Boolean {
    if (link.isBlank()) return false
    return try {
        val normalizedLink = normalizeLink(link)
        val uri = normalizedLink.toUri()
        val hasValidScheme = uri.scheme != null && uri.scheme!!.isNotBlank()
        val hasValidAuthority = uri.authority != null && uri.authority!!.isNotBlank()
        hasValidScheme && hasValidAuthority
    } catch (_: Exception) {
        false
    }
}

fun GetLinkById.toGetLinksAndTags() =
    GetLinksAndTags(
        id = id,
        link = link,
        name = name,
        createdAt = createdAt,
        openedCount = openedCount,
        isFavourite = isFavourite,
        notes = notes,
        lastOpenedAt = lastOpenedAt,
        tagsNames = tagsNames,
        tagsIds = tagsIds,
    )
