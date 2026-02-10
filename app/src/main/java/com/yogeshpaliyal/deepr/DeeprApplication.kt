package com.yogeshpaliyal.deepr

import android.app.Application
import android.util.Log
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.analytics.AnalyticsManagerFactoryImpl
import com.yogeshpaliyal.deepr.backup.AutoBackupWorker
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ExportRepositoryImpl
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepositoryImpl
import com.yogeshpaliyal.deepr.data.HtmlParser
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.data.LinkRepositoryImpl
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.gdrive.DriveSyncManager
import com.yogeshpaliyal.deepr.gdrive.DriveSyncManagerFactoryImpl
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.review.ReviewManager
import com.yogeshpaliyal.deepr.review.ReviewManagerFactory
import com.yogeshpaliyal.deepr.server.LocalServerRepository
import com.yogeshpaliyal.deepr.server.LocalServerRepositoryImpl
import com.yogeshpaliyal.deepr.server.LocalServerTransferLink
import com.yogeshpaliyal.deepr.sync.SyncRepository
import com.yogeshpaliyal.deepr.sync.SyncRepositoryImpl
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import com.yogeshpaliyal.deepr.viewmodel.LocalServerViewModel
import com.yogeshpaliyal.deepr.viewmodel.TransferLinkLocalServerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
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
                            schema = DeeprDB.Schema,
                            context = this@DeeprApplication,
                            name = "deepr.db",
                            callback = object : AndroidSqliteDriver.Callback(DeeprDB.Schema) {
                                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    db.setForeignKeyConstraintsEnabled(true)
                                }
                            },
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

                single<LinkRepository> { LinkRepositoryImpl(androidContext(), get()) }

                single<ExportRepository> { ExportRepositoryImpl(androidContext(), get()) }
                single<DriveSyncManager> { DriveSyncManagerFactoryImpl.create(androidContext(), get()) }

                single<ImportRepository> { ImportRepositoryImpl(androidContext(), get(), get()) }

                single<SyncRepository> { SyncRepositoryImpl(androidContext(), get(), get()) }

                single<AutoBackupWorker> { AutoBackupWorker(androidContext(), get(), get()) }

                single {
                    HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json(
                                Json {
                                    prettyPrint = true
                                    isLenient = true
                                    ignoreUnknownKeys = true
                                },
                            )
                        }
                    }
                }

                viewModelOf(::AccountViewModel)

                single {
                    HtmlParser()
                }

                single {
                    NetworkRepository(get())
                }

                single<LocalServerRepository> {
                    LocalServerRepositoryImpl(
                        androidContext(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                    )
                }

                factory {
                    LocalServerTransferLink(
                        androidContext(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                    )
                }

                viewModel {
                    LocalServerViewModel(get())
                }

                single<ReviewManager> {
                    ReviewManagerFactory.create()
                }

                viewModel {
                    TransferLinkLocalServerViewModel(get())
                }

                viewModel {
                    TransferLinkLocalServerViewModel(get())
                }
                single<AnalyticsManager> {
                    AnalyticsManagerFactoryImpl.create(get())
                }
            }

        startKoin {
            // Provide Android context to Koin
            androidContext(this@DeeprApplication)

            modules(appModule)
        }
    }
}
