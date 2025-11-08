package com.yogeshpaliyal.deepr.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getDeeprItemBackgroundColor(isFavourite: Long): Color =
    if (isFavourite == 1L) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer

@Composable
fun getDeeprItemTextColor(isFavourite: Long): Color =
    if (isFavourite == 1L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
