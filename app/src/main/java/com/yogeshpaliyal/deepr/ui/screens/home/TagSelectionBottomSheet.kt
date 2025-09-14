package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yogeshpaliyal.deepr.Tags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionBottomSheet(
    tags: List<Tags>,
    selectedTag: Tags?,
    dismissBottomSheet: () -> Unit,
    setTagFilter: (Tags?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = dismissBottomSheet) {
        Column(modifier) {
            TopAppBar(
                title = {
                    Text("Tags")
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            HorizontalDivider()
            ListItem(
                modifier =
                    Modifier.clickable {
                        setTagFilter(null)
                        dismissBottomSheet()
                    },
                headlineContent = { Text("All") },
                colors =
                    if (selectedTag == null) {
                        ListItemDefaults.colors(
                            headlineColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        ListItemDefaults.colors(containerColor = Color.Transparent)
                    },
            )
            tags.forEach { tag ->
                ListItem(
                    modifier =
                        Modifier.clickable {
                            setTagFilter(tag)
                            dismissBottomSheet()
                        },
                    headlineContent = { Text(tag.name) },
                    colors =
                        if (selectedTag?.id == tag.id) {
                            ListItemDefaults.colors(
                                headlineColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            ListItemDefaults.colors(containerColor = Color.Transparent)
                        },
                )
            }
        }
    }
}
