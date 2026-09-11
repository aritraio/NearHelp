package com.example.nearhelp.ui.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.StatusLiveRed
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimGreenBorder
import com.example.nearhelp.theme.VictimGreenCard
import com.example.nearhelp.theme.VictimOrangeBorder
import com.example.nearhelp.theme.VictimOrangeCard
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimPurpleBorder
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.victim.MapPlaceholder
import com.example.nearhelp.ui.victim.NearHelpWordmark
import com.example.nearhelp.ui.victim.PastelInfoCard
import com.example.nearhelp.ui.victim.SectionHeader
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimLocationCard
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimOutlineButton
import com.example.nearhelp.ui.victim.VictimShapes
import kotlinx.coroutines.delay

private enum class VictimHomeState { HOME, SOS_SHEET, FINDING }

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(VictimHomeState.HOME) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier.fillMaxSize().background(VictimBackground)) {
        when (state) {
            VictimHomeState.HOME -> VictimHomeContent(
                onSosTap = { state = VictimHomeState.SOS_SHEET },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToMap = onNavigateToMap,
                onNavigateToAssistant = onNavigateToAssistant,
                onNavigateToTracking = onNavigateToTracking,
            )
            VictimHomeState.SOS_SHEET -> SosDetailSheet(
                onCancel = { state = VictimHomeState.HOME },
                onAutoSend = { state = VictimHomeState.FINDING },
            )
            VictimHomeState.FINDING -> FindingRespondersContent(
                onCancel = { state = VictimHomeState.HOME },
                onNavigateToTracking = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToTracking()
                },
                onNavigateToAssistant = onNavigateToAssistant,
            )
        }
    }
}

