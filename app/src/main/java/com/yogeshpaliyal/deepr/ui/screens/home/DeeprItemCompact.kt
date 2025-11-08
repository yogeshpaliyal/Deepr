package com.yogeshpaliyal.deepr.ui.screens.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.getDeeprItemBackgroundColor
import com.yogeshpaliyal.deepr.ui.getDeeprItemTextColor
import compose.icons.TablerIcons
import compose.icons.tablericons.DotsVertical

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeeprItemCompact(
    account: GetLinksAndTags,
    onItemClick: (MenuItem) -> Unit,
    isThumbnailEnable: Boolean,
    modifier: Modifier = Modifier,
) {
    DeeprItemSwipable(account, onItemClick, modifier) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = getDeeprItemBackgroundColor(account.isFavourite),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onItemClick(MenuItem.Click(account)) },
                        onLongClick = {
                            onItemClick(MenuItem.Copy(account))
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
                    if (account.thumbnail.isNotEmpty() && isThumbnailEnable) {
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
                                color = getDeeprItemTextColor(account.isFavourite),
                            )
                        }
                        Text(
                            text = account.link,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = getDeeprItemTextColor(account.isFavourite),
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.opened_count, account.openedCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = getDeeprItemTextColor(account.isFavourite),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            account.tagsIds?.split(",")?.size?.let { tagsCount ->
                                if (tagsCount > 0) {
                                    Text(
                                        text = stringResource(R.string.number_tags, tagsCount),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = getDeeprItemTextColor(account.isFavourite),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        onItemClick(MenuItem.MoreOptionsBottomSheet(account))
                    }) {
                        Icon(
                            imageVector = TablerIcons.DotsVertical,
                            contentDescription = stringResource(R.string.more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
