package com.yogeshpaliyal.deepr.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import compose.icons.TablerIcons
import compose.icons.tablericons.Copy
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Trash
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeeprItem(
    account: GetLinksAndTags,
    selectedTag: Tags?,
    modifier: Modifier = Modifier,
    onItemClick: ((GetLinksAndTags) -> Unit)? = null,
    onRemoveClick: ((GetLinksAndTags) -> Unit)? = null,
    onShortcutClick: ((GetLinksAndTags) -> Unit)? = null,
    onQrCodeClick: ((GetLinksAndTags) -> Unit)? = null,
    onEditClick: ((GetLinksAndTags) -> Unit)? = null,
    onItemLongClick: ((GetLinksAndTags) -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectedTags =
        remember(account.tagsNames) { account.tagsNames?.split(",")?.toMutableList() }

    // Extract string resources at Composable level
    val copyLinkText = stringResource(R.string.copy_link)
    val linkCopiedText = stringResource(R.string.link_copied)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onItemClick?.invoke(account) },
                    onLongClick = { onItemLongClick?.invoke(account) },
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
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.opened_count, account.openedCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(TablerIcons.DotsVertical, contentDescription = stringResource(R.string.more_options))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(copyLinkText) },
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(linkCopiedText, account.link)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, linkCopiedText, Toast.LENGTH_SHORT).show()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    TablerIcons.Copy,
                                    contentDescription = copyLinkText,
                                )
                            },
                        )
                        // Display last opened time
                        account.lastOpenedAt?.let { lastOpened ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.last_opened, formatDateTime(lastOpened)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                onClick = { },
                                enabled = false,
                            )
                        } ?: run {
                            if (account.openedCount > 0) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.never_opened),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    onClick = { },
                                    enabled = false,
                                )
                            }
                        }
                        ShortcutMenuItem(account, {
                            onShortcutClick?.invoke(it)
                            expanded = false
                        })
                        ShowQRCodeMenuItem(account, {
                            onQrCodeClick?.invoke(it)
                            expanded = false
                        })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            onClick = {
                                onEditClick?.invoke(account)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    TablerIcons.Edit,
                                    contentDescription = stringResource(R.string.edit),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                onRemoveClick?.invoke(account)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    TablerIcons.Trash,
                                    contentDescription = stringResource(R.string.delete),
                                )
                            },
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                selectedTags?.forEach { tag ->
                    FilterChip(
                        modifier = Modifier.padding(0.dp),
                        elevation = null,
                        selected = selectedTag?.name == tag.trim(),
                        onClick = { onTagClick?.invoke(tag.trim()) },
                        label = { Text(tag.trim()) },
                    )
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
