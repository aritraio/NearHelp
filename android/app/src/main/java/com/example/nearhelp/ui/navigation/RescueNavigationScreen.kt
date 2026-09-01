package com.example.nearhelp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.theme.AiBlue
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.CardSurfaceVariant
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedContainer
import com.example.nearhelp.theme.EmergencyRedDark
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted

@Composable
fun RescueNavigationScreen(
  onNavigateBack: () -> Unit,
  viewModel: RescueNavigationViewModel,
  incidentId: String = "KOL-SOS-8821",
  token: String? = null,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  var showHazardDrawer by remember { mutableStateOf(false) }

  LaunchedEffect(incidentId) {
    viewModel.connectToTracking(incidentId, token)
  }

  // Animation for Victim SOS Pulse Marker
  val victimTransition = rememberInfiniteTransition(label = "VictimBeaconPulse")
  val victimRadiusAnim by victimTransition.animateFloat(
    initialValue = 10f,
    targetValue = 28f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "VictimRadius"
  )
  val victimAlphaAnim by victimTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "VictimAlpha"
  )

  // Animation for AHA 110 BPM CPR Metronome (545.45 ms period)
  val cprTransition = rememberInfiniteTransition(label = "CprMetronomePulse")
  val cprPulseScale by cprTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(545, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "CprScale"
  )

  val activeRoute = viewModel.getActiveRoute()
  val currentStep = activeRoute.steps.getOrNull(state.currentTurnStepIndex)
    ?: activeRoute.steps.firstOrNull()
    ?: DEFAULT_PRIMARY_RESCUE_ROUTE.steps[0]

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF000000))
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {

    // 1. Top Bar with Live WS Indicator & Back Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CardSurface)
            .border(1.dp, SurfaceBorder, CircleShape)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextHighContrast,
            modifier = Modifier.size(18.dp)
          )
        }

        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "AI RESCUE NAVIGATION",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Black,
              color = AiCyan,
              letterSpacing = 1.sp
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(EmergencyRedContainer)
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "CRITICAL L1",
                style = MaterialTheme.typography.labelSmall,
                color = EmergencyRed,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
              )
            }
          }
          Text(
            text = "Target: ${state.victimName} • Elevator Bank B",
            style = MaterialTheme.typography.bodySmall,
            color = TextMediumContrast,
            fontSize = 11.sp
          )
        }
      }

      // Status indicator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (state.connectionStatus == ConnectionStatus.CONNECTED) Color(0x2200E676) else Color(0x22FFA000)
          )
          .border(
            1.dp,
            if (state.connectionStatus == ConnectionStatus.CONNECTED) SafeGreen else Color(0xFFFFA000),
            RoundedCornerShape(12.dp)
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Wifi,
          contentDescription = "Stream",
          tint = if (state.connectionStatus == ConnectionStatus.CONNECTED) SafeGreen else Color(0xFFFFA000),
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = if (state.connectionStatus == ConnectionStatus.CONNECTED) "LIVE GPS" else "CONNECTING",
          color = if (state.connectionStatus == ConnectionStatus.CONNECTED) SafeGreen else Color(0xFFFFA000),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 2. Toast Notification Bar
    state.toastMessage?.let { msg ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x3300E676))
          .border(1.dp, SafeGreen, RoundedCornerShape(8.dp))
          .clickable { viewModel.clearToast() }
          .padding(10.dp)
      ) {
        Text(
          text = msg,
          color = SafeGreen,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 3. Turn-by-Turn Maneuver HUD Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = CardSurface),
      border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SafeGreen, AiCyan)))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          // Maneuver Icon
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (currentStep.maneuver == "ARRIVE") Color(0x33FF2A44) else Color(0x2200E676)
              )
              .border(
                1.5.dp,
                if (currentStep.maneuver == "ARRIVE") EmergencyRed else SafeGreen,
                RoundedCornerShape(10.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            val icon = when (currentStep.maneuver) {
              "TURN_RIGHT" -> Icons.Default.TurnRight
              "TURN_LEFT" -> Icons.Default.TurnLeft
              "ARRIVE" -> Icons.Default.LocationOn
              else -> Icons.Default.Navigation
            }
            Icon(
              imageVector = icon,
              contentDescription = currentStep.maneuver,
              tint = if (currentStep.maneuver == "ARRIVE") EmergencyRed else SafeGreen,
              modifier = Modifier.size(24.dp)
            )
          }

          Column {
            Text(
              text = currentStep.instruction,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.ExtraBold,
              color = TextHighContrast,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "📍 ${currentStep.landmark}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumContrast,
                fontSize = 10.5.sp
              )
              Text(text = "•", color = TextMuted, fontSize = 10.sp)
              Text(
                text = currentStep.distanceFormatted,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = SafeGreen,
                fontSize = 11.sp
              )
            }
          }
        }

        // Stepper Controls
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.prevTurnStep() },
            enabled = state.currentTurnStepIndex > 0,
            modifier = Modifier
              .size(30.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(CardSurfaceVariant)
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Prev",
              tint = if (state.currentTurnStepIndex > 0) TextHighContrast else TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(
            onClick = { viewModel.nextTurnStep() },
            enabled = state.currentTurnStepIndex < activeRoute.steps.size - 1,
            modifier = Modifier
              .size(30.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(CardSurfaceVariant)
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next",
              tint = if (state.currentTurnStepIndex < activeRoute.steps.size - 1) SafeGreen else TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 4. Multi-Route Detour Switcher Bar
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = "Routes", tint = AiCyan, modifier = Modifier.size(14.dp))
          Text(
            text = "AI RESCUE ROUTE OPTIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AiCyan,
            letterSpacing = 0.5.sp
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { viewModel.toggleTrafficAvoidance() }
            .background(if (state.isTrafficAvoidanceEnabled) Color(0x2200E5FF) else CardSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Avoidance",
            tint = if (state.isTrafficAvoidanceEnabled) AiCyan else TextMuted,
            modifier = Modifier.size(11.dp)
          )
          Text(
            text = if (state.isTrafficAvoidanceEnabled) "AI Traffic Avoid: ON" else "Avoidance: OFF",
            color = if (state.isTrafficAvoidanceEnabled) AiCyan else TextMuted,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Route 1: Detour (Traffic Bypass)
        state.detourRoute?.let { detour ->
          val isSelected = state.selectedRouteType == "DETOUR"
          RouteOptionChip(
            title = "⚡ AI Detour (Traffic Bypass)",
            eta = detour.durationFormatted,
            distance = detour.distanceFormatted,
            badge = "Saves 1.8 mins",
            badgeColor = SafeGreen,
            isSelected = isSelected,
            onClick = { viewModel.selectRoute("DETOUR") }
          )
        }

        // Route 2: Primary Route (Main Arterial)
        val isPrimarySelected = state.selectedRouteType == "PRIMARY"
        RouteOptionChip(
          title = "🚗 Main Arterial (Ring Rd)",
          eta = state.primaryRoute.durationFormatted,
          distance = state.primaryRoute.distanceFormatted,
          badge = if (state.primaryRoute.hasHazardConflict) "⚠️ +3.5m Delay" else "Standard",
          badgeColor = if (state.primaryRoute.hasHazardConflict) EmergencyRed else TextMuted,
          isSelected = isPrimarySelected,
          onClick = { viewModel.selectRoute("PRIMARY") }
        )

        // Route 3: AED Pickup Route
        state.aedPickupRoute?.let { aedRoute ->
          val isAedSelected = state.selectedRouteType == "AED_PICKUP"
          RouteOptionChip(
            title = "🏥 AED Pickup Route",
            eta = aedRoute.durationFormatted,
            distance = aedRoute.distanceFormatted,
            badge = "+45s Pickup",
            badgeColor = AiBlue,
            isSelected = isAedSelected,
            onClick = { viewModel.selectRoute("AED_PICKUP") }
          )
        }
      }
    }

    // 5. Active Hazard Alert Card (Collapsible)
    if (state.activeHazards.isNotEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showHazardDrawer = !showHazardDrawer },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF160F0F)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmergencyRed, Color(0xFFFFA000))))
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = "Hazard", tint = Color(0xFFFFA000), modifier = Modifier.size(15.dp))
              Text(
                text = "⚠️ ${state.activeHazards.size} ROAD HAZARDS IN ROUTE CORRIDOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFA000)
              )
            }
            Text(
              text = if (showHazardDrawer) "Hide ▲" else "View ▼",
              fontSize = 10.sp,
              color = TextMediumContrast
            )
          }

          AnimatedVisibility(
            visible = showHazardDrawer,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
          ) {
            Column(
              modifier = Modifier.padding(top = 8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              state.activeHazards.forEach { hazard ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33000000), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = hazard.title,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextHighContrast
                    )
                    Text(
                      text = hazard.description,
                      fontSize = 10.sp,
                      color = TextMediumContrast
                    )
                  }
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(if (hazard.severity == "CRITICAL") Color(0x33FF2A44) else Color(0x33FFA000))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "+${hazard.delaySeconds}s delay",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Black,
                      color = if (hazard.severity == "CRITICAL") EmergencyRed else Color(0xFFFFA000)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 6. Interactive Multi-Route Vector Polyline Map Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF07090D))
        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Cartography Grid
        val gridSpacing = 24.dp.toPx()
        var x = 0f
        while (x < w) {
          drawLine(
            color = Color(0x0CFFFFFF),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
          )
          x += gridSpacing
        }
        var y = 0f
        while (y < h) {
          drawLine(
            color = Color(0x0CFFFFFF),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
          )
          y += gridSpacing
        }

        // 2. City Road Arterials
        val roadStroke = 14.dp.toPx()
        val roadColor = Color(0xFF1E232F)

        // Ring Road
        drawLine(
          color = roadColor,
          start = Offset(0f, h * 0.85f),
          end = Offset(w, h * 0.75f),
          strokeWidth = roadStroke
        )
        // Access Concourse
        drawLine(
          color = roadColor,
          start = Offset(w * 0.15f, h * 0.95f),
          end = Offset(w * 0.85f, h * 0.20f),
          strokeWidth = roadStroke * 0.8f
        )
        // EP Block Service Lane
        drawLine(
          color = Color(0xFF161A24),
          start = Offset(w * 0.10f, h * 0.85f),
          end = Offset(w * 0.55f, h * 0.35f),
          strokeWidth = roadStroke * 0.6f
        )

        // 3. Draw Detour Path (Cyan Dashed) if available
        val detourPath = Path().apply {
          moveTo(w * 0.15f, h * 0.85f)
          lineTo(w * 0.35f, h * 0.45f)
          lineTo(w * 0.60f, h * 0.35f)
          lineTo(w * 0.85f, h * 0.25f)
        }
        drawPath(
          path = detourPath,
          color = if (state.selectedRouteType == "DETOUR") AiCyan else Color(0x4400E5FF),
          style = Stroke(
            width = if (state.selectedRouteType == "DETOUR") 4.dp.toPx() else 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
          )
        )

        // 4. Draw Primary Route Polyline (Green / Amber / Red Traffic Segments)
        val primaryPath1 = Path().apply {
          moveTo(w * 0.15f, h * 0.85f)
          lineTo(w * 0.40f, h * 0.65f)
        }
        val primaryPath2 = Path().apply {
          moveTo(w * 0.40f, h * 0.65f)
          lineTo(w * 0.65f, h * 0.45f)
        }
        val primaryPath3 = Path().apply {
          moveTo(w * 0.65f, h * 0.45f)
          lineTo(w * 0.85f, h * 0.25f)
        }

        val primaryWidth = if (state.selectedRouteType == "PRIMARY") 4.dp.toPx() else 2.dp.toPx()
        drawPath(primaryPath1, color = SafeGreen, style = Stroke(width = primaryWidth))
        drawPath(primaryPath2, color = if (state.primaryRoute.hasHazardConflict) EmergencyRed else SafeGreen, style = Stroke(width = primaryWidth))
        drawPath(primaryPath3, color = SafeGreen, style = Stroke(width = primaryWidth))

        // 5. Draw Hazard Blockage Node (Traffic & Flood)
        drawCircle(
          color = Color(0x33FF2A44),
          radius = 14.dp.toPx(),
          center = Offset(w * 0.50f, h * 0.55f)
        )
        drawCircle(
          color = EmergencyRed,
          radius = 4.dp.toPx(),
          center = Offset(w * 0.50f, h * 0.55f)
        )

        // 6. Draw AED Node Marker
        drawCircle(
          color = Color(0x4400E5FF),
          radius = 10.dp.toPx(),
          center = Offset(w * 0.35f, h * 0.45f)
        )
        drawCircle(
          color = AiCyan,
          radius = 4.dp.toPx(),
          center = Offset(w * 0.35f, h * 0.45f)
        )

        // 7. Draw Victim SOS Target Beacon
        val victimCenter = Offset(w * 0.85f, h * 0.25f)
        drawCircle(
          color = EmergencyRed.copy(alpha = victimAlphaAnim),
          radius = victimRadiusAnim.dp.toPx(),
          center = victimCenter
        )
        drawCircle(
          color = EmergencyRed,
          radius = 7.dp.toPx(),
          center = victimCenter
        )

        // 8. Draw Moving Responder Pin
        val stepPositions = listOf(
          Offset(w * 0.15f, h * 0.85f),
          if (state.selectedRouteType == "DETOUR") Offset(w * 0.35f, h * 0.45f) else Offset(w * 0.40f, h * 0.65f),
          if (state.selectedRouteType == "DETOUR") Offset(w * 0.60f, h * 0.35f) else Offset(w * 0.65f, h * 0.45f),
          Offset(w * 0.85f, h * 0.25f)
        )
        val responderCenter = stepPositions.getOrNull(state.currentTurnStepIndex) ?: stepPositions[0]

        drawCircle(
          color = Color(0x4400E676),
          radius = 12.dp.toPx(),
          center = responderCenter
        )
        drawCircle(
          color = SafeGreen,
          radius = 6.dp.toPx(),
          center = responderCenter
        )
      }

      // Live Telemetry HUD Overlay
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(8.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xCC000000))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "⚡ Speed: 4.8 km/h", fontSize = 9.5.sp, color = TextHighContrast, fontWeight = FontWeight.Bold)
          Text(text = "🛰️ GPS: ±2.5m", fontSize = 9.5.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
          Text(text = "🧭 34° NE", fontSize = 9.5.sp, color = AiCyan, fontWeight = FontWeight.Bold)
        }
      }

      // Active Route ETA Badge
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xEE000000))
          .border(1.dp, SafeGreen, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "ETA: ${state.liveEtaFormatted}",
          color = SafeGreen,
          fontSize = 11.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    // 7. Encrypted Medical ID Reveal Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = CardSurface),
      border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AiCyan, Color(0xFF2979FF))))
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = AiCyan, modifier = Modifier.size(13.dp))
            Text(
              text = "ENCRYPTED MEDICAL ID REVEAL",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Black,
              color = AiCyan,
              letterSpacing = 0.5.sp
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .clickable { viewModel.toggleMedicalId() }
              .background(CardSurfaceVariant)
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Icon(
              imageVector = if (state.isMedicalIdRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle",
              tint = TextMediumContrast,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = if (state.isMedicalIdRevealed) "Hide" else "Reveal",
              fontSize = 10.sp,
              color = TextMediumContrast
            )
          }
        }

        if (state.isMedicalIdRevealed) {
          // Vitals Grid
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF121418))
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "BLOOD GROUP", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
              Text(text = "B+", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EmergencyRed)
            }
            Column {
              Text(text = "PACEMAKER", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
              Text(text = "⚠️ Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmergencyRed)
            }
            Column {
              Text(text = "ALLERGIES", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
              Text(text = "Penicillin, Sulfa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
            }
          }

          // Emergency Contact & Communication
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Kin: Subhash Das (+91 98301 99881)",
                fontSize = 11.sp,
                color = TextMediumContrast
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x2200E676))
                  .border(1.dp, SafeGreen, RoundedCornerShape(6.dp))
                  .clickable { viewModel.showToast("📞 Calling Subhash Das (+91 98301 99881)... Connected.") }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.Call, contentDescription = "Call", tint = SafeGreen, modifier = Modifier.size(11.dp))
                  Text(text = "Call", color = SafeGreen, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x2200E5FF))
                  .border(1.dp, AiCyan, RoundedCornerShape(6.dp))
                  .clickable { viewModel.showToast("💬 Encrypted SMS sent: Dr. Anirban Roy is en route to Elevator Bank B.") }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "SMS", tint = AiCyan, modifier = Modifier.size(11.dp))
                  Text(text = "SMS", color = AiCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // 8. AED Status & AHA CPR Rhythm Metronome
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = CardSurface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = "AED",
            tint = if (state.isAedAttached) SafeGreen else Color(0xFFFFA000),
            modifier = Modifier.size(18.dp)
          )
          Column {
            Text(
              text = if (state.isAedAttached) "⚡ AED Attached — Rhythm: Normal Sinus" else "Webel Bhavan AED: 120m away",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (state.isAedAttached) SafeGreen else TextHighContrast
            )
            Text(
              text = if (state.isAedAttached) "Shock delivered. Resume compressions." else "Wall cabinet #2 opposite Elevator Bank B",
              fontSize = 10.sp,
              color = TextMediumContrast
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (state.isAedAttached) Color(0x2200E676) else Color(0x22FFA000))
            .border(1.dp, if (state.isAedAttached) SafeGreen else Color(0xFFFFA000), RoundedCornerShape(6.dp))
            .clickable { viewModel.toggleAedAttached() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(
            text = if (state.isAedAttached) "AED Active" else "Attach AED",
            color = if (state.isAedAttached) SafeGreen else Color(0xFFFFA000),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }

    // 9. Emergency Action Controls & Simulation
    Spacer(modifier = Modifier.height(4.dp))

    // Simulation Trigger
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(CardSurfaceVariant)
          .clickable { viewModel.toggleGpsSimulation() }
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = if (state.isGpsSimulationActive) Icons.Default.Stop else Icons.Default.PlayArrow,
          contentDescription = "Sim",
          tint = if (state.isGpsSimulationActive) EmergencyRed else SafeGreen,
          modifier = Modifier.size(14.dp)
        )
        Text(
          text = if (state.isGpsSimulationActive) "Pause GPS Walk" else "Simulate GPS Walk",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextHighContrast
        )
      }

      Text(
        text = "Step ${state.currentTurnStepIndex + 1} of ${activeRoute.steps.size}",
        fontSize = 11.sp,
        color = TextMediumContrast
      )
    }

    // Action Flow Buttons
    if (state.incidentStatus != "ARRIVED" && state.incidentStatus != "HANDOVER_108" && state.incidentStatus != "RESOLVED") {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(SafeGreen)
          .clickable { viewModel.onArrivedClick() }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = "Arrive", tint = Color.Black, modifier = Modifier.size(18.dp))
          Text(
            text = "📍 I HAVE ARRIVED ON SCENE",
            color = Color.Black,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }

    if (state.incidentStatus == "ARRIVED") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (state.isCprMetronomeActive) EmergencyRed else CardSurfaceVariant)
            .border(1.dp, if (state.isCprMetronomeActive) EmergencyRed else SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { viewModel.toggleCprMetronome() }
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "CPR",
              tint = Color.White,
              modifier = Modifier
                .size(16.dp)
                .scale(if (state.isCprMetronomeActive) cprPulseScale else 1.0f)
            )
            Text(
              text = if (state.isCprMetronomeActive) "CPR 110 BPM (ON)" else "Start CPR Metronome",
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(AiBlue)
            .clickable { viewModel.onHandover108Click() }
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.LocalHospital, contentDescription = "Ambulance", tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
              text = "Handover to 108",
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    if (state.incidentStatus == "HANDOVER_108") {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(SafeGreen)
          .clickable { viewModel.onResolveClick() }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = "Resolve", tint = Color.Black, modifier = Modifier.size(18.dp))
          Text(
            text = "✨ MARK RESCUE RESOLVED (AMRI ICU)",
            color = Color.Black,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }

    if (state.incidentStatus == "RESOLVED") {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0x2200E676))
          .border(1.dp, SafeGreen, RoundedCornerShape(10.dp))
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "✅ Incident Successfully Resolved & Handover Archived.",
          color = SafeGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun RouteOptionChip(
  title: String,
  eta: String,
  distance: String,
  badge: String,
  badgeColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) Color(0xFF141C24) else CardSurface)
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) AiCyan else SurfaceBorder,
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(onClick = onClick)
      .padding(10.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = title,
          fontSize = 11.5.sp,
          fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
          color = if (isSelected) Color.White else TextMediumContrast
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(badgeColor.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = badge,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            color = badgeColor
          )
        }
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "ETA: $eta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) SafeGreen else TextMuted)
        Text(text = "•", fontSize = 9.sp, color = TextMuted)
        Text(text = distance, fontSize = 11.sp, color = TextMediumContrast)
      }
    }
  }
}
