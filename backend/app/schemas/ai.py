"""NearHelp AI — Backend AI Pydantic Schemas."""

from typing import Any

from pydantic import BaseModel, Field


class ClassificationRequest(BaseModel):
    """Input payload for emergency classification from text, voice audio, or photo."""

    text: str | None = Field(
        None,
        description="Free-text emergency description or bystander notes",
        examples=["Man collapsed in lobby, unresponsive, clutching chest, gasping for air"],
    )
    audio_base64: str | None = Field(
        None,
        description="Base64-encoded audio byte stream from voice SOS or microphone",
    )
    audio_format: str | None = Field(
        "wav",
        description="Audio format encoding (wav, mp3, ogg, m4a, flac, webm)",
    )
    image_base64: str | None = Field(
        None,
        description="Base64-encoded photo of emergency scene or injuries",
    )
    image_mime_type: str | None = Field(
        "image/jpeg",
        description="MIME type of attached image (image/jpeg, image/png, image/webp)",
    )
    language_code: str | None = Field(
        None,
        description="Spoken or written language hint (en-IN, bn-IN, hi-IN, auto)",
    )
    location: list[float] | None = Field(
        None,
        description="GPS coordinates of the incident [latitude, longitude]",
        examples=[[22.5726, 88.4312]],
    )
    metadata: dict[str, Any] | None = Field(
        default_factory=dict,
        description="Optional auxiliary metadata (victim profile, caller info, etc.)",
    )


class ClassificationResponse(BaseModel):
    """Structured clinical and operational triage output from the AI emergency detector."""

    emergency_type: str = Field(
        ...,
        description="Primary crisis classification category (medical, fire, gas_leak, accident, crime, natural_disaster)",
        examples=["medical"],
    )
    sub_type: str = Field(
        ...,
        description="Granular emergency subtype or clinical condition identifier",
        examples=["cardiac_arrest"],
    )
    priority: str = Field(
        ...,
        description="Triage priority level (critical, high, medium, low)",
        examples=["critical"],
    )
    severity_level: int = Field(
        ...,
        ge=1,
        le=5,
        description="Quantified emergency triage severity level (1=Lowest, 5=Life Threat)",
        examples=[5],
    )
    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Cosine similarity and clinical inference confidence score (0.00 to 1.00)",
        examples=[0.984],
    )
    confidence_percentage: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Human-readable confidence percentage (e.g. 98.4%)",
        examples=[98.4],
    )
    recommended_radius_km: float = Field(
        ...,
        ge=0.1,
        le=20.0,
        description="Calculated spatial dispatch search perimeter in kilometers",
        examples=[3.0],
    )
    suggested_responder_skills: list[str] = Field(
        default_factory=list,
        description="List of verified skills prioritized for responder recruitment",
        examples=[["CPR_CERTIFIED", "DOCTOR", "EMT"]],
    )
    immediate_action: str = Field(
        ...,
        description="Direct AHA/IRC first-aid action directive for on-scene bystanders",
        examples=["Begin CPR immediately. Push hard and fast in center of chest at 110-120 BPM."],
    )
    requires_professional: bool = Field(
        ...,
        description="Indicates whether advance emergency medical services/fire brigade are mandatory",
        examples=[True],
    )
    call_emergency_services: bool = Field(
        ...,
        description="Triggers automatic or recommended 108/101/100 emergency dispatch dialer",
        examples=[True],
    )
    emergency_number: str = Field(
        ...,
        description="Applicable statutory Indian emergency helpline number (108, 101, 100, 112)",
        examples=["108"],
    )
    detected_symptoms: list[str] = Field(
        default_factory=list,
        description="Extracted key clinical symptom markers and danger indicators",
        examples=[["Unresponsive", "Agonal Breathing", "Sudden Collapse"]],
    )
    transcription: str | None = Field(
        None,
        description="Transcribed text from voice audio input, if provided",
    )
    image_description: str | None = Field(
        None,
        description="Multimodal scene understanding summary from photo input, if provided",
    )
    processing_time_ms: float = Field(
        ...,
        description="Total classification inference and vector matching time in milliseconds",
        examples=[42.5],
    )


