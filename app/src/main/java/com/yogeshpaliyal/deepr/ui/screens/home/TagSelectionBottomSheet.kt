package com.yogeshpaliyal.deepr.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionBottomSheet(
    tagsWithCount: List<GetAllTagsWithCount>,
    selectedTag: Tags?,
    dismissBottomSheet: () -> Unit,
    setTagFilter: (Tags?) -> Unit,
    editTag: (Tags) -> Result<Boolean>,
    deleteTag: (Tags) -> Result<Boolean>,
    deeprQueries: com.yogeshpaliyal.deepr.DeeprQueries,
    modifier: Modifier = Modifier,
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isTagEditEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
    var isTagDeleteEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
    var tagEditError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var newTagName by remember { mutableStateOf("") }

    isTagEditEnable?.let { tag ->
        AlertDialog(
            onDismissRequest = {
                isTagEditEnable = null
                tagEditError = null
            },
            title = {
                Text(text = stringResource(R.string.edit_tag))
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
                        suffix =
                            if (isTagEditEnable?.name.isNullOrEmpty()) {
                                null
                            } else {
                                {
                                    ClearInputIconButton(
                                        onClick = {
                                            isTagEditEnable = tag.copy(name = "")
                                        },
                                    )
                                }
                            },
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val result = editTag(Tags(tag.id, tag.name))
                    if (result.isFailure) {
                        val exception = result.exceptionOrNull()
                        tagEditError =
                            when (exception) {
                                is SQLiteConstraintException -> {
                                    context.getString(R.string.tag_name_exists)
                                }

                                else -> {
                                    context.getString(R.string.failed_to_edit_tag)
                                }
                            }
                    } else {
                        isTagEditEnable = null
                        tagEditError = null
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.tag_edited_successfully),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }) {
                    Text(stringResource(R.string.edit))
                }
            },
            dismissButton = {
                Button(onClick = {
                    isTagEditEnable = null
                    tagEditError = null
                }) {
                    Text(stringResource(R.string.cancel))
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
                Text(text = stringResource(R.string.delete_tag))
            },
            text = {
                Text(text = stringResource(R.string.delete_tag_confirmation))
            },
            confirmButton = {
                Button(onClick = {
                    val result = deleteTag(Tags(tag.id, tag.name))
                    if (result.isFailure) {
                        Toast
                            .makeText(
                                context,
                                context.getString(
                                    R.string.failed_to_delete_tag,
                                    result.exceptionOrNull(),
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                    } else {
                        isTagDeleteEnable = null
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.tag_deleted_successfully),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    isTagDeleteEnable = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = dismissBottomSheet) {
        Column(modifier) {
            TopAppBar(
                title = {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.tags))

                        Spacer(modifier = Modifier.width(24.dp))

                        OutlinedTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.new_tag)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = {
                                val trimmedTagName = newTagName.trim()
                                if (trimmedTagName.isNotBlank()) {
                                    val existingTag =
                                        tagsWithCount.find {
                                            it.name.equals(
                                                trimmedTagName,
                                                ignoreCase = true,
                                            )
                                        }

                                    if (existingTag != null) {
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.tag_name_exists),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    } else {
                                        deeprQueries.insertTag(trimmedTagName)
                                        newTagName = ""
                                    }
                                }
                            },
                            enabled = newTagName.isNotBlank(),
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = TablerIcons.Plus,
                                contentDescription = stringResource(R.string.add_tag),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
            LazyColumn {
                item {
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                setTagFilter(null)
                                dismissBottomSheet()
                            },
                        headlineContent = { Text(stringResource(R.string.all)) },
                        colors =
                            if (selectedTag == null) {
                                ListItemDefaults.colors(
                                    headlineColor = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                ListItemDefaults.colors(containerColor = Color.Transparent)
                            },
                    )
                }
                items(tagsWithCount) { tag ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                setTagFilter(Tags(tag.id, tag.name))
                                dismissBottomSheet()
                            },
                        headlineContent = { Text("${tag.name} (${tag.linkCount})") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    isTagEditEnable = tag
                                }) {
                                    Icon(
                                        imageVector = TablerIcons.Edit,
                                        contentDescription = stringResource(R.string.edit_tag_description),
                                    )
                                }

                                IconButton(onClick = {
                                    isTagDeleteEnable = tag
                                }) {
                                    Icon(
                                        imageVector = TablerIcons.Trash,
                                        contentDescription = stringResource(R.string.delete_tag_description),
                                    )
                                }
                            }
                        },
                        colors =
                            if (selectedTag?.id == tag.id) {
                                ListItemDefaults.colors(
                                    headlineColor = MaterialTheme.colorScheme.primary,
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
