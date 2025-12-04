package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yogeshpaliyal.deepr.server.DeeprTag
import com.yogeshpaliyal.deepr.LocalSharedText
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.SharedLink
import com.yogeshpaliyal.deepr.analytics.AnalyticsEvents
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.analytics.AnalyticsParams
import com.yogeshpaliyal.shared.data.DeeprLink
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.shared.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.components.DeleteConfirmationDialog
import com.yogeshpaliyal.deepr.ui.components.QrCodeDialog
import com.yogeshpaliyal.deepr.ui.components.ServerStatusBar
import com.yogeshpaliyal.deepr.ui.screens.LocalNetworkServer
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Click
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Copy
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Delete
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Edit
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.FavouriteClick
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.MoreOptionsBottomSheet
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.ResetCounter
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Shortcut
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.ShowQrCode
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplinkExternal
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import com.yogeshpaliyal.shared.data.SharedLink
import com.yogeshpaliyal.shared.ui.screens.LocalSharedText
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Edit
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Home
import compose.icons.tablericons.Link
import compose.icons.tablericons.Note
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Search
import compose.icons.tablericons.Tag
import compose.icons.tablericons.Trash
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

data object Home



@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
class Dashboard2(
    val mSelectedLink: DeeprLink? = null,
) : TopLevelRoute {
    override val icon: ImageVector
        get() = TablerIcons.Home
    override val label: Int
        get() = R.string.home

    @Composable
    override fun Content(windowInsets: WindowInsets) {
        val localSharedText = LocalSharedText.current
        Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
            HomeScreen(
                windowInsets,
                mSelectedLink = mSelectedLink,
                sharedText = localSharedText?.first,
                resetSharedText = {
                    localSharedText?.second?.invoke()
                },
            )
        }
    }
}

