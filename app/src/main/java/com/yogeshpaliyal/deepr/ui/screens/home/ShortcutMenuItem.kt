package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.util.hasShortcut
import com.yogeshpaliyal.deepr.util.isShortcutSupported
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus

@Composable
fun ShortcutMenuItem(
    account: Deepr,
    onShortcutClick: (Deepr) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shortcutExists = remember(account.id) { hasShortcut(context, account.id) }

    if (isShortcutSupported(LocalContext.current)) {
        DropdownMenuItem(
            modifier = modifier,
            text = { Text(if (shortcutExists) "Edit shortcut" else "Add shortcut") },
            onClick = {
                onShortcutClick(account)
            },
            leadingIcon = {
                Icon(
                    TablerIcons.Plus,
                    contentDescription = if (shortcutExists) "Edit shortcut" else "Add shortcut",
                )
            },
        )
    }
}
