package com.example.nearhelp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

import com.example.nearhelp.theme.ThemeManager

/**
 * 360° Interactive Radar Map Visualizer
 *
 * Implements the Calm Guardian State's central scanner as specified in design.md:
 * - 3 concentric distance rings (500m, 1.5km, 3km)
 * - Continuous 360° rotational gradient sweep beam cone (3.5s period)
 * - Pulsing central glass micro-card housing AI Voice SOS microphone
 * - Desaturated mint cartography underlay canvas with terrain and radial depth
 */
@Composable
fun GuardianRadarView(
    onVoiceSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "GuardianRadarSweep")
    val isDark = ThemeManager.isDarkMode

    // Continuous 360-degree rotation for radar beam
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepAngle"
    )

    // Pulsating radius expansion for radar wave
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarPulseScale"
    )

    val ringColor = if (isDark) Color(0x6634D399) else Color(0x4D48BB78)
    val mapColor = if (isDark) Color(0x2634D399) else Color(0x1834C759)
    val streetColor = if (isDark) Color(0x3334D399) else Color(0x2034C759)
    val echoColor = if (isDark) Color(0x3834D399) else Color(0x2634C759)
    val micBgColor = if (isDark) Color(0xFF0F172A) else Color(0xF5FFFFFF)
    val micIconTint = if (isDark) Color(0xFF34D399) else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .size(300.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Base Canvas for Desaturated Cartography, Range Circles & Sweep Cone
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // 1. Subtle Cartography Topo Underlay
            val mapPath = Path().apply {
                moveTo(center.x - maxRadius * 0.8f, center.y - maxRadius * 0.3f)
                quadraticTo(center.x - maxRadius * 0.2f, center.y - maxRadius * 0.7f, center.x + maxRadius * 0.4f, center.y - maxRadius * 0.4f)
                quadraticTo(center.x + maxRadius * 0.8f, center.y + maxRadius * 0.2f, center.x + maxRadius * 0.3f, center.y + maxRadius * 0.7f)
                quadraticTo(center.x - maxRadius * 0.4f, center.y + maxRadius * 0.8f, center.x - maxRadius * 0.8f, center.y - maxRadius * 0.3f)
                close()
            }
            drawPath(
                path = mapPath,
                color = mapColor
            )

            // Street lines underlay
            drawLine(
                color = streetColor,
                start = Offset(center.x - maxRadius * 0.85f, center.y + maxRadius * 0.2f),
                end = Offset(center.x + maxRadius * 0.85f, center.y - maxRadius * 0.3f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = streetColor,
                start = Offset(center.x - maxRadius * 0.3f, center.y - maxRadius * 0.85f),
                end = Offset(center.x + maxRadius * 0.4f, center.y + maxRadius * 0.85f),
                strokeWidth = 2f
            )

            // 2. Concentric Range Circles (500m, 1.5km, 3km)
            val rings = listOf(0.35f, 0.65f, 0.95f)
            rings.forEach { ratio ->
                drawCircle(
                    color = ringColor,
                    radius = maxRadius * ratio,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Pulsing Range Echo Ring
            drawCircle(
                color = echoColor,
                radius = maxRadius * 0.65f * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Radar Sweep Beam Cone
            rotate(degrees = angle, pivot = center) {
                val sweepBrush = Brush.sweepGradient(
                    colors = if (isDark) {
                        listOf(
                            Color.Transparent,
                            Color(0x0834D399),
                            Color(0x3034D399),
                            Color(0x9510B981)
                        )
                    } else {
                        listOf(
                            Color.Transparent,
                            Color(0x0534C759),
                            Color(0x2034C759),
                            Color(0x7534C759)
                        )
                    },
                    center = center
                )
                drawCircle(
                    brush = sweepBrush,
                    radius = maxRadius * 0.95f,
                    center = center
                )
            }
        }

        // Center Pulsing Glass Micro-Card with Voice SOS Microphone
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = if (isDark) Color(0x66000000) else Color(0x2E000000),
                    spotColor = Color(0x5534D399)
                )
                .background(micBgColor, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onVoiceSosClick()
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "AI Voice SOS Triage",
                tint = micIconTint,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
