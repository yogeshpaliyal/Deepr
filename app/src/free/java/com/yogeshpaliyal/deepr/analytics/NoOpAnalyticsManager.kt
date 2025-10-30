package com.yogeshpaliyal.deepr.analytics

class NoOpAnalyticsManager : AnalyticsManager {
    override fun logEvent(
        eventName: String,
        params: Map<String, Any>,
    ) {
        // No-op implementation for free variant
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        // No-op implementation for free variant
    }
}
