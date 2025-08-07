package com.yogeshpaliyal.deepr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.yogeshpaliyal.deepr.ui.screens.AboutUs
import com.yogeshpaliyal.deepr.ui.screens.AboutUsScreen
import com.yogeshpaliyal.deepr.ui.screens.Home
import com.yogeshpaliyal.deepr.ui.screens.HomeScreen
import com.yogeshpaliyal.deepr.ui.screens.Settings
import com.yogeshpaliyal.deepr.ui.screens.SettingsScreen
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeeprTheme {
                Surface {
                    Dashboard(viewModel)
                }
            }
        }
    }
}

@Composable
fun Dashboard(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = remember { mutableStateListOf<Any>(Home) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Home ->
                    NavEntry(key) {
                        HomeScreen(viewModel, backStack)
                    }

                is Settings ->
                    NavEntry(key) {
                        SettingsScreen(viewModel, backStack)
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
