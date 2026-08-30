"""NearHelp AI — Module 3 Skill Verification & Admin Queue Test Suite."""

import io
import uuid

import pytest
from httpx import AsyncClient
from sqlalchemy import select

from app.models.user import User
from app.services.notification_service import DISPATCHED_NOTIFICATIONS


async def _create_user(client: AsyncClient, is_superuser: bool = False) -> tuple[dict, dict]:
    """Helper to create a user and return authorization headers and user data."""
    email = f"user_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    payload = {
        "email": email,
        "password": "SecurePassword123!",
        "name": "Dr. Sarah Chen" if is_superuser else "John Doe (Volunteer)",
        "phone": f"+9198{uuid.uuid4().int % 100000000:08d}",
        "blood_group": "A+",
    }
    res = await client.post("/api/v1/auth/register", json=payload)
    assert res.status_code == 201
    data = res.json()
    token = data["access_token"]
    user_info = data["user"]

    if is_superuser:
        # Update user to superuser directly
        try:
            from tests.conftest import TestingSessionLocal
            session_factory = TestingSessionLocal
        except ImportError:
            from app.db.session import AsyncSessionLocal
            session_factory = AsyncSessionLocal
        async with session_factory() as session:
            stmt = select(User).where(User.id == uuid.UUID(user_info["id"]))
            db_res = await session.execute(stmt)
            db_user = db_res.scalars().first()
            if db_user:
                db_user.is_superuser = True
                await session.commit()

    headers = {"Authorization": f"Bearer {token}"}
    return headers, user_info


@pytest.mark.asyncio
async def test_skill_claim_unauthorized(client: AsyncClient):
    """Test accessing skill endpoints without valid JWT returns 401."""
    res = await client.post("/api/v1/users/me/skills", json={"skill_type": "CPR_CERTIFIED"})
    assert res.status_code == 401


@pytest.mark.asyncio
async def test_admin_queue_forbidden_for_regular_user(client: AsyncClient):
    """Test that a non-admin user cannot access admin verification queue (returns 403)."""
    user_headers, _ = await _create_user(client, is_superuser=False)
    res = await client.get("/api/v1/admin/verification-queue", headers=user_headers)
    assert res.status_code == 403
    assert "admin" in res.json()["detail"].lower()


@pytest.mark.asyncio
async def test_certificate_upload_standalone(client: AsyncClient):
    """Test uploading certificate file (PDF/Image) via standalone upload endpoint."""
    user_headers, _ = await _create_user(client)

    # 1. Valid PDF upload
    fake_pdf = b"%PDF-1.4\n%fake pdf binary content for medical certification\n%%EOF"
    files = {"file": ("cpr_certificate.pdf", io.BytesIO(fake_pdf), "application/pdf")}
    res = await client.post("/api/v1/users/me/skills/upload", files=files, headers=user_headers)
    assert res.status_code == 200
    data = res.json()
    assert data["success"] is True
    assert data["certificate_url"].startswith("/uploads/certificates/")
    assert data["file_type"] == "application/pdf"

    # 2. Invalid file format returns 400
    bad_file = {"file": ("script.sh", io.BytesIO(b"#!/bin/bash\necho hello"), "application/x-sh")}
    res_bad = await client.post("/api/v1/users/me/skills/upload", files=bad_file, headers=user_headers)
    assert res_bad.status_code == 400


