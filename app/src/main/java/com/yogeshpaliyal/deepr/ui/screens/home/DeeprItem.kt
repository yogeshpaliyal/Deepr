package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import compose.icons.TablerIcons
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Note
import compose.icons.tablericons.Refresh
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

sealed class MenuItem(
    val item: GetLinksAndTags,
) {
    class Click(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class OpenWith(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class Shortcut(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class ShowQrCode(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class FavouriteClick(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class Edit(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class ResetCounter(
        item: GetLinksAndTags,
    ) : MenuItem(item)

    class Delete(
        item: GetLinksAndTags,
    ) : MenuItem(item)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeeprItem(
    account: GetLinksAndTags,
    onItemClick: (MenuItem) -> Unit,
    onTagClick: (tag: String) -> Unit,
    selectedTag: List<Tags>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<String?>(null) }
    var tagsExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectedTags =
        remember(account.tagsNames) { account.tagsNames?.split(",")?.toMutableList() }

    val linkCopied = stringResource(R.string.link_copied)

    selectedNote?.let {
        AlertDialog(
            {
                selectedNote = null
            },
            title = {
                Text("Note")
            },
            text = {
                Text(it)
            },
            confirmButton = {
                OutlinedButton({
                    selectedNote = null
                }) {
                    Text("Okay")
                }
            },
        )
    }

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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (account.name.isNotEmpty()) {
                        Text(
                            text = account.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Text(
                        text = account.link,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatDateTime(account.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        IconButton(onClick = {
                            onItemClick(MenuItem.FavouriteClick(account))
                        }) {
                            Icon(
                                imageVector =
                                    if (account.isFavourite == 1L) {
                                        Icons.Rounded.Star
                                    } else {
                                        Icons.Rounded.StarBorder
                                    },
                                contentDescription =
                                    if (account.isFavourite == 1L) {
                                        stringResource(R.string.remove_from_favourites)
                                    } else {
                                        stringResource(R.string.add_to_favourites)
                                    },
                                tint =
                                    if (account.isFavourite == 1L) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                TablerIcons.DotsVertical,
                                contentDescription = stringResource(R.string.more_options),
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            if (account.notes.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.view_note)) },
                                    onClick = {
                                        expanded = false
                                        selectedNote = account.notes
                                    },
                                    leadingIcon = {
                                        Icon(
                                            TablerIcons.Note,
                                            contentDescription = stringResource(R.string.view_note),
                                        )
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.open_with)) },
                                onClick = {
                                    onItemClick(MenuItem.OpenWith(account))
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        TablerIcons.ExternalLink,
                                        contentDescription = stringResource(R.string.open_with),
                                    )
                                },
                            )

                            // Display last opened time
                            if (account.lastOpenedAt != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(
                                                R.string.last_opened,
                                                formatDateTime(account.lastOpenedAt),
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    onClick = { },
                                    enabled = false,
                                )
                            }
                            ShortcutMenuItem(account, {
                                onItemClick(MenuItem.Shortcut(it))
                                expanded = false
                            })
                            ShowQRCodeMenuItem(account, {
                                onItemClick(MenuItem.ShowQrCode(it))
                                expanded = false
                            })
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (account.isFavourite == 1L) {
                                            stringResource(R.string.remove_from_favourites)
                                        } else {
                                            stringResource(R.string.add_to_favourites)
                                        },
                                    )
                                },
                                onClick = {
                                    onItemClick(MenuItem.FavouriteClick(account))
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            if (account.isFavourite == 1L) {
                                                Icons.Rounded.Star
                                            } else {
                                                Icons.Rounded.StarBorder
                                            },
                                        contentDescription =
                                            if (account.isFavourite == 1L) {
                                                stringResource(R.string.remove_from_favourites)
                                            } else {
                                                stringResource(R.string.add_to_favourites)
                                            },
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reset_opened_count)) },
                                onClick = {
                                    onItemClick(MenuItem.ResetCounter(account))
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        TablerIcons.Refresh,
                                        contentDescription = stringResource(R.string.reset_opened_count),
                                    )
                                },
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.opened_count, account.openedCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column {
                // Determine max tags to show based on expanded state
                val maxTagsToShow = if (tagsExpanded) selectedTags?.size ?: 0 else 9
                val visibleTags = selectedTags?.take(maxTagsToShow) ?: emptyList()
                val hiddenTagsCount = (selectedTags?.size ?: 0) - visibleTags.size

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    visibleTags.forEach { tag ->
                        val isSelected = selectedTag.any { it.name == tag.trim() }
                        FilterChip(
                            modifier = Modifier.padding(0.dp),
                            elevation = null,
                            selected = isSelected,
                            onClick = { onTagClick(tag.trim()) },
                            label = { Text(tag.trim()) },
                        )
                    }
                }

                // Show "Load More" or "Show Less" button if there are more than 9 tags
                if ((selectedTags?.size ?: 0) > 9) {
                    androidx.compose.material3.TextButton(
                        onClick = { tagsExpanded = !tagsExpanded },
                        modifier = Modifier.padding(start = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text =
                                if (tagsExpanded) {
                                    stringResource(R.string.show_less_tags)
                                } else {
                                    stringResource(R.string.load_more_tags, hiddenTagsCount)
                                },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    try {
        val dbFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dbFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val date = dbFormatter.parse(dateTimeString)
        val displayFormatter =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        return date?.let { displayFormatter.format(it) } ?: dateTimeString
    } catch (_: Exception) {
        return dateTimeString // fallback to raw string
    }
}
