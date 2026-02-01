package com.yogeshpaliyal.deepr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Default Purple theme
private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = PurpleGrey80,
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = PurpleGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Red theme
private val RedDarkColorScheme =
    darkColorScheme(
        primary = Red80,
        onPrimary = Color(0xFF690005),
        primaryContainer = Color(0xFF93000A),
        onPrimaryContainer = Color(0xFFFFDAD6),
        secondary = RedGrey80,
        onSecondary = Color(0xFF442926),
        secondaryContainer = Color(0xFF5D3F3B),
        onSecondaryContainer = Color(0xFFFFDAD6),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val RedLightColorScheme =
    lightColorScheme(
        primary = Red40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDAD6),
        onPrimaryContainer = Color(0xFF410002),
        secondary = RedGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFDAD6),
        onSecondaryContainer = Color(0xFF2C1512),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Orange theme
private val OrangeDarkColorScheme =
    darkColorScheme(
        primary = Orange80,
        onPrimary = Color(0xFF4E2600),
        primaryContainer = Color(0xFF6F3800),
        onPrimaryContainer = Color(0xFFFFDCC2),
        secondary = OrangeGrey80,
        onSecondary = Color(0xFF3D2E1F),
        secondaryContainer = Color(0xFF554434),
        onSecondaryContainer = Color(0xFFFFDCC2),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val OrangeLightColorScheme =
    lightColorScheme(
        primary = Orange40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDCC2),
        onPrimaryContainer = Color(0xFF2C1600),
        secondary = OrangeGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFDCC2),
        onSecondaryContainer = Color(0xFF251A0C),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Blue theme
private val BlueDarkColorScheme =
    darkColorScheme(
        primary = Blue80,
        onPrimary = Color(0xFF002F65),
        primaryContainer = Color(0xFF00458E),
        onPrimaryContainer = Color(0xFFD6E3FF),
        secondary = BlueGrey80,
        onSecondary = Color(0xFF2E3D50),
        secondaryContainer = Color(0xFF455468),
        onSecondaryContainer = Color(0xFFD6E3FF),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val BlueLightColorScheme =
    lightColorScheme(
        primary = Blue40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E3FF),
        onPrimaryContainer = Color(0xFF001B3D),
        secondary = BlueGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD6E3FF),
        onSecondaryContainer = Color(0xFF17283D),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Green theme
private val GreenDarkColorScheme =
    darkColorScheme(
        primary = Green80,
        onPrimary = Color(0xFF003919),
        primaryContainer = Color(0xFF005227),
        onPrimaryContainer = Color(0xFFA0F5B2),
        secondary = GreenGrey80,
        onSecondary = Color(0xFF253528),
        secondaryContainer = Color(0xFF3B4C3E),
        onSecondaryContainer = Color(0xFFC1ECC5),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val GreenLightColorScheme =
    lightColorScheme(
        primary = Green40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFA0F5B2),
        onPrimaryContainer = Color(0xFF002108),
        secondary = GreenGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFC1ECC5),
        onSecondaryContainer = Color(0xFF0E2014),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Teal theme
private val TealDarkColorScheme =
    darkColorScheme(
        primary = Teal80,
        onPrimary = Color(0xFF00363D),
        primaryContainer = Color(0xFF004F58),
        onPrimaryContainer = Color(0xFF9CF1FF),
        secondary = TealGrey80,
        onSecondary = Color(0xFF1F3438),
        secondaryContainer = Color(0xFF364B4F),
        onSecondaryContainer = Color(0xFFBCEBF0),
        tertiary = Pink80,
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
    )

private val TealLightColorScheme =
    lightColorScheme(
        primary = Teal40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF9CF1FF),
        onPrimaryContainer = Color(0xFF001F24),
        secondary = TealGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFBCEBF0),
        onSecondaryContainer = Color(0xFF051F23),
        tertiary = Pink40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
    )

// Pink theme
private val PinkDarkColorScheme =
    darkColorScheme(
        primary = Pink80,
        onPrimary = Color(0xFF5D1133),
        primaryContainer = Color(0xFF7A294A),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary = PurpleGrey80,
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Purple80,
        onTertiary = Color(0xFF381E72),
        tertiaryContainer = Color(0xFF4F378B),
        onTertiaryContainer = Color(0xFFEADDFF),
    )

private val PinkLightColorScheme =
    lightColorScheme(
        primary = Pink40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3E001D),
        secondary = PurpleGrey40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Purple40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFEADDFF),
        onTertiaryContainer = Color(0xFF21005D),
    )

@Composable
fun DeeprTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "system",
    colorTheme: String = "dynamic",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val useDarkTheme =
        when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> darkTheme // "system" or any other value defaults to system
        }

    val colorScheme =
        when {
            colorTheme == "dynamic" && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            colorTheme == "red" -> if (useDarkTheme) RedDarkColorScheme else RedLightColorScheme
            colorTheme == "orange" -> if (useDarkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
            colorTheme == "blue" -> if (useDarkTheme) BlueDarkColorScheme else BlueLightColorScheme
            colorTheme == "green" -> if (useDarkTheme) GreenDarkColorScheme else GreenLightColorScheme
            colorTheme == "teal" -> if (useDarkTheme) TealDarkColorScheme else TealLightColorScheme
            colorTheme == "pink" -> if (useDarkTheme) PinkDarkColorScheme else PinkLightColorScheme
            colorTheme == "purple" -> if (useDarkTheme) DarkColorScheme else LightColorScheme
            useDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
