"""NearHelp AI — Module 6 Smart SOS Engine Test Suite."""

import uuid

import pytest
from httpx import AsyncClient

from app.models.user import User
from app.services.ranking_service import ranking_service


async def _create_user(
    client: AsyncClient,
    name: str = "Test User",
    skills: list | None = None,
    badges: list | None = None,
    trust_score: float = 50.0,
    lat: float | None = None,
    lon: float | None = None,
) -> tuple[dict, dict]:
    """Helper to create a test user with custom skills, badges, and location."""
    email = f"user_{uuid.uuid4().hex[:8]}@nearhelp.ai"
    payload = {
        "email": email,
        "password": "SecurePassword123!",
        "name": name,
        "phone": f"+9198{uuid.uuid4().int % 100000000:08d}",
        "blood_group": "O+",
    }
    res = await client.post("/api/v1/auth/register", json=payload)
    assert res.status_code == 201
    data = res.json()
    token = data["access_token"]
    user_info = data["user"]

    # Directly adjust skills/badges/trust_score for unit testing
    try:
        from tests.conftest import TestingSessionLocal
        session_factory = TestingSessionLocal
    except ImportError:
        from app.db.session import AsyncSessionLocal
        session_factory = AsyncSessionLocal

    async with session_factory() as session:
        from sqlalchemy import select
        stmt = select(User).where(User.id == uuid.UUID(user_info["id"]))
        db_res = await session.execute(stmt)
        db_user = db_res.scalars().first()
        if db_user:
            if skills is not None:
                db_user.skills = skills
            if badges is not None:
                db_user.badges = badges
            if trust_score is not None:
                db_user.trust_score = trust_score
            if lat is not None and lon is not None:
                db_user.device_info = {"last_latitude": lat, "last_longitude": lon}
            await session.commit()

    headers = {"Authorization": f"Bearer {token}"}
    return headers, user_info


# ==============================================================================
# 1. Mathematical Ranking Formula Validation (Section 12 Benchmark)
# ==============================================================================
@pytest.mark.asyncio
async def test_responder_ranking_mathematical_validation():
    """Verify Section 12 exact weighted ranking formula and candidate comparison.
    
    Scenario:
      Emergency: Cardiac Arrest, max_radius = 3000m, required_skills = ['CPR_CERTIFIED', 'DOCTOR', 'EMT', 'NURSE']
      - Responder 1: 200m away, unverified bystander, no medical skills, trust = 60
      - Responder 2: 800m away, verified nurse + CPR, trust = 85
      
      Expected:
        Score 1 = 0.40*(1 - 200/3000) + 0.35*(0) + 0.25*(0.60) = 0.3733 + 0.0 + 0.150 = 0.5233
        Score 2 = 0.40*(1 - 800/3000) + 0.35*(1.0 + 0.2) + 0.25*(0.85) = 0.2933 + 0.420 + 0.2125 = 0.9258
    """
    req_skills = ["CPR_CERTIFIED", "DOCTOR", "EMT", "NURSE"]
    max_rad = 3000.0

    # Mock user 1
    u1 = User(
        id=uuid.uuid4(),
        name="Unskilled Bystander",
        skills=[],
        badges=[],
        trust_score=60.0,
    )
    res1 = ranking_service.score_responder(
        user=u1,
        distance_meters=200.0,
        max_radius_meters=max_rad,
        required_skills=req_skills,
    )

    # Mock user 2
    u2 = User(
        id=uuid.uuid4(),
        name="Nurse Sarah",
        skills=[
            {"name": "CPR_CERTIFIED", "verified": True, "status": "APPROVED"},
            {"name": "NURSE", "verified": True, "status": "APPROVED"},
            {"name": "DOCTOR", "verified": True, "status": "APPROVED"},
            {"name": "EMT", "verified": True, "status": "APPROVED"},
        ],
        badges=["CPR_HERO", "VERIFIED_NURSE"],
        trust_score=85.0,
    )
    res2 = ranking_service.score_responder(
        user=u2,
        distance_meters=800.0,
        max_radius_meters=max_rad,
        required_skills=req_skills,
    )

    # Assert expected score tolerances
    assert abs(res1.total_ranking_score - 0.5233) < 0.01
    assert abs(res2.total_ranking_score - 0.9258) < 0.01
    assert res2.total_ranking_score > res1.total_ranking_score

    # Rank both candidates
    ranked_list = ranking_service.rank_responders(
        candidates=[(u1, 200.0), (u2, 800.0)],
        max_radius_meters=max_rad,
        required_skills=req_skills,
        severity_level=5,
    )
    assert len(ranked_list) == 2
    assert ranked_list[0].responder_id == u2.id
    assert ranked_list[1].responder_id == u1.id


