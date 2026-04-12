package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.ripple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.LocalClipboardLink
import com.yogeshpaliyal.deepr.LocalSharedText
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.SharedLink
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.analytics.AnalyticsEvents
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.analytics.AnalyticsParams
import com.yogeshpaliyal.deepr.ui.AddLinkScreen
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.deepr.ui.components.ClearInputIconButton
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.components.DeleteConfirmationDialog
import com.yogeshpaliyal.deepr.ui.components.NoteViewDialog
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
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Share
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.Shortcut
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.ShowQrCode
import com.yogeshpaliyal.deepr.ui.screens.home.MenuItem.ViewNote
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplinkExternal
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ArrowsSort
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Check
import compose.icons.tablericons.Edit
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Link
import compose.icons.tablericons.Note
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Search
import compose.icons.tablericons.Share
import compose.icons.tablericons.Tag
import compose.icons.tablericons.Trash
import compose.icons.tablericons.User
import compose.icons.tablericons.ArrowsSort
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

data object Home

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
class Dashboard2(
    val mSelectedLink: GetLinksAndTags? = null,
) : TopLevelRoute {
    override val icon: ImageVector
        get() = TablerIcons.User
    override val label: Int
        get() = R.string.profiles

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
    ExperimentalFoundationApi::class,
)
@Composable
fun HomeScreen(
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    deeprQueries: DeeprQueries = koinInject(),
    analyticsManager: AnalyticsManager = koinInject(),
    mSelectedLink: GetLinksAndTags? = null,
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    val viewModel: AccountViewModel = koinActivityViewModel()
    val currentViewType by viewModel.viewType.collectAsStateWithLifecycle()
    val localNavigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    val tags = viewModel.allTagsWithCount.collectAsStateWithLifecycle()

    val showProfilesGrid by viewModel.showProfilesGrid.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()

    var selectedLink by remember { mutableStateOf<GetLinksAndTags?>(mSelectedLink) }
    val selectedTag by viewModel.selectedTagFilter.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState(blurEnabled = true)
    val context = LocalContext.current
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    // Clipboard link detection
    val clipboardLinkState = LocalClipboardLink.current
    val clipboardLink = clipboardLinkState?.first
    val resetClipboardLink = clipboardLinkState?.second
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

    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var profileToManage by remember { mutableStateOf<com.yogeshpaliyal.deepr.Profile?>(null) }
    var isReordering by remember { mutableStateOf(false) }

    BackHandler(enabled = isReordering || profileToManage != null || !showProfilesGrid || selectedTag.isNotEmpty() || searchBarState.currentValue == SearchBarValue.Expanded) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        if (isReordering) {
            isReordering = false
        } else if (profileToManage != null) {
            profileToManage = null
        } else if (searchBarState.currentValue == SearchBarValue.Expanded) {
            scope.launch {
                searchBarState.animateToCollapsed()
            }
        } else if (!showProfilesGrid) {
            viewModel.setShowProfilesGrid(true)
        } else if (selectedTag.isNotEmpty()) {
            viewModel.setTagFilter(null)
        }
    }

    // Handle shared text from other apps
    LaunchedEffect(sharedText, resetSharedText) {
        if (!sharedText?.url.isNullOrBlank()) {
            val normalizedLink = normalizeLink(sharedText.url)
            if (isValidDeeplink(normalizedLink)) {
                selectedLink =
                    createDeeprObject(link = normalizedLink, name = sharedText.title ?: "")
            } else {
                Toast
                    .makeText(context, context.getString(R.string.invalid_shared_link), Toast.LENGTH_SHORT)
                    .show()
            }
            // Reset shared text even on error to prevent stuck state
            resetSharedText()
        }
    }

    LaunchedEffect(selectedTag, tags.value) {
        // Get unique tags by merging both but first items should be selected tag and then tags
        val allTagsList = tags.value
        val mergedList = mutableListOf<FilterTagItem>()
        val mapOfSelectedList = HashMap<String, Long>()

        val alreadyAdded = HashSet<String>()

        allTagsList.forEach { tag ->
            mapOfSelectedList.put(tag.name, tag.linkCount)
        }

        selectedTag.forEach { tag ->
            alreadyAdded.add(tag.name)
            val count = mapOfSelectedList[tag.name] ?: 0L
            mergedList.add(FilterTagItem(tag.name, count, true))
        }

        allTagsList.forEach { tag ->
            if (alreadyAdded.contains(tag.name).not()) {
                alreadyAdded.add(tag.name)
                mergedList.add(FilterTagItem(tag.name, tag.linkCount, false))
            }
        }
        finalTagsInfo = mergedList
    }

    LaunchedEffect(textFieldState.text) {
        viewModel.search(textFieldState.text.toString())
    }

    // Scroll to top when filters change
    LaunchedEffect(favouriteFilter, selectedTag) {
        try {
            when (listState) {
                is LazyStaggeredGridState -> listState.scrollToItem(0)
                is LazyListState -> listState.scrollToItem(0)
            }
        } catch (e: Exception) {
            // Ignore scroll errors that may occur if list is not laid out yet
        }
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
                                tooltip = { PlainTooltip { Text(stringResource(R.string.view_type)) } },
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

    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        contentWindowInsets = windowInsets,
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showProfilesGrid) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.profiles),
                            )
                            if (allProfiles.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(
                                        text = "${allProfiles.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp,
                                            ),
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (allProfiles.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    isReordering = !isReordering
                                },
                            ) {
                                Icon(
                                    imageVector = if (isReordering) TablerIcons.Check else TablerIcons.ArrowsSort,
                                    contentDescription = if (isReordering) "Finish Reordering" else stringResource(R.string.reorder),
                                    tint = if (isReordering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
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
                        if (textFieldState.text.isEmpty()) {
                            item {
                                FilterChip(selectedTag.isEmpty() && favouriteFilter == -1, {
                                    viewModel.setFavouriteFilter(-1)
                                    viewModel.setTagFilter(null)
                                }, label = {
                                    Text(stringResource(R.string.all) + " (${totalLinks ?: 0})")
                                }, modifier = Modifier.animateItem(), shape = RoundedCornerShape(percent = 50))
                            }
                            item {
                                FilterChip(selectedTag.isEmpty() && favouriteFilter == 1, {
                                    viewModel.setFavouriteFilter(1)
                                    viewModel.setTagFilter(null)
                                }, label = {
                                    Text(stringResource(R.string.favourites) + " (${favouriteLinks ?: 0})")
                                }, modifier = Modifier.animateItem(), shape = RoundedCornerShape(percent = 50))
                            }
                        }

                        items(finalTagsInfo ?: listOf()) {
                            FilterChip(it.isSelected, {
                                viewModel.setSelectedTagByName(it.name)
                            }, label = {
                                Text(it.name + " (${it.count})")
                            }, modifier = Modifier.animateItem(), shape = RoundedCornerShape(percent = 50))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showProfilesGrid) {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            localNavigator.add(AddLinkScreen(createDeeprObject()))
                                        },
                                        onLongPress = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            var linkToPass = clipboardLink?.url ?: ""
                                            
                                            // Fallback: try to read directly from clipboard manager if state is empty
                                            if (linkToPass.isBlank()) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData = clipboard.primaryClip
                                                if (clipData != null && clipData.itemCount > 0) {
                                                    val text = clipData.getItemAt(0).text?.toString()
                                                    if (!text.isNullOrBlank()) {
                                                        val normalized = normalizeLink(text)
                                                        if (isValidDeeplink(normalized)) {
                                                            linkToPass = normalized
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            localNavigator.add(AddLinkScreen(createDeeprObject(link = linkToPass)))
                                        },
                                    )
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            TablerIcons.Plus,
                            contentDescription = stringResource(R.string.add_link),
                        )
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        showCreateProfileDialog = true
                    },
                ) {
                    Icon(
                        TablerIcons.Plus,
                        contentDescription = stringResource(R.string.create_profile),
                    )
                }
            }
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            val layoutDirection = LocalLayoutDirection.current
            if (showProfilesGrid) {
                ProfilesGrid(
                    profiles = allProfiles,
                    isReordering = isReordering,
                    currentProfileId =
                        viewModel.currentProfile
                            .collectAsStateWithLifecycle()
                            .value
                            ?.id ?: -1L,
                    contentPaddingValues =
                        PaddingValues(
                            start = contentPadding.calculateLeftPadding(layoutDirection) + 16.dp,
                            end = contentPadding.calculateRightPadding(layoutDirection) + 16.dp,
                            top = contentPadding.calculateTopPadding() + 16.dp,
                            bottom = contentPadding.calculateBottomPadding() + 16.dp,
                        ),
                    onProfileClick = {
                        if (!isReordering) {
                            viewModel.setSelectedProfile(it.id)
                            viewModel.setShowProfilesGrid(false)
                        }
                    },
                    onProfileLongClick = {
                        if (!isReordering) {
                            profileToManage = it
                        }
                    },
                    onMoveUp = {
                        viewModel.moveProfileUp(it.id)
                    },
                    onMoveDown = {
                        viewModel.moveProfileDown(it.id)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
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
                        localNavigator.add(AddLinkScreen(it))
                    },
                )
            }
        }

        selectedLink?.let {
            localNavigator.add(AddLinkScreen(it))
            selectedLink = null
        }

        profileToManage?.let { profile ->
            RenameDeleteProfileDialog(
                profile = profile,
                onDismiss = { profileToManage = null },
                viewModel = viewModel,
                allProfiles = allProfiles,
            )
        }
    }

    if (showCreateProfileDialog) {
        var newProfileName by remember { mutableStateOf("") }
        var profileCreationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showCreateProfileDialog = false
                profileCreationError = null
            },
            title = {
                Text(stringResource(R.string.create_profile))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = {
                            newProfileName = it
                            profileCreationError = null
                        },
                        label = { Text(stringResource(R.string.profile_name)) },
                        singleLine = true,
                        isError = profileCreationError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    profileCreationError?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedProfileName = newProfileName.trim()
                        if (trimmedProfileName.isBlank()) {
                            profileCreationError = context.getString(R.string.profile_name_cannot_be_blank)
                            return@TextButton
                        }

                        val existingProfile =
                            allProfiles.find {
                                it.name.equals(trimmedProfileName, ignoreCase = true)
                            }

                        if (existingProfile != null) {
                            profileCreationError = context.getString(R.string.profile_name_exists)
                        } else {
                            viewModel.insertProfile(trimmedProfileName)
                            showCreateProfileDialog = false
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.profile_created_successfully),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                    enabled = newProfileName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.create_profile))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateProfileDialog = false
                        profileCreationError = null
                    },
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

