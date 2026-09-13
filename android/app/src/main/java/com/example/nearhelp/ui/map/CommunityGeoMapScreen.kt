package com.example.nearhelp.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.NearHelpTopBar
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

/**
 * 3 Core Emergency Categories (concise labels to avoid any text truncation)
 */
enum class EmergencyMapCategory(
  val label: String,
  val icon: ImageVector,
  val primaryColor: Color,
  val cardBg: Color,
  val cardBorder: Color,
) {
  HOSPITALS(
    label = "Hospitals",
    icon = Icons.Default.LocalHospital,
    primaryColor = Color(0xFFE11D48),
    cardBg = Color(0xFFFFF1F2),
    cardBorder = Color(0xFFFECDD3),
  ),
  PHARMACIES(
    label = "Pharmacies",
    icon = Icons.Default.LocalPharmacy,
    primaryColor = Color(0xFF059669),
    cardBg = Color(0xFFECFDF5),
    cardBorder = Color(0xFFA7F3D0),
  ),
  OTHER_HELP(
    label = "Other Help",
    icon = Icons.Default.Shield,
    primaryColor = Color(0xFF2563EB),
    cardBg = Color(0xFFEFF6FF),
    cardBorder = Color(0xFFBFDBFE),
  ),
}

data class EmergencyMapFacility(
  val id: String,
  val name: String,
  val category: EmergencyMapCategory,
  val distance: String,
  val distanceKm: Double,
  val status: String = "Open 24 hours",
  val subDetail: String,
  val phone: String,
  val pinNormalizedX: Float, // -1f (left) to 1f (right) relative to center
  val pinNormalizedY: Float, // -1f (top) to 1f (bottom) relative to center
  val icon: ImageVector,
)

/**
 * Full-Screen Map Overlay with Bottom-Left Floating Dialer Pills & Auto-Contracting Bottom Sheet
 */
