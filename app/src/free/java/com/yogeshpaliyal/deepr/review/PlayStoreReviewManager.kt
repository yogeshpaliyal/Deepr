package com.yogeshpaliyal.deepr.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.BuildConfig

class PlayStoreReviewManager : ReviewManager {
    override fun requestReview(activity: Activity) {
        val appPackageName = BuildConfig.APPLICATION_ID
        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$appPackageName".toUri(),
                ),
            )
        } catch (e: ActivityNotFoundException) {
            // Handle case where Play Store is not available
        }
    }
}
