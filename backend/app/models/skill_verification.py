"""NearHelp AI — Skill Verification Model Definition."""

import uuid

from sqlalchemy import (
    Column,
    DateTime,
    ForeignKey,
    String,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.orm import relationship

from app.db.base import Base


class SkillVerification(Base):
    """Skill Verification request entity.
    
    Tracks credentials, certification uploads, review status, and trust score attribution.
    """
    __tablename__ = "skill_verifications"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique skill verification identifier (UUID v4)",
    )

    # User Reference
    user_id = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
        doc="User who claimed and submitted the skill certification",
    )

    # Skill Details
    skill_type = Column(
        String(100),
        nullable=False,
        index=True,
        doc="Standardized skill code (e.g. CPR_CERTIFIED, DOCTOR, EMT, NURSE, FIRST_AID, PARAMEDIC)",
    )
    certificate_url = Column(
        String(1024),
        nullable=False,
        doc="Static/Cloud storage URL to uploaded certificate document (PDF or image)",
    )

    # Verification Lifecycle Status
    status = Column(
        String(50),
        default="PENDING",
        nullable=False,
        index=True,
        doc="Verification status: 'PENDING', 'APPROVED', 'REJECTED'",
    )
    rejection_reason = Column(
        String(1024),
        nullable=True,
        doc="Administrative feedback explaining why a verification was rejected",
    )
    notes = Column(
        String(1024),
        nullable=True,
        doc="Optional user notes or credential ID/license numbers",
    )

    # Reviewer Reference & Timestamps
    reviewed_by = Column(
        PG_UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="SET NULL"),
        nullable=True,
        doc="Admin user ID who approved or rejected the verification",
    )
    submitted_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        doc="Timestamp when verification was submitted by the user",
    )
    reviewed_at = Column(
        DateTime(timezone=True),
        nullable=True,
        doc="Timestamp when admin reviewed (approved/rejected) the request",
    )

    # Relationships
    user = relationship(
        "User",
        foreign_keys=[user_id],
        backref="skill_verifications",
        lazy="joined",
    )
    reviewer = relationship(
        "User",
        foreign_keys=[reviewed_by],
        lazy="joined",
    )

    def __repr__(self) -> str:
        return f"<SkillVerification id={self.id} user_id={self.user_id} skill={self.skill_type} status={self.status}>"
