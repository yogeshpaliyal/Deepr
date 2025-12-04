package com.yogeshpaliyal.shared

import androidx.compose.runtime.Composable

actual fun featuresList(): Map<Features, Boolean> {
    return mapOf(
        Features.SETTINGS to false,
    )
}

@Composable
actual fun RenderFeatures(feature: Features) {

}