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
import com.yogeshpaliyal.deepr.Profile
import com.yogeshpaliyal.deepr.R

@Composable
fun ProfileSelectionDialog(
    profiles: List<Profile>,
    currentProfileId: Long,
    onProfileSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.select_profile),
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                profiles.forEach { profile ->
                    ListItem(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onProfileSelect(profile.id)
                                    onDismiss()
                                }.padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = currentProfileId == profile.id,
                                onClick = {
                                    onProfileSelect(profile.id)
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