@pytest.mark.asyncio
async def test_skill_claim_json_and_list(client: AsyncClient):
    """Test claiming a skill using JSON payload and retrieving user's skill verification list."""
    user_headers, user = await _create_user(client)

    claim_payload = {
        "skill_type": "CPR_CERTIFIED",
        "certificate_url": "/uploads/certificates/cert_aha_cpr_2026.pdf",
        "notes": "American Heart Association BLS Certificate #AHA-99214",
    }
    res = await client.post("/api/v1/users/me/skills", json=claim_payload, headers=user_headers)
    assert res.status_code == 201
    claim = res.json()

    assert claim["skill_type"] == "CPR_CERTIFIED"
    assert claim["certificate_url"] == "/uploads/certificates/cert_aha_cpr_2026.pdf"
    assert claim["status"] == "PENDING"
    assert claim["notes"] == "American Heart Association BLS Certificate #AHA-99214"
    assert claim["user_id"] == user["id"]

    # Verify user profile now has the skill recorded as unverified
    res_me = await client.get("/api/v1/users/me", headers=user_headers)
    profile = res_me.json()
    assert len(profile["skills"]) == 1
    assert profile["skills"][0]["skill_type"] == "CPR_CERTIFIED"
    assert profile["skills"][0]["verified"] is False
    assert profile["trust_score"] == 50.0  # Not incremented yet

    # List user's verification claims
    res_list = await client.get("/api/v1/users/me/skills", headers=user_headers)
    assert res_list.status_code == 200
    my_claims = res_list.json()
    assert len(my_claims) == 1
    assert my_claims[0]["id"] == claim["id"]


@pytest.mark.asyncio
async def test_skill_claim_form_multipart(client: AsyncClient):
    """Test claiming a skill with multipart/form-data upload in one step."""
    user_headers, _ = await _create_user(client)

    fake_png = b"\x89PNG\r\n\x1a\n" + b"\x00" * 80
    data = {
        "skill_type": "EMT",
        "notes": "State Paramedic License #EMT-54321",
    }
    files = {"file": ("license.png", io.BytesIO(fake_png), "image/png")}

    res = await client.post("/api/v1/users/me/skills/form", data=data, files=files, headers=user_headers)
    assert res.status_code == 201
    claim = res.json()
    assert claim["skill_type"] == "EMT"
    assert claim["status"] == "PENDING"
    assert claim["certificate_url"].startswith("/uploads/certificates/")


@pytest.mark.asyncio
async def test_duplicate_pending_claim_rejected(client: AsyncClient):
    """Test that submitting duplicate pending claims for the same skill is rejected."""
    user_headers, _ = await _create_user(client)

    payload = {
        "skill_type": "DOCTOR",
        "certificate_url": "/uploads/certificates/doctor_license.pdf",
    }
    res1 = await client.post("/api/v1/users/me/skills", json=payload, headers=user_headers)
    assert res1.status_code == 201

    # Second claim for same skill should return 400
    res2 = await client.post("/api/v1/users/me/skills", json=payload, headers=user_headers)
    assert res2.status_code == 400
    assert "pending" in res2.json()["detail"].lower()


@pytest.mark.asyncio
async def test_admin_approve_skill_verification_flow(client: AsyncClient):
    """Full lifecycle: User claims skill -> Admin approves -> Trust Score +5 & badges awarded -> Notification dispatched."""
    DISPATCHED_NOTIFICATIONS.clear()

    user_headers, user = await _create_user(client, is_superuser=False)
    admin_headers, admin = await _create_user(client, is_superuser=True)

    # 1. User claims CPR skill
    claim_res = await client.post(
        "/api/v1/users/me/skills",
        json={
            "skill_type": "cpr",
            "certificate_url": "/uploads/certificates/cpr_cert.pdf",
            "notes": "Red Cross CPR Certified 2026",
        },
        headers=user_headers,
    )
    assert claim_res.status_code == 201
    claim_id = claim_res.json()["id"]

    # 2. Admin views queue
    queue_res = await client.get("/api/v1/admin/verification-queue?status=PENDING", headers=admin_headers)
    assert queue_res.status_code == 200
    queue = queue_res.json()
    assert queue["total"] >= 1
    target = next((item for item in queue["verifications"] if item["id"] == claim_id), None)
    assert target is not None
    assert target["skill_type"] == "CPR_CERTIFIED"
    assert target["user_email"] == user["email"]

    # 3. Admin approves verification
    approve_res = await client.post(
        f"/api/v1/admin/verification-queue/{claim_id}/approve",
        json={"notes": "Verified against Red Cross registry."},
        headers=admin_headers,
    )
    assert approve_res.status_code == 200
    approved_data = approve_res.json()
    assert approved_data["status"] == "APPROVED"
    assert approved_data["reviewed_by"] == admin["id"]
    assert approved_data["reviewed_at"] is not None

    # 4. Verify user's updated profile (Trust score increment + badges)
    user_me_res = await client.get("/api/v1/users/me", headers=user_headers)
    assert user_me_res.status_code == 200
    user_profile = user_me_res.json()

    # Trust score incremented by +5.0 (from 50.0 to 55.0)
    assert user_profile["trust_score"] == 55.0

    # Skill is marked verified
    cpr_skill = next(s for s in user_profile["skills"] if s["skill_type"] == "CPR_CERTIFIED")
    assert cpr_skill["verified"] is True
    assert cpr_skill["verified_at"] is not None

    # Badges awarded
    assert "CPR_CERTIFIED" in user_profile["badges"]
    assert "VERIFIED_RESPONDER" in user_profile["badges"]

    # 5. Notification dispatched
    assert any(n["user_id"] == user["id"] and "Verified" in n["title"] for n in DISPATCHED_NOTIFICATIONS)

    # 6. Attempting to claim the same verified skill again is rejected
    duplicate_res = await client.post(
        "/api/v1/users/me/skills",
        json={"skill_type": "CPR_CERTIFIED", "certificate_url": "/uploads/certificates/another.pdf"},
        headers=user_headers,
    )
    assert duplicate_res.status_code == 400
    assert "already verified" in duplicate_res.json()["detail"].lower()


