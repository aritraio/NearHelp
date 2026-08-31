"""NearHelp AI — Module 8 Live Tracking Stream & WebSocket Test Suite."""

import json
import uuid

import pytest
from fastapi.testclient import TestClient
from httpx import AsyncClient

from app.core.security import create_access_token
from app.main import app as fastapi_app
from app.models.response import SOSResponse
from app.models.sos_event import SOSEvent
from app.models.user import User
from app.services.eta_service import (
    bearing_to_compass,
    calculate_bearing,
    eta_service,
    format_distance,
    format_eta,
    haversine_distance,
)


# ==============================================================================
# 1. ETA & Distance Telemetry Unit Tests
# ==============================================================================

def test_haversine_distance_and_bearing():
    """Verify great-circle distance and 360-degree compass bearing math."""
    # Sector V (Godrej Waterside: 22.5804, 88.4378) to Webel Bhavan (22.5840, 88.4340)
    lat1, lon1 = 22.5804, 88.4378
    lat2, lon2 = 22.5840, 88.4340

    dist = haversine_distance(lat1, lon1, lat2, lon2)
    assert 400.0 < dist < 650.0

    bearing = calculate_bearing(lat1, lon1, lat2, lon2)
    assert 270.0 <= bearing <= 360.0  # North-West quadrant
    compass = bearing_to_compass(bearing)
    assert compass in ("NW", "N", "W")


def test_eta_service_urban_calculations():
    """Verify dynamic urban ETA calculation with tortuosity factor (1.35x) and speed modes."""
    # 500m distance walking (~1.25 m/s)
    # Effective distance = 500 * 1.35 = 675m -> 675 / 1.25 = 540s = 9.0 min
    lat1, lon1 = 22.5800, 88.4300
    lat2, lon2 = 22.5800, 88.43486  # ~500m east

    (
        dist_m,
        eta_min,
        eta_str,
        bearing_deg,
        bearing_compass,
        dist_str,
    ) = eta_service.calculate_eta(
        responder_lat=lat1,
        responder_lon=lon1,
        target_lat=lat2,
        target_lon=lon2,
        speed_mps=None,
        travel_mode="walking",
    )

    assert 480.0 <= dist_m <= 520.0
    assert 8.0 <= eta_min <= 10.0
    assert "mins" in eta_str
    assert dist_str == "500m" or "m" in dist_str


def test_eta_service_arrival_detection():
    """Verify proximity arrival trigger (< 35m)."""
    lat1, lon1 = 22.58040, 88.43780
    lat2, lon2 = 22.58042, 88.43782  # ~3 meters away

    (
        dist_m,
        eta_min,
        eta_str,
        bearing_deg,
        bearing_compass,
        dist_str,
    ) = eta_service.calculate_eta(
        responder_lat=lat1,
        responder_lon=lon1,
        target_lat=lat2,
        target_lon=lon2,
    )

    assert dist_m <= 35.0
    assert eta_min == 0.0
    assert eta_str == "Arrived"
    assert eta_service.is_arrived(dist_m) is True


def test_formatting_helpers():
    """Verify human-friendly distance and ETA formatters."""
    assert format_distance(250.0) == "250m"
    assert format_distance(1500.0) == "1.5km"
    assert format_eta(0.0, 10.0) == "Arrived"
    assert format_eta(0.5, 100.0) == "30 secs"
    assert format_eta(2.5, 300.0) == "2.5 mins"
    assert format_eta(4.0, 500.0) == "4 mins"


# ==============================================================================
# Helper to Seed User, Incident, and Responder in SQLite Test DB
# ==============================================================================

