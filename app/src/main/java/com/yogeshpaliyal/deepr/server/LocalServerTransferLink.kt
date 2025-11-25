package com.yogeshpaliyal.deepr.server

import android.content.Context
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.LinksDataRepository
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import io.ktor.client.HttpClient

class LocalServerTransferLink(
    context: Context,
    linksDataRepository: LinksDataRepository,
    httpClient: HttpClient,
    networkRepository: NetworkRepository,
    preferenceDataStore: AppPreferenceDataStore,
    analyticsManager: AnalyticsManager,
) : LocalServerRepositoryImpl(
        context,
        linksDataRepository,
        httpClient,
        networkRepository,
        analyticsManager,
        preferenceDataStore,
    ) {
    override suspend fun startServer(port: Int) {
        super.startServer(port)
    }
}