class ClinicalConditionItem(BaseModel):
    """One of the 8 curated clinical conditions in the emergency matrix."""

    id: str
    label: str
    icon_name: str
    severity: int
    priority: str
    description: str
    symptoms: list[str]
    suggested_skills: list[str]
    immediate_action: str
    recommended_radius_km: float
    emergency_number: str


class CrisisTypeItem(BaseModel):
    """Top-level crisis category definition."""

    id: str
    name: str
    description: str
    default_emergency_number: str
    sub_types: list[str]


class TaxonomyResponse(BaseModel):
    """Full crisis taxonomy and 8 clinical conditions matrix."""

    crisis_types: list[CrisisTypeItem]
    clinical_conditions: list[ClinicalConditionItem]
    version: str = "1.0.0"


class SeverityScoreFactors(BaseModel):
    """Detailed breakdown of clinical severity scoring factors (0 to 100)."""

    life_threat_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Immediacy of threat to victim's life and vital organ failure risk (0-100)",
        examples=[95.0],
    )
    time_sensitivity_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Time sensitivity and urgency of intervention before irreversible damage/hypoxia (0-100)",
        examples=[98.0],
    )
    casualty_risk_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Risk of multiple casualties or situation escalating to more victims (0-100)",
        examples=[30.0],
    )
    environmental_hazard_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Hazards present on scene such as fire, toxic gas, collapse, live electrical wires, or traffic (0-100)",
        examples=[20.0],
    )


class SeverityRequest(BaseModel):
    """Input payload for emergency severity evaluation and clinical triage."""

    text: str | None = Field(
        None,
        description="Free-text emergency description or bystander narration",
        examples=["Victim collapsed suddenly in office lobby, unresponsive, no pulse detected, gasping for air"],
    )
    emergency_type: str | None = Field(
        None,
        description="Optional pre-classified crisis category (medical, fire, gas_leak, accident, crime, natural_disaster)",
        examples=["medical"],
    )
    sub_type: str | None = Field(
        None,
        description="Optional granular clinical sub-type (cardiac_arrest, severe_bleeding, structural_fire, etc.)",
        examples=["cardiac_arrest"],
    )
    detected_symptoms: list[str] | None = Field(
        default_factory=list,
        description="List of detected or observed symptoms and clinical markers",
        examples=[["Unresponsive", "No pulse", "Agonal breathing"]],
    )
    patient_age: int | None = Field(
        None,
        ge=0,
        le=130,
        description="Age of the patient/victim if known",
        examples=[58],
    )
    unresponsive: bool | None = Field(
        None,
        description="Whether the victim is unresponsive or unconscious",
        examples=[True],
    )
    breathing_difficulty: bool | None = Field(
        None,
        description="Whether the victim has severe difficulty breathing or agonal gasping",
        examples=[True],
    )
    severe_bleeding: bool | None = Field(
        None,
        description="Whether there is active pulsatile or heavy bleeding",
        examples=[False],
    )
    location: list[float] | None = Field(
        None,
        description="GPS coordinates of the emergency [latitude, longitude]",
        examples=[[22.5726, 88.4312]],
    )
    metadata: dict[str, Any] | None = Field(
        default_factory=dict,
        description="Optional auxiliary metadata (responder count, caller info, etc.)",
    )


