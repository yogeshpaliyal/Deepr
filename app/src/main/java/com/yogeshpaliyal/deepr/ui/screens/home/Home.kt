package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanOptions
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.components.EditDeeplinkDialog
import com.yogeshpaliyal.deepr.ui.components.QrCodeDialog
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Link
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Search
import compose.icons.tablericons.Settings
import compose.icons.tablericons.X
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

data object Home

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    deeprQueries: DeeprQueries = koinInject(),
    sharedText: String? = null,
    resetSharedText: () -> Unit,
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var saveDialogInfo by remember { mutableStateOf<SaveDialogInfo?>(null) }
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents == null) {
                Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                if (isValidDeeplink(result.contents)) {
                    saveDialogInfo = SaveDialogInfo(result.contents, false)
                } else {
                    Toast.makeText(context, "Invalid deeplink", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Handle shared text from other apps
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank() && saveDialogInfo == null) {
            if (isValidDeeplink(sharedText)) {
                saveDialogInfo = SaveDialogInfo(sharedText, false)
            } else {
                Toast
                    .makeText(context, "Invalid deeplink from shared content", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier =
                    Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.ultraThin(),
                        ).fillMaxWidth(),
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.largeTopAppBarColors(Color.Transparent),
                    title = {
                        Text("Deepr")
                    },
                    actions = {
                        IconButton(onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                searchQuery = ""
                                viewModel.search("")
                            }
                        }) {
                            Icon(
                                if (isSearchActive) TablerIcons.X else TablerIcons.Search,
                                contentDescription = if (isSearchActive) "Close search" else "Search",
                            )
                        }
                        IconButton(onClick = {
                            qrScanner.launch(ScanOptions())
                        }) {
                            Icon(
                                TablerIcons.Qrcode,
                                contentDescription = "QR Scanner",
                            )
                        }
                        FilterMenu(onSortOrderChange = {
                            viewModel.setSortOrder(it)
                        })
                        IconButton(onClick = {
                            backStack.add(Settings)
                        }) {
                            Icon(
                                TablerIcons.Settings,
                                contentDescription = "Settings",
                            )
                        }
                    },
                )
                AnimatedVisibility(visible = isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder = { Text("Search...") },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    )
                }
            }
        },
        bottomBar = {
            HomeBottomContent(
                hazeState = hazeState,
                saveDialogInfo = saveDialogInfo,
                deeprQueries = deeprQueries,
            ) {
                saveDialogInfo = it
                if (it == null) {
                    resetSharedText()
                }
            }
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            Content(
                hazeState = hazeState,
                contentPaddingValues = contentPadding,
                deeprQueries = deeprQueries,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Content(
    hazeState: HazeState,
    contentPaddingValues: PaddingValues,
    deeprQueries: DeeprQueries,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    if (accounts == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { ContainedLoadingIndicator() }
        return
    }

    Column(modifier.fillMaxSize()) {
        val context = LocalContext.current
        var showShortcutDialog by remember { mutableStateOf<Deepr?>(null) }
        var showQrCodeDialog by remember { mutableStateOf<Deepr?>(null) }
        var showEditDialog by remember { mutableStateOf<Deepr?>(null) }

        showShortcutDialog?.let { deepr ->
            CreateShortcutDialog(
                deepr = deepr,
                onDismiss = { showShortcutDialog = null },
            )
        }

        showQrCodeDialog?.let {
            QrCodeDialog(it) {
                showQrCodeDialog = null
            }
        }

        showEditDialog?.let { deepr ->
            EditDeeplinkDialog(
                deepr = deepr,
                onDismiss = { showEditDialog = null },
                onSave = { newLink, newName ->
                    if (deeprQueries.getDeeprByLink(newLink).executeAsOneOrNull() != null) {
                        Toast
                            .makeText(context, "Deeplink already exists", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        viewModel.updateDeeplink(deepr.id, newLink, newName)
                        Toast.makeText(context, "Deeplink updated", Toast.LENGTH_SHORT).show()
                    }
                    showEditDialog = null
                },
            )
        }

        DeeprList(
            modifier =
                Modifier
                    .weight(1f)
                    .hazeSource(state = hazeState)
                    .padding(8.dp),
            contentPaddingValues = contentPaddingValues,
            accounts = accounts!!,
            onItemClick = {
                viewModel.incrementOpenedCount(it.id)
                openDeeplink(context, it.link)
            },
            onRemoveClick = {
                viewModel.deleteAccount(it.id)
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            },
            onShortcutClick = {
                showShortcutDialog = it
            },
            onEditClick = {
                showEditDialog = it
            },
            onItemLongClick = {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Link copied", it.link)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
            },
            onQrCodeCLick = {
                showQrCodeDialog = it
            },
        )
    }
}

@Composable
fun DeeprList(
    accounts: List<Deepr>,
    contentPaddingValues: PaddingValues,
    onItemClick: (Deepr) -> Unit,
    onRemoveClick: (Deepr) -> Unit,
    onShortcutClick: (Deepr) -> Unit,
    onEditClick: (Deepr) -> Unit,
    onItemLongClick: (Deepr) -> Unit,
    onQrCodeCLick: (Deepr) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accounts.isEmpty()) {
        // When empty, use a Column with weights to ensure vertical centering
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Push content down

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    TablerIcons.Link,
                    contentDescription = "No links",
                    modifier =
                        Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "No deeplinks saved yet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Save your frequently used deeplinks below to quickly access them later.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push content up
        }
    } else {
        LazyColumn(modifier = modifier, contentPadding = contentPaddingValues) {
            items(accounts) { account ->
                DeeprItem(
                    account = account,
                    onItemClick = onItemClick,
                    onRemoveClick = onRemoveClick,
                    onShortcutClick = onShortcutClick,
                    onEditClick = onEditClick,
                    onItemLongClick = onItemLongClick,
                    onQrCodeClick = onQrCodeCLick,
                )
            }
        }
    }
}
