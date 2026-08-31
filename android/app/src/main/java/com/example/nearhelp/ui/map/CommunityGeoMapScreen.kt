package com.example.nearhelp.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.theme.ActionAmber
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.CardSurfaceVariant
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedContainer
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted
import kotlin.math.roundToInt

@Composable
fun CommunityGeoMapScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CommunityGeoMapViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  // Pulsing animation for victim beacon & PostGIS wave
  val infiniteTransition = rememberInfiniteTransition(label = "GeoMapPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.45f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "BeaconPulseScale",
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "BeaconPulseAlpha",
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Top Telemetry Header Bar
      GeoMapTopBar(
        incident = uiState.incident,
        showSqlHud = uiState.showSqlHud,
        onNavigateBack = onNavigateBack,
        onToggleSqlHud = { viewModel.toggleSqlHud() },
      )

      // 2. Map Layer Chips Bar
      GeoMapLayerChipsBar(
        enabledLayers = uiState.enabledLayers,
        responderCount = uiState.incident.responders.size,
        hospitalCount = uiState.incident.hospitals.size,
        aedCount = uiState.incident.aeds.size,
        searchRadiusKm = uiState.incident.searchRadiusKm,
        onToggleLayer = { viewModel.toggleLayer(it) },
      )

      // 3. Main Interactive Map Canvas Viewport
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val canvasWidth = constraints.maxWidth.toFloat()
          val canvasHeight = constraints.maxHeight.toFloat()
          val centerX = canvasWidth / 2f + uiState.panOffsetX
          val centerY = canvasHeight / 2f + uiState.panOffsetY
          val zoom = uiState.zoomLevel

          // Background Canvas with Cartography Grid, PostGIS waves, and Rescue Routes
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  viewModel.updatePan(dragAmount.x, dragAmount.y)
                }
              }
              .pointerInput(Unit) {
                detectTapGestures {
                  viewModel.clearSelectedEntity()
                }
              }
          ) {
            drawCartographyBase(
              centerX = centerX,
              centerY = centerY,
              zoom = zoom,
            )

            // Draw PostGIS Radar Wave
            if (uiState.enabledLayers.contains(MapLayerKey.POSTGIS_WAVE)) {
              drawPostGisRadarWave(
                centerX = centerX,
                centerY = centerY,
                radiusPx = (uiState.incident.searchRadiusKm.toFloat() * 140f) * zoom,
                pulseScale = pulseScale,
                pulseAlpha = pulseAlpha,
              )
            }

            // Draw Rescue Routes
            if (uiState.enabledLayers.contains(MapLayerKey.ROUTES)) {
              drawRescueRoutes(
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                incident = uiState.incident,
                activeResponderIndex = uiState.activeResponderIndex,
              )
            }
          }

          // Interactive Marker Overlay Elements
          // A. Victim Beacon Marker
          if (uiState.enabledLayers.contains(MapLayerKey.VICTIM)) {
            VictimBeaconMarker(
              centerX = centerX,
              centerY = centerY,
              pulseScale = pulseScale,
              pulseAlpha = pulseAlpha,
              onClick = {
                viewModel.selectEntity(SelectedMapEntity.VictimEntity())
              }
            )
          }

          // B. Responder Beacon Markers
          if (uiState.enabledLayers.contains(MapLayerKey.RESPONDERS)) {
            uiState.incident.responders.forEachIndexed { index, resp ->
              val offsetX = centerX + ((resp.lng - uiState.incident.lng) * 28000f * zoom).toFloat()
              val offsetY = centerY - ((resp.lat - uiState.incident.lat) * 28000f * zoom).toFloat()

              ResponderBeaconMarker(
                responder = resp,
                offsetX = offsetX,
                offsetY = offsetY,
                isSelected = index == uiState.activeResponderIndex,
                onClick = {
                  viewModel.setActiveResponder(index)
                  viewModel.selectEntity(SelectedMapEntity.ResponderEntity(resp))
                }
              )
            }
          }

          // C. Hospital Markers
          if (uiState.enabledLayers.contains(MapLayerKey.HOSPITALS)) {
            uiState.incident.hospitals.forEach { hospital ->
              val offsetX = centerX + ((hospital.lng - uiState.incident.lng) * 18000f * zoom).toFloat()
              val offsetY = centerY - ((hospital.lat - uiState.incident.lat) * 18000f * zoom).toFloat()

              HospitalBeaconMarker(
                hospital = hospital,
                offsetX = offsetX,
                offsetY = offsetY,
                onClick = {
                  viewModel.selectEntity(SelectedMapEntity.HospitalEntity(hospital))
                }
              )
            }
          }

          // D. AED Markers
          if (uiState.enabledLayers.contains(MapLayerKey.AEDS)) {
            uiState.incident.aeds.forEach { aed ->
              val offsetX = centerX + ((aed.lng - uiState.incident.lng) * 32000f * zoom).toFloat()
              val offsetY = centerY - ((aed.lat - uiState.incident.lat) * 32000f * zoom).toFloat()

              AedBeaconMarker(
                aed = aed,
                offsetX = offsetX,
                offsetY = offsetY,
                onClick = {
                  viewModel.selectEntity(SelectedMapEntity.AedEntity(aed))
                }
              )
            }
          }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Recenter)
        FloatingMapControls(
          onZoomIn = { viewModel.zoomIn() },
          onZoomOut = { viewModel.zoomOut() },
          onRecenter = { viewModel.resetView() },
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
        )

        // PostGIS SQL Query HUD Panel (Collapsible)
        androidx.compose.animation.AnimatedVisibility(
          visible = uiState.showSqlHud,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(12.dp)
        ) {
          PostGisSqlHudCard(
            querySnippet = uiState.postgisQuerySnippet,
            onClose = { viewModel.toggleSqlHud() }
          )
        }
      }
    }

    // 4. Interactive Bottom Sheet for Selected Entity
    AnimatedVisibility(
      visible = uiState.selectedEntity != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
    ) {
      uiState.selectedEntity?.let { entity ->
        EntityDetailsBottomSheet(
          entity = entity,
          onDismiss = { viewModel.clearSelectedEntity() }
        )
      }
    }
  }
}

