package com.yogeshpaliyal.deepr

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
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

            viewModel { AccountViewModel(get()) }
        }

        startKoin {
            // Provide Android context to Koin
            androidContext(this@DeeprApplication)

            modules(appModule)
        }
    }
}