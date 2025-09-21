package com.yogeshpaliyal.deepr.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.yogeshpaliyal.deepr.Tags
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Trash

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionBottomSheet(
    tags: List<Tags>,
    selectedTag: Tags?,
    dismissBottomSheet: () -> Unit,
    setTagFilter: (Tags?) -> Unit,
    editTag: (Tags) -> Result<Boolean>,
    deleteTag: (Tags) -> Result<Boolean>,
    modifier: Modifier = Modifier,
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isTagEditEnable by remember { mutableStateOf<Tags?>(null) }
    var isTagDeleteEnable by remember { mutableStateOf<Tags?>(null) }
    var tagEditError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    isTagEditEnable?.let { tag ->
        AlertDialog(
            onDismissRequest = {
                isTagEditEnable = null
                tagEditError = null
            },
            title = {
                Text(text = "Edit Tag")
            },
            text = {
                Column {
                    TextField(
                        value = tag.name,
                        onValueChange = {
                            isTagEditEnable = tag.copy(name = it)
                        },
                        isError = tagEditError != null,
                        supportingText = {
                            tagEditError?.let {
                                Text(text = it)
                            }
                        },
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val result = editTag(tag)
                    if (result.isFailure) {
                        val exception = result.exceptionOrNull()
                        when (exception) {
                            is SQLiteConstraintException -> {
                                tagEditError = "Tag with this name already exists"
                            }

                            else -> {
                                tagEditError = "Failed to edit tag"
                            }
                        }
                    } else {
                        isTagEditEnable = null
                        tagEditError = null
                        Toast
                            .makeText(context, "Tag edited successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                Button(onClick = {
                    isTagEditEnable = null
                    tagEditError = null
                }) {
                    Text("Cancel")
                }
            },
        )
    }

    isTagDeleteEnable?.let { tag ->
        AlertDialog(
            onDismissRequest = {
                isTagDeleteEnable = null
            },
            title = {
                Text(text = "Delete Tag")
            },
            text = {
                Text(text = "Are you sure you want to delete this tag?")
            },
            confirmButton = {
                Button(onClick = {
                    val result = deleteTag(tag)
                    if (result.isFailure) {
                        Toast.makeText(context, "Failed to delete tag ${result.exceptionOrNull()}", Toast.LENGTH_SHORT).show()
                    } else {
                        isTagDeleteEnable = null
                        Toast
                            .makeText(context, "Tag deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    isTagDeleteEnable = null
                }) {
                    Text("Cancel")
                }
            },
        )
    }

    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = dismissBottomSheet) {
        Column(modifier) {
            TopAppBar(
                title = {
                    Text("Tags")
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            HorizontalDivider()
            LazyColumn {
                item {
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
                }
                items(tags) { tag ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                setTagFilter(tag)
                                dismissBottomSheet()
                            },
                        headlineContent = { Text(tag.name) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    isTagEditEnable = tag
                                }) {
                                    Icon(
                                        imageVector = TablerIcons.Edit,
                                        contentDescription = "Edit Tag",
                                    )
                                }

                                IconButton(onClick = {
                                    isTagDeleteEnable = tag
                                }) {
                                    Icon(
                                        imageVector = TablerIcons.Trash,
                                        contentDescription = "Clear Tag",
                                    )
                                }
                            }
                        },
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
}
