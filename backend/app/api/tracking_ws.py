"""NearHelp AI — Live Location Tracking WebSocket & Streaming Endpoints."""

import json
import logging
import uuid
from datetime import datetime

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Query,
    WebSocket,
    WebSocketDisconnect,
    status,
)
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from app.core.dependencies import get_current_user
from app.core.security import decode_token, verify_token_type
from app.core.websocket_manager import ws_manager
from app.db.session import AsyncSessionLocal, get_db
from app.models.response import SOSResponse
from app.models.sos_event import SOSEvent
from app.models.user import User
from app.schemas.tracking import (
    ActionLogMessage,
    ChatMessage,
    ErrorMessage,
    HeartbeatAck,
    LocationUpdateMessage,
    ResponderTrackingUpdate,
    StatusUpdateMessage,
    TimelineTrackingEvent,
    TrackingSnapshot,
)
from app.services.tracking_service import tracking_service

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Live Tracking Stream"])


@asynccontextmanager
async def _get_db_session() -> AsyncGenerator[AsyncSession, None]:
    """Provide active async session respecting dependency overrides in test/production."""
    from app.main import app as fastapi_app
    override = fastapi_app.dependency_overrides.get(get_db)
    if override:
        gen = override()
        session = await anext(gen)
        try:
            yield session
        finally:
            try:
                await anext(gen)
            except (StopAsyncIteration, Exception):
                pass
    else:
        async with AsyncSessionLocal() as session:
            try:
                yield session
                await session.commit()
            except Exception:
                await session.rollback()
                raise


async def _authenticate_ws_token(
    db: AsyncSession, token: str | None
) -> tuple[User | None, uuid.UUID | None, str | None, bool]:
    """Authenticate and extract user identity from JWT query parameter or initial auth frame."""
    if not token:
        return None, None, None, False

    try:
        payload = decode_token(token)
        if not verify_token_type(payload, "access"):
            return None, None, None, False

        user_id_str = payload.get("sub")
        is_anon = payload.get("is_anonymous", False)
        if not user_id_str:
            return None, None, None, False

        user_id = uuid.UUID(user_id_str)
        stmt = select(User).where(User.id == user_id)
        res = await db.execute(stmt)
        user = res.scalars().first()
        user_name = user.name if user else ("Anonymous User" if is_anon else None)
        return user, user_id, user_name, is_anon
    except Exception as e:
        logger.debug(f"WebSocket auth token validation error: {e}")
        return None, None, None, False


