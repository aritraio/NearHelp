package com.example.nearhelp.ui.tracking

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LiveTrackingViewModelTest {

  private lateinit var viewModel: LiveTrackingViewModel
  private val gson = Gson()

  @Before
  fun setUp() {
    viewModel = LiveTrackingViewModel()
  }

  @Test
  fun `initial state contains valid incident, turn steps, and responder data`() {
    val state = viewModel.uiState.value
    assertEquals("KOL-SOS-8821", state.incidentId)
    assertEquals(0, state.currentTurnStepIndex)
    assertEquals(4, state.turnSteps.size)
    assertEquals("2.5 mins", state.liveEtaFormatted)
    assertEquals("340m", state.liveDistanceFormatted)
    assertTrue(state.isMedicalIdRevealed)
    assertFalse(state.isCprMetronomeActive)
    assertFalse(state.isAedAttached)
    assertTrue(state.responders.isNotEmpty())
    assertNotNull(state.closestAed)
    assertNotNull(state.closestHospital)
  }

  @Test
  fun `nextTurnStep and prevTurnStep correctly navigate bounds`() {
    assertEquals(0, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.nextTurnStep()
    assertEquals(1, viewModel.uiState.value.currentTurnStepIndex)
    assertEquals("1.8 mins", viewModel.uiState.value.liveEtaFormatted)

    viewModel.nextTurnStep()
    assertEquals(2, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.nextTurnStep()
    assertEquals(3, viewModel.uiState.value.currentTurnStepIndex)

    // Cannot advance past last step
    viewModel.nextTurnStep()
    assertEquals(3, viewModel.uiState.value.currentTurnStepIndex)

    // Move back
    viewModel.prevTurnStep()
    assertEquals(2, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.prevTurnStep()
    viewModel.prevTurnStep()
    assertEquals(0, viewModel.uiState.value.currentTurnStepIndex)

    // Cannot go below 0
    viewModel.prevTurnStep()
    assertEquals(0, viewModel.uiState.value.currentTurnStepIndex)
  }

  @Test
  fun `onArrivedClick sets status to ARRIVED and updates ETA`() {
    viewModel.onArrivedClick()
    val state = viewModel.uiState.value

    assertEquals("ARRIVED", state.incidentStatus)
    assertEquals("Arrived", state.liveEtaFormatted)
    assertEquals("0m", state.liveDistanceFormatted)
    assertEquals(3, state.currentTurnStepIndex)
  }

  @Test
  fun `toggleAedAttached and toggleCprMetronome toggle state flags`() {
    assertFalse(viewModel.uiState.value.isAedAttached)
    viewModel.toggleAedAttached()
    assertTrue(viewModel.uiState.value.isAedAttached)
    viewModel.toggleAedAttached()
    assertFalse(viewModel.uiState.value.isAedAttached)

    assertFalse(viewModel.uiState.value.isCprMetronomeActive)
    viewModel.toggleCprMetronome()
    assertTrue(viewModel.uiState.value.isCprMetronomeActive)
    viewModel.toggleCprMetronome()
    assertFalse(viewModel.uiState.value.isCprMetronomeActive)
  }

  @Test
  fun `toggleMedicalId toggles reveal state`() {
    assertTrue(viewModel.uiState.value.isMedicalIdRevealed)
    viewModel.toggleMedicalId()
    assertFalse(viewModel.uiState.value.isMedicalIdRevealed)
    viewModel.toggleMedicalId()
    assertTrue(viewModel.uiState.value.isMedicalIdRevealed)
  }

  @Test
  fun `status progression flow transitions from ARRIVED to HANDOVER_108 to RESOLVED`() {
    viewModel.onArrivedClick()
    assertEquals("ARRIVED", viewModel.uiState.value.incidentStatus)

    viewModel.onHandover108Click()
    assertEquals("HANDOVER_108", viewModel.uiState.value.incidentStatus)

    viewModel.onResolveClick()
    assertEquals("RESOLVED", viewModel.uiState.value.incidentStatus)
  }

  @Test
  fun `JSON deserialization of backend WebSocket payloads works accurately`() {
    val sampleSnapshotJson = """
      {
        "type": "tracking_snapshot",
        "incident_id": "00000000-0000-0000-0000-000000000001",
        "status": "RESPONDER_ACCEPTED",
        "crisis_type": "medical",
        "sub_type": "cardiac_arrest",
        "severity_score": 90,
        "priority": "critical",
        "incident_latitude": 22.5804,
        "incident_longitude": 88.4378,
        "incident_address": "Godrej Waterside, Sector V",
        "is_anonymous": false,
        "current_radius_meters": 1500.0,
        "responders": [
          {
            "type": "responder_update",
            "responder_id": "00000000-0000-0000-0000-000000000002",
            "responder_name": "Dr. Anirban Roy",
            "latitude": 22.5835,
            "longitude": 88.4410,
            "heading": 34.0,
            "bearing_compass": "NE",
            "speed_kmh": 4.8,
            "distance_meters": 340.0,
            "distance_formatted": "340m",
            "eta_minutes": 2.5,
            "eta_formatted": "2.5 mins",
            "status": "EN_ROUTE",
            "is_doctor": true,
            "is_cpr_certified": true,
            "verified_skills": ["DOCTOR", "CPR_CERTIFIED"]
          }
        ],
        "connected_clients_count": 2
      }
    """.trimIndent()

    val snapshot = gson.fromJson(sampleSnapshotJson, TrackingSnapshotDto::class.java)
    assertNotNull(snapshot)
    assertEquals("00000000-0000-0000-0000-000000000001", snapshot.incidentId)
    assertEquals("cardiac_arrest", snapshot.subType)
    assertEquals(1, snapshot.responders.size)

    val responder = snapshot.responders.first()
    assertEquals("Dr. Anirban Roy", responder.responderName)
    assertTrue(responder.isDoctor)
    assertTrue(responder.isCprCertified)
    assertEquals(340.0, responder.distanceMeters, 0.01)
    assertEquals("2.5 mins", responder.etaFormatted)
  }
}