@Composable
fun CommunityGeoMapScreen(
  onNavigateBack: () -> Unit,
  onNavigateToHome: () -> Unit = onNavigateBack,
  onNavigateToAssistant: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  onNavigateToTracking: () -> Unit = {},
  onNavigateToNavigation: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: CommunityGeoMapViewModel = viewModel(),
  showBottomBar: Boolean = true,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(EmergencyMapCategory.HOSPITALS) }
  var isSheetExpanded by remember { mutableStateOf(false) }
  var selectedFacilityId by remember { mutableStateOf<String?>(null) }

  val sheetHeight by animateDpAsState(
    targetValue = if (isSheetExpanded) 460.dp else 138.dp,
    animationSpec = tween(durationMillis = 300),
    label = "sheetHeight"
  )

  fun dialPhoneNumber(phone: String) {
    try {
      val clean = phone.filter { it.isDigit() || it == '+' }
      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  // Pre-configured realistic emergency facilities around Kolkata Sector V / Salt Lake
  val allFacilities = remember(uiState.incident.hospitals) {
    val hospitalsFromState = uiState.incident.hospitals.mapIndexed { index, hosp ->
      EmergencyMapFacility(
        id = hosp.id.ifBlank { "hosp_$index" },
        name = hosp.name.ifBlank { "Hospital" },
        category = EmergencyMapCategory.HOSPITALS,
        distance = "${hosp.distanceKm} km",
        distanceKm = hosp.distanceKm,
        status = "Open 24 hours",
        subDetail = "${hosp.bedAvailability} beds • ${hosp.traumaLevel}",
        phone = hosp.emergencyHelpline,
        pinNormalizedX = when (index) {
          0 -> -0.42f
          1 -> 0.45f
          else -> -0.32f
        },
        pinNormalizedY = when (index) {
          0 -> -0.36f
          1 -> -0.15f
          else -> 0.28f
        },
        icon = Icons.Default.LocalHospital,
      )
    }

    val defaultHospitals = if (hospitalsFromState.isEmpty()) {
      listOf(
        EmergencyMapFacility("hosp_1", "AMRI Hospitals — Salt Lake", EmergencyMapCategory.HOSPITALS, "1.8 km", 1.8, "Open 24 hours", "38 beds available • Trauma Level 1", "033 6606 3800", -0.45f, -0.35f, Icons.Default.LocalHospital),
        EmergencyMapFacility("hosp_2", "Apollo Multispeciality Hospitals", EmergencyMapCategory.HOSPITALS, "2.9 km", 2.9, "Open 24 hours", "64 beds available • Cardiac Center", "033 2320 3040", -0.35f, 0.25f, Icons.Default.LocalHospital),
        EmergencyMapFacility("hosp_3", "Fortis Hospital Anandapur", EmergencyMapCategory.HOSPITALS, "4.6 km", 4.6, "Open 24 hours", "51 beds available • Acute Care", "033 6628 4444", 0.48f, -0.12f, Icons.Default.LocalHospital),
        EmergencyMapFacility("hosp_4", "ILS Hospitals — Salt Lake", EmergencyMapCategory.HOSPITALS, "2.1 km", 2.1, "Open 24 hours", "22 beds available • 24/7 ER", "033 4031 5000", 0.35f, 0.32f, Icons.Default.LocalHospital),
      )
    } else {
      hospitalsFromState
    }

    val pharmacies = listOf(
      EmergencyMapFacility("pharm_1", "Apollo Pharmacy — Sector V", EmergencyMapCategory.PHARMACIES, "450 m", 0.45, "Open 24 hours", "Emergency Meds & Oxygen Cylinders", "+91 98300 22114", -0.22f, -0.18f, Icons.Default.LocalPharmacy),
      EmergencyMapFacility("pharm_2", "Frank Ross Pharmacy — Salt Lake", EmergencyMapCategory.PHARMACIES, "850 m", 0.85, "Open 24 hours", "Life-Saving Prescriptions & First Aid", "+91 98311 44552", 0.28f, -0.28f, Icons.Default.LocalPharmacy),
      EmergencyMapFacility("pharm_3", "MedPlus 24x7 Chemist", EmergencyMapCategory.PHARMACIES, "1.2 km", 1.2, "Open 24 hours", "Critical Care Supplies & Surgical Items", "+91 98305 66771", -0.38f, 0.15f, Icons.Default.LocalPharmacy),
      EmergencyMapFacility("pharm_4", "Suraksha Pharma Depot", EmergencyMapCategory.PHARMACIES, "1.7 km", 1.7, "Open 24 hours", "Prescriptions & Nebulizer Kits", "+91 98322 88990", 0.42f, 0.22f, Icons.Default.LocalPharmacy),
    )

    val otherHelp = listOf(
      EmergencyMapFacility("emerg_1", "Sector V Police Station", EmergencyMapCategory.OTHER_HELP, "900 m", 0.9, "24/7 Police Outpost", "Rapid Response Patrol • Emergency Police", "100", -0.25f, 0.22f, Icons.Default.LocalPolice),
      EmergencyMapFacility("emerg_2", "EMS Rapid Ambulance Depot", EmergencyMapCategory.OTHER_HELP, "600 m", 0.6, "24/7 EMS Dispatch", "Advanced Life Support Fleet", "108", -0.15f, -0.28f, Icons.Default.LocalHospital),
      EmergencyMapFacility("emerg_3", "Central Blood Bank & Plasma Unit", EmergencyMapCategory.OTHER_HELP, "2.2 km", 2.2, "Open 24 hours", "All Blood Groups • Emergency Support", "033 2357 3200", 0.25f, 0.35f, Icons.Default.Favorite),
      EmergencyMapFacility("emerg_4", "Disaster Emergency Helpline", EmergencyMapCategory.OTHER_HELP, "1.1 km", 1.1, "24/7 Helpline", "Toll-Free Emergency Response", "112", 0.38f, -0.24f, Icons.Default.Shield),
    )

    defaultHospitals + pharmacies + otherHelp
  }

  // Filtered facilities for active list
  val filteredFacilities = remember(allFacilities, selectedCategory, searchQuery) {
    allFacilities.filter {
      it.category == selectedCategory &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.subDetail.contains(searchQuery, ignoreCase = true))
    }.sortedBy { it.distanceKm }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VictimBackground)
  ) {
    // Top-to-Bottom Layered Box: Full Map underneath, floating controls on top
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // 1. FULL SCREEN INTERACTIVE MAP CANVAS (Pinch-to-zoom, Pan, and Auto-Contracting)
      FullScreenEmergencyMap(
        zoomLevel = uiState.zoomLevel,
        panOffsetX = uiState.panOffsetX,
        panOffsetY = uiState.panOffsetY,
        onPan = { dx, dy ->
          if (isSheetExpanded) isSheetExpanded = false
          viewModel.updatePan(dx, dy)
        },
        onZoom = { factor ->
          if (isSheetExpanded) isSheetExpanded = false
          viewModel.zoomBy(factor)
        },
        onMapClick = {
          if (isSheetExpanded) isSheetExpanded = false
        },
        facilities = allFacilities,
        selectedCategory = selectedCategory,
        selectedFacilityId = selectedFacilityId,
        onSelectFacility = { facility ->
          selectedFacilityId = facility.id
          selectedCategory = facility.category
          isSheetExpanded = true
        },
        modifier = Modifier.fillMaxSize(),
      )

      // Transparent touch listener above bottom sheet to contract menu immediately when map is tapped
      if (isSheetExpanded) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = sheetHeight)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              isSheetExpanded = false
            }
        )
      }

      // 2. FLOATING MAP CONTROLS (Recenter & Zoom) on the right
      Column(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FloatingMapButton(
          icon = Icons.Default.MyLocation,
          contentDescription = "Recenter GPS",
          onClick = {
            if (isSheetExpanded) isSheetExpanded = false
            viewModel.resetView()
          },
        )
        FloatingMapButton(
          icon = Icons.Default.Add,
          contentDescription = "Zoom In",
          onClick = {
            if (isSheetExpanded) isSheetExpanded = false
            viewModel.zoomIn()
          },
        )
        FloatingMapButton(
          icon = Icons.Default.Remove,
          contentDescription = "Zoom Out",
          onClick = {
            if (isSheetExpanded) isSheetExpanded = false
            viewModel.zoomOut()
          },
        )
      }

      // 3. TOP FLOATING HEADER & SEARCH BAR (Clean, spacious, no bulky buttons blocking the map)
      Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Unified Brand Header
        NearHelpTopBar(
          onAvatarClick = onNavigateToProfile,
        )

        // Floating Search Bar with Elevation Shadow
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x22000000))
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                "Search hospitals, pharmacies, police...",
                fontSize = 14.sp,
                color = VictimTextMuted
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = VictimPrimary,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = VictimTextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VictimPrimary.copy(alpha = 0.5f),
              unfocusedBorderColor = VictimBorder,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedTextColor = VictimTextDark,
              unfocusedTextColor = VictimTextDark,
            ),
          )
        }
      }

      // 4. BOTTOM-LEFT FLOATING DIALER PILLS (Positioned just above the hospital tab on the bottom-left)
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 14.dp, bottom = 148.dp)
      ) {
        androidx.compose.animation.AnimatedVisibility(
          visible = !isSheetExpanded,
          enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
          exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 },
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
          ) {
            // Red dialer icon with Ambulance
            CompactEmergencyPill(
              label = "Ambulance",
              borderColor = Color(0xFFFECACA),
              badgeColor = Color(0xFFDC2626),
              onClick = { dialPhoneNumber("108") },
            )

            // Blue dialer icon with Police
            CompactEmergencyPill(
              label = "Police",
              borderColor = Color(0xFFBFDBFE),
              badgeColor = Color(0xFF2563EB),
              onClick = { dialPhoneNumber("100") },
            )
          }
        }
      }

      // 5. BOTTOM-RIGHT CIRCULAR SOS BUTTON (Symmetrical to the 80.dp stack on bottom-left)
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 14.dp, bottom = 148.dp)
      ) {
        androidx.compose.animation.AnimatedVisibility(
          visible = !isSheetExpanded,
          enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
          exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 },
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF2563EB),
            shadowElevation = 6.dp,
            border = BorderStroke(1.5.dp, Color(0xFF60A5FA).copy(alpha = 0.6f)),
            modifier = Modifier
              .size(80.dp)
              .clickable { onNavigateToHome() }
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.fillMaxSize()
            ) {
              Text(
                text = "SOS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp,
              )
            }
          }
        }
      }

      // 6. BOTTOM EXPANDABLE DRAWER / POP-UP TAB PANEL
      ExpandableEmergencySheet(
        sheetHeight = sheetHeight,
        selectedCategory = selectedCategory,
        onSelectCategory = { cat ->
          selectedCategory = cat
          isSheetExpanded = true
        },
        facilities = filteredFacilities,
        isExpanded = isSheetExpanded,
        onToggleExpanded = { isSheetExpanded = !isSheetExpanded },
        onDial = { dialPhoneNumber(it) },
        onDirections = { onNavigateToNavigation() },
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    }

    // Bottom Navigation Bar (if enabled)
    if (showBottomBar) {
      VictimBottomNavBar(
        selected = VictimNavTab.MAP,
        onSelect = {
          when (it) {
            VictimNavTab.HOME -> onNavigateToHome()
            VictimNavTab.CHAT -> onNavigateToAssistant()
            VictimNavTab.MAP -> Unit
            VictimNavTab.PROFILE -> onNavigateToProfile()
          }
        }
      )
    }
  }
}

