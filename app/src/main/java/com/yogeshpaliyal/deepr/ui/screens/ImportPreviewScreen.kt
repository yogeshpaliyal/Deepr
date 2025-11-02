package com.yogeshpaliyal.deepr.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.backup.LinkImportCandidate
import com.yogeshpaliyal.deepr.backup.importer.TextFileImporter
import com.yogeshpaliyal.deepr.util.RequestResult
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class ImportPreviewScreen(val uri: Uri)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreenContent(
    uri: Uri,
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var candidates by remember { mutableStateOf<List<LinkImportCandidate>>(emptyList()) }
    val selectedStates = remember { mutableStateMapOf<String, Boolean>() }
    var isLoading by remember { mutableStateOf(true) }
    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load the preview data
    LaunchedEffect(uri) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val textFileImporter: TextFileImporter = object : KoinComponent {
                    fun getImporter() = get<TextFileImporter>()
                }.getImporter()

                when (val result = textFileImporter.parseForPreview(uri)) {
                    is RequestResult.Success -> {
                        candidates = result.data
                        // Initialize selection states
                        result.data.forEach { candidate ->
                            selectedStates[candidate.link] = candidate.isSelected
                        }
                        isLoading = false
                    }
                    is RequestResult.Error -> {
                        errorMessage = result.message
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Select Links to Import")
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
        },
        bottomBar = {
            if (!isLoading && errorMessage == null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            // Select all valid, non-duplicate links
                            candidates.forEach { candidate ->
                                if (candidate.isValid && !candidate.isDuplicate) {
                                    selectedStates[candidate.link] = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    ) {
                        Text("Select All")
                    }
                    Button(
                        onClick = {
                            // Deselect all
                            candidates.forEach { candidate ->
                                selectedStates[candidate.link] = false
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    ) {
                        Text("Deselect All")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            when {
                isLoading -> {
                    Text(
                        "Loading links...",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                errorMessage != null -> {
                    Text(
                        "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                candidates.isEmpty() -> {
                    Text(
                        "No links found in the file",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                    ) {
                        item {
                            Text(
                                "Found ${candidates.size} link(s). Select the ones you want to import:",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        items(candidates) { candidate ->
                            LinkImportItem(
                                candidate = candidate,
                                isSelected = selectedStates[candidate.link] ?: false,
                                onSelectionChange = { selected ->
                                    selectedStates[candidate.link] = selected
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val selectedLinks =
                                candidates
                                    .filter { selectedStates[it.link] == true && it.isValid && !it.isDuplicate }
                                    .map { it.link }

                            if (selectedLinks.isEmpty()) {
                                Toast.makeText(context, "No valid links selected", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isImporting = true
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val textFileImporter: TextFileImporter = object : KoinComponent {
                                        fun getImporter() = get<TextFileImporter>()
                                    }.getImporter()

                                    when (val result = textFileImporter.importSelected(selectedLinks)) {
                                        is RequestResult.Success -> {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Import complete! Added: ${result.data.importedCount}, Skipped: ${result.data.skippedCount}",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                                backStack.removeLastOrNull()
                                            }
                                        }
                                        is RequestResult.Error -> {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Import failed: ${result.message}",
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                        }
                                    }
                                    isImporting = false
                                }
                            }
                        },
                        enabled = !isImporting && selectedStates.values.any { it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(if (isImporting) "Importing..." else "Import Selected Links")
                    }
                }
            }
        }
    }
}

@Composable
fun LinkImportItem(
    candidate: LinkImportCandidate,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = when {
                        !candidate.isValid -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        candidate.isDuplicate -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                    shape = MaterialTheme.shapes.small,
                ).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { checked ->
                if (candidate.isValid && !candidate.isDuplicate) {
                    onSelectionChange(checked)
                }
            },
            enabled = candidate.isValid && !candidate.isDuplicate,
        )

        Column(
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        ) {
            Text(
                text = candidate.link,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!candidate.isValid) {
                Text(
                    text = "Invalid link",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (candidate.isDuplicate) {
                Text(
                    text = "Already exists",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
