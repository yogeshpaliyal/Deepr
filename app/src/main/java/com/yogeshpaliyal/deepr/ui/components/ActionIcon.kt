package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionIcon(
    onClick: () -> Unit,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    contentDescription: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