class SeverityResponse(BaseModel):
    """Structured clinical severity score, Level 1-5 triage, radius, auto-dial flags, and reasoning."""

    severity_score: int = Field(
        ...,
        ge=0,
        le=100,
        description="Quantified overall severity score on a 0 to 100 continuous scale",
        examples=[96],
    )
    severity_level: int = Field(
        ...,
        ge=1,
        le=5,
        description="Standardized discrete triage level (Level 5=Critical Life Threat, 4=Urgent Trauma, 3=Moderate Emergency, 2=Minor/Low, 1=Non-acute)",
        examples=[5],
    )
    priority: str = Field(
        ...,
        description="Operational triage priority tier (critical, high, medium, low)",
        examples=["critical"],
    )
    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Clinical confidence score of the AI assessment (0.00 to 1.00)",
        examples=[0.984],
    )
    confidence_percentage: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Human-readable confidence percentage (e.g. 98.4%)",
        examples=[98.4],
    )
    reasoning: list[str] = Field(
        ...,
        description="Bulleted clinical justifications and observations explaining the severity score",
        examples=[[
            "Unresponsive victim indicates possible cardiac arrest or acute neurological catastrophe.",
            "Absence of pulse and agonal breathing represents an immediate, irreversible life threat.",
            "CPR and defibrillation must be initiated within the 4-5 minute hypoxia window for brain survival.",
        ]],
    )
    factors: SeverityScoreFactors = Field(
        ...,
        description="Breakdown of individual risk factor components",
    )
    recommended_radius_km: float = Field(
        ...,
        ge=0.1,
        le=20.0,
        description="Dynamically calculated spatial responder recruitment radius in kilometers",
        examples=[3.5],
    )
    survival_window_minutes: int | None = Field(
        None,
        description="Estimated critical survival window before irreversible brain damage or mortality (e.g. 5 min hypoxia window)",
        examples=[5],
    )
    auto_call_emergency_services: bool = Field(
        ...,
        description="Indicates whether the system automatically dials or mandates statutory emergency services (108/101/100/112)",
        examples=[True],
    )
    suggested_call_action: str = Field(
        ...,
        description="Action directive for calling emergency services: 'auto_dial', 'suggested', 'optional', 'none'",
        examples=["auto_dial"],
    )
    emergency_number: str = Field(
        ...,
        description="Applicable statutory Indian emergency helpline number (108, 101, 100, 112)",
        examples=["108"],
    )
    recommended_actions: list[str] = Field(
        default_factory=list,
        description="Immediate life-saving clinical first-aid or safety action directives for on-scene bystanders",
        examples=[[
            "Begin CPR immediately: compress center of chest 5-6 cm deep at 110-120 BPM.",
            "Send a bystander to fetch the nearest Automated External Defibrillator (AED).",
            "Call 108 Emergency Medical Services immediately.",
            "Never administer water, oral fluids, or food to an unresponsive person.",
        ]],
    )
    required_responder_skills: list[str] = Field(
        default_factory=list,
        description="List of verified skills prioritized for responder recruitment",
        examples=[["CPR_CERTIFIED", "DOCTOR", "EMT", "NURSE"]],
    )
    processing_time_ms: float = Field(
        ...,
        description="Total AI severity assessment inference and scoring latency in milliseconds",
        examples=[24.6],
    )


# ==============================================================================
# EMERGENCY CRISIS ASSISTANT AGENT SCHEMAS
# ==============================================================================

class CitationItem(BaseModel):
    """Evidence-based clinical or statutory citation."""

    source: str = Field(..., description="Organization or Act (e.g. 'AHA CPR Guidelines 2020', 'Motor Vehicles Act 2019')")
    section: str = Field(..., description="Specific section, clause, or paragraph (e.g. 'Part 3: Adult Basic Life Support §3.2', 'Section 134A')")
    guideline_name: str = Field(..., description="Full guideline or statute name")
    authority: str = Field(..., description="Issuing authority (e.g. 'American Heart Association', 'Ministry of Road Transport & Highways')")
    url: str | None = Field(default=None, description="Optional reference documentation URL")


