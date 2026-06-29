package com.yogeshpaliyal.deepr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.ui.BaseScreen
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.Screen
import com.yogeshpaliyal.deepr.ui.TopLevelBackStack
import com.yogeshpaliyal.deepr.ui.TopLevelRoute
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.ui.screens.home.Dashboard2
import com.yogeshpaliyal.deepr.ui.screens.home.TagSelectionScreen
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.LanguageUtil
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.koin.compose.viewmodel.koinActivityViewModel

data class SharedLink(
    val url: String,
    val title: String?,
)

data class ClipboardLink(
    val url: String,
)

class MainActivity : FragmentActivity() {
    val sharingLink = MutableStateFlow<SharedLink?>(null)

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.let { context ->
                try {
                    val preferenceDataStore = AppPreferenceDataStore(context)
                    val languageCode =
                        runBlocking {
                            preferenceDataStore.getLanguageCode.first()
                        }
                    if (languageCode.isNotEmpty()) {
                        LanguageUtil.updateLocale(context, languageCode)
                    } else {
                        context
                    }
                } catch (_: Exception) {
                    context
                }
            },
        )
    }

    /**
     * Initializes the activity, sets up the splash screen, and configures the Compose UI
     * with theme and color mode based on user preferences.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        getLinkFromIntent(intent)

        setContent {
            val isProUser = BuildConfig.APPLICATION_ID.contains(".pro")
            val viewModel: AccountViewModel = koinActivityViewModel()

            // Pro users use per-profile theme, non-pro users use global theme
            val themeMode =
                if (isProUser) {
                    val profileTheme by viewModel.currentProfileTheme.collectAsStateWithLifecycle()
                    profileTheme
                } else {
                    val preferenceDataStore = remember { AppPreferenceDataStore(this) }
                    val globalTheme by preferenceDataStore.getThemeMode.collectAsStateWithLifecycle(
                        initialValue = "system",
                    )
                    globalTheme
                }

            // Pro users use per-profile color theme
            val colorTheme =
                if (isProUser) {
                    val profileColorTheme by viewModel.currentProfileColorTheme.collectAsStateWithLifecycle()
                    profileColorTheme
                } else {
                    "dynamic"
                }

            // Set main profile on launch
            val defaultProfileId by viewModel.defaultProfileId.collectAsStateWithLifecycle()
            val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
            LaunchedEffect(defaultProfileId) {
                if (defaultProfileId != null && defaultProfileId != currentProfile?.id) {
                    viewModel.setSelectedProfile(defaultProfileId!!)
                }
            }

            DeeprTheme(themeMode = themeMode, colorTheme = colorTheme) {
                Surface {
                    val sharedText by sharingLink.collectAsStateWithLifecycle()
                    Dashboard(sharedText = sharedText) {
                        sharingLink.update { null }
                    }
                }
            }
        }
    }

    fun getLinkFromIntent(intent: Intent) {
        // Check if this activity was started via a share intent
        val sharedText =
            when {
                intent.action == Intent.ACTION_SEND -> {
                    if (intent.type == "text/plain") {
                        val link = intent.getStringExtra(Intent.EXTRA_TEXT)
                        val title = intent.getStringExtra(Intent.EXTRA_TITLE)
                        if (link != null) {
                            SharedLink(link, title)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

                else -> null
            }
        sharingLink.update { sharedText }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getLinkFromIntent(intent)
    }

    fun showBiometricPrompt(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {},
    ) {
        val executor =
            androidx.core.content.ContextCompat
                .getMainExecutor(this)
        val biometricPrompt =
            androidx.biometric.BiometricPrompt(
                this,
                executor,
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Authentication failed")
                    }
                },
            )

        val promptInfo =
            androidx.biometric.BiometricPrompt.PromptInfo
                .Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                ).build()

        biometricPrompt.authenticate(promptInfo)
    }
}

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> =
    listOf(Dashboard2(), TagSelectionScreen, Settings)

val LocalSharedText =
    compositionLocalOf<Pair<SharedLink?, () -> Unit>?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    val backStack =
        remember {
            TopLevelBackStack<BaseScreen>(
                Dashboard2(),
            )
        }
    val current = backStack.getLast()
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val viewModel: AccountViewModel = koinActivityViewModel()
    val showProfilesGrid by viewModel.showProfilesGrid.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalSharedText provides Pair(sharedText, resetSharedText)) {
        CompositionLocalProvider(LocalNavigator provides backStack) {
            Scaffold(
                modifier = modifier,
                bottomBar = {
                    AnimatedVisibility(
                        (TOP_LEVEL_ROUTES.any { it::class == current::class }),
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                    ) {
                        BottomAppBar(scrollBehavior = scrollBehavior) {
                            val isPrivateMode by viewModel.isPrivateMode.collectAsStateWithLifecycle()
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                val isSelected =
                                    topLevelRoute::class == backStack.topLevelKey::class
                                val isProfilesTab = topLevelRoute is Dashboard2
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isProfilesTab) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            backStack.addTopLevel(topLevelRoute)
                                        } else {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            if (!isPrivateMode && !showProfilesGrid) {
                                                viewModel.setShowProfilesGrid(true)
                                            }
                                            backStack.addTopLevel(topLevelRoute)
                                        }
                                    },
                                    label = {
                                        Text(stringResource(topLevelRoute.label))
                                    },
                                    icon = {
                                        val boxModifier =
                                            if (isProfilesTab) {
                                                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                                                Modifier.combinedClickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                                        if (!isPrivateMode && !showProfilesGrid) {
                                                            viewModel.setShowProfilesGrid(true)
                                                        }
                                                        backStack.addTopLevel(topLevelRoute)
                                                    },
                                                    onLongClick = {
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        if (isPrivateMode) {
                                                            viewModel.setPrivateMode(false)
                                                        } else {
                                                            val mainActivity = context as? MainActivity
                                                            mainActivity?.showBiometricPrompt(
                                                                title = mainActivity.getString(R.string.unlock_private_links),
                                                                subtitle = mainActivity.getString(R.string.unlock_private_links_desc),
                                                                onSuccess = {
                                                                    viewModel.setPrivateMode(true)
                                                                },
                                                            )
                                                        }
                                                    },
                                                )
                                            } else {
                                                Modifier
                                            }

                                        androidx.compose.foundation.layout.Box(modifier = boxModifier) {
                                            Icon(
                                                imageVector = topLevelRoute.icon,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            ) { contentPadding ->
                NavDisplay(
                    backStack = backStack.backStack,
                    entryDecorators =
                        listOf(
                            // Add the default decorators for managing scenes and saving state
                            rememberSceneSetupNavEntryDecorator(),
                            rememberSavedStateNavEntryDecorator(),
                            // Then add the view model store decorator
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    onBack = {
                        backStack.removeLast()
                    },
                    entryProvider = {
                        NavEntry(it) { entryItem ->
                            if (entryItem is TopLevelRoute) {
                                entryItem.Content(
                                    WindowInsets(
                                        left = contentPadding.calculateLeftPadding(layoutDirection),
                                        right = contentPadding.calculateRightPadding(layoutDirection),
                                        top = contentPadding.calculateTopPadding(),
                                        bottom = contentPadding.calculateBottomPadding(),
                                    ),
                                )
                            } else if (entryItem is Screen) {
                                entryItem.Content()
                            }
                        }
                    },
                )
            }
        }
    }
}
