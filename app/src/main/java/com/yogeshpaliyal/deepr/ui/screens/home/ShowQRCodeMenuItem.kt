package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.material3.DropdownMenuItem
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
    DropdownMenuItem(
        modifier = modifier,
        text = { Text(stringResource(R.string.show_qr_code)) },
        onClick = {
            onQrCodeClick(account)
        },
        leadingIcon = {
            Icon(
                TablerIcons.Qrcode,
                contentDescription = stringResource(R.string.show_qr_code),
            )
        },
    )
}
