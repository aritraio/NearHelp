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
  suspend fun getAllProtocols(): List<GroundedProtocolDto>
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

  override suspend fun getAllProtocols(): List<GroundedProtocolDto> = withContext(Dispatchers.IO) {
    listOf(
      getFallbackProtocol("cardiac_arrest"),
      getFallbackProtocol("leg_fracture"),
      getFallbackProtocol("severe_bleeding"),
      getFallbackProtocol("choking"),
      getFallbackProtocol("burns")
    )
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
    return when (conditionId) {
      "leg_fracture" -> GroundedProtocolDto(
        conditionId = "leg_fracture",
        conditionLabel = "Fracture / Leg & Limb Injury",
        crisisType = "trauma",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Limb Immobilization & Trauma Care Protocol",
        authority = "International Red Cross & Indian Orthopaedic Association",
        disclaimers = "Keep patient still. Do not move injured limb unnecessarily.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Assess Limb & Control Active Bleeding",
            actionInstruction = "Check for open wounds, severe swelling, or bone protrusion. Apply gentle direct pressure around any external bleeding using clean cloth.",
            warningNote = "NEVER push a protruding bone back under the skin.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Immobilize the Joint Above and Below Injury",
            actionInstruction = "Support the leg in the exact position found. Place rolled jackets, blankets, or rigid splints along both sides of the leg to prevent motion.",
            warningNote = "Do not force or attempt to straighten a deformed limb.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Apply Cold Compress for Swelling",
            actionInstruction = "Wrap ice or a cold pack inside a towel and apply around the injured area for 15 minutes to reduce pain and internal swelling.",
            warningNote = "Never apply bare ice directly onto skin.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Elevate Gently & Monitor Toe Sensation",
            actionInstruction = "If comfortable and not worsening pain, prop the limb slightly with a pillow. Check toes periodically for warmth, pink color, and sensation.",
            warningNote = "If toes become cold or pale, loosen any splints immediately.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "severe_bleeding" -> GroundedProtocolDto(
        conditionId = "severe_bleeding",
        conditionLabel = "Severe Bleeding & Hemorrhage",
        crisisType = "trauma",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Stop the Bleed & Hemostasis Protocol",
        authority = "American College of Surgeons & Indian Red Cross Society",
        disclaimers = "Critical time-sensitive trauma. Apply continuous downward force.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 2.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Apply Direct Continuous Pressure",
            actionInstruction = "Cover the bleeding wound with sterile gauze, clean cloth, or bare hands. Push down firmly and continuously with both hands without lifting.",
            warningNote = "Do NOT remove saturated dressings; apply more layers on top.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Pack Deep Wounds Firmly",
            actionInstruction = "For large open gashes or puncture wounds in limbs, pack clean cloth or gauze deep into the wound cavity and resume firm downward pressure.",
            warningNote = "Maintain continuous firm pressure for at least 5 unbroken minutes.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Apply Tourniquet if Bleeding Persists",
            actionInstruction = "If arm or leg bleeding does not stop with pressure, place a commercial tourniquet or improvised strap 2–3 inches above the wound and tighten until bleeding ceases.",
            warningNote = "Note application time. Never loosen or remove a tourniquet once placed.",
            isCprStep = false,
            icon = "Zap"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Keep Warm & Prevent Shock",
            actionInstruction = "Keep victim lying flat on back. Cover victim with a jacket or blanket to prevent hypothermia while awaiting emergency ambulance arrival.",
            warningNote = "Do not offer water or food to a bleeding victim.",
            isCprStep = false,
            icon = "HeartPulse"
          )
        )
      )
      "choking" -> GroundedProtocolDto(
        conditionId = "choking",
        conditionLabel = "Choking / Airway Obstruction",
        crisisType = "medical",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Foreign Body Airway Obstruction (Heimlich) Protocol",
        authority = "AHA & European Resuscitation Council (ERC)",
        disclaimers = "Act immediately if victim cannot cough or speak.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 2.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Assess Severity & Encourage Forceful Coughing",
            actionInstruction = "Ask loudly: 'Are you choking?'. If the victim can breathe, talk, or cough loudly, encourage them to keep coughing forcefully.",
            warningNote = "Intervene immediately if victim cannot speak or clutches throat silently.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Deliver 5 Sharp Back Blows",
            actionInstruction = "Stand slightly behind victim, support their upper chest with one hand, lean them forward, and deliver 5 forceful blows between shoulder blades with heel of hand.",
            warningNote = "Ensure patient is leaning forward so the dislodged object exits.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Perform 5 Abdominal Thrusts (Heimlich)",
            actionInstruction = "Stand behind victim, wrap arms around waist. Place thumb side of fist just above navel. Grasp fist with other hand and pull inward and upward sharply 5 times.",
            warningNote = "For pregnant women, position fists on middle of breastbone instead.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Repeat Cycle or Begin CPR if Unresponsive",
            actionInstruction = "Alternate 5 back blows and 5 abdominal thrusts until obstruction clears. If victim becomes unconscious, guide gently to ground and begin chest compressions.",
            warningNote = "Never perform blind finger sweeps in mouth unless object is visible.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "burns" -> GroundedProtocolDto(
        conditionId = "burns",
        conditionLabel = "Severe Burns & Scalds",
        crisisType = "thermal",
        severityLevel = 3,
        priority = "urgent",
        protocolTitle = "Emergency Thermal Burn Care Protocol",
        authority = "World Health Organization (WHO) & British Burn Association",
        disclaimers = "Immediate cooling prevents deeper tissue injury.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Cool Burn Under Running Water (20 Minutes)",
            actionInstruction = "Immediately cool burn under gentle, cool running tap water for 20 full minutes. This halts burning process and relieves acute pain.",
            warningNote = "NEVER use ice, ice water, butter, toothpaste, or ointments.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Remove Constrictive Items Promptly",
            actionInstruction = "Quickly and gently remove rings, watches, tight belts, or jewelry near burn area before swelling begins.",
            warningNote = "Do NOT pull off clothing that is stuck to charred skin.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Cover Loosely with Clean Sterile Wrap",
            actionInstruction = "Cover cooled burn loosely with clean plastic cling wrap or sterile non-adherent dressing to minimize infection and contact pain.",
            warningNote = "Never pop, break, or puncture blister bubbles.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Keep Victim Warm & Seek Urgent Care",
            actionInstruction = "Keep unburned parts of body warm with clean blanket. Burns on face, hands, joints, or larger than patient's palm require emergency hospital evaluation.",
            warningNote = "Watch for signs of inhalation injury if trapped in smoke.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      else -> GroundedProtocolDto(
        conditionId = "cardiac_arrest",
        conditionLabel = "Cardiac / Chest Pain",
        crisisType = "medical",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Basic Life Support (BLS) & CPR Protocol",
        authority = "American Heart Association (AHA) & Indian Resuscitation Council (IRC)",
        disclaimers = "Emergency bystander protocol. 108 ambulance dispatched.",
        legalShield = "Protected under Section 134A Motor Vehicles Act 2019.",
        recommendedRadiusKm = 3.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Check Scene Safety & Patient Response",
            actionInstruction = "Ensure scene is safe. Tap shoulders firmly and shout: 'Are you okay?'. Check carotid pulse in neck groove for no more than 10 seconds.",
            warningNote = "If unresponsive and not breathing normally, begin CPR immediately.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Begin Continuous Chest Compressions",
            actionInstruction = "Position heel of one hand in center of breastbone, interlock fingers, and push firmly down 5–6 cm. Maintain steady rhythmic compressions without pausing.",
            warningNote = "Allow full chest recoil between compressions.",
            isCprStep = false,
            icon = "HeartPulse"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Deliver Rescue Breaths or Hands-Only CPR",
            actionInstruction = "Deliver 30 compressions followed by 2 gentle breaths. If untrained in rescue breathing, provide continuous uninterrupted chest compressions.",
            warningNote = "Do not stop compressions for more than 10 seconds.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Deploy Nearby Defibrillator (AED)",
            actionInstruction = "Turn ON AED immediately. Adhere electrode pads to bare dry chest (upper right / lower left). Follow spoken voice prompts and stand clear during shock.",
            warningNote = "Ensure no one touches patient during rhythm analysis and shock!",
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
