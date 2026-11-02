package com.example.nearhelp.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppTheme(val title: String, val description: String, val dark: Boolean) {
    PURE_WHITE("Pure White", "Clean white · teal accents", false),
    SOFT_WHITE("Soft White", "Soft white · blue accents", false),
    OLED_TEAL("OLED Teal", "True black · teal accents", true),
    OLED_BLUE("OLED Blue", "True black · blue accents", true),
}

/** A single saved appearance preference, shared by every screen. */
object ThemeManager {
    private var context: Context? = null
    var selectedTheme by mutableStateOf(AppTheme.PURE_WHITE)
        private set
    val isDarkMode: Boolean get() = selectedTheme.dark

    fun initialize(context: Context) {
        this.context = context.applicationContext
        val saved = context.getSharedPreferences("appearance", Context.MODE_PRIVATE).getString("theme", null)
        selectedTheme = AppTheme.entries.firstOrNull { it.name == saved } ?: AppTheme.PURE_WHITE
    }

    fun select(theme: AppTheme) {
        selectedTheme = theme
        context?.getSharedPreferences("appearance", Context.MODE_PRIVATE)?.edit()?.putString("theme", theme.name)?.apply()
    }

    fun toggleTheme() = setDarkModeEnabled(!isDarkMode)
    fun setDarkModeEnabled(enabled: Boolean) = select(if (enabled) AppTheme.OLED_TEAL else AppTheme.PURE_WHITE)
}
