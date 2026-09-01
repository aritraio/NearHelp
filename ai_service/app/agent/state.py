"""NearHelp AI — LangGraph Agent State Definition."""

from typing import Any, Literal, TypedDict


class AgentMessageDict(TypedDict, total=False):
    id: str
    sender: str
    sender_name: str
    text: str
    timestamp: str
    highlight_text: str | None
    is_actionable: bool
    citations: list[dict[str, Any]]
    contraindications: list[dict[str, Any]]


class EmergencyAgentState(TypedDict, total=False):
    """Unified state container for NearHelp LangGraph Emergency Crisis Agent."""

    # Session & Identity
    session_id: str
    role: Literal["victim", "bystander", "responder"]
    language: str

    # Emergency Triage Context
    condition_id: str
    emergency_type: str
    sub_type: str
    severity_level: int
    priority: str
    triage_state: Literal["UNDERSTAND", "TRIAGE", "GUIDANCE", "HANDOVER", "RESOLVED"]

    # Dialogue & History
    user_query: str
    messages: list[dict[str, Any]]

    # Step Progression & Protocol Checklist
    current_step_index: int
    completed_steps: list[int]
    protocol: dict[str, Any]

    # Clinical Metronome & Devices
    cpr_metronome_active: bool
    cpr_bpm: int
    aed_attached: bool

    # Safety Guardrails & Evidence Grounding
    contraindications_flagged: list[dict[str, Any]]
    citations: list[dict[str, Any]]
    legal_shield_applied: bool

    # Output & Handover
    reply_text: str
    highlight_text: str
    suggested_quick_questions: list[str]
    handover_report: dict[str, Any] | None
    processing_time_ms: float
