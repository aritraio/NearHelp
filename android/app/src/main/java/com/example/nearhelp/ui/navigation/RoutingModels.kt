package com.example.nearhelp.ui.navigation

import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.google.gson.annotations.SerializedName

data class RouteLocationPoint(
  @SerializedName("lat") val lat: Double = 0.0,
  @SerializedName("lng") val lng: Double = 0.0
)

data class RouteStepDto(
  @SerializedName("step_index") val stepIndex: Int = 0,
  @SerializedName("maneuver") val maneuver: String = "STRAIGHT", // DEPART, TURN_LEFT, TURN_RIGHT, STRAIGHT, UTURN, ARRIVE
  @SerializedName("instruction") val instruction: String = "",
  @SerializedName("street_name") val streetName: String = "",
  @SerializedName("distance_meters") val distanceMeters: Int = 0,
  @SerializedName("distance_formatted") val distanceFormatted: String = "",
  @SerializedName("duration_seconds") val durationSeconds: Int = 0,
  @SerializedName("duration_formatted") val durationFormatted: String = "",
  @SerializedName("landmark") val landmark: String = "",
  @SerializedName("traffic_level") val trafficLevel: String = "LOW", // LOW, MODERATE, HEAVY, BLOCKED
  @SerializedName("start_location") val startLocation: RouteLocationPoint = RouteLocationPoint(),
  @SerializedName("end_location") val endLocation: RouteLocationPoint = RouteLocationPoint()
)

data class RoadHazardDto(
  @SerializedName("hazard_id") val hazardId: String = "",
  @SerializedName("title") val title: String = "",
  @SerializedName("hazard_type") val hazardType: String = "TRAFFIC_JAM", // FLOODING, CONSTRUCTION, TRAFFIC_JAM, ROAD_CLOSURE
  @SerializedName("severity") val severity: String = "MODERATE", // LOW, MODERATE, CRITICAL, BLOCKED
  @SerializedName("latitude") val latitude: Double = 0.0,
  @SerializedName("longitude") val longitude: Double = 0.0,
  @SerializedName("radius_meters") val radiusMeters: Double = 100.0,
  @SerializedName("description") val description: String = "",
  @SerializedName("delay_seconds") val delaySeconds: Int = 180,
  @SerializedName("is_passable_for_emergency") val isPassableForEmergency: Boolean = false
)

data class AedWaypointDto(
  @SerializedName("name") val name: String = "",
  @SerializedName("lat") val lat: Double = 0.0,
  @SerializedName("lng") val lng: Double = 0.0,
  @SerializedName("detour_delta_seconds") val detourDeltaSeconds: Int = 30
)

data class RescueRouteDto(
  @SerializedName("route_id") val routeId: String = "route_primary",
  @SerializedName("route_name") val routeName: String = "Main Arterial Route",
  @SerializedName("route_type") val routeType: String = "PRIMARY", // PRIMARY, DETOUR, AED_PICKUP
  @SerializedName("distance_meters") val distanceMeters: Int = 340,
  @SerializedName("distance_formatted") val distanceFormatted: String = "340m",
  @SerializedName("duration_seconds") val durationSeconds: Int = 150,
  @SerializedName("duration_formatted") val durationFormatted: String = "2.5 mins",
  @SerializedName("traffic_level") val trafficLevel: String = "MODERATE",
  @SerializedName("traffic_delay_seconds") val trafficDelaySeconds: Int = 0,
  @SerializedName("polyline_points") val polylinePoints: List<RouteLocationPoint> = emptyList(),
  @SerializedName("steps") val steps: List<RouteStepDto> = emptyList(),
  @SerializedName("has_hazard_conflict") val hasHazardConflict: Boolean = false,
  @SerializedName("detected_hazards") val detectedHazards: List<RoadHazardDto> = emptyList(),
  @SerializedName("bypassed_hazards") val bypassedHazards: List<RoadHazardDto> = emptyList(),
  @SerializedName("aed_waypoint") val aedWaypoint: AedWaypointDto? = null,
  @SerializedName("time_saved_seconds") val timeSavedSeconds: Int = 0
)

data class RoutingRecommendationDto(
  @SerializedName("suggested_route_id") val suggestedRouteId: String = "",
  @SerializedName("badge") val badge: String = "⚡ AI FASTEST ROUTE",
  @SerializedName("summary") val summary: String = "",
  @SerializedName("reasons") val reasons: List<String> = emptyList()
)