@pytest.mark.asyncio
async def test_admin_reject_skill_verification_flow(client: AsyncClient):
    """Test rejecting a skill verification with feedback reason."""
    DISPATCHED_NOTIFICATIONS.clear()

    user_headers, user = await _create_user(client, is_superuser=False)
    admin_headers, admin = await _create_user(client, is_superuser=True)

    # 1. User claims Doctor skill with blurry image
    claim_res = await client.post(
        "/api/v1/users/me/skills",
        json={
            "skill_type": "DOCTOR",
            "certificate_url": "/uploads/certificates/blurry_photo.jpg",
        },
        headers=user_headers,
    )
    assert claim_res.status_code == 201
    claim_id = claim_res.json()["id"]

    # 2. Admin rejects claim
    reject_res = await client.post(
        f"/api/v1/admin/verification-queue/{claim_id}/reject",
        json={"rejection_reason": "Medical council registration number is unreadable in the photo."},
        headers=admin_headers,
    )
    assert reject_res.status_code == 200
    rejected = reject_res.json()
    assert rejected["status"] == "REJECTED"
    assert "unreadable" in rejected["rejection_reason"]
    assert rejected["reviewed_by"] == admin["id"]

    # 3. User profile verification status and trust score
    user_me = await client.get("/api/v1/users/me", headers=user_headers)
    profile = user_me.json()
    assert profile["trust_score"] == 50.0  # Trust score was NOT incremented
    doc_skill = next(s for s in profile["skills"] if s["skill_type"] == "DOCTOR")
    assert doc_skill["verified"] is False
    assert "DOCTOR" not in profile["badges"]

    # 4. Rejection notification dispatched
    assert any(n["user_id"] == user["id"] and "Update" in n["title"] for n in DISPATCHED_NOTIFICATIONS)


@pytest.mark.asyncio
async def test_admin_put_review_and_aliases(client: AsyncClient):
    """Test review via PUT endpoint and direct compatibility paths (/api/admin/verifications)."""
    user_headers, _ = await _create_user(client)
    admin_headers, _ = await _create_user(client, is_superuser=True)

    claim_res = await client.post(
        "/api/users/me/skills",
        json={"skill_type": "NURSE", "certificate_url": "/uploads/certificates/nurse.pdf"},
        headers=user_headers,
    )
    assert claim_res.status_code == 201
    claim_id = claim_res.json()["id"]

    # Test alias GET /api/admin/verifications
    alias_get = await client.get("/api/admin/verifications", headers=admin_headers)
    assert alias_get.status_code == 200
    assert alias_get.json()["total"] >= 1

    # Test PUT review endpoint
    put_res = await client.put(
        f"/api/admin/verifications/{claim_id}",
        json={"action": "APPROVE", "notes": "Approved via PUT review alias."},
        headers=admin_headers,
    )
    assert put_res.status_code == 200
    assert put_res.json()["status"] == "APPROVED"
