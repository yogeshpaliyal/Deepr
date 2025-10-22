package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeeprItemGrid(
    account: GetLinksAndTags,
    onItemClick: (MenuItem) -> Unit,
    onTagClick: (tag: String) -> Unit,
    selectedTag: List<Tags>,
    modifier: Modifier = Modifier,
    dropdownMenu: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val linkCopied = stringResource(R.string.link_copied)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onItemClick(MenuItem.Click(account)) },
                    onLongClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(linkCopied, account.link)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, linkCopied, Toast.LENGTH_SHORT).show()
                    },
                ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                if (account.thumbnail.isNotEmpty()) {
                    AsyncImage(
                        model = account.thumbnail,
                        contentDescription = account.name,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        placeholder = null,
                        error = null,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = account.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Favorite indicator overlay
                if (account.isFavourite == 1L) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = stringResource(R.string.add_to_favourites),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp),
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
            ) {
                if (account.name.isNotEmpty()) {
                    Text(
                        text = account.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = account.link,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.opened_count, account.openedCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    dropdownMenu?.invoke()
                }
            }
        }
    }
}
