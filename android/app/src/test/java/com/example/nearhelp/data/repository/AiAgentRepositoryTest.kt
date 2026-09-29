package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.AiAgentApiService
import com.example.nearhelp.data.model.AgentChatRequestDto
import com.example.nearhelp.data.model.AgentChatResponseDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AiAgentRepositoryTest {

  private lateinit var fakeApiService: FakeAiAgentApiService
  private lateinit var repository: AiAgentRepository

  private class FakeAiAgentApiService : AiAgentApiService {
    var shouldFail: Boolean = true

    override suspend fun chatWithAgent(request: AgentChatRequestDto): Response<AgentChatResponseDto> {
      if (shouldFail) {
        val errorBody = "{\"error\": \"offline\"}".toResponseBody("application/json".toMediaTypeOrNull())
        return Response.error(503, errorBody)
      }
      return Response.error(500, "".toResponseBody(null))
    }

    override suspend fun getAllProtocols(): Response<List<GroundedProtocolDto>> {
      return Response.error(503, "".toResponseBody(null))
    }

    override suspend fun getProtocolByCondition(conditionId: String): Response<GroundedProtocolDto> {
      return Response.error(503, "".toResponseBody(null))
    }

    override suspend fun generateHandoverReport(request: AgentChatRequestDto): Response<ClinicalHandoverSummaryDto> {
      return Response.error(503, "".toResponseBody(null))
    }
  }

  @Before
  fun setUp() {
    fakeApiService = FakeAiAgentApiService()
    repository = AiAgentRepository(fakeApiService, null)
  }

  @Test
  fun `fallback chat for burns sets condition burns and does not activate CPR metronome`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "How to treat a hot oil burn on hand?"
    )

    assertEquals("burns", response.conditionId)
    assertFalse("CPR metronome should NOT be active for burn emergencies", response.cprMetronomeActive)
    assertEquals(0, response.cprBpm)
    assertTrue(response.replyText.contains("20 full minutes"))
    assertTrue(response.suggestedQuickQuestions.any { it.contains("blister") || it.contains("cool") || it.contains("ice") })
  }

  @Test
  fun `fallback chat for cardiac query activates CPR metronome and returns cardiac_arrest`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Patient is unresponsive, start CPR chest compressions"
    )

    assertEquals("cardiac_arrest", response.conditionId)
    assertTrue("CPR metronome should be active for cardiac emergencies", response.cprMetronomeActive)
    assertEquals(110, response.cprBpm)
    assertTrue(response.replyText.contains("110"))
    assertTrue(response.suggestedQuickQuestions.any { it.contains("compressions") || it.contains("AED") })
  }

  @Test
  fun `fallback chat for choking sets condition choking without CPR metronome`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Victim is choking on food and holding their neck"
    )

    assertEquals("choking", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertTrue(response.replyText.contains("Heimlich") || response.replyText.contains("abdominal thrusts"))
  }

  @Test
  fun `detectConditionFromText maps symptoms accurately`() {
    assertEquals("severe_bleeding", repository.detectConditionFromText("Heavy bleeding from leg"))
    assertEquals("leg_fracture", repository.detectConditionFromText("Suspected broken bone in leg"))
    assertEquals("seizures", repository.detectConditionFromText("Patient having fit and convulsion"))
    assertEquals("stroke", repository.detectConditionFromText("Face is drooping and slurred speech"))
    assertEquals("snakebite", repository.detectConditionFromText("Bitten by a venomous snake"))
    assertEquals("asthma", repository.detectConditionFromText("Need inhaler for breathlessness"))
    assertEquals("heatstroke", repository.detectConditionFromText("High fever from sunstroke in heat"))
    assertEquals("poisoning", repository.detectConditionFromText("Accidentally swallowed toxic chemical"))
  }

  @Test
  fun `water contraindication is triggered for unconscious queries`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Can I give water or drink to an unconscious person?"
    )

    assertTrue(response.contraindications.isNotEmpty())
    assertEquals("NO_ORAL_FLUIDS_UNCONSCIOUS", response.contraindications.first().flag)
    assertTrue(response.replyText.contains("NEVER administer water"))
  }

  @Test
  fun `fallback chat for nosebleed provides epistaxis protocol and no CPR metronome`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Victim has severe nosebleed"
    )

    assertEquals("severe_bleeding", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertTrue(response.replyText.contains("lean slightly FORWARD", ignoreCase = true))
    assertTrue(response.replyText.contains("Pinch the soft part", ignoreCase = true))
  }

  @Test
  fun `fallback chat for infant choking provides pediatric protocol`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Baby is choking and cannot breathe"
    )

    assertEquals("choking", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertTrue(response.replyText.contains("back slaps", ignoreCase = true))
    assertTrue(response.replyText.contains("chest thrusts", ignoreCase = true))
  }

  @Test
  fun `fallback chat for spinal trauma warns against moving victim`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Person fell down stairs and has neck injury"
    )

    assertEquals("head_injury", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertTrue(response.replyText.contains("DO NOT MOVE", ignoreCase = true))
  }

  @Test
  fun `fallback chat for dog bite provides rabies prevention protocol`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "Street dog bite on leg"
    )

    assertFalse(response.cprMetronomeActive)
    assertTrue(response.replyText.contains("15 full minutes", ignoreCase = true))
    assertTrue(response.replyText.contains("Rabies", ignoreCase = true))
  }

  @Test
  fun `fallback chat for headache provides clinical advisory without CPR metronome`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "test-session",
      text = "I have headache"
    )

    assertEquals("medical_symptom", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertEquals(0, response.cprBpm)
    assertTrue(response.replyText.contains("headache", ignoreCase = true) || response.replyText.contains("Medical Advisory", ignoreCase = true))
    assertTrue(response.suggestedQuickQuestions.any { it.contains("headache") || it.contains("painkillers") })
  }
}
