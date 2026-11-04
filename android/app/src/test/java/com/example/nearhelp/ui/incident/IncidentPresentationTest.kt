package com.example.nearhelp.ui.incident

import com.example.nearhelp.data.api.Incident
import com.example.nearhelp.data.api.IncidentResponse
import com.example.nearhelp.ui.tracking.ResponderTrackingUpdateDto
import com.example.nearhelp.ui.tracking.TrackingSnapshotDto
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class IncidentPresentationTest {
    private val wall = Instant.parse("2026-11-03T00:00:10Z").toEpochMilli()
    private fun position(status: String = "ACCEPTED", stamp: String? = "2026-11-03T00:00:05Z") = ResponderTrackingUpdateDto(
        responderId = "helper", responderName = "Riya", latitude = 22.724, longitude = 88.48,
        distanceMeters = 200.0, etaMinutes = 2.1, status = status, lastUpdated = stamp)
    private fun state(status: String = "ACCEPTED", position: ResponderTrackingUpdateDto? = position(status)) = IncidentUiState(
        incident = Incident(id = "id", status = "RESPONDER_ACCEPTED", responses = listOf(IncidentResponse("helper", "Riya", status))),
        snapshot = TrackingSnapshotDto(incidentId = "id", responders = listOfNotNull(position)), lastTrackingUpdateElapsed = 1000L)

    @Test fun confirmedSOSWithoutAcceptanceIsSearching_andAcceptanceIsNotEnRoute() {
        assertEquals(RescuePhase.SEARCHING, presentIncident(IncidentUiState(incident = Incident(id = "id")), 1000, wall).phase)
        val accepted = presentIncident(state(), 1000, wall)
        assertEquals(RescuePhase.ACCEPTED, accepted.phase)
        assertEquals(1, accepted.progress)
        assertEquals("Riya", accepted.primary?.name)
        assertEquals("3 min", accepted.primary?.eta)
        assertEquals(RescuePhase.EN_ROUTE, presentIncident(state("EN_ROUTE"), 1000, wall).phase)
        assertEquals(RescuePhase.ARRIVED, presentIncident(state("ARRIVED"), 1000, wall).phase)
    }

    @Test fun acceptedNameAppearsBeforeGps_andNoEtaOrPinIsInvented() {
        val result = presentIncident(state(position = null), 1000, wall)
        assertEquals("Riya", result.primary?.name)
        assertNull(result.primary?.position)
        assertNull(result.primary?.eta)
        assertEquals(RescuePhase.ACCEPTED, result.phase)
    }

    @Test fun unknownOrOldTelemetryAndDisconnectedReceiptsSuppressEtaWithoutRemovingName() {
        for (stamp in listOf(null, "2026-11-02T23:50:00Z", "bad-time")) {
            val result = presentIncident(state(position = position(stamp = stamp)), 1000, wall)
            assertNull(result.primary?.eta)
            assertEquals("Riya", result.primary?.name)
        }
        assertNull(presentIncident(state(), 31000, wall).primary?.eta)
    }

    @Test fun snapshotAcceptanceCanArriveBeforeHttpDetail_andExplicitDeclinesRemoveOldPins() {
        val snapshotFirst = state().copy(incident = Incident(id = "id"))
        assertEquals(RescuePhase.ACCEPTED, presentIncident(snapshotFirst, 1000, wall).phase)
        assertTrue(presentIncident(state("DECLINED", position()), 1000, wall).responders.isEmpty())
    }

    @Test fun arrivalIsNotLostWhenAnOlderLocationPacketStillSaysEnRoute() {
        assertEquals(RescuePhase.ARRIVED, presentIncident(state("ARRIVED", position("EN_ROUTE")), 1000, wall).phase)
    }

    @Test fun sparseJsonNeverSuppliesDemoCoordinatesEtaOrPhone() {
        val gson = Gson()
        val responder = gson.fromJson("""{"responder_id":"helper","responder_name":"Riya","status":"ACCEPTED"}""", ResponderTrackingUpdateDto::class.java)
        assertFalse(validPosition(responder)); assertTrue(responder.etaMinutes.isNaN()); assertNull(responder.phone)
        val created = gson.fromJson("""{"id":"id","status":"SOS_TRIGGERED"}""", Incident::class.java)
        assertTrue(created.responses.isEmpty()); assertTrue(created.timeline.isEmpty())
    }
}
