package com.yogeshpaliyal.deepr.analytics

import com.yogeshpaliyal.shared.analytics.AnalyticsManager
import com.yogeshpaliyal.shared.analytics.AnalyticsManagerFactory

object AnalyticsManagerFactoryImpl : AnalyticsManagerFactory {
    override fun create(): AnalyticsManager = NoOpAnalyticsManager()
}
