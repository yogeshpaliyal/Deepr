package com.yogeshpaliyal.deepr.gdrive

import android.content.Context
import com.yogeshpaliyal.deepr.data.LinkRepository

/**
 * Factory for creating DriveSyncManager instances.
 * Implementation differs by build variant.
 */
interface DriveSyncManagerFactory {
    fun create(
        context: Context,
        linkRepository: LinkRepository,
    ): DriveSyncManager
}
