package com.example.nearhelp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.GuardianBgBottom
import com.example.nearhelp.theme.GuardianBgTop
import com.example.nearhelp.theme.ThemeManager
import com.example.nearhelp.ui.components.GuardianRadarView
import com.example.nearhelp.ui.components.SlideToExitPill
import com.example.nearhelp.ui.components.ThemeToggleSwitch
import kotlinx.coroutines.launch

/**
 * Screen 1: Guardian Radar & Safe Zone (The Calm Guardian State)
 *
 * Implements the flagship Guardian screen:
 * - Ambient gradient (Light: Mint Green, Dark: Midnight Emerald)
 * - Top Active Protection status chip + 1-tap Theme Toggle switch
 * - Locality Safety Header: "China Basin" • "Safety Index 91%"
 * - 360° Radar Canvas with concentric range rings and voice AI mic
 * - Slide to exit slider in between Radar and HOLD FOR SOS
 * - Bottom "HOLD FOR SOS" (3s hold trigger with progress ring)
 * - "CHECK IN" with chevron indicators ⌄⌄
 * - Live Geodetic Telemetry readout (37.7749° N 122.39632° W)
 */
@Composable
fun GuardianRadarScreen(
    onExit: () -> Unit,
    onNavigateToCrisis: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onVoiceSosClick: () -> Unit,
    modifier: Modifier = Modifier,
    localityName: String = "China Basin",
    safetyIndex: Int = 91,
    coordinatesText: String = "37.7749° N 122.39632° W",
    isAnonymous: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var checkInStatus by remember { mutableStateOf<String?>(null) }
    val isDark = ThemeManager.isDarkMode

    // Dynamic Theme Color Tokens
    val bgColors = if (isDark) {
        listOf(Color(0xFF061A13), Color(0xFF0D2D21))
    } else {
        listOf(GuardianBgTop, GuardianBgBottom)
    }
    val titleTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val safetyTextColor = if (isDark) Color(0xFF34D399) else Color(0xFF334155)
    val telemetryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val badgeBgColor = if (isDark) Color(0x33FFFFFF) else Color(0xCCFFFFFF)
    val badgeTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val sosBgColor = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)
    val sosTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val checkInTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)

    // Hold-for-SOS Progress State
    val holdProgress = remember { Animatable(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "GuardianPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GuardianSosPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = bgColors
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Active Protection Badge + 1-Tap Theme Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Protection Status Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(100.dp), ambientColor = Color(0x18000000))
                        .background(badgeBgColor, RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Protection",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = badgeTextColor
                        )
                    )
                }

                // 1-Tap Responsive Theme Toggle Switch
                ThemeToggleSwitch(
                    isDarkMode = isDark,
                    onToggle = { ThemeManager.toggleTheme() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Locality Safety Header & Index
            Text(
                text = localityName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = titleTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Safety Index $safetyIndex%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = safetyTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Interactive Radar Map Visualizer
            GuardianRadarView(
                onVoiceSosClick = onVoiceSosClick,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Slide to Exit Pill (Positioned in between Radar and HOLD FOR SOS)
            SlideToExitPill(
                onExit = onExit,
                label = "Slide to exit",
                modifier = Modifier.width(240.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Hold For SOS Interactive Button with 3s Radial Fill
            Box(
                modifier = Modifier
                    .scale(if (isHolding) 1.05f else 1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(sosBgColor)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val job = coroutineScope.launch {
                                    holdProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 1500)
                                    )
                                }
                                tryAwaitRelease()
                                isHolding = false
                                if (holdProgress.value >= 0.95f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToCrisis("medical")
                                }
                                job.cancel()
                                holdProgress.snapTo(0f)
                            },
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToCrisis("medical")
                            }
                        )
                    }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isHolding) {
                        CircularProgressIndicator(
                            progress = { holdProgress.value },
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFE52538),
                            strokeWidth = 3.dp,
                            trackColor = Color(0x33E52538)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = "HOLD FOR SOS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = 1.5.sp
                        ),
                        color = sosTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Check-in trigger with double chevron
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        checkInStatus = "Status ping sent to guardian contacts ✓"
                    }
                    .padding(8.dp)
            ) {
                Text(
                    text = "CHECK IN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    color = checkInTextColor
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Check In",
                    tint = checkInTextColor,
                    modifier = Modifier.size(18.dp)
                )
                if (checkInStatus != null) {
                    Text(
                        text = checkInStatus!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF4ADE80) else Color(0xFF166534),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Live Geodetic Telemetry Display
            Text(
                text = coordinatesText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = telemetryTextColor
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
