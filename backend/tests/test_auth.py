"""NearHelp AI — Authentication & Identity Comprehensive Test Suite."""

import uuid

import pytest
from httpx import AsyncClient

from app.core.config import settings


@pytest.mark.asyncio
async def test_health_and_root_endpoints(client: AsyncClient):
    """Test health check and root landing endpoints."""
    res_health = await client.get("/health")
    assert res_health.status_code == 200
    data_health = res_health.json()
    assert data_health["status"] == "healthy"
    assert data_health["service"] == "nearhelp-backend"
    assert data_health["spatial_engine"] == "PostGIS 3.4"

    res_root = await client.get("/")
    assert res_root.status_code == 200
    assert res_root.json()["status"] == "online"


@pytest.mark.asyncio
async def test_register_user_success(client: AsyncClient):
    """Test successful user registration with email and password."""
    unique_email = f"responder_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    payload = {
        "email": unique_email,
        "password": "SecurePassword123!",
        "name": "Dr. Sarah Jenkins",
        "phone": f"+9198{uuid.uuid4().int % 100000000:08d}",
        "blood_group": "O+",
    }

    res = await client.post("/api/v1/auth/register", json=payload)
    assert res.status_code == 201
    data = res.json()

    assert "access_token" in data
    assert "refresh_token" in data
    assert data["token_type"] == "bearer"
    assert data["expires_in"] == settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60

    user = data["user"]
    assert user["email"] == unique_email
    assert user["name"] == "Dr. Sarah Jenkins"
    assert user["blood_group"] == "O+"
    assert user["auth_provider"] == "email"
    assert user["is_anonymous"] is False
    assert user["is_active"] is True
    assert "hashed_password" not in user


@pytest.mark.asyncio
async def test_register_user_duplicate_email(client: AsyncClient):
    """Test registration rejection when email already exists."""
    unique_email = f"duplicate_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    payload = {
        "email": unique_email,
        "password": "Password123!",
        "name": "First User",
    }

    # First registration
    res1 = await client.post("/api/v1/auth/register", json=payload)
    assert res1.status_code == 201

    # Second registration with identical email
    res2 = await client.post("/api/v1/auth/register", json=payload)
    assert res2.status_code == 409
    assert "already exists" in res2.json()["detail"]


@pytest.mark.asyncio
async def test_login_user_success(client: AsyncClient):
    """Test login with valid email and password."""
    unique_email = f"login_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    password = "MyStrongPassword456"

    # Create account
    await client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": password, "name": "Adil Khan"},
    )

    # Login
    res = await client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": password},
    )
    assert res.status_code == 200
    data = res.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["user"]["email"] == unique_email
    assert data["user"]["name"] == "Adil Khan"


@pytest.mark.asyncio
async def test_login_user_invalid_password(client: AsyncClient):
    """Test login rejection when password is incorrect."""
    unique_email = f"wrongpass_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    password = "CorrectPassword123"

    await client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": password, "name": "Test User"},
    )

    res = await client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": "WrongPassword!"},
    )
    assert res.status_code == 401
    assert "Invalid email or password" in res.json()["detail"]


@pytest.mark.asyncio
async def test_login_user_nonexistent_email(client: AsyncClient):
    """Test login rejection for unknown email."""
    res = await client.post(
        "/api/v1/auth/login",
        json={"email": "nobody@nearhelp.ai", "password": "AnyPassword"},
    )
    assert res.status_code == 401
    assert "Invalid email or password" in res.json()["detail"]


@pytest.mark.asyncio
async def test_refresh_token_lifecycle(client: AsyncClient):
    """Test exchanging a 7-day refresh token for a new access token."""
    unique_email = f"refresh_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    reg_res = await client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": "Password789!", "name": "Refresh Tester"},
    )
    refresh_token = reg_res.json()["refresh_token"]

    # Request new access token using refresh token
    ref_res = await client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token},
    )
    assert ref_res.status_code == 200
    ref_data = ref_res.json()
    assert "access_token" in ref_data
    assert "refresh_token" in ref_data
    assert ref_data["user"]["email"] == unique_email

    # Verify new access token grants access to protected endpoint
    me_res = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {ref_data['access_token']}"},
    )
    assert me_res.status_code == 200
    assert me_res.json()["email"] == unique_email