// --------------------------------------------------------------------------
// TOP TELEMETRY BAR
// --------------------------------------------------------------------------
@Composable
private fun GeoMapTopBar(
  incident: SpatialIncident,
  showSqlHud: Boolean,
  onNavigateBack: () -> Unit,
  onToggleSqlHud: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFF090B10))
      .border(1.dp, SurfaceBorder)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextHighContrast,
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(EmergencyRed, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Kolkata Spatial Dispatch Engine",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextHighContrast,
          )
        }
        Text(
          text = "Salt Lake Sec V • ${incident.lat}°N, ${incident.lng}°E • GPS ±2.8m",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, fontFamily = FontFamily.Monospace),
          color = AiCyan,
        )
      }
    }

    // SQL Toggle Button
    OutlinedButton(
      onClick = onToggleSqlHud,
      shape = RoundedCornerShape(8.dp),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = if (showSqlHud) AiCyan.copy(alpha = 0.15f) else Color.Transparent,
        contentColor = if (showSqlHud) AiCyan else TextMediumContrast,
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (showSqlHud) AiCyan else SurfaceBorder
      ),
    ) {
      Icon(imageVector = Icons.Default.Code, contentDescription = "SQL", modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text("PostGIS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
  }
}

// --------------------------------------------------------------------------
// LAYER CHIPS BAR
// --------------------------------------------------------------------------
@Composable
private fun GeoMapLayerChipsBar(
  enabledLayers: Set<MapLayerKey>,
  responderCount: Int,
  hospitalCount: Int,
  aedCount: Int,
  searchRadiusKm: Double,
  onToggleLayer: (MapLayerKey) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFF0F1218))
      .border(1.dp, SurfaceBorder.copy(alpha = 0.4f))
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    MapLayerKey.entries.forEach { layer ->
      val isEnabled = enabledLayers.contains(layer)
      val labelText = when (layer) {
        MapLayerKey.VICTIM -> "📍 Victim SOS"
        MapLayerKey.RESPONDERS -> "🏃 Responders ($responderCount)"
        MapLayerKey.HOSPITALS -> "🏥 Hospitals ($hospitalCount)"
        MapLayerKey.AEDS -> "⚡ AEDs ($aedCount)"
        MapLayerKey.POSTGIS_WAVE -> "🌊 PostGIS (${searchRadiusKm}km)"
        MapLayerKey.ROUTES -> "🛤️ Rescue Routes"
      }
      val activeColor = when (layer) {
        MapLayerKey.VICTIM -> EmergencyRed
        MapLayerKey.RESPONDERS -> SafeGreen
        MapLayerKey.HOSPITALS -> AiCyan
        MapLayerKey.AEDS -> ActionAmber
        MapLayerKey.POSTGIS_WAVE -> AiCyan
        MapLayerKey.ROUTES -> SafeGreen
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isEnabled) activeColor.copy(alpha = 0.18f) else Color(0xFF161922),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isEnabled) activeColor.copy(alpha = 0.8f) else SurfaceBorder
        ),
        modifier = Modifier.clickable { onToggleLayer(layer) }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(if (isEnabled) activeColor else Color.Gray, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = labelText,
            color = if (isEnabled) activeColor else TextMediumContrast,
            fontSize = 11.sp,
            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}

