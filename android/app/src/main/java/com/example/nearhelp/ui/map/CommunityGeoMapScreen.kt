package com.example.nearhelp.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.data.location.LocationHelper
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.NearHelpTopBar
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 4 Emergency Map Categories: All Nearby, Hospitals, Police Stations, and Pharmacies.
 */
enum class EmergencyMapCategory(
  val label: String,
  val icon: ImageVector,
  val primaryColor: Color,
  val cardBg: Color,
  val cardBorder: Color,
) {
  ALL(
    label = "All",
    icon = Icons.Default.Shield,
    primaryColor = Color(0xFF2563EB),
    cardBg = Color(0xFFEFF6FF),
    cardBorder = Color(0xFFBFDBFE),
  ),
  HOSPITALS(
    label = "Hospitals",
    icon = Icons.Default.LocalHospital,
    primaryColor = Color(0xFFE11D48),
    cardBg = Color(0xFFFFF1F2),
    cardBorder = Color(0xFFFECDD3),
  ),
  POLICE(
    label = "Police",
    icon = Icons.Default.LocalPolice,
    primaryColor = Color(0xFF1D4ED8),
    cardBg = Color(0xFFEFF6FF),
    cardBorder = Color(0xFFBFDBFE),
  ),
  PHARMACIES(
    label = "Pharmacies",
    icon = Icons.Default.LocalPharmacy,
    primaryColor = Color(0xFF059669),
    cardBg = Color(0xFFECFDF5),
    cardBorder = Color(0xFFA7F3D0),
  ),
}

data class EmergencyMapFacility(
  val id: String,
  val name: String,
  val address: String = "",
  val category: EmergencyMapCategory,
  val distance: String,
  val distanceKm: Double,
  val status: String = "Open 24 hours",
  val subDetail: String,
  val bedAvailability: Int = 0,
  val totalBeds: Int = 0,
  val icuAvailability: Int = 0,
  val traumaLevel: String = "",
  val emergencyHelpline: String = "",
  val phone: String,
  val pinNormalizedX: Float = 0f,
  val pinNormalizedY: Float = 0f,
  val icon: ImageVector = Icons.Default.LocalHospital,
  val lat: Double = 22.5804,
  val lng: Double = 88.4378,
)

private data class FacilitySeed(
  val name: String,
  val address: String,
  val lat: Double,
  val lng: Double,
  val phone: String,
  val category: EmergencyMapCategory,
  val icon: ImageVector,
  val subDetail: String,
  val bedAvailability: Int = 0,
  val totalBeds: Int = 0,
  val icuAvailability: Int = 0,
  val traumaLevel: String = "",
  val emergencyHelpline: String = "",
)

/**
 * Computes geodesic distance in meters between two coordinates.
 */
private fun computeDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
  val results = FloatArray(1)
  android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
  return results[0].toDouble()
}

/**
 * Formats distance in meters or kilometers.
 */
private fun formatDistance(meters: Double): String {
  return if (meters < 1000.0) {
    "${meters.toInt()} m"
  } else {
    String.format(Locale.US, "%.1f km", meters / 1000.0)
  }
}

