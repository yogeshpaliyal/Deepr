package com.yogeshpaliyal.deepr.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.server.DeeprTag
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.shared.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Eye
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Tag
import compose.icons.tablericons.Trash
import kotlinx.coroutines.runBlocking
import org.koin.compose.viewmodel.koinActivityViewModel

object TagSelectionScreen : TopLevelRoute {
    override val icon: ImageVector
        get() = TablerIcons.Tag
    override val label: Int
        get() = R.string.tags

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(windowInsets: WindowInsets) {
        val viewModel: AccountViewModel = koinActivityViewModel()
        val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
        var newTagName by remember { mutableStateOf("") }
        val tagsWithCountState by viewModel.allTagsWithCount.collectAsStateWithLifecycle()
        val tagsWithCount = tagsWithCountState?.tags ?: listOf()
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        var isTagEditEnable by remember { mutableStateOf<DeeprTag?>(null) }
        var isTagDeleteEnable by remember { mutableStateOf<DeeprTag?>(null) }
        var tagEditError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            contentWindowInsets = windowInsets,
            floatingActionButton = {
                AnimatedVisibility(
                    selectedTag.isNotEmpty(),
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            navigator.clearStackAndAdd(Dashboard2())
                        },
                        icon = {
                            Icon(
                                imageVector = TablerIcons.Eye,
                                contentDescription = "View Filtered Links",
                            )
                        },
                        text = { Text("View Filtered Links") },
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                // Top Section - Create New Tag
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.tags),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = newTagName,
                                onValueChange = { newTagName = it },
                                modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.new_tag)) },
                                placeholder = { Text("Enter tag name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon =
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
                                            viewModel.insertTag(trimmedTagName)
                                            newTagName = ""
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Tag created successfully",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        }
                                    }
                                },
                                enabled = newTagName.isNotBlank(),
                                modifier = Modifier.size(56.dp),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Plus,
                                    contentDescription = stringResource(R.string.create_tag),
                                )
                            }
                        }

                        // Show selected tags info
                        AnimatedVisibility(selectedTag.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.selected_tags_count,
                                                    selectedTag.size,
                                                ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                        TextButton(
                                            onClick = { viewModel.setTagFilter(null) },
                                        ) {
                                            Text(stringResource(R.string.clear_all_filters))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface {
                    // Tags List
                    if (tagsWithCount.isEmpty()) {
                        // Empty State
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tags yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Create your first tag to organize your links",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                androidx.compose.foundation.layout
                                    .PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(tagsWithCount.sortedBy { it.name }) { tag ->
                                val isSelected = selectedTag.any { it.id == tag.id }
                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setTagFilter(tag)
                                            },
                                    shape = RoundedCornerShape(12.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                        ),
                                    border =
                                        if (isSelected) {
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        } else {
                                            null
                                        },
                                ) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        androidx.compose.material3.Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                viewModel.setTagFilter(tag)
                                            },
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tag.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color =
                                                    if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    },
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${tag.count} ${if (tag.count == 1L) "link" else "links"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color =
                                                    if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                            alpha = 0.7f,
                                                        )
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            IconButton(
                                                onClick = { isTagEditEnable = tag },
                                                colors =
                                                    IconButtonDefaults.iconButtonColors(
                                                        contentColor =
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                    ),
                                            ) {
                                                Icon(
                                                    imageVector = TablerIcons.Edit,
                                                    contentDescription = stringResource(R.string.edit_tag_description),
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }

                                            IconButton(
                                                onClick = { isTagDeleteEnable = tag },
                                                colors =
                                                    IconButtonDefaults.iconButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error,
                                                    ),
                                            ) {
                                                Icon(
                                                    imageVector = TablerIcons.Trash,
                                                    contentDescription = stringResource(R.string.delete_tag_description),
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
                            Text(
                                text = stringResource(R.string.edit_tag),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = tag.name,
                                    onValueChange = {
                                        isTagEditEnable = tag.copy(name = it)
                                        tagEditError = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tag name") },
                                    singleLine = true,
                                    isError = tagEditError != null,
                                    supportingText = {
                                        tagEditError?.let {
                                            Text(
                                                text = it,
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon =
                                        if (isTagEditEnable?.name?.isNotBlank() == true) {
                                            {
                                                ClearInputIconButton(
                                                    onClick = {
                                                        isTagEditEnable = tag.copy(name = "")
                                                    },
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val trimmedName = isTagEditEnable?.name?.trim() ?: ""
                                    if (trimmedName.isBlank()) {
                                        tagEditError = "Tag name cannot be empty"
                                        return@Button
                                    }

                                    val result =
                                        runBlocking {
                                            try {
                                                viewModel.updateTag(tag)
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
                                },
                                enabled = isTagEditEnable?.name?.isNotBlank() == true,
                            ) {
                                Text(stringResource(R.string.edit))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
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
                        icon = {
                            Icon(
                                imageVector = TablerIcons.Trash,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp),
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.delete_tag),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        },
                        text = {
                            Column {
                                val message =
                                    buildAnnotatedString {
                                        append("Are you sure you want to delete ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("'${tag.name}'")
                                        }
                                        append(" tag?")
                                    }
                                Text(text = message)

                                if (tag.count > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.errorContainer.copy(
                                                        alpha = 0.3f,
                                                    ),
                                            ),
                                    ) {
                                        Text(
                                            text = "This tag is used by ${tag.count} ${if (tag.count == 1L) "link" else "links"}",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                }
                            }
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
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ),
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                isTagDeleteEnable = null
                            }) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                    )
                }
            }
        }
    }
}
