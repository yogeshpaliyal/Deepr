package com.yogeshpaliyal.deepr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.yogeshpaliyal.deepr.ui.screens.AboutUs
import com.yogeshpaliyal.deepr.ui.screens.AboutUsScreen
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.ui.screens.SettingsScreen
import com.yogeshpaliyal.deepr.ui.screens.home.Home
import com.yogeshpaliyal.deepr.ui.screens.home.HomeScreen
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    val sharingLink = MutableStateFlow<String?>(null)

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
                        intent.getStringExtra(Intent.EXTRA_TEXT)
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
    sharedText: String? = null,
    resetSharedText: () -> Unit,
) {
    val backStack = remember(sharedText) { mutableStateListOf<Any>(Home) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
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

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        },
    )
}
