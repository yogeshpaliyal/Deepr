package com.yogeshpaliyal.deepr.google_drive

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries

object DriveSyncManagerFactoryImpl : DriveSyncManagerFactory {
    override fun create(
        context: Context,
        deeprQueries: DeeprQueries,
    ): DriveSyncManager = DriveSyncManagerImpl(context, deeprQueries)
}
