package com.yogeshpaliyal.deepr.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Download
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Upload
import kotlinx.coroutines.flow.collectLatest

data object Settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(viewModel: AccountViewModel, backStack: SnapshotStateList<Any>) {
    val context = LocalContext.current
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val launcherActivityPickResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importCsvData(it)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Settings")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            backStack.removeLastOrNull()
                        }) {
                            Icon(
                                TablerIcons.ArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ListItem(
                    modifier = Modifier.clickable(true) {
                        backStack.add(AboutUs)
                    },
                    headlineContent = { Text("About Us") },
                    leadingContent = {
                        Icon(
                            TablerIcons.InfoCircle,
                            contentDescription = "About Us"
                        )
                    }
                )
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.clickable {
                        launcherActivityPickResult.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv"
                            )
                        )
                    },
                    headlineContent = { Text("Import Deeplinks") },
                    supportingContent = { Text("Live Now") },
                    leadingContent = {
                        Icon(
                            TablerIcons.Download,
                            contentDescription = "Import Deeplinks"
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {
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
                    headlineContent = { Text("Export Deeplinks") },
                    supportingContent = { Text("Live Now") },
                    leadingContent = {
                        Icon(
                            TablerIcons.Upload,
                            contentDescription = "Export Deeplinks"
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text("Transfer Deeplinks") },
                    supportingContent = { Text("Coming Soon") },
                    leadingContent = {
                        Icon(
                            TablerIcons.Upload,
                            contentDescription = "Transfer Deeplinks"
                        )
                    }
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "App Version: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Made with ❤️ in India",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
