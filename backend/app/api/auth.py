"""NearHelp AI — Authentication & Identity API Routes."""

from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_active_user
from app.db.session import get_db
from app.models.user import User
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
from app.schemas.user import DeviceRegisterRequest, UserResponse
from app.services.auth_service import AuthService

router = APIRouter(prefix="", tags=["Authentication & Identity"])


@router.post(
    "/register",
    response_model=TokenResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Register new user with email & password",
)
async def register(
    req: RegisterRequest,
    db: AsyncSession = Depends(get_db),
):
    """Create a new user account with email and password, returning JWT access & refresh tokens."""
    _, token_response = await AuthService.register_user(db, req)
    return token_response


@router.post(
    "/login",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Login with email & password",
)
async def login(
    req: LoginRequest,
    db: AsyncSession = Depends(get_db),
):
    """Authenticate user with email and password, returning 15-minute access token and 7-day refresh token."""
    _, token_response = await AuthService.login_user(db, req)
    return token_response


@router.post(
    "/google",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Sign in or register with Google OAuth / Firebase",
)
async def google_auth(
    req: GoogleAuthRequest,
    db: AsyncSession = Depends(get_db),
):
    """Authenticate or register user using Google OAuth via verified Firebase ID Token."""
    _, token_response = await AuthService.login_google(db, req)
    return token_response


@router.post(
    "/phone/send-otp",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Request Phone OTP Verification Code",
)
async def send_phone_otp(
    req: PhoneSendOtpRequest,
):
    """Initiate Phone OTP verification flow via Firebase Phone Auth gateway."""
    return MessageResponse(
        message=f"Verification OTP initiated for {req.phone_number}. Please verify with OTP code or Firebase ID token.",
        success=True,
    )


@router.post(
    "/phone/verify",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Verify Phone OTP & Authenticate",
)
async def verify_phone_otp(
    req: PhoneVerifyRequest,
    db: AsyncSession = Depends(get_db),
):
    """Verify phone OTP or Firebase phone token, logging in or creating a verified phone user."""
    _, token_response = await AuthService.verify_phone_auth(db, req)
    return token_response


@router.post(
    "/anonymous",
    response_model=TokenResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create Anonymous Emergency Session",
)
async def anonymous_session(
    req: AnonymousAuthRequest = AnonymousAuthRequest(),
    db: AsyncSession = Depends(get_db),
):
    """Generate a temporary anonymous session with a disposable ID and no PII stored for immediate 1-tap SOS triage."""
    _, token_response = await AuthService.create_anonymous_session(db, req)
    return token_response


@router.post(
    "/refresh",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Refresh Expired Access Token",
)
async def refresh_token(
    req: TokenRefreshRequest,
    db: AsyncSession = Depends(get_db),
):
    """Exchange a valid 7-day refresh token for a fresh 15-minute access token."""
    return await AuthService.refresh_access_token(db, req)


@router.post(
    "/device",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Register / Update Device FCM Token",
)
async def register_device(
    req: DeviceRegisterRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Register or update the FCM push notification token for the current authenticated user."""
    await AuthService.register_device(db, current_user, req)
    return MessageResponse(message="Device FCM token registered successfully.", success=True)


@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Authenticated User Profile",
)
async def get_me(
    current_user: User = Depends(get_current_active_user),
):
    """Retrieve identity profile of currently authenticated user from Bearer token."""
    return UserResponse.model_validate(current_user)
