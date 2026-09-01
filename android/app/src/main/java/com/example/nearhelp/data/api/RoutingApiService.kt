package com.example.nearhelp.data.api

import com.example.nearhelp.ui.navigation.DirectionsRequestDto
import com.example.nearhelp.ui.navigation.DirectionsResponseDto
import com.example.nearhelp.ui.navigation.HazardsListResponseDto
import com.example.nearhelp.ui.navigation.RoadHazardReportDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RoutingApiService {

  @POST("api/v1/routing/directions")
  suspend fun getDirections(
    @Body request: DirectionsRequestDto
  ): Response<DirectionsResponseDto>

  @POST("api/v1/routing/detour")
  suspend fun getDetourAnalysis(
    @Body request: DirectionsRequestDto
  ): Response<DirectionsResponseDto>

  @GET("api/v1/routing/hazards")
  suspend fun getHazards(): Response<HazardsListResponseDto>

  @POST("api/v1/routing/hazards")
  suspend fun reportHazard(
    @Body report: RoadHazardReportDto
  ): Response<Map<String, Any>>
}
