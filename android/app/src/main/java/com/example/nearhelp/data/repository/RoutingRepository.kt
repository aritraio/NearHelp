package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.RoutingApiService
import com.example.nearhelp.ui.navigation.DEFAULT_AED_RESCUE_ROUTE
import com.example.nearhelp.ui.navigation.DEFAULT_DETOUR_RESCUE_ROUTE
import com.example.nearhelp.ui.navigation.DEFAULT_HAZARDS
import com.example.nearhelp.ui.navigation.DEFAULT_PRIMARY_RESCUE_ROUTE
import com.example.nearhelp.ui.navigation.DEFAULT_RECOMMENDATION
import com.example.nearhelp.ui.navigation.DirectionsRequestDto
import com.example.nearhelp.ui.navigation.DirectionsResponseData
import com.example.nearhelp.ui.navigation.RoadHazardDto
import com.example.nearhelp.ui.navigation.RoadHazardReportDto
import com.example.nearhelp.ui.navigation.RouteLocationPoint

interface IRoutingRepository {
  suspend fun fetchRescueDirections(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    travelMode: String = "walking",
    avoidHazards: Boolean = true,
    includeAedDetour: Boolean = true,
    aedLat: Double? = 22.5806,
    aedLng: Double? = 88.4385,
    aedName: String? = "Godrej Waterside Tower 1 Lobby AED"
  ): Result<DirectionsResponseData>

  suspend fun fetchActiveHazards(): Result<List<RoadHazardDto>>

  suspend fun reportRoadHazard(
    title: String,
    hazardType: String,
    severity: String,
    latitude: Double,
    longitude: Double,
    description: String
  ): Result<Boolean>
}

class RoutingRepository(
  private val apiService: RoutingApiService
) : IRoutingRepository {

  override suspend fun fetchRescueDirections(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    travelMode: String,
    avoidHazards: Boolean,
    includeAedDetour: Boolean,
    aedLat: Double?,
    aedLng: Double?,
    aedName: String?
  ): Result<DirectionsResponseData> {
    return try {
      val request = DirectionsRequestDto(
        originLatitude = originLat,
        originLongitude = originLng,
        destinationLatitude = destLat,
        destinationLongitude = destLng,
        travelMode = travelMode,
        avoidHazards = avoidHazards,
        includeAedDetour = includeAedDetour,
        aedLatitude = aedLat,
        aedLongitude = aedLng,
        aedName = aedName
      )
      val response = apiService.getDirections(request)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!.data)
      } else {
        Result.success(getFallbackDirectionsData(originLat, originLng, destLat, destLng))
      }
    } catch (e: Exception) {
      // Offline fallback for emergency operational resilience
      Result.success(getFallbackDirectionsData(originLat, originLng, destLat, destLng))
    }
  }

  override suspend fun fetchActiveHazards(): Result<List<RoadHazardDto>> {
    return try {
      val response = apiService.getHazards()
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!.hazards)
      } else {
        Result.success(DEFAULT_HAZARDS)
      }
    } catch (e: Exception) {
      Result.success(DEFAULT_HAZARDS)
    }
  }

  override suspend fun reportRoadHazard(
    title: String,
    hazardType: String,
    severity: String,
    latitude: Double,
    longitude: Double,
    description: String
  ): Result<Boolean> {
    return try {
      val payload = RoadHazardReportDto(
        title = title,
        hazardType = hazardType,
        severity = severity,
        latitude = latitude,
        longitude = longitude,
        description = description
      )
      val response = apiService.reportHazard(payload)
      Result.success(response.isSuccessful)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun getFallbackDirectionsData(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
  ): DirectionsResponseData {
    return DirectionsResponseData(
      origin = RouteLocationPoint(originLat, originLng),
      destination = RouteLocationPoint(destLat, destLng),
      travelMode = "walking",
      primaryRoute = DEFAULT_PRIMARY_RESCUE_ROUTE,
      detourRoute = DEFAULT_DETOUR_RESCUE_ROUTE,
      aedPickupRoute = DEFAULT_AED_RESCUE_ROUTE,
      activeHazardsCount = DEFAULT_HAZARDS.size,
      recommendation = DEFAULT_RECOMMENDATION
    )
  }
}