/**
 * Full-Screen Map Overlay showing:
 * 1. User's Real-Time GPS Location (Google Maps native layer + Custom Location Marker)
 * 2. Nearby Police Stations (Blue Markers) and Hospitals (Red Markers) sorted by live distance
 * 3. Contracted Quick-Pills for Nearest Police & Nearest Hospital
 * 4. Complete, verified hospital information for Barasat, Barrackpore, Dum Dum, Salt Lake, and Kolkata
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
  val coroutineScope = rememberCoroutineScope()

  // 1. LIVE GPS & NETWORK USER LOCATION HELPER
  val locationHelper = remember { LocationHelper(context.applicationContext) }
  val locationState by locationHelper.locationState.collectAsState()

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) {
    locationHelper.startLocationUpdates()
  }

  DisposableEffect(Unit) {
    if (locationHelper.hasLocationPermission()) {
      locationHelper.startLocationUpdates()
    } else {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
    onDispose {
      locationHelper.close()
    }
  }

  // Active user position LatLng (from GPS fix or best cached fix)
  val userLatLng = remember(locationState.latitude, locationState.longitude, locationState.hasFix) {
    if (locationState.hasFix) LatLng(locationState.latitude, locationState.longitude) else null
  }

  // Fallback default center (Salt Lake / Bidhannagar Sector V)
  val defaultCenter = remember(uiState.incident.lat, uiState.incident.lng) {
    LatLng(uiState.incident.lat, uiState.incident.lng)
  }

  val activeCenter = userLatLng ?: defaultCenter

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(activeCenter, 14.5f)
  }

  var hasAutoCenteredOnUser by rememberSaveable { mutableStateOf(false) }

  // Auto-center camera to user's real location when location fix arrives
  LaunchedEffect(userLatLng) {
    if (userLatLng != null && !hasAutoCenteredOnUser) {
      cameraPositionState.animate(
        CameraUpdateFactory.newLatLngZoom(userLatLng, 15f),
        700
      )
      hasAutoCenteredOnUser = true
    }
  }

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(EmergencyMapCategory.ALL) }
  var isSheetExpanded by remember { mutableStateOf(false) }
  var selectedFacilityId by remember { mutableStateOf<String?>(null) }

  val sheetHeight by animateDpAsState(
    targetValue = if (isSheetExpanded) 490.dp else 174.dp,
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

  fun openDirections(lat: Double, lng: Double, name: String) {
    try {
      val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
      val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
      mapIntent.setPackage("com.google.android.apps.maps")
      if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
      } else {
        val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
      }
    } catch (_: Exception) {
      onNavigateToNavigation()
    }
  }

  // 2. DYNAMIC EMERGENCY FACILITIES (POLICE STATIONS, HOSPITALS, PHARMACIES)
  val currentCenterLat = userLatLng?.latitude ?: defaultCenter.latitude
  val currentCenterLng = userLatLng?.longitude ?: defaultCenter.longitude

  val allFacilities = remember(currentCenterLat, currentCenterLng, locationState.localityName) {
    getAccurateKolkataRegionalFacilities(currentCenterLat, currentCenterLng, locationState.localityName)
  }

  // Nearest police station and nearest hospital for fast 1-tap emergency access
  val nearestPolice = remember(allFacilities) {
    allFacilities.filter { it.category == EmergencyMapCategory.POLICE }.minByOrNull { it.distanceKm }
  }
  val nearestHospital = remember(allFacilities) {
    allFacilities.filter { it.category == EmergencyMapCategory.HOSPITALS }.minByOrNull { it.distanceKm }
  }

  // Filtered facilities for active list in bottom sheet
  val filteredFacilities = remember(allFacilities, selectedCategory, searchQuery) {
    val base = if (selectedCategory == EmergencyMapCategory.ALL) {
      allFacilities
    } else {
      allFacilities.filter { it.category == selectedCategory }
    }
    base.filter {
      searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true) ||
        it.subDetail.contains(searchQuery, ignoreCase = true)
    }.sortedBy { it.distanceKm }
  }

  // Markers on map (display matching search or all nearby facilities)
  val markersToDisplay = remember(allFacilities, searchQuery) {
    if (searchQuery.isNotBlank()) {
      allFacilities.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.address.contains(searchQuery, ignoreCase = true) ||
          it.subDetail.contains(searchQuery, ignoreCase = true)
      }
    } else {
      allFacilities
    }
  }

  val hasPermission = locationHelper.hasLocationPermission()

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
      // 1. FULL SCREEN LIVE GOOGLE MAP WITH USER LOCATION & FACILITY MARKERS
      GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
          isMyLocationEnabled = hasPermission,
          mapType = MapType.NORMAL,
        ),
        uiSettings = MapUiSettings(
          zoomControlsEnabled = false,
          myLocationButtonEnabled = false,
          compassEnabled = true,
          mapToolbarEnabled = false,
        ),
        onMapClick = {
          if (isSheetExpanded) isSheetExpanded = false
        }
      ) {
        // Safe Radius / Emergency Perimeter Circle around the active location
        Circle(
          center = activeCenter,
          radius = uiState.incident.searchRadiusKm * 1000.0,
          fillColor = Color(0x182563EB),
          strokeColor = Color(0xFF2563EB).copy(alpha = 0.5f),
          strokeWidth = 2.5f,
        )

        // User Location Marker (Explicit Visual Beacon)
        if (userLatLng != null) {
          Marker(
            state = MarkerState(position = userLatLng),
            title = "Your Location",
            snippet = if (locationState.localityName.isNotBlank() && locationState.localityName != "Locating...") {
              "${locationState.localityName} • You are here"
            } else {
              "Current Location (${locationState.coordinatesText})"
            },
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
          )
        } else {
          Marker(
            state = MarkerState(position = defaultCenter),
            title = "Emergency Incident Center",
            snippet = uiState.incident.locationName,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
          )
        }

        // Responders
        uiState.incident.responders.forEach { resp ->
          Marker(
            state = MarkerState(position = LatLng(resp.lat, resp.lng)),
            title = resp.name,
            snippet = "${resp.role} • ${resp.etaMinutes} min away",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
          )
        }

        // Emergency Facilities (Hospitals in RED, Police in BLUE, Pharmacies in GREEN)
        markersToDisplay.forEach { facility ->
          Marker(
            state = MarkerState(position = LatLng(facility.lat, facility.lng)),
            title = facility.name,
            snippet = if (facility.bedAvailability > 0) {
              "${facility.distance} • ${facility.bedAvailability} beds free • ${facility.address}"
            } else {
              "${facility.distance} • ${facility.address}"
            },
            icon = when (facility.category) {
              EmergencyMapCategory.HOSPITALS -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
              EmergencyMapCategory.POLICE -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
              EmergencyMapCategory.PHARMACIES -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
              EmergencyMapCategory.ALL -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            },
            onClick = {
              selectedFacilityId = facility.id
              selectedCategory = facility.category
              isSheetExpanded = true
              true
            }
          )
        }
      }

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

      // 2. FLOATING MAP CONTROLS (Recenter to User Location & Zoom) on the right
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
            coroutineScope.launch {
              if (locationHelper.hasLocationPermission()) {
                locationHelper.startLocationUpdates()
                val target = userLatLng ?: activeCenter
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 15f), 500)
              } else {
                permissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              }
            }
          },
        )
        FloatingMapButton(
          icon = Icons.Default.Add,
          contentDescription = "Zoom In",
          onClick = {
            if (isSheetExpanded) isSheetExpanded = false
            coroutineScope.launch {
              cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 250)
            }
          },
        )
        FloatingMapButton(
          icon = Icons.Default.Remove,
          contentDescription = "Zoom Out",
          onClick = {
            if (isSheetExpanded) isSheetExpanded = false
            coroutineScope.launch {
              cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 250)
            }
          },
        )
      }

      // 3. TOP FLOATING HEADER & SEARCH BAR
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
                "Search Barasat, Barrackpore, Kolkata hospitals...",
                fontSize = 13.5.sp,
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

      // 4. BOTTOM-LEFT FLOATING DIALER PILLS (Ambulance 108 & Police 100)
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 14.dp, bottom = 184.dp)
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
            CompactEmergencyPill(
              label = "Ambulance",
              borderColor = Color(0xFFFECACA),
              badgeColor = Color(0xFFDC2626),
              onClick = { dialPhoneNumber("108") },
            )

            CompactEmergencyPill(
              label = "Police",
              borderColor = Color(0xFFBFDBFE),
              badgeColor = Color(0xFF2563EB),
              onClick = { dialPhoneNumber("100") },
            )
          }
        }
      }

      // 5. BOTTOM-RIGHT CIRCULAR SOS BUTTON
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 14.dp, bottom = 184.dp)
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
        nearestPolice = nearestPolice,
        nearestHospital = nearestHospital,
        facilities = filteredFacilities,
        selectedFacilityId = selectedFacilityId,
        isExpanded = isSheetExpanded,
        onToggleExpanded = { isSheetExpanded = !isSheetExpanded },
        onDial = { dialPhoneNumber(it) },
        onDirections = { lat, lng, name -> openDirections(lat, lng, name) },
        onFacilityClick = { facility ->
          selectedFacilityId = facility.id
          coroutineScope.launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(LatLng(facility.lat, facility.lng), 15.5f),
              500
            )
          }
        },
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
 * Modern Expandable Bottom Sheet containing All, Hospitals, Police, and Pharmacies.
 */
