"""Database-backed presence shared across API workers."""

from sqlalchemy import Column, DateTime, ForeignKey
from sqlalchemy.dialects.postgresql import UUID

from app.db.base import Base


class UserPresence(Base):
    __tablename__ = "user_presence"

    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    last_seen_at = Column(DateTime(timezone=True), nullable=False, index=True)
