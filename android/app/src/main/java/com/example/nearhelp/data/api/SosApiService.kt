package com.example.nearhelp.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class SosCreateRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("crisis_type") val crisisType: String,
    val description: String,
    val address: String,
)
data class Incident(
    val id: String,
    @SerializedName("broadcaster_id") val broadcasterId: String? = null,
    @SerializedName("crisis_type") val crisisType: String = "medical",
    val status: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val description: String? = null,
    @SerializedName("severity_level") val severityLevel: Int = 0,
    @SerializedName("distance_meters") val distanceMeters: Double? = null,
    val responses: List<IncidentResponse> = emptyList(),
    val timeline: List<IncidentEvent> = emptyList(),
)
data class IncidentResponse(
    @SerializedName("responder_id") val responderId: String,
    @SerializedName("responder_name") val responderName: String,
    val status: String,
)
data class IncidentEvent(
    val id: String,
    @SerializedName("actor_name") val actorName: String? = null,
    @SerializedName("event_type") val eventType: String,
    val details: Map<String, Any?> = emptyMap(),
    val timestamp: String = "",
)
data class IncidentFeed(@SerializedName("active_events") val events: List<Incident> = emptyList())
data class SosRespondRequest(
    val status: String = "ACCEPTED",
    @SerializedName("current_latitude") val latitude: Double?,
    @SerializedName("current_longitude") val longitude: Double?,
)
data class DeviceLocation(val latitude: Double, val longitude: Double, val available: Boolean = true)

interface SosApiService {
    @POST("api/v1/sos/create")
    suspend fun create(@Header("Authorization") token: String, @Header("Idempotency-Key") key: String, @Body body: SosCreateRequest): Response<Incident>
    @GET("api/v1/sos/active")
    suspend fun active(@Header("Authorization") token: String, @Query("lat") lat: Double?, @Query("lon") lon: Double?): Response<IncidentFeed>
    @GET("api/v1/sos/{id}")
    suspend fun detail(@Path("id") id: String): Response<Incident>
    @POST("api/v1/sos/{id}/respond")
    suspend fun respond(@Path("id") id: String, @Header("Authorization") token: String, @Body body: SosRespondRequest): Response<IncidentResponse>
    @PUT("api/v1/sos/{id}/resolve")
    suspend fun resolve(@Path("id") id: String, @Header("Authorization") token: String, @Body body: Map<String, String>): Response<Map<String, Any>>
    @PUT("api/v1/sos/{id}/cancel")
    suspend fun cancel(@Path("id") id: String, @Header("Authorization") token: String): Response<Map<String, Any>>
    @PUT("api/v1/users/me/location")
    suspend fun location(@Header("Authorization") token: String, @Body body: DeviceLocation): Response<Map<String, Any>>
    @POST("api/v1/users/me/fcm-token")
    suspend fun registerPush(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Map<String, Any>>
}
