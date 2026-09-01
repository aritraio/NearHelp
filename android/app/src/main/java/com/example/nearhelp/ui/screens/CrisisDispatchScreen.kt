package com.example.nearhelp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.CoralActive
import com.example.nearhelp.theme.CrisisSurfaceBg
import com.example.nearhelp.theme.EmergencyCrimson
import com.example.nearhelp.ui.components.AIDisclaimerBanner
import com.example.nearhelp.ui.components.AddressConfirmCard
import com.example.nearhelp.ui.components.CountdownDispatchSlider
import com.example.nearhelp.ui.components.EmergencyCategoryGrid

enum class CrisisTab {
    COMMUNITY,
    SHARING,
    MESSAGE,
    ALERT
}

/**
 * Screen 2: Crisis Dispatch & Category Matrix (The High-Urgency Crisis State)
 *
 * Implements the right flagship screen from design.md & reference visual artifact:
 * - Clean slate surface (#EFF3F6)
 * - Top Segmented Navigation Pills (Community, Sharing, Message, Alert)
 * - Address Verification & Action Card (1234 Mission St)
 * - The 16-Category Emergency Matrix (4x4 Responsive Grid)
 * - Bottom Dual-Action Countdown Dispatch Slider (Cancel • 3 • Send SOS)
 * - Legal Immunity & Good Samaritan Compliance Banner
 */
@Composable
fun CrisisDispatchScreen(
    onCancel: () -> Unit,
    onDispatch: (String, String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: String = "robbery",
    defaultStreetAddress: String = "1234 Mission St",
    defaultSubAddress: String = "Apt #345B, 27th Floor • San Francisco, CA"
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var activeTab by remember { mutableStateOf(CrisisTab.COMMUNITY) }
    var streetAddress by remember { mutableStateOf(defaultStreetAddress) }
    var subAddress by remember { mutableStateOf(defaultSubAddress) }
    var isAddressConfirmed by remember { mutableStateOf(false) }
    var showAddressEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CrisisSurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Segmented Navigation Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SegmentedPill(
                    icon = Icons.Default.Groups,
                    label = "Community",
                    isSelected = activeTab == CrisisTab.COMMUNITY,
                    onClick = {
                        activeTab = CrisisTab.COMMUNITY
                        onNavigateToMap()
                    }
                )
                SegmentedPill(
                    icon = Icons.Default.Link,
                    label = "Sharing",
                    isSelected = activeTab == CrisisTab.SHARING,
                    onClick = { activeTab = CrisisTab.SHARING }
                )
                SegmentedPill(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Message",
                    isSelected = activeTab == CrisisTab.MESSAGE,
                    onClick = {
                        activeTab = CrisisTab.MESSAGE
                        onNavigateToAssistant()
                    }
                )
                SegmentedPill(
                    icon = Icons.Default.NotificationsActive,
                    label = "Alert",
                    isSelected = activeTab == CrisisTab.ALERT,
                    onClick = { activeTab = CrisisTab.ALERT }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Address Verification & Action Card
            AddressConfirmCard(
                streetAddress = streetAddress,
                subAddress = subAddress,
                isConfirmed = isAddressConfirmed,
                onEditAddressClick = { showAddressEditDialog = true },
                onConfirmClick = {
                    isAddressConfirmed = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. The 16-Category Emergency Matrix (4x4 Grid)
            EmergencyCategoryGrid(
                selectedCategoryId = selectedCategory,
                onCategorySelected = { categoryId ->
                    selectedCategory = categoryId
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Bottom Dual-Action Countdown Dispatch Slider
            CountdownDispatchSlider(
                onCancel = onCancel,
                onDispatch = {
                    onDispatch(selectedCategory, streetAddress)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Legal Immunity Banner
            AIDisclaimerBanner()

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Edit Address Dialog Sheet
        if (showAddressEditDialog) {
            var tempStreet by remember { mutableStateOf(streetAddress) }
            var tempSub by remember { mutableStateOf(subAddress) }

            AlertDialog(
                onDismissRequest = { showAddressEditDialog = false },
                title = {
                    Text(
                        text = "Edit Incident Pinpoint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempStreet,
                            onValueChange = { tempStreet = it },
                            label = { Text("Street Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmergencyCrimson
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempSub,
                            onValueChange = { tempSub = it },
                            label = { Text("Apt / Floor / Landmark") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmergencyCrimson
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            streetAddress = tempStreet
                            subAddress = tempSub
                            isAddressConfirmed = true
                            showAddressEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyCrimson)
                    ) {
                        Text("Save & Pinpoint", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddressEditDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

@Composable
private fun SegmentedPill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Color(0xFFFFF0F2) else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CoralActive else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CoralActive else Color(0xFF64748B)
            )
        }
    }
}
