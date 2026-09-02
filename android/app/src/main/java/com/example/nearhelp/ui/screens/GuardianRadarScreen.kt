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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.nearhelp.theme.EmergencyCrimson
import com.example.nearhelp.theme.GuardianBgBottom
import com.example.nearhelp.theme.GuardianBgTop
import com.example.nearhelp.ui.components.GuardianRadarView
import com.example.nearhelp.ui.components.SafeRouteSearchPill
import com.example.nearhelp.ui.components.SlideToExitPill
import kotlinx.coroutines.launch

/**
 * Screen 1: Guardian Radar & Safe Zone (The Calm Guardian State)
 *
 * Implements the left flagship screen from design.md & reference visual artifact:
 * - Soothing mint-green ambient gradient (#C8F5DC → #E3FAF0)
 * - Top Status Bar with Guardian Active Shield Badge & Map/Profile Action Icons
 * - Locality Safety Header: "China Basin" • "Safety Index 91%"
 * - Frosted glass destination search pill: "Where are you going today?"
 * - 360° Radar Canvas with concentric range rings and voice AI mic
 * - Big vibrant Red "HOLD FOR SOS" trigger (with progress ring on press)
 * - "CHECK IN" with chevron indicators ⌄
 * - Live Geodetic Telemetry readout (37.7749° N 122.39632° W)
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
    localityName: String = "China Basin",
    safetyIndex: Int = 91,
    coordinatesText: String = "37.7749° N 122.39632° W",
    isAnonymous: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var checkInStatus by remember { mutableStateOf<String?>(null) }

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
                    colors = listOf(GuardianBgTop, GuardianBgBottom)
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
            // 1. Top Bar: Guardian Active Badge + Map/Profile Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(100.dp), ambientColor = Color(0x18000000))
                        .background(Color(0xCCFFFFFF), RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GUARDIAN ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x18000000))
                            .background(Color(0xCCFFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Community Map",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!isAnonymous) {
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x18000000))
                                .background(Color(0xCCFFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile & Medical ID",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Locality Safety Header & Index
            Text(
                text = localityName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Safety Index $safetyIndex%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = Color(0xFF334155),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Destination & Safe Route Search Pill
            SafeRouteSearchPill(
                onClick = onNavigateToMap,
                onFilterClick = onNavigateToMap,
                placeholder = "Where are you going today?"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Interactive Radar Map Visualizer
            GuardianRadarView(
                onVoiceSosClick = onVoiceSosClick,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Big Red HOLD FOR SOS Interactive Button with Radial Fill & Pulse
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

            // 6. Check-in trigger with double chevron
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
                    color = Color(0xFF334155)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Check In",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
                if (checkInStatus != null) {
                    Text(
                        text = checkInStatus!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7. Live Geodetic Telemetry Display
            Text(
                text = coordinatesText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Bottom: Slide To Exit Button
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
