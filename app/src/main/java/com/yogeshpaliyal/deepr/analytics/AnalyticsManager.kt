package com.yogeshpaliyal.deepr.analytics

interface AnalyticsManager {
    fun logEvent(
        eventName: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun setUserProperty(
        propertyName: String,
        value: String,
    )
}
