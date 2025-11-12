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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Tag
import compose.icons.tablericons.Trash
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel


object TagSelectionScreen : TopLevelRoute {
    override val icon: ImageVector
        get() = TablerIcons.Tag
    override val label: Int
        get() = R.string.tags

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: AccountViewModel = koinActivityViewModel()
        val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
        var newTagName by remember { mutableStateOf("") }
        val tagsWithCount by viewModel.allTagsWithCount.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val deeprQueries: DeeprQueries = koinInject()
        var isTagEditEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
        var isTagDeleteEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
        var tagEditError by remember { mutableStateOf<String?>(null) }

        Column {
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
                            suffix =
                                if (newTagName.isNotBlank()) {
                                    {
                                        ClearInputIconButton(
                                            onClick = {
                                                newTagName = ""
                                            },
                                        )
                                    }
                                } else {
                                    null
                                },
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
                                contentDescription = stringResource(R.string.create_tag),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
            LazyColumn {
                // Show "Clear All Filters" option if any tags are selected
                if (selectedTag.isNotEmpty()) {
                    item {
                        ListItem(
                            modifier =
                                Modifier.clickable {
                                    viewModel.setTagFilter(null)
                                },
                            headlineContent = {
                                Text(
                                    stringResource(R.string.clear_all_filters),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }

                item {
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                // Don't dismiss, allow multi-selection
                            },
                        headlineContent = {
                            Text(
                                if (selectedTag.isEmpty()) {
                                    stringResource(R.string.all)
                                } else {
                                    stringResource(R.string.selected_tags_count, selectedTag.size)
                                },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }

                items(tagsWithCount.sortedBy { it.name }) { tag ->
                    val isSelected = selectedTag.any { it.id == tag.id }
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                viewModel.setTagFilter(Tags(tag.id, tag.name))
                                // Don't dismiss to allow multi-selection
                            },
                        leadingContent = {
                            androidx.compose.material3.Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    viewModel.setTagFilter(Tags(tag.id, tag.name))
                                },
                            )
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
                            if (isSelected) {
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
                        val result = runBlocking {
                            try {
                                viewModel.updateTag(Tags(tag.id, tag.name))
                                Result.success(true)
                            } catch (e: Exception) {
                                return@runBlocking Result.failure(e)
                            }
                        }
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
                    val message =
                        buildAnnotatedString {
                            append("Are you sure you want to delete ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("'${tag.name}'")
                            }
                            append(" tag?")
                        }
                    Text(text = message)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTag(tag.id)

                            isTagDeleteEnable = null
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.tag_deleted_successfully),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                    ) {
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
    }
}
