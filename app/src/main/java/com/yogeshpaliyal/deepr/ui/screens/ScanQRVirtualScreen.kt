package com.yogeshpaliyal.deepr.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.journeyapps.barcodescanner.ScanOptions
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.components.SettingsItem
import com.yogeshpaliyal.deepr.ui.screens.home.Dashboard2
import com.yogeshpaliyal.deepr.ui.screens.home.createDeeprObject
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import compose.icons.TablerIcons
import compose.icons.tablericons.Qrcode

@Composable
fun ScanQRCode() {
    val navigatorContext = LocalNavigator.current
    val context = LocalContext.current

    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents == null) {
                Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                val normalizedLink = normalizeLink(result.contents)
                if (isValidDeeplink(normalizedLink)) {
                    navigatorContext.clearStackAndAdd(
                        Dashboard2(
                            mSelectedLink =
                                createDeeprObject(
                                    link = normalizedLink,
                                ),
                        ) {},
                    )
                } else {
                    Toast.makeText(context, "Invalid deeplink", Toast.LENGTH_SHORT).show()
                }
            }
        }

    SettingsItem(
        TablerIcons.Qrcode,
        title = stringResource(R.string.scan_qr_code),
        onClick = {
            qrScanner.launch(ScanOptions())
        },
    )
}
