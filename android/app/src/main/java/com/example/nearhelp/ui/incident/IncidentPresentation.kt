package com.example.nearhelp.ui.incident

import com.example.nearhelp.ui.tracking.ResponderTrackingUpdateDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.ceil

val ENGAGED_STATUSES = setOf("ACCEPTED", "EN_ROUTE", "ARRIVED", "ON_SCENE", "HANDOVER_108")
val TERMINAL_STATUSES = setOf("RESOLVED", "CANCELLED")
private val arrivedStatuses = setOf("ARRIVED", "ON_SCENE", "HANDOVER_108")

enum class RescuePhase { SEARCHING, ACCEPTED, EN_ROUTE, ARRIVED, RESOLVED, CANCELLED }

data class ResponderPresentation(val id: String, val name: String, val status: String,
    val position: ResponderTrackingUpdateDto?, val fresh: Boolean, val eta: String?)
data class IncidentPresentation(val phase: RescuePhase, val responders: List<ResponderPresentation>) {
    val primary get() = responders.firstOrNull()
    val progress: Int get() = when (phase) { RescuePhase.SEARCHING -> 0; RescuePhase.ACCEPTED -> 1; RescuePhase.EN_ROUTE -> 2; else -> 3 }
}

fun telemetryTime(timestamp: String?): Long = timestamp?.let { stamp ->
    runCatching { Instant.parse(stamp).toEpochMilli() }
        .recoverCatching { LocalDateTime.parse(stamp).toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull()
} ?: Long.MIN_VALUE

fun validPosition(value: ResponderTrackingUpdateDto): Boolean = value.latitude.isFinite() && value.longitude.isFinite()
    && value.latitude in -90.0..90.0 && value.longitude in -180.0..180.0

/** Server state drives acceptance/arrival. Fresh telemetry drives ETA, never an animated timer. */
fun presentIncident(state: IncidentUiState, elapsedMillis: Long, wallMillis: Long): IncidentPresentation {
    val incident = state.incident
    val responses = incident?.responses.orEmpty().associateBy { it.responderId }
    val positions = state.snapshot?.responders.orEmpty().filter { it.responderId.isNotBlank() }.associateBy { it.responderId }
    val receiptFresh = state.lastTrackingUpdateElapsed?.let { elapsedMillis - it in 0 until 30_000 } == true
    val responders = (responses.keys + positions.keys).mapNotNull { id ->
        val response = responses[id]
        if (response != null && response.status !in ENGAGED_STATUSES) return@mapNotNull null
        val position = positions[id]
        val statuses = listOfNotNull(response?.status, position?.status).filter { it in ENGAGED_STATUSES }
        val status = statuses.maxByOrNull { when (it) { "HANDOVER_108" -> 4; "ARRIVED", "ON_SCENE" -> 3; "EN_ROUTE" -> 2; else -> 1 } } ?: return@mapNotNull null
        val timestamp = telemetryTime(position?.lastUpdated)
        val sourceFresh = timestamp != Long.MIN_VALUE && wallMillis - timestamp in -30_000..120_000
        val fresh = position != null && validPosition(position) && receiptFresh && sourceFresh
        val eta = if (fresh && position != null && status !in arrivedStatuses && position.etaMinutes.isFinite() && position.etaMinutes >= 0)
            "${ceil(position.etaMinutes).toInt().coerceAtLeast(1)} min" else null
        ResponderPresentation(id, response?.responderName?.takeIf(String::isNotBlank) ?: position?.responderName?.takeIf(String::isNotBlank) ?: "Responder",
            status, position?.takeIf(::validPosition), fresh, eta)
    }.sortedWith(compareBy<ResponderPresentation> { if (it.status in arrivedStatuses) 0 else 1 }.thenBy { if (it.fresh) it.position?.etaMinutes ?: Double.MAX_VALUE else Double.MAX_VALUE })
    val phase = when {
        state.terminalStatus == "RESOLVED" || incident?.status == "RESOLVED" || state.snapshot?.status == "RESOLVED" -> RescuePhase.RESOLVED
        state.terminalStatus == "CANCELLED" || incident?.status == "CANCELLED" || state.snapshot?.status == "CANCELLED" -> RescuePhase.CANCELLED
        responders.any { it.status in arrivedStatuses } -> RescuePhase.ARRIVED
        responders.any { it.status == "EN_ROUTE" } -> RescuePhase.EN_ROUTE
        responders.isNotEmpty() -> RescuePhase.ACCEPTED
        else -> RescuePhase.SEARCHING
    }
    return IncidentPresentation(phase, responders)
}
