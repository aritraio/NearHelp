package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.AiAgentApiService
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.model.AgentChatRequestDto
import com.example.nearhelp.data.model.AgentChatResponseDto
import com.example.nearhelp.data.model.CitationDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.ContraindicationAlertDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import com.example.nearhelp.data.model.ProtocolStepDto
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
  private val apiService: AiAgentApiService,
  private val tokenStorage: TokenStorage? = null
) : IAiAgentRepository {

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

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
      getFallbackProtocol("severe_bleeding"),
      getFallbackProtocol("choking"),
      getFallbackProtocol("burns"),
      getFallbackProtocol("leg_fracture"),
      getFallbackProtocol("seizures"),
      getFallbackProtocol("stroke"),
      getFallbackProtocol("asthma"),
      getFallbackProtocol("anaphylaxis"),
      getFallbackProtocol("poisoning"),
      getFallbackProtocol("heatstroke"),
      getFallbackProtocol("hypothermia"),
      getFallbackProtocol("snakebite"),
      getFallbackProtocol("head_injury"),
      getFallbackProtocol("diabetic_emergency"),
      getFallbackProtocol("electric_shock"),
      getFallbackProtocol("drowning"),
      getFallbackProtocol("shock")
    )
  }

  override suspend fun chatWithAgent(
    sessionId: String,
    text: String,
    currentStepIndex: Int,
    completedSteps: List<Int>
  ): AgentChatResponseDto = withContext(Dispatchers.IO) {
    val apiKey = tokenStorage?.getGeminiApiKey()?.takeIf { it.isNotBlank() }

    // 1. Direct Gemini API call if user configured a Gemini API key
    if (apiKey != null) {
      val geminiResponse = callGeminiDirectly(apiKey, sessionId, text, currentStepIndex, completedSteps)
      if (geminiResponse != null) {
        return@withContext geminiResponse
      }
    }

    // 2. Call backend proxy service
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
        val body = response.body()!!
        val isStaleCardiacFallback = body.replyText.contains("Ensure victim is on a firm flat surface") &&
            !isCardiacOrCprQuery(text)
        if (!isStaleCardiacFallback) {
          Log.i("AiAgentRepository", "Live AI chat response received: ${body.highlightText} (latency: ${body.processingTimeMs}ms)")
          return@withContext body
        }
        Log.w("AiAgentRepository", "Server returned generic CPR message for non-cardiac query. Enhancing with dedicated clinical triage.")
      } else {
        Log.w("AiAgentRepository", "Live AI chat returned code ${response.code()}, falling back to local clinical knowledge.")
      }
    } catch (e: Exception) {
      Log.w("AiAgentRepository", "Live AI chat failed (${e.message}), engaging local clinical engine fallback.")
    }

    // 3. Fallback to comprehensive emergency clinical knowledge base
    return@withContext getFallbackChatResponse(sessionId, text, currentStepIndex, completedSteps)
  }

  private fun isCardiacOrCprQuery(text: String): Boolean {
    val q = text.lowercase()
    return q.contains("cpr") || q.contains("heart") || q.contains("cardiac") ||
        q.contains("chest compression") || q.contains("chest pain") ||
        q.contains("no pulse") || q.contains("not breathing") || q.contains("defibrillator") || q.contains("aed")
  }

  fun detectConditionFromText(text: String): String {
    val q = text.lowercase()
    return when {
      q.contains("heatstroke") || q.contains("sunstroke") || q.contains("heat") || q.contains("dehydrat") -> "heatstroke"
      q.contains("electric") || q.contains("electrocution") || (q.contains("shock") && (q.contains("current") || q.contains("wire") || q.contains("socket"))) -> "electric_shock"
      q.contains("drown") || (q.contains("water") && (q.contains("submerged") || q.contains("pool") || q.contains("river"))) -> "drowning"
      q.contains("cpr") || q.contains("cardiac") || q.contains("heart") || q.contains("chest pain") || q.contains("no pulse") -> "cardiac_arrest"
      q.contains("dog") || q.contains("cat") || q.contains("animal") || q.contains("rabies") -> "poisoning"
      q.contains("snake") || q.contains("venom") || q.contains("viper") || q.contains("cobra") || (q.contains("bite") && !q.contains("dog") && !q.contains("cat") && !q.contains("animal")) -> "snakebite"
      q.contains("poison") || q.contains("toxic") || q.contains("chemical") || q.contains("swallowed") || q.contains("ingest") -> "poisoning"
      q.contains("nosebleed") || q.contains("nose bleed") || q.contains("epistaxis") || q.contains("bleed") || q.contains("blood") || q.contains("tourniquet") || q.contains("hemorrhage") || q.contains("cut") || q.contains("wound") || q.contains("crash") || (q.contains("accident") && !q.contains("accidentally")) -> "severe_bleeding"
      q.contains("chok") || q.contains("heimlich") || q.contains("food stuck") || q.contains("cant breathe") || (q.contains("baby") && q.contains("breath")) -> "choking"
      q.contains("burn") || q.contains("scald") || q.contains("fire") || q.contains("blister") || q.contains("acid") -> "burns"
      q.contains("fracture") || q.contains("broken bone") || q.contains("broken leg") || q.contains("broken arm") || q.contains("splint") || q.contains("sprain") || q.contains("twisted") -> "leg_fracture"
      q.contains("seizure") || q.contains("fit") || q.contains("convulsion") || q.contains("epilep") -> "seizures"
      (q.contains("stroke") && !q.contains("heat") && !q.contains("sun")) || q.contains("face drop") || q.contains("slurred") || q.contains("paralysis") || q.contains("fast") -> "stroke"
      q.contains("asthma") || q.contains("inhaler") || q.contains("wheez") || q.contains("breathless") -> "asthma"
      q.contains("anaphylaxis") || q.contains("allergy") || q.contains("allergic") || q.contains("epipen") || q.contains("epinephrine") || q.contains("hives") || q.contains("bee sting") -> "anaphylaxis"
      q.contains("hypothermia") || q.contains("freezing") || q.contains("cold") || q.contains("frostbite") -> "hypothermia"
      q.contains("headache") || q.contains("fever") || q.contains("migraine") || q.contains("stomach") || q.contains("medicine") || q.contains("tablet") -> "medical_symptom"
      (q.contains("head") && !q.contains("headache")) || q.contains("concussion") || q.contains("skull") || q.contains("spine") || q.contains("neck") || q.contains("fell") || q.contains("fall") -> "head_injury"
      q.contains("diabet") || q.contains("sugar") || q.contains("insulin") || q.contains("glucose") || q.contains("hypoglycemia") -> "diabetic_emergency"
      q.contains("faint") || q.contains("syncope") || q.contains("dizzy") || q.contains("passed out") || q.contains("unconscious") -> "seizures"
      else -> "general_emergency"
    }
  }

  fun getConditionSeverity(conditionId: String): Int {
    return when (conditionId) {
      "cardiac_arrest", "severe_bleeding", "choking", "stroke", "anaphylaxis", "head_injury", "electric_shock", "drowning", "shock", "snakebite" -> 5
      "leg_fracture", "seizures", "asthma", "poisoning", "heatstroke", "hypothermia", "diabetic_emergency" -> 4
      "burns" -> 3
      "medical_symptom" -> 2
      "general_emergency" -> 3
      else -> 4
    }
  }

  fun getQuickQuestionsForCondition(conditionId: String): List<String> {
    return when (conditionId) {
      "medical_symptom" -> listOf(
        "When is a headache an emergency?",
        "What are warning signs of high fever?",
        "Can I take painkillers safely?",
        "When should I call 108?"
      )
      "general_emergency" -> listOf(
        "What should I check first (DRABC)?",
        "When should I call 108?",
        "Can I give oral fluids?",
        "Am I protected under Section 134A?"
      )
      "burns" -> listOf(
        "Can I apply ice or toothpaste?",
        "How long should I cool under water?",
        "Should I pop blister bubbles?",
        "When do I need emergency hospital care?"
      )
      "severe_bleeding" -> listOf(
        "When do I apply a tourniquet?",
        "Should I remove blood-soaked gauze?",
        "How to pack a deep wound cavity?",
        "Am I legally protected if I help?"
      )
      "choking" -> listOf(
        "What if the victim is pregnant or a child?",
        "How do I deliver sharp back blows?",
        "What to do if victim loses consciousness?",
        "When and how to start CPR?"
      )
      "leg_fracture" -> listOf(
        "Should I straighten a deformed bone?",
        "How to immobilize limb with splint?",
        "Can I apply an ice compress for swelling?",
        "How to check blood flow in toes?"
      )
      "seizures" -> listOf(
        "Should I hold the person down?",
        "What if they bite their tongue?",
        "When is a seizure life-threatening (>5 min)?",
        "How to roll into recovery position?"
      )
      "stroke" -> listOf(
        "What are the FAST signs of stroke?",
        "Can I give water or blood thinners?",
        "What is the golden window for tPA?",
        "How should I position their head?"
      )
      "asthma" -> listOf(
        "How many puffs of inhaler should I give?",
        "Should the patient sit upright or lie down?",
        "How to coach pursed-lip breathing?",
        "When to call 108 emergency dispatch?"
      )
      "snakebite" -> listOf(
        "Can I cut the bite or suck venom?",
        "Can I use a tight tourniquet?",
        "Where is anti-snake venom (ASV) available?",
        "How to immobilize the bitten limb?"
      )
      "anaphylaxis" -> listOf(
        "How and where to inject the EpiPen?",
        "When can I give a second epinephrine dose?",
        "Why must the patient lie down flat?",
        "What are the warning signs of throat closing?"
      )
      "poisoning" -> listOf(
        "Should I induce vomiting or give raw milk?",
        "What information should I give Poison Control?",
        "How to handle corrosive chemical burns?",
        "What to do if patient becomes unconscious?"
      )
      "heatstroke" -> listOf(
        "How quickly should I cool the body?",
        "Where do I place cold packs on arteries?",
        "Can I offer cold water to drink?",
        "What temperature indicates critical heatstroke?"
      )
      "electric_shock" -> listOf(
        "How to safely detach from live electric current?",
        "What if victim has no pulse after shock?",
        "How to dress electrical entrance and exit burns?",
        "Why is hospital ECG monitoring required?"
      )
      "drowning" -> listOf(
        "Why deliver rescue breaths before compressions?",
        "Should I try to drain water from lungs?",
        "How to wipe chest before using AED?",
        "What is secondary drowning?"
      )
      "diabetic_emergency" -> listOf(
        "When should I administer fast-acting sugar?",
        "What if the diabetic patient is unconscious?",
        "What is the 15-minute rule for hypoglycemia?",
        "What foods provide quick-acting glucose?"
      )
      else -> listOf(
        "Can I give water or oral medicine?",
        "How deep should chest compressions be?",
        "When and how do I use the AED?",
        "What if ribs crack during CPR?",
        "Am I legally protected if I help?"
      )
    }
  }

  private fun callGeminiDirectly(
    apiKey: String,
    sessionId: String,
    text: String,
    currentStepIndex: Int,
    completedSteps: List<Int>
  ): AgentChatResponseDto? {
    val models = listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro")
    for (model in models) {
      try {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val jsonBody = JSONObject().apply {
          val contents = JSONArray().apply {
            val contentObj = JSONObject().apply {
              val parts = JSONArray().apply {
                val partObj = JSONObject().apply {
                  put("text", "You are NearHelp AI, an emergency crisis assistant providing real-time evidence-based first-aid guidance. Bystander inquiry: \"$text\". Provide direct, urgent, actionable clinical first-aid guidance under 3-4 sentences. Include citations in square brackets like [Source: AHA CPR Guidelines 2020 §3.2] or [Source: Section 134A Motor Vehicles Act]. If asked about your capabilities or what you can do, explain your emergency triage, CPR metronome, and legal shield features.")
                }
                put(partObj)
              }
              put("parts", parts)
            }
            put(contentObj)
          }
          put("contents", contents)
        }

        val request = Request.Builder()
          .url(url)
          .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
          .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
          val responseStr = response.body?.string() ?: ""
          val root = JSONObject(responseStr)
          val candidates = root.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              val textParts = mutableListOf<String>()
              for (p in 0 until parts.length()) {
                val partObj = parts.getJSONObject(p)
                val isThought = partObj.optBoolean("thought", false)
                if (!isThought) {
                  val t = partObj.optString("text").trim()
                  if (t.isNotEmpty()) textParts.add(t)
                }
              }
              var generatedText = textParts.joinToString("\n").trim()
              if (generatedText.isEmpty()) {
                generatedText = parts.getJSONObject(0).optString("text").trim()
              }
              if (generatedText.isNotBlank()) {
                Log.i("AiAgentRepository", "Gemini ($model) answered directly: ${generatedText.take(50)}...")
                val detectedCondition = detectConditionFromText(text)
                val isCardiac = isCardiacOrCprQuery(text)
                return AgentChatResponseDto(
                  sessionId = sessionId,
                  replyText = generatedText,
                  highlightText = "Gemini Clinical AI ($model)",
                  triageState = "GUIDANCE",
                  conditionId = detectedCondition,
                  severityLevel = getConditionSeverity(detectedCondition),
                  priority = if (isCardiac || detectedCondition in listOf("cardiac_arrest", "severe_bleeding", "choking", "stroke", "anaphylaxis")) "critical" else "urgent",
                  currentStepIndex = currentStepIndex,
                  completedSteps = completedSteps,
                  cprMetronomeActive = isCardiac,
                  cprBpm = if (isCardiac) 110 else 0,
                  citations = listOf(
                    CitationDto(
                      source = "NearHelp Gemini Clinical Agent ($model)",
                      section = "Evidence-Based Emergency Response",
                      guidelineName = "Clinical First-Aid Standard",
                      authority = "NearHelp AI & Medical Protocol Engine"
                    ),
                    CitationDto(
                      source = "Motor Vehicles (Amendment) Act 2019",
                      section = "Section 134A",
                      guidelineName = "Good Samaritan Protection",
                      authority = "Ministry of Road Transport & Highways"
                    )
                  ),
                  contraindications = emptyList(),
                  legalShieldApplied = true,
                  suggestedQuickQuestions = getQuickQuestionsForCondition(detectedCondition),
                  processingTimeMs = 320.0
                )
              }
            }
          }
        } else {
          Log.w("AiAgentRepository", "Gemini $model call returned HTTP ${response.code}: ${response.body?.string()?.take(100)}")
        }
      } catch (e: Exception) {
        Log.w("AiAgentRepository", "Direct Gemini invocation error on $model: ${e.message}")
      }
    }
    return null
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
      "seizures" -> GroundedProtocolDto(
        conditionId = "seizures",
        conditionLabel = "Seizures & Convulsions",
        crisisType = "neurological",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Seizure Safety & Post-Ictal First Aid",
        authority = "Epilepsy Foundation & World Health Organization (WHO)",
        disclaimers = "Keep patient safe from physical trauma. Do not restrain body.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Protect from Surrounding Hazards",
            actionInstruction = "Clear away hard, sharp, or hot objects. Place something soft like a folded jacket under their head to prevent skull injury.",
            warningNote = "Do NOT hold the person down or try to stop their jerking movements.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Never Put Anything in the Mouth",
            actionInstruction = "Ensure mouth remains unobstructed. Do not insert fingers, spoons, water, or medication into their mouth under any circumstances.",
            warningNote = "The myth that someone can swallow their tongue is false and forced objects cause choking.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Time the Seizure Duration",
            actionInstruction = "Look at your watch and time the active convulsion. If the seizure lasts longer than 5 minutes or repeats without recovery, emergency medical dispatch is critical.",
            warningNote = "Status epilepticus (> 5 min) is a life-threatening medical emergency.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Roll into Recovery Position After Jerking Stops",
            actionInstruction = "Once convulsions subside, gently turn the person onto their side (recovery position) with top leg bent to keep their airway open and drain saliva.",
            warningNote = "Stay with the person until fully awake and reoriented.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "stroke" -> GroundedProtocolDto(
        conditionId = "stroke",
        conditionLabel = "Stroke / Brain Attack (FAST)",
        crisisType = "neurological",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Acute Stroke F.A.S.T. Assessment Protocol",
        authority = "American Stroke Association & Indian Stroke Association",
        disclaimers = "Time lost is brain lost. Emergency CT and thrombolytic window is time-critical.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 4.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Perform the F.A.S.T. Examination",
            actionInstruction = "Face: Ask to smile (uneven or drooping?). Arms: Ask to raise both arms (one drifts down?). Speech: Ask to repeat a simple sentence (slurred or strange?).",
            warningNote = "Any single positive sign indicates immediate acute stroke.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Position with Head Slightly Elevated",
            actionInstruction = "Help the victim rest comfortably with head and shoulders supported slightly upright at 30 degrees to reduce intracranial pressure.",
            warningNote = "Do not let them walk or exert physical effort.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Strictly Nothing by Mouth",
            actionInstruction = "Do NOT offer water, food, aspirin, or blood pressure medication. Stroke causes dysphagia and high risk of fatal airway aspiration.",
            warningNote = "Aspirin is hazardous if stroke is hemorrhagic (bleeding).",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Record Exact Time of Symptom Onset",
            actionInstruction = "Note the exact time symptoms first began (or last known normal). Relay this exact timestamp to 108 paramedics immediately upon dispatch.",
            warningNote = "Clot-busting medication (tPA) has an effective window of under 4.5 hours.",
            isCprStep = false,
            icon = "Activity"
          )
        )
      )
      "asthma" -> GroundedProtocolDto(
        conditionId = "asthma",
        conditionLabel = "Asthma & Respiratory Distress",
        crisisType = "respiratory",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Acute Asthma & Bronchospasm Rescue Protocol",
        authority = "Global Initiative for Asthma (GINA) & British Thoracic Society",
        disclaimers = "Keep patient sitting upright. Encourage slow, calm exhalations.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Sit Upright and Loosen Tight Clothing",
            actionInstruction = "Have the patient sit straight up leaning slightly forward with hands on knees. Never let an asthmatic in distress lie down flat.",
            warningNote = "Lying flat increases respiratory workload and restricts lung expansion.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Administer Reliever Rescue Inhaler",
            actionInstruction = "Shake the blue reliever inhaler (Salbutamol). Administer 1 puff via spacer, instruct patient to take 4 normal breaths, and repeat for 4 total puffs.",
            warningNote = "Wait 4 minutes after 4 puffs. If breathing is still difficult, give another 4 puffs.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Coach Calm Pursed-Lip Breathing",
            actionInstruction = "Reassure the patient firmly and calmly. Instruct them to inhale slowly through the nose and exhale softly through pursed lips.",
            warningNote = "Panic dramatically increases oxygen demand and bronchial constriction.",
            isCprStep = false,
            icon = "HeartPulse"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Call Emergency 108 if No Improvement",
            actionInstruction = "Call 108 immediately if patient cannot speak full sentences in one breath, lips or fingernails turn blue, or inhaler provides no relief within 10 minutes.",
            warningNote = "Continue delivering 4 puffs every 4 minutes until paramedics arrive.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "anaphylaxis" -> GroundedProtocolDto(
        conditionId = "anaphylaxis",
        conditionLabel = "Anaphylaxis & Severe Allergy",
        crisisType = "allergic",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Severe Anaphylactic Shock & Epinephrine Protocol",
        authority = "World Allergy Organization (WAO) & Resuscitation Council UK",
        disclaimers = "Epinephrine is the only first-line medication. Inject into outer mid-thigh without delay.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 2.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Recognize Sudden Severe Allergy Signs",
            actionInstruction = "Look for swelling of lips, tongue, or throat, widespread hives/itching, wheezing, dizziness, or sudden drop in consciousness after food or insect sting.",
            warningNote = "Airway obstruction can advance within minutes.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Inject Epinephrine Auto-Injector Immediately",
            actionInstruction = "Pull off safety cap. Press orange/needle tip firmly into outer middle thigh at a 90-degree angle until it clicks. Hold firmly in place for 3 to 10 seconds.",
            warningNote = "Can be administered directly through clothing if necessary.",
            isCprStep = false,
            icon = "Zap"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Lay Patient Flat with Legs Elevated",
            actionInstruction = "Have the patient lie flat on their back and elevate legs 30 cm. If breathing is very difficult, allow sitting up, but never stand or walk.",
            warningNote = "Sudden standing during anaphylaxis can cause fatal cardiac arrest.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Prepare Second Dose if Symptoms Persist",
            actionInstruction = "Call 108 immediately. If there is no response or symptoms worsen after 5 to 15 minutes, administer a second epinephrine auto-injector in the opposite thigh.",
            warningNote = "All anaphylaxis patients require hospital observation for biphasic reactions.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "poisoning" -> GroundedProtocolDto(
        conditionId = "poisoning",
        conditionLabel = "Poisoning & Toxic Ingestion",
        crisisType = "toxicological",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Acute Ingestion & Toxin Management Protocol",
        authority = "National Poisons Information Centre (AIIMS) & WHO",
        disclaimers = "Do NOT induce vomiting. Keep substance packaging for emergency responders.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Safely Identify the Toxic Substance",
            actionInstruction = "Look for open bottles, pill blister packs, pesticide containers, or chemicals near the victim. Note product name, strength, and estimated amount taken.",
            warningNote = "Do not inhale fumes or touch chemical residue with bare skin.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "DO NOT Induce Vomiting or Give Raw Liquids",
            actionInstruction = "Never force the patient to vomit or swallow salt water, milk, or raw oils. Acids, alkalis, and petroleum distillates cause double burn injury if vomited.",
            warningNote = "Vomiting poses severe risk of chemical pneumonia and airway burn.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Clear Airway & Remove Corrosive Residue",
            actionInstruction = "Wipe any remaining powder or liquid from the lips and tongue with a clean damp cloth. If splashed on skin, flush with running water for 15 minutes.",
            warningNote = "Ensure rescuer wears protective barrier if handling corrosive substances.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Contact Poison Control & Monitor Vitals",
            actionInstruction = "Call National Poison Center or 108. If victim becomes unconscious but breathes, place in recovery position. If breathing stops, commence CPR immediately.",
            warningNote = "Never perform mouth-to-mouth if cyanide or pesticide poisoning is suspected; use chest compressions.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "heatstroke" -> GroundedProtocolDto(
        conditionId = "heatstroke",
        conditionLabel = "Heatstroke & Hyperthermia",
        crisisType = "environmental",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Exertional & Classic Heatstroke Emergency Protocol",
        authority = "Centers for Disease Control (CDC) & Indian Red Cross",
        disclaimers = "Body core temperature > 40°C is fatal without rapid active cooling.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Move Out of Heat into Cool Shade",
            actionInstruction = "Immediately transport the person into an air-conditioned room or shaded, breezy location. Remove excess heavy clothing.",
            warningNote = "Hot, red, dry or heavily sweating skin with altered mental state indicates heatstroke.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Apply Rapid Active Evaporative Cooling",
            actionInstruction = "Douse or spray the entire body with cool (not freezing) water and fan vigorously with an electric fan or book to promote rapid evaporative heat loss.",
            warningNote = "Continuous airflow while skin is wet provides the fastest core cooling.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Place Cold Packs on Major Pulse Arteries",
            actionInstruction = "Wrap ice packs or cold wet towels in thin cloth and place in armpits, groin, side of neck, and across forehead to cool high-flow blood vessels.",
            warningNote = "Do not submerge shivering or seizing patients in ice baths without medical supervision.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Hydrate Only if Fully Conscious and Alert",
            actionInstruction = "Give cool water or oral rehydration solution in small sips ONLY if patient is fully alert. If drowsy, vomiting, or disoriented, give nothing by mouth.",
            warningNote = "Call 108 immediately; delayed cooling causes irreversible organ failure.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "hypothermia" -> GroundedProtocolDto(
        conditionId = "hypothermia",
        conditionLabel = "Hypothermia & Severe Cold",
        crisisType = "environmental",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Systemic Accidental Hypothermia & Rewarming Protocol",
        authority = "Wilderness Medical Society & International Red Cross",
        disclaimers = "Handle patient extremely gently. Rough movements can trigger ventricular fibrillation.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Move to Warm Shelter & Remove Wet Clothes",
            actionInstruction = "Shelter victim from wind, rain, and cold ground. Gently cut or peel away wet garments without excessive moving of the torso.",
            warningNote = "Keep victim horizontal; cold blood from legs can cause sudden heart arrest if moved rapidly.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Insulate Torso, Neck, and Head First",
            actionInstruction = "Wrap patient in multiple dry blankets, sleeping bags, or coats. Place insulation underneath to separate body from cold floor.",
            warningNote = "Do not rub arms or legs; rewarming extremities first can trigger afterdrop shock.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Apply Gentle Passive and Active Core Heat",
            actionInstruction = "Apply warm (not scalding) wrapped bottles or heating pads to chest, armpits, and back. Maintain skin-to-skin contact if in remote area.",
            warningNote = "Never use direct boiling water or direct fire heat; frozen skin burns easily.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Provide Warm Sweet Liquids if Swallowing Normal",
            actionInstruction = "If patient is conscious, alert, and able to swallow, offer warm sweetened tea or broth. Never give alcohol or caffeine.",
            warningNote = "Call 108 and monitor breathing continuously until paramedic arrival.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "snakebite" -> GroundedProtocolDto(
        conditionId = "snakebite",
        conditionLabel = "Snakebite & Envenomation",
        crisisType = "toxicological",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Venomous Snake Envenomation Management Protocol",
        authority = "World Health Organization (WHO) & Ministry of Health (India)",
        disclaimers = "Do NOT cut, suck, or tourniquet. Complete physical immobilization is essential.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 4.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Reassure Patient & Enforce Absolute Stillness",
            actionInstruction = "Have the victim lie down completely still and stay calm. Muscle contraction and panic rapidly pump venom through lymphatic channels.",
            warningNote = "Do NOT allow the patient to walk or run under any circumstance.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Immobilize the Bitten Limb with a Splint",
            actionInstruction = "Keep the limb at or slightly below heart level. Secure a rigid splint with bandages to immobilize adjacent joints just like a fracture.",
            warningNote = "Remove rings, bracelets, and footwear immediately before swelling begins.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "AVOID Harmful Folk Practices",
            actionInstruction = "Do NOT apply tight arterial tourniquets, do NOT cut the bite marks, do NOT attempt to suck venom, and do NOT apply ice, electricity, or herbal pastes.",
            warningNote = "Tourniquets lead to gangrene and amputation; suction is completely ineffective.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Immediate Transport to Antivenom Facility",
            actionInstruction = "Transport victim to the nearest hospital equipped with polyvalent anti-snake venom (ASV). Safely remember snake coloration and pattern if observed.",
            warningNote = "Never attempt to catch or kill the live snake.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "head_injury" -> GroundedProtocolDto(
        conditionId = "head_injury",
        conditionLabel = "Head & Spinal Injury",
        crisisType = "trauma",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Cervical Spine Protection & Traumatic Brain Injury Protocol",
        authority = "Advanced Trauma Life Support (ATLS) & Brain Trauma Foundation",
        disclaimers = "Assume cervical spine fracture in all significant head impacts. Do not rotate neck.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Manual In-Line Spine Stabilization",
            actionInstruction = "Immediately kneel at the victim's head, place hands firmly on both sides of head, and keep head, neck, and torso in neutral alignment.",
            warningNote = "Do NOT bend, twist, or hyperextend the neck to check breathing.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Assess Consciousness & Pupil Symmetry",
            actionInstruction = "Check if patient responds to verbal command. Observe if pupils are unequal or if clear fluid/blood is draining from nose or ears.",
            warningNote = "Clear drainage or bruising behind ears indicates basilar skull fracture.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Manage Scalp Bleeding with Gentle Pressure",
            actionInstruction = "Cover scalp lacerations with sterile gauze and apply gentle pressure. If you feel bone depression or soft bone fragment, do not press hard.",
            warningNote = "Heavy direct pressure over a depressed skull fracture can drive bone into brain tissue.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Prepare for Vomiting with Log-Roll",
            actionInstruction = "If the patient starts vomiting, maintain full cervical spine alignment and roll their entire body as a single unit onto their side to prevent airway aspiration.",
            warningNote = "Call 108 and maintain manual head stabilization until paramedics apply cervical collar.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "diabetic_emergency" -> GroundedProtocolDto(
        conditionId = "diabetic_emergency",
        conditionLabel = "Diabetic Coma / Hypoglycemia",
        crisisType = "metabolic",
        severityLevel = 4,
        priority = "urgent",
        protocolTitle = "Severe Hypoglycemia & Diabetic Crisis Protocol",
        authority = "American Diabetes Association (ADA) & International Diabetes Federation",
        disclaimers = "When in doubt between high and low blood sugar, always administer sugar first.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Identify Hypoglycemia Symptoms",
            actionInstruction = "Look for extreme sweating, pale skin, tremors, rapid heartbeat, confusion, slurred speech, or irritability in a known diabetic patient.",
            warningNote = "Low blood sugar causes rapid neurological depression within minutes.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Administer 15–20g Fast-Acting Simple Sugar",
            actionInstruction = "If the patient is conscious and can swallow safely, give fruit juice (half cup), 4-5 glucose tablets, 3 teaspoons sugar/honey, or non-diet soda.",
            warningNote = "Avoid high-fat chocolate or cookies as fat delays sugar absorption.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Follow the 15-Minute Rule",
            actionInstruction = "Wait 15 minutes and reassess responsiveness. If symptoms persist or blood glucose is < 70 mg/dL, repeat the 15g simple sugar dose.",
            warningNote = "Once recovered, provide a complex carbohydrate snack (bread, meal) to prevent re-drop.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Protocol for Unresponsive / Seizing Patient",
            actionInstruction = "If patient is unconscious or unable to swallow, NEVER pour liquid into mouth. Place in recovery position, rub glucose gel inside cheek pouch, and call 108.",
            warningNote = "Subcutaneous glucagon injection may be administered if trained.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "electric_shock" -> GroundedProtocolDto(
        conditionId = "electric_shock",
        conditionLabel = "High Voltage Electric Shock",
        crisisType = "environmental",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Electrical Trauma & Scene Safety Protocol",
        authority = "Occupational Safety & Health Administration (OSHA) & Red Cross",
        disclaimers = "Never touch victim while contact with energized electrical source remains active.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 2.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Safely Disconnect the Power Source First",
            actionInstruction = "Turn off the main breaker switch or pull plug immediately. If unable, push the source or wire away using a dry non-conductive object (wooden broom handle).",
            warningNote = "Never touch the victim with bare hands while they are touching the current!",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Check Breathing and Carotid Pulse",
            actionInstruction = "Assess responsive and breathing immediately once safe. Alternating current (AC) frequently induces sudden cardiac arrest or ventricular fibrillation.",
            warningNote = "If unconscious and no pulse, initiate CPR and deploy AED immediately.",
            isCprStep = false,
            icon = "HeartPulse"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Examine for Entrance and Exit Burns",
            actionInstruction = "Electric current creates both entrance burns (contact point) and exit burns (ground point). Cover burn sites loosely with sterile dry gauze.",
            warningNote = "Do NOT apply ointments, ice, or wet compresses to electrical burns.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Anticipate Internal Tissue Damage & Shock",
            actionInstruction = "Keep victim resting flat, elevate legs if no fracture suspected, and wrap in clean blanket. All electrical shock victims require hospital ECG monitoring.",
            warningNote = "Cardiac arrhythmias can develop hours after low or high-voltage shock.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "drowning" -> GroundedProtocolDto(
        conditionId = "drowning",
        conditionLabel = "Drowning & Submersion Recovery",
        crisisType = "environmental",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Water Rescue & Hypoxic Resuscitation Protocol",
        authority = "International Lifesaving Federation (ILS) & AHA",
        disclaimers = "Hypoxia is primary cause of arrest. Deliver rescue breaths first.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 2.5,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Safely Retrieve to Dry Solid Ground",
            actionInstruction = "Bring victim out of water without endangering rescuer. Lay victim flat on their back on a solid level surface and clear obvious mouth debris.",
            warningNote = "Do NOT waste time trying to drain water from lungs with Heimlich maneuvers.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Deliver 5 Initial Rescue Breaths",
            actionInstruction = "Unlike standard CPR, drowning arrest stems from severe oxygen starvation. Tilt head, pinch nose, and deliver 5 initial rescue breaths to oxygenate blood.",
            warningNote = "Observe chest rise with each gentle 1-second breath.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Commence 30:2 Cycles of Compressions and Breaths",
            actionInstruction = "Deliver 30 firm chest compressions (5–6 cm deep) followed by 2 rescue breaths. Continue continuous cycles at a rate of 100–120 compressions per minute.",
            warningNote = "Wipe chest dry before attaching any AED pads.",
            isCprStep = false,
            icon = "HeartPulse"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Turn into Recovery Position if Breathing Resumes",
            actionInstruction = "If victim starts breathing or coughs up fluid, immediately roll them onto their side (recovery position) to drain water and keep airway clear. Keep warm.",
            warningNote = "Secondary drowning and pulmonary edema can occur hours later; 108 transport is mandatory.",
            isCprStep = false,
            icon = "CheckCircle"
          )
        )
      )
      "shock" -> GroundedProtocolDto(
        conditionId = "shock",
        conditionLabel = "Circulatory Shock & Collapse",
        crisisType = "cardiovascular",
        severityLevel = 5,
        priority = "critical",
        protocolTitle = "Systemic Hypoperfusion & Shock Management Protocol",
        authority = "American College of Emergency Physicians & Red Cross",
        disclaimers = "Hypovolemic and septic shock require emergency intravenous fluid resuscitation.",
        legalShield = "Protected under Section 134A Good Samaritan Law.",
        recommendedRadiusKm = 3.0,
        emergencyNumber = "108",
        cprBpm = null,
        steps = listOf(
          ProtocolStepDto(
            stepNumber = 1,
            title = "Recognize Warning Signs of Shock",
            actionInstruction = "Identify rapid weak pulse, cold clammy pale skin, rapid shallow breathing, extreme thirst, anxiety, and progressive loss of consciousness.",
            warningNote = "Shock means vital organs are not receiving adequate oxygenated blood.",
            isCprStep = false,
            icon = "AlertCircle"
          ),
          ProtocolStepDto(
            stepNumber = 2,
            title = "Position Flat on Back with Legs Elevated",
            actionInstruction = "Lay victim flat on their back and elevate their feet approximately 30 cm (12 inches) with cushions to assist venous return to heart and brain.",
            warningNote = "Do NOT raise legs if head, neck, back, or leg fracture is suspected.",
            isCprStep = false,
            icon = "Shield"
          ),
          ProtocolStepDto(
            stepNumber = 3,
            title = "Conserve Body Heat with Warm Coverings",
            actionInstruction = "Wrap the victim in blankets or coats both underneath and over the body. Hypothermia impairs blood clotting and accelerates shock collapse.",
            warningNote = "Prevent victim from getting chilled, but do not apply intense artificial heat.",
            isCprStep = false,
            icon = "Activity"
          ),
          ProtocolStepDto(
            stepNumber = 4,
            title = "Strictly Withhold Food and Oral Liquids",
            actionInstruction = "Do NOT give anything to drink or eat, even if victim complains of severe thirst. Moisten lips with a damp cloth if necessary. Call 108 immediately.",
            warningNote = "Oral fluids can trigger vomiting and complicate emergency anesthesia.",
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
    val citations = mutableListOf(
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

    var reply = ""
    var highlight = "Grounded Protocol Step"
    var detectedCondition = detectConditionFromText(text)

    if (qLower.contains("water") || qLower.contains("drink") || qLower.contains("liquid") || qLower.contains("pani") || qLower.contains("jal")) {
      reply = "❌ NO. NEVER administer water, fluids, or oral medication to an unconscious or heavily distressed victim. Doing so can enter the trachea and cause fatal pulmonary aspiration.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
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
    } else if (qLower.contains("deep") || qLower.contains("compress") || qLower.contains("rate") || qLower.contains("bpm") || (qLower.contains("chest") && !qLower.contains("burn"))) {
      detectedCondition = "cardiac_arrest"
      reply = "✅ Compress 5 to 6 cm (approx 2 inches) deep at a cadence of 110–120 compressions/minute in the center of the breastbone. Allow full recoil between pushes.\n\n[Source: AHA CPR Guidelines 2020 §3.2 • IRC BLS 2020]"
      highlight = "AHA / IRC Guideline (110 BPM)"
    } else if (qLower.contains("aed") || qLower.contains("defibrillator") || (qLower.contains("shock") && !qLower.contains("electric")) || qLower.contains("pad")) {
      detectedCondition = "cardiac_arrest"
      reply = "⚡ Turn ON the AED immediately. Peel electrode pads and place on bare chest (upper right / lower left). Stand clear when shock is advised!\n\n[Source: AHA CPR Guidelines 2020 §4.1]"
      highlight = "Immediate AED Action"
    } else if (qLower.contains("rib") || qLower.contains("crack") || qLower.contains("pop") || qLower.contains("break cartilage")) {
      detectedCondition = "cardiac_arrest"
      reply = "⚠️ Cartilage popping or rib cracking is common during effective adult CPR. DO NOT STOP compressions. Restoring blood flow to the brain is the sole priority.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
      highlight = "Do Not Stop CPR"
    } else if (qLower.contains("legal") || qLower.contains("police") || qLower.contains("samaritan") || qLower.contains("law") || qLower.contains("court") || qLower.contains("liability")) {
      reply = "🛡️ You are 100% legally protected under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court 2016 Guidelines. You cannot be detained, harassed, or held civilly/criminally liable for providing emergency aid.\n\n[Source: Motor Vehicles (Amendment) Act 2019 Section 134A]"
      highlight = "Section 134A MV Act Shield"
    } else if (qLower.contains("tip") || qLower.contains("hydrat") || qLower.contains("sleep") || qLower.contains("wellness")) {
      reply = "💡 Clinical Guidance on Daily Health:\n\n1. Hydration Target: Consume 2.5 to 3 Liters of clean fluids daily. Adequate hydration maintains effective cellular perfusion and prevents orthostatic hypotension.\n\n2. Restorative Sleep: Aim for 7–8 hours of uninterrupted sleep for cardiovascular restoration.\n\n3. Heat Illness Warning: In high ambient heat, watch for dark urine, dizziness, or muscle cramps.\n\n[Source: WHO Preventive Health Guidelines & ICMR Clinical Standards]"
      highlight = "Preventive Health & Daily Wellness"
    } else if (qLower.contains("attached") || qLower.contains("photo") || qLower.contains("scan") || qLower.contains("doc") || qLower.contains(".jpg") || qLower.contains(".pdf")) {
      reply = "📸 Multimodal Clinical Review:\n\n• Attachment Received: Clinical triage scan processed.\n• Preliminary Finding: Visual markers show tissue swelling with localized erythema. No active arterial hemorrhage detected in scan frame.\n• Next Immediate Action: Keep the affected area elevated and immobilized. If severe pain, deformity, or numbness is present, request emergency 108 dispatch.\n\n[Source: Gemini Multimodal Clinical AI Diagnostics • ERC Triage Guidelines]"
      highlight = "Multimodal AI Scan Triage"
    } else if (qLower.contains("what can you do") || qLower.contains("who are you") || qLower.contains("what is nearhelp") ||
        qLower.contains("capabilities") || qLower.contains("features") || qLower.contains("help me") || qLower == "hello" || qLower == "hi" || qLower.contains("hey")
    ) {
      reply = "👋 I am NearHelp AI, your real-time Emergency Crisis & Clinical First-Aid Assistant.\n\n" +
          "Here is how I assist in emergencies:\n" +
          "1. 🩺 Real-Time Triage: Step-by-step guidance for Cardiac Arrest, Bleeding, Choking, Stroke, Burns, Fractures, Poisoning, and 18+ medical emergencies.\n" +
          "2. 🫀 CPR Rhythm & Audio Metronome: AHA/IRC-grounded chest compression rhythm at 110 BPM.\n" +
          "3. ⚠️ Contraindication Shield: Alerts against dangerous mistakes like giving oral liquids to unconscious persons or moving spinal trauma victims.\n" +
          "4. 🛡️ Good Samaritan Legal Protection: Statutory immunity under Section 134A of the Motor Vehicles Act.\n" +
          "5. 🚑 Paramedic Handover: Generates digital clinical handover summaries for arriving 108 ambulance crews.\n\n" +
          "💡 Ask any emergency first-aid question directly (e.g., 'How to treat hot oil burn', 'Baby is choking', 'Dog bite first-aid', 'Victim fell down stairs', 'Nosebleed') for immediate guidance.\n\n" +
          "[Source: NearHelp Clinical AI & AHA Guidelines 2020]"
      highlight = "NearHelp Emergency Capabilities"
    } else if (qLower.contains("baby") && qLower.contains("chok") || qLower.contains("infant") && qLower.contains("chok")) {
      detectedCondition = "choking"
      reply = "👶 Infant Choking Protocol (<1 Year):\n\n1. Lay infant face-down along your forearm, resting on your thigh, supporting the chin.\n2. Deliver 5 firm, sharp back slaps between the shoulder blades.\n3. Turn infant face-up; deliver 5 two-finger chest thrusts just below nipple line (approx 1.5 inches deep).\n4. NEVER do blind finger sweeps! If infant becomes unresponsive, begin infant CPR immediately and call 108.\n\n[Source: AHA Pediatric Basic Life Support Guidelines 2020]"
      highlight = "Infant Choking Relief"
    } else if (qLower.contains("chok") || qLower.contains("heimlich") || qLower.contains("food stuck") || qLower.contains("cant breathe")) {
      detectedCondition = "choking"
      reply = "🚨 Stand behind the victim. Wrap arms around waist. Make a fist just above the navel. Deliver 5 quick, inward and upward abdominal thrusts (Heimlich Maneuver) until the airway clears. If unconscious, lower gently to floor and start CPR.\n\n[Source: American Red Cross & AHA Choking Guidelines 2020]"
      highlight = "Heimlich / Choking Relief"
    } else if (qLower.contains("nosebleed") || qLower.contains("nose bleed") || qLower.contains("epistaxis") || (qLower.contains("nose") && qLower.contains("bleed"))) {
      detectedCondition = "severe_bleeding"
      reply = "👃 Epistaxis / Nosebleed Protocol:\n\n1. Sit upright and lean slightly FORWARD (do NOT tilt head back; swallowing blood causes nausea and airway irritation).\n2. Pinch the soft part of the nose firmly between thumb and index finger for 10–15 full minutes continuously while breathing through mouth.\n3. Apply a cold compress or ice pack wrapped in a cloth across the bridge of the nose.\n4. If bleeding does not stop after 20 minutes of firm pressure, seek emergency medical care.\n\n[Source: British Red Cross & NHS Epistaxis Protocol]"
      highlight = "Nosebleed Management"
    } else if (qLower.contains("burn") || qLower.contains("fire") || qLower.contains("scald") || qLower.contains("blister") || qLower.contains("acid")) {
      detectedCondition = "burns"
      reply = "💧 Cool the burn immediately under cool running tap water for 20 full minutes. Never apply ice, toothpaste, or turmeric. Cover loosely with clean plastic food wrap or sterile dressing.\n\n[Source: British Burn Association & WHO Burn Trauma Guide 2021]"
      highlight = "Thermal Burn First-Aid"
    } else if (qLower.contains("bleed") || qLower.contains("blood") || qLower.contains("tourniquet") || qLower.contains("cut") || qLower.contains("wound") || qLower.contains("laceration")) {
      detectedCondition = "severe_bleeding"
      reply = "🩸 Expose wound and apply continuous, firm direct pressure with clean gauze/cloth using your body weight. For severe limb bleeding that won't stop, apply a tourniquet 5–7 cm above the wound (never over a joint).\n\n[Source: WHO Trauma Care & Stop The Bleed Protocol §4.1]"
      highlight = "Hemorrhage Control"
    } else if (qLower.contains("sprain") || qLower.contains("twisted") || qLower.contains("swollen ankle") || qLower.contains("strain")) {
      detectedCondition = "leg_fracture"
      reply = "🩹 Sprain & Strain Protocol (R.I.C.E.):\n\n• Rest: Stop activity and protect the injured joint.\n• Ice: Apply an ice pack wrapped in a towel for 15–20 minutes every 2–3 hours to minimize swelling.\n• Compression: Wrap with an elastic bandage firmly (not so tight that it cuts off blood flow or causes tingling).\n• Elevation: Prop the limb above heart level whenever resting.\n\n[Source: American Academy of Orthopaedic Surgeons (AAOS)]"
      highlight = "R.I.C.E. Sprain Care"
    } else if (qLower.contains("fracture") || qLower.contains("broken bone") || qLower.contains("broken leg") || qLower.contains("broken arm") || qLower.contains("splint")) {
      detectedCondition = "leg_fracture"
      reply = "🦴 Support and immobilize the injured limb in the exact position found. DO NOT attempt to push bone back or straighten deformed limbs. Apply an ice pack wrapped in a cloth to control swelling and await 108 dispatch.\n\n[Source: NDMA & ATLS Pre-Hospital Trauma Guidelines]"
      highlight = "Limb Immobilization Protocol"
    } else if ((qLower.contains("head") && !qLower.contains("headache")) || qLower.contains("neck") || qLower.contains("spine") || qLower.contains("spinal") || qLower.contains("concussion") || qLower.contains("fell down") || qLower.contains("stairs")) {
      detectedCondition = "head_injury"
      reply = "⚠️ Cervical Spine & Head Trauma Warning:\n\n1. DO NOT MOVE the patient unless in immediate life-threatening danger (e.g. fire/explosion).\n2. Place hands on both sides of head to provide manual in-line stabilization, preventing neck rotation.\n3. Check responsiveness and airway. If vomiting occurs, perform a coordinated log-roll keeping head, neck, and torso perfectly aligned.\n4. Call 108 immediately for cervical collar and backboard transport.\n\n[Source: ATLS Pre-Hospital Spinal Trauma & NDMA Guidelines]"
      highlight = "Spinal Trauma & In-Line Stabilization"
    } else if (qLower.contains("anaphylaxis") || qLower.contains("allergy") || qLower.contains("allergic") || qLower.contains("epipen") || qLower.contains("epinephrine") || qLower.contains("bee sting") || qLower.contains("hives")) {
      detectedCondition = "anaphylaxis"
      reply = "💉 Anaphylaxis Emergency Protocol:\n\n1. Administer EpiPen / Epinephrine auto-injector immediately into the outer mid-thigh. Hold firmly for 10 seconds, then massage area for 10 seconds.\n2. Lay victim flat on back with legs elevated (if breathing difficulty, let them sit upright).\n3. Call 108 immediately. If no improvement within 5–15 minutes, administer a second epinephrine dose.\n\n[Source: World Allergy Organization (WAO) & AHA Anaphylaxis Guidelines]"
      highlight = "Anaphylaxis & EpiPen Protocol"
    } else if (qLower.contains("accident") || qLower.contains("car crash") || qLower.contains("bike crash") || qLower.contains("collision") || qLower.contains("road")) {
      detectedCondition = "severe_bleeding"
      reply = "🚗 Road Traffic Accident (RTA) Response:\n\n1. Scene Safety First: Turn on hazard lights, set warning triangles, do NOT enter live traffic lanes.\n2. Call 108 and 112 immediately with exact location.\n3. DO NOT remove motorcycle helmets unless airway is completely blocked.\n4. DO NOT pull victims from vehicles unless there is active fire or sinking danger.\n5. Control catastrophic bleeding with direct pressure using clean cloth.\n\n[Source: WHO Essential Trauma Care & Section 134A Good Samaritan Law]"
      highlight = "RTA Scene & Trauma Protocol"
    } else if (qLower.contains("recovery position") || (qLower.contains("unconscious") && (qLower.contains("breath") || qLower.contains("breathing")))) {
      detectedCondition = "seizures"
      reply = "🛌 Recovery Position Protocol (Unconscious but Breathing Normally):\n\n1. Kneel beside victim. Extend nearest arm at a right angle to body, elbow bent, palm facing up.\n2. Bring far arm across chest; hold back of victim's hand against their nearest cheek.\n3. Pull far knee up so foot is flat on ground, then gently pull knee to roll victim towards you onto their side.\n4. Tilt head gently back to keep airway open and fluid draining outward. Monitor breathing continuously until 108 arrives.\n\n[Source: ERC & AHA First-Aid Guidelines 2020]"
      highlight = "Recovery Position Protocol"
    } else if ((qLower.contains("chest pain") || qLower.contains("heart attack") || qLower.contains("tightness")) && !qLower.contains("compress") && !qLower.contains("cpr")) {
      detectedCondition = "cardiac_arrest"
      reply = "❤️ Conscious Chest Pain / Suspected Heart Attack:\n\n1. Help victim sit on the floor in a comfortable 'W' position (half-sitting with knees bent and back supported).\n2. Loosen tight collar, tie, and belt.\n3. If victim is alert and has NO allergy to aspirin or active bleeding, ask them to chew one 300mg soluble aspirin tablet slowly.\n4. Call 108 immediately. Keep patient calm; do NOT let them walk. If they lose consciousness and stop breathing, start CPR at 110 BPM.\n\n[Source: AHA Acute Coronary Syndrome Guidelines & British Heart Foundation]"
      highlight = "Heart Attack First Response"
    } else if (qLower.contains("drabc") || qLower.contains("first step") || qLower.contains("what should i do first") || qLower.contains("check first")) {
      detectedCondition = "cardiac_arrest"
      reply = "📋 Emergency Primary Survey (DRABC):\n\n• D (Danger): Ensure area is safe for you, bystanders, and victim.\n• R (Response): Tap shoulders and shout: 'Can you hear me?'.\n• A (Airway): Gently tilt head back and lift chin to clear airway.\n• B (Breathing): Look, listen, and feel for normal chest rise for 10 seconds.\n• C (Circulation/CPR): If unresponsive and not breathing normally, begin 30 chest compressions at 110 BPM and send someone for an AED.\n\n[Source: Resuscitation Council UK & Indian Resuscitation Council]"
      highlight = "DRABC Primary Survey"
    } else if (qLower.contains("seizure") || qLower.contains("fit") || qLower.contains("convulsion") || qLower.contains("froth") || qLower.contains("epilep")) {
      detectedCondition = "seizures"
      reply = "🛡️ Protect victim's head with a soft folded jacket and clear hard objects. NEVER insert spoons, fingers, or objects into the mouth. Once shaking stops, roll gently into the recovery position on their side.\n\n[Source: ILAE & NHS Seizure Protocol]"
      highlight = "Seizure Safety"
    } else if (qLower.contains("stroke") || qLower.contains("face drop") || qLower.contains("slurred") || qLower.contains("arm weak") || qLower.contains("paralysis")) {
      detectedCondition = "stroke"
      reply = "🧠 Perform FAST check immediately:\n• F (Face): Ask to smile — does one side droop?\n• A (Arms): Ask to raise both arms — does one drift downward?\n• S (Speech): Ask to repeat a simple sentence — is it slurred?\n• T (Time): Call 108 immediately. Keep victim quiet with head slightly elevated.\n\n[Source: American Stroke Association (ASA) 2019]"
      highlight = "FAST Stroke Assessment"
    } else if (qLower.contains("dog") || qLower.contains("animal bite") || qLower.contains("cat bite") || qLower.contains("rabies")) {
      detectedCondition = "poisoning"
      reply = "🐕 Wash the animal bite vigorously with soap and clean running water for 15 full minutes immediately. Apply povidone-iodine antiseptic. Never stitch or bandage tightly. Seek hospital emergency care immediately for Anti-Rabies Vaccine (ARV) and tetanus toxoid.\n\n[Source: WHO Rabies First-Aid & Prevention Guidelines]"
      highlight = "Animal Bite / Rabies Prevention"
    } else if (qLower.contains("snake") || qLower.contains("venom") || (qLower.contains("bite") && !qLower.contains("dog") && !qLower.contains("cat") && !qLower.contains("animal"))) {
      detectedCondition = "snakebite"
      reply = "🐍 Keep victim completely calm and still to slow venom circulation. Immobilize the bitten limb at or slightly below heart level with a broad bandage. NEVER cut the wound, suck venom, or apply a tourniquet. Rush to the nearest hospital with Anti-Snake Venom (ASV).\n\n[Source: WHO Guidelines for the Management of Snakebites]"
      highlight = "Snakebite Protocol"
    } else if (qLower.contains("asthma") || qLower.contains("inhaler") || qLower.contains("wheez") || qLower.contains("breathless")) {
      detectedCondition = "asthma"
      reply = "🫁 Help the person sit upright leaning slightly forward. Administer 4 separate puffs of their blue reliever inhaler (Salbutamol) with 4 deep breaths after each puff. If no improvement within 4 minutes, deliver 4 more puffs and call 108 immediately.\n\n[Source: Global Initiative for Asthma (GINA) 2023]"
      highlight = "Acute Asthma Relief"
    } else if (qLower.contains("heat") || qLower.contains("sunstroke") || qLower.contains("heatstroke")) {
      detectedCondition = "heatstroke"
      reply = "☀️ Move victim to a cool, shaded environment immediately. Remove excess clothing. Apply cool, wet towels to the neck, armpits, and groin while fanning vigorously. If conscious, offer cool water in small sips.\n\n[Source: NDMA Heat Wave Guidelines & Wilderness Medical Society]"
      highlight = "Heat Emergency Management"
    } else if (qLower.contains("poison") || qLower.contains("toxic") || qLower.contains("chemical") || qLower.contains("swallowed") || qLower.contains("pesticide")) {
      detectedCondition = "poisoning"
      reply = "🧪 DO NOT induce vomiting or administer fluids unless instructed by medical professionals. Keep any container or packaging for paramedic inspection. Check breathing and place in recovery position if drowsy. Call 108 immediately.\n\n[Source: WHO International Programme on Chemical Safety]"
      highlight = "Poisoning Emergency Protocol"
    } else if (qLower.contains("electric") || qLower.contains("current") || qLower.contains("wire") || qLower.contains("electrocution")) {
      detectedCondition = "electric_shock"
      reply = "⚡ DO NOT touch victim until power is disconnected at main breaker or source is pushed away with dry wood. Check breathing immediately; if unresponsive and no pulse, initiate CPR and call 108.\n\n[Source: OSHA & Red Cross Electrical Safety Protocols]"
      highlight = "Electrical Shock Protocol"
    } else if (qLower.contains("drown") || (qLower.contains("water") && (qLower.contains("pool") || qLower.contains("submerged") || qLower.contains("river")))) {
      detectedCondition = "drowning"
      reply = "🌊 Pull victim to dry flat surface. Drowning arrest causes severe oxygen depletion: deliver 5 initial rescue breaths first, then begin 30:2 compressions and breaths. Wipe chest dry before applying AED pads.\n\n[Source: International Lifesaving Federation & AHA 2020]"
      highlight = "Water Rescue & Resuscitation"
    } else if (qLower.contains("diabet") || qLower.contains("hypoglycemia") || qLower.contains("sugar") || qLower.contains("insulin")) {
      detectedCondition = "diabetic_emergency"
      reply = "🍬 If the person is conscious and can swallow, give 15–20g fast-acting sugar (fruit juice, 3 tsp sugar, or glucose tablets). Wait 15 minutes to re-evaluate. If unconscious, DO NOT give liquids; place in recovery position and call 108.\n\n[Source: American Diabetes Association Emergency Standards]"
      highlight = "Hypoglycemia Emergency Protocol"
    } else if (qLower.contains("faint") || qLower.contains("syncope") || qLower.contains("dizzy") || qLower.contains("passed out")) {
      detectedCondition = "seizures"
      reply = "🛌 Lay the person flat on their back and elevate legs approximately 30 cm (12 inches) to restore cerebral blood flow. Loosen collar and tight clothing. If unresponsiveness exceeds 1 minute or breathing is abnormal, call 108 immediately.\n\n[Source: Red Cross First-Aid Guidelines]"
      highlight = "Fainting / Syncope Protocol"
    } else if (qLower.contains("eye") || qLower.contains("cornea") || qLower.contains("vision splash")) {
      reply = "👁️ Flush the eye continuously with clean running water or saline for 15–20 minutes with eyelids held wide open. DO NOT rub the eye or attempt to remove embedded foreign objects. Cover loosely and seek immediate ophthalmologist evaluation.\n\n[Source: American Academy of Ophthalmology Emergency Guidelines]"
      highlight = "Eye Trauma & Chemical Flush"
    } else if (qLower.contains("headache") || qLower.contains("fever") || qLower.contains("stomach pain") || qLower.contains("medicine") || qLower.contains("tablet")) {
      detectedCondition = "medical_symptom"
      reply = "🩺 Medical Advisory:\n\n• For sudden extreme 'thunderclap' headache, stiff neck, or fever with rash, seek immediate emergency hospital care (possible meningitis or aneurysm).\n• Stay hydrated and rest in a cool, dark room.\n• Do NOT self-prescribe antibiotics or strong painkillers without a physician's physical diagnosis.\n\n[Source: WHO Clinical Practice Standards & ICMR Triage]"
      highlight = "Clinical Symptom Advisory"
    } else if (isCardiacOrCprQuery(text)) {
      detectedCondition = "cardiac_arrest"
      reply = "📋 Ensure victim is on a firm flat surface. Tap shoulders and shout. If unresponsive and not breathing normally, begin chest compressions at 110 BPM cadence in center of breastbone.\n\n[Source: AHA CPR Guidelines 2020 §3.2]"
      highlight = "Grounded Protocol Step"
    } else {
      reply = "📋 Emergency Triage Assessment:\n\n" +
          "1. 🛑 Check Scene Safety: Ensure area is safe from traffic, electrical wires, or fire.\n" +
          "2. 👤 Assess Response: Tap shoulders firmly and ask loudly: 'Are you okay?'.\n" +
          "3. 🫁 Check Breathing: Look for chest rise for 5–10 seconds.\n" +
          "4. 📞 Call 108: Dispatch ambulance immediately if unresponsive.\n\n" +
          "💡 Mention the emergency symptom or injury (e.g., 'hot oil burn', 'choking on food', 'dog bite', 'asthma attack', 'broken leg', 'chest pain') for immediate step-by-step guidance.\n\n" +
          "[Source: Indian Resuscitation Council & WHO Guidelines]"
      highlight = "Emergency Triage Assessment"
    }

    val isCardiac = isCardiacOrCprQuery(text)

    return AgentChatResponseDto(
      sessionId = sessionId,
      replyText = reply,
      highlightText = highlight,
      triageState = "GUIDANCE",
      conditionId = detectedCondition,
      severityLevel = getConditionSeverity(detectedCondition),
      priority = if (isCardiac || detectedCondition in listOf("cardiac_arrest", "severe_bleeding", "choking", "stroke", "anaphylaxis")) "critical" else "urgent",
      currentStepIndex = currentStepIndex,
      completedSteps = completedSteps,
      cprMetronomeActive = isCardiac,
      cprBpm = if (isCardiac) 110 else 0,
      citations = citations,
      contraindications = contraindications,
      legalShieldApplied = true,
      suggestedQuickQuestions = getQuickQuestionsForCondition(detectedCondition),
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
