package com.yogeshpaliyal.deepr.server

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.client.HttpClient

class LocalServerTransferLink(
    context: Context,
    deeprQueries: DeeprQueries,
    httpClient: HttpClient,
    accountViewModel: AccountViewModel,
    networkRepository: NetworkRepository,
    preferenceDataStore: AppPreferenceDataStore,
    analyticsManager: AnalyticsManager,
) : LocalServerRepositoryImpl(
        context,
        deeprQueries,
        httpClient,
        accountViewModel,
        networkRepository,
        analyticsManager,
        preferenceDataStore,
    ) {
    override suspend fun startServer(port: Int) {
        super.startServer(port)
    }
}
