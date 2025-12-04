package com.yogeshpaliyal.shared.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import compose.icons.TablerIcons
import compose.icons.tablericons.LayoutGrid
import compose.icons.tablericons.LayoutList
import compose.icons.tablericons.LayoutRows

@Composable
fun ViewTypeMenu(
    currentViewType: @ViewType Int,
    setViewType: (@ViewType Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                when (currentViewType) {
                    ViewType.LIST -> TablerIcons.LayoutList
                    ViewType.GRID -> TablerIcons.LayoutGrid
                    ViewType.COMPACT -> TablerIcons.LayoutRows
                    else -> TablerIcons.LayoutList
                },
                contentDescription = "View Type",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("List view") },
                onClick = {
                    setViewType(ViewType.LIST)
                    expanded = false
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor =
                            if (currentViewType == ViewType.LIST) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    ),
                leadingIcon = {
                    Icon(
                        TablerIcons.LayoutList,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text("Grid view") },
                onClick = {
                    setViewType(ViewType.GRID)
                    expanded = false
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor =
                            if (currentViewType == ViewType.GRID) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    ),
                leadingIcon = {
                    Icon(
                        TablerIcons.LayoutGrid,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text("Compat view") },
                onClick = {
                    setViewType(ViewType.COMPACT)
                    expanded = false
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor =
                            if (currentViewType == ViewType.COMPACT) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    ),
                leadingIcon = {
                    Icon(
                        TablerIcons.LayoutRows,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
