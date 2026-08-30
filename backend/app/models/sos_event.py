"""NearHelp AI — SOS Event Model Definition."""

import uuid

from geoalchemy2 import Geometry
from sqlalchemy import (
    JSON,
    Boolean,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.db.base import Base


class SOSEvent(Base):
    """SOS emergency event entity for NearHelp AI.
    
    Represents an emergency broadcast triggered by a victim or bystander,
    tracking geospatial coordinates, AI triage severity, escalation state, and lifecycle.
    """
    __tablename__ = "sos_events"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique SOS incident identifier (UUID v4)",
    )

    # Broadcaster / Victim Linking (Nullable for anonymous emergency mode)
    broadcaster_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="SET NULL"),
        nullable=True,
        index=True,
        doc="User ID of victim/broadcaster (null in anonymous mode)",
    )

    # Emergency Categorization & AI Triage
    crisis_type = Column(
        String(50),
        default="medical",
        nullable=False,
        index=True,
        doc="Primary crisis category: 'medical', 'fire', 'crime', 'accident', 'gas_leak'",
    )
    sub_type = Column(
        String(100),
        nullable=True,
        doc="Specific emergency condition: 'cardiac_arrest', 'severe_bleeding', etc.",
    )
    severity_score = Column(
        Integer,
        default=50,
        nullable=False,
        doc="AI predicted clinical severity score (0 to 100)",
    )
    severity_level = Column(
        Integer,
        default=3,
        nullable=False,
        doc="Triage priority level (Level 1 to Level 5)",
    )
    priority = Column(
        String(50),
        default="high",
        nullable=False,
        doc="Emergency priority string: 'critical', 'high', 'moderate', 'low'",
    )

    # Narrative & Clinical Observation
    description = Column(
        String(2048),
        nullable=True,
        doc="Free-text or transcribed voice description of the emergency",
    )
    symptoms = Column(
        JSON,
        default=list,
        nullable=False,
        doc="List of detected symptoms / clinical tags",
    )
    immediate_action = Column(
        String(1024),
        nullable=True,
        doc="First-aid directive recommended for bystanders on scene",
    )
    required_skills = Column(
        JSON,
        default=list,
        nullable=False,
        doc="Recommended responder skill qualifications (e.g. ['CPR_CERTIFIED', 'DOCTOR'])",
    )

    # Geospatial Coordinates & Location
    location = Column(
        Geometry(geometry_type="POINT", srid=4326),
        nullable=True,
        doc="PostGIS Point coordinates (WGS84 4326)",
    )
    latitude = Column(
        Float,
        nullable=False,
        doc="Incident latitude (WGS84)",
    )
    longitude = Column(
        Float,
        nullable=False,
        doc="Incident longitude (WGS84)",
    )
    address = Column(
        String(512),
        nullable=True,
        doc="Primary street address / landmark",
    )
    sub_address = Column(
        String(512),
        nullable=True,
        doc="Secondary address details (e.g. Sector V, Salt Lake, Kolkata)",
    )

    # Status & Privacy
    status = Column(
        String(50),
        default="SOS_TRIGGERED",
        nullable=False,
        index=True,
        doc="Lifecycle state: 'IDLE', 'COUNTDOWN', 'SOS_TRIGGERED', 'AI_TRIAGING', 'RESPONDER_ACCEPTED', 'HANDOVER_108', 'RESOLVED', 'CANCELLED'",
    )
    is_anonymous = Column(
        Boolean,
        default=False,
        nullable=False,
        index=True,
        doc="Whether incident was initiated in zero-PII anonymous mode",
    )

    # 3-Layer Escalation Tracking
    current_radius_meters = Column(
        Float,
        default=1500.0,
        nullable=False,
        doc="Current geospatial search & dispatch wave radius in meters",
    )
    max_radius_meters = Column(
        Float,
        default=5000.0,
        nullable=False,
        doc="Upper boundary for radial expansion in meters",
    )
    escalation_layer = Column(
        Integer,
        default=1,
        nullable=False,
        doc="Active escalation layer: 1 (Community), 2 (108/112 Auto-dial), 3 (AI Self-Care)",
    )
    auto_call_108_triggered = Column(
        Boolean,
        default=False,
        nullable=False,
        doc="Flags whether 108/112 auto-dial recommendation gateway has triggered",
    )

    # Stored AI Triage Results & Summaries
    ai_triage_data = Column(
        JSON,
        default=dict,
        nullable=False,
        doc="Structured response payload from AI classification & severity predictors",
    )
    metadata_info = Column(
        JSON,
        default=dict,
        nullable=True,
        doc="Additional contextual metadata (voice duration, photo attachment, device telemetry)",
    )

    # Audit Timestamps
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        index=True,
        doc="Emergency trigger timestamp",
    )
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
        doc="Last state update timestamp",
    )
    resolved_at = Column(
        DateTime(timezone=True),
        nullable=True,
        doc="Timestamp when emergency was marked resolved",
    )

    def __repr__(self) -> str:
        return f"<SOSEvent id={self.id} crisis={self.crisis_type} status={self.status} severity={self.severity_score}>"
