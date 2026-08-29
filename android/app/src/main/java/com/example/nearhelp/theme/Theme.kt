package com.example.nearhelp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = EmergencyRed,
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
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = EmergencyRedDark,
    secondary = ActionAmberDark,
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEDE7F6),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),
    error = Color(0xFFB00020),
    onError = Color.White,
  )

@Composable
fun NearHelpTheme(
  darkTheme: Boolean = true, // Default to high-contrast dark emergency theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}

