package com.example.nearhelp.data.api.models

import com.google.gson.annotations.SerializedName

data class RouteLocationPoint(
  @SerializedName("lat") val lat: Double = 0.0,
  @SerializedName("lng") val lng: Double = 0.0
)

data class RouteStepDto(
  @SerializedName("step_index") val stepIndex: Int = 0,
  @SerializedName("maneuver") val maneuver: String = "STRAIGHT",
  @SerializedName("instruction") val instruction: String = "",
  @SerializedName("street_name") val streetName: String = "",
  @SerializedName("distance_meters") val distanceMeters: Int = 0,
  @SerializedName("distance_formatted") val distanceFormatted: String = "",
  @SerializedName("duration_seconds") val durationSeconds: Int = 0,
  @SerializedName("duration_formatted") val durationFormatted: String = "",
  @SerializedName("landmark") val landmark: String = "",
  @SerializedName("traffic_level") val trafficLevel: String = "LOW",
  @SerializedName("start_location") val startLocation: RouteLocationPoint = RouteLocationPoint(),
  @SerializedName("end_location") val endLocation: RouteLocationPoint = RouteLocationPoint()
)

data class RoadHazardDto(
  @SerializedName("hazard_id") val hazardId: String = "",
  @SerializedName("title") val title: String = "",
  @SerializedName("hazard_type") val hazardType: String = "TRAFFIC_JAM",
  @SerializedName("severity") val severity: String = "MODERATE",
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
  @SerializedName("route_type") val routeType: String = "PRIMARY",
  @SerializedName("distance_meters") val distanceMeters: Int = 0,
  @SerializedName("distance_formatted") val distanceFormatted: String = "",
  @SerializedName("duration_seconds") val durationSeconds: Int = 0,
  @SerializedName("duration_formatted") val durationFormatted: String = "",
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
  @SerializedName("badge") val badge: String = "",
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
  @SerializedName("origin_latitude") val originLatitude: Double,
  @SerializedName("origin_longitude") val originLongitude: Double,
  @SerializedName("destination_latitude") val destinationLatitude: Double,
  @SerializedName("destination_longitude") val destinationLongitude: Double,
  @SerializedName("travel_mode") val travelMode: String = "walking",
  @SerializedName("avoid_hazards") val avoidHazards: Boolean = true,
  @SerializedName("include_aed_detour") val includeAedDetour: Boolean = false,
  @SerializedName("aed_latitude") val aedLatitude: Double? = null,
  @SerializedName("aed_longitude") val aedLongitude: Double? = null,
  @SerializedName("aed_name") val aedName: String? = null
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
