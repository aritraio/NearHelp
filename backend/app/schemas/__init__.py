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
    EmergencyContactSchema,
    SkillItemSchema,
    UserResponse,
    UserUpdateRequest,
)

__all__ = [
    "AnonymousAuthRequest",
    "DeviceRegisterRequest",
    "EmergencyContactSchema",
    "GoogleAuthRequest",
    "LoginRequest",
    "MessageResponse",
    "PhoneSendOtpRequest",
    "PhoneVerifyRequest",
    "RegisterRequest",
    "SkillItemSchema",
    "TokenRefreshRequest",
    "TokenResponse",
    "UserResponse",
    "UserUpdateRequest",
]
