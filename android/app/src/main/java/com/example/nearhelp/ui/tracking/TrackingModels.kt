package com.example.nearhelp.ui.tracking

import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.google.gson.annotations.SerializedName

data class ResponderTrackingUpdateDto(
  @SerializedName("responder_id") val responderId: String = "",
  @SerializedName("responder_name") val responderName: String = "",
  @SerializedName("latitude") val latitude: Double = 22.5835,
  @SerializedName("longitude") val longitude: Double = 88.4410,
  @SerializedName("heading") val heading: Float? = 34f,
  @SerializedName("bearing_compass") val bearingCompass: String? = "NE",
  @SerializedName("speed_kmh") val speedKmh: Double? = 4.8,
  @SerializedName("distance_meters") val distanceMeters: Double = 340.0,
  @SerializedName("distance_formatted") val distanceFormatted: String = "340m",
  @SerializedName("eta_minutes") val etaMinutes: Double = 2.5,
  @SerializedName("eta_formatted") val etaFormatted: String = "2.5 mins",
  @SerializedName("status") val status: String = "EN_ROUTE",
  @SerializedName("is_doctor") val isDoctor: Boolean = true,
  @SerializedName("is_cpr_certified") val isCprCertified: Boolean = true,
  @SerializedName("verified_skills") val verifiedSkills: List<String> = listOf("DOCTOR", "CPR_CERTIFIED"),
  @SerializedName("phone") val phone: String? = "+91 98301 55421"
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
  @SerializedName("status") val status: String = "RESPONDER_ACCEPTED",
  @SerializedName("crisis_type") val crisisType: String = "medical",
  @SerializedName("sub_type") val subType: String? = "cardiac_arrest",
  @SerializedName("severity_score") val severityScore: Int = 90,
  @SerializedName("priority") val priority: String = "critical",
  @SerializedName("incident_latitude") val incidentLatitude: Double = 22.5804,
  @SerializedName("incident_longitude") val incidentLongitude: Double = 88.4378,
  @SerializedName("incident_address") val incidentAddress: String? = "Godrej Waterside, Tower 1, DP Block, Sector V, Kolkata",
  @SerializedName("incident_sub_address") val incidentSubAddress: String? = "Sector V, Salt Lake, Kolkata",
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

data class NavigationStepItem(
  val instruction: String,
  val distance: String,
  val turn: String, // "straight", "right", "left", "arrive"
  val eta: String,
  val landmark: String
)

val DEFAULT_NAVIGATION_STEPS = listOf(
  NavigationStepItem(
    instruction = "Head North-East on Ring Rd toward Webel Bhavan",
    distance = "120m",
    turn = "straight",
    eta = "2.5 mins",
    landmark = "Pass Sector V Metro Pillar #104"
  ),
  NavigationStepItem(
    instruction = "Turn right onto Godrej Waterside Access Road",
    distance = "180m",
    turn = "right",
    eta = "1.8 mins",
    landmark = "AED Station on right at Webel Security Gate"
  ),
  NavigationStepItem(
    instruction = "Proceed into Tower 1 Ground Concourse",
    distance = "80m",
    turn = "straight",
    eta = "0.8 mins",
    landmark = "Security desk checkpoint"
  ),
  NavigationStepItem(
    instruction = "Arrive at Elevator Bank B — Victim on floor",
    distance = "40m",
    turn = "arrive",
    eta = "Arrived",
    landmark = "Ground floor elevator lobby"
  )
)

data class LiveTrackingUiState(
  val incidentId: String = "KOL-SOS-8821",
  val incidentStatus: String = "EN_ROUTE",
  val crisisType: String = "medical",
  val subType: String = "cardiac_arrest",
  val priority: String = "CRITICAL (L1)",
  val victimName: String = "Tanushree Das",
  val victimAddress: String = "Godrej Waterside, Tower 1, Elevator Bank B, Sector V, Kolkata",
  val victimLat: Double = 22.5804,
  val victimLng: Double = 88.4378,
  val victimBloodGroup: String = "B+",
  val hasPacemaker: Boolean = true,
  val allergies: List<String> = listOf("Penicillin", "Sulfa Drugs"),
  val medicalConditions: List<String> = listOf("Type-2 Diabetes", "Mild Hypertension"),
  val emergencyContactName: String = "Subhash Das (Spouse)",
  val emergencyContactPhone: String = "+91 98301 99881",
  val isMedicalIdRevealed: Boolean = true,
  val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
  val responders: List<ResponderTrackingUpdateDto> = listOf(
    ResponderTrackingUpdateDto(
      responderId = "resp_1",
      responderName = "Dr. Anirban Roy",
      latitude = 22.5835,
      longitude = 88.4410,
      heading = 34f,
      bearingCompass = "NE",
      speedKmh = 4.8,
      distanceMeters = 340.0,
      distanceFormatted = "340m",
      etaMinutes = 2.5,
      etaFormatted = "2.5 mins",
      status = "EN_ROUTE",
      isDoctor = true,
      isCprCertified = true,
      verifiedSkills = listOf("DOCTOR", "CPR_CERTIFIED"),
      phone = "+91 98301 55421"
    )
  ),
  val closestAed: TrackingFacilityItemDto? = TrackingFacilityItemDto(
    id = "aed_1",
    name = "Godrej Waterside Tower 1 Lobby AED",
    facilityType = "aed",
    latitude = 22.5806,
    longitude = 88.4385,
    distanceMeters = 85.0,
    distanceFormatted = "85m",
    details = mapOf(
      "building" to "Godrej Waterside Tower 1",
      "location" to "Ground Floor Reception, Wall Cabinet #2 opposite Elevator Bank B",
      "access_code" to "Unlocked • Automated Alarmed Door"
    )
  ),
  val closestHospital: TrackingFacilityItemDto? = TrackingFacilityItemDto(
    id = "hosp_1",
    name = "AMRI Hospitals — Salt Lake (Level 1 Trauma)",
    facilityType = "hospital",
    latitude = 22.5712,
    longitude = 88.4120,
    distanceMeters = 1800.0,
    distanceFormatted = "1.8km",
    details = mapOf(
      "bed_availability" to 38,
      "icu_availability" to 7,
      "emergency_helpline" to "033 6606 3800",
      "trauma_level" to "Level 1 Emergency Center"
    )
  ),
  val currentTurnStepIndex: Int = 0,
  val turnSteps: List<NavigationStepItem> = DEFAULT_NAVIGATION_STEPS,
  val liveSpeedKmh: Double = 4.8,
  val liveGpsAccuracy: Float = 2.5f,
  val liveBearingDeg: Float = 34f,
  val liveBearingCompass: String = "NE",
  val liveEtaFormatted: String = "2.5 mins",
  val liveDistanceFormatted: String = "340m",
  val isGpsSimulationActive: Boolean = false,
  val isCprMetronomeActive: Boolean = false,
  val isAedAttached: Boolean = false,
  val timelineEvents: List<TimelineTrackingEventDto> = emptyList(),
  val toastMessage: String? = null
)
