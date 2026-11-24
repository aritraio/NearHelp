package com.example.nearhelp.data.repository

import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.*
import com.example.nearhelp.ui.tracking.TrackingSnapshotDto
import com.example.nearhelp.ui.navigation.DirectionsRequestDto
import com.example.nearhelp.ui.navigation.RouteLocationPoint
import com.google.gson.Gson
import retrofit2.Response

/** An uncertain POST is retained verbatim so every retry uses the same idempotency key. */
data class PendingMedicalSos(val key: String, val request: SosCreateRequest)

interface IncidentRepository {
    val userId: String?
    fun activeIncidentId(): String?
    fun saveActiveIncident(id: String?)
    fun pendingSos(): PendingMedicalSos?
    fun savePendingSos(pending: PendingMedicalSos?)
    fun cancellationPending(): Boolean
    fun saveCancellationPending(value: Boolean)
    fun record(incident: Incident)
    suspend fun socketToken(): String
    suspend fun create(pending: PendingMedicalSos): Incident
    suspend fun detail(id: String): Incident
    suspend fun tracking(id: String): TrackingSnapshotDto
    suspend fun active(): List<Incident>
    suspend fun respond(id: String, request: SosRespondRequest): IncidentResponse
    suspend fun finish(id: String, cancel: Boolean): String
    suspend fun location(value: DeviceLocation)
    suspend fun route(originLat: Double, originLng: Double, destinationLat: Double, destinationLng: Double): List<RouteLocationPoint>
}

class AndroidIncidentRepository : IncidentRepository {
    private val api get() = RetrofitClient.getSosApiService()
    private val storage get() = NearHelpApplication.instance.tokenStorage
    private val gson = Gson()
    override val userId get() = storage.getUserId()
    override fun activeIncidentId() = storage.getActiveIncidentId()
    override fun saveActiveIncident(id: String?) = storage.saveActiveIncidentId(id)
    override fun pendingSos(): PendingMedicalSos? {
        val key = storage.getPendingSosKey() ?: return null
        val payload = storage.getPendingSosPayload() ?: return null
        return runCatching { PendingMedicalSos(key, gson.fromJson(payload, SosCreateRequest::class.java)) }.getOrNull()
    }
    override fun savePendingSos(pending: PendingMedicalSos?) = storage.savePendingSos(pending?.key, pending?.let { gson.toJson(it.request) })
    override fun cancellationPending() = storage.getPendingSosCancellation()
    override fun saveCancellationPending(value: Boolean) = storage.savePendingSosCancellation(value)
    override fun record(incident: Incident) = storage.recordIncident(incident)
    override suspend fun socketToken() = SessionTokens.bearer().removePrefix("Bearer ")
    override suspend fun create(pending: PendingMedicalSos) = checked(api.create(SessionTokens.bearer(), pending.key, pending.request))
    override suspend fun detail(id: String) = checked(api.detail(id))
    override suspend fun tracking(id: String) = checked(api.tracking(id))
    override suspend fun active() = checked(api.active(SessionTokens.bearer(), null, null)).events
    override suspend fun respond(id: String, request: SosRespondRequest) = checked(api.respond(id, SessionTokens.bearer(), request))
    override suspend fun finish(id: String, cancel: Boolean): String {
        val response = if (cancel) checked(api.cancel(id, SessionTokens.bearer()))
            else checked(api.resolve(id, SessionTokens.bearer(), mapOf("resolution_notes" to "Resolved from Android", "resolved_by" to "victim")))
        return response["status"]?.toString()?.takeIf { it in listOf("RESOLVED", "CANCELLED") }
            ?: error("The server has not confirmed that the SOS ended. Please retry.")
    }
    override suspend fun location(value: DeviceLocation) { checked(api.location(SessionTokens.bearer(), value)) }
    override suspend fun route(originLat: Double, originLng: Double, destinationLat: Double, destinationLng: Double): List<RouteLocationPoint> {
        val response = checked(RetrofitClient.getRoutingApiService().getDirections(DirectionsRequestDto(
            originLatitude = originLat, originLongitude = originLng, destinationLatitude = destinationLat,
            destinationLongitude = destinationLng, includeAedDetour = false, aedLatitude = null, aedLongitude = null, aedName = null)))
        return response.data.primaryRoute.polylinePoints
    }
    private fun <T> checked(response: Response<T>): T {
        if (!response.isSuccessful) {
            val detail = runCatching { gson.fromJson(response.errorBody()?.string(), Map::class.java)["detail"] }.getOrNull()
            error(detail?.toString() ?: "Server returned ${response.code()}. Please try again.")
        }
        return response.body() ?: error("The server returned an empty response.")
    }
}
