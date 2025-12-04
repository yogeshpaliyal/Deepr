package com.yogeshpaliyal.shared

import androidx.compose.runtime.Composable
import com.yogeshpaliyal.shared.ui.ShortcutMenuItem

actual fun featuresList(): Map<Features, Boolean> {
    return mapOf(
        Features.SETTINGS to true,
        Features.SHORTCUT_MENU_ITEM to true,
    )
}

@Composable
actual fun RenderFeatures(feature: Features) {
    when(feature) {
        Features.SHORTCUT_MENU_ITEM -> ShortcutMenuItem()
        else -> {}
    }
}