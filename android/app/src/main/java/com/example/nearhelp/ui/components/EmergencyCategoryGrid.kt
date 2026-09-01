package com.example.nearhelp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmergencyCategory(
    val id: String,
    val label: String,
    val emoji: String
)

val EmergencyCategories = listOf(
    EmergencyCategory("medical", "Medical", "🩺"),
    EmergencyCategory("police", "Police", "👮"),
    EmergencyCategory("fire", "Fire", "🔥"),
    EmergencyCategory("accident", "Accident", "🚗"),
    EmergencyCategory("robbery", "Robbery", "🥷"),
    EmergencyCategory("kidnapping", "Kidnapping", "🏃"),
    EmergencyCategory("gas_leak", "Gas Leak", "⚠️"),
    EmergencyCategory("flood", "Flood", "🌊"),
    EmergencyCategory("earthquake", "Earthquake", "🏚️"),
    EmergencyCategory("tsunami", "Tsunami", "🌊"),
    EmergencyCategory("power_out", "Power out", "⚡"),
    EmergencyCategory("structural", "Structural", "🏢"),
    EmergencyCategory("hazmat", "Hazmat", "🧪"),
    EmergencyCategory("wildfire", "Wildfire", "🌲"),
    EmergencyCategory("weather", "Weather", "⛈️"),
    EmergencyCategory("cyber", "Cyber", "🔒")
)

/**
 * 16-Category Emergency Matrix (4x4 Responsive Grid)
 *
 * Implements the tactile neomorphic matrix specified in design.md:
 * - 4x4 layout of 16 crisis types
 * - Default: White neomorphic card, subtle drop shadow
 * - Selected: Full background animation to Emergency Crimson (#E52538), white icon circle, white bold label, 1.04x scale
 */
@Composable
fun EmergencyCategoryGrid(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Organize into 4 rows of 4 items for crisp, clean layout
    val rows = EmergencyCategories.chunked(4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2E7EB))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { category ->
                    val isSelected = category.id.equals(selectedCategoryId, ignoreCase = true)

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFE52538) else Color.White,
                        animationSpec = spring(),
                        label = "CategoryBgColor"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0xFF334155),
                        animationSpec = spring(),
                        label = "CategoryTextColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "CategoryScale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .aspectRatio(0.92f)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = if (isSelected) Color(0x40E52538) else Color(0x18000000)
                            )
                            .background(bgColor, RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCategorySelected(category.id)
                            }
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon Container Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    color = if (isSelected) Color(0x33FFFFFF) else Color(0xFFF1F5F9),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.emoji,
                                fontSize = 17.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = category.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
