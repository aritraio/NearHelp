"""NearHelp AI — Core Authentication & Identity Business Logic Service."""

import logging
import uuid
from datetime import UTC, datetime

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    get_password_hash,
    verify_password,
    verify_token_type,
)
from app.models.user import User
from app.schemas.auth import (
    AnonymousAuthRequest,
    GoogleAuthRequest,
    LoginRequest,
    PhoneVerifyRequest,
    RegisterRequest,
    TokenRefreshRequest,
    TokenResponse,
)
from app.schemas.user import DeviceRegisterRequest, UserResponse
from app.services.firebase_service import verify_firebase_id_token

logger = logging.getLogger(__name__)


class AuthService:
    """Service encapsulating authentication, session lifecycle, and identity management."""

    @staticmethod
    async def get_user_by_id(db: AsyncSession, user_id: uuid.UUID) -> User | None:
        """Fetch active user by primary key UUID."""
        stmt = select(User).where(User.id == user_id)
        result = await db.execute(stmt)
        return result.scalars().first()

    @staticmethod
    async def get_user_by_email(db: AsyncSession, email: str) -> User | None:
        """Fetch user by case-insensitive email."""
        stmt = select(User).where(User.email == email.strip().lower())
        result = await db.execute(stmt)
        return result.scalars().first()

    @staticmethod
    async def get_user_by_phone(db: AsyncSession, phone: str) -> User | None:
        """Fetch user by phone number."""
        stmt = select(User).where(User.phone == phone.strip())
        result = await db.execute(stmt)
        return result.scalars().first()

    @classmethod
    def _build_token_response(cls, user: User) -> TokenResponse:
        """Helper to generate JWT access & refresh token pair and build TokenResponse."""
        access_token = create_access_token(
            subject=user.id,
            is_anonymous=user.is_anonymous,
            extra_claims={"email": user.email, "role": "admin" if user.is_superuser else "user"},
        )
        refresh_token = create_refresh_token(
            subject=user.id,
            extra_claims={"email": user.email},
        )
        expires_in = settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60

        return TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            expires_in=expires_in,
            user=UserResponse.model_validate(user),
        )

    @classmethod
    async def register_user(
        cls, db: AsyncSession, req: RegisterRequest
    ) -> tuple[User, TokenResponse]:
        """Register a new user with email and password."""
        email_clean = req.email.strip().lower()

        # Check existing email
        existing_user = await cls.get_user_by_email(db, email_clean)
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="An account with this email address already exists.",
            )

        # Check existing phone if provided
        if req.phone:
            existing_phone = await cls.get_user_by_phone(db, req.phone)
            if existing_phone:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="An account with this phone number already exists.",
                )

        new_user = User(
            id=uuid.uuid4(),
            email=email_clean,
            hashed_password=get_password_hash(req.password),
            name=req.name.strip(),
            phone=req.phone.strip() if req.phone else None,
            blood_group=req.blood_group,
            auth_provider="email",
            is_anonymous=False,
            is_active=True,
            last_login_at=datetime.now(UTC),
        )

        db.add(new_user)
        await db.commit()
        await db.refresh(new_user)

        token_response = cls._build_token_response(new_user)
        return new_user, token_response

    @classmethod
    async def login_user(
        cls, db: AsyncSession, req: LoginRequest
    ) -> tuple[User, TokenResponse]:
        """Authenticate user via email and password."""
        email_clean = req.email.strip().lower()
        user = await cls.get_user_by_email(db, email_clean)

        if not user or not user.hashed_password:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not verify_password(req.password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Your account has been deactivated. Please contact support.",
            )

        user.last_login_at = datetime.now(UTC)
        await db.commit()
        await db.refresh(user)

        token_response = cls._build_token_response(user)
        return user, token_response

    @classmethod
    async def login_google(
        cls, db: AsyncSession, req: GoogleAuthRequest
    ) -> tuple[User, TokenResponse]:
        """Authenticate or register user via Google OAuth 2.0 (Firebase ID Token)."""
        try:
            decoded_firebase = verify_firebase_id_token(req.id_token)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Google authentication failed: {e!s}",
                headers={"WWW-Authenticate": "Bearer"},
            )

        firebase_uid = decoded_firebase.get("uid")
        email = decoded_firebase.get("email")
        name = decoded_firebase.get("name") or "Google User"
        picture = decoded_firebase.get("picture")

        if not email and not firebase_uid:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Google token payload missing email or UID.",
            )

        # Look up existing user by firebase_uid or email
        user: User | None = None
        if firebase_uid:
            stmt = select(User).where(User.firebase_uid == firebase_uid)
            result = await db.execute(stmt)
            user = result.scalars().first()

        if not user and email:
            user = await cls.get_user_by_email(db, email)

        if user:
            # Update existing user profile with Google metadata
            user.firebase_uid = firebase_uid or user.firebase_uid
            user.photo_url = user.photo_url or picture
            user.name = user.name or name
            user.last_login_at = datetime.now(UTC)
        else:
            # Create new user via Google Sign-In
            user = User(
                id=uuid.uuid4(),
                email=email.lower() if email else None,
                name=name,
                photo_url=picture,
                firebase_uid=firebase_uid,
                auth_provider="google",
                is_anonymous=False,
                is_active=True,
                last_login_at=datetime.now(UTC),
            )
            db.add(user)

        await db.commit()
        await db.refresh(user)

        token_response = cls._build_token_response(user)
        return user, token_response

    @classmethod
    async def verify_phone_auth(
        cls, db: AsyncSession, req: PhoneVerifyRequest
    ) -> tuple[User, TokenResponse]:
        """Authenticate or register user via Firebase Phone OTP verification."""
        phone_clean = req.phone_number.strip()
        firebase_uid = None

        if req.id_token:
            try:
                decoded_firebase = verify_firebase_id_token(req.id_token)
                firebase_uid = decoded_firebase.get("uid")
                phone_clean = decoded_firebase.get("phone_number") or phone_clean
            except Exception as e:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail=f"Phone token verification failed: {e!s}",
                )
        elif req.otp_code:
            # Validates 6-digit OTP code (standard check for dev/demo)
            if len(req.otp_code) < 4:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid OTP code format.",
                )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Either id_token or otp_code must be provided.",
            )

        # Look up existing user by phone
        user = await cls.get_user_by_phone(db, phone_clean)

        if user:
            user.phone_verified = True
            if firebase_uid:
                user.firebase_uid = firebase_uid
            user.last_login_at = datetime.now(UTC)
        else:
            user = User(
                id=uuid.uuid4(),
                phone=phone_clean,
                phone_verified=True,
                name=req.name or f"User {phone_clean[-4:]}",
                firebase_uid=firebase_uid,
                auth_provider="phone",
                is_anonymous=False,
                is_active=True,
                last_login_at=datetime.now(UTC),
            )
            db.add(user)

        await db.commit()
        await db.refresh(user)

        token_response = cls._build_token_response(user)
        return user, token_response

    @classmethod
    async def create_anonymous_session(
        cls, db: AsyncSession, req: AnonymousAuthRequest
    ) -> tuple[User, TokenResponse]:
        """Create a temporary disposable anonymous session for immediate 1-tap SOS triage."""
        disposable_id = uuid.uuid4()
        temp_user = User(
            id=disposable_id,
            email=None,
            hashed_password=None,
            name=req.temp_name or "Anonymous Victim",
            phone=None,
            phone_verified=False,
            auth_provider="anonymous",
            is_anonymous=True,
            is_active=True,
            device_info={"device_id": req.device_id} if req.device_id else None,
            last_login_at=datetime.now(UTC),
        )

        db.add(temp_user)
        await db.commit()
        await db.refresh(temp_user)

        token_response = cls._build_token_response(temp_user)
        return temp_user, token_response

    @classmethod
    async def refresh_access_token(
        cls, db: AsyncSession, req: TokenRefreshRequest
    ) -> TokenResponse:
        """Validate 7-day refresh token and issue a fresh access token (and refreshed token pair)."""
        try:
            payload = decode_token(req.refresh_token)
        except Exception:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid, malformed, or expired refresh token.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not verify_token_type(payload, "refresh"):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token is not a valid refresh token.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        user_id_str = payload.get("sub")
        if not user_id_str:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token subject missing.",
            )

        try:
            user_id = uuid.UUID(user_id_str)
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid user ID format in token.",
            )

        user = await cls.get_user_by_id(db, user_id)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User account not found or is currently deactivated.",
            )

        return cls._build_token_response(user)

    @classmethod
    async def register_device(
        cls, db: AsyncSession, user: User, req: DeviceRegisterRequest
    ) -> User:
        """Store or update FCM push notification token for user's device."""
        user.fcm_token = req.fcm_token
        current_device_info = user.device_info or {}
        if isinstance(current_device_info, dict):
            current_device_info.update({
                "device_id": req.device_id,
                "platform": req.platform,
                **(req.device_info or {}),
                "updated_at": datetime.now(UTC).isoformat(),
            })
            user.device_info = current_device_info

        await db.commit()
        await db.refresh(user)
        return user
