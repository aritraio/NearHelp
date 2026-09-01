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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.nearhelp.ui.components.GuardianRadarView
import com.example.nearhelp.ui.components.SafeRouteSearchPill
import com.example.nearhelp.ui.components.SlideToExitPill
import kotlinx.coroutines.launch

/**
 * Screen 1: Guardian Radar & Safe Zone (The Calm Guardian State)
 *
 * Implements the left flagship screen from design.md & reference visual artifact:
 * - Soothing mint-green ambient gradient (#C8F5DC → #E3FAF0)
 * - Top Slide-To-Exit Pill
 * - Locality Safety Header: "China Basin" • "Safety Index 91%"
 * - Frosted glass destination search pill: "Where are you going today?"
 * - 360° Radar Canvas with concentric range rings and voice AI mic
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

    // Hold-for-SOS Progress State
    val holdProgress = remember { Animatable(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "GuardianPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Slide to Exit Pill + Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlideToExitPill(
                    onExit = onExit,
                    label = "Slide to exit"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier
                            .size(42.dp)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier
                                .size(42.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Destination & Safe Route Search Pill
            SafeRouteSearchPill(
                onClick = onNavigateToMap,
                onFilterClick = onNavigateToMap,
                placeholder = "Where are you going today?"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Interactive Radar Map Visualizer
            GuardianRadarView(
                onVoiceSosClick = onVoiceSosClick,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Hold For SOS Interactive Button with 3s Radial Fill
            Box(
                modifier = Modifier
                    .scale(if (isHolding) 1.05f else 1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0x26000000))
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
                        color = Color(0xFF0F172A)
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
