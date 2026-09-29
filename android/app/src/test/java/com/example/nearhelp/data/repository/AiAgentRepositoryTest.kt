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

  @Test
  fun `fallback chat for period cramps provides dedicated menstrual protocol without generic assessment`() = runTest {
    val response = repository.chatWithAgent(
      sessionId = "period-session-001",
      text = "my girlfriend having periods how to solve this"
    )

    assertEquals("menstrual_health", response.conditionId)
    assertFalse(response.cprMetronomeActive)
    assertEquals(0, response.cprBpm)
    assertFalse(response.replyText.contains("Check scene safety"))
    assertFalse(response.replyText.contains("Dial 108 immediately for ambulance"))
    assertTrue(response.replyText.contains("Menstrual Cramp", ignoreCase = true) || response.replyText.contains("Dysmenorrhea", ignoreCase = true))
    assertTrue(response.replyText.contains("Heat Therapy", ignoreCase = true))
    assertTrue(response.suggestedQuickQuestions.any { it.contains("cramps", ignoreCase = true) })
  }

  @Test
  fun `multi-turn period conversation preserves menstrual context across follow-ups`() = runTest {
    val sessionId = "multi-turn-period-session"

    // Turn 1: Initial inquiry
    val t1 = repository.chatWithAgent(sessionId = sessionId, text = "my girlfriend having periods how to solve this")
    assertEquals("menstrual_health", t1.conditionId)

    // Turn 2: Tablet query without mentioning period
    val t2 = repository.chatWithAgent(sessionId = sessionId, text = "what tablet can she take?")
    assertEquals("menstrual_health", t2.conditionId)
    assertTrue(t2.replyText.contains("Meftal") || t2.replyText.contains("Ibuprofen") || t2.replyText.contains("NSAID"))

    // Turn 3: Food query
    val t3 = repository.chatWithAgent(sessionId = sessionId, text = "what to eat?")
    assertEquals("menstrual_health", t3.conditionId)
    assertTrue(t3.replyText.contains("Nutrition") || t3.replyText.contains("Bananas") || t3.replyText.contains("diet", ignoreCase = true))

    // Turn 4: Heating pad query
    val t4 = repository.chatWithAgent(sessionId = sessionId, text = "can I use heating pad?")
    assertEquals("menstrual_health", t4.conditionId)
    assertTrue(t4.replyText.contains("Heat Therapy", ignoreCase = true))
    assertFalse(t4.replyText.contains("AED"))

    // Turn 5: Hydration query
    val t5 = repository.chatWithAgent(sessionId = sessionId, text = "can she drink cold water?")
    assertEquals("menstrual_health", t5.conditionId)
    assertTrue(t5.contraindications.isEmpty())
    assertTrue(t5.replyText.contains("tea") || t5.replyText.contains("warm water", ignoreCase = true))

    // Turn 6: Escalating pain
    val t6 = repository.chatWithAgent(sessionId = sessionId, text = "what if that doesn't work?")
    assertEquals("menstrual_health", t6.conditionId)
    assertTrue(t6.replyText.contains("Escalating", ignoreCase = true) || t6.replyText.contains("fetal position", ignoreCase = true))
  }

  @Test
  fun `server returning generic triage is intercepted and overridden for non-cardiac query`() = runTest {
    val mockService = object : com.example.nearhelp.data.api.AiAgentApiService {
      override suspend fun getProtocolByCondition(conditionId: String) = throw UnsupportedOperationException()
      override suspend fun getAllProtocols() = throw UnsupportedOperationException()
      override suspend fun chatWithAgent(request: com.example.nearhelp.data.model.AgentChatRequestDto): retrofit2.Response<com.example.nearhelp.data.model.AgentChatResponseDto> {
        val genericBody = com.example.nearhelp.data.model.AgentChatResponseDto(
          sessionId = request.sessionId,
          replyText = "📋 General Emergency Assessment:\n\n1. Check scene safety before approaching.\n2. Check victim responsiveness (tap shoulders and shout).\n3. Check for normal breathing.\n4. Dial 108 immediately for ambulance dispatch.\n\nPlease state the specific emergency (e.g. CPR, bleeding, burns, choking, fracture, snakebite) for step-by-step guidance.\n\n[Source: Indian Resuscitation Council & WHO First-Aid Guidelines]",
          highlightText = "Emergency Triage Assessment",
          triageState = "GUIDANCE",
          conditionId = "general_emergency",
          severityLevel = 3,
          priority = "urgent",
          currentStepIndex = 0,
          completedSteps = emptyList(),
          cprMetronomeActive = false,
          cprBpm = 0,
          citations = emptyList(),
          contraindications = emptyList(),
          legalShieldApplied = true,
          suggestedQuickQuestions = listOf("What should I check first (DRABC)?", "When should I call 108?"),
          processingTimeMs = 120.0
        )
        return retrofit2.Response.success(genericBody)
      }
      override suspend fun generateHandoverReport(request: com.example.nearhelp.data.model.AgentChatRequestDto) = throw UnsupportedOperationException()
    }

    val interceptingRepo = com.example.nearhelp.data.repository.AiAgentRepository(mockService)
    val response = interceptingRepo.chatWithAgent(
      sessionId = "intercept-session-001",
      text = "my girlfriend having periods how to solve this"
    )

    // Verify the generic response from the server was REJECTED and replaced with rich menstrual protocol!
    assertEquals("menstrual_health", response.conditionId)
    assertFalse(response.replyText.contains("General Emergency Assessment"))
    assertFalse(response.replyText.contains("Check scene safety"))
    assertTrue(response.replyText.contains("Menstrual Cramp", ignoreCase = true) || response.replyText.contains("Dysmenorrhea", ignoreCase = true))
  }
}