# ==============================================================================
# 2. Authenticated SOS Creation
# ==============================================================================
@pytest.mark.asyncio
async def test_sos_create_authenticated_flow(client: AsyncClient):
    """Test creating an SOS emergency when logged in as an authenticated victim."""
    victim_headers, victim_info = await _create_user(
        client, name="Victim User", lat=22.5726, lon=88.3639
    )

    # Also seed a nearby responder
    await _create_user(
        client,
        name="Dr. Aris",
        skills=[{"name": "CPR_CERTIFIED", "verified": True}],
        trust_score=90.0,
        lat=22.5740,
        lon=88.3650,
    )

    payload = {
        "latitude": 22.5726,
        "longitude": 88.3639,
        "crisis_type": "medical",
        "sub_type": "cardiac_arrest",
        "description": "Patient collapsed suddenly, unconscious with no pulse. Severe chest pain prior to collapse.",
        "symptoms": ["chest_pain", "unconscious", "no_pulse"],
        "address": "Salt Lake Sector V, Electronics Complex",
        "sub_address": "Kolkata, WB",
        "is_anonymous": False,
    }

    res = await client.post("/api/v1/sos/create", json=payload, headers=victim_headers)
    assert res.status_code == 201
    data = res.json()

    assert "id" in data
    assert data["broadcaster_id"] == victim_info["id"]
    assert data["status"] == "SOS_TRIGGERED"
    assert data["crisis_type"] == "medical"
    assert data["severity_score"] >= 80  # Cardiac arrest triggers high severity
    assert data["priority"] in ("critical", "high")
    assert data["immediate_action"] is not None
    assert isinstance(data["required_skills"], list)
    assert data["escalation"]["current_layer"] == 1
    assert len(data["top_ranked_responders"]) >= 1


# ==============================================================================
# 3. Anonymous Emergency Trigger
# ==============================================================================
@pytest.mark.asyncio
async def test_sos_create_anonymous_mode(client: AsyncClient):
    """Test zero-PII anonymous emergency broadcast creation."""
    payload = {
        "latitude": 22.5800,
        "longitude": 88.3700,
        "crisis_type": "fire",
        "sub_type": "electrical_fire",
        "description": "Thick black smoke and flames coming from transformer.",
        "address": "Karunamoyee Bus Terminus",
        "is_anonymous": True,
    }

    # Post without authorization header
    res = await client.post("/api/v1/sos/create", json=payload)
    assert res.status_code == 201
    data = res.json()

    assert data["broadcaster_id"] is None
    assert data["is_anonymous"] is True
    assert data["crisis_type"] == "fire"
    assert data["escalation"]["recommended_emergency_number"] in ("101", "108", "112")


# ==============================================================================
# 4. Idempotency Key Middleware Replay
# ==============================================================================
@pytest.mark.asyncio
async def test_sos_create_idempotency_replay(client: AsyncClient):
    """Verify Idempotency-Key prevents double emergency creation upon network retry."""
    idempotency_key = f"sos_idem_{uuid.uuid4().hex}"
    headers = {"Idempotency-Key": idempotency_key}

    payload = {
        "latitude": 22.5690,
        "longitude": 88.3610,
        "crisis_type": "medical",
        "description": "Accidental fall down the stairs, severe fracture.",
        "is_anonymous": True,
    }

    # 1. First request
    res1 = await client.post("/api/v1/sos/create", json=payload, headers=headers)
    assert res1.status_code == 201
    data1 = res1.json()

    # 2. Second request with same idempotency key
    res2 = await client.post("/api/v1/sos/create", json=payload, headers=headers)
    assert res2.status_code == 201
    data2 = res2.json()

    # Assert exact replay without creating a duplicate incident ID
    assert data1["id"] == data2["id"]
    assert res2.headers.get("X-Idempotent-Replay") == "true"


