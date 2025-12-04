package com.yogeshpaliyal.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yogeshpaliyal.shared.data.DeeprLink
import com.yogeshpaliyal.shared.ui.screens.MenuListItem
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import deepr.shared.generated.resources.Res
import deepr.shared.generated.resources.add_shortcut
import deepr.shared.generated.resources.edit_shortcut
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShortcutMenuItem(
    account: DeeprLink,
    onShortcutClick: (DeeprLink) -> Unit,
) {
    val context = LocalContext.current
    val shortcutExists = remember(account.id) { hasShortcut(context, account.id) }

    val addShortcutText = stringResource(Res.string.add_shortcut)
    val editShortcutText = stringResource(Res.string.edit_shortcut)

    if (isShortcutSupported(LocalContext.current)) {
        MenuListItem(
            text = if (shortcutExists) editShortcutText else addShortcutText,
            icon = TablerIcons.Plus,
            onClick = {
                onShortcutClick(account)
            },
        )
    }
}
