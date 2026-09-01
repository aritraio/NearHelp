package com.example.nearhelp.data.api

import com.example.nearhelp.data.model.AgentChatRequestDto
import com.example.nearhelp.data.model.AgentChatResponseDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AiAgentApiService {

  @POST("api/v1/ai/agent/chat")
  suspend fun chatWithAgent(
    @Body request: AgentChatRequestDto
  ): Response<AgentChatResponseDto>

  @GET("api/v1/ai/agent/protocols")
  suspend fun getAllProtocols(): Response<List<GroundedProtocolDto>>

  @GET("api/v1/ai/agent/protocols/{condition_id}")
  suspend fun getProtocolByCondition(
    @Path("condition_id") conditionId: String
  ): Response<GroundedProtocolDto>

  @POST("api/v1/ai/agent/handover")
  suspend fun generateHandoverReport(
    @Body request: AgentChatRequestDto
  ): Response<ClinicalHandoverSummaryDto>
}