@pytest.mark.asyncio
async def test_refresh_token_invalid_or_expired(client: AsyncClient):
    """Test rejection of malformed or invalid refresh tokens."""
    res = await client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": "invalid.jwt.token.string"},
    )
    assert res.status_code == 401


@pytest.mark.asyncio
async def test_google_oauth_flow(client: AsyncClient):
    """Test Google OAuth 2.0 / Firebase ID token authentication."""
    google_token = f"mock_google_token_{uuid.uuid4().hex[:8]}"

    res = await client.post(
        "/api/v1/auth/google",
        json={"id_token": google_token},
    )
    assert res.status_code == 200
    data = res.json()
    assert "access_token" in data
    assert data["user"]["auth_provider"] == "google"
    assert data["user"]["email"] == "google_user@nearhelp.ai"


@pytest.mark.asyncio
async def test_phone_otp_verification_flow(client: AsyncClient):
    """Test phone OTP dispatch and verification flow."""
    phone_number = f"+9198{uuid.uuid4().int % 100000000:08d}"

    # 1. Send OTP
    send_res = await client.post(
        "/api/v1/auth/phone/send-otp",
        json={"phone_number": phone_number},
    )
    assert send_res.status_code == 200
    assert send_res.json()["success"] is True

    # 2. Verify OTP
    verify_res = await client.post(
        "/api/v1/auth/phone/verify",
        json={
            "phone_number": phone_number,
            "otp_code": "123456",
            "name": "Verified Citizen",
        },
    )
    assert verify_res.status_code == 200
    data = verify_res.json()
    assert "access_token" in data
    assert data["user"]["phone"] == phone_number
    assert data["user"]["phone_verified"] is True
    assert data["user"]["auth_provider"] == "phone"


@pytest.mark.asyncio
async def test_anonymous_emergency_mode(client: AsyncClient):
    """Test Anonymous Emergency Mode creating disposable session with zero PII."""
    device_id = str(uuid.uuid4())
    res = await client.post(
        "/api/v1/auth/anonymous",
        json={"device_id": device_id, "temp_name": "Emergency Bystander"},
    )
    assert res.status_code == 201
    data = res.json()

    assert "access_token" in data
    assert data["user"]["is_anonymous"] is True
    assert data["user"]["auth_provider"] == "anonymous"
    assert data["user"]["email"] is None
    assert data["user"]["phone"] is None
    assert data["user"]["name"] == "Emergency Bystander"

    # Authenticate with anonymous token
    me_res = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {data['access_token']}"},
    )
    assert me_res.status_code == 200
    assert me_res.json()["is_anonymous"] is True


@pytest.mark.asyncio
async def test_get_current_user_me_protection(client: AsyncClient):
    """Test /me endpoint authentication protection (valid token, missing token, invalid token)."""
    # 1. Missing header
    res_no_auth = await client.get("/api/v1/auth/me")
    assert res_no_auth.status_code == 401

    # 2. Tampered token
    res_bad_token = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": "Bearer not.a.valid.jwt.token"},
    )
    assert res_bad_token.status_code == 401

    # 3. Valid user
    reg = await client.post(
        "/api/v1/auth/register",
        json={"email": f"authme_{uuid.uuid4().hex[:6]}@nearhelp.ai", "password": "Password123!", "name": "Me User"},
    )
    token = reg.json()["access_token"]
    res_valid = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert res_valid.status_code == 200
    assert res_valid.json()["name"] == "Me User"