/**
 * Compact Pill Button overlay on the bottom-left: dialer icon then label (Ambulance / Police)
 */
@Composable
private fun CompactEmergencyPill(
  label: String,
  borderColor: Color,
  badgeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(100.dp),
    color = Color.White,
    shadowElevation = 6.dp,
    border = BorderStroke(1.dp, borderColor),
    modifier = modifier
      .height(36.dp)
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(start = 6.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(badgeColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Call,
          contentDescription = "Dial",
          tint = Color.White,
          modifier = Modifier.size(13.dp)
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = VictimTextDark,
      )
    }
  }
}

/**
 * Modern Expandable Bottom Sheet containing the 3 consolidated categories
 */
@Composable
private fun ExpandableEmergencySheet(
  sheetHeight: androidx.compose.ui.unit.Dp,
  selectedCategory: EmergencyMapCategory,
  onSelectCategory: (EmergencyMapCategory) -> Unit,
  facilities: List<EmergencyMapFacility>,
  isExpanded: Boolean,
  onToggleExpanded: () -> Unit,
  onDial: (String) -> Unit,
  onDirections: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .height(sheetHeight)
      .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), spotColor = Color(0x33000000))
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .background(Color.White)
      .border(1.dp, VictimBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .animateContentSize()
  ) {
    // Sheet Drag Handle & Peek Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onToggleExpanded() }
        .padding(top = 10.dp, bottom = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .width(42.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(VictimBorder)
      )
    }

    // 3 Category Tabs (Hospitals, Pharmacies, Other Help)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      EmergencyMapCategory.entries.forEach { category ->
        val isSelected = category == selectedCategory
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) category.cardBg else Color(0xFFF8FAFC))
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) category.cardBorder else VictimBorder,
              shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelectCategory(category) }
            .padding(horizontal = 6.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = if (isSelected) category.primaryColor else VictimTextMuted,
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = category.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) VictimTextDark else VictimTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    // Header with Count & Expand/Collapse Chevron
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clickable { onToggleExpanded() },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = "Nearest ${selectedCategory.label}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = VictimTextDark
        )
        Text(
          text = if (isExpanded) "${facilities.size} facilities found near you" else "Tap or slide up to view list",
          fontSize = 12.sp,
          color = VictimTextMuted
        )
      }
      IconButton(
        onClick = { onToggleExpanded() },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
          contentDescription = if (isExpanded) "Collapse" else "Expand",
          tint = VictimTextDark
        )
      }
    }

    // Scrollable Facility List (Visible when expanded)
    if (isExpanded) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(facilities, key = { it.id }) { facility ->
          FacilityCard(
            facility = facility,
            onDial = { onDial(facility.phone) },
            onDirections = onDirections,
          )
        }

        item {
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}

