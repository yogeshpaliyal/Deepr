package com.yogeshpaliyal.deepr.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomContent(
    deeprQueries: DeeprQueries,
    saveDialogInfo: SaveDialogInfo,
    modifier: Modifier = Modifier,
    onSaveDialogInfoChange: ((SaveDialogInfo?) -> Unit) = {},
) {
    var deeprInfo by remember(saveDialogInfo) {
        mutableStateOf(
            saveDialogInfo.deepr,
        )
    }
    var isError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    val isCreate = saveDialogInfo.deepr.id == 0L

    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = {
        onSaveDialogInfoChange(null)
    }) {
        Column(
            modifier =
                modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                        ),
                    ).fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(8.dp),
            ) {
                TextField(
                    value = deeprInfo.name,
                    onValueChange = {
                        deeprInfo = deeprInfo.copy(name = it)
                        isNameError = false
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    label = { Text(stringResource(R.string.enter_link_name)) },
                    supportingText = {
                        if (isNameError) {
                            Text(text = stringResource(R.string.enter_link_name_error))
                        }
                    },
                )

                TextField(
                    value = deeprInfo.link,
                    onValueChange = {
                        deeprInfo = deeprInfo.copy(link = it)
                        isError = false
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    label = { Text(stringResource(R.string.enter_deeplink_command)) },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(text = stringResource(R.string.invalid_empty_deeplink))
                        }
                    },
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    OutlinedButton(modifier = Modifier.then(if (isCreate) Modifier else Modifier.fillMaxWidth()), onClick = {
                        if (isValidDeeplink(deeprInfo.link)) {
                            if (deeprQueries
                                    .getDeeprByLink(deeprInfo.link)
                                    .executeAsOneOrNull() != null
                            ) {
                                Toast
                                    .makeText(
                                        context,
                                        "Deeplink already exists",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            } else {
                                onSaveDialogInfoChange(
                                    SaveDialogInfo(
                                        deeprInfo,
                                        saveDialogInfo.executeAfterSave,
                                    ),
                                )
                            }
                        } else {
                            isError = true
                        }
                    }) {
                        Text(stringResource(R.string.save))
                    }

                    if (isCreate) {
                        OutlinedButton(onClick = {
                            isError = !openDeeplink(context, deeprInfo.link)
                        }) {
                            Text(stringResource(R.string.execute))
                        }
                    }

                    if (isCreate) {
                        Button(onClick = {
                            if (isValidDeeplink(deeprInfo.link)) {
                                if (deeprQueries
                                        .getDeeprByLink(deeprInfo.link)
                                        .executeAsOneOrNull() != null
                                ) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Deeplink already exists",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } else {
                                    onSaveDialogInfoChange(
                                        SaveDialogInfo(
                                            deeprInfo,
                                            saveDialogInfo.executeAfterSave,
                                        ),
                                    )
                                }
                            } else {
                                isError = true
                            }
                        }) {
                            Text(stringResource(R.string.save_and_execute))
                        }
                    }
                }
            }
        }
    }
}
