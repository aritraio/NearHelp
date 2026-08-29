"""NearHelp AI — Admin Command Center & Skill Verification Queue API Routes."""

import uuid

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_current_admin_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.skill import (
    SkillVerificationListResponse,
    SkillVerificationResponse,
    SkillVerificationReviewRequest,
)
from app.services.skill_service import SkillService

router = APIRouter(prefix="", tags=["Admin Verification & Management"])


# ==============================================================================
# 1. Admin Verification Queue Listing & Search
# ==============================================================================
@router.get(
    "/verification-queue",
    response_model=SkillVerificationListResponse,
    status_code=status.HTTP_200_OK,
    summary="List Skill Verification Queue (Admin)",
)
@router.get(
    "/verifications",
    response_model=SkillVerificationListResponse,
    status_code=status.HTTP_200_OK,
    summary="List Skill Verification Queue (Alias)",
    include_in_schema=False,
)
async def list_verification_queue(
    status_filter: str | None = Query(
        None,
        alias="status",
        description="Filter status: 'PENDING', 'APPROVED', 'REJECTED', or 'ALL'",
    ),
    limit: int = Query(50, ge=1, le=100),
    offset: int = Query(0, ge=0),
    current_admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve all pending or historical skill verification requests submitted by community members."""
    return await SkillService.list_verification_queue(
        db, status_filter=status_filter, limit=limit, offset=offset
    )


@router.get(
    "/verification-queue/{verification_id}",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Verification Request Details (Admin)",
)
@router.get(
    "/verifications/{verification_id}",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Verification Details (Alias)",
    include_in_schema=False,
)
async def get_verification_detail(
    verification_id: uuid.UUID,
    current_admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve details for a single skill verification record."""
    return await SkillService.get_verification_by_id(db, verification_id)


# ==============================================================================
# 2. Approve Skill Verification
# ==============================================================================
@router.post(
    "/verification-queue/{verification_id}/approve",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Approve Skill Verification (Admin)",
)
async def approve_verification(
    verification_id: uuid.UUID,
    req: SkillVerificationReviewRequest | None = None,
    current_admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Approve a skill verification claim:
    
    1. Sets status to 'APPROVED' with admin reviewer attribution.
    2. Increments the user's community Trust Score by +5.0 points (max 100.0).
    3. Awards corresponding skill badge (e.g. CPR_CERTIFIED, DOCTOR, EMT, etc.) and VERIFIED_RESPONDER.
    4. Updates user's verified skills array with timestamp.
    5. Dispatches push/in-app notification to the user.
    """
    notes = req.notes if req else None
    return await SkillService.approve_verification(
        db, verification_id, current_admin, notes=notes
    )


# ==============================================================================
# 3. Reject Skill Verification
# ==============================================================================
@router.post(
    "/verification-queue/{verification_id}/reject",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Reject Skill Verification (Admin)",
)
async def reject_verification(
    verification_id: uuid.UUID,
    req: SkillVerificationReviewRequest | None = None,
    current_admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Reject a skill claim and record feedback reason for user notification."""
    reason = req.rejection_reason if req else None
    notes = req.notes if req else None
    return await SkillService.reject_verification(
        db, verification_id, current_admin, reason=reason, notes=notes
    )


# ==============================================================================
# 4. Universal Review Handler (PUT)
# ==============================================================================
@router.put(
    "/verifications/{verification_id}",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Review Skill Verification (PUT)",
)
@router.put(
    "/verification-queue/{verification_id}",
    response_model=SkillVerificationResponse,
    status_code=status.HTTP_200_OK,
    summary="Review Skill Verification Queue (PUT Alias)",
    include_in_schema=False,
)
async def review_verification_put(
    verification_id: uuid.UUID,
    req: SkillVerificationReviewRequest,
    current_admin: User = Depends(get_current_admin_user),
    db: AsyncSession = Depends(get_db),
):
    """Update verification status via PUT review payload ('APPROVE' or 'REJECT')."""
    action = (req.action or req.status or "APPROVE").upper()
    if action in {"APPROVE", "APPROVED"}:
        return await SkillService.approve_verification(
            db, verification_id, current_admin, notes=req.notes
        )
    else:
        return await SkillService.reject_verification(
            db, verification_id, current_admin, reason=req.rejection_reason, notes=req.notes
        )
