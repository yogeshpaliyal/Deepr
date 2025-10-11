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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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
fun AboutUsScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.about_us))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
                    }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.back),
                            modifier = if (isRtl) Modifier.scale(-1f, 1f) else Modifier,
                        )
                    }
                },
            )
        }
    }) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(R.string.app_logo),
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.author),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.author_name),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val uriHandler = LocalUriHandler.current

                        IconButton(onClick = { uriHandler.openUri("https://twitter.com/yogeshpaliyal") }) {
                            Icon(TablerIcons.BrandTwitter, contentDescription = stringResource(R.string.twitter))
                        }
                        IconButton(onClick = { uriHandler.openUri("https://www.linkedin.com/in/yogeshpaliyal/") }) {
                            Icon(TablerIcons.BrandLinkedin, contentDescription = stringResource(R.string.linkedin))
                        }
                        IconButton(onClick = { uriHandler.openUri("https://github.com/yogeshpaliyal") }) {
                            Icon(TablerIcons.BrandGithub, contentDescription = stringResource(R.string.github))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            val uriHandler = LocalUriHandler.current
            Button(onClick = { uriHandler.openUri("https://github.com/yogeshpaliyal/Deepr") }) {
                Icon(
                    TablerIcons.BrandGithub,
                    contentDescription = stringResource(R.string.github),
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.view_on_github))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
