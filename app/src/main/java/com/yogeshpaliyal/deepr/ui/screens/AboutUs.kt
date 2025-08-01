package com.yogeshpaliyal.deepr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.R
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.BrandLinkedin
import compose.icons.tablericons.BrandTwitter

data object AboutUs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(backStack: SnapshotStateList<Any>) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(
                title = {
                    Text("About Us")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
                    }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Deepr",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A simple app to save, organize, and launch deeplinks.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Author",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yogesh Choudhary",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val uriHandler = LocalUriHandler.current

                        IconButton(onClick = { uriHandler.openUri("https://twitter.com/yogeshpaliyal") }) {
                            Icon(TablerIcons.BrandTwitter, contentDescription = "Twitter")
                        }
                        IconButton(onClick = { uriHandler.openUri("https://www.linkedin.com/in/yogeshpaliyal/") }) {
                            Icon(TablerIcons.BrandLinkedin, contentDescription = "LinkedIn")
                        }
                        IconButton(onClick = { uriHandler.openUri("https://github.com/yogeshpaliyal") }) {
                            Icon(TablerIcons.BrandGithub, contentDescription = "GitHub")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            val uriHandler = LocalUriHandler.current
            Button(onClick = { uriHandler.openUri("https://github.com/yogeshpaliyal/Deepr") }) {
                Icon(
                    TablerIcons.BrandGithub,
                    contentDescription = "GitHub",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("View on GitHub")
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }

}