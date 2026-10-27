package com.example.nearhelp.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.nearhelp.data.location.LocationHelper
import com.example.nearhelp.data.location.UserLocationState
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimGreenBorder
import com.example.nearhelp.theme.VictimGreenCard
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimPurpleBorder
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.victim.LiveMapFeedCard
import com.example.nearhelp.ui.victim.NearHelpTopBar
import com.example.nearhelp.ui.victim.SectionHeader
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimLocationCard
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToResponderProfile: () -> Unit = onNavigateToProfile,
    onNavigateToHistory: () -> Unit = onNavigateToProfile,
    onNavigateToMap: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onCreateSos: () -> Unit = onNavigateToTracking,
    onNavigateToAssistant: () -> Unit = {},
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    onOverlayStateChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context.applicationContext) }
    val locationState by locationHelper.locationState.collectAsState()
    var customLocation by rememberSaveable { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        locationHelper.startLocationUpdates()
    }
    DisposableEffect(Unit) {
        onOverlayStateChanged(false)
        if (locationHelper.hasLocationPermission()) locationHelper.startLocationUpdates()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        onDispose { locationHelper.close() }
    }
    Box(modifier.fillMaxSize().background(VictimBackground)) {
        VictimHomeContent(
            locationState = locationState,
            customLocation = customLocation,
            onLocationChange = { customLocation = it },
            onResetGps = { customLocation = null; locationHelper.startLocationUpdates() },
            onSosTap = onCreateSos,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToResponderProfile = onNavigateToResponderProfile,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToMap = onNavigateToMap,
            onNavigateToAssistant = onNavigateToAssistant,
            onNavigateToTracking = onNavigateToTracking,
            showBottomBar = showBottomBar,
        )
    }
}