data class FilterTagItem(
    val name: String,
    val count: Long,
    val isSelected: Boolean,
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun HomeScreen(
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    analyticsManager: AnalyticsManager = koinInject(),
    mSelectedLink: DeeprLink? = null,
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    val viewModel: AccountViewModel = koinActivityViewModel()
    val currentViewType by viewModel.viewType.collectAsStateWithLifecycle()
    val localNavigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    val tagsCollect = viewModel.allTagsWithCount.collectAsStateWithLifecycle()
    val tags = tagsCollect.value?.tags ?: listOf()

    var selectedLink by remember { mutableStateOf<DeeprLink?>(mSelectedLink) }
    val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState(blurEnabled = true)
    val context = LocalContext.current
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    val totalLinks by viewModel.countOfLinks.collectAsStateWithLifecycle()
    val favouriteLinks by viewModel.countOfFavouriteLinks.collectAsStateWithLifecycle()
    val favouriteFilter by viewModel.favouriteFilter.collectAsStateWithLifecycle()
    var finalTagsInfo by remember { mutableStateOf<List<FilterTagItem>?>(listOf()) }
    val listState =
        if (currentViewType == ViewType.GRID) rememberLazyStaggeredGridState() else rememberLazyListState()
    val isExpanded by remember(listState) {
        // Example: expanded only when at the very top of the list
        derivedStateOf {
            if (listState is LazyStaggeredGridState) {
                listState.firstVisibleItemIndex == 0
            } else if (listState is LazyListState) {
                listState.firstVisibleItemIndex == 0
            } else {
                true
            }
        }
    }

    BackHandler(enabled = selectedTag.isNotEmpty() || searchBarState.currentValue == SearchBarValue.Expanded) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        if (searchBarState.currentValue == SearchBarValue.Expanded) {
            scope.launch {
                searchBarState.animateToCollapsed()
            }
        } else if (selectedTag.isNotEmpty()) {
            viewModel.setTagFilter(null)
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

    LaunchedEffect(selectedTag, tags) {
        // Get unique tags by merging both but first items should be selected tag and then tags
        val allTagsList = tags
        val mergedList = mutableListOf<FilterTagItem>()
        val mapOfSelectedList = HashMap<String, Long>()

        val alreadyAdded = HashSet<String>()

        allTagsList.forEach { tag ->
            mapOfSelectedList.put(tag.name, tag.count)
        }

        selectedTag.forEach { tag ->
            alreadyAdded.add(tag.name)
            val count = mapOfSelectedList[tag.name] ?: 0L
            mergedList.add(FilterTagItem(tag.name, count, true))
        }

        allTagsList.forEach { tag ->
            if (alreadyAdded.contains(tag.name).not()) {
                alreadyAdded.add(tag.name)
                mergedList.add(FilterTagItem(tag.name, tag.count, false))
            }
        }
        finalTagsInfo = mergedList
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
                        Row {
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Below,
                                    ),
                                tooltip = { PlainTooltip { Text("View Type") } },
                                state = rememberTooltipState(),
                            ) {
                                ViewTypeMenu(currentViewType, {
                                    viewModel.setViewType(it)
                                })
                            }

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
        contentWindowInsets = windowInsets,
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
                        analyticsManager.logEvent(AnalyticsEvents.NAVIGATE_LOCAL_SERVER)
                        if (localNavigator.getLast() !is LocalNetworkServer) {
                            localNavigator.add(LocalNetworkServer)
                        }
                    },
                )

                LazyRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    item {
                        FilterChip(
                            selectedTag.isEmpty() && favouriteFilter == -1,
                            {
                                viewModel.setFavouriteFilter(-1)
                                viewModel.setTagFilter(null)
                            },
                            label = {
                                Text(stringResource(R.string.all) + " (${totalLinks?.count ?: 0})")
                            },
                            modifier = Modifier.animateItem(),
                            shape = RoundedCornerShape(percent = 50),
                        )
                    }
                    item {
                        FilterChip(
                            selectedTag.isEmpty() && favouriteFilter == 1,
                            {
                                viewModel.setFavouriteFilter(1)
                                viewModel.setTagFilter(null)
                            },
                            label = {
                                Text(stringResource(R.string.favourites) + " (${favouriteLinks?.count ?: 0})")
                            },
                            modifier = Modifier.animateItem(),
                            shape = RoundedCornerShape(percent = 50),
                        )
                    }

                    items(finalTagsInfo ?: listOf()) {
                        FilterChip(
                            it.isSelected,
                            {
                                viewModel.setSelectedTagByName(it.name)
                            },
                            label = {
                                Text(it.name + " (${it.count})")
                            },
                            modifier = Modifier.animateItem(),
                            shape = RoundedCornerShape(percent = 50),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        TablerIcons.Plus,
                        contentDescription = stringResource(R.string.add_link),
                    )
                },
                text = {
                    Text(stringResource(R.string.add_link))
                },
                expanded = isExpanded,
                onClick = {
                    selectedLink = createDeeprObject()
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            val layoutDirection = LocalLayoutDirection.current
            Content(
                listState = listState,
                viewModel = viewModel,
                hazeState = hazeState,
                contentPaddingValues =
                    PaddingValues(
                        start = contentPadding.calculateLeftPadding(layoutDirection),
                        end = contentPadding.calculateRightPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding() + 8.dp,
                        bottom = contentPadding.calculateBottomPadding() + 8.dp,
                    ),
                selectedTag = selectedTag,
                currentViewType = currentViewType,
                searchQuery = textFieldState.text.toString(),
                favouriteFilter = favouriteFilter,
                editDeepr = {
                    selectedLink = it
                },
            )
        }

        selectedLink?.let {
            HomeBottomContent(
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
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Content(
    listState: ScrollableState,
    hazeState: HazeState,
    selectedTag: List<DeeprTag>,
    contentPaddingValues: PaddingValues,
    currentViewType: @ViewType Int,
    searchQuery: String,
    favouriteFilter: Int,
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    editDeepr: (DeeprLink) -> Unit = {},
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()
    val showMoreBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMoreSelectedItem by remember { mutableStateOf<DeeprLink?>(null) }
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
    var showShortcutDialog by remember { mutableStateOf<DeeprLink?>(null) }
    var showQrCodeDialog by remember { mutableStateOf<DeeprLink?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<DeeprLink?>(null) }

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
            is Click -> {
                viewModel.incrementOpenedCount(it.item.id)
                openDeeplink(context, it.item.link)
                analyticsManager.logEvent(
                    AnalyticsEvents.OPEN_LINK,
                    mapOf(AnalyticsParams.LINK_ID to it.item.id),
                )
            }

            is Delete -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_DELETE)
                showDeleteConfirmDialog = it.item
            }

            is Edit -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_EDIT)
                editDeepr(it.item)
            }

            is FavouriteClick -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_FAVOURITE)
                viewModel.toggleFavourite(it.item.id)
            }

            is ResetCounter -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_RESET_COUNTER)
                viewModel.resetOpenedCount(it.item.id)
                Toast.makeText(context, "Opened count reset", Toast.LENGTH_SHORT).show()
            }

            is Shortcut -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_SHORTCUT)
                showShortcutDialog = it.item
            }

            is ShowQrCode -> {
                analyticsManager.logEvent(AnalyticsEvents.ITEM_MENU_QR_CODE)
                showQrCodeDialog = it.item
            }

            is MoreOptionsBottomSheet -> {
                showMoreSelectedItem = it.item
            }

            is Copy -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText(context.getString(R.string.link_copied), it.item.link)
                clipboard.setPrimaryClip(clip)
                Toast
                    .makeText(context, context.getString(R.string.link_copied), Toast.LENGTH_SHORT)
                    .show()
            }

            is MenuItem.OpenWith -> {
                openDeeplinkExternal(context, it.item.link)
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        DeeprList(
            listState = listState,
            modifier =
                Modifier
                    .weight(1f)
                    .hazeSource(state = hazeState)
                    .padding(horizontal = 8.dp),
            contentPaddingValues = contentPaddingValues,
            accounts = accounts?.links ?: emptyList(),
            selectedTag = selectedTag,
            onTagClick = {
                viewModel.setSelectedTagByName(it)
            },
            isThumbnailEnable = isThumbnailEnable,
            searchQuery = searchQuery,
            favouriteFilter = favouriteFilter,
            viewType = currentViewType,
            onItemClick = onItemClick,
        )
    }
    showMoreSelectedItem?.let { account ->
        ModalBottomSheet(sheetState = showMoreBottomSheet, onDismissRequest = {
            showMoreSelectedItem = null
        }) {
            val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()
            var tagsExpanded by remember { mutableStateOf(false) }
            val selectedTags =
                remember(account.tagsNames) { account.tagsNames?.split(",")?.toMutableList() }

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
                                    onItemClick(Click(account))
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

                item {
                    MenuListItem(
                        text =
                            if (account.isFavourite == 1L) {
                                stringResource(R.string.remove_from_favourites)
                            } else {
                                stringResource(
                                    R.string.add_to_favourites,
                                )
                            },
                        icon = if (account.isFavourite == 1L) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        selectable = true,
                        onClick = {
                            onItemClick(FavouriteClick(account))
                        },
                    )
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
                        onItemClick(Shortcut(it))
                    })
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.show_qr_code),
                        icon = TablerIcons.Qrcode,
                        onClick = {
                            onItemClick(ShowQrCode(account))
                        },
                    )
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.open_with),
                        icon = TablerIcons.ExternalLink,
                        onClick = {
                            onItemClick(MenuItem.OpenWith(account))
                        },
                    )
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.reset_opened_count),
                        icon = TablerIcons.Refresh,
                        onClick = {
                            onItemClick(ResetCounter(account))
                        },
                    )
                }

                item {
                    MenuListItem(
                        text = stringResource(R.string.edit),
                        icon = TablerIcons.Edit,
                        onClick = {
                            onItemClick(Edit(account))
                        },
                    )
                }
                item {
                    MenuListItem(
                        text = stringResource(R.string.delete),
                        icon = TablerIcons.Trash,
                        onClick = {
                            onItemClick(Delete(account))
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
                                onItemClick(Edit(account))
                            },
                            icon = null,
                            colors =
                                ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        // Determine max tags to show based on expanded state
                        val maxTagsToShow = if (tagsExpanded) selectedTags?.size ?: 0 else 9
                        val visibleTags = selectedTags?.take(maxTagsToShow) ?: emptyList()
                        val hiddenTagsCount = (selectedTags?.size ?: 0) - visibleTags.size

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            visibleTags.forEach { tag ->
                                val isSelected = selectedTag.any { it.name == tag.trim() }
                                FilterChip(
                                    modifier = Modifier.padding(0.dp),
                                    elevation = null,
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setSelectedTagByName(tag)
                                        showMoreSelectedItem = null
                                    },
                                    label = { Text(tag.trim()) },
                                    shape = RoundedCornerShape(percent = 50),
                                )
                            }
                        }

                        // Show "Load More" or "Show Less" button if there are more than 9 tags
                        if ((selectedTags?.size ?: 0) > 9) {
                            TextButton(
                                onClick = { tagsExpanded = !tagsExpanded },
                                modifier = Modifier.padding(start = 4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text =
                                        if (tagsExpanded) {
                                            stringResource(R.string.show_less_tags)
                                        } else {
                                            stringResource(R.string.load_more_tags, hiddenTagsCount)
                                        },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
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
    listState: ScrollableState,
    accounts: List<DeeprLink>,
    selectedTag: List<DeeprTag>,
    contentPaddingValues: PaddingValues,
    onItemClick: (MenuItem) -> Unit,
    onTagClick: (String) -> Unit,
    isThumbnailEnable: Boolean,
    searchQuery: String,
    favouriteFilter: Int,
    modifier: Modifier = Modifier,
    viewType: @ViewType Int = ViewType.LIST,
) {
    // Determine which empty state to show
    val isSearchActive = searchQuery.isNotBlank()
    val isFavouriteFilterActive = favouriteFilter == 1
    val isTagFilterActive = selectedTag.isNotEmpty()

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
                // Choose appropriate icon and messages based on state
                val (icon, titleRes, descriptionRes) =
                    when {
                        isSearchActive ->
                            Triple(
                                TablerIcons.Search,
                                R.string.no_search_results,
                                R.string.no_search_results_description,
                            )

                        isTagFilterActive ->
                            Triple(
                                TablerIcons.Tag,
                                R.string.no_links_with_tags,
                                R.string.no_links_with_tags_description,
                            )

                        isFavouriteFilterActive ->
                            Triple(
                                TablerIcons.Link,
                                R.string.no_favourites_found,
                                R.string.no_favourites_description,
                            )

                        else ->
                            Triple(
                                TablerIcons.Link,
                                R.string.no_links_saved_yet,
                                R.string.save_your_link_below,
                            )
                    }

                Icon(
                    icon,
                    contentDescription = stringResource(titleRes),
                    modifier =
                        Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(descriptionRes),
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
        when (viewType) {
            ViewType.LIST -> {
                LazyColumn(
                    state = listState as? LazyListState ?: rememberLazyListState(),
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

            ViewType.GRID -> {
                LazyVerticalStaggeredGrid(
                    state =
                        listState as? LazyStaggeredGridState
                            ?: rememberLazyStaggeredGridState(),
                    columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
                    modifier = modifier,
                    contentPadding = contentPaddingValues,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                ) {
                    items(
                        count = accounts.size,
                        key = { index -> accounts[index].id },
                    ) { index ->
                        val account = accounts[index]

                        DeeprItemGrid(
                            modifier = Modifier.animateItem(),
                            account = account,
                            onItemClick = onItemClick,
                        )
                    }
                }
            }

            ViewType.COMPACT -> {
                LazyColumn(
                    state = listState as? LazyListState ?: rememberLazyListState(),
                    modifier = modifier,
                    contentPadding = contentPaddingValues,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        count = accounts.size,
                        key = { index -> accounts[index].id },
                    ) { index ->
                        val account = accounts[index]

                        DeeprItemCompact(
                            modifier = Modifier.animateItem(),
                            account = account,
                            onItemClick = onItemClick,
                            isThumbnailEnable = isThumbnailEnable,
                        )
                    }
                }
            }
        }
    }
}
