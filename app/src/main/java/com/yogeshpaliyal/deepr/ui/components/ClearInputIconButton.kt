package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.X

@Composable
fun ClearInputIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    contentDescription: String = "Clear",
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
    ) {
        Icon(TablerIcons.X, contentDescription = contentDescription)
    }
}
