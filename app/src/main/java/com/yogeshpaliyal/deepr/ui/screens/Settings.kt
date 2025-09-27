package com.yogeshpaliyal.deepr.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.MainActivity
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.components.LanguageSelectionDialog
import com.yogeshpaliyal.deepr.util.LanguageUtil
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Download
import compose.icons.tablericons.FileText
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Language
import compose.icons.tablericons.Refresh
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
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val launcherActivityPickResult =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                viewModel.importCsvData(it)
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

    LaunchedEffect(storagePermissionState.status) {
        if (storagePermissionState.status.isGranted) {
            viewModel.exportCsvData()
        }
    }

    LaunchedEffect(true) {
        viewModel.exportResultFlow.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f, fill = true)
                        .verticalScroll(rememberScrollState()),
            ) {
                HorizontalDivider()

                ListItem(
                    modifier =
                        Modifier.clickable {
                            launcherActivityPickResult.launch(
                                arrayOf(
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "application/csv",
                                ),
                            )
                        },
                    headlineContent = { Text(stringResource(R.string.import_deeplinks)) },
                    supportingContent = { Text(stringResource(R.string.import_deeplinks_description)) },
                    leadingContent = {
                        Icon(
                            TablerIcons.Download,
                            contentDescription = stringResource(R.string.import_deeplinks),
                        )
                    },
                )
                ListItem(
                    modifier =
                        Modifier.clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                viewModel.exportCsvData()
                            } else {
                                if (storagePermissionState.status.isGranted) {
                                    viewModel.exportCsvData()
                                } else {
                                    storagePermissionState.launchPermissionRequest()
                                }
                            }
                        },
                    headlineContent = { Text(stringResource(R.string.export_deeplinks)) },
                    supportingContent = { Text(stringResource(R.string.export_deeplinks_description)) },
                    leadingContent = {
                        Icon(
                            TablerIcons.Upload,
                            contentDescription = stringResource(R.string.export_deeplinks),
                        )
                    },
                )

                HorizontalDivider()

                // Sync Settings Section
                ListItem(
                    modifier =
                        Modifier.clickable {
                            viewModel.setSyncEnabled(!syncEnabled)
                        },
                    headlineContent = { Text(stringResource(R.string.sync_to_file)) },
                    supportingContent = { Text(stringResource(R.string.sync_to_file_description)) },
                    leadingContent = {
                        Icon(
                            TablerIcons.Refresh,
                            contentDescription = stringResource(R.string.sync_to_file),
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { viewModel.setSyncEnabled(it) },
                        )
                    },
                )

                if (syncEnabled) {
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                syncFileLauncher.launch("deeplinks.md")
                            },
                        headlineContent = { Text(stringResource(R.string.select_sync_file)) },
                        supportingContent = {
                            Text(
                                if (syncFilePath.isNotEmpty()) {
                                    syncFilePath
                                        .substringAfterLast("/")
                                        .replace("%2F", "/")
                                        .replace("%20", " ")
                                        .replace("%3A", ":")
                                } else {
                                    stringResource(R.string.select_sync_file_description)
                                },
                            )
                        },
                        leadingContent = {
                            Icon(
                                TablerIcons.FileText,
                                contentDescription = stringResource(R.string.select_sync_file),
                            )
                        },
                    )

                    // Show last sync time if sync is enabled
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.last_sync_time)) },
                        supportingContent = {
                            Text(
                                if (lastSyncTime > 0) {
                                    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                                    stringResource(R.string.last_sync_time_format, formatter.format(Date(lastSyncTime)))
                                } else {
                                    stringResource(R.string.last_sync_time_never)
                                },
                            )
                        },
                        leadingContent = {
                            Icon(
                                TablerIcons.InfoCircle,
                                contentDescription = stringResource(R.string.last_sync_time),
                            )
                        },
                    )

                    if (syncFilePath.isNotEmpty()) {
                        ListItem(
                            modifier =
                                Modifier.clickable {
                                    viewModel.syncToMarkdown()
                                },
                            headlineContent = { Text(stringResource(R.string.sync_now)) },
                            supportingContent = { Text(stringResource(R.string.sync_now_description)) },
                            leadingContent = {
                                Icon(
                                    TablerIcons.Upload,
                                    contentDescription = stringResource(R.string.sync_now),
                                )
                            },
                        )
                    }
                }

                HorizontalDivider()

                // Add Shortcut Icon Setting
                ListItem(
                    modifier =
                        Modifier.clickable {
                            // Toggle the preference
                            viewModel.setUseLinkBasedIcons(!useLinkBasedIcons)
                        },
                    headlineContent = { Text(stringResource(R.string.shortcut_icon)) },
                    supportingContent = {
                        Text(
                            if (useLinkBasedIcons) {
                                stringResource(
                                    R.string.use_link_app_icon,
                                )
                            } else {
                                stringResource(R.string.use_deepr_app_icon)
                            },
                        )
                    },
                    leadingContent = {
                        Icon(
                            TablerIcons.Settings,
                            contentDescription = stringResource(R.string.shortcut_icon_setting),
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = useLinkBasedIcons,
                            onCheckedChange = { viewModel.setUseLinkBasedIcons(it) },
                        )
                    },
                )
                HorizontalDivider()

                // Language Selection Setting
                ListItem(
                    modifier =
                        Modifier.clickable {
                            showLanguageDialog = true
                        },
                    headlineContent = { Text(stringResource(R.string.language)) },
                    supportingContent = {
                        Text(
                            if (languageCode.isEmpty()) {
                                stringResource(R.string.system_default)
                            } else {
                                LanguageUtil.getLanguageNativeName(languageCode).ifEmpty {
                                    stringResource(R.string.system_default)
                                }
                            },
                        )
                    },
                    leadingContent = {
                        Icon(
                            TablerIcons.Language,
                            contentDescription = stringResource(R.string.language),
                        )
                    },
                )
                HorizontalDivider()

                ListItem(
                    modifier =
                        Modifier.clickable(true) {
                            backStack.add(AboutUs)
                        },
                    headlineContent = { Text(stringResource(R.string.about_us)) },
                    leadingContent = {
                        Icon(
                            TablerIcons.InfoCircle,
                            contentDescription = stringResource(R.string.about_us),
                        )
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
    }
}
