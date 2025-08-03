package com.yogeshpaliyal.deepr

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.deepr.backup.ExportRepository
import com.yogeshpaliyal.deepr.backup.ExportRepositoryImpl
import com.yogeshpaliyal.deepr.backup.ImportRepository
import com.yogeshpaliyal.deepr.backup.ImportRepositoryImpl
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class DeeprApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            // Provide the Android-specific SqlDriver
            single<SqlDriver> {
                AndroidSqliteDriver(DeeprDB.Schema, this@DeeprApplication, "deepr.db")
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

            viewModel { AccountViewModel(get(), get(), get()) }
        }

        startKoin {
            // Provide Android context to Koin
            androidContext(this@DeeprApplication)

            modules(appModule)
        }
    }
}