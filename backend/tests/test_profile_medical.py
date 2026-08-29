"""NearHelp AI — Module 2 User Profile & Encrypted Medical ID Test Suite."""

import io
import uuid

import pytest
from httpx import AsyncClient
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.crypto import CIPHERTEXT_PREFIX
from app.models.user import User


async def _create_authenticated_user(client: AsyncClient) -> tuple[str, dict]:
    """Helper fixture to register a test user and obtain auth headers."""
    email = f"user_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    payload = {
        "email": email,
        "password": "SecurePassword123!",
        "name": "Alex Mercer",
        "phone": f"+9198{uuid.uuid4().int % 100000000:08d}",
        "blood_group": "B+",
    }
    res = await client.post("/api/v1/auth/register", json=payload)
    assert res.status_code == 201
    data = res.json()
    token = data["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    return headers, data["user"]


@pytest.mark.asyncio
async def test_profile_unauthorized(client: AsyncClient):
    """Test accessing profile endpoints without valid JWT token returns 401."""
    res = await client.get("/api/v1/users/me")
    assert res.status_code == 401


@pytest.mark.asyncio
async def test_get_and_update_profile(client: AsyncClient):
    """Test fetching and updating user profile attributes."""
    headers, user = await _create_authenticated_user(client)

    # 1. Fetch Profile
    res = await client.get("/api/v1/users/me", headers=headers)
    assert res.status_code == 200
    data = res.json()
    assert data["email"] == user["email"]
    assert data["blood_group"] == "B+"
    assert data["languages"] == ["en"]

    # 2. Update Profile
    update_payload = {
        "name": "Alexander Mercer, MD",
        "blood_group": "O+",
        "languages": ["en", "bn"],
        "has_pacemaker": True,
        "is_organ_donor": True,
        "medical_conditions": ["Type 1 Diabetes", "Asthma"],
        "known_allergies": ["Penicillin", "Peanuts"],
        "medical_notes": "Patient carries EpiPen at all times.",
    }
    res_update = await client.put("/api/v1/users/me", json=update_payload, headers=headers)
    assert res_update.status_code == 200
    updated = res_update.json()

    assert updated["name"] == "Alexander Mercer, MD"
    assert updated["blood_group"] == "O+"
    assert updated["has_pacemaker"] is True
    assert updated["is_organ_donor"] is True
    assert updated["languages"] == ["en", "bn"]
    assert updated["medical_conditions"] == ["Type 1 Diabetes", "Asthma"]
    assert updated["known_allergies"] == ["Penicillin", "Peanuts"]
    assert updated["medical_notes"] == "Patient carries EpiPen at all times."


@pytest.mark.asyncio
async def test_medical_id_encryption_at_rest(client: AsyncClient, db_session: AsyncSession):
    """Verify that medical conditions and allergies are encrypted with AES-256-GCM in the DB."""
    headers, user = await _create_authenticated_user(client)

    medical_payload = {
        "blood_group": "AB-",
        "medical_conditions": ["Cardiac Arrhythmia", "Severe Hypertension"],
        "known_allergies": ["Sulfa Drugs", "Latex"],
        "has_pacemaker": True,
        "is_organ_donor": True,
        "medical_notes": "Implant model: Medtronic Azure XT 2024",
    }

    # 1. Update Medical ID via API
    res = await client.patch("/api/v1/users/me/medical", json=medical_payload, headers=headers)
    assert res.status_code == 200
    resp_data = res.json()

    # API returns decrypted plaintext for authorized user
    assert resp_data["blood_group"] == "AB-"
    assert resp_data["medical_conditions"] == ["Cardiac Arrhythmia", "Severe Hypertension"]
    assert resp_data["known_allergies"] == ["Sulfa Drugs", "Latex"]
    assert resp_data["has_pacemaker"] is True
    assert resp_data["is_organ_donor"] is True
    assert resp_data["medical_notes"] == "Implant model: Medtronic Azure XT 2024"
    assert resp_data["is_encrypted_at_rest"] is True

    # 2. Query Raw Database Column directly to prove AES-256-GCM encryption at rest
    stmt = select(User).where(User.id == uuid.UUID(user["id"]))
    result = await db_session.execute(stmt)
    db_user = result.scalars().first()

    assert db_user is not None
    # Raw DB columns must be encrypted ciphertexts
    assert isinstance(db_user.medical_conditions, str)
    assert db_user.medical_conditions.startswith(CIPHERTEXT_PREFIX)
    assert "Arrhythmia" not in db_user.medical_conditions  # Plaintext is NEVER stored

    assert isinstance(db_user.known_allergies, str)
    assert db_user.known_allergies.startswith(CIPHERTEXT_PREFIX)
    assert "Latex" not in db_user.known_allergies

    assert isinstance(db_user.medical_notes, str)
    assert db_user.medical_notes.startswith(CIPHERTEXT_PREFIX)
    assert "Medtronic" not in db_user.medical_notes


@pytest.mark.asyncio
async def test_emergency_contacts_crud_and_limit(client: AsyncClient):
    """Test adding, listing, updating, deleting emergency contacts and enforcing max 5 contacts limit."""
    headers, _ = await _create_authenticated_user(client)

    # 1. Initial list is empty
    res_list = await client.get("/api/v1/users/me/contacts", headers=headers)
    assert res_list.status_code == 200
    assert res_list.json() == []

    # 2. Add Contact 1 (Mother) -> becomes primary
    c1_payload = {
        "name": "Maria Mercer",
        "phone": "+919830011223",
        "relationship": "Mother",
        "is_primary": True,
    }
    res_c1 = await client.post("/api/v1/users/me/contacts", json=c1_payload, headers=headers)
    assert res_c1.status_code == 201
    c1 = res_c1.json()
    assert c1["name"] == "Maria Mercer"
    assert c1["is_primary"] is True
    c1_id = c1["id"]

    # 3. Add Contact 2 (Spouse) -> set as primary, unsetting Contact 1
    c2_payload = {
        "name": "Elena Mercer",
        "phone": "+919830099887",
        "relationship": "Spouse",
        "is_primary": True,
    }
    res_c2 = await client.post("/api/v1/users/me/contacts", json=c2_payload, headers=headers)
    assert res_c2.status_code == 201
    c2 = res_c2.json()
    assert c2["is_primary"] is True
    c2_id = c2["id"]

    # 4. Verify Contact 1 was unset as primary
    res_list_2 = await client.get("/api/v1/users/me/contacts", headers=headers)
    contacts = res_list_2.json()
    assert len(contacts) == 2
    contact1 = next(c for c in contacts if c["id"] == c1_id)
    contact2 = next(c for c in contacts if c["id"] == c2_id)
    assert contact1["is_primary"] is False
    assert contact2["is_primary"] is True

    # 5. Update Contact 1
    update_c1 = {"relationship": "Mother (Doctor)", "phone": "+919830099999"}
    res_up_c1 = await client.put(f"/api/v1/users/me/contacts/{c1_id}", json=update_c1, headers=headers)
    assert res_up_c1.status_code == 200
    assert res_up_c1.json()["relationship"] == "Mother (Doctor)"
    assert res_up_c1.json()["phone"] == "+919830099999"

    # 6. Add remaining contacts up to 5
    for i in range(3, 6):
        res_ci = await client.post(
            "/api/v1/users/me/contacts",
            json={"name": f"Contact {i}", "phone": f"+91983000000{i}", "relationship": "Friend"},
            headers=headers,
        )
        assert res_ci.status_code == 201

    # 7. Attempt to add 6th contact -> Must fail with 400 Bad Request
    res_c6 = await client.post(
        "/api/v1/users/me/contacts",
        json={"name": "Excess Contact", "phone": "+919830099999", "relationship": "Neighbor"},
        headers=headers,
    )
    assert res_c6.status_code == 400
    assert "limit" in res_c6.json()["detail"].lower()

    # 8. Delete a contact
    res_del = await client.delete(f"/api/v1/users/me/contacts/{c1_id}", headers=headers)
    assert res_del.status_code == 200

    # 9. Verify count is now 4
    res_after_del = await client.get("/api/v1/users/me/contacts", headers=headers)
    assert len(res_after_del.json()) == 4


@pytest.mark.asyncio
async def test_profile_photo_upload(client: AsyncClient):
    """Test uploading avatar image via multipart/form-data."""
    headers, _ = await _create_authenticated_user(client)

    # 1. Upload valid PNG image
    fake_png_bytes = b"\x89PNG\r\n\x1a\n" + b"\x00" * 100
    files = {"file": ("avatar.png", io.BytesIO(fake_png_bytes), "image/png")}

    res = await client.post("/api/v1/users/me/photo", files=files, headers=headers)
    assert res.status_code == 200
    data = res.json()
    assert "photo_url" in data
    assert data["photo_url"].startswith("/uploads/avatars/")

    # Verify profile now reflects photo_url
    res_me = await client.get("/api/v1/users/me", headers=headers)
    assert res_me.json()["photo_url"] == data["photo_url"]

    # 2. Upload invalid mime type fails with 400
    invalid_file = {"file": ("malicious.exe", io.BytesIO(b"binary"), "application/x-msdownload")}
    res_invalid = await client.post("/api/v1/users/me/photo", files=invalid_file, headers=headers)
    assert res_invalid.status_code == 400


@pytest.mark.asyncio
async def test_language_preferences_endpoint(client: AsyncClient):
    """Test language preference updates and validation."""
    headers, _ = await _create_authenticated_user(client)

    # Valid languages
    res = await client.put("/api/v1/users/me/languages", json={"languages": ["en", "bn", "hi"]}, headers=headers)
    assert res.status_code == 200
    assert res.json()["languages"] == ["en", "bn", "hi"]

    # Empty languages list fails
    res_bad = await client.put("/api/v1/users/me/languages", json={"languages": []}, headers=headers)
    assert res_bad.status_code == 422