@Composable
private fun VictimHomeContent(
    locationState: UserLocationState,
    customLocation: String?,
    onLocationChange: (String?) -> Unit,
    onResetGps: () -> Unit,
    onSosTap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToResponderProfile: () -> Unit = onNavigateToProfile,
    onNavigateToHistory: () -> Unit = onNavigateToProfile,
    onNavigateToMap: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToTracking: () -> Unit,
    showBottomBar: Boolean = true,
) {
    var showEditLocationDialog by remember { mutableStateOf(false) }

    val displayLocationTitle = customLocation ?: if (locationState.isLocating && locationState.localityName == "Locating...") {
        "Locating GPS..."
    } else {
        locationState.localityName
    }

    val displayAccuracyText = if (customLocation != null) {
        "Custom location • Tap Edit to change or reset"
    } else if (locationState.hasFix) {
        locationState.accuracyMeters?.let { "GPS accuracy: ±${it.toInt()} m" } ?: "Location from device"
    } else if (locationState.hasPermission) {
        "Waiting for a current GPS fix"
    } else {
        "Tap Edit to set location or enable GPS"
    }

    if (showEditLocationDialog) {
        var tempText by remember { mutableStateOf(if (displayLocationTitle == "Locating GPS...") "" else displayLocationTitle) }
        AlertDialog(
            onDismissRequest = { showEditLocationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VictimPinkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VictimPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Edit Location",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = VictimTextDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Enter your current address or locality so nearby responders can locate you immediately.",
                        fontSize = 13.5.sp,
                        color = VictimTextMuted,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        label = { Text("Address / Landmark", fontSize = 13.sp) },
                        placeholder = { Text("e.g. Park Street, Kolkata", fontSize = 13.sp, color = VictimTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VictimPrimary,
                            unfocusedBorderColor = VictimBorder,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = VictimTextDark,
                            unfocusedTextColor = VictimTextDark,
                            cursorColor = VictimPrimary,
                            focusedLabelColor = VictimPrimary,
                        )
                    )

                    // Quick option: Revert to Live GPS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VictimPinkCard)
                            .border(1.dp, VictimPinkBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onResetGps()
                                showEditLocationDialog = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = VictimPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use Current GPS Location",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VictimPrimary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempText.isNotBlank()) {
                            onLocationChange(tempText.trim())
                        }
                        showEditLocationDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = VictimPrimary, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLocationDialog = false }) {
                    Text("Cancel", color = VictimTextMuted, fontSize = 14.sp)
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
        )
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Unified NearHelp Top Bar with brand wordmark and avatar
            NearHelpTopBar(
                onAvatarClick = onNavigateToProfile,
            )

            // Location Card with Live GPS & Edit option
            VictimLocationCard(
                locationTitle = displayLocationTitle,
                accuracyText = displayAccuracyText,
                actionLabel = "Edit",
                actionIcon = Icons.Default.Edit,
                onAction = { showEditLocationDialog = true },
            )

            // Giant Hero SOS Card with Live Map Feed Background
            LiveMapFeedCard(
                latitude = locationState.latitude,
                longitude = locationState.longitude,
                coordinatesText = locationState.coordinatesText,
                onMapClick = onNavigateToMap,
                modifier = Modifier.height(390.dp)
            ) {
                HoldForSosButton(
                    onSosTriggered = onSosTap,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                )
            }

            androidx.compose.material3.OutlinedButton(onClick = onNavigateToTracking, modifier = Modifier.fillMaxWidth()) {
                Text("Community emergencies & active SOS")
            }

            SectionHeader(title = "Be Prepared", actionLabel = "Learn More", onAction = onNavigateToAssistant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BePreparedCard(
                    bg = VictimGreenCard, border = VictimGreenBorder,
                    icon = Icons.Default.Book, iconTint = Color(0xFF059669),
                    title = "First Aid Tips", subtitle = "Learn life-saving basics",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAssistant,
                )
                BePreparedCard(
                    bg = VictimBlueCard, border = VictimBlueBorder,
                    icon = Icons.Default.Groups, iconTint = Color(0xFF2563EB),
                    title = "Be a Responder", subtitle = "Help people in need",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToResponderProfile,
                )
                BePreparedCard(
                    bg = VictimPurpleCard, border = VictimPurpleBorder,
                    icon = Icons.Default.Timer, iconTint = Color(0xFF7C3AED),
                    title = "Emergency History", subtitle = "View past incidents",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (showBottomBar) {
            VictimBottomNavBar(
                selected = VictimNavTab.HOME,
                onSelect = {
                    when (it) {
                        VictimNavTab.HOME -> Unit
                        VictimNavTab.CHAT -> onNavigateToAssistant()
                        VictimNavTab.MAP -> onNavigateToMap()
                        VictimNavTab.PROFILE -> onNavigateToProfile()
                    }
                },
            )
        }
    }
}

@Composable
private fun HoldForSosButton(
    onSosTriggered: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val holdProgress = remember { Animatable(0f) }
    var isHolding by remember { mutableStateOf(false) }
    var hasTriggered by remember { mutableStateOf(false) }

    // 1. Idle Heartbeat Scale Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "SosHeartbeatEmission")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SosIdleScale"
    )

    // Rapid holding emission pulse for shockwaves
    val holdTransition = rememberInfiniteTransition(label = "SosHoldingEmission")
    val holdEmission by holdTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SosHoldEmission"
    )

    val pillShape = RoundedCornerShape(percent = 50)
    val effectiveScale = if (isHolding) {
        0.98f + (holdProgress.value * 0.04f)
    } else {
        idleScale
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(effectiveScale),
        contentAlignment = Alignment.Center
    ) {
        // Holding Emission Aura (pill-shaped shockwaves around the button when holding)
        if (isHolding) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val spread = holdEmission * 14.dp.toPx()
                val alpha = (1f - holdEmission) * 0.55f
                val strokeW = 2.5.dp.toPx()
                drawRoundRect(
                    color = VictimPrimary.copy(alpha = alpha),
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(size.height / 2f + spread, size.height / 2f + spread),
                    style = Stroke(width = strokeW)
                )
            }
        }

        // Core Interactive Pill SOS Button (which acts as a 2-second filler)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = if (isHolding) 14.dp else 6.dp,
                    shape = pillShape,
                    spotColor = Color(0xFFE52538),
                    ambientColor = Color(0x33E52538)
                )
                .clip(pillShape)
                .background(
                    // Dark crimson base track when holding, solid emergency red when idle
                    if (isHolding) Color(0xFF7F1D1D) else Color(0xFFDC2626)
                )
                .border(
                    width = 1.5.dp,
                    color = if (isHolding) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.35f),
                    shape = pillShape
                )
                .semantics {
                    role = Role.Button
                    onClick(label = "Request emergency help") { onSosTriggered(); true }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            hasTriggered = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val animationJob = coroutineScope.launch {
                                holdProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                                )
                                // Triggers automatically the instant 2000ms completes, even if the person has not let go!
                                if (isHolding && !hasTriggered) {
                                    hasTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSosTriggered()
                                }
                            }
                            tryAwaitRelease()
                            isHolding = false
                            // Fallback check in case release coincided with the 2-second completion
                            if (!hasTriggered && holdProgress.value >= 0.98f) {
                                hasTriggered = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSosTriggered()
                            }
                            animationJob.cancel()
                            coroutineScope.launch {
                                holdProgress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                )
                                hasTriggered = false
                            }
                        },
                        onTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
        ) {
            // Idle gradient fill
            if (!isHolding) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFE52538),
                                    Color(0xFFDC2626),
                                    Color(0xFFB91C1C)
                                )
                            )
                        )
                )
            }

            // The 2-Second Filler Layer (fills horizontally as holdProgress progresses from 0f to 1f)
            if (isHolding) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(holdProgress.value)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFDC2626),
                                    Color(0xFFFF334B),
                                    Color(0xFFFF6370)
                                )
                            )
                        )
                )
                // Bright glowing leading edge bar
                if (holdProgress.value in 0.02f..0.98f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(holdProgress.value),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.95f))
                        )
                    }
                }
            }

            // Foreground Content (Centrally Aligned Hero SOS Text, No Plus Icon)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val remainingSec = (2.0f - holdProgress.value * 2.0f).coerceAtLeast(0f)
                val mainText = if (hasTriggered) {
                    "SOS READY"
                } else if (isHolding && holdProgress.value >= 0.88f) {
                    "ACTIVATING SOS..."
                } else if (isHolding) {
                    "HOLDING FOR SOS"
                } else {
                    "HOLD FOR SOS"
                }

                val subText = if (hasTriggered) {
                    "Review and send your emergency"
                } else if (isHolding) {
                    String.format(Locale.US, "%.1fs remaining", remainingSec)
                } else {
                    "Hold for 2 seconds to activate"
                }

                Text(
                    text = mainText,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subText,
                    color = Color.White.copy(alpha = if (isHolding || hasTriggered) 0.95f else 0.88f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BePreparedCard(
    bg: Color,
    border: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.clip(VictimShapes.Card16).background(bg).border(1.dp, border, VictimShapes.Card16)
            .clickable { onClick() }.padding(12.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = subtitle, fontSize = 12.sp, color = VictimTextMuted, lineHeight = 16.sp)
    }
}
