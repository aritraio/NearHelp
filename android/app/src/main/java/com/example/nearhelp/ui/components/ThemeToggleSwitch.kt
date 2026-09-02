package com.example.nearhelp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Responsive Dark / Light Mode Interactive Toggle Switch
 *
 * Provides a fluid 1-tap animated toggle between Dark and Light themes:
 * - Direct click response with haptic feedback (no confirmation dialogs)
 * - Spring-animated sliding capsule thumb
 * - Sun ☀️ and Moon 🌙 glyphs with contextual glow
 */
@Composable
fun ThemeToggleSwitch(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkMode) 34.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ThemeSwitchThumbOffset"
    )

    val trackBgColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF1E293B) else Color(0xCCFFFFFF),
        label = "ThemeSwitchTrackBg"
    )

    val trackBorderColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0x4D34D399) else Color(0x1F000000),
        label = "ThemeSwitchTrackBorder"
    )

    val thumbBgColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        label = "ThemeSwitchThumbBg"
    )

    val activeIconTint by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFF59E0B),
        label = "ThemeSwitchActiveIconTint"
    )

    Box(
        modifier = modifier
            .width(72.dp)
            .height(38.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(100.dp),
                ambientColor = if (isDarkMode) Color(0x33000000) else Color(0x15000000)
            )
            .clip(RoundedCornerShape(100.dp))
            .background(trackBgColor)
            .border(1.dp, trackBorderColor, RoundedCornerShape(100.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Inactive background icons under track
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sun slot
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Light Mode",
                    tint = if (isDarkMode) Color(0xFF64748B) else Color.Transparent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(modifier = Modifier.weight(1f))

            // Moon slot
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = if (!isDarkMode) Color(0xFF94A3B8) else Color.Transparent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Animated Sliding Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(30.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x33000000)
                )
                .clip(CircleShape)
                .background(thumbBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDarkMode) "Dark Mode Active" else "Light Mode Active",
                tint = activeIconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
