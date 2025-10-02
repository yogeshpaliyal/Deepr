package com.yogeshpaliyal.deepr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.ui.screens.AboutUs
import com.yogeshpaliyal.deepr.ui.screens.AboutUsScreen
import com.yogeshpaliyal.deepr.ui.screens.LocalNetworkServer
import com.yogeshpaliyal.deepr.ui.screens.LocalNetworkServerScreen
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.ui.screens.SettingsScreen
import com.yogeshpaliyal.deepr.ui.screens.home.Home
import com.yogeshpaliyal.deepr.ui.screens.home.HomeScreen
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.LanguageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
            DeeprTheme {
                Surface {
                    val sharedText by sharingLink.collectAsStateWithLifecycle()
                    Dashboard(sharedText = sharedText) {
                        sharingLink.value = null
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
        sharingLink.value = sharedText
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getLinkFromIntent(intent)
    }
}

@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    sharedText: SharedLink? = null,
    resetSharedText: () -> Unit,
) {
    val backStack = remember(sharedText) { mutableStateListOf<Any>(Home) }

    Column(modifier = modifier) {
        NavDisplay(
            backStack = backStack,
            entryDecorators =
                listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    // Then add the view model store decorator
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is Home ->
                        NavEntry(key) {
                            HomeScreen(
                                backStack,
                                sharedText = sharedText,
                                resetSharedText = resetSharedText,
                            )
                        }

                    is Settings ->
                        NavEntry(key) {
                            SettingsScreen(backStack)
                        }

                    is AboutUs ->
                        NavEntry(key) {
                            AboutUsScreen(backStack)
                        }

                    is LocalNetworkServer ->
                        NavEntry(key) {
                            LocalNetworkServerScreen(backStack)
                        }

                    else -> NavEntry(Unit) { Text("Unknown route") }
                }
            },
        )
    }
}
