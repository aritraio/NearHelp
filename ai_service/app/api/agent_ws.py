"""NearHelp AI — Production Real-Time WebSocket Endpoint (/ws/ai/chat).

Handles bidirectional streaming dialogue, step checklist state sync,
AHA 110 BPM CPR rhythm triggers, and instant clinical contraindication alerts.
"""

import asyncio
import json
import logging
from datetime import datetime, timezone

from app.agent.gemini_agent import emergency_agent_service, handover_node
from app.agent.knowledge import get_grounded_protocol
from app.agent.state import EmergencyAgentState
from app.schemas.agent import (
    AgentChatRequest,
    WebSocketServerFrame,
)
from fastapi import APIRouter, WebSocket, WebSocketDisconnect

logger = logging.getLogger(__name__)

router = APIRouter(tags=["AI Crisis Assistant WebSocket"])


class AgentConnectionManager:
    """Manages active WebSocket connections for emergency agent dialogue."""

    def __init__(self):
        self.active_connections: dict[str, list[WebSocket]] = {}

    async def connect(self, session_id: str, websocket: WebSocket):
        await websocket.accept()
        if session_id not in self.active_connections:
            self.active_connections[session_id] = []
        self.active_connections[session_id].append(websocket)
        logger.info("WebSocket connected for session %s (total: %d)", session_id, len(self.active_connections[session_id]))

    def disconnect(self, session_id: str, websocket: WebSocket):
        if session_id in self.active_connections:
            if websocket in self.active_connections[session_id]:
                self.active_connections[session_id].remove(websocket)
            if not self.active_connections[session_id]:
                del self.active_connections[session_id]
        logger.info("WebSocket disconnected for session %s", session_id)

    async def send_frame(self, websocket: WebSocket, frame: WebSocketServerFrame):
        """Send JSON frame to specific socket."""
        try:
            await websocket.send_text(frame.model_dump_json())
        except Exception as e:
            logger.warning("Error sending WebSocket frame: %s", e)

    async def broadcast_session(self, session_id: str, frame: WebSocketServerFrame):
        """Broadcast frame to all connections in a session."""
        if session_id in self.active_connections:
            dead_sockets = []
            for ws in self.active_connections[session_id]:
                try:
                    await ws.send_text(frame.model_dump_json())
                except Exception:
                    dead_sockets.append(ws)
            for dead in dead_sockets:
                self.disconnect(session_id, dead)


ws_manager = AgentConnectionManager()


