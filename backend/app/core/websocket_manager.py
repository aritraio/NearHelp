"""NearHelp AI — WebSocket Connection Lifecycle & Incident Room Manager."""

import json
import logging
import uuid
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any

from fastapi import WebSocket, WebSocketDisconnect
from pydantic import BaseModel

from app.core.middleware import get_redis_client
from app.schemas.tracking import ConnectionAck

logger = logging.getLogger(__name__)


@dataclass
class ClientConnection:
    """Represents an active client WebSocket connection in an incident tracking room."""
    websocket: WebSocket
    connection_id: str
    incident_id: uuid.UUID
    user_id: uuid.UUID | None
    user_name: str | None
    role: str
    connected_at: datetime = field(default_factory=datetime.utcnow)
    last_heartbeat: datetime = field(default_factory=datetime.utcnow)


class ConnectionManager:
    """Manages active WebSocket connections partitioned by emergency incident ID."""

    def __init__(self) -> None:
        # incident_id -> { connection_id: ClientConnection }
        self._rooms: dict[uuid.UUID, dict[str, ClientConnection]] = {}

    def get_room_connections(self, incident_id: uuid.UUID) -> list[ClientConnection]:
        """Get all active client connections in a specific incident room."""
        return list(self._rooms.get(incident_id, {}).values())

    def get_connected_count(self, incident_id: uuid.UUID) -> int:
        """Return the number of active connected clients in an incident room."""
        return len(self._rooms.get(incident_id, {}))

    async def connect(
        self,
        websocket: WebSocket,
        incident_id: uuid.UUID,
        user_id: uuid.UUID | None = None,
        user_name: str | None = None,
        role: str = "victim",
    ) -> ClientConnection:
        """Accept new WebSocket connection, assign connection ID, and register in incident room."""
        await websocket.accept()

        conn_id = str(uuid.uuid4())
        client_conn = ClientConnection(
            websocket=websocket,
            connection_id=conn_id,
            incident_id=incident_id,
            user_id=user_id,
            user_name=user_name,
            role=role,
            connected_at=datetime.utcnow(),
            last_heartbeat=datetime.utcnow(),
        )

        if incident_id not in self._rooms:
            self._rooms[incident_id] = {}

        self._rooms[incident_id][conn_id] = client_conn

        # Emit ConnectionAck frame
        ack = ConnectionAck(
            connection_id=conn_id,
            incident_id=incident_id,
            user_id=user_id,
            user_name=user_name,
            role=role,
            server_time=datetime.utcnow().timestamp(),
        )
        await self.send_personal_json(websocket, ack)

        logger.info(
            f"WebSocket connected: conn={conn_id} incident={incident_id} user={user_id} role={role} (room_size={len(self._rooms[incident_id])})"
        )
        return client_conn

    async def disconnect(self, connection_id: str, incident_id: uuid.UUID) -> None:
        """Unregister a client connection and clean up empty incident rooms."""
        if incident_id in self._rooms and connection_id in self._rooms[incident_id]:
            client = self._rooms[incident_id].pop(connection_id)
            logger.info(f"WebSocket disconnected: conn={connection_id} incident={incident_id} role={client.role}")
            if not self._rooms[incident_id]:
                self._rooms.pop(incident_id, None)

    def record_heartbeat(self, connection_id: str, incident_id: uuid.UUID) -> None:
        """Update last heartbeat timestamp for liveness monitoring."""
        if incident_id in self._rooms and connection_id in self._rooms[incident_id]:
            self._rooms[incident_id][connection_id].last_heartbeat = datetime.utcnow()

    @staticmethod
    def _serialize_payload(payload: Any) -> str:
        """Convert Pydantic model or dict to JSON string."""
        if isinstance(payload, BaseModel):
            return payload.model_dump_json()
        if isinstance(payload, dict):
            return json.dumps(payload, default=str)
        return str(payload)

    async def send_personal_json(self, websocket: WebSocket, payload: Any) -> bool:
        """Send JSON text message to a specific WebSocket client."""
        try:
            json_str = self._serialize_payload(payload)
            await websocket.send_text(json_str)
            return True
        except Exception as e:
            logger.debug(f"Failed to send personal websocket message: {e}")
            return False

    async def broadcast_to_incident(
        self,
        incident_id: uuid.UUID,
        payload: Any,
        exclude_connection_id: str | None = None,
    ) -> int:
        """Broadcast payload to all active clients registered to the incident room.
        
        Also publishes to Redis Pub/Sub channel for multi-worker scaling if Redis is connected.
        """
        json_str = self._serialize_payload(payload)
        sent_count = 0

        # 1. Local in-memory fan-out
        room_clients = self.get_room_connections(incident_id)
        disconnected_ids: list[str] = []

        for client in room_clients:
            if exclude_connection_id and client.connection_id == exclude_connection_id:
                continue
            try:
                await client.websocket.send_text(json_str)
                sent_count += 1
            except (WebSocketDisconnect, RuntimeError, Exception) as e:
                logger.debug(f"Broadcast failed on connection {client.connection_id}: {e}")
                disconnected_ids.append(client.connection_id)

        # Clean up any dead sockets detected during broadcast
        for dead_id in disconnected_ids:
            await self.disconnect(dead_id, incident_id)

        # 2. Redis Pub/Sub multi-instance fan-out
        try:
            redis_client = await get_redis_client()
            if redis_client:
                channel = f"channel:tracking:{incident_id}"
                await redis_client.publish(channel, json_str)
        except Exception as e:
            logger.debug(f"Redis pub/sub broadcast notice: {e}")

        return sent_count

    async def cleanup_stale_connections(self, max_idle_seconds: int = 120) -> int:
        """Prune connections that have exceeded the heartbeat silence window."""
        now = datetime.utcnow()
        pruned = 0

        for incident_id, room in list(self._rooms.items()):
            for conn_id, client in list(room.items()):
                idle_sec = (now - client.last_heartbeat).total_seconds()
                if idle_sec > max_idle_seconds:
                    logger.warning(
                        f"Evicting stale WebSocket conn={conn_id} idle for {idle_sec:.1f}s (incident={incident_id})"
                    )
                    try:
                        await client.websocket.close(code=1000, reason="Heartbeat timeout")
                    except Exception:
                        pass
                    await self.disconnect(conn_id, incident_id)
                    pruned += 1

        return pruned


# Global singleton connection manager
ws_manager = ConnectionManager()