data class DirectionsResponseData(
  @SerializedName("origin") val origin: RouteLocationPoint = RouteLocationPoint(),
  @SerializedName("destination") val destination: RouteLocationPoint = RouteLocationPoint(),
  @SerializedName("travel_mode") val travelMode: String = "walking",
  @SerializedName("primary_route") val primaryRoute: RescueRouteDto = RescueRouteDto(),
  @SerializedName("detour_route") val detourRoute: RescueRouteDto? = null,
  @SerializedName("aed_pickup_route") val aedPickupRoute: RescueRouteDto? = null,
  @SerializedName("active_hazards_count") val activeHazardsCount: Int = 0,
  @SerializedName("recommendation") val recommendation: RoutingRecommendationDto? = null
)

data class DirectionsResponseDto(
  @SerializedName("status") val status: String = "success",
  @SerializedName("data") val data: DirectionsResponseData = DirectionsResponseData()
)

data class DirectionsRequestDto(
  @SerializedName("origin_latitude") val originLatitude: Double = 22.5835,
  @SerializedName("origin_longitude") val originLongitude: Double = 88.4410,
  @SerializedName("destination_latitude") val destinationLatitude: Double = 22.5804,
  @SerializedName("destination_longitude") val destinationLongitude: Double = 88.4378,
  @SerializedName("travel_mode") val travelMode: String = "walking",
  @SerializedName("avoid_hazards") val avoidHazards: Boolean = true,
  @SerializedName("include_aed_detour") val includeAedDetour: Boolean = true,
  @SerializedName("aed_latitude") val aedLatitude: Double? = 22.5806,
  @SerializedName("aed_longitude") val aedLongitude: Double? = 88.4385,
  @SerializedName("aed_name") val aedName: String? = "Godrej Waterside Tower 1 Lobby AED"
)

data class RoadHazardReportDto(
  @SerializedName("title") val title: String,
  @SerializedName("hazard_type") val hazardType: String = "FLOODING",
  @SerializedName("severity") val severity: String = "MODERATE",
  @SerializedName("latitude") val latitude: Double,
  @SerializedName("longitude") val longitude: Double,
  @SerializedName("radius_meters") val radiusMeters: Double = 100.0,
  @SerializedName("description") val description: String = "",
  @SerializedName("delay_seconds") val delaySeconds: Int = 180,
  @SerializedName("is_passable_for_emergency") val isPassableForEmergency: Boolean = false
)

data class HazardsListResponseDto(
  @SerializedName("status") val status: String = "success",
  @SerializedName("count") val count: Int = 0,
  @SerializedName("hazards") val hazards: List<RoadHazardDto> = emptyList()
)

// UI State for Rescue Navigation Screen
data class RescueNavigationUiState(
  val incidentId: String = "KOL-SOS-8821",
  val responderName: String = "Dr. Anirban Roy",
  val victimName: String = "Tanushree Das",
  val victimAddress: String = "Godrej Waterside, Tower 1, Elevator Bank B, Sector V, Kolkata",
  val victimLat: Double = 22.5804,
  val victimLng: Double = 88.4378,
  val responderLat: Double = 22.5835,
  val responderLng: Double = 88.4410,
  val travelMode: String = "walking",
  val isTrafficAvoidanceEnabled: Boolean = true,
  val selectedRouteType: String = "PRIMARY", // PRIMARY, DETOUR, AED_PICKUP
  val primaryRoute: RescueRouteDto = DEFAULT_PRIMARY_RESCUE_ROUTE,
  val detourRoute: RescueRouteDto? = DEFAULT_DETOUR_RESCUE_ROUTE,
  val aedPickupRoute: RescueRouteDto? = DEFAULT_AED_RESCUE_ROUTE,
  val recommendation: RoutingRecommendationDto? = DEFAULT_RECOMMENDATION,
  val activeHazards: List<RoadHazardDto> = DEFAULT_HAZARDS,
  val currentTurnStepIndex: Int = 0,
  val liveSpeedKmh: Double = 4.8,
  val liveBearingCompass: String = "NE",
  val liveBearingDeg: Float = 34f,
  val liveGpsAccuracy: Float = 2.5f,
  val liveEtaFormatted: String = "2.5 mins",
  val liveDistanceFormatted: String = "340m",
  val isGpsSimulationActive: Boolean = false,
  val isMedicalIdRevealed: Boolean = true,
  val isAedAttached: Boolean = false,
  val isCprMetronomeActive: Boolean = false,
  val incidentStatus: String = "EN_ROUTE",
  val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
  val toastMessage: String? = null,
  val isLoadingRoute: Boolean = false
)

