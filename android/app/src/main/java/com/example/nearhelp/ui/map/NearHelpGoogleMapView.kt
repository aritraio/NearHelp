package com.example.nearhelp.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Standard Coordinates for Kolkata Central District (Emergency Command Hub).
 */
val DEFAULT_KOLKATA_COORDINATES = LatLng(22.5726, 88.3639)

/**
 * Data model for a map marker representing an emergency facility or volunteer responder.
 */
data class MapPinItem(
  val id: String,
  val title: String,
  val snippet: String,
  val position: LatLng,
  val hueColor: Float = BitmapDescriptorFactory.HUE_RED,
  val isEmergencyIncident: Boolean = false,
)

/**
 * Production-ready Google Maps Composable for NearHelp.
 *
 * Integrates:
 * 1. Camera positioning and smooth pan/zoom tracking
 * 2. Real-time emergency facility markers (Hospitals, Blood banks, AEDs)
 * 3. Dynamic radial dispatch escalation circle (Layer 1/2/3 SOS radius)
 * 4. Optional polyline rescue routing
 */
@Composable
fun NearHelpGoogleMapView(
  modifier: Modifier = Modifier,
  centerPosition: LatLng = DEFAULT_KOLKATA_COORDINATES,
  zoomLevel: Float = 14.5f,
  pins: List<MapPinItem> = emptyList(),
  dispatchRadiusMeters: Double = 1500.0,
  routePoints: List<LatLng> = emptyList(),
  onPinClick: ((MapPinItem) -> Unit)? = null,
) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(centerPosition, zoomLevel)
  }

  var uiSettings by remember {
    mutableStateOf(
      MapUiSettings(
        zoomControlsEnabled = false,
        myLocationButtonEnabled = true,
        compassEnabled = true,
        mapToolbarEnabled = false,
        rotationGesturesEnabled = true,
        scrollGesturesEnabled = true,
        tiltGesturesEnabled = true,
        zoomGesturesEnabled = true,
      )
    )
  }

  var mapProperties by remember {
    mutableStateOf(
      MapProperties(
        isMyLocationEnabled = false, // Set to true once runtime location permissions are granted
        mapType = MapType.NORMAL,
      )
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      properties = mapProperties,
      uiSettings = uiSettings,
    ) {
      // 1. Dynamic SOS Radial Dispatch Area (Red Translucent Circle)
      if (dispatchRadiusMeters > 0.0) {
        Circle(
          center = centerPosition,
          radius = dispatchRadiusMeters,
          fillColor = Color(0x22DC2626), // Translucent red
          strokeColor = Color(0xFFDC2626), // Solid crimson outline
          strokeWidth = 3f,
        )
      }

      // 2. Incident Center Marker
      Marker(
        state = MarkerState(position = centerPosition),
        title = "Emergency Incident Center",
        snippet = "Radial search radius: ${dispatchRadiusMeters.toInt()}m",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
      )

      // 3. Render all facility / responder pins
      pins.forEach { pin ->
        Marker(
          state = MarkerState(position = pin.position),
          title = pin.title,
          snippet = pin.snippet,
          icon = BitmapDescriptorFactory.defaultMarker(pin.hueColor),
          onClick = {
            onPinClick?.invoke(pin)
            false
          },
        )
      }

      // 4. Render Active Rescue Route Polyline if present
      if (routePoints.isNotEmpty()) {
        Polyline(
          points = routePoints,
          color = Color(0xFF2563EB), // Rescue Blue
          width = 10f,
          geodesic = true,
        )
      }
    }
  }
}
