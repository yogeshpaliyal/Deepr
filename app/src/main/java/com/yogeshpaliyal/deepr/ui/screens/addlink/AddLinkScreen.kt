package com.yogeshpaliyal.deepr.ui.screens.addlink

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanOptions
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.Download
import compose.icons.tablericons.Link
import compose.icons.tablericons.Note
import compose.icons.tablericons.Photo
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Tag
import compose.icons.tablericons.User
import compose.icons.tablericons.X
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkScreen(
    selectedLink: GetLinksAndTags,
    modifier: Modifier = Modifier,
    deeprQueries: DeeprQueries = koinInject(),
    viewModel: AccountViewModel = koinInject(),
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val fetchMetadataErrorText = stringResource(R.string.failed_to_fetch_metadata)
    val removeTagText = stringResource(R.string.remove_tag)
    val deeplinkExistsText = stringResource(R.string.deeplink_already_exists)
    var deeprInfo by remember(selectedLink) {
        mutableStateOf(
            selectedLink,
        )
    }
    var isError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    var isFetchingMetadata by remember { mutableStateOf(false) }

    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents != null) {
                val normalizedLink = normalizeLink(result.contents)
                deeprInfo = deeprInfo.copy(link = normalizedLink)
                isError = false
            }
        }

    // Tags
    var newTagName by remember { mutableStateOf("") }
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val selectedTags = remember { mutableStateListOf<Tags>() }
    val initialSelectedTags = remember { mutableStateListOf<Tags>() }
    val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()
    val isCreate = selectedLink.id == 0L

    // Profile selection
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    var selectedProfileId by remember(selectedLink) {
        mutableStateOf(selectedLink.profileId.takeIf { !isCreate } ?: currentProfile?.id ?: 1L)
    }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var pendingProfileNameToSelect by remember { mutableStateOf<String?>(null) }

    val fetchMetadata: () -> Unit = {
        isFetchingMetadata = true
        viewModel.fetchMetaData(deeprInfo.link) {
            isFetchingMetadata = false
            if (it != null) {
                deeprInfo = deeprInfo.copy(name = it.title ?: "", thumbnail = it.image ?: "")
                isNameError = false
            } else {
                Toast
                    .makeText(
                        context,
                        fetchMetadataErrorText,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    LaunchedEffect(selectedLink) {
        if (isValidDeeplink(selectedLink.link) && selectedLink.name.isEmpty()) {
            fetchMetadata()
        }
    }

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
                            ?.trim() ?: "Unknown",
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

    val save: (executeAfterSave: Boolean) -> Unit = save@{ executeAfterSave ->
        // Normalize the link before saving
        val normalizedLink = normalizeLink(deeprInfo.link)

        if (isCreate && deeprQueries.getDeeprByLink(normalizedLink).executeAsOneOrNull() != null) {
            Toast.makeText(context, deeplinkExistsText, Toast.LENGTH_SHORT).show()
            return@save
        }

        // Remove unselected tags
        val initialTagIds = initialSelectedTags.map { it.id }.toSet()
        val currentTagIds = selectedTags.map { it.id }.toSet()
        val tagsToRemove = initialTagIds - currentTagIds
        tagsToRemove.forEach { tagId ->
            viewModel.removeTagFromLink(deeprInfo.id, tagId)
        }

        if (deeprInfo.id == 0L) {
            // New Account
            viewModel.insertAccount(
                normalizedLink,
                deeprInfo.name,
                executeAfterSave,
                selectedTags,
                deeprInfo.notes,
                deeprInfo.thumbnail,
                selectedProfileId,
            )
        } else {
            // Edit
            viewModel.updateDeeplink(
                deeprInfo.id,
                normalizedLink,
                deeprInfo.name,
                selectedTags,
                deeprInfo.notes,
                deeprInfo.thumbnail,
                selectedProfileId,
            )
        }
        if (executeAfterSave) {
            openDeeplink(context, normalizedLink)
        }
        navigator.removeLast()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = if (isCreate) stringResource(R.string.create) else stringResource(R.string.edit),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.removeLast() }) {
                        Icon(TablerIcons.ArrowLeft, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .imePadding(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Link Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.enter_deeplink_command),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        OutlinedTextField(
                            value = deeprInfo.link,
                            onValueChange = {
                                deeprInfo = deeprInfo.copy(link = it)
                                isError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://example.com or app://deeplink") },
                            isError = isError,
                            supportingText = {
                                if (isError) {
                                    Text(text = stringResource(R.string.invalid_empty_deeplink))
                                }
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (deeprInfo.link.isNotEmpty()) {
                                        ClearInputIconButton(
                                            onClick = {
                                                deeprInfo = deeprInfo.copy(link = "")
                                                isError = false
                                            },
                                        )
                                    }
                                    IconButton(onClick = {
                                        qrScanner.launch(ScanOptions())
                                    }) {
                                        Icon(
                                            imageVector = TablerIcons.Qrcode,
                                            contentDescription = stringResource(R.string.qr_scanner),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        FilledTonalButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = deeprInfo.link.isNotBlank() && !isFetchingMetadata,
                            onClick = fetchMetadata,
                        ) {
                            if (isFetchingMetadata) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = TablerIcons.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.fetch_name_from_link))
                        }
                    }
                }

                // Thumbnail Preview Section
                AnimatedVisibility(
                    visible = deeprInfo.thumbnail.isNotEmpty() && isThumbnailEnable,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Photo,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "Preview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            AsyncImage(
                                model = deeprInfo.thumbnail,
                                contentDescription = deeprInfo.name,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.91f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                placeholder = null,
                                error = null,
                                contentScale = ContentScale.Crop,
                            )

                            // Thumbnail URL field
                            OutlinedTextField(
                                value = deeprInfo.thumbnail,
                                onValueChange = {
                                    deeprInfo = deeprInfo.copy(thumbnail = it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.thumbnail_url)) },
                                trailingIcon =
                                    if (deeprInfo.thumbnail.isEmpty()) {
                                        null
                                    } else {
                                        {
                                            ClearInputIconButton(
                                                onClick = {
                                                    deeprInfo = deeprInfo.copy(thumbnail = "")
                                                },
                                            )
                                        }
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )

                            TextButton(
                                onClick = { deeprInfo = deeprInfo.copy(thumbnail = "") },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.X,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.remove_thumbnail))
                            }
                        }
                    }
                }

                // Details Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Name Field
                        OutlinedTextField(
                            value = deeprInfo.name,
                            onValueChange = {
                                deeprInfo = deeprInfo.copy(name = it)
                                isNameError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.enter_link_name)) },
                            supportingText = {
                                if (isNameError) {
                                    Text(text = stringResource(R.string.enter_link_name_error))
                                }
                            },
                            trailingIcon =
                                if (deeprInfo.name.isEmpty()) {
                                    null
                                } else {
                                    {
                                        ClearInputIconButton(
                                            onClick = {
                                                deeprInfo = deeprInfo.copy(name = "")
                                                isNameError = false
                                            },
                                        )
                                    }
                                },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Notes Field
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.Note,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            OutlinedTextField(
                                value = deeprInfo.notes,
                                onValueChange = {
                                    deeprInfo = deeprInfo.copy(notes = it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.enter_notes)) },
                                trailingIcon =
                                    if (deeprInfo.notes.isEmpty()) {
                                        null
                                    } else {
                                        {
                                            ClearInputIconButton(
                                                onClick = {
                                                    deeprInfo = deeprInfo.copy(notes = "")
                                                },
                                            )
                                        }
                                    },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }

                // Profile Selection Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.User,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.profile),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        // Profile Dropdown
                        var profileExpanded by remember { mutableStateOf(false) }
                        val selectedProfile = allProfiles.firstOrNull { it.id == selectedProfileId }

                        ExposedDropdownMenuBox(
                            expanded = profileExpanded,
                            onExpandedChange = { profileExpanded = !profileExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedProfile?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.select_profile)) },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                            )

                            ExposedDropdownMenu(
                                expanded = profileExpanded,
                                onDismissRequest = { profileExpanded = false },
                            ) {
                                allProfiles.forEach { profile ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                if (profile.id == selectedProfileId) {
                                                    Icon(
                                                        imageVector = TablerIcons.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                                Text(profile.name)
                                            }
                                        },
                                        onClick = {
                                            selectedProfileId = profile.id
                                            profileExpanded = false
                                        },
                                    )
                                }

                                if (allProfiles.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Plus,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                text = stringResource(R.string.create_profile),
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    },
                                    onClick = {
                                        profileExpanded = false
                                        showCreateProfileDialog = true
                                    },
                                )
                            }
                        }
                    }
                }

                // Tags Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = TablerIcons.Tag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.tags),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        // Tag Input with Autocomplete
                        var expanded by remember { mutableStateOf(false) }
                        val exactMatchExists =
                            allTags.any {
                                it.name.equals(newTagName, ignoreCase = true)
                            }
                        val filteredTags =
                            allTags.filter {
                                it.name.contains(newTagName, ignoreCase = true) &&
                                    !selectedTags.contains(it)
                            }
                        val alreadySelected =
                            selectedTags.any {
                                it.name.equals(newTagName, ignoreCase = true)
                            }
                        val showCreateOption = newTagName.isNotBlank() && !exactMatchExists && !alreadySelected

                        ExposedDropdownMenuBox(
                            expanded = expanded && (showCreateOption || filteredTags.isNotEmpty()),
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = newTagName,
                                onValueChange = {
                                    newTagName = it
                                    expanded = true
                                },
                                label = { Text(stringResource(R.string.add_tag)) },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = TablerIcons.Plus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                            )

                            ExposedDropdownMenu(
                                expanded = expanded && (showCreateOption || filteredTags.isNotEmpty()),
                                onDismissRequest = { expanded = false },
                            ) {
                                if (showCreateOption) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Icon(
                                                    imageVector = TablerIcons.Plus,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                Text(
                                                    stringResource(R.string.create_tag) + ": \"$newTagName\"",
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedTags.add(Tags(0, newTagName))
                                            newTagName = ""
                                            expanded = false
                                        },
                                    )
                                }
                                filteredTags.forEach { tag ->
                                    DropdownMenuItem(
                                        text = { Text(tag.name) },
                                        onClick = {
                                            selectedTags.add(tag)
                                            newTagName = ""
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }

                        // Selected Tags Display
                        AnimatedVisibility(
                            visible = selectedTags.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    stringResource(R.string.tags_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

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
                                                        contentDescription = removeTagText,
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(percent = 50),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                stringResource(R.string.save),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    } else {
                        Button(
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
                                                deeplinkExistsText,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    } else {
                                        save(true)
                                    }
                                } else {
                                    isError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                stringResource(R.string.save_and_execute),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FilledTonalButton(
                                modifier = Modifier.weight(1f),
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
                                                    deeplinkExistsText,
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        } else {
                                            save(false)
                                        }
                                    } else {
                                        isError = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.save),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }

                            FilledTonalButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isError = !openDeeplink(context, deeprInfo.link)
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.execute),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Auto-select newly created profile
    LaunchedEffect(allProfiles, pendingProfileNameToSelect) {
        if (pendingProfileNameToSelect != null) {
            val newProfile =
                allProfiles.find {
                    it.name.equals(pendingProfileNameToSelect, ignoreCase = true)
                }
            if (newProfile != null) {
                selectedProfileId = newProfile.id
                pendingProfileNameToSelect = null
            }
        }
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        // State variables are intentionally declared inside the dialog condition block
        // to reset when the dialog is dismissed and reopened, providing a clean state for each use
        var newProfileName by remember { mutableStateOf("") }
        var profileCreationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showCreateProfileDialog = false
                profileCreationError = null
            },
            title = {
                Text(stringResource(R.string.create_profile))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = {
                            newProfileName = it
                            profileCreationError = null
                        },
                        label = { Text(stringResource(R.string.profile_name)) },
                        singleLine = true,
                        isError = profileCreationError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    profileCreationError?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedProfileName = newProfileName.trim()
                        if (trimmedProfileName.isBlank()) {
                            profileCreationError = context.getString(R.string.profile_name_cannot_be_blank)
                            return@TextButton
                        }

                        val existingProfile =
                            allProfiles.find {
                                it.name.equals(trimmedProfileName, ignoreCase = true)
                            }

                        if (existingProfile != null) {
                            profileCreationError = context.getString(R.string.profile_name_exists)
                        } else {
                            viewModel.insertProfile(trimmedProfileName)
                            pendingProfileNameToSelect = trimmedProfileName
                            showCreateProfileDialog = false
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.profile_created_successfully),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                    enabled = newProfileName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.create_profile))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateProfileDialog = false
                        profileCreationError = null
                    },
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
