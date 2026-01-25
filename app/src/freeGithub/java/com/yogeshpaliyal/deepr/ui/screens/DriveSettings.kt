package com.yogeshpaliyal.deepr.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yogeshpaliyal.deepr.ui.components.GoogleDrivePromoItem
import com.yogeshpaliyal.deepr.ui.components.ProUpgradeDialog
import com.yogeshpaliyal.deepr.ui.components.SettingsSection

@Composable
fun DriveSettingsItem() {
    var showProUpgradeDialog by remember { mutableStateOf(false) }

    // Google Drive section - show for all users
    // Pro users get full functionality, free users see upgrade prompt
    SettingsSection("Google Drive") {
        GoogleDrivePromoItem(
            onClick = { showProUpgradeDialog = true },
        )
    }

    // Pro Upgrade Dialog
    if (showProUpgradeDialog) {
        ProUpgradeDialog(
            onDismiss = { showProUpgradeDialog = false },
        )
    }
}
