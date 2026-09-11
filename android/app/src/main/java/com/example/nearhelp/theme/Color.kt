package com.example.nearhelp.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --------------------------------------------------------------------------
// 🤍 Victim Mockup Base (Clean White + Soft Pastel Cards)
// Matches ui/demo-ui-1st/victim/* : white base, red primary, pastel cards.
// Light-Mode-First design system — rounded modern surfaces (16–24dp).
// --------------------------------------------------------------------------
val VictimBackground = Color(0xFFFFFFFF)
val VictimBackgroundSoft = Color(0xFFFFF7F7)
val VictimSurface = Color(0xFFFFFFFF)
val VictimBorder = Color(0xFFE2E8F0)
val VictimDivider = Color(0xFFCBD5E1)
val VictimTextDark = Color(0xFF0F172A)
val VictimTextMuted = Color(0xFF64748B)
val VictimPrimary = Color(0xFFE52538)
val VictimPrimaryDark = Color(0xFFC81E2B)
val VictimPrimaryLight = Color(0xFFFF4D4F)
val VictimPinkCard = Color(0xFFFFF1F2)
val VictimPinkBorder = Color(0xFFFECDD3)
val VictimBlueCard = Color(0xFFEFF6FF)
val VictimBlueBorder = Color(0xFFBFDBFE)
val VictimGreenCard = Color(0xFFECFDF5)
val VictimGreenBorder = Color(0xFFA7F3D0)
val VictimPurpleCard = Color(0xFFF5F3FF)
val VictimPurpleBorder = Color(0xFFDDD6FE)
val VictimOrangeCard = Color(0xFFFFFBEB)
val VictimOrangeBorder = Color(0xFFFDE68A)
val VictimInputBg = Color(0xFFF1F5F9)
val StatusLiveRed = Color(0xFFE52538)
val StatusSafeGreen = Color(0xFF22C55E)
val StarRatingYellow = Color(0xFFFBBF24)

// --------------------------------------------------------------------------
// 🌿 Guardian Palette (Calm, Safe, Proactive Radar State)
// NOTE: Aligned to victim mockups — clean white base instead of mint.
// --------------------------------------------------------------------------
val GuardianBgTop = Color(0xFFFFFFFF)
val GuardianBgBottom = Color(0xFFFFF1F2)
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
// Victim mockup base surface: pure white (was #EFF3F6 slate).
val CrisisSurfaceBg = Color(0xFFFFFFFF)
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