/**
 * Individual Facility Item Card with Call & Directions Actions
 */
@Composable
private fun FacilityCard(
  facility: EmergencyMapFacility,
  onDial: () -> Unit,
  onDirections: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(VictimShapes.Card16)
      .background(Color.White)
      .border(1.dp, VictimBorder, VictimShapes.Card16)
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(facility.category.cardBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = facility.icon,
        contentDescription = null,
        tint = facility.category.primaryColor,
        modifier = Modifier.size(24.dp)
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = facility.name,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.Bold,
        color = VictimTextDark,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = facility.distance, fontSize = 12.sp, color = VictimTextMuted)
        Text(text = " • ", fontSize = 12.sp, color = VictimTextMuted)
        Text(
          text = facility.status,
          fontSize = 12.sp,
          color = Color(0xFF16A34A),
          fontWeight = FontWeight.SemiBold
        )
      }
      Text(
        text = facility.subDetail,
        fontSize = 11.5.sp,
        color = VictimTextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Directions Action
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.clickable { onDirections() }
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(VictimBlueCard),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Navigation,
          contentDescription = "Directions",
          tint = Color(0xFF2563EB),
          modifier = Modifier.size(18.dp)
        )
      }
      Text(text = "Directions", fontSize = 10.5.sp, color = VictimTextMuted)
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Call Action
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.clickable { onDial() }
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(facility.category.cardBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Call,
          contentDescription = "Call",
          tint = facility.category.primaryColor,
          modifier = Modifier.size(18.dp)
        )
      }
      Text(text = "Call", fontSize = 10.5.sp, color = VictimTextMuted)
    }
  }
}

