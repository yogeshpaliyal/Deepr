package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun DeeprItemCompact(
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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (account.thumbnail.isNotEmpty()) {
                    AsyncImage(
                        model = account.thumbnail,
                        contentDescription = account.name,
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        placeholder = null,
                        error = null,
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (account.name.isNotEmpty()) {
                        Text(
                            text = account.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    Text(
                        text = account.link,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (account.isFavourite == 1L) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = stringResource(R.string.add_to_favourites),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = account.openedCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                dropdownMenu?.invoke()
            }
        }
    }
}
