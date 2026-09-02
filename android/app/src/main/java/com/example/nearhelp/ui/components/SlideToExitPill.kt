package com.example.nearhelp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.ThemeManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Slide-To-Exit Pill Slider
 *
 * Implements the Guardian disarm gesture:
 * - Translucent glass pill (Light: `#FFFFFFCC`, Dark: `#1E293B`)
 * - Circular thumb button containing `>`
 * - Horizontal swipe gesture with spring snap-back physics
 */
@Composable
fun SlideToExitPill(
    onExit: () -> Unit,
    modifier: Modifier = Modifier.width(240.dp),
    label: String = "Slide to exit"
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val isDark = ThemeManager.isDarkMode

    val pillBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E293B) else Color(0xCCFFFFFF),
        label = "SlidePillBg"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
        label = "SlidePillLabel"
    )
    val thumbBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF10B981) else Color(0xFF0F172A),
        label = "SlidePillThumbBg"
    )
    val thumbIconColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF061A13) else Color.White,
        label = "SlidePillThumbIcon"
    )

    BoxWithConstraints(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(100.dp),
                ambientColor = if (isDark) Color(0x33000000) else Color(0x18000000)
            )
            .background(pillBgColor, RoundedCornerShape(100.dp))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val thumbSizePx = with(density) { 42.dp.toPx() }
        val maxDragPx = with(density) { maxWidth.toPx() } - thumbSizePx - with(density) { 8.dp.toPx() }

        // Centered Label
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }

        // Draggable Thumb Button
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .size(44.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x33000000)
                )
                .background(thumbBgColor, CircleShape)
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (dragOffset.value >= maxDragPx * 0.75f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dragOffset.animateTo(
                                        targetValue = maxDragPx,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                    onExit()
                                    dragOffset.snapTo(0f)
                                } else {
                                    dragOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.7f,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (dragOffset.value + dragAmount).coerceIn(0f, maxDragPx)
                            coroutineScope.launch {
                                dragOffset.snapTo(newOffset)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Slide to exit thumb",
                tint = thumbIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