/**
 * Full Screen Map Canvas with Pan, Pinch-to-Zoom, Extended Grid, Beacon, and Facility Pins
 */
@Composable
private fun FullScreenEmergencyMap(
  zoomLevel: Float,
  panOffsetX: Float,
  panOffsetY: Float,
  onPan: (Float, Float) -> Unit,
  onZoom: (Float) -> Unit,
  onMapClick: () -> Unit,
  facilities: List<EmergencyMapFacility>,
  selectedCategory: EmergencyMapCategory,
  selectedFacilityId: String?,
  onSelectFacility: (EmergencyMapFacility) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Pulsing animation for the user GPS beacon
  val infiniteTransition = rememberInfiniteTransition(label = "beaconPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 2.4f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.45f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAlpha"
  )

  Box(
    modifier = modifier
      .background(Color(0xFFF1F5F9))
      .clipToBounds()
      .pointerInput(Unit) {
        detectTransformGestures(panZoomLock = false) { _, pan, zoom, _ ->
          onPan(pan.x, pan.y)
          if (zoom != 1f) {
            onZoom(zoom)
          }
        }
      }
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { onMapClick() },
          onDoubleTap = { onZoom(1.35f) }
        )
      }
  ) {
    // Map Canvas: Roads, Grid, River, Landmarks
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          scaleX = zoomLevel
          scaleY = zoomLevel
          translationX = panOffsetX
          translationY = panOffsetY
        }
    ) {
      val w = size.width
      val h = size.height

      // Water feature (Hooghly river / Salt Lake wetlands) extending beyond viewport
      val waterPath = Path().apply {
        moveTo(w * 0.05f, -h * 0.8f)
        cubicTo(w * 0.12f, h * 0.35f, w * 0.02f, h * 0.65f, w * 0.08f, h * 1.8f)
        lineTo(-w * 0.8f, h * 1.8f)
        lineTo(-w * 0.8f, -h * 0.8f)
        close()
      }
      drawPath(waterPath, color = Color(0xFFBAE6FD).copy(alpha = 0.45f))

      // Parks / Green Zones
      drawRoundRect(
        color = Color(0xFFDCFCE7).copy(alpha = 0.6f),
        topLeft = Offset(w * 0.65f, h * 0.12f),
        size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.25f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
      )
      drawRoundRect(
        color = Color(0xFFDCFCE7).copy(alpha = 0.45f),
        topLeft = Offset(-w * 0.3f, h * 0.5f),
        size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
      )

      // Extensive Secondary Grid Roads spanning well beyond viewport
      val roadColor = Color(0xFFE2E8F0)
      for (i in -6..18) {
        val y = h * (i / 11f)
        drawLine(
          color = roadColor,
          start = Offset(-w * 1.2f, y),
          end = Offset(w * 2.2f, y),
          strokeWidth = 3.dp.toPx()
        )
      }
      for (j in -6..16) {
        val x = w * (j / 9f)
        drawLine(
          color = roadColor,
          start = Offset(x, -h * 1.2f),
          end = Offset(x, h * 2.2f),
          strokeWidth = 3.dp.toPx()
        )
      }

      // Major Arterial Roads (White with border) extending across space
      val mainRoadStroke = 7.dp.toPx()
      val majorRoadColor = Color.White
      // EM Bypass Diagonal
      drawLine(
        color = majorRoadColor,
        start = Offset(-w * 0.8f, h * 1.5f),
        end = Offset(w * 1.8f, -h * 0.3f),
        strokeWidth = mainRoadStroke,
      )
      // Broadway Rd Horizontal
      drawLine(
        color = majorRoadColor,
        start = Offset(-w * 1.2f, h * 0.48f),
        end = Offset(w * 2.2f, h * 0.48f),
        strokeWidth = mainRoadStroke,
      )
      // Central Sector V Boulevard
      drawLine(
        color = majorRoadColor,
        start = Offset(w * 0.48f, -h * 1.2f),
        end = Offset(w * 0.48f, h * 2.2f),
        strokeWidth = mainRoadStroke,
      )
    }

    // Facility Markers & User Location Pin overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          scaleX = zoomLevel
          scaleY = zoomLevel
          translationX = panOffsetX
          translationY = panOffsetY
        }
    ) {
      // User Location Beacon at center
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(80.dp),
        contentAlignment = Alignment.Center
      ) {
        // Pulsing radar ripple
        Box(
          modifier = Modifier
            .size(36.dp * pulseScale)
            .clip(CircleShape)
            .background(Color(0xFF2563EB).copy(alpha = pulseAlpha))
        )
        // Blue Core Dot
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0xFF2563EB))
            .border(3.dp, Color.White, CircleShape)
        )
        // "You" Badge
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, VictimBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = "You", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        }
      }

      // Facility Pins on Map
      facilities.forEach { facility ->
        val isCurrentCategory = facility.category == selectedCategory
        val isSelectedFacility = facility.id == selectedFacilityId

        // Calculate offset from center based on normalized coordinates
        InteractiveMapPin(
          facility = facility,
          isProminent = isCurrentCategory,
          isSelected = isSelectedFacility,
          onClick = { onSelectFacility(facility) },
          modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer {
              translationX = facility.pinNormalizedX * 360f
              translationY = facility.pinNormalizedY * 420f
            }
        )
      }
    }
  }
}

/**
 * Interactive Pin for Map with dynamic label & color
 */
@Composable
private fun InteractiveMapPin(
  facility: EmergencyMapFacility,
  isProminent: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pinBg = if (isProminent) facility.category.primaryColor else Color(0xFF64748B)
  val scale = if (isSelected) 1.15f else if (isProminent) 1.0f else 0.85f

  Row(
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clip(RoundedCornerShape(12.dp))
      .background(Color.White.copy(alpha = if (isProminent) 1.0f else 0.85f))
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) facility.category.primaryColor else VictimBorder,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 7.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(CircleShape)
        .background(pinBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = facility.icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(13.dp)
      )
    }
    if (isProminent) {
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = facility.name.substringBefore("—").trim(),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = VictimTextDark,
          maxLines = 1
        )
        Text(
          text = facility.distance,
          fontSize = 10.sp,
          color = VictimTextMuted
        )
      }
    }
  }
}

/**
 * Floating Circular Action Button for Map Controls
 */
@Composable
private fun FloatingMapButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(42.dp)
      .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x22000000))
      .clip(CircleShape)
      .background(Color.White)
      .border(1.dp, VictimBorder, CircleShape)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = VictimTextDark,
      modifier = Modifier.size(20.dp)
    )
  }
}
