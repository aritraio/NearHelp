"""NearHelp AI — Smart SOS Engine Schemas."""

import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class RankedResponderItem(BaseModel):
    """Ranked candidate responder item scored by weighted algorithm."""
    model_config = ConfigDict(from_attributes=True)

    responder_id: uuid.UUID
    name: str
    distance_meters: float
    distance_score: float = Field(..., description="Normalized inverse distance score D in [0, 1]")
    skill_match_score: float = Field(..., description="Skill match score S with +0.2 verified bonus in [0, 1.2]")
    reliability_score: float = Field(..., description="Normalized trust score R in [0, 1]")
    total_ranking_score: float = Field(..., description="Weighted composite score: 0.40*D + 0.35*S + 0.25*R")
    skills: list[str] = Field(default_factory=list)
    verified_skills: list[str] = Field(default_factory=list)
    trust_score: float = 50.0
    eta_minutes: float = Field(..., description="Estimated arrival time in minutes")
    fcm_token_available: bool = True


class SOSEscalationStatus(BaseModel):
    """3-Layer Escalation Protocol State."""
    model_config = ConfigDict(from_attributes=True)

    current_layer: int = Field(1, description="Active escalation layer: 1 (Community), 2 (108/112 Auto-dial), 3 (AI Self-Care)")
    current_radius_meters: float = Field(1500.0, description="Current radial dispatch wave radius in meters")
    max_radius_meters: float = Field(5000.0, description="Maximum radial expansion limit")
    elapsed_seconds: int = Field(0, description="Elapsed seconds since emergency creation")
    auto_call_108_triggered: bool = Field(False, description="Whether Layer 2 emergency services auto-dial recommendation is active")
    recommended_emergency_number: str = Field("108", description="Official municipal helpline number ('108' medical, '101' fire, '112' unified)")
    offline_fallback_ready: bool = Field(True, description="Whether Layer 3 offline RAG first-aid protocol is cached and ready")
    layer_description: str = Field("Layer 1: Community Network Radial Dispatch", description="Human-readable escalation state")


class SOSCreateRequest(BaseModel):
    """Payload for triggering an SOS emergency broadcast."""
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Incident GPS Latitude (WGS84)")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Incident GPS Longitude (WGS84)")
    crisis_type: str = Field("medical", description="Emergency category: medical, fire, crime, accident, gas_leak")
    sub_type: str | None = Field(None, description="Specific clinical condition (e.g. cardiac_arrest, severe_bleeding)")
    description: str | None = Field(None, max_length=2048, description="Emergency text notes or transcribed voice notes")
    address: str | None = Field(None, max_length=512, description="Street address or landmark landmark description")
    sub_address: str | None = Field(None, max_length=512, description="City / neighborhood / district")
    is_anonymous: bool = Field(False, description="Flag for zero-PII anonymous emergency mode")
    symptoms: list[str] | None = Field(None, description="Reported symptom tags")
    voice_transcript: str | None = Field(None, description="Voice SOS speech transcription")
    photo_url: str | None = Field(None, description="Uploaded scene image URL for Gemini Vision analysis")
    initial_radius_meters: float | None = Field(None, description="Optional custom initial search radius in meters")


class SOSCreateResponse(BaseModel):
    """Response returned upon successful SOS creation."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    broadcaster_id: uuid.UUID | None = None
    status: str
    crisis_type: str
    sub_type: str | None = None
    severity_score: int
    severity_level: int
    priority: str
    immediate_action: str | None = None
    required_skills: list[str] = Field(default_factory=list)
    latitude: float
    longitude: float
    address: str | None = None
    sub_address: str | None = None
    is_anonymous: bool
    current_radius_meters: float
    escalation: SOSEscalationStatus
    top_ranked_responders: list[RankedResponderItem] = Field(default_factory=list)
    candidates_notified_count: int = 0
    created_at: datetime


class SOSResponseRequest(BaseModel):
    """Payload for responder accepting or declining an SOS dispatch."""
    status: str = Field("ACCEPTED", description="Engagement response: ACCEPTED, DECLINED, EN_ROUTE, ARRIVED")
    eta_minutes: float | None = Field(None, description="Responder declared ETA in minutes")
    current_latitude: float | None = Field(None, ge=-90.0, le=90.0)
    current_longitude: float | None = Field(None, ge=-180.0, le=180.0)


class SOSResponseItem(BaseModel):
    """Responder engagement summary item."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    sos_event_id: uuid.UUID
    responder_id: uuid.UUID
    responder_name: str
    responder_trust_score: float = 50.0
    responder_skills: list[str] = Field(default_factory=list)
    status: str
    initial_distance_meters: float | None = None
    initial_eta_seconds: int | None = None
    ranking_score: float | None = None
    joined_at: datetime
    arrived_at: datetime | None = None


class TimelineEventItem(BaseModel):
    """Chronological milestone in emergency event timeline."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    sos_event_id: uuid.UUID
    actor_id: uuid.UUID | None = None
    actor_name: str | None = None
    event_type: str
    details: dict = Field(default_factory=dict)
    timestamp: datetime


class SOSDetailResponse(BaseModel):
    """Full detail view of an SOS emergency incident."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    broadcaster_id: uuid.UUID | None = None
    broadcaster_name: str | None = None
    status: str
    crisis_type: str
    sub_type: str | None = None
    severity_score: int
    severity_level: int
    priority: str
    description: str | None = None
    symptoms: list[str] = Field(default_factory=list)
    immediate_action: str | None = None
    required_skills: list[str] = Field(default_factory=list)
    latitude: float
    longitude: float
    address: str | None = None
    sub_address: str | None = None
    is_anonymous: bool
    current_radius_meters: float
    escalation: SOSEscalationStatus
    responses: list[SOSResponseItem] = Field(default_factory=list)
    timeline: list[TimelineEventItem] = Field(default_factory=list)
    ai_triage_data: dict = Field(default_factory=dict)
    created_at: datetime
    resolved_at: datetime | None = None


class SOSEscalateRequest(BaseModel):
    """Payload to trigger time-based or manual escalation step."""
    elapsed_seconds: int | None = Field(None, description="Elapsed seconds to evaluate escalation gate (30s, 45s, 60s)")
    force_layer: int | None = Field(None, ge=1, le=3, description="Force transition to specific escalation layer")


class SOSResolveRequest(BaseModel):
    """Payload for marking an SOS emergency resolved."""
    resolution_notes: str | None = Field(None, max_length=1024, description="Clinical or incident resolution summary")
    feedback_score: float | None = Field(None, ge=1.0, le=5.0, description="Rating score for responders")
    resolved_by: str = Field("victim", description="Actor resolving incident: 'victim', 'responder', 'admin'")


class SOSResolveResponse(BaseModel):
    """Response returned when an SOS emergency is resolved."""
    id: uuid.UUID
    status: str = "RESOLVED"
    resolved_at: datetime
    message: str
    reputation_updates: list[dict] = Field(default_factory=list)


class SOSActiveListItem(BaseModel):
    """Concise item for active emergency feed list."""
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    crisis_type: str
    sub_type: str | None = None
    severity_score: int
    severity_level: int
    priority: str
    status: str
    latitude: float
    longitude: float
    address: str | None = None
    distance_meters: float | None = None
    responders_count: int = 0
    is_anonymous: bool = False
    created_at: datetime


class SOSActiveListResponse(BaseModel):
    """List of active SOS emergencies."""
    active_events: list[SOSActiveListItem] = Field(default_factory=list)
    total_count: int = 0
