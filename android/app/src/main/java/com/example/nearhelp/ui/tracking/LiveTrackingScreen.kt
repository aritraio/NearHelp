package com.example.nearhelp.ui.tracking

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.StarRatingYellow
import com.example.nearhelp.theme.StatusLiveRed
import com.example.nearhelp.theme.StatusSafeGreen
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimDivider
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
import com.example.nearhelp.ui.victim.MapPlaceholder
import com.example.nearhelp.ui.victim.StatusBadge
import com.example.nearhelp.ui.victim.VictimOutlineButton
import com.example.nearhelp.ui.victim.VictimPrimaryButton
import com.example.nearhelp.ui.victim.VictimShapes

/**
 * Victim live tracking — light redesign matching mockups:
 * Medical Emergency + LIVE, route map with Priya/ETA/You, responder sheet,
 * stat pills, reassurance banners, AI instructions, arrived + feedback + summary.
 * Preserves existing ViewModel contract.
 */
@Composable
fun LiveTrackingScreen(
  onNavigateBack: () -> Unit,
  onNavigateToNavigation: () -> Unit = {},
  viewModel: LiveTrackingViewModel,
  incidentId: String = "KOL-SOS-8821",
  token: String? = null,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  LaunchedEffect(incidentId) { viewModel.connectToIncident(incidentId, token) }

  var feedbackStep by remember { mutableStateOf(0) } // 0 tracking, 1 feedback, 2 summary
  var aiExpanded by remember { mutableStateOf(false) }
  val isArrived = state.incidentStatus in listOf("ARRIVED", "ON_SCENE", "HANDOVER_108", "RESOLVED") || feedbackStep > 0

  if (feedbackStep == 1) {
    FeedbackContent(onSubmit = { feedbackStep = 2 }, onClose = { feedbackStep = 0 })
    return
  }
  if (feedbackStep == 2) {
    SummaryContent(onBackHome = onNavigateBack)
    return
  }

  Column(
    modifier = modifier.fillMaxSize().background(VictimBackground)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Top bar: back + title + LIVE + menu
    Row(
      modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card20).background(Color.White)
        .border(1.dp, VictimBorder, VictimShapes.Card20).padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VictimTextDark)
      }
      Text(text = "Medical Emergency", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, modifier = Modifier.weight(1f))
      StatusBadge(text = "LIVE")
      IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = VictimTextDark)
      }
    }

    // Map with route + markers
    Box(
      modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card20)
        .background(Color(0xFFE8EEF3)).border(1.dp, VictimBorder, VictimShapes.Card20),
    ) {
      MapPlaceholder(modifier = Modifier.height(300.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Route line suggestion
          Box(modifier = Modifier.align(Alignment.Center).size(width = 8.dp, height = 200.dp).background(Color(0xFF2563EB), RoundedCornerShape(4.dp)))
          // Priya bubble top
          Row(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 26.dp)
              .clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFD6E4FF)), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = "Priya", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VictimTextDark)
              Text(text = "2 min away", fontSize = 12.sp, color = VictimTextMuted)
            }
          }
          // ETA bubble
          Box(
            modifier = Modifier.align(Alignment.Center).padding(start = 90.dp, top = 10.dp)
              .clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "ETA", fontSize = 11.sp, color = VictimTextMuted)
              Text(text = "2 min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            }
          }
          // You pin
          Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(150.dp).clip(CircleShape).background(StatusLiveRed.copy(alpha = 0.12f)))
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(VictimPrimary).padding(horizontal = 16.dp, vertical = 4.dp)) {
              Text(text = "You", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
          // Nearest hospital overlay
          Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
              .clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(14.dp)).padding(10.dp).clickable { onNavigateToNavigation() },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = "Nearest Hospital", fontSize = 11.5.sp, color = VictimTextMuted)
              Text(text = "Apollo Hospital", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              Text(text = "1.8 km away", fontSize = 12.sp, color = VictimTextMuted)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(16.dp))
          }
          // Right map controls
          Column(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Icons.Default.Layers, Icons.Default.Navigation, Icons.Default.MyLocation).forEach {
              Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White).border(1.dp, VictimBorder, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = it, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(20.dp))
              }
            }
          }
          // Location pill top-left
          Row(
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
              .clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Kolkata, West Bengal", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
          }
        }
      }
      if (isArrived) {
        Box(
          modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
            .clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, VictimBlueBorder, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Responder", fontSize = 12.sp, color = VictimTextMuted)
            Text(text = "Arrived", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
          }
        }
      }
    }

    // Responder sheet
    Column(
      modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 44.dp, height = 5.dp).clip(CircleShape).background(VictimBorder))
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFE8EDF3)), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Priya Sharma", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.Check, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(0xFF2563EB)).padding(3.dp))
          }
          Text(text = "Verified Medical Responder", fontSize = 13.5.sp, color = VictimTextMuted)
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(VictimGreenCard).border(1.dp, VictimGreenBorder, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = StatusSafeGreen, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "CPR Certified", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
            }
            Row(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color(0xFFF1F5F9)).border(1.dp, VictimBorder, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "First Aid Trained", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
            }
          }
        }
        if (!isArrived) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.showToast("Opening chat with Priya…") }) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(VictimBlueCard), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(22.dp))
            }
            Text(text = "Chat", fontSize = 12.sp, color = VictimTextDark, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      if (!isArrived) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          TrackingStat(icon = Icons.Default.Navigation, iconBg = VictimBlueCard, tint = Color(0xFF2563EB), title = "420 m", subtitle = "away", modifier = Modifier.weight(1f))
          TrackingStat(icon = Icons.Default.LocationOn, iconBg = VictimPinkCard, tint = VictimPrimary, title = state.liveEtaFormatted.ifBlank { "2 min" }, subtitle = "ETA", modifier = Modifier.weight(1f))
          TrackingStat(icon = Icons.Default.Person, iconBg = VictimBlueCard, tint = Color(0xFF2563EB), title = "On the way", subtitle = "to you", modifier = Modifier.weight(1f))
        }
        Row(
          modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimPinkCard).border(1.dp, VictimPinkBorder, VictimShapes.Card16).padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = "Help is on the way", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
            Text(text = "Stay calm. Follow the AI instructions while you wait.", fontSize = 13.sp, color = VictimTextMuted)
          }
        }
        // Preserved tactical actions (hidden behind details to keep ViewModel wired)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F5F9)).border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).clickable { viewModel.onArrivedClick(); }.padding(10.dp), contentAlignment = Alignment.Center) {
            Text(text = "Simulate: Arrived", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          }
          Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F5F9)).border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).clickable { viewModel.toggleMedicalId() }.padding(10.dp), contentAlignment = Alignment.Center) {
            Text(text = if (state.isMedicalIdRevealed) "Hide Medical ID" else "Reveal Medical ID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          }
        }
        if (state.isMedicalIdRevealed) {
          Text(text = "Blood ${state.victimBloodGroup} • Allergies: ${state.allergies.joinToString(", ")} • ${state.emergencyContactName}", fontSize = 12.sp, color = VictimTextMuted)
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimGreenCard).border(1.dp, VictimGreenBorder, VictimShapes.Card16).padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(StatusSafeGreen), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = "Responder has arrived", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusSafeGreen)
            Text(text = "They are with you now.", fontSize = 13.sp, color = VictimTextMuted)
          }
        }
        Box(
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(VictimBlueCard).border(1.dp, VictimBlueBorder, RoundedCornerShape(14.dp)).clickable { viewModel.showToast("Opening chat with Priya…") }.padding(14.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Chat with Priya", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF2563EB))
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimPinkCard).border(1.dp, VictimPinkBorder, VictimShapes.Card16).padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = "You're in safe hands", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
            Text(text = "Stay calm. Follow the responder's instructions.", fontSize = 13.sp, color = VictimTextMuted)
          }
        }
        VictimPrimaryButton(text = "Complete & Rate Help", onClick = { viewModel.onHandover108Click(); feedbackStep = 1 })
      }

      // AI Medical Instructions expandable (both states)
      Column(
        modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimPurpleCard).border(1.dp, VictimPurpleBorder, VictimShapes.Card16).clickable { aiExpanded = !aiExpanded }.padding(14.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
            Text(text = "🧠", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "AI Medical Instructions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Text(text = "Get step-by-step guidance for this emergency", fontSize = 13.sp, color = VictimTextMuted)
          }
          Text(text = if (aiExpanded) "▲" else "▲", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        }
        if (aiExpanded) {
          Spacer(modifier = Modifier.height(10.dp))
          val steps = state.turnSteps.ifEmpty { DEFAULT_NAVIGATION_STEPS }
          steps.forEachIndexed { i, s ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
              Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Text(text = "${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = s.instruction, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
                Text(text = "${s.landmark} • ${s.distance}", fontSize = 12.sp, color = VictimTextMuted)
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(10.dp)).clickable { viewModel.toggleCprMetronome() }.padding(8.dp), contentAlignment = Alignment.Center) {
              Text(text = if (state.isCprMetronomeActive) "CPR 110 BPM (ON)" else "Start CPR Metronome", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(10.dp)).clickable { onNavigateToNavigation() }.padding(8.dp), contentAlignment = Alignment.Center) {
              Text(text = "AI Detour Nav", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            }
          }
        }
      }

      state.toastMessage?.let { toast ->
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF0F172A)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(text = toast, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
          Text(text = "✕", color = Color.White, modifier = Modifier.clickable { viewModel.clearToast() }.padding(start = 8.dp))
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}

