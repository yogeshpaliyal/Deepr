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

fun openDeeplinkExternal(
    context: Context,
    link: String,
): Boolean {
    if (!isValidDeeplink(link)) return false
    val normalizedLink = normalizeLink(link)

    // The deep link URL the user clicked (e.g., a Flipkart product URL)
    val deepLinkUri = normalizedLink.toUri()

    // 1. Create the base Intent (ACTION_VIEW for the URL)
    val baseIntent = Intent(Intent.ACTION_VIEW, deepLinkUri)

    // 2. Query the system for all activities that can handle the baseIntent
    val packageManager: PackageManager = context.packageManager
    val resolvedInfoList = packageManager.queryIntentActivities(baseIntent, PackageManager.MATCH_ALL)

    // 3. Create a list to hold the Intents for the Chooser dialog
    val initialIntents: MutableList<Intent> = ArrayList()

    // 4. Iterate through the resolved activities (apps and browsers)
    for (info in resolvedInfoList) {
        val packageName = info.activityInfo.packageName

        // Check if the resolved activity is NOT the package that would open automatically.
        // In this case, we're adding the browser as the 'initial intent' (which is the default behavior),
        // and letting the Chooser handle the rest.

        // **KEY STEP:** If the resolved activity is not the specific app (Flipkart) and is not a browser,
        // it's an app we want to explicitly list.

        // This creates a new intent for non-browser apps like your own deep-link app
        val newIntent =
            Intent(baseIntent).apply {
                setPackage(packageName)
            }
        initialIntents.add(newIntent)
    }

    // 5. Create the Chooser Intent. The key trick here is sometimes using a generic intent
    // for the primary Chooser target, but a simpler method is just using the baseIntent
    // and adding all other options as 'initial intents'
    val chooserIntent = Intent.createChooser(baseIntent, "Choose how to open the link")

// 6. Add all the explicitly resolved app Intents to the chooser
    if (initialIntents.isNotEmpty()) {
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            initialIntents.toTypedArray(),
        )
    }

    // 7. Start the chooser
    context.startActivity(chooserIntent)
    return true
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