class ContraindicationAlert(BaseModel):
    """Clinical contraindication or life-safety warning detected in context."""

    flag: str = Field(..., description="Identifier for contraindication (e.g. 'NO_ORAL_FLUIDS', 'SPINAL_IMMOBILIZATION')")
    severity: str = Field(default="CRITICAL")
    warning_title: str = Field(..., description="Short prominent alert title")
    warning_message: str = Field(..., description="Clinical rationale and danger explanation")
    action_directive: str = Field(..., description="Immediate DO / DO NOT instruction")


class ProtocolStepItem(BaseModel):
    """Actionable step in an evidence-based first-aid protocol."""

    step_number: int = Field(..., description="1-indexed sequence number")
    title: str = Field(..., description="Step headline")
    action_instruction: str = Field(..., description="Detailed bystander instruction")
    warning_note: str | None = Field(default=None, description="Critical warning or precaution")
    is_cpr_step: bool = Field(default=False, description="Whether this step involves CPR rhythm compressions")
    beat_bpm: int | None = Field(default=None, description="Target cadence (e.g. 110 BPM)")
    icon: str = Field(default="AlertCircle", description="Lucide icon identifier")


class GroundedProtocolResponse(BaseModel):
    """Full grounded first-aid protocol response."""

    condition_id: str
    condition_label: str
    crisis_type: str
    severity_level: int
    priority: str
    protocol_title: str
    authority: str
    disclaimers: str
    legal_shield: str
    recommended_radius_km: float
    emergency_number: str
    cpr_bpm: int | None = None
    steps: list[ProtocolStepItem]
    citations: list[CitationItem]


class AgentInitRequest(BaseModel):
    """Request to start or reset an AI emergency agent session."""

    session_id: str = Field(..., description="Unique session or incident identifier")
    condition_id: str = Field(default="cardiac_arrest", description="Emergency condition identifier")
    role: str = Field(default="bystander")
    language: str = Field(default="en")
    initial_text: str | None = None


class AgentChatRequest(BaseModel):
    """Request for dialogue turn with the emergency agent."""

    session_id: str = Field(..., description="Active session ID")
    text: str = Field(..., description="Bystander query or emergency update")
    role: str = Field(default="bystander")
    language: str = Field(default="en")
    current_step_index: int = Field(default=0)
    completed_steps: list[int] = Field(default_factory=list)


class AgentChatResponse(BaseModel):
    """Agent response containing grounded response, citations, and contraindication alerts."""

    session_id: str
    reply_text: str
    highlight_text: str
    triage_state: str
    condition_id: str
    severity_level: int
    priority: str
    current_step_index: int
    completed_steps: list[int]
    cpr_metronome_active: bool
    cpr_bpm: int
    citations: list[CitationItem]
    contraindications: list[ContraindicationAlert]
    legal_shield_applied: bool = True
    suggested_quick_questions: list[str] = Field(default_factory=list)
    processing_time_ms: float = 0.0


class StepProgressRequest(BaseModel):
    """Request to toggle or advance protocol step completion."""

    session_id: str
    step_number: int
    completed: bool = True


class StepProgressResponse(BaseModel):
    """Updated protocol progress status."""

    session_id: str
    completed_steps: list[int]
    total_steps: int
    progress_percentage: int
    current_step_index: int
    all_completed: bool


class ClinicalHandoverSummary(BaseModel):
    """Structured clinical handover report for arriving paramedics and 108 ALS teams."""

    report_id: str
    session_id: str
    incident_code: str
    generated_at: str
    victim_profile: dict[str, Any]
    emergency_location: str
    severity_level: int
    diagnostic_summary: str
    ai_confidence_score: float
    reported_symptoms: list[str]
    cpr_metronome_used: bool
    cpr_compressions_estimated: int
    cpr_duration_seconds: int
    aed_deployed: bool
    aed_shocks_delivered: int
    completed_protocol_steps: list[str]
    citations: list[CitationItem]
    destination_hospital: str
    legal_shield_compliance: str
    digital_signature_hash: str