@Composable
private fun VictimHomeContent(
    onSosTap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToTracking: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: NearHelp + avatar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    NearHelpWordmark(fontSize = 30)
                    Text(text = "Connect. Respond. Save time.", fontSize = 12.5.sp, color = VictimTextMuted)
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8EDF3)).clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center,
                ) { Text(text = "A", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VictimTextDark) }
            }

            VictimLocationCard()

            // Map + big SOS circle
            MapPlaceholder(modifier = Modifier.height(300.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    // Pulse rings
                    Box(modifier = Modifier.size(252.dp).clip(CircleShape).background(VictimPrimary.copy(alpha = 0.12f)))
                    Box(modifier = Modifier.size(216.dp).clip(CircleShape).background(VictimPrimary.copy(alpha = 0.16f)))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.size(184.dp).clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(Color(0xFFF0564A), VictimPrimary)))
                            .clickable { onSosTap() }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                        Text(text = "Tap for SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = "Medical Emergency", color = Color.White.copy(alpha = 0.9f), fontSize = 12.5.sp)
                    }
                }
            }

            PastelInfoCard(
                cardBg = VictimPinkCard,
                cardBorder = VictimPinkBorder,
                title = "Get help from nearby responders",
                subtitle = "Your location will be shared with trusted responders when you send an SOS.",
                icon = { Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(26.dp)) },
                trailing = { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimPrimary) },
                onClick = onSosTap,
            )

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
                    onClick = onNavigateToTracking,
                )
                BePreparedCard(
                    bg = VictimPurpleCard, border = VictimPurpleBorder,
                    icon = Icons.Default.Timer, iconTint = Color(0xFF7C3AED),
                    title = "Emergency History", subtitle = "View past incidents",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMap,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
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

@Composable
private fun SosDetailSheet(onCancel: () -> Unit, onAutoSend: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        // 5-second auto-send countdown
        repeat(50) {
            delay(100)
            progress = (it + 1) / 50f
        }
        onAutoSend()
    }
    Column(modifier = Modifier.fillMaxSize().background(Color(0x800F172A))) {
        Spacer(modifier = Modifier.weight(0.35f))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
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
                SosInputOption(
                    bg = VictimBlueCard, iconBg = Color.White,
                    icon = Icons.Default.Notes, iconTint = Color(0xFF2563EB),
                    title = "Type", subtitle = "Describe what happened",
                    modifier = Modifier.weight(1f),
                )
                SosInputOption(
                    bg = VictimGreenCard, iconBg = Color.White,
                    icon = Icons.Default.Mic, iconTint = Color(0xFF059669),
                    title = "Speak", subtitle = "Tell us using voice",
                    modifier = Modifier.weight(1f),
                )
                SosInputOption(
                    bg = VictimPurpleCard, iconBg = Color.White,
                    icon = Icons.Default.CameraAlt, iconTint = Color(0xFF7C3AED),
                    title = "Camera", subtitle = "Take a photo or video",
                    modifier = Modifier.weight(1f),
                )
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
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White)
                        .border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
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
            Column(
                modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimPinkCard)
                    .border(1.dp, VictimPinkBorder, VictimShapes.Card16).padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Auto-send in 5 seconds", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If no additional details are provided, we'll send a basic medical emergency alert with your location.",
                    fontSize = 13.sp, color = VictimTextMuted, lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = VictimPrimary,
                    trackColor = VictimTextMuted.copy(alpha = 0.2f),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            VictimOutlineButton(text = "Cancel", onClick = onCancel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SosInputOption(
    bg: Color,
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clip(VictimShapes.Card16).background(bg).padding(vertical = 16.dp, horizontal = 8.dp),
    ) {
        Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 12.sp, color = VictimTextMuted, lineHeight = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun FindingRespondersContent(
    onCancel: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToAssistant: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "←", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, modifier = Modifier.clickable { onCancel() }.padding(end = 10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Medical Emergency", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
                Text(text = "Help is being found", fontSize = 13.sp, color = VictimTextMuted)
            }
            Row(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(VictimPinkCard).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(StatusLiveRed))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "ACTIVE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card20).background(VictimPinkCard).border(1.dp, VictimPinkBorder, VictimShapes.Card20).padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(34.dp).clip(CircleShape).border(2.dp, VictimPrimary, CircleShape), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(VictimPrimary))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Finding nearby responders...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
                        Text(text = "Searching within 500 m", fontSize = 13.sp, color = VictimTextMuted)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Step 1 of 3", fontSize = 12.sp, color = VictimTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(VictimPrimary))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(VictimBorder))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(VictimBorder))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = VictimPrimary, trackColor = VictimTextMuted.copy(alpha = 0.18f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "If no one responds, we'll expand the search to 2 km, then 5 km.", fontSize = 12.5.sp, color = VictimTextMuted)
        }

        MapPlaceholder(modifier = Modifier.height(240.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.size(190.dp).clip(CircleShape).background(VictimPrimary.copy(alpha = 0.12f)))
                Box(modifier = Modifier.size(150.dp).clip(CircleShape).border(1.5.dp, VictimPrimary.copy(alpha = 0.35f), CircleShape))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(VictimPrimary).padding(horizontal = 14.dp, vertical = 4.dp)) {
                        Text(text = "You", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(VictimPrimary).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text(text = "500 m", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FindingHelpCard(bg = VictimBlueCard, border = VictimBlueBorder, title = "AI Medical Help", subtitle = "Get step-by-step first-aid guidance", modifier = Modifier.weight(1f), onClick = onNavigateToAssistant)
            FindingHelpCard(bg = VictimGreenCard, border = VictimGreenBorder, title = "Nearest Hospital", subtitle = "Apollo Gleneagles 1.8 km away", modifier = Modifier.weight(1f), onClick = onNavigateToTracking)
            FindingHelpCard(bg = VictimOrangeCard, border = VictimOrangeBorder, title = "Emergency Services", subtitle = "Emergency numbers (Coming soon)", modifier = Modifier.weight(1f), onClick = {})
        }

        PastelInfoCard(
            cardBg = VictimPinkCard, cardBorder = VictimPinkBorder,
            title = "Stay calm, you're not alone",
            subtitle = "Help is on the way. You can use AI assistance, check nearby hospitals, or prepare basic first aid.",
            icon = { Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(24.dp)) },
        )

        VictimOutlineButton(text = "✕   Cancel SOS", onClick = onCancel)
        // Demo fast-forward to live tracking
        Text(
            text = "View live responder →",
            fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = VictimPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onNavigateToTracking() }.padding(6.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FindingHelpCard(bg: Color, border: Color, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clip(VictimShapes.Card16).background(bg).border(1.dp, border, VictimShapes.Card16).clickable { onClick() }.padding(12.dp)) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = subtitle, fontSize = 12.sp, color = VictimTextMuted, lineHeight = 16.sp, modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
            Text(text = "→", fontWeight = FontWeight.Bold, color = VictimTextDark, fontSize = 15.sp)
        }
    }
}