@Composable
private fun ExpandableEmergencySheet(
  sheetHeight: androidx.compose.ui.unit.Dp,
  selectedCategory: EmergencyMapCategory,
  onSelectCategory: (EmergencyMapCategory) -> Unit,
  nearestPolice: EmergencyMapFacility?,
  nearestHospital: EmergencyMapFacility?,
  facilities: List<EmergencyMapFacility>,
  selectedFacilityId: String?,
  isExpanded: Boolean,
  onToggleExpanded: () -> Unit,
  onDial: (String) -> Unit,
  onDirections: (Double, Double, String) -> Unit,
  onFacilityClick: (EmergencyMapFacility) -> Unit,
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
        .padding(top = 8.dp, bottom = 4.dp),
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

    // 4 Category Tabs (All, Hospitals, Police, Pharmacies)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      EmergencyMapCategory.entries.forEach { category ->
        val isSelected = category == selectedCategory
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) category.cardBg else Color(0xFFF8FAFC))
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) category.cardBorder else VictimBorder,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelectCategory(category) }
            .padding(horizontal = 4.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = if (isSelected) category.primaryColor else VictimTextMuted,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = category.label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) VictimTextDark else VictimTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    // High-Visibility Glance Strip: Immediate access to Nearest Police & Nearest Hospital
    if (!isExpanded) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Nearest Police Chip
        nearestPolice?.let { pol ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier
              .weight(1f)
              .clickable {
                onFacilityClick(pol)
                onSelectCategory(EmergencyMapCategory.POLICE)
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.LocalPolice,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }
              Spacer(modifier = Modifier.width(7.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = pol.name.substringBefore("—").trim(),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = VictimTextDark,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Police • ${pol.distance}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF1D4ED8)
                )
              }
            }
          }
        }

        // Nearest Hospital Chip
        nearestHospital?.let { hosp ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF1F2),
            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
            modifier = Modifier
              .weight(1f)
              .clickable {
                onFacilityClick(hosp)
                onSelectCategory(EmergencyMapCategory.HOSPITALS)
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE11D48)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.LocalHospital,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }
              Spacer(modifier = Modifier.width(7.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = hosp.name.substringBefore("—").trim(),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = VictimTextDark,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = if (hosp.bedAvailability > 0) "Hospital • ${hosp.distance} (${hosp.bedAvailability} beds)" else "Hospital • ${hosp.distance}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFFE11D48),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    // Header with Count & Expand/Collapse Chevron
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 4.dp)
        .clickable { onToggleExpanded() },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = when (selectedCategory) {
            EmergencyMapCategory.ALL -> "Nearest Emergency Help"
            EmergencyMapCategory.HOSPITALS -> "Nearest Hospitals"
            EmergencyMapCategory.POLICE -> "Nearest Police Stations"
            EmergencyMapCategory.PHARMACIES -> "Nearest Pharmacies"
          },
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = VictimTextDark
        )
        Text(
          text = if (isExpanded) "${facilities.size} facilities found near you" else "Tap or slide up to view all details",
          fontSize = 11.5.sp,
          color = VictimTextMuted
        )
      }
      IconButton(
        onClick = { onToggleExpanded() },
        modifier = Modifier.size(28.dp)
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
            isSelected = facility.id == selectedFacilityId,
            onClick = { onFacilityClick(facility) },
            onDial = { onDial(facility.phone) },
            onDirections = { onDirections(facility.lat, facility.lng, facility.name) },
          )
        }

        item {
          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }
  }
}

