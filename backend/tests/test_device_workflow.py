"""Verify two users share a real incident through REST, sockets and persisted history."""
import uuid
from datetime import UTC, datetime, timedelta
from unittest.mock import AsyncMock, patch

import pytest
from fastapi.testclient import TestClient
from httpx import AsyncClient
from sqlalchemy import select

from app.main import app
from app.models.user import User
from app.services.geo_service import geo_service
from app.services.notification_service import NotificationService
from tests.conftest import TestingSessionLocal
from tests.test_sos_engine import _create_user


@pytest.mark.asyncio
async def test_two_users_create_accept_stream_chat_reconnect_and_resolve(client: AsyncClient):
    victim_headers, victim = await _create_user(client, name="Victim")
    responder_headers, responder = await _create_user(client, name="Responder")
    availability = await client.put("/api/v1/users/me/location", headers=responder_headers, json={
        "latitude": 22.574, "longitude": 88.365, "available": True,
    })
    assert availability.status_code == 200
    async with TestingSessionLocal() as db:
        user = (await db.execute(select(User).where(User.id == uuid.UUID(responder["id"])))).scalars().one()
        assert geo_service.is_available(user)
        assert user.device_info["last_latitude"] == 22.574
    request = {"latitude": 22.5726, "longitude": 88.3639, "description": "Need help", "crisis_type": "medical"}
    headers = {**victim_headers, "Idempotency-Key": str(uuid.uuid4())}
    created = await client.post("/api/v1/sos/create", headers=headers, json=request)
    assert created.status_code == 201
    incident = created.json()
    incident_id = incident["id"]
    assert incident["broadcaster_id"] == victim["id"]
    assert incident["candidates_notified_count"] == 0
    assert responder["id"] in [r["responder_id"] for r in incident["top_ranked_responders"]]
    replay = await client.post("/api/v1/sos/create", headers=headers, json=request)
    assert replay.json()["id"] == incident_id
    from app.core.middleware import _in_memory_idempotency
    _in_memory_idempotency.clear()
    replay = await client.post("/api/v1/sos/create", headers=headers, json=request)
    assert replay.status_code == 201
    assert replay.json()["id"] == incident_id
    conflict = await client.post("/api/v1/sos/create", headers=headers, json={**request, "description": "different"})
    assert conflict.status_code == 409
    feed = await client.get("/api/v1/sos/active", headers=responder_headers)
    assert incident_id in [e["id"] for e in feed.json()["active_events"]]
    response = await client.post(f"/api/v1/sos/{incident_id}/respond", headers=responder_headers,
                                 json={"status": "ACCEPTED", "current_latitude": 22.574, "current_longitude": 88.365})
    assert response.status_code == 200
    with patch("app.main.init_db", new=AsyncMock()), TestClient(app) as sockets:
        vt = victim_headers["Authorization"].split()[1]
        rt = responder_headers["Authorization"].split()[1]
        with sockets.websocket_connect(f"/ws/tracking/{incident_id}?token={vt}") as a:
            assert a.receive_json()["role"] == "victim"
            assert a.receive_json()["status"] == "RESPONDER_ACCEPTED"
            with sockets.websocket_connect(f"/ws/tracking/{incident_id}?token={rt}") as b:
                assert b.receive_json()["role"] == "responder"
                b.receive_json()
                b.send_json({"type": "location_update", "latitude": 22.5741, "longitude": 88.3651})
                update_a, update_b = a.receive_json(), b.receive_json()
                assert update_a["type"] == update_b["type"] == "responder_update"
                assert update_a["responder_id"] == responder["id"]
                assert update_a["latitude"] == 22.5741
                a.send_json({"type": "chat_message", "text": "I am near the gate"})
                assert a.receive_json()["text"] == b.receive_json()["text"] == "I am near the gate"
                b.send_json({"type": "chat_message", "text": "I can see you"})
                assert a.receive_json()["sender_id"] == b.receive_json()["sender_id"] == responder["id"]
        detail = (await client.get(f"/api/v1/sos/{incident_id}")).json()
        texts = [e["details"]["text"] for e in detail["timeline"] if e["event_type"] == "chat_message"]
        assert texts == ["I am near the gate", "I can see you"]
        with sockets.websocket_connect(f"/ws/tracking/{incident_id}?token={vt}") as a:
            a.receive_json()
            assert a.receive_json()["responders"][0]["latitude"] == 22.5741
        assert (await client.put(f"/api/v1/sos/{incident_id}/resolve", headers=responder_headers, json={})).status_code == 403
        assert (await client.put(f"/api/v1/sos/{incident_id}/resolve", headers=victim_headers, json={})).status_code == 200
        assert (await client.put(f"/api/v1/sos/{incident_id}/resolve", headers=victim_headers, json={})).status_code == 409
        assert incident_id not in [e["id"] for e in (await client.get("/api/v1/sos/active")).json()["active_events"]]



