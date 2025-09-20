@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.yogeshpaliyal.deepr.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import org.koin.compose.koinInject

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomContent(
    deeprQueries: DeeprQueries,
    selectedLink: GetLinksAndTags,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinInject(),
    onSaveDialogInfoChange: ((SaveDialogInfo?) -> Unit) = {},
) {
    var deeprInfo by remember(selectedLink) {
        mutableStateOf(
            selectedLink,
        )
    }
    var isError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    // Tags
    var newTagName by remember { mutableStateOf("") }
    val allTags by viewModel.allTags.collectAsState()
    val selectedTags = remember { mutableStateListOf<Tags>() }
    val initialSelectedTags = remember { mutableStateListOf<Tags>() }
    val isCreate = selectedLink.id == 0L

    // Initialize selected tags if in edit mode
    LaunchedEffect(isCreate) {
        if (isCreate.not()) {
            val existingTags =
                selectedLink.tagsIds?.split(",")?.mapIndexed { index, tagId ->
                    Tags(
                        tagId.trim().toLong(),
                        selectedLink.tagsNames
                            ?.split(",")
                            ?.getOrNull(index)
                            ?.trim()
                            ?.toString() ?: "Unknown",
                    )
                }
            selectedTags.clear()
            initialSelectedTags.clear()
            existingTags?.toList()?.let {
                selectedTags.addAll(it)
                initialSelectedTags.addAll(it)
            }
        }
    }

    val save: (executeAfterSave: Boolean) -> Unit = { executeAfterSave ->
        // Remove unselected tags
        val initialTagIds = initialSelectedTags.map { it.id }.toSet()
        val currentTagIds = selectedTags.map { it.id }.toSet()
        val tagsToRemove = initialTagIds - currentTagIds
        tagsToRemove.forEach { tagId ->
            viewModel.removeTagFromLink(deeprInfo.id, tagId)
        }

        // Then add selected tags
        selectedTags.forEach { tag ->
            if (tag.id > 0) {
                // Existing tag
                viewModel.addTagToLink(deeprInfo.id, tag.id)
            } else {
                // New tag
                viewModel.addTagToLinkByName(deeprInfo.id, tag.name)
            }
        }

        if (deeprInfo.id == 0L) {
            // New Account
            viewModel.insertAccount(deeprInfo.link, deeprInfo.name, executeAfterSave)
        } else {
            // Edit
            viewModel.updateDeeplink(deeprInfo.id, deeprInfo.link, deeprInfo.name)
        }
        onSaveDialogInfoChange(SaveDialogInfo(deeprInfo, executeAfterSave))
    }

    val context = LocalContext.current
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = {
        onSaveDialogInfoChange(null)
    }) {
        Column(
            modifier =
                modifier
                    .verticalScroll(rememberScrollState())
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                        ),
                    ).fillMaxWidth(),
        ) {
            Text(
                text = if (isCreate) "Create" else "Edit",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineMedium,
            )
            Column(
                modifier =
                    Modifier
                        .padding(8.dp),
            ) {
                TextField(
                    value = deeprInfo.link,
                    onValueChange = {
                        deeprInfo = deeprInfo.copy(link = it)
                        isError = false
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    label = { Text(stringResource(R.string.enter_deeplink_command)) },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(text = stringResource(R.string.invalid_empty_deeplink))
                        }
                    },
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = deeprInfo.link.isNotBlank(),
                    onClick = {
                        viewModel.fetchMetaData(deeprInfo.link) {
                            if (it != null) {
                                deeprInfo = deeprInfo.copy(name = it.title ?: "")
                                isNameError = false
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "Failed to fetch metadata",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                    },
                ) {
                    Text("Fetch name from link")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = deeprInfo.name,
                    onValueChange = {
                        deeprInfo = deeprInfo.copy(name = it)
                        isNameError = false
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    label = { Text(stringResource(R.string.enter_link_name)) },
                    supportingText = {
                        if (isNameError) {
                            Text(text = stringResource(R.string.enter_link_name_error))
                        }
                    },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("New tag") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newTagName.isNotBlank()) {
                                // Check if tag already exists in allTags
                                val existingTag = allTags.find { it.name == newTagName }

                                if (existingTag != null) {
                                    // Add existing tag if not already selected
                                    if (!selectedTags.contains(existingTag)) {
                                        selectedTags.add(existingTag)
                                    }
                                } else {
                                    // Create a temporary tag with ID 0 (will be properly created on save)
                                    selectedTags.add(Tags(0, newTagName))
                                }

                                newTagName = "" // Clear input
                            }
                        },
                        enabled = newTagName.isNotBlank(),
                    ) {
                        Text("Add Tag")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display selected tags - only show the label if there are tags
                if (selectedTags.isNotEmpty()) {
                    Text(
                        "Tags:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedTags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = { /* Do nothing on click */ },
                                label = { Text(tag.name) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { selectedTags.remove(tag) },
                                        modifier = Modifier.size(InputChipDefaults.IconSize),
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.X,
                                            contentDescription = "Remove tag",
                                        )
                                    }
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tag suggestions - only show if there are suggestions
                val availableSuggestions = allTags.filter { tag -> !selectedTags.contains(tag) }
                if (availableSuggestions.isNotEmpty()) {
                    Text(
                        "Suggestions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        availableSuggestions.forEach { tag ->
                            SuggestionChip(
                                onClick = { selectedTags.add(tag) },
                                label = { Text(tag.name) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    if (!isCreate) {
                        Button(
                            onClick = {
                                if (isValidDeeplink(deeprInfo.link)) {
                                    save(false)
                                } else {
                                    isError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier,
                            onClick = {
                                if (isValidDeeplink(deeprInfo.link)) {
                                    if (deeprQueries
                                            .getDeeprByLink(deeprInfo.link)
                                            .executeAsList()
                                            .isNotEmpty()
                                    ) {
                                        Toast
                                            .makeText(
                                                context,
                                                "Deeplink already exists",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    } else {
                                        save(false)
                                    }
                                } else {
                                    isError = true
                                }
                            },
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    }

                    if (isCreate) {
                        OutlinedButton(onClick = {
                            isError = !openDeeplink(context, deeprInfo.link)
                        }) {
                            Text(stringResource(R.string.execute))
                        }
                    }

                    if (isCreate) {
                        Button(onClick = {
                            if (isValidDeeplink(deeprInfo.link)) {
                                if (deeprQueries
                                        .getDeeprByLink(deeprInfo.link)
                                        .executeAsList()
                                        .isNotEmpty()
                                ) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Deeplink already exists",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } else {
                                    save(true)
                                }
                            } else {
                                isError = true
                            }
                        }) {
                            Text(stringResource(R.string.save_and_execute))
                        }
                    }
                }
            }
        }
    }
}
