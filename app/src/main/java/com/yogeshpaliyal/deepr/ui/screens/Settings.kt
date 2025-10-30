package com.yogeshpaliyal.deepr.ui.screens

import android.content.Intent
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.MainActivity
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.components.LanguageSelectionDialog
import com.yogeshpaliyal.deepr.ui.components.ServerStatusBar
import com.yogeshpaliyal.deepr.ui.components.SettingsItem
import com.yogeshpaliyal.deepr.ui.components.SettingsSection
import com.yogeshpaliyal.deepr.util.LanguageUtil
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Download
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Language
import compose.icons.tablericons.Photo
import compose.icons.tablericons.Server
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Share
import compose.icons.tablericons.Star
import compose.icons.tablericons.Upload
import org.koin.androidx.compose.koinViewModel

data object Settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    // Collect the shortcut icon preference state
    val useLinkBasedIcons by viewModel.useLinkBasedIcons.collectAsStateWithLifecycle()

    // Collect language preference state
    val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Collect default page preference state
    val defaultPageFavourites by viewModel.defaultPageFavouritesEnabled.collectAsStateWithLifecycle()
    val isThumbnailEnable by viewModel.isThumbnailEnable.collectAsStateWithLifecycle()

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
            SettingsSection("Data Management") {
                SettingsItem(
                    TablerIcons.Upload,
                    title = stringResource(R.string.backup),
                    description = "Export to CSV, Local file sync, Auto backup",
                    onClick = {
                        backStack.add(BackupScreen)
                    },
                )

                SettingsItem(
                    TablerIcons.Download,
                    title = stringResource(R.string.restore),
                    description = "Import from CSV, Bookmarks, and other formats",
                    onClick = {
                        backStack.add(RestoreScreen)
                    },
                )
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
                    TablerIcons.Star,
                    title = stringResource(R.string.default_page),
                    description =
                        if (defaultPageFavourites) {
                            stringResource(R.string.default_page_favourites)
                        } else {
                            stringResource(R.string.default_page_all)
                        },
                    onClick = {
                        viewModel.setDefaultPageFavourites(!defaultPageFavourites)
                    },
                    trailing = {
                        Switch(
                            checked = defaultPageFavourites,
                            onCheckedChange = { viewModel.setDefaultPageFavourites(it) },
                        )
                    },
                )

                SettingsItem(
                    TablerIcons.Photo,
                    title = "Show thumbnails for links",
                    description = "If enabled, thumbnails will be displayed for saved links where available",
                    onClick = {
                        viewModel.setIsThumbnailEnable(!isThumbnailEnable)
                    },
                    trailing = {
                        Switch(
                            checked = isThumbnailEnable,
                            onCheckedChange = { viewModel.setIsThumbnailEnable(it) },
                        )
                    },
                )
            }

            SettingsSection("About") {
                val appName = stringResource(R.string.app_name)
                // Report a problem or feedback via email on yogeshpaliyal.foss+shelfy@gmail.com
                SettingsItem(
                    icon = TablerIcons.AlertTriangle,
                    title = "Report a Problem / Feedback",
                    shouldShowLoading = false,
                    description = "Help us improve by reporting issues or sharing feedback",
                    onClick = {
                        val emailIntent =
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:".toUri()
                                putExtra(
                                    Intent.EXTRA_EMAIL,
                                    arrayOf("yogeshpaliyal.foss+deepr@gmail.com"),
                                )
                                putExtra(Intent.EXTRA_SUBJECT, "$appName App Feedback/Issue")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Describe your issue or feedback here:\n App Version : ${BuildConfig.VERSION_NAME}\n",
                                )
                            }
                        context.startActivity(
                            Intent.createChooser(
                                emailIntent,
                                "Send email via...",
                            ),
                        )
                    },
                )

                // Rate and review open playstore link
                SettingsItem(
                    icon = TablerIcons.Star,
                    title = "Rate & Review",
                    description = "Rate us on the Play Store",
                    shouldShowLoading = false,
                    onClick = {
                        viewModel.requestReview(context as MainActivity)
                    },
                )

                // Create share app item
                SettingsItem(
                    icon = TablerIcons.Share,
                    title = "Share $appName",
                    shouldShowLoading = false,
                    description = "Share $appName with your friends",
                    onClick = {
                        val shareIntent =
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out $appName - Your link organizer and Read Later App! Download it from https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}",
                                )
                                type = "text/plain"
                            }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share via",
                            ),
                        )
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
                    (context as? MainActivity)?.recreate()
                },
                onDismiss = { showLanguageDialog = false },
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
                            onClick { isLoading = false }
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
