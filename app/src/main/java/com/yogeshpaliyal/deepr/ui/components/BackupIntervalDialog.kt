package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.R

data class BackupInterval(
    val intervalMillis: Long,
    val nameResId: Int,
)

@Composable
fun BackupIntervalDialog(
    currentInterval: Long,
    onIntervalSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val intervals =
        listOf(
            BackupInterval(3600000L, R.string.interval_1_hour),
            BackupInterval(21600000L, R.string.interval_6_hours),
            BackupInterval(43200000L, R.string.interval_12_hours),
            BackupInterval(86400000L, R.string.interval_24_hours),
            BackupInterval(604800000L, R.string.interval_7_days),
        )

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.backup_interval_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                intervals.forEach { interval ->
                    ListItem(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onIntervalSelect(interval.intervalMillis)
                                    onDismiss()
                                }.padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = stringResource(interval.nameResId),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = currentInterval == interval.intervalMillis,
                                onClick = {
                                    onIntervalSelect(interval.intervalMillis)
                                    onDismiss()
                                },
                            )
                        },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = Color.Transparent,
                            ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
