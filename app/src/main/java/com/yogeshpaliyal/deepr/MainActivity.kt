package com.yogeshpaliyal.deepr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.journeyapps.barcodescanner.ScanOptions
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.ui.screens.ScanQRVirtualScreen
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.ui.screens.home.Dashboard2
import com.yogeshpaliyal.deepr.ui.screens.home.createDeeprObject
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.LanguageUtil
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class SharedLink(
    val url: String,
    val title: String?,
)

class MainActivity : ComponentActivity() {
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        getLinkFromIntent(intent)

        setContent {
            val preferenceDataStore = remember { AppPreferenceDataStore(this) }
            val themeMode by preferenceDataStore.getThemeMode.collectAsStateWithLifecycle(
                initialValue = "system"
            )

            DeeprTheme(themeMode = themeMode) {
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
}

val LocalNavigator =
    compositionLocalOf<TopLevelBackStack<Screen>> { TopLevelBackStack(Dashboard2 {}) }

interface Screen : NavKey {
    @Composable
    fun Content()
}

interface TopLevelRoute : Screen {
    val icon: ImageVector
}


private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(Dashboard2 {}, ScanQRVirtualScreen(), Settings)

class TopLevelBackStack<T : Any>(startKey: T) {

    // Maintain a stack for each top level route
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }


    fun clearStackAndAdd(key: T) {
        topLevelStacks.clear()
        addTopLevel(key)
    }

    fun addTopLevel(key: T) {

        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null) {
            topLevelStacks.put(key, mutableStateListOf(key))
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun getLast() = backStack.last()

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    val backStack = remember { TopLevelBackStack<Screen>(Dashboard2(sharedText = sharedText, resetSharedText = resetSharedText)) }
    val current = backStack.getLast()
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents == null) {
                Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                val normalizedLink = normalizeLink(result.contents)
                if (isValidDeeplink(normalizedLink)) {
                    backStack.add(Dashboard2(mSelectedLink = createDeeprObject(link = normalizedLink), sharedText, resetSharedText))
                } else {
                    Toast.makeText(context, "Invalid deeplink", Toast.LENGTH_SHORT).show()
                }
            }
        }

    CompositionLocalProvider(LocalNavigator provides backStack) {

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    (TOP_LEVEL_ROUTES.any { it::class == current::class }),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomAppBar(scrollBehavior = scrollBehavior) {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                            val isSelected = topLevelRoute::class == backStack.topLevelKey::class
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    when (topLevelRoute) {
                                        is ScanQRVirtualScreen -> qrScanner.launch(ScanOptions())
                                        else -> backStack.addTopLevel(topLevelRoute)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            NavDisplay(
                modifier = Modifier.padding(contentPadding),
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
                    NavEntry(it) {
                        it.Content()
                    }
                }
            )
        }
    }

//
//    Column(modifier = modifier) {
//        NavDisplay(
//            backStack = backStack,
//            entryDecorators =
//                listOf(
//                    // Add the default decorators for managing scenes and saving state
//                    rememberSceneSetupNavEntryDecorator(),
//                    rememberSavedStateNavEntryDecorator(),
//                    // Then add the view model store decorator
//                    rememberViewModelStoreNavEntryDecorator(),
//                ),
//            onBack = { backStack.removeLastOrNull() },
//            entryProvider = { key ->
//                when (key) {
//                    is Home ->
//                        NavEntry(key) {
//                            HomeScreen(
//                                backStack,
//                                sharedText = sharedText,
//                                resetSharedText = resetSharedText,
//                            )
//                        }
//
//                    is Settings ->
//                        NavEntry(key) {
//                            SettingsScreen(backStack)
//                        }
//
//                    is AboutUs ->
//                        NavEntry(key) {
//                            AboutUsScreen(backStack)
//                        }
//
//                    is LocalNetworkServer ->
//                        NavEntry(key) {
//                            LocalNetworkServerScreen(backStack)
//                        }
//
//                    is TransferLinkLocalNetworkServer ->
//                        NavEntry(key) {
//                            TransferLinkLocalServerScreen(backStack)
//                        }
//
//                    is BackupScreen ->
//                        NavEntry(key) {
//                            BackupScreenContent(backStack)
//                        }
//
//                    is RestoreScreen ->
//                        NavEntry(key) {
//                            RestoreScreenContent(backStack)
//                        }
//
//                    else -> NavEntry(Unit) { Text("Unknown route") }
//                }
//            },
//        )
//    }
}
