"""NearHelp AI — Skill Verification & Certification Schemas."""

import uuid
from datetime import datetime
from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class SkillClaimRequest(BaseModel):
    """Payload for submitting a skill verification claim."""
    skill_type: str = Field(
        ...,
        min_length=2,
        max_length=100,
        description="Standardized skill code (e.g., 'CPR_CERTIFIED', 'DOCTOR', 'EMT', 'NURSE', 'FIRST_AID', 'PARAMEDIC')",
    )
    certificate_url: str | None = Field(
        None,
        description="URL/Path to uploaded certificate document if uploaded separately",
    )
    notes: str | None = Field(
        None,
        max_length=1000,
        description="Optional notes, license/registration number, or issuer details",
    )


class SkillVerificationResponse(BaseModel):
    """Detailed DTO for a skill verification record."""
    id: uuid.UUID
    user_id: uuid.UUID
    user_name: str | None = None
    user_email: str | None = None
    user_phone: str | None = None
    skill_type: str
    certificate_url: str
    status: str = "PENDING"
    rejection_reason: str | None = None
    notes: str | None = None
    reviewed_by: uuid.UUID | None = None
    submitted_at: datetime
    reviewed_at: datetime | None = None

    model_config = ConfigDict(from_attributes=True)


class SkillVerificationReviewRequest(BaseModel):
    """Admin request payload for approving or rejecting a skill claim."""
    action: str | None = Field(
        None,
        description="Review action: 'APPROVE' or 'REJECT'",
    )
    status: str | None = Field(
        None,
        description="Alternative status value: 'APPROVED' or 'REJECTED'",
    )
    rejection_reason: str | None = Field(
        None,
        max_length=1000,
        description="Explanation provided to user if claim is rejected",
    )
    notes: str | None = Field(
        None,
        max_length=1000,
        description="Administrative review notes",
    )


class SkillCertificateUploadResponse(BaseModel):
    """DTO returned upon successful certificate file upload."""
    certificate_url: str = Field(..., description="Static URL to access the uploaded certificate")
    filename: str = Field(..., description="Stored certificate filename")
    file_type: str = Field(..., description="MIME content type")
    message: str = Field(default="Certificate uploaded successfully.")
    success: bool = True


class TrustScoreUpdateResponse(BaseModel):
    """DTO summarizing trust score increment and badge attribution."""
    user_id: uuid.UUID
    previous_trust_score: float
    new_trust_score: float
    added_badges: list[str] = Field(default_factory=list)
    skills: list[dict[str, Any]] = Field(default_factory=list)
    message: str = "Trust score updated successfully."


class SkillVerificationListResponse(BaseModel):
    """Paginated list of verification queue entries."""
    total: int
    verifications: list[SkillVerificationResponse]
