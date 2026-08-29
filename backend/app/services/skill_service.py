"""NearHelp AI — Skill Verification & Trust Scoring Business Logic Service."""

import logging
import os
import uuid
from datetime import datetime

from fastapi import HTTPException, UploadFile, status
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.skill_verification import SkillVerification
from app.models.user import User
from app.schemas.skill import (
    SkillCertificateUploadResponse,
    SkillClaimRequest,
    SkillVerificationListResponse,
    SkillVerificationResponse,
)
from app.services.notification_service import NotificationService

logger = logging.getLogger(__name__)

ALLOWED_CERTIFICATE_TYPES = {
    "application/pdf",
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp",
}
MAX_CERTIFICATE_SIZE_BYTES = 10 * 1024 * 1024  # 10 MB
CERTIFICATE_UPLOAD_DIR = os.path.abspath(
    os.path.join(os.path.dirname(__file__), "../../uploads/certificates")
)

# Standard recognized skill types mapping to canonical codes
CANONICAL_SKILL_MAP = {
    "cpr": "CPR_CERTIFIED",
    "cpr_certified": "CPR_CERTIFIED",
    "cpr certified": "CPR_CERTIFIED",
    "doctor": "DOCTOR",
    "physician": "DOCTOR",
    "emt": "EMT",
    "emergency_medical_technician": "EMT",
    "nurse": "NURSE",
    "registered_nurse": "NURSE",
    "first_aid": "FIRST_AID",
    "first-aid": "FIRST_AID",
    "first aid": "FIRST_AID",
    "paramedic": "PARAMEDIC",
    "fire_safety": "FIRE_SAFETY",
    "lifeguard": "LIFEGUARD",
}


