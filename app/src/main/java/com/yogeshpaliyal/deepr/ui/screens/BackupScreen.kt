package com.yogeshpaliyal.deepr.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.Screen
import com.yogeshpaliyal.deepr.ui.components.ServerStatusBar
import com.yogeshpaliyal.deepr.ui.components.SettingsItem
import com.yogeshpaliyal.deepr.ui.components.SettingsSection
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.FileText
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Upload
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupScreen : Screen {
    @Composable
    override fun Content() {
        BackupScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreenContent(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val backStack = LocalNavigator.current
    val context = LocalContext.current

    // Launcher for picking CSV export location
    val csvExportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv"),
        ) { uri ->
            uri?.let {
                viewModel.exportCsvData(it)
            }
        }

    // Collect sync preference states
    val syncEnabled by viewModel.syncEnabled.collectAsStateWithLifecycle()
    val syncFilePath by viewModel.syncFilePath.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    // Collect auto backup preference states
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val autoBackupLocation by viewModel.autoBackupLocation.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()

    // Launcher for picking sync file location
    val syncFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/markdown"),
        ) { uri ->
            uri?.let {
                val contentResolver = context.contentResolver

                val takeFlags: Int =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.setSyncFilePath(it.toString())
            }
        }

    // Launcher for picking auto backup location
    val backupLocationLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
        ) { uri ->
            uri?.let {
                val contentResolver = context.contentResolver

                val takeFlags: Int =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.setAutoBackupLocation(it.toString())
            }
        }

    LaunchedEffect(true) {
        viewModel.exportResultFlow.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(true) {
        viewModel.syncResultFlow.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Backup")
                    },
                    navigationIcon = {
                        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

                        IconButton(onClick = {
                            backStack.removeLast()
                        }) {
                            Icon(
                                TablerIcons.ArrowLeft,
                                contentDescription = stringResource(R.string.back),
                                modifier =
                                    if (isRtl) {
                                        Modifier.graphicsLayer(scaleX = -1f)
                                    } else {
                                        Modifier
                                    },
                            )
                        }
                    },
                )
                ServerStatusBar(
                    onServerStatusClick = {
                        if (backStack.getLast() !is LocalNetworkServer) {
                            backStack.add(LocalNetworkServer)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection("Export") {
                SettingsItem(
                    TablerIcons.Upload,
                    title = stringResource(R.string.export_deeplinks),
                    description = stringResource(R.string.export_deeplinks_description),
                    onClick = {
                        val timeStamp =
                            SimpleDateFormat(
                                "yyyyMMdd_HHmmss",
                                Locale.US,
                            ).format(Date())
                        csvExportLauncher.launch("deepr_export_$timeStamp.csv")
                    },
                )
            }

            SettingsSection("Local File Sync") {
                SettingsItem(
                    TablerIcons.Refresh,
                    title = stringResource(R.string.sync_to_file),
                    description = stringResource(R.string.sync_to_file_description),
                    onClick = {
                        viewModel.setSyncEnabled(!syncEnabled)
                    },
                    trailing = {
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { viewModel.setSyncEnabled(it) },
                        )
                    },
                )

                AnimatedVisibility(syncEnabled) {
                    Column {
                        SettingsItem(
                            TablerIcons.FileText,
                            title = stringResource(R.string.select_sync_file),
                            description =
                                if (syncFilePath.isNotEmpty()) {
                                    syncFilePath
                                        .substringAfterLast("/")
                                        .replace("%2F", "/")
                                        .replace("%20", " ")
                                        .replace("%3A", ":")
                                } else {
                                    stringResource(R.string.select_sync_file_description)
                                },
                            onClick = {
                                syncFileLauncher.launch("deeplinks.md")
                            },
                        )

                        SettingsItem(
                            TablerIcons.InfoCircle,
                            title = stringResource(R.string.last_sync_time),
                            description =
                                if (lastSyncTime > 0) {
                                    val formatter =
                                        SimpleDateFormat(
                                            "MMM dd, yyyy 'at' HH:mm",
                                            Locale.getDefault(),
                                        )
                                    stringResource(
                                        R.string.last_sync_time_format,
                                        formatter.format(Date(lastSyncTime)),
                                    )
                                } else {
                                    stringResource(R.string.last_sync_time_never)
                                },
                        )

                        AnimatedVisibility(syncFilePath.isNotEmpty()) {
                            SettingsItem(
                                TablerIcons.Upload,
                                title = stringResource(R.string.sync_now),
                                description = stringResource(R.string.sync_now_description),
                                onClick = {
                                    viewModel.syncToMarkdown()
                                },
                            )
                        }
                    }
                }
            }

            SettingsSection("Auto Backup") {
                SettingsItem(
                    TablerIcons.Upload,
                    title = stringResource(R.string.auto_backup),
                    description = stringResource(R.string.auto_backup_description),
                    onClick = {
                        viewModel.setAutoBackupEnabled(!autoBackupEnabled)
                    },
                    trailing = {
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { viewModel.setAutoBackupEnabled(it) },
                        )
                    },
                )

                AnimatedVisibility(autoBackupEnabled) {
                    Column {
                        SettingsItem(
                            TablerIcons.FileText,
                            title = stringResource(R.string.select_backup_location),
                            description =
                                if (autoBackupLocation.isNotEmpty()) {
                                    autoBackupLocation
                                        .substringAfterLast("/")
                                        .replace("%2F", "/")
                                        .replace("%20", " ")
                                        .replace("%3A", ":")
                                } else {
                                    stringResource(R.string.select_backup_location_description)
                                },
                            onClick = {
                                backupLocationLauncher.launch(null)
                            },
                        )

                        SettingsItem(
                            TablerIcons.InfoCircle,
                            title = stringResource(R.string.last_backup_time),
                            description =
                                if (lastBackupTime > 0) {
                                    val formatter =
                                        SimpleDateFormat(
                                            "MMM dd, yyyy 'at' HH:mm",
                                            Locale.getDefault(),
                                        )
                                    stringResource(
                                        R.string.last_backup_time_format,
                                        formatter.format(Date(lastBackupTime)),
                                    )
                                } else {
                                    stringResource(R.string.last_backup_time_never)
                                },
                        )
                    }
                }
            }

            SettingsSection("Transfer Links") {
                SettingsItem(
                    TablerIcons.Upload,
                    title = stringResource(R.string.transfer_link_to_another_device),
                    description = "",
                    onClick = {
                        backStack.add(TransferLinkLocalNetworkServer)
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
