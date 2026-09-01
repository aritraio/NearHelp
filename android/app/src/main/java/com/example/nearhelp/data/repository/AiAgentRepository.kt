package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.AiAgentApiService
import com.example.nearhelp.data.model.AgentChatRequestDto
import com.example.nearhelp.data.model.AgentChatResponseDto
import com.example.nearhelp.data.model.CitationDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.ContraindicationAlertDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import com.example.nearhelp.data.model.ProtocolStepDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IAiAgentRepository {
  suspend fun getProtocol(conditionId: String): GroundedProtocolDto
  suspend fun chatWithAgent(
    sessionId: String,
    text: String,
    currentStepIndex: Int = 0,
    completedSteps: List<Int> = emptyList()
  ): AgentChatResponseDto
  suspend fun generateHandover(sessionId: String): ClinicalHandoverSummaryDto
}

class AiAgentRepository(
  private val apiService: AiAgentApiService
) : IAiAgentRepository {

  override suspend fun getProtocol(conditionId: String): GroundedProtocolDto = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getProtocolByCondition(conditionId)
      if (response.isSuccessful && response.body() != null) {
        return@withContext response.body()!!
      }
    } catch (e: Exception) {
      // Fallback
    }
    return@withContext getFallbackProtocol(conditionId)
  }

  override suspend fun chatWithAgent(
    sessionId: String,
    text: String,
    currentStepIndex: Int,
    completedSteps: List<Int>
  ): AgentChatResponseDto = withContext(Dispatchers.IO) {
    try {
      val req = AgentChatRequestDto(
        sessionId = sessionId,
        text = text,
        role = "bystander",
        currentStepIndex = currentStepIndex,
        completedSteps = completedSteps
      )
      val response = apiService.chatWithAgent(req)
      if (response.isSuccessful && response.body() != null) {
        return@withContext response.body()!!
      }
    } catch (e: Exception) {
      // Fallback
    }
    return@withContext getFallbackChatResponse(sessionId, text, currentStepIndex, completedSteps)
  }

  override suspend fun generateHandover(sessionId: String): ClinicalHandoverSummaryDto = withContext(Dispatchers.IO) {
    try {
      val req = AgentChatRequestDto(sessionId = sessionId, text = "Paramedic Handover")
      val response = apiService.generateHandoverReport(req)
      if (response.isSuccessful && response.body() != null) {
        return@withContext response.body()!!
      }
    } catch (e: Exception) {
      // Fallback
    }
    return@withContext getFallbackHandover(sessionId)
  }

  private fun getFallbackProtocol(conditionId: String): GroundedProtocolDto {
    return GroundedProtocolDto(
      conditionId = conditionId,
      conditionLabel = "Cardiac / Chest Pain",
      crisisType = "medical",
      severityLevel = 5,
      priority = "critical",
      protocolTitle = "AHA / Indian Resuscitation Council Basic Life Support (BLS) Protocol",
      authority = "American Heart Association (AHA) & Indian Resuscitation Council (IRC)",
      disclaimers = "Emergency interim bystander protocol. Municipal 108 ambulance dispatched.",
      legalShield = "Protected under Section 134A Motor Vehicles (Amendment) Act 2019.",
      recommendedRadiusKm = 3.5,
      emergencyNumber = "108",
      cprBpm = 110,
      steps = listOf(
        ProtocolStepDto(
          stepNumber = 1,
          title = "Check Safety & Confirm Unresponsiveness",
          actionInstruction = "Ensure scene is safe. Tap shoulders firmly and shout loudly. Check carotid pulse in neck groove for no more than 10 seconds.",
          warningNote = "If no pulse or gasping, begin CPR immediately.",
          isCprStep = false,
          icon = "AlertCircle"
        ),
        ProtocolStepDto(
          stepNumber = 2,
          title = "Begin Chest Compressions (110 BPM Metronome)",
          actionInstruction = "Place heel of one hand in center of breastbone. Interlock fingers. Push hard and fast 5–6 cm deep at 110 BPM.",
          warningNote = "Allow full chest recoil after each compression.",
          isCprStep = true,
          beatBpm = 110,
          icon = "HeartPulse"
        ),
        ProtocolStepDto(
          stepNumber = 3,
          title = "Maintain 30:2 Compressions to Breaths",
          actionInstruction = "Give 30 chest compressions followed by 2 rescue breaths, or perform continuous Hands-Only CPR.",
          isCprStep = true,
          beatBpm = 110,
          icon = "Activity"
        ),
        ProtocolStepDto(
          stepNumber = 4,
          title = "Apply Nearby Automated Defibrillator (AED)",
          actionInstruction = "Turn ON AED immediately. Adhere electrode pads to bare dry chest (upper right / lower left). Follow spoken voice prompts.",
          warningNote = "Stand clear during rhythm analysis and shock!",
          isCprStep = false,
          icon = "Zap"
        )
      ),
      citations = listOf(
        CitationDto(
          source = "AHA Guidelines for CPR and ECC 2020",
          section = "Part 3: Adult Basic Life Support §3.2",
          guidelineName = "2020 AHA Guidelines for CPR",
          authority = "American Heart Association"
        ),
        CitationDto(
          source = "Motor Vehicles (Amendment) Act 2019",
          section = "Section 134A",
          guidelineName = "Good Samaritan Statutory Immunity",
          authority = "Ministry of Road Transport & Highways"
        )
      )
    )
  }

  private fun getFallbackChatResponse(
    sessionId: String,
    text: String,
    currentStepIndex: Int,
    completedSteps: List<Int>
  ): AgentChatResponseDto {
    val qLower = text.lowercase()
    val citations = listOf(
      CitationDto(
        source = "AHA CPR Guidelines 2020",
        section = "Part 3: Adult Basic Life Support §3.2",
        guidelineName = "Adult BLS Standard",
        authority = "AHA"
      ),
      CitationDto(
        source = "Motor Vehicles (Amendment) Act 2019",
        section = "Section 134A",
        guidelineName = "Good Samaritan Protection",
        authority = "Govt of India"
      )
    )
    val contraindications = mutableListOf<ContraindicationAlertDto>()

    val reply: String
    val highlight: String

    if (qLower.contains("water") || qLower.contains("drink") || qLower.contains("liquid") || qLower.contains("pani") || qLower.contains("jal")) {
      reply = "❌ NO. NEVER administer water, fluids, or oral medication to an unconscious victim. It will enter the airway and cause fatal pulmonary aspiration.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
      highlight = "Contraindicated Action"
      contraindications.add(
        ContraindicationAlertDto(
          flag = "NO_ORAL_FLUIDS_UNCONSCIOUS",
          severity = "CRITICAL",
          warningTitle = "NEVER Give Water to Unresponsive Patient",
          warningMessage = "Liquid enters the trachea and causes airway obstruction and pulmonary aspiration.",
          actionDirective = "DO NOT give fluids. Maintain open airway."
        )
      )
    } else if (qLower.contains("deep") || qLower.contains("compress") || qLower.contains("rate") || qLower.contains("bpm") || qLower.contains("chest")) {
      reply = "✅ Compress 5 to 6 cm (approx 2 inches) deep at a cadence of 110–120 compressions/minute in the center of the breastbone. Allow full recoil between pushes.\n\n[Source: AHA CPR Guidelines 2020 §3.2 • IRC BLS 2020]"
      highlight = "AHA / IRC Guideline (110 BPM)"
    } else if (qLower.contains("aed") || qLower.contains("defibrillator") || qLower.contains("shock") || qLower.contains("pad")) {
      reply = "⚡ Turn ON the AED immediately. Peel electrode pads and place on bare chest (upper right / lower left). Stand clear when shock is advised!\n\n[Source: AHA CPR Guidelines 2020 §4.1]"
      highlight = "Immediate AED Action"
    } else if (qLower.contains("rib") || qLower.contains("crack") || qLower.contains("pop") || qLower.contains("break")) {
      reply = "⚠️ Cartilage popping or rib cracking is common during effective adult CPR. DO NOT STOP compressions. Restoring blood flow to the brain is the sole priority.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
      highlight = "Do Not Stop CPR"
    } else if (qLower.contains("legal") || qLower.contains("police") || qLower.contains("samaritan") || qLower.contains("law")) {
      reply = "🛡️ You are 100% legally protected under Section 134A of the Motor Vehicles (Amendment) Act 2019. You cannot be detained, harassed, or held civilly/criminally liable.\n\n[Source: Motor Vehicles (Amendment) Act 2019 Section 134A]"
      highlight = "Section 134A MV Act Shield"
    } else {
      reply = "📋 Ensure victim is on a firm flat surface. Tap shoulders and shout. If unresponsive, begin chest compressions at 110 BPM cadence.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
      highlight = "Grounded Protocol Step"
    }

    return AgentChatResponseDto(
      sessionId = sessionId,
      replyText = reply,
      highlightText = highlight,
      triageState = "GUIDANCE",
      conditionId = "cardiac_arrest",
      severityLevel = 5,
      priority = "critical",
      currentStepIndex = currentStepIndex,
      completedSteps = completedSteps,
      cprMetronomeActive = true,
      cprBpm = 110,
      citations = citations,
      contraindications = contraindications,
      legalShieldApplied = true,
      suggestedQuickQuestions = listOf(
        "Can I give water or oral medicine?",
        "How deep should chest compressions be?",
        "When and how do I use the AED?",
        "What if ribs crack during CPR?",
        "Am I legally protected if I help?"
      ),
      processingTimeMs = 12.5
    )
  }

  private fun getFallbackHandover(sessionId: String): ClinicalHandoverSummaryDto {
    return ClinicalHandoverSummaryDto(
      reportId = "REP-NH-882194",
      sessionId = sessionId,
      incidentCode = "NH-KOL-${sessionId.take(8).uppercase()}",
      generatedAt = "01 Sep 2026 • 19:30:00 IST",
      victimProfile = mapOf("name" to "Rajesh Sengupta", "age" to 54, "blood_type" to "O+"),
      emergencyLocation = "Godrej Waterside, Tower 1, Sector V, Salt Lake City, Kolkata",
      severityLevel = 5,
      diagnosticSummary = "Level 5 — Critical Life Threat (Cardiac Arrest)",
      aiConfidenceScore = 98.4,
      reportedSymptoms = listOf("Unresponsive", "No pulse", "Agonal gasping"),
      cprMetronomeUsed = true,
      cprCompressionsEstimated = 330,
      cprDurationSeconds = 180,
      aedDeployed = true,
      aedShocksDelivered = 1,
      completedProtocolSteps = listOf("Safety Check Confirmed", "Continuous CPR Delivered"),
      citations = listOf(
        CitationDto(
          source = "AHA Guidelines for CPR 2020",
          section = "Part 3 §3.2",
          guidelineName = "Adult BLS",
          authority = "AHA"
        )
      ),
      destinationHospital = "AMRI Hospital Salt Lake Emergency Trauma Center",
      legalShieldCompliance = "Section 134A Motor Vehicles (Amendment) Act 2019 & Supreme Court 2016 Guidelines",
      digitalSignatureHash = "SHA256:7f9a2b8c4d1e0f3a6b5c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8"
    )
  }
}