// --------------------------------------------------------------------------
// CANVAS DRAWING EXTENSIONS (Kolkata Cartography, Waves, Polyline Routes)
// --------------------------------------------------------------------------
private fun DrawScope.drawCartographyBase(
  centerX: Float,
  centerY: Float,
  zoom: Float,
) {
  // Grid background lines
  val step = 40f * zoom
  var x = 0f
  while (x < size.width) {
    drawLine(
      color = Color(0xFF1E2430).copy(alpha = 0.35f),
      start = Offset(x, 0f),
      end = Offset(x, size.height),
      strokeWidth = 1f
    )
    x += step
  }
  var y = 0f
  while (y < size.height) {
    drawLine(
      color = Color(0xFF1E2430).copy(alpha = 0.35f),
      start = Offset(0f, y),
      end = Offset(size.width, y),
      strokeWidth = 1f
    )
    y += step
  }

  // Salt Lake Wetlands Water body
  val waterPath = Path().apply {
    moveTo(size.width * 0.75f, 0f)
    quadraticTo(size.width * 0.85f, size.height * 0.35f, size.width * 0.95f, size.height)
    lineTo(size.width, size.height)
    lineTo(size.width, 0f)
    close()
  }
  drawPath(
    path = waterPath,
    color = Color(0xFF071B2B).copy(alpha = 0.8f)
  )

  // Sector V Arterial Main Roads
  drawLine(
    color = Color(0xFF1C2433),
    start = Offset(0f, centerY - 80f * zoom),
    end = Offset(size.width, centerY - 80f * zoom),
    strokeWidth = 14f * zoom
  )
  drawLine(
    color = Color(0xFF2E384D),
    start = Offset(0f, centerY - 80f * zoom),
    end = Offset(size.width, centerY - 80f * zoom),
    strokeWidth = 2f * zoom,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
  )

  // Vertical Ring Road
  drawLine(
    color = Color(0xFF1C2433),
    start = Offset(centerX, 0f),
    end = Offset(centerX, size.height),
    strokeWidth = 12f * zoom
  )
  drawLine(
    color = Color(0xFF2E384D),
    start = Offset(centerX, 0f),
    end = Offset(centerX, size.height),
    strokeWidth = 2f * zoom,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
  )

  // Tech Park Block Polygons (Godrej Waterside, DP Block, DLF)
  drawRect(
    color = Color(0xFF0F1522),
    topLeft = Offset(centerX + 20f * zoom, centerY - 60f * zoom),
    size = Size(140f * zoom, 100f * zoom)
  )
  drawRect(
    color = Color(0xFF253347),
    topLeft = Offset(centerX + 20f * zoom, centerY - 60f * zoom),
    size = Size(140f * zoom, 100f * zoom),
    style = Stroke(width = 1.5f * zoom)
  )

  drawRect(
    color = Color(0xFF0D121B),
    topLeft = Offset(centerX - 170f * zoom, centerY - 60f * zoom),
    size = Size(140f * zoom, 100f * zoom)
  )
  drawRect(
    color = Color(0xFF1C2738),
    topLeft = Offset(centerX - 170f * zoom, centerY - 60f * zoom),
    size = Size(140f * zoom, 100f * zoom),
    style = Stroke(width = 1.2f * zoom)
  )
}

