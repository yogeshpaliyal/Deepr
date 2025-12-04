package com.yogeshpaliyal.shared

import androidx.compose.runtime.Composable

expect fun featuresList(): Map<Features, Boolean>


enum class Features {
    SETTINGS,
    SHORTCUT_MENU_ITEM
}



@Composable
expect fun RenderFeatures(feature: Features, options: RenderFeatureContent)


data class

interface RenderFeatureContent {

}