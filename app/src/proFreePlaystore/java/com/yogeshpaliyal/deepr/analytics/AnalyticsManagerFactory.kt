package com.yogeshpaliyal.deepr.analytics

import android.content.Context

object AnalyticsManagerFactory {
    fun create(context: Context): AnalyticsManager = FirebaseAnalyticsManager(context)
}