@router.websocket("/ws/ai/chat")
@router.websocket("/api/v1/ws/ai/chat")
async def ai_chat_websocket_endpoint(websocket: WebSocket):
    """Production WebSocket endpoint for AI Emergency Crisis Assistant."""
    session_id = "default-session"

    try:
        # Initial accept
        await websocket.accept()
        logger.info("Accepted raw WebSocket connection.")

        while True:
            raw_data = await websocket.receive_text()
            try:
                msg_dict = json.loads(raw_data)
            except Exception as json_err:
                await websocket.send_text(
                    json.dumps({
                        "type": "error",
                        "session_id": session_id,
                        "payload": {"error": f"Invalid JSON frame: {json_err!s}"},
                    })
                )
                continue

            action = msg_dict.get("action", "ping")
            session_id = msg_dict.get("session_id") or session_id
            now_iso = datetime.now(timezone.utc).isoformat()

            # Handle action types
            if action == "ping":
                pong_frame = WebSocketServerFrame(
                    type="pong",
                    session_id=session_id,
                    payload={"pong": True, "server_time": now_iso},
                    timestamp=now_iso,
                )
                await websocket.send_text(pong_frame.model_dump_json())

            elif action == "init":
                condition_id = msg_dict.get("condition_id", "cardiac_arrest")
                role = msg_dict.get("role", "bystander")
                session = emergency_agent_service.get_or_create_session(
                    session_id=session_id,
                    condition_id=condition_id,
                    role=role,
                )
                protocol = get_grounded_protocol(condition_id)

                init_frame = WebSocketServerFrame(
                    type="protocol_update",
                    session_id=session_id,
                    payload={
                        "condition_id": condition_id,
                        "protocol_title": protocol.protocol_title,
                        "steps": [s.model_dump() for s in protocol.steps],
                        "completed_steps": session.get("completed_steps", []),
                        "cpr_metronome_active": session.get("cpr_metronome_active", True),
                        "cpr_bpm": protocol.cpr_bpm or 110,
                        "legal_shield": protocol.legal_shield,
                        "citations": [c.model_dump() for c in protocol.citations],
                    },
                    timestamp=now_iso,
                )
                await websocket.send_text(init_frame.model_dump_json())

            elif action == "user_message":
                text = msg_dict.get("text", "")
                role = msg_dict.get("role", "bystander")

                chat_req = AgentChatRequest(
                    session_id=session_id,
                    text=text,
                    role=role,
                )
                agent_resp = await emergency_agent_service.execute_turn(chat_req)

                # Emit instant contraindication alert if flagged
                if agent_resp.contraindications:
                    for alert in agent_resp.contraindications:
                        contra_frame = WebSocketServerFrame(
                            type="contraindication_alert",
                            session_id=session_id,
                            payload=alert.model_dump(),
                            timestamp=now_iso,
                        )
                        await websocket.send_text(contra_frame.model_dump_json())

                # Send simulated token streaming chunks for UX responsiveness
                words = agent_resp.reply_text.split()
                chunk_size = 6
                for i in range(0, len(words), chunk_size):
                    chunk_text = " ".join(words[i : i + chunk_size]) + " "
                    chunk_frame = WebSocketServerFrame(
                        type="agent_chunk",
                        session_id=session_id,
                        payload={"chunk": chunk_text, "is_final": (i + chunk_size >= len(words))},
                        timestamp=now_iso,
                    )
                    await websocket.send_text(chunk_frame.model_dump_json())
                    await asyncio.sleep(0.015)

                # Send full final structured message frame
                full_frame = WebSocketServerFrame(
                    type="agent_message",
                    session_id=session_id,
                    payload=agent_resp.model_dump(),
                    timestamp=now_iso,
                )
                await websocket.send_text(full_frame.model_dump_json())

            elif action == "step_toggle":
                step_number = msg_dict.get("step_number", 1)
                completed = msg_dict.get("completed", True)
                completed_steps, total, pct, all_done = emergency_agent_service.toggle_step(
                    session_id=session_id,
                    step_number=step_number,
                    completed=completed,
                )
                step_frame = WebSocketServerFrame(
                    type="protocol_update",
                    session_id=session_id,
                    payload={
                        "completed_steps": completed_steps,
                        "total_steps": total,
                        "progress_percentage": pct,
                        "all_completed": all_done,
                    },
                    timestamp=now_iso,
                )
                await websocket.send_text(step_frame.model_dump_json())

            elif action == "set_metronome":
                active = msg_dict.get("metronome_active", True)
                session = emergency_agent_service.get_or_create_session(session_id)
                session["cpr_metronome_active"] = active
                metro_frame = WebSocketServerFrame(
                    type="metronome_sync",
                    session_id=session_id,
                    payload={
                        "cpr_metronome_active": active,
                        "cpr_bpm": 110,
                        "cadence_ms": 545.45,
                    },
                    timestamp=now_iso,
                )
                await websocket.send_text(metro_frame.model_dump_json())

            elif action == "request_handover":
                session = emergency_agent_service.get_or_create_session(session_id)
                state: EmergencyAgentState = {
                    "session_id": session_id,
                    "condition_id": session.get("condition_id", "cardiac_arrest"),
                    "completed_steps": session.get("completed_steps", [1, 2, 3]),
                    "cpr_metronome_active": session.get("cpr_metronome_active", True),
                    "aed_attached": session.get("aed_attached", True),
                    "severity_level": 5,
                }
                res = handover_node(state)
                handover_frame = WebSocketServerFrame(
                    type="handover_report",
                    session_id=session_id,
                    payload=res["handover_report"],
                    timestamp=now_iso,
                )
                await websocket.send_text(handover_frame.model_dump_json())

    except WebSocketDisconnect:
        logger.info("WebSocket disconnected gracefully for session %s", session_id)
    except Exception as ws_err:
        logger.warning("WebSocket exception for session %s: %s", session_id, ws_err)