private fun DrawScope.drawPostGisRadarWave(
  centerX: Float,
  centerY: Float,
  radiusPx: Float,
  pulseScale: Float,
  pulseAlpha: Float,
) {
  val victimCenter = Offset(centerX, centerY)

  // Search boundary dashed circle
  drawCircle(
    color = AiCyan.copy(alpha = 0.12f),
    radius = radiusPx,
    center = victimCenter,
  )
  drawCircle(
    color = AiCyan.copy(alpha = 0.8f),
    radius = radiusPx,
    center = victimCenter,
    style = Stroke(
      width = 2f,
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    )
  )

  // Expanding radar pulse wave
  drawCircle(
    color = AiCyan.copy(alpha = pulseAlpha * 0.6f),
    radius = radiusPx * pulseScale,
    center = victimCenter,
    style = Stroke(width = 2f)
  )

  // Concentric inner ring
  drawCircle(
    color = AiCyan.copy(alpha = 0.35f),
    radius = radiusPx * 0.45f,
    center = victimCenter,
    style = Stroke(width = 1.2f)
  )
}

private fun DrawScope.drawRescueRoutes(
  centerX: Float,
  centerY: Float,
  zoom: Float,
  incident: SpatialIncident,
  activeResponderIndex: Int,
) {
  val victimPos = Offset(centerX, centerY)

  incident.responders.forEachIndexed { index, resp ->
    val respPos = Offset(
      centerX + ((resp.lng - incident.lng) * 28000f * zoom).toFloat(),
      centerY - ((resp.lat - incident.lat) * 28000f * zoom).toFloat()
    )
    val isSelected = index == activeResponderIndex

    val routePath = Path().apply {
      moveTo(respPos.x, respPos.y)
      val controlX = (respPos.x + victimPos.x) / 2f + (if (index == 0) 30f * zoom else -30f * zoom)
      val controlY = (respPos.y + victimPos.y) / 2f
      quadraticTo(controlX, controlY, victimPos.x, victimPos.y)
    }

    drawPath(
      path = routePath,
      color = if (isSelected) SafeGreen else SafeGreen.copy(alpha = 0.45f),
      style = Stroke(
        width = if (isSelected) 3.5f * zoom else 2f * zoom,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
      )
    )
  }
}

// --------------------------------------------------------------------------
// BEACON MARKERS (Victim, Responders, Hospitals, AEDs)
// --------------------------------------------------------------------------
@Composable
private fun VictimBeaconMarker(
  centerX: Float,
  centerY: Float,
  pulseScale: Float,
  pulseAlpha: Float,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .offset { IntOffset((centerX - 24.dp.toPx()).roundToInt(), (centerY - 24.dp.toPx()).roundToInt()) }
      .size(48.dp)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    // Outer breathing pulse
    Box(
      modifier = Modifier
        .size(44.dp * pulseScale)
        .background(EmergencyRedGlow.copy(alpha = pulseAlpha), CircleShape)
    )

    // Inner Pin
    Box(
      modifier = Modifier
        .size(32.dp)
        .background(EmergencyRed, CircleShape)
        .border(2.dp, Color.White, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "SOS Beacon",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun ResponderBeaconMarker(
  responder: ResponderMarker,
  offsetX: Float,
  offsetY: Float,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .offset { IntOffset((offsetX - 32.dp.toPx()).roundToInt(), (offsetY - 32.dp.toPx()).roundToInt()) }
      .clickable { onClick() }
  ) {
    // Responder Badge Pill
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = if (isSelected) SafeGreen else Color(0xFF10281A),
      border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen),
      modifier = Modifier.padding(bottom = 2.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${responder.name.split(' ').first()} • ${responder.etaMinutes}m",
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = if (isSelected) Color.Black else SafeGreen
        )
      }
    }

    // Pin Head Icon
    Box(
      modifier = Modifier
        .size(26.dp)
        .background(if (isSelected) SafeGreen else Color(0xFF0F3B20), CircleShape)
        .border(1.5.dp, SafeGreen, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (responder.isDoctor) Icons.Default.LocalHospital else Icons.Default.Person,
        contentDescription = responder.name,
        tint = if (isSelected) Color.Black else SafeGreen,
        modifier = Modifier.size(15.dp)
      )
    }
  }
}

