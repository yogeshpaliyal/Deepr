package com.yogeshpaliyal.deepr.ui.screens.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun DeeprItemGrid(
    account: GetLinksAndTags,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    isThumbnailEnable: Boolean = true,
    showOpenCounter: Boolean = true,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box {
                    if (account.thumbnail.isNotEmpty() && isThumbnailEnable) {
                        AsyncImage(
                            model = account.thumbnail,
                            contentDescription = account.name,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.91f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            placeholder = null,
                            error = null,
                            contentScale = ContentScale.Crop,
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
                            style = MaterialTheme.typography.titleSmall,
                            color = getDeeprItemTextColor(account.isFavourite),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = account.link,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = getDeeprItemTextColor(account.isFavourite),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OpenCountAndTags(account, Modifier.weight(1f), showOpenCounter)
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
}