@pytest.mark.asyncio
async def test_cancellation_is_owner_only_and_stops_responses(client: AsyncClient):
    owner, _ = await _create_user(client)
    stranger, _ = await _create_user(client)
    incident = (await client.post("/api/v1/sos/create", headers=owner, json={"latitude": 0, "longitude": 0})).json()["id"]
    assert (await client.put(f"/api/v1/sos/{incident}/cancel", headers=stranger)).status_code == 403
    assert (await client.put(f"/api/v1/sos/{incident}/cancel", headers=owner)).json()["status"] == "CANCELLED"
    assert (await client.put(f"/api/v1/sos/{incident}/cancel", headers=owner)).status_code == 200
    assert (await client.post(f"/api/v1/sos/{incident}/respond", headers=stranger, json={})).status_code == 400


@pytest.mark.asyncio
async def test_availability_requires_opt_in_and_freshness(client: AsyncClient):
    headers, info = await _create_user(client, lat=22.57, lon=88.36)
    async with TestingSessionLocal() as db:
        user = (await db.execute(select(User).where(User.id == uuid.UUID(info["id"])))).scalars().one()
        user.device_info = {"last_latitude": 22.57, "last_longitude": 88.36, "available": False}
        assert not geo_service.is_available(user)
        user.device_info = {**user.device_info, "available": True, "location_updated_at": (datetime.now(UTC) - timedelta(hours=1)).isoformat()}
        assert not geo_service.is_available(user)
    assert (await client.put("/api/v1/users/me/location", headers=headers, json={"latitude": 100, "longitude": 0})).status_code == 422


@pytest.mark.asyncio
async def test_push_reports_actual_fcm_acceptance():
    with patch("app.services.notification_service.init_firebase", return_value=True), patch("app.services.notification_service.messaging.send", return_value="message-id") as send:
        assert await NotificationService._send("real-token", "SOS", "Help", {"sos_event_id": "id", "skills": ["CPR"]})
        message = send.call_args.args[0]
        assert message.data["skills"] == '["CPR"]'
        assert message.android.priority == "high"
    with patch("app.services.notification_service.messaging.send") as send:
        assert not await NotificationService._send(None, "SOS", "Help", {})
        send.assert_not_called()
    with patch("app.services.notification_service.init_firebase", return_value=True), patch("app.services.notification_service.messaging.send", side_effect=RuntimeError("offline")):
        assert not await NotificationService._send("token", "SOS", "Help", {})


@pytest.mark.asyncio
async def test_health_checks_the_database_instead_of_claiming_connectivity(client: AsyncClient):
    assert (await client.get("/health")).json()["database"] == "connected"
    with patch("sqlalchemy.ext.asyncio.AsyncSession.execute", side_effect=RuntimeError("connection lost")):
        response = await client.get("/health")
        assert response.status_code == 503
        assert response.json()["database"] == "disconnected"