@Composable
private fun HospitalBeaconMarker(
  hospital: HospitalMarker,
  offsetX: Float,
  offsetY: Float,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .offset { IntOffset((offsetX - 36.dp.toPx()).roundToInt(), (offsetY - 28.dp.toPx()).roundToInt()) }
      .clickable { onClick() }
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color(0xFF0C2433),
      border = androidx.compose.foundation.BorderStroke(1.dp, AiCyan.copy(alpha = 0.7f)),
      modifier = Modifier.padding(bottom = 2.dp)
    ) {
      Text(
        text = "${hospital.name.split(' ').first()} (${hospital.bedAvailability} Beds)",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = AiCyan,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
      )
    }

    Box(
      modifier = Modifier
        .size(24.dp)
        .background(Color(0xFF081C2B), CircleShape)
        .border(1.5.dp, AiCyan, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.LocalHospital,
        contentDescription = hospital.name,
        tint = AiCyan,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
private fun AedBeaconMarker(
  aed: AedMarker,
  offsetX: Float,
  offsetY: Float,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .offset { IntOffset((offsetX - 24.dp.toPx()).roundToInt(), (offsetY - 24.dp.toPx()).roundToInt()) }
      .clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(22.dp)
        .background(Color(0xFF332005), CircleShape)
        .border(1.5.dp, ActionAmber, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.FlashOn,
        contentDescription = aed.buildingName,
        tint = ActionAmber,
        modifier = Modifier.size(13.dp)
      )
    }
  }
}

// --------------------------------------------------------------------------
// FLOATING CONTROLS & SQL HUD
// --------------------------------------------------------------------------
@Composable
private fun FloatingMapControls(
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onRecenter: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141722).copy(alpha = 0.92f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      IconButton(onClick = onZoomIn, modifier = Modifier.size(34.dp)) {
        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextHighContrast, modifier = Modifier.size(18.dp))
      }
      IconButton(onClick = onZoomOut, modifier = Modifier.size(34.dp)) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextHighContrast, modifier = Modifier.size(18.dp))
      }
      IconButton(onClick = onRecenter, modifier = Modifier.size(34.dp)) {
        Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = AiCyan, modifier = Modifier.size(18.dp))
      }
    }
  }
}

@Composable
private fun PostGisSqlHudCard(
  querySnippet: String,
  onClose: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F18).copy(alpha = 0.95f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, AiCyan.copy(alpha = 0.4f)),
    modifier = Modifier.width(310.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Code, contentDescription = "SQL", tint = AiCyan, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "PostGIS ST_DWithin Telemetry",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AiCyan
          )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(14.dp))
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = querySnippet,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFB0D5FF),
        lineHeight = 13.sp
      )
    }
  }
}

// --------------------------------------------------------------------------
// BOTTOM SHEET FOR SELECTED ENTITY DETAILS
// --------------------------------------------------------------------------
@Composable
private fun EntityDetailsBottomSheet(
  entity: SelectedMapEntity,
  onDismiss: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        when (entity) {
          is SelectedMapEntity.VictimEntity -> {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(EmergencyRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = entity.title,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextHighContrast
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = entity.locationName, style = MaterialTheme.typography.bodySmall, color = TextMediumContrast)
              Text(text = "Severity: ${entity.severity}", style = MaterialTheme.typography.labelSmall, color = EmergencyRed)
            }
          }

          is SelectedMapEntity.ResponderEntity -> {
            val resp = entity.responder
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(SafeGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = resp.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextHighContrast
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = resp.role, style = MaterialTheme.typography.bodySmall, color = SafeGreen)
              Text(
                text = "ETA: ${resp.etaMinutes} min • Distance: ${resp.distanceMeters}m • Reliability: ${(resp.reliabilityScore * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumContrast
              )
            }
          }

          is SelectedMapEntity.HospitalEntity -> {
            val hosp = entity.hospital
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(AiCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = hosp.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextHighContrast
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = hosp.address, style = MaterialTheme.typography.bodySmall, color = TextMediumContrast, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(
                text = "Live Capacity: ${hosp.bedAvailability} Beds • ${hosp.icuAvailability} ICUs • ${hosp.distanceKm} km away",
                style = MaterialTheme.typography.labelSmall,
                color = AiCyan
              )
            }
          }

          is SelectedMapEntity.AedEntity -> {
            val aed = entity.aed
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(ActionAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "AED: ${aed.buildingName}",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextHighContrast
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = aed.locationDescription, style = MaterialTheme.typography.bodySmall, color = TextMediumContrast)
              Text(
                text = "Access: ${aed.accessCode} • ${aed.distanceMeters}m away",
                style = MaterialTheme.typography.labelSmall,
                color = ActionAmber
              )
            }
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onDismiss,
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Directions, contentDescription = "Directions", tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Route Assist", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
          Icon(Icons.Default.Call, contentDescription = "Call", tint = TextHighContrast, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Emergency Comms", color = TextHighContrast)
        }
      }
    }
  }
}
