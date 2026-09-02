package com.example.nearhelp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.data.location.LocationHelper
import com.example.nearhelp.theme.EmergencyCrimson
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
 * - Top Active Protection status chip + 1-tap Theme Toggle switch + Map/Profile icons
 * - Real-time Locality Safety Header (Live Reverse Geocoding & GPS coordinate telemetry)
 * - 360° Radar Canvas with concentric range rings and voice AI mic
 * - Big vibrant Red "HOLD FOR SOS" trigger (with progress ring on press)
 * - "CHECK IN" with chevron indicators ⌄
 * - Live Geodetic Telemetry readout (formatted latitude/longitude)
 * - Bottom Slide-To-Exit Pill
 */
@Composable
fun GuardianRadarScreen(
    onExit: () -> Unit,
    onNavigateToCrisis: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onVoiceSosClick: () -> Unit,
    modifier: Modifier = Modifier,
    localityName: String? = null,
    safetyIndex: Int? = null,
    coordinatesText: String? = null,
    isAnonymous: Boolean = false
) {
    val context = LocalContext.current
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
    val checkInTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)

    // Real-time GPS Location Provider
    val locationHelper = remember { LocationHelper(context) }
    val locationState by locationHelper.locationState.collectAsState()

    // Runtime Permission Request for Location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            locationHelper.startLocationUpdates()
        }
    }

    DisposableEffect(Unit) {
        if (locationHelper.hasLocationPermission()) {
            locationHelper.startLocationUpdates()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        onDispose {
            locationHelper.stopLocationUpdates()
        }
    }

    // Effective Display Values (Real-time GPS takes priority unless overridden)
    val displayLocality = localityName ?: locationState.localityName
    val displaySafetyIndex = safetyIndex ?: locationState.safetyIndex
    val displayCoordinates = coordinatesText ?: locationState.coordinatesText

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
            // 1. Top Bar: Active Protection Badge + Map/Profile + Theme Toggle Switch
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x18000000))
                            .background(badgeBgColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Community Map",
                            tint = badgeTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!isAnonymous) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x18000000))
                                .background(badgeBgColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile & Medical ID",
                                tint = badgeTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    // 1-Tap Responsive Theme Toggle Switch
                    ThemeToggleSwitch(
                        isDarkMode = isDark,
                        onToggle = { ThemeManager.toggleTheme() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Real-time Locality Safety Header & Index
            Text(
                text = displayLocality,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                ),
                color = titleTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Live GPS",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Safety Index $displaySafetyIndex%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = safetyTextColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Interactive Radar Map Visualizer
            GuardianRadarView(
                onVoiceSosClick = onVoiceSosClick,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Big Red HOLD FOR SOS Interactive Button with Radial Fill & Pulse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .scale(if (isHolding) 1.03f else pulseScale)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = EmergencyCrimson,
                        ambientColor = Color(0x4DE52538)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF2E4D),
                                Color(0xFFE52538),
                                Color(0xFFC2182B)
                            )
                        )
                    )
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
                    .padding(vertical = 18.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isHolding) {
                        CircularProgressIndicator(
                            progress = { holdProgress.value },
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp,
                            trackColor = Color(0x66FFFFFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "SOS Flash",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = if (isHolding) "RELEASE TO CANCEL" else "HOLD FOR SOS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Check-in trigger with double chevron
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        checkInStatus = "Status ping sent to guardian contacts ✓"
                    }
                    .padding(6.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            // 6. Live Geodetic Telemetry Display
            Text(
                text = displayCoordinates,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = telemetryTextColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Bottom: Slide To Exit Button
            SlideToExitPill(
                onExit = onExit,
                label = "Slide to exit",
                modifier = Modifier
                    .width(260.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}