# ==============================================================================
# 5. SOS Detail View and Audit Timeline
# ==============================================================================
@pytest.mark.asyncio
async def test_sos_detail_and_timeline_endpoint(client: AsyncClient):
    """Test retrieving full incident details and immutable milestone timeline."""
    headers, _ = await _create_user(client, name="Victim John")

    # Create emergency
    create_res = await client.post(
        "/api/v1/sos/create",
        json={
            "latitude": 22.5726,
            "longitude": 88.3639,
            "crisis_type": "medical",
            "description": "Severe asthma attack with wheezing and respiratory distress.",
            "symptoms": ["wheezing", "shortness_of_breath"],
        },
        headers=headers,
    )
    sos_id = create_res.json()["id"]

    # 1. Fetch details
    detail_res = await client.get(f"/api/v1/sos/{sos_id}")
    assert detail_res.status_code == 200
    detail = detail_res.json()
    assert detail["id"] == sos_id
    assert len(detail["timeline"]) >= 2  # 'sos_created' and 'ai_classified'
    event_types = [t["event_type"] for t in detail["timeline"]]
    assert "sos_created" in event_types
    assert "ai_classified" in event_types

    # 2. Fetch timeline dedicated route
    timeline_res = await client.get(f"/api/v1/sos/{sos_id}/timeline")
    assert timeline_res.status_code == 200
    assert isinstance(timeline_res.json(), list)
    assert len(timeline_res.json()) >= 2


# ==============================================================================
# 6. Responder Accept, Live ETA, and Arrival Flow
# ==============================================================================
@pytest.mark.asyncio
async def test_responder_accept_and_arrived_flow(client: AsyncClient):
    """Test responder accepting an SOS alert and marking on-scene arrival."""
    victim_headers, _ = await _create_user(client, name="Victim A")
    responder_headers, responder_info = await _create_user(
        client,
        name="Volunteer EMT Bob",
        skills=[{"name": "EMT", "verified": True}],
        trust_score=80.0,
    )

    # 1. Create emergency
    create_res = await client.post(
        "/api/v1/sos/create",
        json={
            "latitude": 22.5726,
            "longitude": 88.3639,
            "crisis_type": "medical",
            "description": "Road accident with lacerations and bleeding.",
        },
        headers=victim_headers,
    )
    sos_id = create_res.json()["id"]

    # 2. Responder accepts alert
    accept_payload = {
        "status": "ACCEPTED",
        "eta_minutes": 3.5,
        "current_latitude": 22.5740,
        "current_longitude": 88.3650,
    }
    accept_res = await client.post(
        f"/api/v1/sos/{sos_id}/respond",
        json=accept_payload,
        headers=responder_headers,
    )
    assert accept_res.status_code == 200
    resp_data = accept_res.json()
    assert resp_data["responder_id"] == responder_info["id"]
    assert resp_data["status"] == "ACCEPTED"

    # Verify event transitioned to RESPONDER_ACCEPTED
    detail_res = await client.get(f"/api/v1/sos/{sos_id}")
    assert detail_res.json()["status"] == "RESPONDER_ACCEPTED"

    # 3. Responder arrives on scene
    arrived_payload = {
        "status": "ARRIVED",
        "current_latitude": 22.5726,
        "current_longitude": 88.3639,
    }
    arrived_res = await client.post(
        f"/api/v1/sos/{sos_id}/respond",
        json=arrived_payload,
        headers=responder_headers,
    )
    assert arrived_res.status_code == 200
    assert arrived_res.json()["status"] == "ARRIVED"
    assert arrived_res.json()["arrived_at"] is not None


