package com.yogeshpaliyal.deepr.analytics

object AnalyticsManagerFactory {
    fun create(): AnalyticsManager = NoOpAnalyticsManager()
}
