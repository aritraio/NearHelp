package com.example.nearhelp.ui.victim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.StatusLiveRed
import com.example.nearhelp.theme.StatusSafeGreen
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimDivider
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted

object VictimShapes {
    val Card16 = RoundedCornerShape(16.dp)
    val Card20 = RoundedCornerShape(20.dp)
    val Card24 = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(100.dp)
}

@Composable
fun NearHelpWordmark(
    fontSize: Int = 30,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = VictimTextDark)) { append("Near") }
            withStyle(SpanStyle(color = VictimPrimary)) { append("Help") }
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = (-0.5).sp,
        ),
        modifier = modifier,
    )
}

@Composable
fun NearHelpLogoMark(
    size: Int = 56,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "NearHelp heart",
            tint = VictimPrimary,
            modifier = Modifier.size(size.dp),
        )
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}

@Composable
fun NearHelpBrandHeader(
    tagline: String = "Connect. Respond. Save time.",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        NearHelpLogoMark(size = 72)
        Spacer(modifier = Modifier.height(8.dp))
        NearHelpWordmark(fontSize = 38)
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = VictimTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VictimPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VictimPrimary,
            contentColor = Color.White,
            disabledContainerColor = VictimBorder,
            disabledContentColor = VictimTextMuted,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            ),
        )
    }
}

@Composable
fun VictimOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, VictimPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = VictimPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun VictimLocationCard(
    locationTitle: String = "Kolkata, West Bengal",
    accuracyText: String = "Accuracy: ±12 m • Updated now",
    actionLabel: String = "Edit",
    actionIcon: ImageVector = Icons.Default.Edit,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color.White)
            .border(1.dp, VictimBorder, VictimShapes.Card20)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = VictimPrimary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Your location", fontSize = 12.sp, color = VictimTextMuted)
            Text(
                text = locationTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VictimTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = accuracyText,
                fontSize = 12.sp,
                color = VictimTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, VictimBorder, RoundedCornerShape(12.dp))
                .clickable { onAction() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = VictimTextDark,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
            }
        }
    }
}

