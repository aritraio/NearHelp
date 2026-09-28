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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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

/**
 * Unified NearHelp top bar reproducing the polished Home Screen header lockup across all screens:
 * - Left: "NearHelp" dual-tone wordmark (custom typography) + "Connect. Respond. Save time." subtitle
 * - Right: Circular avatar button ("A") or optional custom trailing content
 */
@Composable
fun NearHelpTopBar(
    modifier: Modifier = Modifier,
    tagline: String = "Connect. Respond. Save time.",
    avatarInitial: String = "A",
    onAvatarClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            NearHelpWordmark(fontSize = 30)
            Text(
                text = tagline,
                fontSize = 12.5.sp,
                color = VictimTextMuted,
            )
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EDF3))
                    .then(
                        if (onAvatarClick != null) Modifier.clickable { onAvatarClick() }
                        else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarInitial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = VictimTextDark,
                )
            }
        }
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
    val userPosition = remember(latitude, longitude) { LatLng(latitude, longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userPosition, 15f)
    }

    LaunchedEffect(latitude, longitude) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(userPosition, 15f),
            durationMs = 600
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true,
        )
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL,
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color(0xFFEDF3F7))
            .border(1.dp, VictimBorder, VictimShapes.Card20),
    ) {
        // 1. Live Google Map View
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = {
                onMapClick?.invoke()
            }
        ) {
            // SOS Emergency Radial Dispatch Zone (Red Translucent Circle)
            Circle(
                center = userPosition,
                radius = 500.0,
                fillColor = Color(0x24DC2626),
                strokeColor = Color(0xFFDC2626),
                strokeWidth = 3f,
            )

            // Current Victim Location Pin
            Marker(
                state = MarkerState(position = userPosition),
                title = "Your Location",
                snippet = coordinatesText ?: String.format(Locale.US, "%.5f, %.5f", latitude, longitude),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
            )
        }

        // 2. Top Status Overlay: "LIVE MAP" + Coordinates + Zoom Telemetry
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
                text = String.format(Locale.US, "• %.1fx", cameraPositionState.position.zoom / 15f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = VictimPrimary
            )
        }

        // 3. Top-Right: Recenter Button + Open Full Map Button
        Row(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recenter to Current Location button
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, VictimPrimary.copy(alpha = 0.45f), RoundedCornerShape(100.dp))
                    .clickable {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(userPosition, 15f),
                                500
                            )
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

        // 4. Floating Interactive Zoom Controls (+ and −) on Right Center
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
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 250)
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
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 250)
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
    val tabs = remember { listOf(VictimNavTab.HOME, VictimNavTab.CHAT, VictimNavTab.MAP, VictimNavTab.PROFILE) }
    val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)

    // Smooth gliding pill indicator across tabs with organic spring dynamics
    val animatedTabIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_pill_glide"
    )

    Surface(
        color = VictimBackground,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 6.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
        ) {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / tabs.size
            val pillWidth = 58.dp
            val pillHeight = 32.dp

            // Active Sliding Capsule (Visual Guide)
            val indicatorLeft = (tabWidth * animatedTabIndex) + (tabWidth - pillWidth) / 2
            Box(
                modifier = Modifier
                    .offset(x = indicatorLeft, y = 2.dp)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VictimPinkCard)
                    .border(0.5.dp, VictimPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == selected
                    VictimNavItem(
                        label = when (tab) {
                            VictimNavTab.HOME -> "Home"
                            VictimNavTab.CHAT -> "Chat"
                            VictimNavTab.MAP -> "Map"
                            VictimNavTab.PROFILE -> "Profile"
                        },
                        icon = when (tab) {
                            VictimNavTab.HOME -> Icons.Default.Home
                            VictimNavTab.CHAT -> Icons.Default.ChatBubbleOutline
                            VictimNavTab.MAP -> Icons.Default.Map
                            VictimNavTab.PROFILE -> Icons.Default.Person
                        },
                        isSelected = isSelected,
                        onClick = { onSelect(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
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

    // Smooth color transition
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) VictimPrimary else VictimTextMuted,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "nav_item_color"
    )

    // Bouncy pop-in / pop-out icon scale
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_icon_scale"
    )

    // Active vertical lift
    val iconElevation by animateDpAsState(
        targetValue = if (isSelected) (-2.5).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_icon_elevation"
    )

    // Active indicator dot alpha and scale
    val dotScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_dot_scale"
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
            .padding(vertical = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        translationY = iconElevation.toPx()
                    },
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Micro-indicator dot
        Box(
            modifier = Modifier
                .size(3.5.dp)
                .graphicsLayer {
                    scaleX = dotScale
                    scaleY = dotScale
                    alpha = dotScale
                }
                .clip(CircleShape)
                .background(VictimPrimary)
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
