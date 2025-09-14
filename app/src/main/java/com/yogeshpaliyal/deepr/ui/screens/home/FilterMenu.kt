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
import com.yogeshpaliyal.deepr.viewmodel.SortType
import compose.icons.TablerIcons
import compose.icons.tablericons.Filter

@Composable
fun FilterMenu(
    onSortOrderChange: (sortType: @SortType String) -> Unit,
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
                    onSortOrderChange(SortType.SORT_CREATED_BY_ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Date Descending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_CREATED_BY_DESC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Ascending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_OPENED_ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Descending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_OPENED_DESC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Name Ascending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_NAME_ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Name Descending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_NAME_DESC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Link Ascending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_LINK_ASC)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Sort by Link Descending") },
                onClick = {
                    onSortOrderChange(SortType.SORT_LINK_DESC)
                    expanded = false
                },
            )
        }
    }
}
