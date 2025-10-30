package com.yogeshpaliyal.deepr.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanOptions
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.SharedLink
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.components.DeleteConfirmationDialog
import com.yogeshpaliyal.deepr.ui.components.QrCodeDialog
import com.yogeshpaliyal.deepr.ui.components.ServerStatusBar
import com.yogeshpaliyal.deepr.ui.screens.LocalNetworkServer
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Link
import compose.icons.tablericons.Note
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Search
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Tag
import compose.icons.tablericons.Trash
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

data object Home

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun HomeScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    deeprQueries: DeeprQueries = koinInject(),
    analyticsManager: com.yogeshpaliyal.deepr.analytics.AnalyticsManager = koinInject(),
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    var isTagsSelectionActive by remember { mutableStateOf(false) }

    var selectedLink by remember { mutableStateOf<GetLinksAndTags?>(null) }
    val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState(blurEnabled = true)
    val context = LocalContext.current
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    val totalLinks by viewModel.countOfLinks.collectAsStateWithLifecycle()
    val favouriteLinks by viewModel.countOfFavouriteLinks.collectAsStateWithLifecycle()
    val allTagsWithCount by viewModel.allTagsWithCount.collectAsStateWithLifecycle()

    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents == null) {
                Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                val normalizedLink = normalizeLink(result.contents)
                if (isValidDeeplink(normalizedLink)) {
                    selectedLink = createDeeprObject(link = normalizedLink)
                } else {
                    Toast.makeText(context, "Invalid deeplink", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Handle shared text from other apps
    LaunchedEffect(sharedText) {
        if (!sharedText?.url.isNullOrBlank()) {
            val normalizedLink = normalizeLink(sharedText.url)
            if (isValidDeeplink(normalizedLink)) {
                selectedLink =
                    createDeeprObject(link = normalizedLink, name = sharedText.title ?: "")
            } else {
                Toast
                    .makeText(context, "Invalid deeplink from shared content", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    LaunchedEffect(textFieldState.text) {
        viewModel.search(textFieldState.text.toString())
    }

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = {
                    if (searchBarState.currentValue == SearchBarValue.Collapsed) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.search),
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                leadingIcon = {
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above,
                                ),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.back)) } },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(
                                onClick = { scope.launch { searchBarState.animateToCollapsed() } },
                            ) {
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
                        }
                    } else {
                        Icon(TablerIcons.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (searchBarState.currentValue == SearchBarValue.Collapsed) {
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Below,
                                ),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.sorting)) } },
                            state = rememberTooltipState(),
                        ) {
                            FilterMenu(onSortOrderChange = {
                                viewModel.setSortOrder(it)
                            })
                        }
                    } else {
                        if (textFieldState.text.isNotEmpty()) {
                            ClearInputIconButton(
                                onClick = {
                                    textFieldState.clearText()
                                },
                            )
                        }
                    }
                },
            )
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
                AppBarWithSearch(
                    scrollBehavior = scrollBehavior,
                    state = searchBarState,
                    inputField = inputField,
                    colors =
                        SearchBarDefaults.appBarWithSearchColors(
                            appBarContainerColor = Color.Transparent,
                        ),
                )
                ServerStatusBar(
                    onServerStatusClick = {
                        // Navigate to LocalNetworkServer screen when status bar is clicked
                        analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.NAVIGATE_LOCAL_SERVER)
                        if (backStack.lastOrNull() !is LocalNetworkServer) {
                            backStack.add(LocalNetworkServer)
                        }
                    },
                )

                val favouriteFilter by viewModel.favouriteFilter.collectAsStateWithLifecycle()
                // Favourite filter tabs
                SingleChoiceSegmentedButtonRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    SegmentedButton(
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = 0,
                                count = 2,
                            ),
                        onClick = { viewModel.setFavouriteFilter(-1) },
                        selected = favouriteFilter == -1,
                        label = { Text(stringResource(R.string.all) + " (${totalLinks ?: 0})") },
                    )
                    SegmentedButton(
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = 1,
                                count = 2,
                            ),
                        onClick = { viewModel.setFavouriteFilter(1) },
                        selected = favouriteFilter == 1,
                        label = { Text(stringResource(R.string.favourites) + " (${favouriteLinks ?: 0})") },
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom),
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                    content = {
                        IconButton(onClick = {
                            analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.SCAN_QR_CODE)
                            qrScanner.launch(ScanOptions())
                        }) {
                            Icon(
                                TablerIcons.Qrcode,
                                contentDescription = stringResource(R.string.qr_scanner),
                            )
                        }
                        IconButton(onClick = {
                            isTagsSelectionActive = true
                        }) {
                            Icon(
                                TablerIcons.Tag,
                                contentDescription = stringResource(R.string.tags),
                            )
                        }
                        IconButton(onClick = {
                            // Settings action
                            analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.NAVIGATE_SETTINGS)
                            backStack.add(Settings)
                        }) {
                            Icon(
                                TablerIcons.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = {
                            selectedLink = createDeeprObject()
                        }) {
                            Icon(
                                TablerIcons.Plus,
                                contentDescription = stringResource(R.string.add_link),
                            )
                        }
                    },
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            Content(
                hazeState = hazeState,
                contentPaddingValues = contentPadding,
                selectedTag = selectedTag,
                editDeepr = {
                    selectedLink = it
                },
            )
        }

        selectedLink?.let {
            HomeBottomContent(
                deeprQueries = deeprQueries,
                selectedLink = it,
            ) { updatedValue ->
                if (updatedValue != null) {
                    if (updatedValue.executeAfterSave) {
                        openDeeplink(context, updatedValue.deepr.link)
                    }
                }
                selectedLink = null
                resetSharedText()
            }
        }

        if (isTagsSelectionActive) {
            TagSelectionBottomSheet(
                tagsWithCount = allTagsWithCount,
                selectedTag = selectedTag,
                dismissBottomSheet = {
                    isTagsSelectionActive = false
                },
                setTagFilter = { viewModel.setTagFilter(it) },
                editTag = { tag ->
                    runBlocking {
                        try {
                            viewModel.updateTag(tag)
                            Result.success(true)
                        } catch (e: Exception) {
                            return@runBlocking Result.failure(e)
                        }
                    }
                },
                deleteTag = {
                    viewModel.deleteTag(it.id)
                    Result.success(true)
                },
                deeprQueries = deeprQueries,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Content(
    hazeState: HazeState,
    selectedTag: List<Tags>,
    contentPaddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    editDeepr: (GetLinksAndTags) -> Unit = {},
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()
    val showMoreBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMoreSelectedItem by remember { mutableStateOf<GetLinksAndTags?>(null) }
    val analyticsManager = koinInject<AnalyticsManager>()

    if (accounts == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { ContainedLoadingIndicator() }
        return
    }

    val context = LocalContext.current
    var showShortcutDialog by remember { mutableStateOf<GetLinksAndTags?>(null) }
    var showQrCodeDialog by remember { mutableStateOf<GetLinksAndTags?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<GetLinksAndTags?>(null) }

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

    showDeleteConfirmDialog?.let { deepr ->
        DeleteConfirmationDialog(
            deepr = deepr,
            onDismiss = { showDeleteConfirmDialog = null },
            onConfirm = {
                viewModel.deleteAccount(it.id)
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            },
        )
    }

    val onItemClick: (MenuItem) -> Unit = {
        showMoreSelectedItem = null
        when (it) {
            is MenuItem.Click -> {
                viewModel.incrementOpenedCount(it.item.id)
                openDeeplink(context, it.item.link)
                analyticsManager.logEvent(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.OPEN_LINK,
                    mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to it.item.id),
                )
            }

            is MenuItem.Delete -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_DELETE)
                showDeleteConfirmDialog = it.item
            }
            is MenuItem.Edit -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_EDIT)
                editDeepr(it.item)
            }
            is MenuItem.FavouriteClick -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_FAVOURITE)
                viewModel.toggleFavourite(it.item.id)
            }
            is MenuItem.ResetCounter -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_RESET_COUNTER)
                viewModel.resetOpenedCount(it.item.id)
                Toast.makeText(context, "Opened count reset", Toast.LENGTH_SHORT).show()
            }

            is MenuItem.Shortcut -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_SHORTCUT)
                showShortcutDialog = it.item
            }

            is MenuItem.ShowQrCode -> {
                analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.ITEM_MENU_QR_CODE)
                showQrCodeDialog = it.item
            }
            is MenuItem.MoreOptionsBottomSheet -> {
                showMoreSelectedItem = it.item
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        DeeprList(
            modifier =
                Modifier
                    .weight(1f)
                    .hazeSource(state = hazeState)
                    .padding(8.dp),
            contentPaddingValues = contentPaddingValues,
            accounts = accounts!!,
            selectedTag = selectedTag,
            onItemClick = onItemClick,
            onTagClick = {
                // Toggle the tag in the filter by tag name
                viewModel.setSelectedTagByName(it)
            },
            isThumbnailEnable = isThumbnailEnable,
        )
    }

    showMoreSelectedItem?.let { account ->
        ModalBottomSheet(sheetState = showMoreBottomSheet, onDismissRequest = {
            showMoreSelectedItem = null
        }) {
            val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = {
                            Column {
                                Text(
                                    text = account.name,
                                )
                                Text(
                                    text = account.link,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .clickable {
                                    onItemClick(MenuItem.Click(account))
                                },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }

                if (account.thumbnail.isNotEmpty() && isThumbnailEnable) {
                    item {
                        AsyncImage(
                            model = account.thumbnail,
                            contentDescription = account.name,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.91f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            placeholder = null,
                            error = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                if (account.notes.isNotEmpty()) {
                    item {
                        MenuListItem(
                            text = account.notes,
                            icon = TablerIcons.Note,
                            selectable = true,
                        )
                    }
                }

                item {
                    ShortcutMenuItem(account, {
                        onItemClick(MenuItem.Shortcut(it))
                    })
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.show_qr_code),
                        icon = TablerIcons.Qrcode,
                        onClick = {
                            onItemClick(MenuItem.ShowQrCode(account))
                        },
                    )
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.reset_opened_count),
                        icon = TablerIcons.Refresh,
                        onClick = {
                            onItemClick(MenuItem.ResetCounter(account))
                        },
                    )
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.edit),
                        icon = TablerIcons.Edit,
                        onClick = {
                            onItemClick(MenuItem.Edit(account))
                        },
                    )
                }
                item {
                    MenuListItem(
                        text = stringResource(R.string.delete),
                        icon = TablerIcons.Trash,
                        onClick = {
                            onItemClick(MenuItem.Delete(account))
                        },
                        colors =
                            ListItemDefaults.colors(
                                headlineColor = MaterialTheme.colorScheme.error,
                                leadingIconColor = MaterialTheme.colorScheme.error,
                                containerColor = Color.Transparent,
                            ),
                    )
                }

                // Display last opened time
                if (account.lastOpenedAt != null) {
                    item {
                        MenuListItem(
                            text =
                                stringResource(
                                    R.string.last_opened,
                                    formatDateTime(account.lastOpenedAt),
                                ),
                            textStyle = MaterialTheme.typography.bodySmall,
                            onClick = {
                                onItemClick(MenuItem.Edit(account))
                            },
                            icon = null,
                            colors =
                                ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuListItem(
    text: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    onClick: (() -> Unit)? = null,
    selectable: Boolean = false,
) {
    ListItem(
        headlineContent = {
            if (selectable) {
                SelectionContainer {
                    Text(
                        text = text,
                        style = textStyle,
                    )
                }
            } else {
                Text(
                    text = text,
                    style = textStyle,
                )
            }
        },
        modifier =
            modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        leadingContent = {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = text,
                )
            }
        },
        colors = colors,
    )
}

@Composable
fun DeeprList(
    accounts: List<GetLinksAndTags>,
    selectedTag: List<Tags>,
    contentPaddingValues: PaddingValues,
    onItemClick: (MenuItem) -> Unit,
    onTagClick: (String) -> Unit,
    isThumbnailEnable: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = accounts.isEmpty(),
        enter = scaleIn() + expandVertically(expandFrom = Alignment.CenterVertically),
        exit = scaleOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
    ) {
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
                    text = stringResource(R.string.no_links_saved_yet),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.save_your_link_below),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push content up
        }
    }
    AnimatedVisibility(
        visible = accounts.isNotEmpty(),
        enter = scaleIn() + expandVertically(expandFrom = Alignment.CenterVertically),
        exit = scaleOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPaddingValues,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                count = accounts.size,
                key = { index -> accounts[index].id },
            ) { index ->
                val account = accounts[index]

                DeeprItem(
                    modifier = Modifier.animateItem(),
                    account = account,
                    selectedTag = selectedTag,
                    onItemClick = onItemClick,
                    onTagClick = onTagClick,
                    isThumbnailEnable = isThumbnailEnable,
                )
            }
        }
    }
}