async def _handle_tracking_websocket(
    websocket: WebSocket,
    incident_id: uuid.UUID,
    token: str | None = None,
) -> None:
    """Core WebSocket handler for live location streaming and bidirectional telemetry."""
    async with _get_db_session() as db:
        # 1. Verify that the SOS emergency incident exists
        stmt = select(SOSEvent).where(SOSEvent.id == incident_id)
        event_res = await db.execute(stmt)
        event = event_res.scalars().first()

        if not event:
            await websocket.accept()
            err = ErrorMessage(
                code="INCIDENT_NOT_FOUND",
                message=f"SOS Incident '{incident_id}' was not found.",
            )
            await websocket.send_text(err.model_dump_json())
            await websocket.close(code=4004, reason="Incident not found")
            return

        # 2. Authenticate user from query parameter token
        user, user_id, user_name, is_anon = await _authenticate_ws_token(db, token)

        if not user and not is_anon and token:
            # Token provided was invalid
            await websocket.accept()
            err = ErrorMessage(
                code="UNAUTHORIZED",
                message="Invalid or expired authentication credentials.",
            )
            await websocket.send_text(err.model_dump_json())
            await websocket.close(code=1008, reason="Policy violation: Unauthorized")
            return

        # Determine user role in this incident
        role = "guest"
        if user_id:
            if event.broadcaster_id == user_id or is_anon:
                role = "victim"
            elif getattr(user, "is_superuser", False):
                role = "admin"
            else:
                resp_stmt = select(SOSResponse).where(
                    SOSResponse.sos_event_id == incident_id,
                    SOSResponse.responder_id == user_id,
                )
                resp_res = await db.execute(resp_stmt)
                if resp_res.scalars().first():
                    role = "responder"
                else:
                    role = "dispatcher"

        # 3. Register client in connection manager
        client_conn = await ws_manager.connect(
            websocket=websocket,
            incident_id=incident_id,
            user_id=user_id,
            user_name=user_name,
            role=role,
        )

        # 4. Deliver immediate full tracking snapshot upon connection (Reconnection state recovery)
        current_connected = ws_manager.get_connected_count(incident_id)
        snapshot = await tracking_service.build_tracking_snapshot(
            db=db,
            incident_id=incident_id,
            connected_clients_count=current_connected,
        )
        if snapshot:
            await ws_manager.send_personal_json(websocket, snapshot)

    # 5. Continuous message listener loop
    try:
        while True:
            raw_text = await websocket.receive_text()
            if not raw_text or not raw_text.strip():
                continue

            try:
                data = json.loads(raw_text)
            except Exception:
                err = ErrorMessage(
                    code="MALFORMED_JSON",
                    message="Received payload is not valid JSON.",
                )
                await ws_manager.send_personal_json(websocket, err)
                continue

            msg_type = data.get("type", "location_update")
            ws_manager.record_heartbeat(client_conn.connection_id, incident_id)

            # Route messages
            if msg_type in ("ping", "heartbeat"):
                client_ts = data.get("timestamp")
                pong = HeartbeatAck(
                    server_time=datetime.utcnow().timestamp(),
                    client_timestamp=float(client_ts) if client_ts is not None else None,
                )
                await ws_manager.send_personal_json(websocket, pong)

            elif msg_type == "location_update":
                try:
                    loc_msg = LocationUpdateMessage(**data)
                except Exception as e:
                    err = ErrorMessage(
                        code="INVALID_LOCATION_PAYLOAD",
                        message=f"Location update format error: {e}",
                    )
                    await ws_manager.send_personal_json(websocket, err)
                    continue

                if not user_id:
                    err = ErrorMessage(
                        code="UNAUTHORIZED_BROADCAST",
                        message="Anonymous or unauthenticated connections cannot stream responder location.",
                    )
                    await ws_manager.send_personal_json(websocket, err)
                    continue

                async with _get_db_session() as db_session:
                    update_payload, arrival_event = await tracking_service.process_location_update(
                        db=db_session,
                        incident_id=incident_id,
                        responder_id=user_id,
                        location_data=loc_msg,
                    )

                if update_payload:
                    # Broadcast telemetry update to everyone in the incident room
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=update_payload,
                    )

                if arrival_event:
                    # Broadcast arrival milestone
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=arrival_event,
                    )

            elif msg_type == "status_update":
                try:
                    status_msg = StatusUpdateMessage(**data)
                except Exception as e:
                    err = ErrorMessage(
                        code="INVALID_STATUS_PAYLOAD",
                        message=f"Status update format error: {e}",
                    )
                    await ws_manager.send_personal_json(websocket, err)
                    continue

                if not user_id:
                    err = ErrorMessage(
                        code="UNAUTHORIZED",
                        message="Authentication required to update responder status.",
                    )
                    await ws_manager.send_personal_json(websocket, err)
                    continue

                async with _get_db_session() as db_session:
                    sos_resp, timeline_evt = await tracking_service.process_status_update(
                        db=db_session,
                        incident_id=incident_id,
                        responder_id=user_id,
                        new_status=status_msg.status,
                        note=status_msg.note,
                    )

                if timeline_evt:
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=timeline_evt,
                    )

                # Re-broadcast updated snapshot to refresh UI states
                async with _get_db_session() as db_session:
                    fresh_snapshot = await tracking_service.build_tracking_snapshot(
                        db=db_session,
                        incident_id=incident_id,
                        connected_clients_count=ws_manager.get_connected_count(incident_id),
                    )
                if fresh_snapshot:
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=fresh_snapshot,
                    )

            elif msg_type == "action_log":
                try:
                    act_msg = ActionLogMessage(**data)
                    log_evt = TimelineTrackingEvent(
                        sos_event_id=incident_id,
                        actor_id=user_id,
                        actor_name=user_name,
                        event_type=f"ACTION_{act_msg.action_type.upper()}",
                        details=act_msg.details,
                    )
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=log_evt,
                    )
                except Exception as e:
                    logger.debug(f"Action log parse notice: {e}")

            elif msg_type == "chat_message":
                try:
                    chat_msg = ChatMessage(**data)
                    broadcast_chat = {
                        "type": "new_message",
                        "sender_id": str(user_id) if user_id else None,
                        "sender_name": user_name or "Participant",
                        "text": chat_msg.text,
                        "language": chat_msg.language,
                        "timestamp": datetime.utcnow().isoformat(),
                    }
                    await ws_manager.broadcast_to_incident(
                        incident_id=incident_id,
                        payload=broadcast_chat,
                    )
                except Exception as e:
                    logger.debug(f"Chat parse notice: {e}")

            elif msg_type == "get_snapshot":
                async with _get_db_session() as db_session:
                    fresh_snapshot = await tracking_service.build_tracking_snapshot(
                        db=db_session,
                        incident_id=incident_id,
                        connected_clients_count=ws_manager.get_connected_count(incident_id),
                    )
                if fresh_snapshot:
                    await ws_manager.send_personal_json(websocket, fresh_snapshot)

    except WebSocketDisconnect:
        await ws_manager.disconnect(client_conn.connection_id, incident_id)
    except Exception as e:
        logger.warning(f"Unhandled WebSocket exception on conn={client_conn.connection_id}: {e}")
        await ws_manager.disconnect(client_conn.connection_id, incident_id)


