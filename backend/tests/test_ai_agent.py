"""NearHelp AI — Backend Module 10 AI Crisis Assistant Agent Test Suite."""

import json

import pytest
from fastapi.testclient import TestClient
from httpx import AsyncClient

from app.main import app as fastapi_app


@pytest.mark.asyncio
async def test_backend_agent_protocols_catalog(client: AsyncClient):
    """Verify backend proxy returns full catalog of grounded first-aid protocols."""
    resp = await client.get("/api/v1/ai/agent/protocols")
    assert resp.status_code == 200
    data = resp.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert data[0]["condition_id"] == "cardiac_arrest"
    assert data[0]["cpr_bpm"] == 110


@pytest.mark.asyncio
async def test_backend_agent_protocol_by_condition(client: AsyncClient):
    """Verify fetching specific condition protocol."""
    resp = await client.get("/api/v1/ai/agent/protocols/cardiac_arrest")
    assert resp.status_code == 200
    proto = resp.json()
    assert proto["condition_id"] == "cardiac_arrest"
    assert proto["severity_level"] == 5
    assert len(proto["steps"]) >= 4
    assert len(proto["citations"]) >= 1


@pytest.mark.asyncio
async def test_backend_agent_chat_dialogue(client: AsyncClient):
    """Verify agent chat dialogue turn through backend gateway."""
    payload = {
        "session_id": "backend-test-001",
        "text": "How deep should chest compressions be?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/ai/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert "5 to 6 cm" in data["reply_text"] or "5-6 cm" in data["reply_text"] or "5\u20136 cm" in data["reply_text"]
    assert data["cpr_bpm"] == 110
    assert data["legal_shield_applied"] is True


@pytest.mark.asyncio
async def test_backend_agent_chat_contraindication_alert(client: AsyncClient):
    """Verify asking to give water to an unconscious victim triggers contraindication warning."""
    payload = {
        "session_id": "backend-test-002",
        "text": "The collapsed person is unresponsive. Can I give him water to drink?",
        "role": "bystander",
    }
    resp = await client.post("/api/v1/ai/agent/chat", json=payload)
    assert resp.status_code == 200
    data = resp.json()
    assert len(data["contraindications"]) >= 1
    assert data["contraindications"][0]["flag"] == "NO_ORAL_FLUIDS_UNCONSCIOUS"
    assert "pulmonary aspiration" in data["reply_text"].lower() or "airway" in data["reply_text"].lower()


@pytest.mark.asyncio
async def test_backend_agent_handover_report(client: AsyncClient):
    """Verify clinical handover summary report generation."""
    payload = {
        "session_id": "backend-test-003",
        "text": "Ambulance arrived.",
    }
    resp = await client.post("/api/v1/ai/agent/handover", json=payload)
    assert resp.status_code == 200
    report = resp.json()
    assert "report_id" in report
    assert report["severity_level"] == 5
    assert report["cpr_metronome_used"] is True
    assert "SHA256:" in report["digital_signature_hash"]


def test_backend_agent_ws_ping_pong():
    """Verify backend WebSocket accepts ping and sends pong."""
    test_client = TestClient(fastapi_app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        ws.send_text(json.dumps({"action": "ping", "session_id": "ws-backend-001"}))
        raw = ws.receive_text()
        frame = json.loads(raw)
        assert frame["type"] == "pong"
        assert frame["session_id"] == "ws-backend-001"
        assert frame["payload"]["pong"] is True


def test_backend_agent_ws_full_dialogue_flow():
    """Verify backend WebSocket init, user_message streaming, metronome, and step toggle."""
    test_client = TestClient(fastapi_app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        # 1. Init
        ws.send_text(json.dumps({"action": "init", "session_id": "ws-backend-002", "condition_id": "cardiac_arrest"}))
        raw1 = ws.receive_text()
        frame1 = json.loads(raw1)
        assert frame1["type"] == "protocol_update"
        assert frame1["payload"]["condition_id"] == "cardiac_arrest"

        # 2. Metronome Sync
        ws.send_text(json.dumps({"action": "set_metronome", "session_id": "ws-backend-002", "metronome_active": True}))
        raw2 = ws.receive_text()
        frame2 = json.loads(raw2)
        assert frame2["type"] == "metronome_sync"
        assert frame2["payload"]["cpr_bpm"] == 110

        # 3. User Message
        ws.send_text(json.dumps({"action": "user_message", "session_id": "ws-backend-002", "text": "What is the CPR rhythm?"}))
        
        # Read chunks until final message
        final_msg = None
        for _ in range(50):
            raw = ws.receive_text()
            frame = json.loads(raw)
            if frame["type"] == "agent_message":
                final_msg = frame
                break

        assert final_msg is not None
        assert "110" in final_msg["payload"]["reply_text"]

        # 4. Step Toggle
        ws.send_text(json.dumps({"action": "step_toggle", "session_id": "ws-backend-002", "step_number": 1, "completed": True}))
        raw_step = ws.receive_text()
        frame_step = json.loads(raw_step)
        assert frame_step["type"] == "protocol_update"
        assert frame_step["payload"]["completed_steps"] == [1]
