package com.example.nearhelp.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global Theme Manager for responsive Dark / Light theme switching across the app.
 */
object ThemeManager {
    var isDarkMode by mutableStateOf(false)

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        isDarkMode = enabled
    }
}
