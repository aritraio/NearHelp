"""NearHelp AI — Authentication Request and Response Schemas."""


from pydantic import BaseModel, ConfigDict, EmailStr, Field

from app.schemas.user import UserResponse


class RegisterRequest(BaseModel):
    email: EmailStr = Field(..., description="Valid user email address")
    password: str = Field(..., min_length=6, max_length=128, description="User password (min 6 chars)")
    name: str = Field(..., min_length=1, max_length=100, description="Full display name")
    phone: str | None = Field(None, description="Optional phone number in E.164 format")
    blood_group: str | None = Field(None, description="Optional blood group (e.g. A+, O-)")


class LoginRequest(BaseModel):
    email: EmailStr = Field(..., description="Registered user email")
    password: str = Field(..., min_length=1, description="User password")


class GoogleAuthRequest(BaseModel):
    id_token: str = Field(..., min_length=10, description="Firebase or Google OAuth ID token")


class PhoneSendOtpRequest(BaseModel):
    phone_number: str = Field(..., min_length=8, max_length=20, description="Phone number with country code")


class PhoneVerifyRequest(BaseModel):
    phone_number: str = Field(..., min_length=8, max_length=20, description="Phone number with country code")
    otp_code: str | None = Field(None, min_length=4, max_length=10, description="6-digit OTP code")
    id_token: str | None = Field(None, description="Firebase Phone Auth verification token")
    name: str | None = Field(None, description="Optional display name for newly created phone user")


class AnonymousAuthRequest(BaseModel):
    device_id: str | None = Field(None, description="Optional client device UUID")
    temp_name: str | None = Field("Anonymous Victim", description="Display name for emergency session")


class TokenRefreshRequest(BaseModel):
    refresh_token: str = Field(..., min_length=20, description="Valid 7-day refresh JWT token")


class TokenResponse(BaseModel):
    access_token: str = Field(..., description="Signed JWT access token (15-min expiry)")
    refresh_token: str = Field(..., description="Signed JWT refresh token (7-day expiry)")
    token_type: str = Field("bearer", description="Bearer token type")
    expires_in: int = Field(..., description="Access token lifetime in seconds")
    user: UserResponse = Field(..., description="Authenticated user profile")

    model_config = ConfigDict(from_attributes=True)


class MessageResponse(BaseModel):
    message: str
    success: bool = True
