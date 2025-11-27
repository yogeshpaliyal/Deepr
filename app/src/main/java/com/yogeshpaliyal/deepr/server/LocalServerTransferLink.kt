package com.yogeshpaliyal.deepr.server

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.DataProvider
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.client.HttpClient

class LocalServerTransferLink(
    context: Context,
    httpClient: HttpClient,
    accountViewModel: AccountViewModel,
    networkRepository: NetworkRepository,
    preferenceDataStore: AppPreferenceDataStore,
    analyticsManager: AnalyticsManager,
    dataProvider: DataProvider,
) : LocalServerRepositoryImpl(
        context,
        httpClient,
        accountViewModel,
        networkRepository,
        analyticsManager,
        preferenceDataStore,
        dataProvider,
    ) {
    override suspend fun startServer(port: Int) {
        super.startServer(port)
    }
}
