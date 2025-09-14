package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yogeshpaliyal.deepr.GetLinksAndTags
import compose.icons.TablerIcons
import compose.icons.tablericons.Qrcode

@Composable
fun ShowQRCodeMenuItem(
    account: GetLinksAndTags,
    onQrCodeClick: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        modifier = modifier,
        text = { Text("Show QR Code") },
        onClick = {
            onQrCodeClick(account)
        },
        leadingIcon = {
            Icon(
                TablerIcons.Qrcode,
                contentDescription = "Show QR Code",
            )
        },
    )
}
