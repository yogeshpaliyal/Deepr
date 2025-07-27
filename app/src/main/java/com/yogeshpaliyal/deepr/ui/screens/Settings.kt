package com.yogeshpaliyal.deepr.ui.screens

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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.BuildConfig
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Download
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Upload

data object Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(backStack: SnapshotStateList<Any>) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
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
                    headlineContent = { Text("Import Deeplinks") },
                    supportingContent = { Text("Coming Soon") },
                    leadingContent = {
                        Icon(
                            TablerIcons.Download,
                            contentDescription = "Import Deeplinks"
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text("Export Deeplinks") },
                    supportingContent = { Text("Coming Soon") },
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