@Composable
fun LiveMapFeedCard(
    latitude: Double = 22.5726,
    longitude: Double = 88.3639,
    coordinatesText: String? = null,
    onMapClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val zoomAnim = remember { Animatable(1f) }
    val panXAnim = remember { Animatable(0f) }
    val panYAnim = remember { Animatable(0f) }

    // Subtle localized pulse for current location blue dot
    val infiniteTransition = rememberInfiniteTransition(label = "LocationDotPulse")
    val dotPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotPulseProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color(0xFFEDF3F7))
            .border(1.dp, VictimBorder, VictimShapes.Card20),
    ) {
        // 1. Interactive Dynamic Cartography Canvas with Pinch & Drag Gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var zoomAcc = 1f
                        var panAcc = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop

                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoomAcc *= zoomChange
                                panAcc += panChange
                                val panMotion = panAcc.getDistance()
                                val zoomMotion = abs(1f - zoomAcc)
                                if (panMotion > touchSlop || zoomMotion > 0.04f) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                                coroutineScope.launch {
                                    val currentZoom = zoomAnim.value
                                    val newZoom = (currentZoom * zoomChange).coerceIn(0.6f, 3.5f)
                                    zoomAnim.snapTo(newZoom)
                                    val maxPanX = 1400f * newZoom.coerceAtLeast(1f)
                                    val maxPanY = 1200f * newZoom.coerceAtLeast(1f)
                                    panXAnim.snapTo((panXAnim.value + panChange.x).coerceIn(-maxPanX, maxPanX))
                                    panYAnim.snapTo((panYAnim.value + panChange.y).coerceIn(-maxPanY, maxPanY))
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            coroutineScope.launch {
                                val targetZoom = if (zoomAnim.value < 1.6f) 2.2f else 1f
                                zoomAnim.animateTo(targetZoom, tween(300, easing = FastOutSlowInEasing))
                                if (targetZoom == 1f) {
                                    launch { panXAnim.animateTo(0f, tween(300)) }
                                    launch { panYAnim.animateTo(0f, tween(300)) }
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomAnim.value
                        scaleY = zoomAnim.value
                        translationX = panXAnim.value
                        translationY = panYAnim.value
                        transformOrigin = TransformOrigin(0.50f, 0.38f)
                    }
            ) {
                val w = size.width
                val h = size.height
                val userLocation = Offset(w * 0.50f, h * 0.38f)

                // 1. Waterway / River Path (expanded boundaries so it flows endlessly on pan)
                val riverPath = Path().apply {
                    moveTo(w * 0.14f, -h * 1.2f)
                    cubicTo(
                        w * 0.20f, h * 0.35f,
                        w * 0.07f, h * 0.65f,
                        w * 0.26f, h * 2.2f
                    )
                    lineTo(w * 0.38f, h * 2.2f)
                    cubicTo(
                        w * 0.17f, h * 0.65f,
                        w * 0.30f, h * 0.35f,
                        w * 0.24f, -h * 1.2f
                    )
                    close()
                }
                drawPath(
                    path = riverPath,
                    color = Color(0xFFD3E4EF)
                )

                // 2. Secondary Street Grid lines (continuous regional grid spanning -w to 2w, -h to 2h)
                val gridColor = Color(0xFFDEE8F0)
                val streetStroke = 1.5.dp.toPx()
                val step = 40.dp.toPx()
                var x = -w * 1.2f
                while (x < w * 2.2f) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, -h * 1.2f),
                        end = Offset(x + h * 0.45f, h * 2.2f),
                        strokeWidth = streetStroke
                    )
                    x += step
                }
                var y = -h * 1.2f
                while (y < h * 2.2f) {
                    drawLine(
                        color = gridColor,
                        start = Offset(-w * 1.2f, y),
                        end = Offset(w * 2.2f, y - w * 0.25f),
                        strokeWidth = streetStroke
                    )
                    y += step
                }

                // 3. Major Arteries & Expressways (cross-regional)
                val majorRoadColor = Color(0xFFCADAE5)
                val majorStroke = 3.5.dp.toPx()
                // Horizontal arterial avenue passing through junction
                drawLine(
                    color = majorRoadColor,
                    start = Offset(-w * 1.2f, userLocation.y),
                    end = Offset(w * 2.2f, userLocation.y - 18.dp.toPx()),
                    strokeWidth = majorStroke
                )
                // Diagonal arterial expressway
                drawLine(
                    color = majorRoadColor,
                    start = Offset(-w * 0.5f, -h * 1.2f),
                    end = Offset(w * 1.8f, h * 2.2f),
                    strokeWidth = majorStroke
                )
                // Secondary cross-avenue
                drawLine(
                    color = majorRoadColor,
                    start = Offset(userLocation.x - 65.dp.toPx(), -h * 1.2f),
                    end = Offset(userLocation.x - 65.dp.toPx(), h * 2.2f),
                    strokeWidth = 2.5.dp.toPx()
                )
                // Central Junction Roundabout at user's location
                drawCircle(
                    color = majorRoadColor,
                    radius = 26.dp.toPx(),
                    center = userLocation,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 4. Nearby Responder / Emergency Facility Markers
                // Responder 1 (North-East - CPR Verified)
                val resp1Offset = Offset(userLocation.x + 85.dp.toPx(), userLocation.y - 50.dp.toPx())
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 4.5.dp.toPx(),
                    center = resp1Offset
                )
                drawCircle(
                    color = Color(0x3510B981),
                    radius = 9.dp.toPx(),
                    center = resp1Offset
                )

                // Responder 2 (West - First Aider)
                val resp2Offset = Offset(userLocation.x - 75.dp.toPx(), userLocation.y + 40.dp.toPx())
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 4.dp.toPx(),
                    center = resp2Offset
                )
                drawCircle(
                    color = Color(0x300284C7),
                    radius = 8.dp.toPx(),
                    center = resp2Offset
                )

                // Responder 3 (South-East - EMT)
                val resp3Offset = Offset(userLocation.x + 130.dp.toPx(), userLocation.y + 110.dp.toPx())
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 4.dp.toPx(),
                    center = resp3Offset
                )

                // Hospital 1: AMRI Trauma Care (East)
                val hospOffset = Offset(userLocation.x + 95.dp.toPx(), userLocation.y + 55.dp.toPx())
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 5.dp.toPx(),
                    center = hospOffset
                )
                drawCircle(
                    color = Color(0x30EF4444),
                    radius = 9.dp.toPx(),
                    center = hospOffset
                )

                // Hospital 2: Apollo Multispecialty (North-West)
                val hosp2Offset = Offset(userLocation.x - 110.dp.toPx(), userLocation.y - 70.dp.toPx())
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 5.dp.toPx(),
                    center = hosp2Offset
                )

                // 5. CURRENT LOCATION SPOTTED AS A BLUE DOT (Iconic, tight & unobtrusive animation)
                val pulseR = 12.dp.toPx() + (dotPulseProgress * 12.dp.toPx())
                val pulseAlpha = (1f - dotPulseProgress) * 0.35f
                drawCircle(
                    color = Color(0xFF3B82F6).copy(alpha = pulseAlpha),
                    radius = pulseR,
                    center = userLocation
                )
                drawCircle(
                    color = Color(0xFF2563EB).copy(alpha = pulseAlpha * 0.7f),
                    radius = pulseR,
                    center = userLocation,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Crisp White Ring for separation against map
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = userLocation
                )
                // Solid Vibrant Blue Core (Google Maps standard #1D4ED8 / #2563EB)
                drawCircle(
                    color = Color(0xFF1D4ED8),
                    radius = 6.5.dp.toPx(),
                    center = userLocation
                )
                // Tiny Specular Light Reflection
                drawCircle(
                    color = Color(0xFFBFDBFE),
                    radius = 1.8.dp.toPx(),
                    center = Offset(userLocation.x - 1.5.dp.toPx(), userLocation.y - 1.5.dp.toPx())
                )
            }
        }

        // Top Status Overlay: "LIVE MAP FEED" + Coordinates + Zoom Telemetry
        Row(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.94f))
                .border(1.dp, VictimBorder, RoundedCornerShape(100.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "LIVE MAP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VictimTextDark,
                letterSpacing = 0.3.sp
            )
            val coordDisplay = coordinatesText ?: String.format(Locale.US, "%.4f° N, %.4f° E", latitude, longitude)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "• $coordDisplay",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = VictimTextMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format(Locale.US, "• %.1fx", zoomAnim.value),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = VictimPrimary
            )
        }

        // Top-Right: Recenter Button (when panned/zoomed) + Open Full Map Button
        Row(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recenter to Current Location button (Highlights when panned/zoomed)
            val isPannedOrZoomed = Math.abs(zoomAnim.value - 1f) > 0.05f || Math.abs(panXAnim.value) > 10f || Math.abs(panYAnim.value) > 10f
            if (isPannedOrZoomed) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .border(1.dp, VictimPrimary.copy(alpha = 0.45f), RoundedCornerShape(100.dp))
                        .clickable {
                            coroutineScope.launch {
                                launch { zoomAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
                                launch { panXAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                                launch { panYAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                            }
                        }
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = VictimPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recenter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VictimPrimary
                        )
                    }
                }
            }

            if (onMapClick != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.94f))
                        .border(1.dp, VictimBorder, CircleShape)
                        .clickable { onMapClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open Full Map",
                        tint = VictimTextDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Floating Interactive Zoom Controls (+ and −) on Right Center
        Column(
            modifier = Modifier
                .padding(end = 12.dp, bottom = 40.dp)
                .align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom In (+)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, VictimBorder, CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            val target = (zoomAnim.value + 0.35f).coerceAtMost(3.5f)
                            zoomAnim.animateTo(target, tween(250, easing = FastOutSlowInEasing))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = VictimTextDark,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Zoom Out (−)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, VictimBorder, CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            val target = (zoomAnim.value - 0.35f).coerceAtLeast(0.6f)
                            zoomAnim.animateTo(target, tween(250, easing = FastOutSlowInEasing))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = VictimTextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Content slot (e.g. Pill HoldForSosButton aligned at BottomCenter)
        content()
    }
}