@pytest.mark.asyncio
async def test_device_fcm_registration(client: AsyncClient):
    """Test registering FCM token for push notification delivery."""
    reg = await client.post(
        "/api/v1/auth/register",
        json={"email": f"fcm_{uuid.uuid4().hex[:6]}@nearhelp.ai", "password": "Password123!", "name": "Device User"},
    )
    token = reg.json()["access_token"]
    fcm_token_val = "fcm_token_mock_android_pixel_8_pro_device_key_abcdef123456"

    res = await client.post(
        "/api/v1/auth/device",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "fcm_token": fcm_token_val,
            "device_id": "device_pixel_8",
            "platform": "android",
            "device_info": {"model": "Pixel 8 Pro", "os": "Android 15"},
        },
    )
    assert res.status_code == 200
    assert res.json()["success"] is True

    # Verify user profile now stores FCM token
    me_res = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert me_res.json()["fcm_token"] == fcm_token_val


@pytest.mark.asyncio
async def test_update_user_profile(client: AsyncClient):
    """Test updating user profile attributes and emergency contacts."""
    reg = await client.post(
        "/api/v1/auth/register",
        json={"email": f"profile_{uuid.uuid4().hex[:6]}@nearhelp.ai", "password": "Password123!", "name": "Initial Name"},
    )
    token = reg.json()["access_token"]

    update_payload = {
        "name": "Updated Name, MD",
        "blood_group": "AB+",
        "languages": ["en", "bn", "hi"],
        "emergency_contacts": [
            {"name": "Kin Contact 1", "phone": "+919876500001", "relationship": "Spouse"},
            {"name": "Kin Contact 2", "phone": "+919876500002", "relationship": "Parent"},
        ],
        "medical_conditions": ["Asthma", "Mild Hypertension"],
        "known_allergies": ["Penicillin", "Peanuts"],
    }

    res = await client.put(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
        json=update_payload,
    )
    assert res.status_code == 200
    data = res.json()
    assert data["name"] == "Updated Name, MD"
    assert data["blood_group"] == "AB+"
    assert data["languages"] == ["en", "bn", "hi"]
    assert len(data["emergency_contacts"]) == 2
    assert "Asthma" in data["medical_conditions"]
    assert "Penicillin" in data["known_allergies"]


@pytest.mark.asyncio
async def test_idempotency_key_middleware_replay(client: AsyncClient):
    """Test idempotency middleware returning cached response on key replay."""
    idempotency_key = str(uuid.uuid4())
    unique_email = f"idempotent_{uuid.uuid4().hex[:6]}@nearhelp.ai"
    payload = {
        "email": unique_email,
        "password": "Password123!",
        "name": "Idempotent User",
    }

    # First request
    res1 = await client.post(
        "/api/v1/auth/register",
        headers={"Idempotency-Key": idempotency_key},
        json=payload,
    )
    assert res1.status_code == 201
    token1 = res1.json()["access_token"]

    # Replay request with same idempotency key (would normally 409 conflict if executed again)
    res2 = await client.post(
        "/api/v1/auth/register",
        headers={"Idempotency-Key": idempotency_key},
        json=payload,
    )
    assert res2.status_code == 201
    assert res2.headers.get("X-Idempotent-Replay") == "true"
    token2 = res2.json()["access_token"]
    assert token1 == token2


@pytest.mark.asyncio
async def test_compatibility_routes(client: AsyncClient):
    """Test backward compatibility aliases /api/auth/* matching /api/v1/auth/*."""
    unique_email = f"compat_{uuid.uuid4().hex[:6]}@nearhelp.ai"
    res = await client.post(
        "/api/auth/register",
        json={"email": unique_email, "password": "Password123!", "name": "Compat User"},
    )
    assert res.status_code == 201
    assert "access_token" in res.json()


@pytest.mark.asyncio
async def test_rate_limiting_middleware_throttling(client: AsyncClient):
    """Test rate limiting middleware throttling rapid auth requests."""
    # Rapidly fire requests beyond auth_limit threshold (30 per min)
    got_429 = False
    for i in range(35):
        res = await client.post(
            "/api/v1/auth/login",
            json={"email": f"ratelimit_{i}@nearhelp.ai", "password": "DummyPassword123"},
        )
        if res.status_code == 429:
            got_429 = True
            assert "Too many authentication attempts" in res.json()["detail"]
            assert "Retry-After" in res.headers
            break

    assert got_429 is True
