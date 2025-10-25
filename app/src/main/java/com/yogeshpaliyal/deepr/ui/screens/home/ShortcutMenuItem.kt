package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.hasShortcut
import com.yogeshpaliyal.deepr.util.isShortcutSupported
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus

@Composable
fun ShortcutMenuItem(
    account: GetLinksAndTags,
    onShortcutClick: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shortcutExists = remember(account.id) { hasShortcut(context, account.id) }

    val addShortcutText = stringResource(R.string.add_shortcut)
    val editShortcutText = stringResource(R.string.edit_shortcut)

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
