package com.yogeshpaliyal.deepr.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Trash
import compose.icons.tablericons.User
import kotlinx.coroutines.launch

@Composable
fun ProfilesGrid(
    profiles: List<Profile>,
    currentProfileId: Long,
    isReordering: Boolean,
    contentPaddingValues: PaddingValues,
    onProfileClick: (Profile) -> Unit,
    onProfileLongClick: (Profile) -> Unit,
    onMoveUp: (Profile) -> Unit,
    onMoveDown: (Profile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = contentPaddingValues,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(profiles, key = { it.id }) { profile ->
                val isSelected = profile.id == currentProfileId
                val hapticFeedback = LocalHapticFeedback.current
                val index = profiles.indexOf(profile)

                ElevatedCard(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .pointerInput(profile.id) {
                                detectTapGestures(
                                    onTap = { onProfileClick(profile) },
                                    onLongPress = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onProfileLongClick(profile)
                                    },
                                )
                            },
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                        ),
                ) {
                    Box {
                        Column(
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    imageVector = TablerIcons.User,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier =
                                        Modifier
                                            .padding(12.dp)
                                            .size(32.dp),
                                )
                            }
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        }

                        if (isReordering) {
                            Column(
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 4.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                if (index > 0) {
                                    IconButton(
                                        onClick = {
                                            onMoveUp(profile)
                                        },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.ChevronUp,
                                            contentDescription = "Move Up",
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                                if (index < profiles.size - 1) {
                                    IconButton(
                                        onClick = {
                                            onMoveDown(profile)
                                        },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.ChevronDown,
                                            contentDescription = "Move Down",
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
    }
}

@Composable
fun RenameDeleteProfileDialog(
    profile: Profile,
    allProfiles: List<Profile>,
    onDismiss: () -> Unit,
    viewModel: AccountViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var newName by remember { mutableStateOf(profile.name) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
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
                    text = stringResource(R.string.delete_profile),
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
                    val fullMessage = stringResource(R.string.profile_delete_confirmation_with_name, profile.name)
                    val parts = fullMessage.split(profile.name)
                    val annotatedString = buildAnnotatedString {
                        if (parts.size >= 2) {
                            append(parts[0])
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(profile.name)
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProfile(profile.id)
                        showDeleteConfirmation = false
                        onDismiss()
                        Toast.makeText(context, context.getString(R.string.profile_deleted_successfully), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allProfiles.size > 1) {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = true
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
                
                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isBlank()) {
                            error = context.getString(R.string.profile_name_cannot_be_blank)
                            return@Button
                        }
                        if (allProfiles.any { it.name.equals(trimmed, ignoreCase = true) && it.id != profile.id }) {
                            error = context.getString(R.string.profile_name_exists)
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            viewModel.updateProfile(profile.id, trimmed, profile.themeMode, profile.colorTheme)
                            onDismiss()
                            Toast.makeText(context, context.getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newName.isNotBlank() && newName != profile.name
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
