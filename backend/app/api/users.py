"""NearHelp AI — User Profile, Encrypted Medical ID & Emergency Contacts API Routes."""

from fastapi import APIRouter, Depends, File, Form, UploadFile, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_active_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.auth import MessageResponse
from app.schemas.skill import (
    SkillCertificateUploadResponse,
    SkillClaimRequest,
    SkillVerificationResponse,
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
    UserResponse,
    UserUpdateRequest,
)
from app.services.auth_service import AuthService
from app.services.skill_service import SkillService
from app.services.user_service import UserService

router = APIRouter(prefix="", tags=["User Profile & Encrypted Medical ID"])


# ==============================================================================
# 1. User Profile CRUD
# ==============================================================================
@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Current User Profile & Decrypted Medical ID",
)
async def get_current_user_profile(
    current_user: User = Depends(get_current_active_user),
):
    """Retrieve the full user profile including decrypted health data and emergency contacts."""
    return UserService.format_user_response(current_user)


@router.put(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Update Full User Profile",
)
async def update_current_user_profile(
    req: UserUpdateRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Update profile attributes for authenticated user (automatically encrypts medical fields at rest)."""
    return await UserService.update_profile(db, current_user, req)


@router.patch(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Partial Update User Profile",
)
async def patch_current_user_profile(
    req: UserUpdateRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Partially update user profile attributes."""
    return await UserService.update_profile(db, current_user, req)


# ==============================================================================
# 2. Encrypted Medical ID Sub-Resource
# ==============================================================================
@router.get(
    "/me/medical",
    response_model=MedicalIDResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Decrypted Medical ID Card",
)
async def get_medical_id(
    current_user: User = Depends(get_current_active_user),
):
    """Retrieve decrypted Medical ID (Blood Group, Conditions, Allergies, Pacemaker, Organ Donor, Notes)."""
    return UserService.get_medical_id(current_user)


@router.patch(
    "/me/medical",
    response_model=MedicalIDResponse,
    status_code=status.HTTP_200_OK,
    summary="Patch Encrypted Medical ID",
)
async def patch_medical_id(
    req: MedicalIDUpdateRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Update Medical ID attributes with AES-256-GCM encryption at rest."""
    return await UserService.update_medical_id(db, current_user, req)


@router.put(
    "/me/medical",
    response_model=MedicalIDResponse,
    status_code=status.HTTP_200_OK,
    summary="Replace Encrypted Medical ID",
)
async def put_medical_id(
    req: MedicalIDUpdateRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Replace/update Medical ID attributes with AES-256-GCM encryption at rest."""
    return await UserService.update_medical_id(db, current_user, req)


# ==============================================================================
# 3. Emergency Contacts (Max 5 Kin Contacts)
# ==============================================================================
@router.get(
    "/me/contacts",
    response_model=list[EmergencyContactSchema],
    status_code=status.HTTP_200_OK,
    summary="List Emergency Contacts",
)
async def list_emergency_contacts(
    current_user: User = Depends(get_current_active_user),
):
    """List all registered emergency contacts for the authenticated user (max 5)."""
    return UserService.list_contacts(current_user)


@router.post(
    "/me/contacts",
    response_model=EmergencyContactSchema,
    status_code=status.HTTP_201_CREATED,
    summary="Add Emergency Contact",
)
async def add_emergency_contact(
    req: EmergencyContactCreateSchema,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Register a new emergency contact (enforces max 5 contacts limit)."""
    return await UserService.add_contact(db, current_user, req)


@router.put(
    "/me/contacts/{contact_id}",
    response_model=EmergencyContactSchema,
    status_code=status.HTTP_200_OK,
    summary="Update Emergency Contact",
)
async def update_emergency_contact(
    contact_id: str,
    req: EmergencyContactUpdateSchema,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Update details of an existing emergency contact."""
    return await UserService.update_contact(db, current_user, contact_id, req)


@router.delete(
    "/me/contacts/{contact_id}",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Delete Emergency Contact",
)
async def delete_emergency_contact(
    contact_id: str,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Remove an emergency contact by ID."""
    await UserService.delete_contact(db, current_user, contact_id)
    return MessageResponse(message=f"Emergency contact '{contact_id}' deleted successfully.", success=True)


# ==============================================================================
# 4. Profile Photo & Media Upload
# ==============================================================================
@router.post(
    "/me/photo",
    response_model=PhotoUploadResponse,
    status_code=status.HTTP_200_OK,
    summary="Upload Profile Picture",
)
@router.post(
    "/me/avatar",
    response_model=PhotoUploadResponse,
    status_code=status.HTTP_200_OK,
    summary="Upload Avatar (Alias)",
    include_in_schema=False,
)
async def upload_profile_photo(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Upload user avatar image (JPEG, PNG, WebP up to 5 MB)."""
    photo_url = await UserService.save_profile_photo(db, current_user, file)
    return PhotoUploadResponse(photo_url=photo_url)


# ==============================================================================
# 5. Language Preferences & FCM Token
# ==============================================================================
@router.put(
    "/me/languages",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Update Language Preferences",
)
async def update_language_preferences(
    req: LanguagePreferencesRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Update spoken language ISO 639-1 preferences."""
    UserService.validate_languages(req.languages)
    current_user.languages = req.languages
    await db.commit()
    await db.refresh(current_user)
    return UserService.format_user_response(current_user)


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


# ==============================================================================
# 6. Skill Verification & Certificate Claims (Module 3)
# ==============================================================================
@router.post(
    "/me/skills",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Submit Skill Verification Claim (JSON)",
)
async def claim_skill_json(
    req: SkillClaimRequest,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Submit a new skill claim with an existing certificate URL."""
    return await SkillService.claim_skill(db, current_user, req)


@router.post(
    "/me/skills/upload",
    response_model=SkillCertificateUploadResponse,
    status_code=status.HTTP_200_OK,
    summary="Upload Certificate Document (PDF or Image)",
)
async def upload_certificate_file(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user),
):
    """Upload a certificate file (PDF, PNG, JPG, WebP up to 10 MB) to receive a certificate URL."""
    return await SkillService.upload_certificate(current_user, file)


@router.post(
    "/me/skills/form",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Submit Skill Claim with File Attachment (Multipart Form)",
)
async def claim_skill_form(
    skill_type: str = Form(..., description="Skill name or code e.g. CPR_CERTIFIED"),
    file: UploadFile = File(..., description="Certificate PDF or image"),
    notes: str | None = Form(None, description="Optional notes"),
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Upload certificate and submit skill verification claim in one single multipart request."""
    cert_url, _, _ = await SkillService.save_certificate_file(current_user, file)
    claim_req = SkillClaimRequest(
        skill_type=skill_type,
        certificate_url=cert_url,
        notes=notes,
    )
    return await SkillService.claim_skill(db, current_user, claim_req)


@router.get(
    "/me/skills",
    response_model=list[SkillVerificationResponse],
    status_code=status.HTTP_200_OK,
    summary="List My Skill Verification Requests",
)
async def list_my_skills(
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve history of all skill verification requests submitted by current user."""
    return await SkillService.list_user_verifications(db, current_user)

