package com.yogeshpaliyal.deepr.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Eye
import compose.icons.tablericons.Hash
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search
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
    override fun Content(windowInsets: WindowInsets) {
        val viewModel: AccountViewModel = koinActivityViewModel()
        val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
        var newTagName by remember { mutableStateOf("") }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchVisible by remember { mutableStateOf(false) }
        val tagsWithCount by viewModel.allTagsWithCount.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val deeprQueries: DeeprQueries = koinInject()
        var isTagEditEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
        var isTagDeleteEnable by remember { mutableStateOf<GetAllTagsWithCount?>(null) }
        var tagEditError by remember { mutableStateOf<String?>(null) }

        // Filter tags based on search query
        val filteredTags =
            remember(tagsWithCount, searchQuery) {
                val trimmedQuery = searchQuery.trim()
                if (trimmedQuery.isBlank()) {
                    tagsWithCount
                } else {
                    tagsWithCount.filter { tag ->
                        tag.name.contains(trimmedQuery, ignoreCase = true)
                    }
                }
            }

        Scaffold(
            contentWindowInsets = windowInsets,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.tags),
                                )
                                if (tagsWithCount.isNotEmpty()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Text(
                                            text = "${tagsWithCount.size}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 4.dp,
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        if (tagsWithCount.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = {
                                    isSearchVisible = !isSearchVisible
                                    if (!isSearchVisible) {
                                        searchQuery = ""
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Search,
                                    contentDescription = "Toggle Search",
                                )
                            }
                        }
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    selectedTag.isNotEmpty(),
                    enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
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
                        text = { Text("View ${selectedTag.size} ${if (selectedTag.size == 1) "Tag" else "Tags"}") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AnimatedVisibility(
                        visible = isSearchVisible && tagsWithCount.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = TablerIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            trailingIcon =
                                if (searchQuery.isNotBlank()) {
                                    {
                                        ClearInputIconButton(onClick = { searchQuery = "" })
                                    }
                                } else {
                                    null
                                },
                        )
                    }
                }

                // Create New Tag Section
                item {
                    AnimatedVisibility(
                        visible = !isSearchVisible || tagsWithCount.isEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier.padding(0.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Plus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .size(24.dp),
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.create_tag),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedTextField(
                                    value = newTagName,
                                    onValueChange = { newTagName = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Enter tag name") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = TablerIcons.Hash,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    trailingIcon =
                                        if (newTagName.isNotBlank()) {
                                            {
                                                ClearInputIconButton(onClick = {
                                                    newTagName = ""
                                                })
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
                                                deeprQueries.insertTag(trimmedTagName)
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
                        }
                    }
                }

                // Selected Tags Info
                item {
                    AnimatedVisibility(
                        visible = selectedTag.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Tag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text =
                                            stringResource(
                                                R.string.selected_tags_count,
                                                selectedTag.size,
                                            ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.setTagFilter(null) },
                                ) {
                                    Text(stringResource(R.string.clear_all_filters))
                                }
                            }
                        }
                    }
                }

                // Section Header for Tags List
                if (tagsWithCount.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.all_tags),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.filtered_tags_count,
                                            filteredTags.size,
                                            tagsWithCount.size,
                                        ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }
                }

                // Tags List
                if (tagsWithCount.isEmpty()) {
                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Tag,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .padding(20.dp)
                                            .size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "No tags yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
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
                } else if (filteredTags.isEmpty()) {
                    item {
                        // Empty State - No search results

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Search,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .padding(20.dp)
                                            .size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.no_search_results),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_search_results_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    items(
                        filteredTags.sortedBy { it.name },
                        key = { it.id },
                    ) { tag ->
                        TagItem(
                            tag = tag,
                            isSelected = selectedTag.any { it.id == tag.id },
                            onTagClick = { viewModel.setTagFilter(Tags(tag.id, tag.name)) },
                            onEditClick = { isTagEditEnable = tag },
                            onDeleteClick = { isTagDeleteEnable = tag },
                        )
                    }
                }
            }

            // Edit Tag Dialog
            isTagEditEnable?.let { tag ->
                AlertDialog(
                    onDismissRequest = {
                        isTagEditEnable = null
                        tagEditError = null
                    },
                    icon = {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = TablerIcons.Hash,
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon =
                                    if (isTagEditEnable?.name?.isNotBlank() == true) {
                                        {
                                            ClearInputIconButton(
                                                onClick = { isTagEditEnable = tag.copy(name = "") },
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
                                            viewModel.updateTag(Tags(tag.id, trimmedName))
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
                            Text(stringResource(R.string.save))
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

            // Delete Tag Dialog
            isTagDeleteEnable?.let { tag ->
                AlertDialog(
                    onDismissRequest = { isTagDeleteEnable = null },
                    icon = {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.Trash,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.delete_tag),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fullMessage = stringResource(R.string.delete_tag_confirmation_with_name, tag.name)
                            val parts = fullMessage.split(tag.name)
                            val annotatedString = buildAnnotatedString {
                                if (parts.size >= 2) {
                                    append(parts[0])
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(tag.name)
                                    }
                                    append(parts[1])
                                } else {
                                    append(fullMessage)
                                }
                            }
                            Text(
                                text = annotatedString,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (tag.linkCount > 0) {
                                Card(
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                        ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.Tag,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        val linkText = if (tag.linkCount == 1L) stringResource(R.string.link) else stringResource(R.string.links)
                                        Text(
                                            text = stringResource(R.string.tag_used_by_links, tag.linkCount, linkText),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
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
                        TextButton(onClick = { isTagDeleteEnable = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TagItem(
    tag: GetAllTagsWithCount,
    isSelected: Boolean,
    onTagClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "contentColor",
    )

    Card(
        onClick = onTagClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
            ),
        border =
            if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onTagClick() },
                colors =
                    CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = "${tag.linkCount} ${if (tag.linkCount == 1L) "link" else "links"}",
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onEditClick,
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            contentColor =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        ),
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.Edit,
                        contentDescription = stringResource(R.string.edit_tag_description),
                        modifier = Modifier.size(18.dp),
                    )
                }

                FilledTonalIconButton(
                    onClick = onDeleteClick,
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.Trash,
                        contentDescription = stringResource(R.string.delete_tag_description),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
