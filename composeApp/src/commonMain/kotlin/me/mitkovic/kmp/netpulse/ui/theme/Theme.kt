package me.mitkovic.kmp.netpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import me.mitkovic.kmp.netpulse.platform.UpdateStatusBarAppearance

val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        secondary = DarkSecondary,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        tertiary = DarkAccent,
    )

val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        secondary = LightSecondary,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        tertiary = LightAccent,
    )

@Composable
fun AppTheme(
    isLightTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isLightTheme) LightColorScheme else DarkColorScheme,
        typography = Typography,
        content = {
            // On Android, update system UI appearance
            UpdateStatusBarAppearance(!isLightTheme)
            content()
        },
    )
}