async def _setup_test_incident(
    client: AsyncClient,
) -> tuple[dict, dict, dict, str, str, uuid.UUID]:
    """Helper creating broadcaster user, SOS incident, and responding medic."""
    try:
        from tests.conftest import TestingSessionLocal
        session_factory = TestingSessionLocal
    except ImportError:
        from app.db.session import AsyncSessionLocal
        session_factory = AsyncSessionLocal

    phone_b = f"+9198{uuid.uuid4().int % 100000000:08d}"
    phone_r = f"+9197{uuid.uuid4().int % 100000000:08d}"

    # 1. Register victim broadcaster
    res_b = await client.post(
        "/api/v1/auth/register",
        json={
            "email": f"victim_{uuid.uuid4().hex[:8]}@nearhelp.ai",
            "password": "SecurePassword123!",
            "name": "Tanushree Das",
            "phone": phone_b,
            "blood_group": "B+",
        },
    )
    assert res_b.status_code == 201
    broadcaster_data = res_b.json()
    broadcaster_token = broadcaster_data["access_token"]
    broadcaster_id = uuid.UUID(broadcaster_data["user"]["id"])

    # 2. Register responder (Doctor / CPR Certified)
    res_r = await client.post(
        "/api/v1/auth/register",
        json={
            "email": f"doctor_{uuid.uuid4().hex[:8]}@nearhelp.ai",
            "password": "SecurePassword123!",
            "name": "Dr. Anirban Roy",
            "phone": phone_r,
            "blood_group": "O+",
        },
    )
    assert res_r.status_code == 201
    responder_data = res_r.json()
    responder_token = responder_data["access_token"]
    responder_id = uuid.UUID(responder_data["user"]["id"])

    # 3. Create SOS incident in DB
    incident_id = uuid.uuid4()
    async with session_factory() as session:
        event = SOSEvent(
            id=incident_id,
            broadcaster_id=broadcaster_id,
            crisis_type="medical",
            sub_type="cardiac_arrest",
            severity_score=90,
            priority="critical",
            description="Victim collapsed near elevator lobby",
            latitude=22.5804,
            longitude=88.4378,
            address="Godrej Waterside, Tower 1, DP Block, Sector V, Kolkata",
            status="RESPONDER_ACCEPTED",
        )
        session.add(event)

        # Update responder skills
        stmt_u = User.__table__.update().where(User.id == responder_id).values(
            skills=["DOCTOR", "CPR_CERTIFIED"],
            trust_score=95.0,
            device_info={"last_latitude": 22.5835, "last_longitude": 88.4410},
        )
        await session.execute(stmt_u)

        # Create SOSResponse record
        response_entry = SOSResponse(
            sos_event_id=incident_id,
            responder_id=responder_id,
            status="EN_ROUTE",
            initial_distance_meters=340.0,
            initial_eta_seconds=150,
        )
        session.add(response_entry)
        await session.commit()

    return broadcaster_data, responder_data, event.__dict__, broadcaster_token, responder_token, incident_id


# ==============================================================================
# 2. WebSocket Connection & Authentication Tests
# ==============================================================================

@pytest.mark.asyncio
async def test_tracking_ws_incident_not_found(client: AsyncClient):
    """Verify WebSocket connection fails when incident UUID does not exist."""
    fake_id = uuid.uuid4()
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{fake_id}") as ws:
        msg = ws.receive_text()
        data = json.loads(msg)
        assert data["type"] == "error"
        assert data["code"] == "INCIDENT_NOT_FOUND"


@pytest.mark.asyncio
async def test_tracking_ws_invalid_token(client: AsyncClient):
    """Verify WebSocket rejects connection when invalid JWT token is supplied."""
    _, _, _, _, _, incident_id = await _setup_test_incident(client)
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{incident_id}?token=invalid_jwt_token") as ws:
        msg = ws.receive_text()
        data = json.loads(msg)
        assert data["type"] == "error"
        assert data["code"] == "UNAUTHORIZED"


@pytest.mark.asyncio
async def test_tracking_ws_connect_and_receive_snapshot(client: AsyncClient):
    """Verify client connects with valid JWT, receives ConnectionAck and full TrackingSnapshot."""
    _, _, _, broadcaster_token, _, incident_id = await _setup_test_incident(client)
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{incident_id}?token={broadcaster_token}") as ws:
        # Frame 1: ConnectionAck
        msg1 = ws.receive_text()
        ack = json.loads(msg1)
        assert ack["type"] == "connection_ack"
        assert ack["incident_id"] == str(incident_id)
        assert ack["role"] == "victim"

        # Frame 2: TrackingSnapshot (Reconnection recovery)
        msg2 = ws.receive_text()
        snapshot = json.loads(msg2)
        assert snapshot["type"] == "tracking_snapshot"
        assert snapshot["incident_id"] == str(incident_id)
        assert snapshot["status"] == "RESPONDER_ACCEPTED"
        assert len(snapshot["responders"]) >= 1
        assert snapshot["responders"][0]["responder_name"] == "Dr. Anirban Roy"
        assert snapshot["responders"][0]["is_doctor"] is True


# ==============================================================================
# 3. Heartbeat & Telemetry Streaming Tests
# ==============================================================================

@pytest.mark.asyncio
async def test_tracking_ws_heartbeat_ping_pong(client: AsyncClient):
    """Verify ping message receives pong response with server timestamp."""
    _, _, _, broadcaster_token, _, incident_id = await _setup_test_incident(client)
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{incident_id}?token={broadcaster_token}") as ws:
        # Consume ack and snapshot
        ws.receive_text()
        ws.receive_text()

        # Send ping
        ws.send_text(json.dumps({"type": "ping", "timestamp": 1722000000.5}))

        # Receive pong
        msg = ws.receive_text()
        pong = json.loads(msg)
        assert pong["type"] == "pong"
        assert pong["client_timestamp"] == 1722000000.5
        assert "server_time" in pong


