"""NearHelp AI — Timeline Event Model Definition."""

import uuid

from sqlalchemy import (
    JSON,
    Column,
    DateTime,
    ForeignKey,
    String,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.db.base import Base


class TimelineEvent(Base):
    """Auditable emergency timeline milestone entity for NearHelp AI.
    
    Persists immutable chronological events across the entire emergency lifecycle:
    trigger, AI triage, responder notifications, responses, arrivals, handovers, and resolution.
    """
    __tablename__ = "timeline_events"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique timeline event identifier (UUID v4)",
    )

    # Relationships
    sos_event_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("sos_events.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
        doc="Associated SOS emergency event ID",
    )
    actor_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="SET NULL"),
        nullable=True,
        doc="User ID who triggered the action (or null for automated system events)",
    )

    # Event Attributes
    event_type = Column(
        String(100),
        nullable=False,
        index=True,
        doc="Event identifier (e.g. 'sos_created', 'ai_classified', 'response_accepted', 'responder_arrived', 'handover_108', 'sos_resolved')",
    )
    details = Column(
        JSON,
        default=dict,
        nullable=False,
        doc="Contextual metadata, structured attributes, or AI guidance references",
    )

    # Audit Timestamp
    timestamp = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        index=True,
        doc="Chronological occurrence timestamp",
    )

    def __repr__(self) -> str:
        return f"<TimelineEvent id={self.id} event={self.event_type} sos={self.sos_event_id} at={self.timestamp}>"
