package com.yogeshpaliyal.deepr.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsManager(context: Context) : AnalyticsManager {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(
        eventName: String,
        params: Map<String, Any>,
    ) {
        val bundle =
            Bundle().apply {
                params.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        firebaseAnalytics.setUserProperty(propertyName, value)
    }
}
