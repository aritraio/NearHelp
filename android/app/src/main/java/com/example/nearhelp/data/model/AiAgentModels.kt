package com.example.nearhelp.data.model

import com.google.gson.annotations.SerializedName

data class CitationDto(
  @SerializedName("source") val source: String,
  @SerializedName("section") val section: String,
  @SerializedName("guideline_name") val guidelineName: String,
  @SerializedName("authority") val authority: String,
  @SerializedName("url") val url: String? = null
)

data class ContraindicationAlertDto(
  @SerializedName("flag") val flag: String,
  @SerializedName("severity") val severity: String = "CRITICAL",
  @SerializedName("warning_title") val warningTitle: String,
  @SerializedName("warning_message") val warningMessage: String,
  @SerializedName("action_directive") val actionDirective: String
)

data class ProtocolStepDto(
  @SerializedName("step_number") val stepNumber: Int,
  @SerializedName("title") val title: String,
  @SerializedName("action_instruction") val actionInstruction: String,
  @SerializedName("warning_note") val warningNote: String? = null,
  @SerializedName("is_cpr_step") val isCprStep: Boolean = false,
  @SerializedName("beat_bpm") val beatBpm: Int? = null,
  @SerializedName("icon") val icon: String = "AlertCircle"
)

data class GroundedProtocolDto(
  @SerializedName("condition_id") val conditionId: String,
  @SerializedName("condition_label") val conditionLabel: String,
  @SerializedName("crisis_type") val crisisType: String,
  @SerializedName("severity_level") val severityLevel: Int,
  @SerializedName("priority") val priority: String,
  @SerializedName("protocol_title") val protocolTitle: String,
  @SerializedName("authority") val authority: String,
  @SerializedName("disclaimers") val disclaimers: String,
  @SerializedName("legal_shield") val legalShield: String,
  @SerializedName("recommended_radius_km") val recommendedRadiusKm: Double,
  @SerializedName("emergency_number") val emergencyNumber: String,
  @SerializedName("cpr_bpm") val cprBpm: Int? = null,
  @SerializedName("steps") val steps: List<ProtocolStepDto> = emptyList(),
  @SerializedName("citations") val citations: List<CitationDto> = emptyList()
)

data class AgentChatRequestDto(
  @SerializedName("session_id") val sessionId: String,
  @SerializedName("text") val text: String,
  @SerializedName("role") val role: String = "bystander",
  @SerializedName("language") val language: String = "en",
  @SerializedName("current_step_index") val currentStepIndex: Int = 0,
  @SerializedName("completed_steps") val completedSteps: List<Int> = emptyList()
)

data class AgentChatResponseDto(
  @SerializedName("session_id") val sessionId: String,
  @SerializedName("reply_text") val replyText: String,
  @SerializedName("highlight_text") val highlightText: String,
  @SerializedName("triage_state") val triageState: String,
  @SerializedName("condition_id") val conditionId: String,
  @SerializedName("severity_level") val severityLevel: Int,
  @SerializedName("priority") val priority: String,
  @SerializedName("current_step_index") val currentStepIndex: Int,
  @SerializedName("completed_steps") val completedSteps: List<Int>,
  @SerializedName("cpr_metronome_active") val cprMetronomeActive: Boolean,
  @SerializedName("cpr_bpm") val cprBpm: Int,
  @SerializedName("citations") val citations: List<CitationDto> = emptyList(),
  @SerializedName("contraindications") val contraindications: List<ContraindicationAlertDto> = emptyList(),
  @SerializedName("legal_shield_applied") val legalShieldApplied: Boolean = true,
  @SerializedName("suggested_quick_questions") val suggestedQuickQuestions: List<String> = emptyList(),
  @SerializedName("processing_time_ms") val processingTimeMs: Double = 0.0
)

data class ClinicalHandoverSummaryDto(
  @SerializedName("report_id") val reportId: String,
  @SerializedName("session_id") val sessionId: String,
  @SerializedName("incident_code") val incidentCode: String,
  @SerializedName("generated_at") val generatedAt: String,
  @SerializedName("victim_profile") val victimProfile: Map<String, Any> = emptyMap(),
  @SerializedName("emergency_location") val emergencyLocation: String,
  @SerializedName("severity_level") val severityLevel: Int,
  @SerializedName("diagnostic_summary") val diagnosticSummary: String,
  @SerializedName("ai_confidence_score") val aiConfidenceScore: Double,
  @SerializedName("reported_symptoms") val reportedSymptoms: List<String> = emptyList(),
  @SerializedName("cpr_metronome_used") val cprMetronomeUsed: Boolean,
  @SerializedName("cpr_compressions_estimated") val cprCompressionsEstimated: Int,
  @SerializedName("cpr_duration_seconds") val cprDurationSeconds: Int,
  @SerializedName("aed_deployed") val aedDeployed: Boolean,
  @SerializedName("aed_shocks_delivered") val aedShocksDelivered: Int,
  @SerializedName("completed_protocol_steps") val completedProtocolSteps: List<String> = emptyList(),
  @SerializedName("citations") val citations: List<CitationDto> = emptyList(),
  @SerializedName("destination_hospital") val destinationHospital: String,
  @SerializedName("legal_shield_compliance") val legalShieldCompliance: String,
  @SerializedName("digital_signature_hash") val digitalSignatureHash: String
)

data class AiChatMessageUiModel(
  val id: String,
  val isUser: Boolean,
  val senderName: String,
  val text: String,
  val timestamp: String,
  val highlightBadge: String? = null,
  val citations: List<CitationDto> = emptyList(),
  val contraindications: List<ContraindicationAlertDto> = emptyList()
)
