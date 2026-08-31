"""NearHelp AI — Live Location Tracking & WebSocket Stream Schemas."""

import uuid
from datetime import datetime
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field


class LocationUpdateMessage(BaseModel):
    """Payload sent by responder app periodically streaming GPS coordinates."""
    type: Literal["location_update"] = "location_update"
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Current GPS Latitude")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Current GPS Longitude")
    heading: float | None = Field(None, ge=0.0, le=360.0, description="Heading / bearing in degrees (0-360)")
    speed_mps: float | None = Field(None, ge=0.0, description="Speed in meters per second")
    accuracy: float | None = Field(None, ge=0.0, description="GPS horizontal accuracy radius in meters")
    altitude: float | None = Field(None, description="Altitude in meters above sea level")
    battery_level: float | None = Field(None, ge=0.0, le=100.0, description="Device battery percentage")
    timestamp: float | None = Field(None, description="Client Unix timestamp in seconds or milliseconds")


class StatusUpdateMessage(BaseModel):
    """Payload sent when a responder changes rescue engagement status."""
    type: Literal["status_update"] = "status_update"
    status: str = Field(
        ...,
        description="Updated status: 'ACCEPTED', 'EN_ROUTE', 'ARRIVED', 'ON_SCENE', 'HANDOVER_108', 'RESOLVED', 'DECLINED'",
    )
    note: str | None = Field(None, max_length=512, description="Optional status annotation or milestone note")


class HeartbeatMessage(BaseModel):
    """Client-initiated heartbeat ping message to maintain connection liveness."""
    type: Literal["ping", "heartbeat"] = "ping"
    timestamp: float | None = Field(None, description="Client timestamp")


class HeartbeatAck(BaseModel):
    """Server-side heartbeat pong response."""
    type: Literal["pong"] = "pong"
    server_time: float = Field(..., description="Server Unix epoch timestamp")
    client_timestamp: float | None = Field(None, description="Echo of client timestamp for latency calculation")


class ActionLogMessage(BaseModel):
    """Log an immediate clinical or rescue action on scene."""
    type: Literal["action_log"] = "action_log"
    action_type: str = Field(..., description="Action tag: e.g. 'cpr_started', 'aed_attached', 'call_108'")
    details: dict[str, Any] = Field(default_factory=dict, description="Action contextual parameters")


class ChatMessage(BaseModel):
    """Quick tactical message transmitted across incident tracking room."""
    type: Literal["chat_message"] = "chat_message"
    text: str = Field(..., max_length=1000, description="Message body")
    language: str | None = Field("en", description="Source language code (e.g. 'en', 'bn', 'hi')")


class GetSnapshotMessage(BaseModel):
    """Request immediate full tracking snapshot refresh from server."""
    type: Literal["get_snapshot"] = "get_snapshot"


# ==============================================================================
# Server -> Client Broadcast Payloads
# ==============================================================================

class ResponderTrackingUpdate(BaseModel):
    """Real-time responder position, telemetry, and dynamic ETA broadcast payload."""
    model_config = ConfigDict(from_attributes=True)

    type: Literal["responder_update"] = "responder_update"
    responder_id: uuid.UUID
    responder_name: str
    latitude: float
    longitude: float
    heading: float | None = None
    bearing_compass: str | None = None
    speed_kmh: float | None = None
    distance_meters: float = Field(..., description="Distance to victim in meters")
    distance_formatted: str = Field(..., description="Human-readable distance (e.g. '340m', '1.2km')")
    eta_minutes: float = Field(..., description="Estimated travel time remaining in minutes")
    eta_formatted: str = Field(..., description="Human-readable ETA (e.g. '2.5 mins', '45 secs', 'Arrived')")
    status: str = Field("EN_ROUTE", description="Responder status: 'ACCEPTED', 'EN_ROUTE', 'ARRIVED', 'ON_SCENE'")
    is_doctor: bool = False
    is_cpr_certified: bool = False
    verified_skills: list[str] = Field(default_factory=list)
    phone: str | None = None
    last_updated: datetime = Field(default_factory=datetime.utcnow)


class TrackingFacilityItem(BaseModel):
    """Facility marker payload (closest AED, nearest Trauma Hospital) attached to tracking stream."""
    model_config = ConfigDict(from_attributes=True)

    id: str | uuid.UUID
    name: str
    facility_type: str = Field(..., description="'hospital' or 'aed'")
    latitude: float
    longitude: float
    distance_meters: float
    distance_formatted: str
    details: dict[str, Any] = Field(default_factory=dict)


class TrackingSnapshot(BaseModel):
    """Full aggregate state snapshot of an active emergency incident tracking session."""
    model_config = ConfigDict(from_attributes=True)

    type: Literal["tracking_snapshot"] = "tracking_snapshot"
    incident_id: uuid.UUID
    status: str
    crisis_type: str
    sub_type: str | None = None
    severity_score: int = 50
    priority: str = "high"
    incident_latitude: float
    incident_longitude: float
    incident_address: str | None = None
    incident_sub_address: str | None = None
    is_anonymous: bool = False
    current_radius_meters: float = 1500.0
    responders: list[ResponderTrackingUpdate] = Field(default_factory=list)
    closest_aed: TrackingFacilityItem | None = None
    closest_hospital: TrackingFacilityItem | None = None
    connected_clients_count: int = 0
    server_timestamp: datetime = Field(default_factory=datetime.utcnow)


class TimelineTrackingEvent(BaseModel):
    """Milestone notification broadcast when rescue lifecycle state transitions."""
    model_config = ConfigDict(from_attributes=True)

    type: Literal["timeline_event"] = "timeline_event"
    id: uuid.UUID = Field(default_factory=uuid.uuid4)
    sos_event_id: uuid.UUID
    actor_id: uuid.UUID | None = None
    actor_name: str | None = None
    event_type: str
    details: dict[str, Any] = Field(default_factory=dict)
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class ConnectionAck(BaseModel):
    """Acknowledgement message sent immediately when client connects to tracking stream."""
    type: Literal["connection_ack"] = "connection_ack"
    connection_id: str
    incident_id: uuid.UUID
    user_id: uuid.UUID | None = None
    user_name: str | None = None
    role: str = Field("victim", description="Connection role: 'victim', 'responder', 'dispatcher', 'admin', 'guest'")
    message: str = "Connected to NearHelp AI Live Tracking WebSocket"
    server_time: float = Field(..., description="Server Unix epoch timestamp")


class ErrorMessage(BaseModel):
    """Error frame emitted when invalid payload or unauthorized action occurs."""
    type: Literal["error"] = "error"
    code: str
    message: str
    details: dict[str, Any] | None = None
