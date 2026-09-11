package com.example.nearhelp.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = EmergencyCrimson,
        onPrimary = Color.White,
        primaryContainer = EmergencyRedContainer,
        onPrimaryContainer = Color.White,
        secondary = ActionAmber,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF332000),
        onSecondaryContainer = ActionAmber,
        tertiary = AiCyan,
        onTertiary = Color.Black,
        background = DarkBackground,
        onBackground = TextHighContrast,
        surface = CardSurface,
        onSurface = TextHighContrast,
        surfaceVariant = CardSurfaceVariant,
        onSurfaceVariant = TextMediumContrast,
        outline = SurfaceBorder,
        error = TextError,
        onError = Color.White,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = VictimPrimary,
        onPrimary = Color.White,
        primaryContainer = VictimPinkCard,
        onPrimaryContainer = VictimTextDark,
        secondary = StatusSafeGreen,
        onSecondary = Color.White,
        secondaryContainer = VictimGreenCard,
        onSecondaryContainer = VictimTextDark,
        tertiary = VictimPrimaryLight,
        onTertiary = Color.White,
        tertiaryContainer = VictimBlueCard,
        onTertiaryContainer = VictimTextDark,
        background = VictimBackground,
        onBackground = VictimTextDark,
        surface = VictimSurface,
        onSurface = VictimTextDark,
        surfaceVariant = VictimInputBg,
        onSurfaceVariant = VictimTextMuted,
        surfaceContainerLowest = VictimBackgroundSoft,
        outline = VictimBorder,
        outlineVariant = VictimDivider,
        error = VictimPrimary,
        onError = Color.White,
    )

@Composable
fun NearHelpTheme(
    darkTheme: Boolean = false, // Default to clean modern Guardian/Crisis light theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}


