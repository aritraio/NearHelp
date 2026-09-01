"""NearHelp AI — Emergency Agent Schemas & Data Contracts."""

from typing import Any, Literal

from pydantic import BaseModel, Field


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
    severity: Literal["CRITICAL", "WARNING", "CAUTION"] = Field(default="CRITICAL")
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


class AgentChatMessage(BaseModel):
    """Single chat message in an agent session."""

    id: str = Field(..., description="Unique message ID")
    sender: Literal["user", "gemini", "system", "responder", "dispatcher"] = Field(...)
    sender_name: str = Field(default="Bystander")
    text: str = Field(..., description="Message body text")
    timestamp: str = Field(..., description="Human-readable timestamp or ISO format")
    highlight_text: str | None = Field(default=None, description="Badge tag (e.g. 'Grounded Step', 'Contraindication')")
    is_actionable: bool = Field(default=False)
    citations: list[CitationItem] = Field(default_factory=list)
    contraindications: list[ContraindicationAlert] = Field(default_factory=list)


class AgentInitRequest(BaseModel):
    """Request to start or reset an AI emergency agent session."""

    session_id: str = Field(..., description="Unique session or incident identifier")
    condition_id: str = Field(default="cardiac_arrest", description="Emergency condition identifier")
    role: Literal["victim", "bystander", "responder"] = Field(default="bystander")
    language: str = Field(default="en", description="Preferred response language ('en', 'bn', 'hi')")
    initial_text: str | None = Field(default=None, description="Initial emergency utterance or symptom notes")


class AgentChatRequest(BaseModel):
    """Request for dialogue turn with the emergency agent."""

    session_id: str = Field(..., description="Active session ID")
    text: str = Field(..., description="Bystander query or emergency update")
    role: Literal["victim", "bystander", "responder"] = Field(default="bystander")
    language: str = Field(default="en")
    current_step_index: int = Field(default=0)
    completed_steps: list[int] = Field(default_factory=list)


class AgentChatResponse(BaseModel):
    """Agent response containing grounded response, citations, and contraindication alerts."""

    session_id: str
    reply_text: str
    highlight_text: str
    triage_state: Literal["UNDERSTAND", "TRIAGE", "GUIDANCE", "HANDOVER", "RESOLVED"]
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


class WebSocketClientMessage(BaseModel):
    """Payload sent from client over WebSocket."""

    action: Literal["user_message", "step_toggle", "set_metronome", "request_handover", "ping", "init"]
    session_id: str
    text: str | None = None
    step_number: int | None = None
    completed: bool | None = None
    metronome_active: bool | None = None
    condition_id: str | None = None
    role: Literal["victim", "bystander", "responder"] | None = None


class WebSocketServerFrame(BaseModel):
    """Structured frame broadcasted by server over WebSocket."""

    type: Literal["agent_chunk", "agent_message", "protocol_update", "contraindication_alert", "metronome_sync", "handover_report", "pong", "error"]
    session_id: str
    payload: dict[str, Any]
    timestamp: str | None = None
