package com.yogeshpaliyal.deepr.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.MainActivity
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.components.ExportSuccessDialog
import com.yogeshpaliyal.deepr.ui.components.LanguageSelectionDialog
import com.yogeshpaliyal.deepr.ui.components.ServerStatusBar
import com.yogeshpaliyal.deepr.util.LanguageUtil
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Download
import compose.icons.tablericons.FileText
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Language
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Server
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Upload
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data object Settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val launcherActivityPickResult =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                viewModel.importCsvData(it)
            }
        }

    // Launcher for picking CSV export location
    val csvExportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv"),
        ) { uri ->
            uri?.let {
                viewModel.exportCsvData(it)
            }
        }

    // Collect the shortcut icon preference state
    val useLinkBasedIcons by viewModel.useLinkBasedIcons.collectAsStateWithLifecycle()

    // Collect language preference state
    val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Collect sync preference states
    val syncEnabled by viewModel.syncEnabled.collectAsStateWithLifecycle()
    val syncFilePath by viewModel.syncFilePath.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    // State for export dialog
    var exportDialogState by remember { mutableStateOf<AccountViewModel.ExportDialogState?>(null) }

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
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.setSyncFilePath(it.toString())
            }
        }

    LaunchedEffect(true) {
        viewModel.exportDialogStateFlow.collectLatest { state ->
            exportDialogState = state
        }
    }

    LaunchedEffect(true) {
        viewModel.exportResultFlow.collectLatest { message ->
            // Removed toast since we're showing a dialog now
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(true) {
        viewModel.importResultFlow.collectLatest { message ->
            if (message.isNotBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
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
                        Text(stringResource(R.string.settings))
                    },
                    navigationIcon = {
                        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

                        IconButton(onClick = {
                            backStack.removeLastOrNull()
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
                        // Navigate to LocalNetworkServer screen when status bar is clicked
                        if (backStack.lastOrNull() !is LocalNetworkServer) {
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
            SettingsSection("CSV Management") {
                SettingsItem(
                    TablerIcons.Download,
                    title = stringResource(R.string.import_deeplinks),
                    description = stringResource(R.string.import_deeplinks_description),
                    onClick = {
                        launcherActivityPickResult.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv",
                            ),
                        )
                    },
                )
                SettingsItem(
                    TablerIcons.Upload,
                    title = stringResource(R.string.export_deeplinks),
                    description = stringResource(R.string.export_deeplinks_description),
                    onClick = {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
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

            SettingsSection("Others") {
                SettingsItem(
                    TablerIcons.Server,
                    title = stringResource(R.string.local_network_server),
                    onClick = {
                        backStack.add(LocalNetworkServer)
                    },
                )

                SettingsItem(
                    TablerIcons.Settings,
                    title = stringResource(R.string.shortcut_icon),
                    description =
                        if (useLinkBasedIcons) {
                            stringResource(
                                R.string.use_link_app_icon,
                            )
                        } else {
                            stringResource(R.string.use_deepr_app_icon)
                        },
                    onClick = {
                        viewModel.setUseLinkBasedIcons(!useLinkBasedIcons)
                    },
                    trailing = {
                        Switch(
                            checked = useLinkBasedIcons,
                            onCheckedChange = { viewModel.setUseLinkBasedIcons(it) },
                        )
                    },
                )

                SettingsItem(
                    TablerIcons.Language,
                    title = stringResource(R.string.language),
                    description =
                        if (languageCode.isEmpty()) {
                            stringResource(R.string.system_default)
                        } else {
                            LanguageUtil.getLanguageNativeName(languageCode).ifEmpty {
                                stringResource(R.string.system_default)
                            }
                        },
                    onClick = {
                        showLanguageDialog = true
                    },
                )

                SettingsItem(
                    TablerIcons.InfoCircle,
                    title = stringResource(R.string.about_us),
                    onClick = {
                        backStack.add(AboutUs)
                    },
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
            ) {
                Text(
                    stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.made_with_love),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguageCode = languageCode,
                onLanguageSelect = { selectedLanguageCode ->
                    viewModel.setLanguageCode(selectedLanguageCode)
                    showLanguageDialog = false
                    // Recreate activity to apply language change
                    (context as? MainActivity)?.let { activity ->
                        activity.recreate()
                    }
                },
                onDismiss = { showLanguageDialog = false },
            )
        }

        // Export Success Dialog
        exportDialogState?.let { state ->
            ExportSuccessDialog(
                message = state.message,
                uri = state.uri,
                onDismiss = { exportDialogState = null },
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: ((onComplete: (() -> Unit)?) -> Unit)? = null,
    isDestructive: Boolean = false,
    shouldShowLoading: Boolean = false,
) {
    var isLoading by remember { mutableStateOf(false) }

    val contentColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(true, onClick = {
                            if (shouldShowLoading) {
                                isLoading = true
                            }
                            onClick({
                                isLoading = false
                            })
                        })
                    } else {
                        Modifier
                    },
                ).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isDestructive) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }

        trailing?.invoke()

        if (onClick != null && trailing == null && !isLoading) {
            Icon(imageVector = TablerIcons.ChevronRight, contentDescription = "Go")
        }

        if (isLoading) {
            ContainedLoadingIndicator(modifier = Modifier.size(32.dp))
        }
    }
}
