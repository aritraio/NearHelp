"""NearHelp AI — User Pydantic Schemas & DTOs."""

import uuid
from datetime import datetime
from typing import Any

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class EmergencyContactSchema(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    phone: str = Field(..., min_length=5, max_length=20)
    relationship: str = Field(..., min_length=1, max_length=50)


class SkillItemSchema(BaseModel):
    skill_type: str
    verified: bool = False
    certificate_url: str | None = None
    verified_at: datetime | None = None


class UserBase(BaseModel):
    email: EmailStr | None = None
    name: str | None = None
    photo_url: str | None = None
    phone: str | None = None
    blood_group: str | None = None
    languages: list[str] = Field(default_factory=lambda: ["en"])


class UserResponse(UserBase):
    id: uuid.UUID
    phone_verified: bool = False
    auth_provider: str = "email"
    is_anonymous: bool = False
    is_active: bool = True
    is_superuser: bool = False
    emergency_contacts: list[EmergencyContactSchema] = Field(default_factory=list)
    medical_conditions: list[Any] = Field(default_factory=list)
    known_allergies: list[Any] = Field(default_factory=list)
    skills: list[SkillItemSchema] = Field(default_factory=list)
    trust_score: float = 50.0
    badges: list[str] = Field(default_factory=list)
    fcm_token: str | None = None
    created_at: datetime
    updated_at: datetime
    last_login_at: datetime | None = None

    model_config = ConfigDict(from_attributes=True)


class UserUpdateRequest(BaseModel):
    name: str | None = None
    photo_url: str | None = None
    blood_group: str | None = None
    languages: list[str] | None = None
    emergency_contacts: list[EmergencyContactSchema] | None = None
    medical_conditions: list[Any] | None = None
    known_allergies: list[Any] | None = None


class DeviceRegisterRequest(BaseModel):
    fcm_token: str = Field(..., min_length=10, description="FCM push notification token")
    device_id: str | None = None
    platform: str | None = Field(default="android", description="'android', 'ios', or 'web'")
    device_info: dict[str, Any] | None = Field(default_factory=dict)
