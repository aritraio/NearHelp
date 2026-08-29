"""NearHelp AI — User Profile, Encrypted Medical ID & Emergency Contact Schemas."""

import uuid
from datetime import datetime
from typing import Any

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class EmergencyContactCreateSchema(BaseModel):
    """Payload for registering a new emergency contact."""
    name: str = Field(..., min_length=1, max_length=100, description="Full contact name")
    phone: str = Field(..., min_length=5, max_length=20, description="Contact phone number (E.164 format)")
    relationship: str = Field(..., min_length=1, max_length=50, description="Relationship to user (e.g. 'Mother', 'Spouse', 'Doctor')")
    is_primary: bool = Field(default=False, description="Whether this is the primary emergency contact")


class EmergencyContactUpdateSchema(BaseModel):
    """Payload for updating an existing emergency contact."""
    name: str | None = Field(None, min_length=1, max_length=100)
    phone: str | None = Field(None, min_length=5, max_length=20)
    relationship: str | None = Field(None, min_length=1, max_length=50)
    is_primary: bool | None = None


class EmergencyContactSchema(BaseModel):
    """Emergency contact record with unique identifier."""
    id: str = Field(default_factory=lambda: str(uuid.uuid4()), description="Contact unique ID")
    name: str = Field(..., min_length=1, max_length=100)
    phone: str = Field(..., min_length=5, max_length=20)
    relationship: str = Field(..., min_length=1, max_length=50)
    is_primary: bool = Field(default=False)

    model_config = ConfigDict(from_attributes=True)


class SkillItemSchema(BaseModel):
    skill_type: str
    verified: bool = False
    certificate_url: str | None = None
    verified_at: datetime | None = None


class MedicalIDResponse(BaseModel):
    """Encrypted Medical ID Card DTO (Decrypted for authorized view)."""
    blood_group: str | None = Field(None, description="Blood type (e.g., A+, O-, B+)")
    medical_conditions: list[str] = Field(default_factory=list, description="Decrypted active medical conditions")
    known_allergies: list[str] = Field(default_factory=list, description="Decrypted known allergies")
    has_pacemaker: bool = Field(default=False, description="Cardiac pacemaker flag")
    is_organ_donor: bool = Field(default=False, description="Organ donor registration status")
    medical_notes: str | None = Field(None, description="Decrypted clinical directives or physician notes")
    emergency_contacts: list[EmergencyContactSchema] = Field(default_factory=list, description="Emergency kin contacts (max 5)")
    is_encrypted_at_rest: bool = Field(default=True, description="Confirms AES-256-GCM storage encryption in database")

    model_config = ConfigDict(from_attributes=True)


class MedicalIDUpdateRequest(BaseModel):
    """Payload for updating medical ID fields (automatically encrypted at rest)."""
    blood_group: str | None = Field(None, description="Blood type (e.g., A+, O-)")
    medical_conditions: list[str] | None = Field(None, description="List of medical conditions (e.g. Asthma, Diabetes)")
    known_allergies: list[str] | None = Field(None, description="List of allergies (e.g. Penicillin, Peanuts)")
    has_pacemaker: bool | None = Field(None, description="Whether user has a pacemaker")
    is_organ_donor: bool | None = Field(None, description="Whether user is an organ donor")
    medical_notes: str | None = Field(None, max_length=2000, description="Physician directives or emergency notes")


class LanguagePreferencesRequest(BaseModel):
    """Payload for updating spoken language preferences."""
    languages: list[str] = Field(..., min_length=1, description="List of ISO 639-1 language codes (e.g., ['en', 'bn', 'hi'])")


class PhotoUploadResponse(BaseModel):
    photo_url: str = Field(..., description="Public or static URL to uploaded avatar image")
    message: str = Field(default="Profile photo uploaded successfully.")
    success: bool = True


class UserBase(BaseModel):
    email: EmailStr | None = None
    name: str | None = None
    photo_url: str | None = None
    phone: str | None = None
    blood_group: str | None = None
    languages: list[str] = Field(default_factory=lambda: ["en"])


class UserResponse(UserBase):
    """Comprehensive user profile DTO with decrypted medical data."""
    id: uuid.UUID
    phone_verified: bool = False
    auth_provider: str = "email"
    is_anonymous: bool = False
    is_active: bool = True
    is_superuser: bool = False
    has_pacemaker: bool = False
    is_organ_donor: bool = False
    medical_notes: str | None = None
    medical_conditions: list[str] = Field(default_factory=list)
    known_allergies: list[str] = Field(default_factory=list)
    emergency_contacts: list[EmergencyContactSchema] = Field(default_factory=list)
    skills: list[SkillItemSchema] = Field(default_factory=list)
    trust_score: float = 50.0
    badges: list[str] = Field(default_factory=list)
    fcm_token: str | None = None
    created_at: datetime
    updated_at: datetime
    last_login_at: datetime | None = None

    model_config = ConfigDict(from_attributes=True)


class UserUpdateRequest(BaseModel):
    """General profile update payload."""
    name: str | None = None
    phone: str | None = None
    photo_url: str | None = None
    blood_group: str | None = None
    languages: list[str] | None = None
    emergency_contacts: list[EmergencyContactSchema] | None = None
    medical_conditions: list[str] | None = None
    known_allergies: list[str] | None = None
    has_pacemaker: bool | None = None
    is_organ_donor: bool | None = None
    medical_notes: str | None = None


class DeviceRegisterRequest(BaseModel):
    fcm_token: str = Field(..., min_length=10, description="FCM push notification token")
    device_id: str | None = None
    platform: str | None = Field(default="android", description="'android', 'ios', or 'web'")
    device_info: dict[str, Any] | None = Field(default_factory=dict)
