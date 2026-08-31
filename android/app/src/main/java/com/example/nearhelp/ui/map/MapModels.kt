package com.example.nearhelp.ui.map

enum class MapLayerKey(val label: String, val iconEmoji: String) {
  VICTIM("Victim SOS", "📍"),
  RESPONDERS("Responders", "🏃"),
  HOSPITALS("Hospitals", "🏥"),
  AEDS("AEDs", "⚡"),
  POSTGIS_WAVE("PostGIS Wave", "🌊"),
  ROUTES("Rescue Routes", "🛤️"),
}

data class ResponderMarker(
  val id: String,
  val name: String,
  val role: String,
  val isDoctor: Boolean = false,
  val isCprCertified: Boolean = true,
  val lat: Double,
  val lng: Double,
  val distanceMeters: Int,
  val etaMinutes: Int,
  val reliabilityScore: Double,
  val phone: String = "+91 98301 22345",
  val currentEtaFormatted: String = "${etaMinutes}m",
)

data class HospitalMarker(
  val id: String,
  val name: String,
  val address: String,
  val lat: Double,
  val lng: Double,
  val bedAvailability: Int,
  val icuAvailability: Int,
  val distanceKm: Double,
  val traumaLevel: String = "Level 1 Emergency Center",
  val emergencyHelpline: String = "033 6606 3800",
)

data class AedMarker(
  val id: String,
  val buildingName: String,
  val locationDescription: String,
  val lat: Double,
  val lng: Double,
  val distanceMeters: Int,
  val is24Hours: Boolean = true,
  val isVerified: Boolean = true,
  val accessCode: String = "Unlocked / Security Desk",
)

sealed class SelectedMapEntity {
  data class VictimEntity(
    val title: String = "Cardiac / Critical SOS Beacon",
    val locationName: String = "Godrej Waterside, Tower 1, Sector V, Kolkata",
    val lat: Double = 22.5804,
    val lng: Double = 88.4378,
    val severity: String = "Critical (Code Red)",
    val timestamp: String = "Just Now",
  ) : SelectedMapEntity()

  data class ResponderEntity(
    val responder: ResponderMarker,
  ) : SelectedMapEntity()

  data class HospitalEntity(
    val hospital: HospitalMarker,
  ) : SelectedMapEntity()

  data class AedEntity(
    val aed: AedMarker,
  ) : SelectedMapEntity()
}

data class SpatialIncident(
  val incidentId: String = "KOL-SOS-8821",
  val locationName: String = "Godrej Waterside, Tower 1, DP Block, Sector V, Kolkata",
  val lat: Double = 22.5804,
  val lng: Double = 88.4378,
  val category: String = "Medical Cardiac Arrest",
  val severity: String = "CRITICAL (L1)",
  val searchRadiusKm: Double = 1.2,
  val responders: List<ResponderMarker> = listOf(
    ResponderMarker(
      id = "resp_1",
      name = "Dr. Anirban Roy",
      role = "Cardiologist (Apollo Clinic)",
      isDoctor = true,
      isCprCertified = true,
      lat = 22.5835,
      lng = 88.4410,
      distanceMeters = 340,
      etaMinutes = 2,
      reliabilityScore = 0.98,
      phone = "+91 98301 55421"
    ),
    ResponderMarker(
      id = "resp_2",
      name = "Priyanka Sen",
      role = "Red Cross Certified First Responder",
      isDoctor = false,
      isCprCertified = true,
      lat = 22.5768,
      lng = 88.4332,
      distanceMeters = 680,
      etaMinutes = 4,
      reliabilityScore = 0.94,
      phone = "+91 98312 99812"
    ),
    ResponderMarker(
      id = "resp_3",
      name = "Subhash Bose",
      role = "Community Emergency Volunteer",
      isDoctor = false,
      isCprCertified = true,
      lat = 22.5850,
      lng = 88.4320,
      distanceMeters = 920,
      etaMinutes = 6,
      reliabilityScore = 0.89,
      phone = "+91 98305 77610"
    )
  ),
  val hospitals: List<HospitalMarker> = listOf(
    HospitalMarker(
      id = "hosp_1",
      name = "AMRI Hospitals — Salt Lake",
      address = "16, 17, JC Block, Broadway Rd, Sector III, Salt Lake, Kolkata",
      lat = 22.5712,
      lng = 88.4120,
      bedAvailability = 38,
      icuAvailability = 7,
      distanceKm = 1.8,
      traumaLevel = "Level 1 Emergency Center",
      emergencyHelpline = "033 6606 3800"
    ),
    HospitalMarker(
      id = "hosp_2",
      name = "Apollo Multispeciality Hospitals",
      address = "58, Canal Circular Rd, Kadapara, Phool Bagan, Kolkata",
      lat = 22.5685,
      lng = 88.4012,
      bedAvailability = 64,
      icuAvailability = 12,
      distanceKm = 2.9,
      traumaLevel = "Comprehensive Cardiac Center",
      emergencyHelpline = "033 2320 3040"
    ),
    HospitalMarker(
      id = "hosp_3",
      name = "ILS Hospitals — Salt Lake",
      address = "DD-6, Salt Lake City, Sector 1, Kolkata",
      lat = 22.5890,
      lng = 88.4200,
      bedAvailability = 22,
      icuAvailability = 4,
      distanceKm = 2.1,
      traumaLevel = "Trauma & Acute Care",
      emergencyHelpline = "033 4031 5000"
    )
  ),
  val aeds: List<AedMarker> = listOf(
    AedMarker(
      id = "aed_1",
      buildingName = "Godrej Waterside Tower 1 Lobby",
      locationDescription = "Ground Floor Reception, Wall Cabinet #2 opposite Elevator Bank B",
      lat = 22.5806,
      lng = 88.4385,
      distanceMeters = 85,
      is24Hours = true,
      isVerified = true,
      accessCode = "Unlocked • Automated Alarmed Door"
    ),
    AedMarker(
      id = "aed_2",
      buildingName = "Technopolis Ground Lobby",
      locationDescription = "Security Station beside Main Turnstiles",
      lat = 22.5842,
      lng = 88.4355,
      distanceMeters = 420,
      is24Hours = true,
      isVerified = true,
      accessCode = "Security Key Available"
    ),
    AedMarker(
      id = "aed_3",
      buildingName = "DLF 1 Commercial Plaza",
      locationDescription = "First-Aid Room, 1st Floor Food Court Concourse",
      lat = 22.5772,
      lng = 88.4310,
      distanceMeters = 550,
      is24Hours = true,
      isVerified = true,
      accessCode = "Code: 1122"
    )
  )
)
