package com.yogeshpaliyal.deepr.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.gdrive.BackupStatus
import com.yogeshpaliyal.deepr.gdrive.DriveSyncManager
import com.yogeshpaliyal.deepr.ui.components.SettingsItem
import com.yogeshpaliyal.deepr.ui.components.SettingsSection
import com.yogeshpaliyal.deepr.util.formatDateTime
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Cloud
import compose.icons.tablericons.CloudUpload
import compose.icons.tablericons.Download
import compose.icons.tablericons.Refresh
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun DriveSettingsItem() {
    val syncManager: DriveSyncManager = koinInject()
    val viewModel: AccountViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isDriveAuthenticated by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<BackupStatus?>(null) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val googleSignInLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            syncManager.handleSignInResult(result.data)
            isDriveAuthenticated = syncManager.isDriveAuthenticated()
        }

    fun updateBackupStatus() {
        coroutineScope.launch {
            backupStatus = syncManager.getBackupFileInfo()
        }
    }

    LaunchedEffect(isDriveAuthenticated) {
        isDriveAuthenticated = syncManager.isDriveAuthenticated()
        if (isDriveAuthenticated) {
            updateBackupStatus()
        }
    }

    // Collect Google Drive auto backup preference state
    val googleDriveAutoBackupEnabled by viewModel.googleDriveAutoBackupEnabled.collectAsStateWithLifecycle()

    // Google Drive section - show for all users
    // Pro users get full functionality, free users see upgrade prompt
    SettingsSection("Google Drive") {
        // Pro/PlayStore build - full functionality
        if (isDriveAuthenticated) {
            GoogleDriveBackupItem(
                backupStatus = backupStatus,
                isBackingUp = isBackingUp,
                onClick = {
                    coroutineScope.launch {
                        isBackingUp = true
                        val success = syncManager.backupToDrive()
                        updateBackupStatus()
                        isBackingUp = false
                        if (success) {
                            Toast
                                .makeText(
                                    context,
                                    "Backup successful",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "Backup failed. Please try again.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    }
                },
            )
            GoogleDriveRestoreItem(
                isRestoring = isRestoring,
                onClick = {
                    // Check if backup exists before showing dialog
                    if (backupStatus?.hasBackup == true) {
                        showRestoreConfirmDialog = true
                    } else {
                        // No backup found
                        Toast
                            .makeText(
                                context,
                                "No backup found on Google Drive",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                },
            )
            GoogleDriveAutoBackupItem(
                isEnabled = googleDriveAutoBackupEnabled,
                onToggle = { enabled ->
                    viewModel.setGoogleDriveAutoBackupEnabled(enabled)
                },
            )
            SettingsItem(
                TablerIcons.Cloud,
                title = "Logout",
                onClick = {
                    syncManager.signOut()
                    isDriveAuthenticated = false
                },
            )
        } else {
            SettingsItem(
                TablerIcons.Cloud,
                title = "Login to Google Drive",
                onClick = {
                    syncManager.getSignInIntent()?.let { intent ->
                        googleSignInLauncher.launch(intent)
                    }
                },
            )
        }
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog) {
        RestoreConfirmationDialog(
            onDismiss = { showRestoreConfirmDialog = false },
            onConfirm = {
                showRestoreConfirmDialog = false
                coroutineScope.launch {
                    isRestoring = true
                    val success = syncManager.restoreFromDrive()
                    isRestoring = false
                    if (success) {
                        Toast
                            .makeText(
                                context,
                                "Restore successful",
                                Toast.LENGTH_SHORT,
                            ).show()
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Restore failed. Please try again.",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GoogleDriveBackupItem(
    backupStatus: BackupStatus?,
    isBackingUp: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !isBackingUp, onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TablerIcons.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (isBackingUp) "Backing up..." else "Backup",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val description =
                when {
                    isBackingUp -> "Please wait while your data is being backed up"
                    backupStatus?.hasBackup == true -> {
                        "Last backup: ${
                            formatDateTime(backupStatus.lastBackupDate)
                        }"
                    }

                    else -> "No backup found"
                }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isBackingUp) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(imageVector = TablerIcons.ChevronRight, contentDescription = "Go")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GoogleDriveRestoreItem(
    isRestoring: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !isRestoring, onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TablerIcons.Download,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (isRestoring) "Restoring..." else "Restore",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isRestoring) {
                Text(
                    text = "Please wait while your data is being restored",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isRestoring) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(imageVector = TablerIcons.ChevronRight, contentDescription = "Go")
        }
    }
}

@Composable
private fun RestoreConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = TablerIcons.AlertTriangle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
        },
        title = {
            Text(
                text = "Restore from Google Drive?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text =
                    "This will replace all your current data with the backup from Google Drive. " +
                        "This action cannot be undone.\n" +
                        "\nAre you sure you want to continue?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun GoogleDriveAutoBackupItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isEnabled) }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TablerIcons.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "Auto Backup",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Automatically backup when data changes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
        )
    }
}
