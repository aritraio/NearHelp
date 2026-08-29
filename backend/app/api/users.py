"""NearHelp AI — User Profile & Preferences API Routes."""

from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_active_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.auth import MessageResponse
from app.schemas.user import DeviceRegisterRequest, UserResponse, UserUpdateRequest
from app.services.auth_service import AuthService

router = APIRouter(prefix="", tags=["User Profile"])


@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Current User Profile",
)
async def get_current_user_profile(
    current_user: User = Depends(get_current_active_user),
):
    """Retrieve full user profile of authenticated user."""
    return UserResponse.model_validate(current_user)


@router.put(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Update Current User Profile",
)
async def update_current_user_profile(
    req: UserUpdateRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Update profile attributes for authenticated user."""
    if req.name is not None:
        current_user.name = req.name
    if req.photo_url is not None:
        current_user.photo_url = req.photo_url
    if req.blood_group is not None:
        current_user.blood_group = req.blood_group
    if req.languages is not None:
        current_user.languages = req.languages
    if req.emergency_contacts is not None:
        current_user.emergency_contacts = [contact.model_dump() for contact in req.emergency_contacts]
    if req.medical_conditions is not None:
        current_user.medical_conditions = req.medical_conditions
    if req.known_allergies is not None:
        current_user.known_allergies = req.known_allergies

    await db.commit()
    await db.refresh(current_user)
    return UserResponse.model_validate(current_user)


@router.post(
    "/me/fcm-token",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Register / Update FCM Device Push Token",
)
async def update_fcm_token(
    req: DeviceRegisterRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Register or refresh the Firebase Cloud Messaging device push token."""
    await AuthService.register_device(db, current_user, req)
    return MessageResponse(message="FCM token registered successfully.", success=True)
