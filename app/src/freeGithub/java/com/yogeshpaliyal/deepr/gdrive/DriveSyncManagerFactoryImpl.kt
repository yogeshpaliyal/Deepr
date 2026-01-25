package com.yogeshpaliyal.deepr.gdrive

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries

object DriveSyncManagerFactoryImpl : DriveSyncManagerFactory {
    override fun create(
        context: Context,
        deeprQueries: DeeprQueries,
    ): DriveSyncManager = NoOpDriveSyncManager()
}
