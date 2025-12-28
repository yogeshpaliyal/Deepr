package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.ui.getDeeprItemTextColor
import compose.icons.TablerIcons
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Tag

@Composable
fun OpenCountAndTags(
    account: GetLinksAndTags,
    modifier: Modifier = Modifier,
    showOpenCounter: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (showOpenCounter) {
            Row {
                Icon(
                    TablerIcons.ExternalLink,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = account.openedCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = getDeeprItemTextColor(account.isFavourite),
                )
            }
        }
        account.tagsIds?.split(",")?.size?.let { tagsCount ->
            if (tagsCount > 0) {
                if (showOpenCounter) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = getDeeprItemTextColor(account.isFavourite),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Icon(
                        TablerIcons.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tagsCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = getDeeprItemTextColor(account.isFavourite),
                    )
                }
            }
        }
    }
}
