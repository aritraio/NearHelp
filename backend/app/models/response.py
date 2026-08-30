"""NearHelp AI — SOS Response Model Definition."""

import uuid

from sqlalchemy import (
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


class SOSResponse(Base):
    """Responder acceptance and engagement tracking entity.
    
    Records when a community volunteer, EMT, nurse, or doctor accepts an SOS event,
    their initial ranking score, distance, arrival confirmation, and post-rescue feedback.
    """
    __tablename__ = "responses"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique response engagement identifier (UUID v4)",
    )

    # Relationships
    sos_event_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("sos_events.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
        doc="Associated SOS emergency event ID",
    )
    responder_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
        doc="User ID of responding volunteer/medic",
    )

    # Status
    status = Column(
        String(50),
        default="ACCEPTED",
        nullable=False,
        index=True,
        doc="Engagement status: 'ACCEPTED', 'EN_ROUTE', 'ARRIVED', 'DECLINED', 'CANCELLED'",
    )

    # Dispatch Metrics
    initial_distance_meters = Column(
        Float,
        nullable=True,
        doc="Initial distance between responder and victim when alert was accepted (meters)",
    )
    initial_eta_seconds = Column(
        Integer,
        nullable=True,
        doc="Estimated travel time upon acceptance (seconds)",
    )
    ranking_score = Column(
        Float,
        nullable=True,
        doc="Weighted ranking score computed during candidate selection (0.0 to 1.0+)",
    )

    # Timestamps
    joined_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        doc="Timestamp when responder accepted the alert",
    )
    arrived_at = Column(
        DateTime(timezone=True),
        nullable=True,
        doc="Timestamp when responder arrived on scene (GPS confirmed / manual)",
    )

    # Post-Rescue Feedback & Reputation
    feedback_score = Column(
        Float,
        nullable=True,
        doc="Victim rating score (1.0 to 5.0) used for reputation engine trust score adjustments",
    )
    feedback_notes = Column(
        String(1024),
        nullable=True,
        doc="Optional review comments from victim or incident lead",
    )

    def __repr__(self) -> str:
        return f"<SOSResponse id={self.id} event={self.sos_event_id} responder={self.responder_id} status={self.status}>"
