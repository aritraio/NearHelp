package com.example.nearhelp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimGreenCard
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.VictimOutlineButton
import com.example.nearhelp.ui.victim.VictimShapes
import kotlinx.coroutines.delay

enum class CrisisTab { COMMUNITY, SHARING, MESSAGE, ALERT }

/**
 * Victim SOS dispatch — light bottom-sheet style (mockup 09_38_14):
 * Medical Emergency header, Type/Speak/Camera pastel cards, location rows,
 * 5s auto-send progress, Cancel. Keeps legacy signature for Navigation.
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
    var progress by remember { mutableFloatStateOf(0f) }
    var activeTab by remember { mutableStateOf(CrisisTab.COMMUNITY) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        repeat(50) {
            delay(100)
            progress = (it + 1) / 50f
        }
        onDispatch("medical", "Kolkata, West Bengal")
    }

    Column(modifier = modifier.fillMaxSize().background(VictimBackground)) {
        // Legacy segmented pills preserved as compact top tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(100.dp)).background(Color.White)
                .border(1.dp, VictimBorder, RoundedCornerShape(100.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentedPill(icon = Icons.Default.Groups, label = "Community", isSelected = activeTab == CrisisTab.COMMUNITY, onClick = { activeTab = CrisisTab.COMMUNITY; onNavigateToMap() })
            SegmentedPill(icon = Icons.Default.Link, label = "Sharing", isSelected = activeTab == CrisisTab.SHARING, onClick = { activeTab = CrisisTab.SHARING })
            SegmentedPill(icon = Icons.AutoMirrored.Filled.Chat, label = "Message", isSelected = activeTab == CrisisTab.MESSAGE, onClick = { activeTab = CrisisTab.MESSAGE; onNavigateToAssistant() })
            SegmentedPill(icon = Icons.Default.NotificationsActive, label = "Alert", isSelected = activeTab == CrisisTab.ALERT, onClick = { activeTab = CrisisTab.ALERT })
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .border(1.dp, VictimBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 44.dp, height = 5.dp).clip(CircleShape).background(VictimBorder))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(VictimPinkCard), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Medical Emergency", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
                    Text(text = "How can you describe it?", fontSize = 14.sp, color = VictimTextMuted)
                }
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFF1F5F9)).clickable { onCancel() }, contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = VictimTextDark, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DispatchOption(bg = VictimBlueCard, icon = Icons.Default.Notes, tint = Color(0xFF2563EB), title = "Type", subtitle = "Describe what happened", modifier = Modifier.weight(1f))
                DispatchOption(bg = VictimGreenCard, icon = Icons.Default.Mic, tint = Color(0xFF059669), title = "Speak", subtitle = "Tell us using voice", modifier = Modifier.weight(1f))
                DispatchOption(bg = VictimPurpleCard, icon = Icons.Default.CameraAlt, tint = Color(0xFF7C3AED), title = "Camera", subtitle = "Take a photo or video", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White)
                    .border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Your location", fontSize = 12.sp, color = VictimTextMuted)
                    Text(text = "Kolkata, West Bengal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
                    Text(text = "Accuracy: ±12 m • GPS detected", fontSize = 12.sp, color = VictimTextMuted)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(15.dp), tint = VictimTextDark)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Use Current", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color(0xFFF8FAFC))
                    .border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Enter location manually", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VictimTextDark, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimPinkCard).border(1.dp, VictimPinkBorder, VictimShapes.Card16).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Auto-send in 5 seconds", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "If no additional details are provided, we'll send a basic medical emergency alert with your location.", fontSize = 13.sp, color = VictimTextMuted, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = VictimPrimary, trackColor = VictimTextMuted.copy(alpha = 0.2f))
            }
            Spacer(modifier = Modifier.height(14.dp))
            VictimOutlineButton(text = "Cancel", onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCancel()
            })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DispatchOption(bg: Color, icon: ImageVector, tint: Color, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clip(VictimShapes.Card16).background(bg).padding(vertical = 16.dp, horizontal = 8.dp)) {
        Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 12.sp, color = VictimTextMuted, lineHeight = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SegmentedPill(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) VictimPinkCard else Color.Transparent,
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) VictimPrimary else VictimTextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) VictimPrimary else VictimTextMuted)
        }
    }
}
