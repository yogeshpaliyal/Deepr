package com.yogeshpaliyal.deepr.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.viewmodel.AppUpdateViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.CloudDownload
import compose.icons.tablericons.X
import org.koin.androidx.compose.koinViewModel

/**
 * Home screen banner shown when a newer version is available. Dismissing it hides the
 * banner until a different (newer) version is released.
 */
@Composable
fun AppUpdateBanner(
    modifier: Modifier = Modifier,
    viewModel: AppUpdateViewModel = koinViewModel(),
) {
    val activity = LocalContext.current as? Activity
    val bannerUpdate by viewModel.bannerUpdate.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(activity) {
        activity?.let { viewModel.checkForBannerUpdate(it) }
    }

    AppUpdateMessages(viewModel.messages)

    AnimatedVisibility(
        visible = bannerUpdate != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        bannerUpdate?.let { update ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = TablerIcons.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.update_available),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text =
                                update.versionName?.let {
                                    stringResource(R.string.update_available_version, it)
                                } ?: stringResource(R.string.update_available_generic),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    FilledTonalButton(
                        onClick = viewModel::showBannerUpdate,
                        colors =
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(stringResource(R.string.update_now))
                    }

                    IconButton(onClick = viewModel::dismissBanner) {
                        Icon(
                            imageVector = TablerIcons.X,
                            contentDescription = stringResource(R.string.dismiss_update_banner),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
        }
    }

    AppUpdateDialogs(
        state = state,
        onStartUpdate = { update -> activity?.let { viewModel.startUpdate(it, update) } },
        onDismiss = viewModel::dismiss,
        onCancelDownload = viewModel::cancelUpdate,
    )
}