@pytest.mark.asyncio
async def test_tracking_ws_location_update_and_arrival(client: AsyncClient):
    """Verify responder streaming GPS coordinates and triggering arrival."""
    _, _, _, broadcaster_token, responder_token, incident_id = await _setup_test_incident(client)
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{incident_id}?token={responder_token}") as ws:
        # Consume initial ack and snapshot
        ws.receive_text()
        ws.receive_text()

        # Send location update (~100m away)
        loc_payload = {
            "type": "location_update",
            "latitude": 22.5810,
            "longitude": 88.4380,
            "heading": 34.0,
            "speed_mps": 1.4,
            "accuracy": 2.5,
        }
        ws.send_text(json.dumps(loc_payload))

        # Receive responder_update broadcast
        resp_msg = ws.receive_text()
        update_data = json.loads(resp_msg)
        assert update_data["type"] == "responder_update"
        assert update_data["responder_name"] == "Dr. Anirban Roy"
        assert update_data["distance_meters"] > 0
        assert "mins" in update_data["eta_formatted"] or "secs" in update_data["eta_formatted"]

        # Send location update right on victim coordinate (< 35m Arrival)
        arrival_loc = {
            "type": "location_update",
            "latitude": 22.58040,
            "longitude": 88.43780,
            "heading": 0.0,
            "speed_mps": 0.0,
            "accuracy": 1.0,
        }
        ws.send_text(json.dumps(arrival_loc))

        # Should receive responder_update (ARRIVED) and timeline_event
        frame_a = json.loads(ws.receive_text())
        frame_b = json.loads(ws.receive_text())

        types = [frame_a["type"], frame_b["type"]]
        assert "responder_update" in types
        assert "timeline_event" in types


@pytest.mark.asyncio
async def test_tracking_ws_status_update_and_snapshot_request(client: AsyncClient):
    """Verify responder manual status transition and get_snapshot query."""
    _, _, _, broadcaster_token, responder_token, incident_id = await _setup_test_incident(client)
    test_client = TestClient(fastapi_app)

    with test_client.websocket_connect(f"/ws/tracking/{incident_id}?token={responder_token}") as ws:
        # Consume ack and snapshot
        ws.receive_text()
        ws.receive_text()

        # Send status update
        ws.send_text(json.dumps({"type": "status_update", "status": "ON_SCENE", "note": "AED connected"}))

        # Receive timeline_event followed by fresh tracking_snapshot
        msg1 = json.loads(ws.receive_text())
        assert msg1["type"] == "timeline_event"
        assert msg1["event_type"] == "RESPONDER_ON_SCENE"

        msg2 = json.loads(ws.receive_text())
        assert msg2["type"] == "tracking_snapshot"

        # Explicitly request get_snapshot
        ws.send_text(json.dumps({"type": "get_snapshot"}))
        msg3 = json.loads(ws.receive_text())
        assert msg3["type"] == "tracking_snapshot"


# ==============================================================================
# 4. REST API Tracking Snapshot & Location Fallback Tests
# ==============================================================================

@pytest.mark.asyncio
async def test_tracking_rest_endpoints(client: AsyncClient):
    """Verify REST API snapshot retrieval and HTTP POST location update fallback."""
    _, _, _, broadcaster_token, responder_token, incident_id = await _setup_test_incident(client)

    # 1. GET /api/v1/sos/{incident_id}/tracking
    res_snap = await client.get(f"/api/v1/sos/{incident_id}/tracking")
    assert res_snap.status_code == 200
    snap_data = res_snap.json()
    assert snap_data["type"] == "tracking_snapshot"
    assert snap_data["incident_id"] == str(incident_id)
    assert len(snap_data["responders"]) >= 1

    # 2. POST /api/v1/sos/{incident_id}/tracking/location (HTTP fallback)
    loc_body = {
        "type": "location_update",
        "latitude": 22.5820,
        "longitude": 88.4390,
        "heading": 180.0,
        "speed_mps": 2.2,
        "accuracy": 3.0,
    }
    headers = {"Authorization": f"Bearer {responder_token}"}
    res_post = await client.post(
        f"/api/v1/sos/{incident_id}/tracking/location",
        json=loc_body,
        headers=headers,
    )
    assert res_post.status_code == 200
    post_data = res_post.json()
    assert post_data["type"] == "responder_update"
    assert post_data["responder_name"] == "Dr. Anirban Roy"
    assert post_data["distance_meters"] > 0