# ==============================================================================
# 7. 3-Layer Escalation Protocol
# ==============================================================================
@pytest.mark.asyncio
async def test_3_layer_escalation_protocol(client: AsyncClient):
    """Verify 3-Layer Escalation: auto-radius expansion (0-60s) -> 108 auto-dial (60s) -> AI self-care."""
    headers, _ = await _create_user(client)

    # 1. Create emergency with initial radius 1000m
    create_res = await client.post(
        "/api/v1/sos/create",
        json={
            "latitude": 22.5700,
            "longitude": 88.3600,
            "crisis_type": "medical",
            "description": "Suspected stroke with facial drooping.",
            "initial_radius_meters": 1000.0,
        },
        headers=headers,
    )
    sos_id = create_res.json()["id"]

    # 2. Trigger Layer 1b escalation at 35s (2x radius expansion to 2000m)
    esc_res_1 = await client.post(
        f"/api/v1/sos/{sos_id}/escalate",
        json={"elapsed_seconds": 35},
    )
    assert esc_res_1.status_code == 200
    data_1 = esc_res_1.json()
    assert data_1["current_layer"] == 1
    assert data_1["current_radius_meters"] == 2000.0

    # 3. Trigger Layer 2 escalation at 60s (auto-call 108 recommendation gateway)
    esc_res_2 = await client.post(
        f"/api/v1/sos/{sos_id}/escalate",
        json={"elapsed_seconds": 65},
    )
    assert esc_res_2.status_code == 200
    data_2 = esc_res_2.json()
    assert data_2["current_layer"] == 2
    assert data_2["auto_call_108_triggered"] is True
    assert data_2["recommended_emergency_number"] == "108"


# ==============================================================================
# 8. SOS Resolution and Responder Reputation Reward
# ==============================================================================
@pytest.mark.asyncio
async def test_sos_resolve_and_reputation_adjustment(client: AsyncClient):
    """Test emergency resolution and automated reputation trust score increase for responding volunteer."""
    victim_headers, _ = await _create_user(client, name="Victim Jane")
    responder_headers, responder_info = await _create_user(
        client, name="Hero Volunteer", trust_score=70.0
    )

    # 1. Create emergency
    create_res = await client.post(
        "/api/v1/sos/create",
        json={
            "latitude": 22.5726,
            "longitude": 88.3639,
            "crisis_type": "medical",
            "description": "Choking incident resolved with Heimlich maneuver.",
        },
        headers=victim_headers,
    )
    sos_id = create_res.json()["id"]

    # 2. Responder accepts
    await client.post(
        f"/api/v1/sos/{sos_id}/respond",
        json={"status": "ACCEPTED", "eta_minutes": 2.0},
        headers=responder_headers,
    )

    # 3. Victim resolves emergency with 5.0 star feedback
    resolve_payload = {
        "resolution_notes": "Volunteer arrived in 2 minutes and cleared airway successfully. Patient fully recovered.",
        "feedback_score": 5.0,
        "resolved_by": "victim",
    }
    res_resolve = await client.put(
        f"/api/v1/sos/{sos_id}/resolve",
        json=resolve_payload,
        headers=victim_headers,
    )
    assert res_resolve.status_code == 200
    res_data = res_resolve.json()
    assert res_data["status"] == "RESOLVED"
    assert len(res_data["reputation_updates"]) == 1
    update = res_data["reputation_updates"][0]
    assert update["responder_id"] == responder_info["id"]
    assert update["new_trust_score"] == 75.0  # 70 + 3 (rescue) + 2 (5-star feedback)

    # 4. Attempting to respond to resolved emergency should fail
    fail_resp = await client.post(
        f"/api/v1/sos/{sos_id}/respond",
        json={"status": "ACCEPTED"},
        headers=responder_headers,
    )
    assert fail_resp.status_code == 400


# ==============================================================================
# 9. Active SOS Feed and Direct Alias Routes
# ==============================================================================
@pytest.mark.asyncio
async def test_active_sos_feed_and_aliases(client: AsyncClient):
    """Test active emergency feed listing and direct compatibility routes (/api/sos/create)."""
    headers, _ = await _create_user(client)

    # Create active emergency via direct alias /api/sos/create
    res_alias = await client.post(
        "/api/sos/create",
        json={
            "latitude": 22.5750,
            "longitude": 88.3680,
            "crisis_type": "accident",
            "description": "Minor vehicle collision, no major entrapment.",
        },
        headers=headers,
    )
    assert res_alias.status_code == 201

    # Fetch active feed
    feed_res = await client.get("/api/v1/sos/active?lat=22.5750&lon=88.3680")
    assert feed_res.status_code == 200
    feed = feed_res.json()
    assert feed["total_count"] >= 1
    assert len(feed["active_events"]) >= 1
    first_item = feed["active_events"][0]
    assert first_item["status"] in ("SOS_TRIGGERED", "AI_TRIAGING", "RESPONDER_ACCEPTED")
