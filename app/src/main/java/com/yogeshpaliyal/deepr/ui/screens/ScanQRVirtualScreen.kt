package com.yogeshpaliyal.deepr.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.yogeshpaliyal.deepr.TopLevelRoute
import compose.icons.TablerIcons
import compose.icons.tablericons.Qrcode

class ScanQRVirtualScreen : TopLevelRoute {
    @Composable
    override fun Content() {
    }

    override val icon: ImageVector
        get() = TablerIcons.Qrcode
}