class SkillService:
    """Service managing skill claims, certificate uploads, admin review, and trust score increments."""

    @classmethod
    def normalize_skill_type(cls, skill_type: str) -> str:
        """Convert arbitrary skill string into canonical standard badge/skill code."""
        clean = skill_type.strip().lower().replace("-", "_")
        if clean in CANONICAL_SKILL_MAP:
            return CANONICAL_SKILL_MAP[clean]
        # Fallback to uppercase alphanumeric with underscores
        return skill_type.strip().upper().replace(" ", "_").replace("-", "_")

    @classmethod
    async def save_certificate_file(
        cls, user: User, file: UploadFile
    ) -> tuple[str, str, str]:
        """Validate and persist certificate document (PDF or image) to storage."""
        if file.content_type not in ALLOWED_CERTIFICATE_TYPES:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Unsupported certificate format '{file.content_type}'. Allowed: PDF, PNG, JPEG, WebP.",
            )

        content = await file.read()
        if len(content) > MAX_CERTIFICATE_SIZE_BYTES:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail="Certificate file exceeds maximum allowed limit of 10 MB.",
            )

        os.makedirs(CERTIFICATE_UPLOAD_DIR, exist_ok=True)
        ext = file.filename.split(".")[-1].lower() if file.filename and "." in file.filename else "pdf"
        filename = f"cert_{user.id}_{uuid.uuid4().hex[:8]}.{ext}"
        filepath = os.path.join(CERTIFICATE_UPLOAD_DIR, filename)

        with open(filepath, "wb") as f:
            f.write(content)

        certificate_url = f"/uploads/certificates/{filename}"
        return certificate_url, filename, file.content_type

    @classmethod
    async def upload_certificate(
        cls, user: User, file: UploadFile
    ) -> SkillCertificateUploadResponse:
        """Standalone certificate upload endpoint handler."""
        cert_url, filename, content_type = await cls.save_certificate_file(user, file)
        return SkillCertificateUploadResponse(
            certificate_url=cert_url,
            filename=filename,
            file_type=content_type,
            message="Certificate uploaded successfully.",
            success=True,
        )

    @classmethod
    def format_verification_response(
        cls, verification: SkillVerification
    ) -> SkillVerificationResponse:
        """Format SkillVerification model into DTO including user metadata."""
        user = verification.user
        return SkillVerificationResponse(
            id=verification.id,
            user_id=verification.user_id,
            user_name=user.name if user else None,
            user_email=user.email if user else None,
            user_phone=user.phone if user else None,
            skill_type=verification.skill_type,
            certificate_url=verification.certificate_url,
            status=verification.status,
            rejection_reason=verification.rejection_reason,
            notes=verification.notes,
            reviewed_by=verification.reviewed_by,
            submitted_at=verification.submitted_at,
            reviewed_at=verification.reviewed_at,
        )

    @classmethod
    async def claim_skill(
        cls,
        db: AsyncSession,
        user: User,
        req: SkillClaimRequest,
    ) -> SkillVerificationResponse:
        """Submit a skill claim with certificate reference."""
        skill_type = cls.normalize_skill_type(req.skill_type)

        if not req.certificate_url:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="A valid certificate_url or uploaded certificate file is required.",
            )

        # 1. Check if user already has this skill verified
        user_skills = list(user.skills or [])
        for s in user_skills:
            if isinstance(s, dict) and s.get("skill_type") == skill_type and s.get("verified") is True:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Skill '{skill_type}' is already verified on your profile.",
                )

        # 2. Check if a pending verification already exists
        stmt = select(SkillVerification).where(
            SkillVerification.user_id == user.id,
            SkillVerification.skill_type == skill_type,
            SkillVerification.status == "PENDING",
        )
        res = await db.execute(stmt)
        if res.scalars().first() is not None:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"A verification claim for '{skill_type}' is already pending review.",
            )

        # 3. Create verification record
        verification = SkillVerification(
            user_id=user.id,
            skill_type=skill_type,
            certificate_url=req.certificate_url,
            notes=req.notes,
            status="PENDING",
        )
        db.add(verification)

        # 4. Update user's skills list with unverified entry
        skill_found = False
        updated_skills = []
        for s in user_skills:
            if isinstance(s, dict) and s.get("skill_type") == skill_type:
                updated_skills.append({
                    "skill_type": skill_type,
                    "verified": False,
                    "certificate_url": req.certificate_url,
                    "verified_at": None,
                })
                skill_found = True
            else:
                updated_skills.append(s)

        if not skill_found:
            updated_skills.append({
                "skill_type": skill_type,
                "verified": False,
                "certificate_url": req.certificate_url,
                "verified_at": None,
            })

        user.skills = updated_skills
        await db.commit()
        await db.refresh(verification)
        await db.refresh(user)

        return cls.format_verification_response(verification)

    @classmethod
    async def list_user_verifications(
        cls, db: AsyncSession, user: User
    ) -> list[SkillVerificationResponse]:
        """List all verification requests submitted by the current user."""
        stmt = (
            select(SkillVerification)
            .where(SkillVerification.user_id == user.id)
            .order_by(SkillVerification.submitted_at.desc())
        )
        result = await db.execute(stmt)
        verifications = result.scalars().all()
        return [cls.format_verification_response(v) for v in verifications]

    @classmethod
    async def list_verification_queue(
        cls,
        db: AsyncSession,
        status_filter: str | None = None,
        limit: int = 50,
        offset: int = 0,
    ) -> SkillVerificationListResponse:
        """Admin query: list skill verification queue items with optional status filtering."""
        query = select(SkillVerification)
        count_query = select(func.count(SkillVerification.id))

        if status_filter and status_filter.upper() not in {"ALL", ""}:
            filter_val = status_filter.upper()
            query = query.where(SkillVerification.status == filter_val)
            count_query = count_query.where(SkillVerification.status == filter_val)

        total_res = await db.execute(count_query)
        total = total_res.scalar() or 0

        query = query.order_by(SkillVerification.submitted_at.desc()).offset(offset).limit(limit)
        res = await db.execute(query)
        verifications = res.scalars().all()

        formatted = [cls.format_verification_response(v) for v in verifications]
        return SkillVerificationListResponse(total=total, verifications=formatted)

    @classmethod
    async def get_verification_by_id(
        cls, db: AsyncSession, verification_id: uuid.UUID
    ) -> SkillVerificationResponse:
        """Retrieve single verification record by ID."""
        stmt = select(SkillVerification).where(SkillVerification.id == verification_id)
        res = await db.execute(stmt)
        verification = res.scalars().first()
        if not verification:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Skill verification request with id '{verification_id}' not found.",
            )
        return cls.format_verification_response(verification)

    @classmethod
    async def approve_verification(
        cls,
        db: AsyncSession,
        verification_id: uuid.UUID,
        admin_user: User,
        notes: str | None = None,
    ) -> SkillVerificationResponse:
        """Approve a skill verification, increment user trust score by +5, award badges, and notify user."""
        stmt = select(SkillVerification).where(SkillVerification.id == verification_id)
        res = await db.execute(stmt)
        verification = res.scalars().first()

        if not verification:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Skill verification request with id '{verification_id}' not found.",
            )

        if verification.status == "APPROVED":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="This skill claim has already been approved.",
            )

        # 1. Update verification record
        verification.status = "APPROVED"
        verification.reviewed_at = func.now()
        verification.reviewed_by = admin_user.id
        if notes is not None:
            verification.notes = notes

        # 2. Fetch target user
        user_stmt = select(User).where(User.id == verification.user_id)
        user_res = await db.execute(user_stmt)
        target_user = user_res.scalars().first()
        if not target_user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User associated with this verification request was not found.",
            )

        # 3. Trust Score Increment: +5.0 points (capped at 100.0)
        previous_trust = target_user.trust_score or 50.0
        new_trust = min(100.0, previous_trust + 5.0)
        target_user.trust_score = round(new_trust, 1)

        # 4. Update User Skills Array
        now_iso = datetime.utcnow().isoformat()
        skills = list(target_user.skills or [])
        skill_found = False
        updated_skills = []
        for s in skills:
            if isinstance(s, dict) and s.get("skill_type") == verification.skill_type:
                updated_skills.append({
                    "skill_type": verification.skill_type,
                    "verified": True,
                    "certificate_url": verification.certificate_url,
                    "verified_at": now_iso,
                })
                skill_found = True
            else:
                updated_skills.append(s)

        if not skill_found:
            updated_skills.append({
                "skill_type": verification.skill_type,
                "verified": True,
                "certificate_url": verification.certificate_url,
                "verified_at": now_iso,
            })
        target_user.skills = updated_skills

        # 5. Award Badges
        badges = list(target_user.badges or [])
        badge_name = verification.skill_type
        if badge_name not in badges:
            badges.append(badge_name)

        # Award community "VERIFIED_RESPONDER" badge if first skill or trust score >= 55.0
        if "VERIFIED_RESPONDER" not in badges:
            badges.append("VERIFIED_RESPONDER")

        target_user.badges = badges

        # 6. Dispatch Notification
        await NotificationService.notify_skill_approved(
            user=target_user,
            skill_type=verification.skill_type,
            new_trust_score=target_user.trust_score,
            badge=badge_name,
        )

        await db.commit()
        await db.refresh(verification)
        await db.refresh(target_user)

        return cls.format_verification_response(verification)

    @classmethod
    async def reject_verification(
        cls,
        db: AsyncSession,
        verification_id: uuid.UUID,
        admin_user: User,
        reason: str | None = None,
        notes: str | None = None,
    ) -> SkillVerificationResponse:
        """Reject a skill claim with feedback reason and notify user."""
        stmt = select(SkillVerification).where(SkillVerification.id == verification_id)
        res = await db.execute(stmt)
        verification = res.scalars().first()

        if not verification:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Skill verification request with id '{verification_id}' not found.",
            )

        if verification.status == "REJECTED":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="This skill claim has already been rejected.",
            )

        verification.status = "REJECTED"
        verification.rejection_reason = reason or "Submitted certification could not be verified."
        verification.reviewed_at = func.now()
        verification.reviewed_by = admin_user.id
        if notes is not None:
            verification.notes = notes

        # Fetch target user
        user_stmt = select(User).where(User.id == verification.user_id)
        user_res = await db.execute(user_stmt)
        target_user = user_res.scalars().first()

        if target_user:
            # Update user's skills array: mark unverified
            skills = list(target_user.skills or [])
            updated_skills = []
            for s in skills:
                if isinstance(s, dict) and s.get("skill_type") == verification.skill_type:
                    updated_skills.append({
                        "skill_type": verification.skill_type,
                        "verified": False,
                        "certificate_url": verification.certificate_url,
                        "verified_at": None,
                    })
                else:
                    updated_skills.append(s)
            target_user.skills = updated_skills

            # Dispatch notification
            await NotificationService.notify_skill_rejected(
                user=target_user,
                skill_type=verification.skill_type,
                reason=verification.rejection_reason,
            )

        await db.commit()
        await db.refresh(verification)
        return cls.format_verification_response(verification)
