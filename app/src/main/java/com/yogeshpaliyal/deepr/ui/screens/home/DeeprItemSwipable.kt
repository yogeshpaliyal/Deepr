package com.yogeshpaliyal.deepr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Trash
import kotlinx.coroutines.launch

@Composable
fun DeeprItemSwipable(
    account: GetLinksAndTags,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        )

    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
        state = dismissState,
        onDismiss = {
            scope.launch {
                dismissState.reset()
            }
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onItemClick(MenuItem.Delete(account))
                    false
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    onItemClick(MenuItem.Edit(account))
                    false
                }

                else -> {
                    false
                }
            }
        },
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    Color.Gray.copy(alpha = 0.5f),
                                ).fillMaxSize()
                                .clip(
                                    RoundedCornerShape(8.dp),
                                ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Icon(
                            imageVector = TablerIcons.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = Color.White,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    Color.Red.copy(alpha = 0.5f),
                                ).fillMaxSize()
                                .clip(
                                    RoundedCornerShape(8.dp),
                                ),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            imageVector = TablerIcons.Trash,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                else -> {
                    Color.White
                }
            }
        },
    ) {
        content()
    }
}
