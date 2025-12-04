package com.yogeshpaliyal.shared.ui.screens.home

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.TYPE,
)
@IntDef(value = [ViewType.LIST, ViewType.GRID, ViewType.COMPACT])
annotation class ViewType {
    companion object {
        const val LIST = 0
        const val GRID = 1
        const val COMPACT = 2
    }
}