@Composable
fun StatusBadge(
    text: String,
    live: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val bg = if (live) VictimPinkCard else Color(0xFFECFDF5)
    val dot = if (live) StatusLiveRed else StatusSafeGreen
    val fg = if (live) VictimPrimary else StatusSafeGreen
    Row(
        modifier = modifier
            .clip(VictimShapes.Pill)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(9.dp).background(dot, CircleShape))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun PastelInfoCard(
    cardBg: Color,
    cardBorder: Color,
    title: String,
    subtitle: String,
    titleColor: Color = VictimTextDark,
    icon: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickableMod = if (onClick != null) modifier.clickable { onClick() } else modifier
    Row(
        modifier = clickableMod
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(cardBg)
            .border(1.dp, cardBorder, VictimShapes.Card20)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = VictimTextMuted, lineHeight = 18.sp)
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = VictimPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        if (actionLabel != null) {
            Text(
                text = "$actionLabel →",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = VictimPrimary,
                modifier = if (onAction != null) Modifier.clickable { onAction() } else Modifier,
            )
        }
    }
}

enum class VictimNavTab { HOME, CHAT, MAP, PROFILE }

@Composable
fun VictimBottomNavBar(
    selected: VictimNavTab,
    onSelect: (VictimNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = VictimBackground,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 6.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VictimNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = selected == VictimNavTab.HOME,
                onClick = { onSelect(VictimNavTab.HOME) },
            )
            VictimNavItem(
                label = "Chat",
                icon = Icons.Default.ChatBubbleOutline,
                isSelected = selected == VictimNavTab.CHAT,
                onClick = { onSelect(VictimNavTab.CHAT) },
            )
            VictimNavItem(
                label = "Map",
                icon = Icons.Default.Map,
                isSelected = selected == VictimNavTab.MAP,
                onClick = { onSelect(VictimNavTab.MAP) },
            )
            VictimNavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                isSelected = selected == VictimNavTab.PROFILE,
                onClick = { onSelect(VictimNavTab.PROFILE) },
            )
        }
    }
}

@Composable
private fun VictimNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) VictimPrimary else VictimTextMuted,
        animationSpec = tween(durationMillis = 40, easing = androidx.compose.animation.core.LinearEasing),
        label = "nav_item_color"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 40, easing = androidx.compose.animation.core.LinearEasing),
        label = "nav_pill_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = VictimPrimary.copy(alpha = 0.12f)),
            ) {
                if (!isSelected) {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick()
                }
            }
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(VictimPinkCard.copy(alpha = pillAlpha))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
        )
    }
}

@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color(0xFFE8EEF3))
            .border(1.dp, VictimBorder, VictimShapes.Card20),
        contentAlignment = Alignment.Center,
    ) {
        // Faint grid streets suggestion
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Kolkata • Howrah Bridge", fontSize = 12.sp, color = VictimTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Victoria Memorial", fontSize = 12.sp, color = VictimTextMuted)
        }
        if (content != null) content()
    }
}

@Composable
fun VictimDividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(VictimDivider.copy(alpha = 0.6f)),
    )
}
