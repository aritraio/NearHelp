package com.example.nearhelp.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun appColorScheme(theme: AppTheme) = when (theme) {
    AppTheme.PURE_WHITE -> lightColorScheme(
        primary = Color(0xFF17635D), onPrimary = Color.White,
        primaryContainer = Color(0xFFF1F6F5), onPrimaryContainer = Color(0xFF17635D),
        background = Color.White, surface = Color.White, onBackground = Color(0xFF172C30), onSurface = Color(0xFF172C30),
        surfaceVariant = Color(0xFFF1F6F5), onSurfaceVariant = Color(0xFF52686C),
        surfaceContainer = Color(0xFFF1F6F5), surfaceContainerLow = Color(0xFFF1F6F5), surfaceContainerLowest = Color.White,
        outline = Color(0xFFD8E2E1), outlineVariant = Color(0xFFD8E2E1),
        error = Color(0xFFB52D3D), onError = Color.White, errorContainer = Color(0xFFFFF0F0), onErrorContainer = Color(0xFFB52D3D),
        secondary = Color(0xFF24664C), onSecondary = Color.White,
    )
    AppTheme.SOFT_WHITE -> lightColorScheme(
        primary = Color(0xFF205F9F), onPrimary = Color.White,
        primaryContainer = Color(0xFFEDF2F8), onPrimaryContainer = Color(0xFF205F9F),
        background = Color(0xFFF8FAFC), surface = Color(0xFFF8FAFC), onBackground = Color(0xFF172B45), onSurface = Color(0xFF172B45),
        surfaceVariant = Color(0xFFEDF2F8), onSurfaceVariant = Color(0xFF52647B),
        surfaceContainer = Color(0xFFEDF2F8), surfaceContainerLow = Color(0xFFEDF2F8), surfaceContainerLowest = Color(0xFFF8FAFC),
        outline = Color(0xFFD6DFE9), outlineVariant = Color(0xFFD6DFE9),
        error = Color(0xFFB52D3D), onError = Color.White, errorContainer = Color(0xFFFFF0F0), onErrorContainer = Color(0xFFB52D3D),
        secondary = Color(0xFF24664C), onSecondary = Color.White,
    )
    AppTheme.OLED_TEAL -> darkColorScheme(
        primary = Color(0xFF8FD5C7), onPrimary = Color(0xFF102C28),
        primaryContainer = Color(0xFF111C19), onPrimaryContainer = Color(0xFF8FD5C7),
        background = Color.Black, surface = Color.Black, onBackground = Color(0xFFEDF4F3), onSurface = Color(0xFFEDF4F3),
        surfaceVariant = Color(0xFF111C19), onSurfaceVariant = Color(0xFFAFC3C0),
        surfaceContainer = Color(0xFF111C19), surfaceContainerLow = Color(0xFF111C19), surfaceContainerLowest = Color.Black,
        outline = Color(0xFF465B57), outlineVariant = Color(0xFF465B57),
        error = Color(0xFFFFB3B9), onError = Color(0xFF4A1119), errorContainer = Color(0xFF30151C), onErrorContainer = Color(0xFFFFB3B9),
        secondary = Color(0xFFA0DABD), onSecondary = Color(0xFF102C28),
    )
    AppTheme.OLED_BLUE -> darkColorScheme(
        primary = Color(0xFFABCFFF), onPrimary = Color(0xFF122A46),
        primaryContainer = Color(0xFF111B2A), onPrimaryContainer = Color(0xFFABCFFF),
        background = Color.Black, surface = Color.Black, onBackground = Color(0xFFEDF3FF), onSurface = Color(0xFFEDF3FF),
        surfaceVariant = Color(0xFF111B2A), onSurfaceVariant = Color(0xFFB2C1D9),
        surfaceContainer = Color(0xFF111B2A), surfaceContainerLow = Color(0xFF111B2A), surfaceContainerLowest = Color.Black,
        outline = Color(0xFF465972), outlineVariant = Color(0xFF465972),
        error = Color(0xFFFFB3B9), onError = Color(0xFF4A1119), errorContainer = Color(0xFF30151C), onErrorContainer = Color(0xFFFFB3B9),
        secondary = Color(0xFFA0DABD), onSecondary = Color(0xFF122A46),
    )
}

@Composable
fun NearHelpTheme(darkTheme: Boolean = ThemeManager.isDarkMode, content: @Composable () -> Unit) {
    // Preserve the explicit darkTheme argument used by previews; the app follows the saved selection.
    val selected = ThemeManager.selectedTheme
    val theme = if (darkTheme == selected.dark) selected else if (darkTheme) AppTheme.OLED_TEAL else AppTheme.PURE_WHITE
    MaterialTheme(colorScheme = appColorScheme(theme), typography = Typography, content = content)
}
