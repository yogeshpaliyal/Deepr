package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import compose.icons.TablerIcons
import compose.icons.tablericons.Qrcode

@Composable
fun ShowQRCodeMenuItem(
    account: GetLinksAndTags,
    onQrCodeClick: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.ListItem(
        modifier =
            modifier.clickable {
                onQrCodeClick(account)
            },
        headlineContent = { Text(stringResource(R.string.show_qr_code)) },
        leadingContent = {
            Icon(
                TablerIcons.Qrcode,
                contentDescription = stringResource(R.string.show_qr_code),
            )
        },
    )
}
