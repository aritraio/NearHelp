package com.example.nearhelp.data.api.models

import com.google.gson.annotations.SerializedName

data class ResponderTrackingUpdateDto(
  @SerializedName("responder_id") val responderId: String = "",
  @SerializedName("responder_name") val responderName: String = "",
  @SerializedName("latitude") val latitude: Double = Double.NaN,
  @SerializedName("longitude") val longitude: Double = Double.NaN,
  @SerializedName("heading") val heading: Float? = null,
  @SerializedName("bearing_compass") val bearingCompass: String? = null,
  @SerializedName("speed_kmh") val speedKmh: Double? = null,
  @SerializedName("distance_meters") val distanceMeters: Double = Double.NaN,
  @SerializedName("distance_formatted") val distanceFormatted: String = "",
  @SerializedName("eta_minutes") val etaMinutes: Double = Double.NaN,
  @SerializedName("eta_formatted") val etaFormatted: String = "",
  @SerializedName("status") val status: String = "",
  @SerializedName("is_doctor") val isDoctor: Boolean = false,
  @SerializedName("is_cpr_certified") val isCprCertified: Boolean = false,
  @SerializedName("verified_skills") val verifiedSkills: List<String> = emptyList(),
  @SerializedName("phone") val phone: String? = null,
  @SerializedName("last_updated") val lastUpdated: String? = null,
)

data class TrackingFacilityItemDto(
  @SerializedName("id") val id: String = "",
  @SerializedName("name") val name: String = "",
  @SerializedName("facility_type") val facilityType: String = "hospital",
  @SerializedName("latitude") val latitude: Double = 0.0,
  @SerializedName("longitude") val longitude: Double = 0.0,
  @SerializedName("distance_meters") val distanceMeters: Double = 0.0,
  @SerializedName("distance_formatted") val distanceFormatted: String = "",
  @SerializedName("details") val details: Map<String, Any?> = emptyMap()
)

data class TrackingSnapshotDto(
  @SerializedName("incident_id") val incidentId: String = "",
  @SerializedName("status") val status: String = "",
  @SerializedName("crisis_type") val crisisType: String = "medical",
  @SerializedName("sub_type") val subType: String? = null,
  @SerializedName("severity_score") val severityScore: Int = 0,
  @SerializedName("priority") val priority: String = "",
  @SerializedName("incident_latitude") val incidentLatitude: Double = Double.NaN,
  @SerializedName("incident_longitude") val incidentLongitude: Double = Double.NaN,
  @SerializedName("incident_address") val incidentAddress: String? = null,
  @SerializedName("incident_sub_address") val incidentSubAddress: String? = null,
  @SerializedName("is_anonymous") val isAnonymous: Boolean = false,
  @SerializedName("current_radius_meters") val currentRadiusMeters: Double = 1500.0,
  @SerializedName("responders") val responders: List<ResponderTrackingUpdateDto> = emptyList(),
  @SerializedName("closest_aed") val closestAed: TrackingFacilityItemDto? = null,
  @SerializedName("closest_hospital") val closestHospital: TrackingFacilityItemDto? = null,
  @SerializedName("connected_clients_count") val connectedClientsCount: Int = 1
)

data class TimelineTrackingEventDto(
  @SerializedName("id") val id: String = "",
  @SerializedName("sos_event_id") val sosEventId: String = "",
  @SerializedName("actor_id") val actorId: String? = null,
  @SerializedName("actor_name") val actorName: String? = null,
  @SerializedName("event_type") val eventType: String = "",
  @SerializedName("details") val details: Map<String, Any?> = emptyMap(),
  @SerializedName("timestamp") val timestamp: String? = null
)
