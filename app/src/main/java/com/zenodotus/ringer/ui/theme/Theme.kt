package com.zenodotus.ringer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = DividerBlue,
    //tertiary = Pink80
)


private val ColdColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = TextDark,

    primaryContainer = AccentBlue.copy(alpha = 0.15f),
    onPrimaryContainer = TextDark,

    secondary = DividerBlue,
    onSecondary = TextDark,

    secondaryContainer = DividerBlue.copy(alpha = 0.15f),
    onSecondaryContainer = TextDark,

    background = SoftBackground,
    onBackground = TextDark,

    surface = SecondaryBackground,
    onSurface = TextDark,

    surfaceVariant = SecondaryBackground,
    onSurfaceVariant = TextDark,

    outline = DividerBlue,
    tertiary = AccentBlue,
    onTertiary = TextDark
)


@Composable
fun RingerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> ColdColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}