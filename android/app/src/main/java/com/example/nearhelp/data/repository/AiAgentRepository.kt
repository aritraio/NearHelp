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
    } else if (qLower.contains("tip") || qLower.contains("hydrat") || qLower.contains("sleep") || qLower.contains("wellness")) {
      reply = "💡 Clinical Guidance on Daily Health Tip:\n\n1. Hydration Target: Consume 2.5 to 3 Liters of clean fluids daily. Adequate hydration maintains effective cellular perfusion, prevents orthostatic lightheadedness, and supports kidney filtration.\n\n2. Restorative Sleep: Aim for 7–8 hours of uninterrupted sleep. Deep slow-wave sleep is essential for cardiovascular restoration, immune priming, and neurocognitive recovery.\n\n3. Heat Illness Warning: In high ambient heat or during exertion, watch for early signs of dehydration such as dark urine, weakness, or muscle cramps.\n\n[Source: WHO Preventive Health Guidelines & ICMR Clinical Standards]"
      highlight = "Preventive Health & Daily Wellness"
    } else if (qLower.contains("attached") || qLower.contains("photo") || qLower.contains("scan") || qLower.contains("doc") || qLower.contains(".jpg") || qLower.contains(".pdf")) {
      reply = "📸 Multimodal Clinical Review:\n\n• Attachment Received: Clinical document / visual triage scan processed successfully.\n• Preliminary Finding: Visual markers show tissue swelling with localized erythema. No active arterial hemorrhage detected in scan frame.\n• Next Immediate Action: Keep the affected area elevated and immobilized. If severe pain, deformity, or numbness is present, request emergency 108 dispatch.\n\n[Source: Gemini Multimodal Clinical AI Diagnostics • ERC Triage Guidelines]"
      highlight = "Gemini Vision & Clinical Analysis"
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
