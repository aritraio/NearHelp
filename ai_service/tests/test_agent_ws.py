"""NearHelp AI — AI Crisis Assistant WebSocket Test Suite (/ws/ai/chat)."""

import json

from app.main import app
from fastapi.testclient import TestClient


def test_agent_ws_ping_pong():
    """Verify sending ping over WebSocket receives structured pong frame."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        ws.send_text(json.dumps({"action": "ping", "session_id": "test-ws-001"}))
        raw = ws.receive_text()
        frame = json.loads(raw)
        assert frame["type"] == "pong"
        assert frame["session_id"] == "test-ws-001"
        assert frame["payload"]["pong"] is True
        assert "server_time" in frame["payload"]


def test_agent_ws_init_session():
    """Verify sending init frame initializes session and returns protocol update."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        init_msg = {
            "action": "init",
            "session_id": "test-ws-002",
            "condition_id": "cardiac_arrest",
            "role": "bystander",
        }
        ws.send_text(json.dumps(init_msg))
        raw = ws.receive_text()
        frame = json.loads(raw)
        assert frame["type"] == "protocol_update"
        assert frame["session_id"] == "test-ws-002"
        assert frame["payload"]["condition_id"] == "cardiac_arrest"
        assert frame["payload"]["cpr_bpm"] == 110
        assert len(frame["payload"]["steps"]) >= 4


def test_agent_ws_user_message_dialogue():
    """Verify user message streams chunks and delivers structured agent_message with citations."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        msg = {
            "action": "user_message",
            "session_id": "test-ws-003",
            "text": "How deep should chest compressions be?",
            "role": "bystander",
        }
        ws.send_text(json.dumps(msg))

        # Receive streaming chunks until final agent_message
        received_chunks = []
        final_message = None

        for _ in range(50):
            raw = ws.receive_text()
            frame = json.loads(raw)
            if frame["type"] == "agent_chunk":
                received_chunks.append(frame["payload"]["chunk"])
            elif frame["type"] == "agent_message":
                final_message = frame
                break

        assert len(received_chunks) > 0
        assert final_message is not None
        assert final_message["session_id"] == "test-ws-003"
        assert "5 to 6 cm" in final_message["payload"]["reply_text"] or "5–6 cm" in final_message["payload"]["reply_text"]
        assert final_message["payload"]["cpr_bpm"] == 110
        assert len(final_message["payload"]["citations"]) >= 1


def test_agent_ws_contraindication_alert_emission():
    """Verify hazardous action emits instant contraindication_alert frame."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        msg = {
            "action": "user_message",
            "session_id": "test-ws-004",
            "text": "Should I give water or oral medicine to the unconscious victim?",
            "role": "bystander",
        }
        ws.send_text(json.dumps(msg))

        # First frame should be contraindication_alert
        raw1 = ws.receive_text()
        frame1 = json.loads(raw1)
        assert frame1["type"] == "contraindication_alert"
        assert frame1["payload"]["flag"] == "NO_ORAL_FLUIDS_UNCONCONSCIOUS" or "NO_ORAL_FLUIDS" in frame1["payload"]["flag"]
        assert frame1["payload"]["severity"] == "CRITICAL"


def test_agent_ws_step_toggle():
    """Verify toggling protocol step completion over WebSocket."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        ws.send_text(json.dumps({"action": "init", "session_id": "test-ws-005", "condition_id": "cardiac_arrest"}))
        _ = ws.receive_text()  # consume init frame

        # Toggle step 1
        ws.send_text(json.dumps({
            "action": "step_toggle",
            "session_id": "test-ws-005",
            "step_number": 1,
            "completed": True,
        }))
        raw = ws.receive_text()
        frame = json.loads(raw)
        assert frame["type"] == "protocol_update"
        assert frame["payload"]["completed_steps"] == [1]
        assert frame["payload"]["progress_percentage"] == 25


def test_agent_ws_set_metronome():
    """Verify synchronizing 110 BPM CPR rhythm metronome state."""
    test_client = TestClient(app)
    with test_client.websocket_connect("/ws/ai/chat") as ws:
        ws.send_text(json.dumps({
            "action": "set_metronome",
            "session_id": "test-ws-006",
            "metronome_active": True,
        }))
        raw = ws.receive_text()
        frame = json.loads(raw)
        assert frame["type"] == "metronome_sync"
        assert frame["payload"]["cpr_metronome_active"] is True
        assert frame["payload"]["cpr_bpm"] == 110
        assert frame["payload"]["cadence_ms"] == 545.45