# Primary WebSocket route (Module 8 specification)
@router.websocket("/ws/tracking/{incident_id}")
async def ws_tracking_endpoint(
    websocket: WebSocket,
    incident_id: uuid.UUID,
    token: str | None = Query(None),
) -> None:
    """Primary WebSocket stream for real-time responder GPS coordinates, ETA cards, and live rescue tracking."""
    await _handle_tracking_websocket(websocket, incident_id, token)


# Architecture specification compatibility alias (/ws/sos/{incident_id})
@router.websocket("/ws/sos/{incident_id}")
async def ws_sos_compat_endpoint(
    websocket: WebSocket,
    incident_id: uuid.UUID,
    token: str | None = Query(None),
) -> None:
    """Compatibility alias for WebSocket tracking stream."""
    await _handle_tracking_websocket(websocket, incident_id, token)


# ==============================================================================
# REST API Endpoints for Tracking Snapshots & Location Post Fallbacks
# ==============================================================================

@router.get(
    "/api/v1/sos/{incident_id}/tracking",
    response_model=TrackingSnapshot,
    summary="Get active tracking snapshot for an incident",
)
async def get_incident_tracking_snapshot(
    incident_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> TrackingSnapshot:
    """Fetch complete current real-time tracking snapshot including all responding rescuers and dynamic ETAs."""
    connected_count = ws_manager.get_connected_count(incident_id)
    snapshot = await tracking_service.build_tracking_snapshot(
        db=db,
        incident_id=incident_id,
        connected_clients_count=connected_count,
    )
    if not snapshot:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"SOS Incident with ID '{incident_id}' was not found.",
        )
    return snapshot


@router.post(
    "/api/v1/sos/{incident_id}/tracking/location",
    response_model=ResponderTrackingUpdate,
    summary="HTTP location update fallback for responders",
)
async def post_responder_location_http_fallback(
    incident_id: uuid.UUID,
    location_data: LocationUpdateMessage,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> ResponderTrackingUpdate:
    """HTTP POST fallback to stream GPS coordinates if the client WebSocket temporarily drops connection."""
    update_payload, arrival_event = await tracking_service.process_location_update(
        db=db,
        incident_id=incident_id,
        responder_id=current_user.id,
        location_data=location_data,
    )
    if not update_payload:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Unable to process location update: Incident '{incident_id}' not found.",
        )

    # Broadcast to active WebSocket clients listening to this incident
    await ws_manager.broadcast_to_incident(
        incident_id=incident_id,
        payload=update_payload,
    )
    if arrival_event:
        await ws_manager.broadcast_to_incident(
            incident_id=incident_id,
            payload=arrival_event,
        )

    return update_payload
