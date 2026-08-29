"""NearHelp AI — Schemas Registry."""

from app.schemas.auth import (
    AnonymousAuthRequest,
    GoogleAuthRequest,
    LoginRequest,
    MessageResponse,
    PhoneSendOtpRequest,
    PhoneVerifyRequest,
    RegisterRequest,
    TokenRefreshRequest,
    TokenResponse,
)
from app.schemas.user import (
    DeviceRegisterRequest,
    EmergencyContactCreateSchema,
    EmergencyContactSchema,
    EmergencyContactUpdateSchema,
    LanguagePreferencesRequest,
    MedicalIDResponse,
    MedicalIDUpdateRequest,
    PhotoUploadResponse,
    SkillItemSchema,
    UserResponse,
    UserUpdateRequest,
)

__all__ = [
    "AnonymousAuthRequest",
    "DeviceRegisterRequest",
    "EmergencyContactCreateSchema",
    "EmergencyContactSchema",
    "EmergencyContactUpdateSchema",
    "GoogleAuthRequest",
    "LanguagePreferencesRequest",
    "LoginRequest",
    "MedicalIDResponse",
    "MedicalIDUpdateRequest",
    "MessageResponse",
    "PhoneSendOtpRequest",
    "PhoneVerifyRequest",
    "PhotoUploadResponse",
    "RegisterRequest",
    "SkillItemSchema",
    "TokenRefreshRequest",
    "TokenResponse",
    "UserResponse",
    "UserUpdateRequest",
]
