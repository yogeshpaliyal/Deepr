package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yogeshpaliyal.deepr.viewmodel.SortOrder
import compose.icons.TablerIcons
import compose.icons.tablericons.Filter

@Composable
fun FilterMenu(
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(TablerIcons.Filter, contentDescription = "Filter")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Sort by Date Ascending") },
                onClick = {
                    onSortOrderChange(SortOrder.ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Date Descending") },
                onClick = {
                    onSortOrderChange(SortOrder.DESC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Ascending") },
                onClick = {
                    onSortOrderChange(SortOrder.OPENED_ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Descending") },
                onClick = {
                    onSortOrderChange(SortOrder.OPENED_DESC)
                    expanded = false
                },
            )
        }
    }
}
