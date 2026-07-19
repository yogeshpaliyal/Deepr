package com.yogeshpaliyal.deepr.server

import android.content.Context
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.PreferenceRepository
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.client.HttpClient

class LocalServerTransferLink(
    context: Context,
    linkRepository: LinkRepository,
    httpClient: HttpClient,
    accountViewModel: AccountViewModel,
    networkRepository: NetworkRepository,
    preferenceRepository: PreferenceRepository,
    analyticsManager: AnalyticsManager,
) : LocalServerRepositoryImpl(
        context,
        linkRepository,
        httpClient,
        accountViewModel,
        networkRepository,
        analyticsManager,
        preferenceRepository,
    ) {
    override suspend fun startServer(port: Int) {
        super.startServer(port)
    }
}
