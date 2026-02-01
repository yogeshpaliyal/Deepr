package com.yogeshpaliyal.deepr.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.Profile
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.MoonStars
import compose.icons.tablericons.Sun
import compose.icons.tablericons.User

data class ColorThemeOption(
    val id: String,
    val name: String,
    val color: Color,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTheme(
    profile: Profile,
    onProfileUpdate: (profile: Profile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Theme Mode Section (Light/Dark/System)
        Column {
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            val themeOptions =
                listOf(
                    "system" to "System default",
                    "light" to "Light",
                    "dark" to "Dark",
                )
            themeOptions.forEach { (mode, label) ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProfileUpdate(profile.copy(themeMode = mode))
                            }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = profile.themeMode == mode,
                        onClick = {
                            onProfileUpdate(profile.copy(themeMode = mode))
                        },
                    )
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector =
                                when (mode) {
                                    "light" -> TablerIcons.Sun
                                    "dark" -> TablerIcons.MoonStars
                                    else -> TablerIcons.User
                                },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // Color Theme Section
        Column {
            Text(
                text = "Color Theme",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            val colorThemeOptions =
                buildList {
                    // Dynamic color only available on Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        add(ColorThemeOption("dynamic", "Dynamic", Color(0xFF8E8E93)))
                    }
                    add(ColorThemeOption("purple", "Purple", Color(0xFF6650a4)))
                    add(ColorThemeOption("blue", "Blue", Color(0xFF0D6EFD)))
                    add(ColorThemeOption("green", "Green", Color(0xFF198754)))
                    add(ColorThemeOption("teal", "Teal", Color(0xFF20C997)))
                    add(ColorThemeOption("red", "Red", Color(0xFFDC3545)))
                    add(ColorThemeOption("orange", "Orange", Color(0xFFE85D04)))
                    add(ColorThemeOption("pink", "Pink", Color(0xFF7D5260)))
                }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                colorThemeOptions.forEach { option ->
                    ColorThemeItem(
                        option = option,
                        isSelected = profile.colorTheme == option.id,
                        onClick = {
                            onProfileUpdate(profile.copy(colorTheme = option.id))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorThemeItem(
    option: ColorThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(option.color)
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = TablerIcons.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = option.name,
            style = MaterialTheme.typography.labelSmall,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
