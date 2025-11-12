package com.yogeshpaliyal.deepr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yogeshpaliyal.deepr.LocalNavigator
import com.yogeshpaliyal.deepr.Screen
import com.yogeshpaliyal.deepr.ui.screens.home.Dashboard2
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object Splash : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        LaunchedEffect(Unit) {
//            // Check if user is opening the app for the first time
//            val isFirstTime = context.isFirstTimeUser()
//
//            // Navigate to appropriate screen
//            if (isFirstTime) {
//                navigator.add(IntroScreen)
//            } else {
            navigator.clearStackAndAdd(Dashboard2 {})
//            }
        }

        // Loading screen while checking user status
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ContainedLoadingIndicator()
        }
    }
}
