package com.yogeshpaliyal.deepr

import android.app.Application
import android.util.Log
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.analytics.AnalyticsManagerFactory
import com.yogeshpaliyal.deepr.backup.AutoBackupWorker
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ExportRepositoryImpl
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepositoryImpl
import com.yogeshpaliyal.deepr.data.HtmlParser
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.review.ReviewManager
import com.yogeshpaliyal.deepr.review.ReviewManagerFactory
import com.yogeshpaliyal.deepr.server.LocalServerRepository
import com.yogeshpaliyal.deepr.server.LocalServerRepositoryImpl
import com.yogeshpaliyal.deepr.sync.SyncRepository
import com.yogeshpaliyal.deepr.sync.SyncRepositoryImpl
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import com.yogeshpaliyal.deepr.viewmodel.LocalServerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class DeeprApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule =
            module {
                // Provide the Android-specific SqlDriver
                single<SqlDriver> {
                    LogSqliteDriver(
                        AndroidSqliteDriver(
                            DeeprDB.Schema,
                            this@DeeprApplication,
                            "deepr.db",
                        ),
                    ) {
                        Log.d("loggingDB", it)
                    }
                }

                // Provide the Database instance
                single {
                    DeeprDB(get())
                }

                // Provide the generated queries from the database.
                // Replace `accountQueries` if your table has a different name.
                single<DeeprQueries> {
                    val database = get<DeeprDB>()
                    database.deeprQueries
                }

                single { AppPreferenceDataStore(androidContext()) }

                single<ExportRepository> { ExportRepositoryImpl(androidContext(), get()) }

                single<ImportRepository> { ImportRepositoryImpl(androidContext(), get()) }

                single<SyncRepository> { SyncRepositoryImpl(androidContext(), get(), get()) }

                single<AutoBackupWorker> { AutoBackupWorker(androidContext(), get(), get()) }

                single {
                    HttpClient(CIO)
                }

                viewModel { AccountViewModel(get(), get(), get(), get(), get(), get(), get()) }

                single {
                    HtmlParser()
                }

                single {
                    NetworkRepository(get(), get())
                }

                single<LocalServerRepository> {
                    LocalServerRepositoryImpl(androidContext(), get(), get(), get(), get())
                }

                viewModel {
                    LocalServerViewModel(get())
                }

                single<ReviewManager> {
                    ReviewManagerFactory.create()
                }

                single<AnalyticsManager> {
                    AnalyticsManagerFactory.create(androidContext())
                }
            }

        startKoin {
            // Provide Android context to Koin
            androidContext(this@DeeprApplication)

            modules(appModule)
        }
    }
}
