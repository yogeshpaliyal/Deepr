package com.yogeshpaliyal.deepr.gdrive

import android.content.Context
import com.yogeshpaliyal.deepr.data.LinkRepository

object DriveSyncManagerFactoryImpl : DriveSyncManagerFactory {
    override fun create(
        context: Context,
        linkRepository: LinkRepository,
    ): DriveSyncManager = NoOpDriveSyncManager()
}
