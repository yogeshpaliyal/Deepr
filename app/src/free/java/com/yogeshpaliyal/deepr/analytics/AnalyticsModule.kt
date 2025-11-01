package com.yogeshpaliyal.deepr.analytics

import org.koin.dsl.module

val analyticsModule =
    module {
        single<AnalyticsManager> {
            AnalyticsManagerFactory.create()
        }
    }
