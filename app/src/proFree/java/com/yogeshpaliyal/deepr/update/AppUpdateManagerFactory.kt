package com.yogeshpaliyal.deepr.update

import android.content.Context

object AppUpdateManagerFactory {
    fun create(context: Context): AppUpdateManager = PlayStoreAppUpdateManager(context)
}
