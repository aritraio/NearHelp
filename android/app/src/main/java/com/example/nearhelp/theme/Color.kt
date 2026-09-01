package com.example.nearhelp.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --------------------------------------------------------------------------
// 🌿 Guardian Palette (Calm, Safe, Proactive Radar State)
// --------------------------------------------------------------------------
val GuardianBgTop = Color(0xFFC8F5DC)
val GuardianBgBottom = Color(0xFFE3FAF0)
val GuardianRadarCircle = Color(0x5948BB78)
val GuardianRadarSweep = Color(0x4038A169)
val MintLight = Color(0xFFDDF8EA)
val MintPrimary = Color(0xFF8EE4B8)
val EmeraldAccent = Color(0xFF22C55E)

// --------------------------------------------------------------------------
// 🚨 Crisis & Alert Palette (High Urgency, Dispatch State)
// --------------------------------------------------------------------------
val EmergencyCrimson = Color(0xFFE52538)
val EmergencyCrimsonDark = Color(0xFFC2182B)
val EmergencyCrimsonLight = Color(0xFFFF3B30)
val CrisisSurfaceBg = Color(0xFFEFF3F6)
val CardNeomorphicLight = Color(0xFFFFFFFF)
val CardNeomorphicShadow = Color(0x12000000)
val CancelPillGreen = Color(0xFF34C759)
val CountdownBadgeRed = Color(0xFFE52538)
val CoralActive = Color(0xFFFF5A5F)
val AmberAlert = Color(0xFFFF9800)

val EmergencyCrimsonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF3B30), Color(0xFFD70015))
)

val DispatchSliderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF34C759), Color(0xFFE52538))
)

val GuardianBgGradient = Brush.verticalGradient(
    colors = listOf(GuardianBgTop, GuardianBgBottom)
)

// --------------------------------------------------------------------------
// 🔤 Typography, Surfaces & Translucent Glass Tokens
// --------------------------------------------------------------------------
val TextPrimaryDark = Color(0xFF0F172A)
val TextSecondaryMuted = Color(0xFF64748B)
val SearchPillBg = Color(0xCCFFFFFF)
val GlassSurfaceWhite = Color(0xE6FFFFFF)
val GlassSurfaceBorder = Color(0x4DFFFFFF)
val SoftInputBg = Color(0xFFF1F5F9)

// --------------------------------------------------------------------------
// 🖤 Dark Theme & Legacy Compatibility Tokens
// --------------------------------------------------------------------------
val EmergencyRed = Color(0xFFE52538)
val EmergencyRedDark = Color(0xFFC2182B)
val EmergencyRedContainer = Color(0xFF3B1212)
val EmergencyRedGlow = Color(0x66E52538)

val ActionAmber = Color(0xFFFF9800)
val ActionAmberDark = Color(0xFFF57C00)
val SafeGreen = Color(0xFF34C759)
val SafeGreenDark = Color(0xFF2E7D32)
val AiCyan = Color(0xFF00E5FF)
val AiBlue = Color(0xFF2196F3)

val DarkBackground = Color(0xFF0F141C)
val CardSurface = Color(0xFF181F2C)
val CardSurfaceVariant = Color(0xFF222B3D)
val SurfaceBorder = Color(0xFF2D3748)

val TextHighContrast = Color(0xFFFFFFFF)
val TextMediumContrast = Color(0xFFCBD5E1)
val TextMuted = Color(0xFF94A3B8)
val TextError = Color(0xFFFF5252)

