"""NearHelp AI — Backend WebSocket Gateway for AI Crisis Assistant (/ws/ai/chat).

Handles real-time bidirectional streaming dialogue, step checklist state sync,
AHA 110 BPM CPR rhythm triggers, and instant clinical contraindication alerts.
"""

import asyncio
import json
import logging
from datetime import UTC, datetime

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.schemas.ai import AgentChatRequest
from app.services.ai_client import ai_client

logger = logging.getLogger(__name__)

router = APIRouter(tags=["AI Crisis Assistant WebSocket"])


@router.websocket("/ws/ai/chat")
@router.websocket("/api/v1/ws/ai/chat")
async def backend_ai_chat_websocket(websocket: WebSocket):
    """WebSocket gateway for AI Emergency Crisis Assistant with resilient streaming fallback."""
    session_id = "default-session"
    completed_steps: list[int] = []
    cpr_active = True

    try:
        await websocket.accept()
        logger.info("Backend AI WebSocket client connected.")

        while True:
            raw_text = await websocket.receive_text()
            try:
                msg = json.loads(raw_text)
            except Exception as e:
                await websocket.send_text(
                    json.dumps({
                        "type": "error",
                        "session_id": session_id,
                        "payload": {"error": f"Invalid JSON payload: {e!s}"},
                    })
                )
                continue

            action = msg.get("action", "ping")
            session_id = msg.get("session_id") or session_id
            now_iso = datetime.now(UTC).isoformat()

            if action == "ping":
                await websocket.send_text(
                    json.dumps({
                        "type": "pong",
                        "session_id": session_id,
                        "payload": {"pong": True, "server_time": now_iso},
                        "timestamp": now_iso,
                    })
                )

            elif action == "init":
                cond_id = msg.get("condition_id", "cardiac_arrest")
                proto = await ai_client.get_protocol(cond_id)
                await websocket.send_text(
                    json.dumps({
                        "type": "protocol_update",
                        "session_id": session_id,
                        "payload": {
                            "condition_id": proto.condition_id,
                            "protocol_title": proto.protocol_title,
                            "steps": [s.model_dump() for s in proto.steps],
                            "completed_steps": completed_steps,
                            "cpr_metronome_active": cpr_active,
                            "cpr_bpm": proto.cpr_bpm or 110,
                            "legal_shield": proto.legal_shield,
                            "citations": [c.model_dump() for c in proto.citations],
                        },
                        "timestamp": now_iso,
                    })
                )

            elif action == "user_message":
                text = msg.get("text", "")
                role = msg.get("role", "bystander")

                chat_req = AgentChatRequest(
                    session_id=session_id,
                    text=text,
                    role=role,
                    completed_steps=completed_steps,
                )
                agent_resp = await ai_client.agent_chat(chat_req)

                # Contraindication frame
                if agent_resp.contraindications:
                    for alert in agent_resp.contraindications:
                        await websocket.send_text(
                            json.dumps({
                                "type": "contraindication_alert",
                                "session_id": session_id,
                                "payload": alert.model_dump(),
                                "timestamp": now_iso,
                            })
                        )

                # Simulated chunk streaming
                words = agent_resp.reply_text.split()
                chunk_size = 6
                for i in range(0, len(words), chunk_size):
                    chunk_text = " ".join(words[i : i + chunk_size]) + " "
                    await websocket.send_text(
                        json.dumps({
                            "type": "agent_chunk",
                            "session_id": session_id,
                            "payload": {"chunk": chunk_text, "is_final": (i + chunk_size >= len(words))},
                            "timestamp": now_iso,
                        })
                    )
                    await asyncio.sleep(0.015)

                # Full message frame
                await websocket.send_text(
                    json.dumps({
                        "type": "agent_message",
                        "session_id": session_id,
                        "payload": agent_resp.model_dump(),
                        "timestamp": now_iso,
                    })
                )

            elif action == "step_toggle":
                step_no = msg.get("step_number", 1)
                is_comp = msg.get("completed", True)
                if is_comp and step_no not in completed_steps:
                    completed_steps.append(step_no)
                    completed_steps.sort()
                elif not is_comp and step_no in completed_steps:
                    completed_steps.remove(step_no)

                proto = await ai_client.get_protocol("cardiac_arrest")
                total = len(proto.steps) or 4
                pct = int((len(completed_steps) / total) * 100)
                await websocket.send_text(
                    json.dumps({
                        "type": "protocol_update",
                        "session_id": session_id,
                        "payload": {
                            "completed_steps": completed_steps,
                            "total_steps": total,
                            "progress_percentage": pct,
                            "all_completed": len(completed_steps) >= total,
                        },
                        "timestamp": now_iso,
                    })
                )

            elif action == "set_metronome":
                cpr_active = msg.get("metronome_active", True)
                await websocket.send_text(
                    json.dumps({
                        "type": "metronome_sync",
                        "session_id": session_id,
                        "payload": {
                            "cpr_metronome_active": cpr_active,
                            "cpr_bpm": 110,
                            "cadence_ms": 545.45,
                        },
                        "timestamp": now_iso,
                    })
                )

            elif action == "request_handover":
                handover = await ai_client.generate_handover(
                    AgentChatRequest(session_id=session_id, text="Paramedic Handover")
                )
                await websocket.send_text(
                    json.dumps({
                        "type": "handover_report",
                        "session_id": session_id,
                        "payload": handover.model_dump(),
                        "timestamp": now_iso,
                    })
                )

    except WebSocketDisconnect:
        logger.info("Backend AI WebSocket client disconnected gracefully.")
    except Exception as e:
        logger.warning("Backend AI WebSocket error: %s", e)
