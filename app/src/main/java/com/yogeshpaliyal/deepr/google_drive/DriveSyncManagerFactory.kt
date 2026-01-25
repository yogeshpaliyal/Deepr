package com.yogeshpaliyal.deepr.google_drive

import android.content.Context
import com.yogeshpaliyal.deepr.DeeprQueries

/**
 * Factory for creating DriveSyncManager instances.
 * Implementation differs by build variant.
 */
interface DriveSyncManagerFactory {
    fun create(
        context: Context,
        deeprQueries: DeeprQueries,
    ): DriveSyncManager
}
