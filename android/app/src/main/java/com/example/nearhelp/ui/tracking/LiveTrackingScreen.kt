package com.example.nearhelp.ui.tracking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun LiveTrackingScreen(
  onNavigateBack: () -> Unit,
  onNavigateToNavigation: () -> Unit = {},
  viewModel: LiveTrackingViewModel,
  incidentId: String = "KOL-SOS-8821",
  token: String? = null,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()

  LaunchedEffect(incidentId) {
    viewModel.connectToIncident(incidentId, token)
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

  val currentStep = state.turnSteps.getOrNull(state.currentTurnStepIndex) ?: state.turnSteps[0]

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF000000))
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {

    // 1. Top Bar with Live WS Indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(36.dp)
            .background(Color(0xFF1E232F), CircleShape)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextHighContrast,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "RESCUE NAVIGATION",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              ),
              color = TextHighContrast
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                  when (state.connectionStatus) {
                    ConnectionStatus.CONNECTED -> SafeGreen.copy(alpha = 0.2f)
                    ConnectionStatus.CONNECTING -> AiCyan.copy(alpha = 0.2f)
                    ConnectionStatus.RECONNECTING -> Color(0xFFFFA000).copy(alpha = 0.2f)
                    else -> EmergencyRed.copy(alpha = 0.2f)
                  }
                )
                .border(
                  1.dp,
                  when (state.connectionStatus) {
                    ConnectionStatus.CONNECTED -> SafeGreen
                    ConnectionStatus.CONNECTING -> AiCyan
                    ConnectionStatus.RECONNECTING -> Color(0xFFFFA000)
                    else -> EmergencyRed
                  },
                  RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 7.dp, vertical = 2.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Wifi,
                  contentDescription = "WS",
                  tint = when (state.connectionStatus) {
                    ConnectionStatus.CONNECTED -> SafeGreen
                    ConnectionStatus.CONNECTING -> AiCyan
                    ConnectionStatus.RECONNECTING -> Color(0xFFFFA000)
                    else -> EmergencyRed
                  },
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = when (state.connectionStatus) {
                    ConnectionStatus.CONNECTED -> "LIVE WS"
                    ConnectionStatus.CONNECTING -> "CONNECTING"
                    ConnectionStatus.RECONNECTING -> "RETRYING"
                    else -> "OFFLINE"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black
                  ),
                  color = when (state.connectionStatus) {
                    ConnectionStatus.CONNECTED -> SafeGreen
                    ConnectionStatus.CONNECTING -> AiCyan
                    ConnectionStatus.RECONNECTING -> Color(0xFFFFA000)
                    else -> EmergencyRed
                  }
                )
              }
            }
          }
          Text(
            text = "Incident #${state.incidentId} • ${state.subType.replace('_', ' ').uppercase()}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = EmergencyRed
          )
        }
      }

      // Priority Badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(EmergencyRedContainer)
          .border(1.dp, EmergencyRed, RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "CODE RED",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            fontSize = 10.sp
          ),
          color = Color.White
        )
      }
    }

    // 2. Turn-by-Turn Guidance HUD Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D10)),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.4f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SafeGreen.copy(alpha = 0.15f))
              .border(1.5.dp, SafeGreen, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (currentStep.turn == "arrive") Icons.Default.LocationOn else Icons.Default.Navigation,
              contentDescription = "Turn Direction",
              tint = if (currentStep.turn == "arrive") EmergencyRed else SafeGreen,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = currentStep.instruction,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              ),
              color = Color.White,
              maxLines = 2
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "📍 ${currentStep.landmark}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = TextMuted
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = TextMuted
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = currentStep.distance,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = SafeGreen
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
              .size(28.dp)
              .background(Color(0xFF1E232F), RoundedCornerShape(6.dp))
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Prev Step",
              tint = if (state.currentTurnStepIndex > 0) Color.White else TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = { viewModel.nextTurnStep() },
            enabled = state.currentTurnStepIndex < state.turnSteps.size - 1,
            modifier = Modifier
              .size(28.dp)
              .background(Color(0xFF1E232F), RoundedCornerShape(6.dp))
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next Step",
              tint = if (state.currentTurnStepIndex < state.turnSteps.size - 1) SafeGreen else TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = { viewModel.toggleGpsSimulation() },
            modifier = Modifier
              .size(28.dp)
              .background(
                if (state.isGpsSimulationActive) SafeGreen.copy(alpha = 0.25f) else Color(0xFF1E232F),
                RoundedCornerShape(6.dp)
              )
              .border(
                1.dp,
                if (state.isGpsSimulationActive) SafeGreen else Color.Transparent,
                RoundedCornerShape(6.dp)
              )
          ) {
            Icon(
              imageVector = if (state.isGpsSimulationActive) Icons.Default.Stop else Icons.Default.PlayArrow,
              contentDescription = "Simulate GPS",
              tint = if (state.isGpsSimulationActive) SafeGreen else AiCyan,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 2B. Full AI Navigation & Detour Routing Launch Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(Brush.horizontalGradient(listOf(Color(0xFF0F1A24), Color(0xFF162330))))
        .border(1.dp, AiCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .clickable { onNavigateToNavigation() }
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(AiCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Navigation, contentDescription = "Nav", tint = AiCyan, modifier = Modifier.size(15.dp))
          }
          Column {
            Text(
              text = "⚡ AI Detour & Traffic Rescue Routing",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Dynamic traffic bypass • AED waypoint • Turn HUD",
              fontSize = 10.sp,
              color = TextMediumContrast
            )
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(text = "Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCyan)
          Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = AiCyan, modifier = Modifier.size(14.dp))
        }
      }
    }

    // 3. Native Vector Radar & Interactive Map Canvas
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF07090D)),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasWidth = size.width
          val canvasHeight = size.height

          // Grid lines
          val gridSpacing = 40f
          var gx = 0f
          while (gx < canvasWidth) {
            drawLine(
              color = Color(0x0FFFFFFF),
              start = Offset(gx, 0f),
              end = Offset(gx, canvasHeight),
              strokeWidth = 1f
            )
            gx += gridSpacing
          }
          var gy = 0f
          while (gy < canvasHeight) {
            drawLine(
              color = Color(0x0FFFFFFF),
              start = Offset(0f, gy),
              end = Offset(canvasWidth, gy),
              strokeWidth = 1f
            )
            gy += gridSpacing
          }

          // Sector V Street Network
          drawLine(
            color = Color(0xFF1E232F),
            start = Offset(0f, canvasHeight * 0.85f),
            end = Offset(canvasWidth, canvasHeight * 0.75f),
            strokeWidth = 32f
          )
          drawLine(
            color = Color(0xFF171A23),
            start = Offset(canvasWidth * 0.1f, canvasHeight),
            end = Offset(canvasWidth * 0.35f, 0f),
            strokeWidth = 24f
          )
          drawLine(
            color = Color(0xFF1E232F),
            start = Offset(canvasWidth * 0.3f, canvasHeight * 0.55f),
            end = Offset(canvasWidth * 0.9f, canvasHeight * 0.2f),
            strokeWidth = 26f
          )

          // Building Blocks
          drawRoundRect(
            color = Color(0xFF10141D),
            topLeft = Offset(canvasWidth * 0.12f, canvasHeight * 0.15f),
            size = Size(canvasWidth * 0.22f, canvasHeight * 0.25f),
            cornerRadius = CornerRadius(6f, 6f)
          )
          drawRoundRect(
            color = Color(0xFF141824),
            topLeft = Offset(canvasWidth * 0.6f, canvasHeight * 0.05f),
            size = Size(canvasWidth * 0.3f, canvasHeight * 0.22f),
            cornerRadius = CornerRadius(6f, 6f)
          )

          // Route Polyline (Dashed Gradient)
          val routePoints = listOf(
            Offset(canvasWidth * 0.12f, canvasHeight * 0.82f),
            Offset(canvasWidth * 0.35f, canvasHeight * 0.53f),
            Offset(canvasWidth * 0.62f, canvasHeight * 0.44f),
            Offset(canvasWidth * 0.86f, canvasHeight * 0.25f)
          )

          for (i in 0 until routePoints.size - 1) {
            drawLine(
              brush = Brush.linearGradient(
                colors = listOf(SafeGreen, AiCyan, EmergencyRed),
                start = routePoints[0],
                end = routePoints.last()
              ),
              start = routePoints[i],
              end = routePoints[i + 1],
              strokeWidth = 6f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
            )
          }

          // Verified AED Node (Webel Bhavan Gate)
          val aedPos = Offset(canvasWidth * 0.32f, canvasHeight * 0.25f)
          drawCircle(
            color = AiCyan.copy(alpha = 0.2f),
            radius = 16f,
            center = aedPos
          )
          drawCircle(
            color = AiCyan,
            radius = 7f,
            center = aedPos
          )

          // Victim Target Pinpoint (Elevator Lobby)
          val victimPos = routePoints.last()
          drawCircle(
            color = EmergencyRed.copy(alpha = victimAlphaAnim),
            radius = victimRadiusAnim,
            center = victimPos
          )
          drawCircle(
            color = EmergencyRed,
            radius = 9f,
            center = victimPos
          )

          // Responder Pinpoint with live position matching step
          val activeStepIdx = state.currentTurnStepIndex.coerceIn(0, routePoints.size - 1)
          val responderPos = routePoints[activeStepIdx]

          drawCircle(
            color = SafeGreen.copy(alpha = 0.3f),
            radius = 18f,
            center = responderPos
          )
          drawCircle(
            color = SafeGreen,
            radius = 8f,
            center = responderPos
          )
          drawCircle(
            color = Color.White,
            radius = 3f,
            center = responderPos
          )
        }

        // Live Telemetry HUD Bar at bottom
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚡ Speed: ${state.liveSpeedKmh} km/h",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
              color = Color.White
            )
            Text(
              text = "🛰️ GPS: ±${state.liveGpsAccuracy}m",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
              color = SafeGreen
            )
            Text(
              text = "🧭 Bearing: ${state.liveBearingDeg.toInt()}° ${state.liveBearingCompass}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
              color = AiCyan
            )
          }
        }

        // Top-Right Live ETA Pill
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xEE0C0D10))
            .border(1.dp, SafeGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "ETA: ${state.liveEtaFormatted}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Black,
              fontSize = 11.5.sp
            ),
            color = SafeGreen
          )
        }
      }
    }

    // 4. Encrypted Medical ID Reveal Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D10)),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, AiCyan.copy(alpha = 0.3f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Encrypted",
              tint = AiCyan,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ENCRYPTED MEDICAL ID REVEAL",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              ),
              color = AiCyan
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x1AFFFFFF))
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
              .clickable { viewModel.toggleMedicalId() }
              .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (state.isMedicalIdRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle",
                tint = TextMediumContrast,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (state.isMedicalIdRevealed) "Hide" else "Reveal",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMediumContrast
              )
            }
          }
        }

        if (state.isMedicalIdRevealed) {
          // Vitals Grid (Blood Group, Pacemaker, Allergies)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF121418))
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "BLOOD GROUP",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = TextMuted
              )
              Text(
                text = state.victimBloodGroup,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                ),
                color = EmergencyRed
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(Color(0x1AFFFFFF))
            )

            Column(
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
            ) {
              Text(
                text = "PACEMAKER",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = TextMuted
              )
              Text(
                text = if (state.hasPacemaker) "⚠️ Active" else "None",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                ),
                color = if (state.hasPacemaker) EmergencyRed else SafeGreen
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(Color(0x1AFFFFFF))
            )

            Column(
              modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
            ) {
              Text(
                text = "ALLERGIES",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = TextMuted
              )
              Text(
                text = state.allergies.joinToString(", "),
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                ),
                color = Color(0xFFFFA000),
                maxLines = 1
              )
            }
          }

          // Chronic conditions and emergency kin contact
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Chronic: ${state.medicalConditions.joinToString(", ")}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
              color = TextMediumContrast
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Emergency Kin: ${state.emergencyContactName}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMediumContrast
              )

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SafeGreen.copy(alpha = 0.15f))
                    .border(1.dp, SafeGreen, RoundedCornerShape(6.dp))
                    .clickable { viewModel.showToast("📞 Calling ${state.emergencyContactPhone}...") }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Call,
                      contentDescription = "Call",
                      tint = SafeGreen,
                      modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "Call",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      ),
                      color = SafeGreen
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AiCyan.copy(alpha = 0.15f))
                    .border(1.dp, AiCyan, RoundedCornerShape(6.dp))
                    .clickable { viewModel.showToast("✉️ Sent automated SMS status beacon to family.") }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Message,
                      contentDescription = "SMS",
                      tint = AiCyan,
                      modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "SMS",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      ),
                      color = AiCyan
                    )
                  }
                }
              }
            }
          }
        } else {
          Text(
            text = "Medical ID locked. Tap 'Reveal' to unlock emergency clinical record.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextMuted
          )
        }
      }
    }

    // 5. AED Status & 110 BPM CPR Metronome Bar
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D10)),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (state.isAedAttached) SafeGreen.copy(alpha = 0.5f) else Color(0x22FFFFFF)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (state.isAedAttached) SafeGreen.copy(alpha = 0.2f) else Color(0xFFFFA000).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FlashOn,
              contentDescription = "AED",
              tint = if (state.isAedAttached) SafeGreen else Color(0xFFFFA000),
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = if (state.isAedAttached) "⚡ AED Active — Normal Sinus Rhythm" else "Webel Bhavan AED: 85m away",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              color = if (state.isAedAttached) SafeGreen else Color.White
            )
            Text(
              text = if (state.isAedAttached) "Shock delivered. Resume chest compressions." else "Security bringing AED unit to lobby",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
              color = TextMuted
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (state.isAedAttached) SafeGreen.copy(alpha = 0.2f) else Color(0xFFFFA000).copy(alpha = 0.2f))
            .border(
              1.dp,
              if (state.isAedAttached) SafeGreen else Color(0xFFFFA000),
              RoundedCornerShape(8.dp)
            )
            .clickable { viewModel.toggleAedAttached() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (state.isAedAttached) "AED Active" else "Attach AED",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Black,
              fontSize = 11.sp
            ),
            color = if (state.isAedAttached) SafeGreen else Color(0xFFFFA000)
          )
        }
      }
    }

    // 6. Tactical Action Flow Bar
    Spacer(modifier = Modifier.height(4.dp))

    when (state.incidentStatus) {
      "EN_ROUTE", "RESPONDER_ACCEPTED", "ACCEPTED" -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafeGreen)
            .clickable { viewModel.onArrivedClick() }
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Arrive",
              tint = Color.Black,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "📍 I HAVE ARRIVED ON SCENE",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 13.5.sp
              ),
              color = Color.Black
            )
          }
        }
      }

      "ARRIVED", "ON_SCENE" -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // CPR Metronome Button
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (state.isCprMetronomeActive) EmergencyRed else Color(0xFF1E232F))
              .border(
                1.dp,
                if (state.isCprMetronomeActive) EmergencyRed else SurfaceBorder,
                RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.toggleCprMetronome() }
              .padding(12.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "CPR",
                tint = Color.White,
                modifier = Modifier
                  .size(16.dp)
                  .then(if (state.isCprMetronomeActive) Modifier.scale(cprPulseScale) else Modifier)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (state.isCprMetronomeActive) "CPR 110 BPM (ON)" else "Start CPR Metronome",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                ),
                color = Color.White
              )
            }
          }

          // Handover to 108 Button
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(AiBlue)
              .clickable { viewModel.onHandover108Click() }
              .padding(12.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "108 Handover",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Handover to 108",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                ),
                color = Color.White
              )
            }
          }
        }
      }

      "HANDOVER_108" -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafeGreen)
            .clickable { viewModel.onResolveClick() }
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Resolve",
              tint = Color.Black,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "✨ MARK RESCUE RESOLVED (AMRI ICU)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 13.5.sp
              ),
              color = Color.Black
            )
          }
        }
      }

      "RESOLVED" -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafeGreen.copy(alpha = 0.15f))
            .border(1.dp, SafeGreen, RoundedCornerShape(12.dp))
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "✅ Incident Successfully Resolved & Handover Archived.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            ),
            color = SafeGreen
          )
        }
      }
    }

    // Toast Feedback Banner
    state.toastMessage?.let { toast ->
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF1E232F))
          .border(1.dp, AiCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = toast,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = AiCyan
          )
          Text(
            text = "✕",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TextMuted,
            modifier = Modifier.clickable { viewModel.clearToast() }
          )
        }
      }
    }
  }
}