val DEFAULT_PRIMARY_RESCUE_ROUTE = RescueRouteDto(
  routeId = "route_primary_arterial",
  routeName = "Main Arterial (Ring Rd)",
  routeType = "PRIMARY",
  distanceMeters = 340,
  distanceFormatted = "340m",
  durationSeconds = 150,
  durationFormatted = "2.5 mins",
  trafficLevel = "MODERATE",
  trafficDelaySeconds = 210,
  hasHazardConflict = true,
  polylinePoints = listOf(
    RouteLocationPoint(22.5835, 88.4410),
    RouteLocationPoint(22.5822, 88.4398),
    RouteLocationPoint(22.5810, 88.4385),
    RouteLocationPoint(22.5804, 88.4378)
  ),
  steps = listOf(
    RouteStepDto(
      stepIndex = 0,
      maneuver = "DEPART",
      instruction = "Head North-East on Ring Rd toward Webel Bhavan",
      streetName = "Ring Rd",
      distanceMeters = 120,
      distanceFormatted = "120m",
      durationSeconds = 60,
      durationFormatted = "1.0 min",
      landmark = "Pass Sector V Metro Pillar #104",
      trafficLevel = "MODERATE",
      startLocation = RouteLocationPoint(22.5835, 88.4410),
      endLocation = RouteLocationPoint(22.5822, 88.4398)
    ),
    RouteStepDto(
      stepIndex = 1,
      maneuver = "TURN_RIGHT",
      instruction = "Turn right onto Godrej Waterside Access Road",
      streetName = "Godrej Waterside Access Rd",
      distanceMeters = 180,
      distanceFormatted = "180m",
      durationSeconds = 90,
      durationFormatted = "1.5 mins",
      landmark = "AED Station on right at Webel Security Gate #2",
      trafficLevel = "HEAVY",
      startLocation = RouteLocationPoint(22.5822, 88.4398),
      endLocation = RouteLocationPoint(22.5810, 88.4385)
    ),
    RouteStepDto(
      stepIndex = 2,
      maneuver = "STRAIGHT",
      instruction = "Proceed into Tower 1 Ground Concourse",
      streetName = "Tower 1 Access Concourse",
      distanceMeters = 80,
      distanceFormatted = "80m",
      durationSeconds = 40,
      durationFormatted = "0.7 mins",
      landmark = "Security desk checkpoint — Emergency bypass",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5810, 88.4385),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    ),
    RouteStepDto(
      stepIndex = 3,
      maneuver = "ARRIVE",
      instruction = "Arrive at Elevator Bank B — Victim on floor",
      streetName = "Elevator Bank B Lobby",
      distanceMeters = 0,
      distanceFormatted = "0m",
      durationSeconds = 0,
      durationFormatted = "Arrived",
      landmark = "Ground floor elevator lobby",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5804, 88.4378),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    )
  )
)

val DEFAULT_DETOUR_RESCUE_ROUTE = RescueRouteDto(
  routeId = "route_detour_service_lane",
  routeName = "⚡ AI Detour (EP Service Lane)",
  routeType = "DETOUR",
  distanceMeters = 380,
  distanceFormatted = "380m",
  durationSeconds = 110,
  durationFormatted = "1.8 mins",
  trafficLevel = "LOW",
  trafficDelaySeconds = 0,
  hasHazardConflict = false,
  timeSavedSeconds = 180,
  polylinePoints = listOf(
    RouteLocationPoint(22.5835, 88.4410),
    RouteLocationPoint(22.5828, 88.4418),
    RouteLocationPoint(22.5812, 88.4395),
    RouteLocationPoint(22.5804, 88.4378)
  ),
  steps = listOf(
    RouteStepDto(
      stepIndex = 0,
      maneuver = "TURN_LEFT",
      instruction = "Turn left onto EP Block Dedicated Service Lane",
      streetName = "EP Block Service Lane",
      distanceMeters = 150,
      distanceFormatted = "150m",
      durationSeconds = 45,
      durationFormatted = "0.7 mins",
      landmark = "Bypasses Ring Rd Traffic Jam & Waterlogged Underpass",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5835, 88.4410),
      endLocation = RouteLocationPoint(22.5828, 88.4418)
    ),
    RouteStepDto(
      stepIndex = 1,
      maneuver = "TURN_RIGHT",
      instruction = "Turn right into Godrej Waterside Rear Emergency Gate #3",
      streetName = "Waterside Rear Emergency Access",
      distanceMeters = 150,
      distanceFormatted = "150m",
      durationSeconds = 45,
      durationFormatted = "0.7 mins",
      landmark = "Security gate pre-cleared for BLS responders",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5828, 88.4418),
      endLocation = RouteLocationPoint(22.5812, 88.4395)
    ),
    RouteStepDto(
      stepIndex = 2,
      maneuver = "STRAIGHT",
      instruction = "Enter West Concourse directly to Elevator Bank B",
      streetName = "Tower 1 West Corridor",
      distanceMeters = 80,
      distanceFormatted = "80m",
      durationSeconds = 20,
      durationFormatted = "0.4 mins",
      landmark = "Direct covered passage to victim",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5812, 88.4395),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    ),
    RouteStepDto(
      stepIndex = 3,
      maneuver = "ARRIVE",
      instruction = "Arrive at Elevator Bank B Ground Concourse",
      streetName = "Elevator Bank B Lobby",
      distanceMeters = 0,
      distanceFormatted = "0m",
      durationSeconds = 0,
      durationFormatted = "Arrived",
      landmark = "Ground floor elevator lobby",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5804, 88.4378),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    )
  )
)

