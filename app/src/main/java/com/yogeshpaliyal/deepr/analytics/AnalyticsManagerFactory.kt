package com.yogeshpaliyal.deepr.analytics

import android.content.Context

interface AnalyticsManagerFactory {
    fun create(context: Context): AnalyticsManager
}
