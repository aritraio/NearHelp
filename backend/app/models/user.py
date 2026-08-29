"""NearHelp AI — User Model Definition."""

import uuid

from geoalchemy2 import Geometry
from sqlalchemy import (
    JSON,
    Boolean,
    Column,
    DateTime,
    Float,
    String,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.db.base import Base


class User(Base):
    """User account entity for NearHelp AI.
    
    Supports Email/Password, Google OAuth, Phone OTP, and Disposable Anonymous Emergency Mode.
    """
    __tablename__ = "users"

    # Primary Key
    id = Column(
        PG_UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True,
        doc="Unique user identifier (UUID v4)",
    )

    # Core Identity Fields
    email = Column(
        String(255),
        unique=True,
        index=True,
        nullable=True,
        doc="User email address (null for anonymous/phone-only users)",
    )
    hashed_password = Column(
        String(255),
        nullable=True,
        doc="Bcrypt password hash (null for OAuth/Phone/Anonymous users)",
    )
    name = Column(
        String(255),
        nullable=True,
        doc="Full display name of the user",
    )
    photo_url = Column(
        String(1024),
        nullable=True,
        doc="Avatar or profile photo URL",
    )
    phone = Column(
        String(50),
        unique=True,
        index=True,
        nullable=True,
        doc="Primary phone number (E.164 format)",
    )
    phone_verified = Column(
        Boolean,
        default=False,
        nullable=False,
        doc="Indicates whether the phone number has been verified via OTP",
    )

    # Authentication Provider & Firebase Linking
    firebase_uid = Column(
        String(255),
        unique=True,
        index=True,
        nullable=True,
        doc="External Firebase UID for OAuth/Phone linking",
    )
    auth_provider = Column(
        String(50),
        default="email",
        nullable=False,
        doc="Authentication provider: 'email', 'google', 'phone', 'anonymous'",
    )
    is_anonymous = Column(
        Boolean,
        default=False,
        index=True,
        nullable=False,
        doc="Flags temporary disposable session created during anonymous SOS",
    )
    is_active = Column(
        Boolean,
        default=True,
        nullable=False,
        doc="Whether the user account is active and permitted to login",
    )
    is_superuser = Column(
        Boolean,
        default=False,
        nullable=False,
        doc="Platform administrator flag",
    )

    # Profile, Encrypted Medical ID & Emergency Data
    blood_group = Column(
        String(10),
        nullable=True,
        doc="Blood type (e.g., A+, O-, etc.)",
    )
    medical_conditions = Column(
        JSON,
        default=list,
        nullable=False,
        doc="Encrypted medical conditions or descriptors",
    )
    known_allergies = Column(
        JSON,
        default=list,
        nullable=False,
        doc="Encrypted known allergies list",
    )
    emergency_contacts = Column(
        JSON,
        default=list,
        nullable=False,
        doc="List of emergency contacts: [{name, phone, relationship}] (max 5)",
    )
    languages = Column(
        JSON,
        default=lambda: ["en"],
        nullable=False,
        doc="Spoken languages (ISO 639-1 codes)",
    )

    # Skill Verification & Trust Scoring
    skills = Column(
        JSON,
        default=list,
        nullable=False,
        doc="Claimed & verified skills: [{skill_type, verified, certificate_url, verified_at}]",
    )
    trust_score = Column(
        Float,
        default=50.0,
        nullable=False,
        doc="Community reputation trust score (0.0 to 100.0, default 50.0)",
    )
    badges = Column(
        JSON,
        default=list,
        nullable=False,
        doc="User achievement badges (e.g. ['CPR_CERTIFIED', 'FIRST_RESPONDER'])",
    )

    # Push Notification & Device Management
    fcm_token = Column(
        String(512),
        nullable=True,
        doc="Firebase Cloud Messaging device token for push notifications",
    )
    device_info = Column(
        JSON,
        nullable=True,
        doc="Metadata about registered device (OS, model, app version)",
    )

    # Geospatial Location (Updated during active SOS response only)
    location = Column(
        Geometry(geometry_type="POINT", srid=4326),
        nullable=True,
        doc="PostGIS GPS Point coordinates (WGS84 4326)",
    )

    # Audit Timestamps
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
        doc="Account creation timestamp",
    )
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
        doc="Last profile update timestamp",
    )
    last_login_at = Column(
        DateTime(timezone=True),
        nullable=True,
        doc="Last successful authentication timestamp",
    )

    def __repr__(self) -> str:
        return f"<User id={self.id} email={self.email} provider={self.auth_provider} anonymous={self.is_anonymous}>"