val DEFAULT_AED_RESCUE_ROUTE = RescueRouteDto(
  routeId = "route_aed_pickup",
  routeName = "🏥 AED Pickup Route (+30s grab)",
  routeType = "AED_PICKUP",
  distanceMeters = 410,
  distanceFormatted = "410m",
  durationSeconds = 170,
  durationFormatted = "2.8 mins",
  trafficLevel = "LOW",
  trafficDelaySeconds = 0,
  hasHazardConflict = false,
  aedWaypoint = AedWaypointDto(
    name = "Godrej Waterside Tower 1 Lobby AED",
    lat = 22.5806,
    lng = 88.4385,
    detourDeltaSeconds = 45
  ),
  polylinePoints = listOf(
    RouteLocationPoint(22.5835, 88.4410),
    RouteLocationPoint(22.5818, 88.4402),
    RouteLocationPoint(22.5806, 88.4385),
    RouteLocationPoint(22.5804, 88.4378)
  ),
  steps = listOf(
    RouteStepDto(
      stepIndex = 0,
      maneuver = "DEPART",
      instruction = "Head towards Webel Security Gate to collect AED Wall Cabinet unit",
      streetName = "Ring Rd Access",
      distanceMeters = 200,
      distanceFormatted = "200m",
      durationSeconds = 70,
      durationFormatted = "1.2 mins",
      landmark = "AED Wall Cabinet #2 (Automated Alarmed Door)",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5835, 88.4410),
      endLocation = RouteLocationPoint(22.5806, 88.4385)
    ),
    RouteStepDto(
      stepIndex = 1,
      maneuver = "TURN_RIGHT",
      instruction = "Grab AED & proceed 60m into Tower 1 Elevator Bank B",
      streetName = "Tower 1 Lobby Concourse",
      distanceMeters = 80,
      distanceFormatted = "80m",
      durationSeconds = 40,
      durationFormatted = "0.7 mins",
      landmark = "Ground floor elevator lobby",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5806, 88.4385),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    ),
    RouteStepDto(
      stepIndex = 2,
      maneuver = "ARRIVE",
      instruction = "Arrive on scene with AED — Apply pads immediately",
      streetName = "Elevator Bank B Lobby",
      distanceMeters = 0,
      distanceFormatted = "0m",
      durationSeconds = 0,
      durationFormatted = "Arrived",
      landmark = "Victim location with AED ready",
      trafficLevel = "LOW",
      startLocation = RouteLocationPoint(22.5804, 88.4378),
      endLocation = RouteLocationPoint(22.5804, 88.4378)
    )
  )
)

val DEFAULT_RECOMMENDATION = RoutingRecommendationDto(
  suggestedRouteId = "route_detour_service_lane",
  badge = "⚡ AI DETOUR RECOMMENDED",
  summary = "Detour via EP Service Lane saves 1.8 mins by bypassing Ring Rd traffic & waterlogged underpass.",
  reasons = listOf(
    "Traffic congestion penalty on primary route: +210s",
    "Identified 2 hazard(s) along main arterial.",
    "Rear emergency gate cleared and pre-authorized for NearHelp responders."
  )
)

val DEFAULT_HAZARDS = listOf(
  RoadHazardDto(
    hazardId = "haz_sec5_ring_traffic",
    title = "Heavy Congestion on Sector V Ring Rd",
    hazardType = "TRAFFIC_JAM",
    severity = "MODERATE",
    latitude = 22.5828,
    longitude = 88.4402,
    radiusMeters = 180.0,
    description = "Peak-hour IT tech park gridlock approaching SDF Building intersection.",
    delaySeconds = 210,
    isPassableForEmergency = true
  ),
  RoadHazardDto(
    hazardId = "haz_waterside_underpass_flood",
    title = "Monsoon Waterlogging at Concourse Underpass",
    hazardType = "FLOODING",
    severity = "CRITICAL",
    latitude = 22.5815,
    longitude = 88.4390,
    radiusMeters = 90.0,
    description = "1.2ft standing water; inaccessible for low clearance vehicles.",
    delaySeconds = 300,
    isPassableForEmergency = false
  )
)
