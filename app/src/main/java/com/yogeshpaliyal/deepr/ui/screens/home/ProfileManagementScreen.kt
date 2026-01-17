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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.FolderPlus
import compose.icons.tablericons.Folders
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

object ProfileManagementScreen : TopLevelRoute {
    override val icon: ImageVector
        get() = TablerIcons.Folders
    override val label: Int
        get() = R.string.profiles

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(windowInsets: WindowInsets) {
        val viewModel: AccountViewModel = koinActivityViewModel()
        val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
        val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
        var newProfileName by remember { mutableStateOf("") }
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        var isProfileEditEnable by remember { mutableStateOf<Profile?>(null) }
        var isProfileDeleteEnable by remember { mutableStateOf<Profile?>(null) }
        var profileEditError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            contentWindowInsets = windowInsets,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.profiles),
                            )
                            if (profiles.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(
                                        text = "${profiles.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    },
                )
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
                // Create New Profile Section
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
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
                                        imageVector = TablerIcons.FolderPlus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .size(24.dp),
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.create_profile),
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
                                    value = newProfileName,
                                    onValueChange = { newProfileName = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Enter profile name") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = TablerIcons.Folders,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    trailingIcon =
                                        if (newProfileName.isNotBlank()) {
                                            {
                                                ClearInputIconButton(onClick = { newProfileName = "" })
                                            }
                                        } else {
                                            null
                                        },
                                )

                                FilledIconButton(
                                    onClick = {
                                        val trimmedProfileName = newProfileName.trim()
                                        if (trimmedProfileName.isNotBlank()) {
                                            val existingProfile =
                                                profiles.find {
                                                    it.name.equals(trimmedProfileName, ignoreCase = true)
                                                }

                                            if (existingProfile != null) {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        context.getString(R.string.profile_name_exists),
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                            } else {
                                                viewModel.insertProfile(trimmedProfileName)
                                                newProfileName = ""
                                                Toast
                                                    .makeText(
                                                        context,
                                                        context.getString(R.string.profile_created_successfully),
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                            }
                                        }
                                    },
                                    enabled = newProfileName.isNotBlank(),
                                    modifier = Modifier.size(56.dp),
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Plus,
                                        contentDescription = stringResource(R.string.create_profile),
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Profile Info
                item {
                    AnimatedVisibility(
                        visible = currentProfile != null,
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
                                        imageVector = TablerIcons.Folders,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Column {
                                        Text(
                                            text = "Current Profile",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                        )
                                        Text(
                                            text = currentProfile?.name ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Header for Profiles List
                if (profiles.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All Profiles",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }

                // Profiles List
                if (profiles.isEmpty()) {
                    item {
                        // Empty State
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors =
                                CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                        ) {
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
                                        imageVector = TablerIcons.Folders,
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
                                    text = stringResource(R.string.no_profiles_yet),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.create_first_profile),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    items(
                        profiles.sortedBy { it.createdAt },
                        key = { it.id },
                    ) { profile ->
                        ProfileItem(
                            profile = profile,
                            isSelected = currentProfile?.id == profile.id,
                            onProfileClick = { viewModel.setSelectedProfile(profile.id) },
                            onEditClick = { isProfileEditEnable = profile },
                            onDeleteClick = { isProfileDeleteEnable = profile },
                        )
                    }
                }
            }

            // Edit Profile Dialog
            isProfileEditEnable?.let { profile ->
                AlertDialog(
                    onDismissRequest = {
                        isProfileEditEnable = null
                        profileEditError = null
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
                            text = stringResource(R.string.edit_profile),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = profile.name,
                                onValueChange = {
                                    isProfileEditEnable = profile.copy(name = it)
                                    profileEditError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.profile_name)) },
                                singleLine = true,
                                isError = profileEditError != null,
                                supportingText = {
                                    profileEditError?.let {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = TablerIcons.Folders,
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon =
                                    if (isProfileEditEnable?.name?.isNotBlank() == true) {
                                        {
                                            ClearInputIconButton(
                                                onClick = { isProfileEditEnable = profile.copy(name = "") },
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
                                val trimmedName = isProfileEditEnable?.name?.trim() ?: ""
                                if (trimmedName.isBlank()) {
                                    profileEditError = "Profile name cannot be empty"
                                    return@Button
                                }

                                val result =
                                    runBlocking {
                                        try {
                                            viewModel.updateProfile(profile.id, trimmedName)
                                            Result.success(true)
                                        } catch (e: Exception) {
                                            return@runBlocking Result.failure(e)
                                        }
                                    }
                                if (result.isFailure) {
                                    val exception = result.exceptionOrNull()
                                    profileEditError =
                                        when (exception) {
                                            is SQLiteConstraintException -> {
                                                context.getString(R.string.profile_name_exists)
                                            }
                                            else -> {
                                                context.getString(R.string.failed_to_update_profile)
                                            }
                                        }
                                } else {
                                    isProfileEditEnable = null
                                    profileEditError = null
                                    Toast
                                        .makeText(
                                            context,
                                            context.getString(R.string.profile_updated_successfully),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            },
                            enabled = isProfileEditEnable?.name?.isNotBlank() == true,
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            isProfileEditEnable = null
                            profileEditError = null
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                )
            }

            // Delete Profile Dialog
            isProfileDeleteEnable?.let { profile ->
                AlertDialog(
                    onDismissRequest = { isProfileDeleteEnable = null },
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
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val message =
                                buildAnnotatedString {
                                    append(stringResource(R.string.profile_delete_confirmation))
                                }
                            Text(text = message)

                            if (profiles.size <= 1) {
                                Card(
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.cannot_delete_only_profile),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (profiles.size > 1) {
                                    viewModel.deleteProfile(profile.id)
                                    isProfileDeleteEnable = null
                                    Toast
                                        .makeText(
                                            context,
                                            context.getString(R.string.profile_deleted_successfully),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            context.getString(R.string.cannot_delete_only_profile),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                            enabled = profiles.size > 1,
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isProfileDeleteEnable = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(
    profile: Profile,
    isSelected: Boolean,
    onProfileClick: () -> Unit,
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
        onClick = onProfileClick,
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
            RadioButton(
                selected = isSelected,
                onClick = onProfileClick,
                colors =
                    RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor,
                )
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
                        contentDescription = stringResource(R.string.edit_profile),
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
                        contentDescription = stringResource(R.string.delete_profile),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
