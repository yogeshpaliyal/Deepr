package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.LanguageUtil

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onLanguageSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                LanguageUtil.availableLanguages.forEach { language ->
                    ListItem(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onLanguageSelect(language.code)
                                    onDismiss()
                                }.padding(vertical = 4.dp),
                        headlineContent = {
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        supportingContent =
                            if (language.name != language.nativeName) {
                                {
                                    Text(
                                        text = language.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                null
                            },
                        trailingContent = {
                            RadioButton(
                                selected = currentLanguageCode == language.code,
                                onClick = {
                                    onLanguageSelect(language.code)
                                    onDismiss()
                                },
                            )
                        },
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
