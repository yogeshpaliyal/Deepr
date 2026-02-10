package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R

@Composable
fun QrCodeDialog(
    deepr: GetLinksAndTags,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR Code") },
        text = {
            Column {
                QrCodeView(
                    data = deepr.link,
                    modifier = Modifier.size(300.dp),
                    colors =
                        QrCodeColors(
                            background = Color.White,
                            foreground = Color.Black,
                        ),
                    dotShape = DotShape.Circle,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Transparent),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier =
                                Modifier
                                    .fillMaxSize(0.5f)
                                    .clip(CircleShape)
                                    .background(Color.White),
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
