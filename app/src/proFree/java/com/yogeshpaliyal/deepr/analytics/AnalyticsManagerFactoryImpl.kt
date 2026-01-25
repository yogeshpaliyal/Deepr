package com.yogeshpaliyal.deepr.analytics

import android.content.Context

object AnalyticsManagerFactoryImpl : AnalyticsManagerFactory {
    override fun create(context: Context): AnalyticsManager = FirebaseAnalyticsManager(context)
}
