package com.example.nearhelp.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class AppThemeTest {
    @Test fun oledThemes_useTrueBlackSurfaces() {
        assertEquals(4, AppTheme.entries.size)
        AppTheme.entries.filter { it.dark }.forEach { theme ->
            assertEquals(Color.Black, appColorScheme(theme).background)
            assertEquals(Color.Black, appColorScheme(theme).surface)
        }
    }
    @Test fun textAndPrimaryActions_keepReadableContrastInEveryTheme() {
        AppTheme.entries.forEach { theme ->
            val c = appColorScheme(theme)
            listOf(c.onBackground to c.background, c.onSurfaceVariant to c.surfaceVariant,
                c.primary to c.background, c.onPrimary to c.primary, c.onError to c.error).forEach { (text, background) ->
                assertTrue("${theme.title}: contrast below 4.5", contrast(text, background) >= 4.5)
            }
        }
    }
    private fun contrast(a: Color, b: Color): Double {
        fun luminance(c: Color): Double {
            fun channel(x: Float) = if (x <= .04045) x / 12.92 else ((x + .055) / 1.055).pow(2.4)
            return .2126 * channel(c.red) + .7152 * channel(c.green) + .0722 * channel(c.blue)
        }
        val x = luminance(a); val y = luminance(b)
        return (maxOf(x, y) + .05) / (minOf(x, y) + .05)
    }
}
