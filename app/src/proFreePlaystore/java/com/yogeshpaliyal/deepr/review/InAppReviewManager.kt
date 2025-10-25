package com.yogeshpaliyal.deepr.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

class InAppReviewManager : ReviewManager {
    override fun requestReview(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                }
            }
        }
    }
}