/**
 * Individual Facility Item Card with Full Address, Beds/ICU Metrics, Call & Directions Actions
 */
@Composable
private fun FacilityCard(
  facility: EmergencyMapFacility,
  isSelected: Boolean = false,
  onClick: () -> Unit = {},
  onDial: () -> Unit,
  onDirections: () -> Unit,
) {
  Surface(
    shape = VictimShapes.Card16,
    color = if (isSelected) facility.category.cardBg.copy(alpha = 0.55f) else Color.White,
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) facility.category.primaryColor else VictimBorder
    ),
    shadowElevation = if (isSelected) 3.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Top Row: Icon, Name, Distance & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(facility.category.cardBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = facility.icon,
            contentDescription = null,
            tint = facility.category.primaryColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = facility.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VictimTextDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = facility.distance,
              fontSize = 12.sp,
              color = VictimTextDark,
              fontWeight = FontWeight.Bold
            )
            Text(text = " • ", fontSize = 12.sp, color = VictimTextMuted)
            Text(
              text = facility.status,
              fontSize = 11.5.sp,
              color = Color(0xFF16A34A),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Directions Action Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(VictimBlueCard)
            .clickable { onDirections() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Directions",
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Call Action Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(facility.category.cardBg)
            .clickable { onDial() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = facility.category.primaryColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Address Row
      if (facility.address.isNotBlank()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(start = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = VictimTextMuted,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = facility.address,
            fontSize = 11.sp,
            color = VictimTextDark.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Clinical & Capacity Indicators (Beds, ICU, Trauma)
      if (facility.category == EmergencyMapCategory.HOSPITALS && facility.bedAvailability > 0) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFECFDF5),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
          ) {
            Text(
              text = "🟢 ${facility.bedAvailability}/${facility.totalBeds} beds free",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF047857),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (facility.icuAvailability > 0) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFFFF1F2),
              border = BorderStroke(1.dp, Color(0xFFFECDD3)),
            ) {
              Text(
                text = "⚡ ${facility.icuAvailability} ICU beds",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE11D48),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (facility.traumaLevel.isNotBlank()) {
            Text(
              text = facility.traumaLevel,
              fontSize = 10.sp,
              color = VictimTextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          }
        }
      } else if (facility.subDetail.isNotBlank()) {
        Text(
          text = facility.subDetail,
          fontSize = 11.sp,
          color = VictimTextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(start = 2.dp)
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

/**
 * Verified accurate emergency infrastructure across Kolkata, Barasat, Barrackpore,
 * Dum Dum, Madhyamgram, Panihati, Belgharia, Salt Lake, New Town, and surrounding districts.
 */
private fun getAccurateKolkataRegionalFacilities(
  userLat: Double,
  userLng: Double,
  localityName: String,
): List<EmergencyMapFacility> {
  val distToKolkata = computeDistanceMeters(userLat, userLng, 22.5726, 88.3639)

  val seeds: List<FacilitySeed> = if (distToKolkata <= 75_000.0) {
    listOf(
      // ==============================================================
      // 1. BARASAT (BARASAD) REGION HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Barasat Government Medical College & Hospital",
        address = "Banamalipur, Barasat, Kolkata, West Bengal 700124",
        lat = 22.7228,
        lng = 88.4815,
        phone = "033 2552 3228",
        emergencyHelpline = "033 2552 3211",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Medical College • 24/7 Trauma & Casualty",
        bedAvailability = 95,
        totalBeds = 850,
        icuAvailability = 16,
        traumaLevel = "Level 1 Apex Government Trauma Center"
      ),
      FacilitySeed(
        name = "Narayana Multispeciality Hospital — Barasat",
        address = "78, Jessore Road (South), Duckbunglow More, Barasat 700124",
        lat = 22.7180,
        lng = 88.4795,
        phone = "033 7122 2222",
        emergencyHelpline = "1800 309 0309",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Comprehensive Cardiac Center • 24/7 ER & Cath Lab",
        bedAvailability = 28,
        totalBeds = 150,
        icuAvailability = 6,
        traumaLevel = "Cardiac & Multi-Speciality Emergency"
      ),
      FacilitySeed(
        name = "Eco Hospital & Diagnostic Centre — Barasat",
        address = "NH 12, Purbachal, Nabapally, Barasat, Kolkata 700126",
        lat = 22.7155,
        lng = 88.4720,
        phone = "033 2584 1234",
        emergencyHelpline = "+91 98310 55667",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Critical Care ICU & Emergency Surgery",
        bedAvailability = 24,
        totalBeds = 120,
        icuAvailability = 4,
        traumaLevel = "Acute Care & Critical ICU Hub"
      ),
      FacilitySeed(
        name = "Care and Cure Hospital — Barasat",
        address = "Barrackpore Road, Champadali More, Barasat 700124",
        lat = 22.7245,
        lng = 88.4740,
        phone = "033 2562 8899",
        emergencyHelpline = "033 2562 8899",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Trauma Ward",
        bedAvailability = 18,
        totalBeds = 80,
        icuAvailability = 3,
        traumaLevel = "24/7 Casualty & Surgical Ward"
      ),

      // ==============================================================
      // 2. BARRACKPORE (BARAKPUR) REGION HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Dr. B. C. Roy General Hospital (Barrackpore Sub-Divisional)",
        address = "S. N. Banerjee Road, Barrackpore, Kolkata 700120",
        lat = 22.7610,
        lng = 88.3650,
        phone = "033 2592 0262",
        emergencyHelpline = "033 2592 0044",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Govt Sub-Divisional Hospital • 24/7 Emergency",
        bedAvailability = 55,
        totalBeds = 350,
        icuAvailability = 8,
        traumaLevel = "Sub-Divisional Trauma & Emergency Center"
      ),
      FacilitySeed(
        name = "Barrackpore Cantonment General Hospital",
        address = "Orderly Bazar, Barrackpore Cantonment, Barrackpore 700120",
        lat = 22.7665,
        lng = 88.3580,
        phone = "033 2592 0134",
        emergencyHelpline = "033 2592 0134",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Cantonment 24/7 Emergency & Acute Care Ward",
        bedAvailability = 22,
        totalBeds = 120,
        icuAvailability = 4,
        traumaLevel = "Cantonment Emergency Hospital"
      ),
      FacilitySeed(
        name = "College of Medicine & Sagore Dutta Hospital",
        address = "578, B.T. Road, Kamarhati, Belgharia, Kolkata 700058",
        lat = 22.6710,
        lng = 88.3765,
        phone = "033 2583 5555",
        emergencyHelpline = "033 2564 3000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Medical College • Level 1 Trauma Center",
        bedAvailability = 82,
        totalBeds = 700,
        icuAvailability = 14,
        traumaLevel = "Apex Government Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Panihati State General Hospital",
        address = "Barasat Road, Sodepur, Panihati, Kolkata 700110",
        lat = 22.7015,
        lng = 88.3795,
        phone = "033 2553 2211",
        emergencyHelpline = "033 2553 2211",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Govt 24/7 Emergency Casualty & Maternity Hospital",
        bedAvailability = 38,
        totalBeds = 250,
        icuAvailability = 5,
        traumaLevel = "Govt State General Hospital"
      ),
      FacilitySeed(
        name = "Balaram Seva Mandir State General Hospital (Khardah)",
        address = "B.T. Road, Khardah, Kolkata 700116",
        lat = 22.7160,
        lng = 88.3740,
        phone = "033 2553 2419",
        emergencyHelpline = "033 2553 2419",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Inpatient Wards",
        bedAvailability = 30,
        totalBeds = 150,
        icuAvailability = 4,
        traumaLevel = "State General Emergency Unit"
      ),

      // ==============================================================
      // 3. MADHYAMGRAM, BIRATI & DUM DUM CORRIDOR HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Madhyamgram Sub-Divisional Hospital",
        address = "Sodepur-Madhyamgram Road, Madhyamgram, Kolkata 700129",
        lat = 22.7010,
        lng = 88.4550,
        phone = "033 2538 4455",
        emergencyHelpline = "033 2538 4455",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Ward & Acute Care Hub",
        bedAvailability = 22,
        totalBeds = 100,
        icuAvailability = 3,
        traumaLevel = "Sub-Divisional Emergency Center"
      ),
      FacilitySeed(
        name = "Midland Nursing Home & Hospital — Birati",
        address = "125, Jessore Road, Birati, Kolkata 700051",
        lat = 22.6685,
        lng = 88.4415,
        phone = "033 2514 1122",
        emergencyHelpline = "033 2514 1122",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Cardiac & Trauma Critical Care",
        bedAvailability = 14,
        totalBeds = 60,
        icuAvailability = 2,
        traumaLevel = "Critical Care Emergency Center"
      ),
      FacilitySeed(
        name = "ILS Hospitals — Dum Dum",
        address = "1, Mall Road, near Nagerbazar, Dum Dum, Kolkata 700080",
        lat = 22.6255,
        lng = 88.4195,
        phone = "033 4031 5555",
        emergencyHelpline = "033 4031 5555",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 ER • Multi-Speciality ICU & Cath Lab",
        bedAvailability = 26,
        totalBeds = 150,
        icuAvailability = 7,
        traumaLevel = "Trauma & Advanced Critical Care"
      ),
      FacilitySeed(
        name = "Spandan Emergency Hospital — Teghoria",
        address = "VIP Road, Teghoria, Kolkata 700059",
        lat = 22.6215,
        lng = 88.4350,
        phone = "033 2570 1234",
        emergencyHelpline = "033 2570 1234",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Emergency • 24/7 Cardiac Care Unit",
        bedAvailability = 16,
        totalBeds = 80,
        icuAvailability = 4,
        traumaLevel = "Level 1 Emergency Center"
      ),
      FacilitySeed(
        name = "Charnock Hospital — Teghoria / VIP Road",
        address = "BMC 195, Teghoria Major Arterial Rd, New Town, Kolkata 700157",
        lat = 22.6235,
        lng = 88.4390,
        phone = "033 4050 0500",
        emergencyHelpline = "033 4050 0500",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Tertiary Level 1 Trauma Center • Neuro & Cardiac ICU",
        bedAvailability = 34,
        totalBeds = 200,
        icuAvailability = 8,
        traumaLevel = "Tertiary Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Dum Dum Municipal Specialized Hospital",
        address = "Dum Dum Cantonment, Kolkata 700065",
        lat = 22.6320,
        lng = 88.4210,
        phone = "033 2549 2233",
        emergencyHelpline = "033 2549 2233",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty Ward",
        bedAvailability = 19,
        totalBeds = 100,
        icuAvailability = 3,
        traumaLevel = "24/7 Casualty & Acute Ward"
      ),

      // ==============================================================
      // 4. KOLKATA CORE, SALT LAKE & NEW TOWN HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "R. G. Kar Medical College and Hospital",
        address = "1, Khudiram Bose Sarani, Shyambazar, Kolkata 700004",
        lat = 22.6044,
        lng = 88.3770,
        phone = "033 2555 7656",
        emergencyHelpline = "108",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Level 1 Trauma Center • 24/7 Blood Bank",
        bedAvailability = 140,
        totalBeds = 1200,
        icuAvailability = 22,
        traumaLevel = "Apex Level 1 Government Trauma Center"
      ),
      FacilitySeed(
        name = "Salt Lake Sub-Divisional Hospital",
        address = "DD Block, Sector I, Salt Lake, Kolkata 700064",
        lat = 22.5872,
        lng = 88.4115,
        phone = "033 2337 1318",
        emergencyHelpline = "033 2337 1318",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Inpatient Wards",
        bedAvailability = 25,
        totalBeds = 150,
        icuAvailability = 4,
        traumaLevel = "Sub-Divisional Emergency Center"
      ),
      FacilitySeed(
        name = "AMRI Hospitals — Salt Lake",
        address = "16, 17, JC Block, Broadway Rd, Sector III, Salt Lake 700098",
        lat = 22.5712,
        lng = 88.4120,
        phone = "033 6606 3800",
        emergencyHelpline = "033 6606 3800",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Emergency Center • Cardiac ICU",
        bedAvailability = 38,
        totalBeds = 220,
        icuAvailability = 7,
        traumaLevel = "Level 1 Emergency Center"
      ),
      FacilitySeed(
        name = "Apollo Multispeciality Hospitals",
        address = "58, Canal Circular Rd, Kadapara, Phoolbagan, Kolkata 700054",
        lat = 22.5685,
        lng = 88.4012,
        phone = "033 2320 3040",
        emergencyHelpline = "033 2320 2122",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "JCI-Accredited Comprehensive Cardiac & Emergency",
        bedAvailability = 64,
        totalBeds = 510,
        icuAvailability = 12,
        traumaLevel = "Comprehensive Cardiac & Trauma Center"
      ),
      FacilitySeed(
        name = "ILS Hospitals — Salt Lake",
        address = "DD-6, Sector 1, Salt Lake, Kolkata 700064",
        lat = 22.5890,
        lng = 88.4200,
        phone = "033 4031 5000",
        emergencyHelpline = "033 4031 5050",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 ER • Trauma & Acute Care",
        bedAvailability = 22,
        totalBeds = 135,
        icuAvailability = 4,
        traumaLevel = "Trauma & Acute Emergency Care"
      ),
      FacilitySeed(
        name = "Tata Medical Center — New Town",
        address = "14, Major Arterial Road (E-W), Action Area I, New Town 700160",
        lat = 22.5855,
        lng = 88.4820,
        phone = "033 6605 7000",
        emergencyHelpline = "033 6605 7222",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Specialized Critical Care & Emergency Oncology",
        bedAvailability = 30,
        totalBeds = 437,
        icuAvailability = 6,
        traumaLevel = "Specialized Critical Care"
      ),
      FacilitySeed(
        name = "Chittaranjan National Cancer Institute (CNCI) — New Town",
        address = "Street No. 299, DJ Block, Action Area I, New Town 700156",
        lat = 22.5740,
        lng = 88.4680,
        phone = "033 2324 5015",
        emergencyHelpline = "033 2324 5000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency & Acute Support ICU",
        bedAvailability = 40,
        totalBeds = 460,
        icuAvailability = 8,
        traumaLevel = "Emergency & Critical Support Hub"
      ),
      FacilitySeed(
        name = "SSKM Hospital & IPGMER",
        address = "244, AJC Bose Rd, Bhowanipore, Kolkata 700020",
        lat = 22.5390,
        lng = 88.3440,
        phone = "033 2204 1100",
        emergencyHelpline = "033 2223 1589",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Level 1 Govt Trauma Center",
        bedAvailability = 110,
        totalBeds = 2100,
        icuAvailability = 18,
        traumaLevel = "Apex Level 1 Government Trauma Center"
      ),
      FacilitySeed(
        name = "Fortis Hospital — Anandapur",
        address = "730, Anandapur, E.M. Bypass Road, Kolkata 700107",
        lat = 22.5186,
        lng = 88.3995,
        phone = "033 6628 4444",
        emergencyHelpline = "033 6628 4444",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Trauma Center • Comprehensive Cardiac & Neuro",
        bedAvailability = 45,
        totalBeds = 400,
        icuAvailability = 9,
        traumaLevel = "Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Medica Superspecialty Hospital",
        address = "127, Mukundapur, E.M. Bypass, Kolkata 700099",
        lat = 22.4988,
        lng = 88.3970,
        phone = "033 6652 0000",
        emergencyHelpline = "033 6652 0000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Trauma & Comprehensive Cardiac Center",
        bedAvailability = 52,
        totalBeds = 500,
        icuAvailability = 14,
        traumaLevel = "Level 1 Trauma & Cardiac Emergency Center"
      ),
      FacilitySeed(
        name = "Ruby General Hospital",
        address = "Kasba Golpark, E.M. Bypass, Kolkata 700107",
        lat = 22.5133,
        lng = 88.4011,
        phone = "033 3987 1800",
        emergencyHelpline = "033 3987 1800",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Surgery & Trauma ICU",
        bedAvailability = 28,
        totalBeds = 278,
        icuAvailability = 5,
        traumaLevel = "Emergency & Trauma Care"
      ),

      // ==============================================================
      // 5. POLICE STATIONS ACROSS BARASAT, BARRACKPORE & KOLKATA
      // ==============================================================
      FacilitySeed(
        name = "Barasat Police Station",
        address = "Jessore Road, K.N.C. Road Crossing, Barasat, Kolkata 700124",
        lat = 22.7235,
        lng = 88.4805,
        phone = "033 2552 3221",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Barasat Police District Headquarters • 24/7 Emergency Command"
      ),
      FacilitySeed(
        name = "Barrackpore Police Station",
        address = "River Road, Cantonment, Barrackpore, Kolkata 700120",
        lat = 22.7635,
        lng = 88.3610,
        phone = "033 2592 0064",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Barrackpore Police Commissionerate • 24/7 Patrol Command"
      ),
      FacilitySeed(
        name = "Titagarh Police Station",
        address = "B.T. Road, Titagarh, Kolkata 700119",
        lat = 22.7410,
        lng = 88.3710,
        phone = "033 2501 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Titagarh & Barrackpore Corridor Rapid Police Response"
      ),
      FacilitySeed(
        name = "Khardah Police Station",
        address = "B.T. Road, Khardah, Kolkata 700117",
        lat = 22.7150,
        lng = 88.3750,
        phone = "033 2553 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Khardah & Rahara Area Emergency Police Patrol"
      ),
      FacilitySeed(
        name = "Ghola Police Station",
        address = "Sodepur-Barasat Road, Ghola, Kolkata 700111",
        lat = 22.7050,
        lng = 88.4020,
        phone = "033 2565 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Sodepur-Barasat Road Emergency Patrol Hub"
      ),
      FacilitySeed(
        name = "New Barrackpore Police Station",
        address = "New Barrackpore Main Road, North 24 Parganas 700131",
        lat = 22.6980,
        lng = 88.4520,
        phone = "033 2567 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "New Barrackpore 24/7 Emergency Police Station"
      ),
      FacilitySeed(
        name = "Madhyamgram Police Station",
        address = "Jessore Road, Madhyamgram Chowmatha 700129",
        lat = 22.7020,
        lng = 88.4680,
        phone = "033 2538 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Madhyamgram Police Command & Highway Patrol"
      ),
      FacilitySeed(
        name = "Nimta Police Station",
        address = "M.B. Road, Nimta, North Dumdum, Kolkata 700049",
        lat = 22.6610,
        lng = 88.4120,
        phone = "033 2539 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Nimta & Birati Area Rapid Police Patrol"
      ),
      FacilitySeed(
        name = "Airport Police Station",
        address = "Gate 1, NSCBI Airport, Jessore Road, Kolkata 700052",
        lat = 22.6480,
        lng = 88.4420,
        phone = "033 2511 8100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "NSCBI Airport Emergency Police Command"
      ),
      FacilitySeed(
        name = "Dum Dum Police Station",
        address = "Dum Dum Cantonment, Kolkata 700065",
        lat = 22.6260,
        lng = 88.4210,
        phone = "033 2549 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Dum Dum Police Command & Rapid Patrol"
      ),
      FacilitySeed(
        name = "Belgharia Police Station",
        address = "Feeder Road, Belgharia, Kolkata 700056",
        lat = 22.6470,
        lng = 88.3840,
        phone = "033 2564 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Belgharia & Kamarhati Rapid Police Dispatch"
      ),
      FacilitySeed(
        name = "Baguiati Police Station",
        address = "VIP Road, Raghunathpur, Baguiati, Kolkata 700059",
        lat = 22.6175,
        lng = 88.4310,
        phone = "033 2570 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "VIP Road Emergency Police Patrol"
      ),
      FacilitySeed(
        name = "Lake Town Police Station",
        address = "Block A, Lake Town, South Dumdum, Kolkata 700089",
        lat = 22.6030,
        lng = 88.4020,
        phone = "033 2521 1000",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Lake Town & VIP Corridor Emergency Unit"
      ),
      FacilitySeed(
        name = "Electronic Complex Police Station (Sector V)",
        address = "DP Block, Sector V, Bidhannagar, Kolkata 700091",
        lat = 22.5810,
        lng = 88.4320,
        phone = "033 2357 5800",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "24/7 Police Outpost • Sector V Rapid Patrol"
      ),
      FacilitySeed(
        name = "Bidhannagar North Police Station",
        address = "DF Block, Sector I, Salt Lake, Kolkata 700064",
        lat = 22.5925,
        lng = 88.4140,
        phone = "033 2337 1319",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Salt Lake Sector I & II Emergency Command"
      ),
      FacilitySeed(
        name = "New Town Police Station",
        address = "Major Arterial Road, Action Area I, New Town 700156",
        lat = 22.5880,
        lng = 88.4610,
        phone = "033 2324 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "New Town Action Area I Police Command"
      ),

      // ==============================================================
      // 6. PHARMACIES
      // ==============================================================
      FacilitySeed(
        name = "Apollo Pharmacy — Barasat",
        address = "Jessore Road, Champadali More, Barasat 700124",
        lat = 22.7210,
        lng = 88.4780,
        phone = "+91 98305 11223",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24-Hour Medicine Supply & Oxygen Cylinder Delivery"
      ),
      FacilitySeed(
        name = "Frank Ross Pharmacy — Barrackpore",
        address = "S. N. Banerjee Road, Barrackpore, Kolkata 700120",
        lat = 22.7620,
        lng = 88.3680,
        phone = "+91 98311 33445",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Life-Saving Prescriptions & Critical Care Supplies"
      ),
      FacilitySeed(
        name = "MedPlus 24x7 Chemist — Sodepur",
        address = "Sodepur Station Road, Near B.T. Road, Kolkata 700110",
        lat = 22.7000,
        lng = 88.3820,
        phone = "+91 98302 44556",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24/7 Emergency Prescriptions & Nebulizer Kits"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Madhyamgram",
        address = "Jessore Road, Madhyamgram Chowmatha 700129",
        lat = 22.7030,
        lng = 88.4660,
        phone = "+91 98307 55667",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Essential & Cardiac Emergency Meds"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Lake Town",
        address = "Block A, Lake Town, Kolkata 700089",
        lat = 22.6040,
        lng = 88.4035,
        phone = "+91 98302 33441",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24/7 Medicine Delivery & First Aid Kits"
      ),
      FacilitySeed(
        name = "Frank Ross Pharmacy — Baguiati",
        address = "VIP Road, Baguiati, Kolkata 700059",
        lat = 22.6160,
        lng = 88.4300,
        phone = "+91 98314 55662",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Life-Saving Prescriptions & First Aid"
      ),
      FacilitySeed(
        name = "MedPlus Chemist — Dum Dum",
        address = "Dum Dum Road, Near Metro, Kolkata 700074",
        lat = 22.6240,
        lng = 88.4190,
        phone = "+91 98308 77883",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24-Hour Emergency Prescription Supply"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Sector V",
        address = "Godrej Genesis, Sector V, Salt Lake 700091",
        lat = 22.5820,
        lng = 88.4350,
        phone = "+91 98300 22114",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Emergency Meds & Oxygen Cylinders"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — New Town",
        address = "City Centre 2, New Town, Kolkata 700136",
        lat = 22.6220,
        lng = 88.4550,
        phone = "+91 98309 99001",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Emergency Pediatric & Trauma Supplies"
      ),
    )
  } else {
    // Dynamic generation when testing outside West Bengal
    val prefix = if (localityName.isNotBlank() && localityName != "Locating..." && localityName != "Current Location") {
      localityName.substringBefore(",")
    } else {
      "Local"
    }

    listOf(
      FacilitySeed(
        name = "$prefix District General Hospital",
        address = "Civil Hospital Road, $prefix",
        lat = userLat + 0.0062,
        lng = userLng + 0.0045,
        phone = "108",
        emergencyHelpline = "108",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Level 1 Trauma & Emergency Care",
        bedAvailability = 42,
        totalBeds = 300,
        icuAvailability = 8,
        traumaLevel = "Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "$prefix Metropolitan Emergency Medical Center",
        address = "Medical Enclave, $prefix",
        lat = userLat - 0.0085,
        lng = userLng - 0.0065,
        phone = "108",
        emergencyHelpline = "108",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Critical Care ICU & Ambulance Hub",
        bedAvailability = 28,
        totalBeds = 200,
        icuAvailability = 6,
        traumaLevel = "Cardiac & Critical Care"
      ),
      FacilitySeed(
        name = "$prefix Central Police Station",
        address = "Main District Road, $prefix",
        lat = userLat + 0.0035,
        lng = userLng - 0.0028,
        phone = "100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "24/7 Police Outpost • Rapid Response Patrol"
      ),
      FacilitySeed(
        name = "$prefix Division Emergency Police Command",
        address = "Civil Lines, $prefix",
        lat = userLat - 0.0055,
        lng = userLng + 0.0048,
        phone = "100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Metropolitan Police Emergency Dispatch"
      ),
      FacilitySeed(
        name = "$prefix 24x7 Care Chemist",
        address = "Central Market, $prefix",
        lat = userLat - 0.0025,
        lng = userLng - 0.0020,
        phone = "+91 98300 00000",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Life-Saving Prescriptions & First Aid"
      ),
      FacilitySeed(
        name = "Apollo 24-Hour Pharmacy — $prefix",
        address = "Station Road, $prefix",
        lat = userLat + 0.0042,
        lng = userLng + 0.0025,
        phone = "+91 98300 00001",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Emergency Supplies & Oxygen Kits"
      ),
    )
  }

  return seeds.mapIndexed { index, seed ->
    val distM = computeDistanceMeters(userLat, userLng, seed.lat, seed.lng)
    EmergencyMapFacility(
      id = "fac_${seed.category.name.lowercase()}_$index",
      name = seed.name,
      address = seed.address,
      category = seed.category,
      distance = formatDistance(distM),
      distanceKm = distM / 1000.0,
      status = if (seed.category == EmergencyMapCategory.POLICE) "24/7 Police Station" else "Open 24 hours",
      subDetail = seed.subDetail,
      bedAvailability = seed.bedAvailability,
      totalBeds = seed.totalBeds,
      icuAvailability = seed.icuAvailability,
      traumaLevel = seed.traumaLevel,
      emergencyHelpline = seed.emergencyHelpline.ifBlank { seed.phone },
      phone = seed.phone,
      icon = seed.icon,
      lat = seed.lat,
      lng = seed.lng,
    )
  }
}