@Composable
private fun TrackingStat(icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, tint: Color, title: String, subtitle: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(14.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      Text(text = subtitle, fontSize = 12.sp, color = VictimTextMuted)
    }
  }
}

@Composable
private fun FeedbackContent(onSubmit: () -> Unit, onClose: () -> Unit) {
  var rating by remember { mutableIntStateOf(5) }
  var selected by remember { mutableStateOf("Very helpful") }
  var comment by remember { mutableStateOf("") }
  Column(
    modifier = Modifier.fillMaxSize().background(Color.White)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Near", fontWeight = FontWeight.Black, fontSize = 20.sp, color = VictimTextDark)
        Text(text = "Help", fontWeight = FontWeight.Black, fontSize = 20.sp, color = VictimPrimary)
      }
      IconButton(onClick = onClose) { Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = VictimTextDark) }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(VictimPinkCard), contentAlignment = Alignment.Center) {
      Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(52.dp))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(text = "How was the help?", fontSize = 30.sp, fontWeight = FontWeight.Black, color = VictimTextDark, textAlign = TextAlign.Center)
    Text(text = "Your feedback helps us build a safer community for everyone.", fontSize = 14.sp, color = VictimTextMuted, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      repeat(5) { i ->
        Icon(
          imageVector = Icons.Default.Star, contentDescription = null,
          tint = if (i < rating) StarRatingYellow else VictimBorder,
          modifier = Modifier.size(44.dp).clickable { rating = i + 1 },
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Was the responder helpful?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(10.dp))
    listOf("Very helpful" to VictimGreenCard, "Helpful" to Color(0xFFF8FAFC), "Neutral" to Color(0xFFF8FAFC), "Not helpful" to Color(0xFFF8FAFC)).forEach { (label, bg) ->
      val isSel = selected == label
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (isSel && label == "Very helpful") VictimGreenCard else bg)
          .border(1.dp, if (isSel) StatusSafeGreen else VictimBorder, RoundedCornerShape(14.dp))
          .clickable { selected = label }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(if (label == "Very helpful") StatusSafeGreen else if (label == "Not helpful") VictimPrimary else Color(0xFFFBBF24).copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
          Text(text = "☺", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (isSel) StatusSafeGreen else VictimTextMuted, CircleShape), contentAlignment = Alignment.Center) {
          if (isSel) Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(StatusSafeGreen))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
    Text(text = "Add a comment (optional)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = comment, onValueChange = { if (it.length <= 300) comment = it },
      placeholder = { Text("Share your experience...") },
      modifier = Modifier.fillMaxWidth().height(110.dp),
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimBorder, unfocusedBorderColor = VictimBorder, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
    )
    Text(text = "${comment.length}/300", fontSize = 12.sp, color = VictimTextMuted, modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = TextAlign.End)
    Spacer(modifier = Modifier.height(10.dp))
    VictimPrimaryButton(text = "Submit", onClick = onSubmit)
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun SummaryContent(onBackHome: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize().background(Color.White)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = "Near", fontWeight = FontWeight.Black, fontSize = 19.sp, color = VictimTextDark)
        Text(text = "Help", fontWeight = FontWeight.Black, fontSize = 19.sp, color = VictimPrimary)
      }
      Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(22.dp).clickable { onBackHome() })
    }
    Spacer(modifier = Modifier.height(10.dp))
    Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(VictimPinkCard), contentAlignment = Alignment.Center) {
      Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(50.dp))
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "Emergency Summary", fontSize = 30.sp, fontWeight = FontWeight.Black, color = VictimTextDark, textAlign = TextAlign.Center)
    Text(text = "Help reached. Thank you for being part of a safer community.", fontSize = 14.sp, color = VictimTextMuted, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(14.dp))
    SummaryCard(bg = VictimPinkCard, border = VictimPinkBorder, label = "Emergency", title = "Medical Emergency", subtitle = "Sept 7, 2026 • 10:18 PM")
    SummaryCard(bg = VictimBlueCard, border = VictimBlueBorder, label = "Response", title = "2 responders", subtitle = "First responder: 2 min\nTotal response time: 4 min")
    SummaryCard(bg = VictimGreenCard, border = VictimGreenBorder, label = "Help provided", title = "CPR guidance\nFirst-aid assistance", subtitle = "")
    SummaryCard(bg = VictimPurpleCard, border = VictimPurpleBorder, label = "AI assistance", title = "6 instructions provided", subtitle = "")
    Row(
      modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(16.dp)).clickable {}.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(VictimBlueCard), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Default.Directions, contentDescription = null, tint = Color(0xFF2563EB))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = "View Timeline", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VictimTextDark)
        Text(text = "See the full sequence of events", fontSize = 12.5.sp, color = VictimTextMuted)
      }
      Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextDark)
    }
    Spacer(modifier = Modifier.height(12.dp))
    VictimPrimaryButton(text = "⌂  Back to Home", onClick = onBackHome)
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun SummaryCard(bg: Color, border: Color, label: String, title: String, subtitle: String) {
  Row(
    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).border(1.dp, border, RoundedCornerShape(16.dp)).padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
      when (label) {
        "Emergency" -> Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(26.dp))
        "Response" -> Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(26.dp))
        "Help provided" -> Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = StatusSafeGreen, modifier = Modifier.size(26.dp))
        else -> Text(text = "🧠", fontSize = 24.sp)
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = label, fontSize = 13.sp, color = VictimTextMuted)
      Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, lineHeight = 22.sp)
      if (subtitle.isNotBlank()) Text(text = subtitle, fontSize = 13.sp, color = VictimTextMuted, lineHeight = 18.sp)
    }
  }
  Spacer(modifier = Modifier.height(10.dp))
}
