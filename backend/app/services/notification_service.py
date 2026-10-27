"""NearHelp AI — Notification & Push Dispatch Service."""

import asyncio
import json
import logging
from typing import Any

from firebase_admin import messaging

from app.models.user import User
from app.services.firebase_service import init_firebase

logger = logging.getLogger(__name__)

# In-memory dispatch audit log (useful for testing, live telemetry & verification)
DISPATCHED_NOTIFICATIONS: list[dict[str, Any]] = []


class NotificationService:
    """Service managing push notifications and in-app alert dispatch."""

    @classmethod
    async def send_notification(
        cls,
        user: User,
        title: str,
        body: str,
        data: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """Dispatch push notification to user device or record in-app event."""
        payload = {
            "user_id": str(user.id),
            "user_email": user.email,
            "title": title,
            "body": body,
            "data": data or {},
            "fcm_token": user.fcm_token,
            "delivered": False,
        }

        payload["delivered"] = await cls._send(user.fcm_token, title, body, data or {})

        DISPATCHED_NOTIFICATIONS.append(payload)
        return payload

    @classmethod
    async def notify_skill_approved(
        cls,
        user: User,
        skill_type: str,
        new_trust_score: float,
        badge: str | None = None,
    ) -> dict[str, Any]:
        """Notify user that their skill certificate has been verified and trust score incremented."""
        title = "Skill Verified! 🎖️"
        body = (
            f"Congratulations! Your '{skill_type}' certification has been verified by the medical board. "
            f"Your Trust Score is now {new_trust_score:.1f} (+5.0 pts)."
        )
        data = {
            "event": "SKILL_VERIFIED",
            "skill_type": skill_type,
            "trust_score": new_trust_score,
            "badge": badge or "",
        }
        return await cls.send_notification(user, title, body, data)

    @classmethod
    async def notify_skill_rejected(
        cls,
        user: User,
        skill_type: str,
        reason: str | None = None,
    ) -> dict[str, Any]:
        """Notify user that their skill certificate was rejected with admin feedback."""
        title = "Skill Verification Update"
        reason_text = f" Reason: {reason}" if reason else " Please re-upload a clear, valid certification document."
        body = f"Your verification request for '{skill_type}' could not be approved.{reason_text}"
        data = {
            "event": "SKILL_REJECTED",
            "skill_type": skill_type,
            "reason": reason or "",
        }
        return await cls.send_notification(user, title, body, data)

    @staticmethod
    async def _send(token: str | None, title: str, body: str, data: dict) -> bool:
        if not token:
            return False
        try:
            if not init_firebase():
                return False
            message = messaging.Message(
                token=token,
                data={"title": title, "body": body, **{
                    k: v if isinstance(v, str) else json.dumps(v) for k, v in data.items()
                }},
                android=messaging.AndroidConfig(priority="high", ttl=300),
            )
            await asyncio.to_thread(messaging.send, message)
            return True  # Accepted by FCM; this is not an acknowledgement from the phone.
        except Exception as exc:
            logger.warning("FCM send failed (%s)", type(exc).__name__)
            return False

    @classmethod
    async def send_emergency_dispatch_alert(
        cls, user_id: Any, sos_event_id: Any, crisis_type: str, severity: str,
        distance_str: str, eta_str: str, required_skills: list[str] | None = None,
        fcm_token: str | None = None,
    ) -> dict[str, Any]:
        title = f"Emergency: {crisis_type} ({severity})"
        body = f"Nearby incident {distance_str} away. Open NearHelp to respond."
        data = {"sos_event_id": str(sos_event_id), "crisis_type": crisis_type,
                "severity": severity, "required_skills": required_skills or []}
        payload = {"user_id": str(user_id), "title": title, "body": body, "data": data,
                   "delivered": await cls._send(fcm_token, title, body, data)}
        DISPATCHED_NOTIFICATIONS.append(payload)
        return payload

    @classmethod
    async def send_responder_arrival_notification(
        cls, victim_id: Any, sos_event_id: Any, responder_name: str, eta_str: str,
        fcm_token: str | None = None,
    ) -> dict[str, Any]:
        title = "A responder has accepted your SOS"
        body = f"{responder_name} is responding (estimated ETA {eta_str})."
        data = {"sos_event_id": str(sos_event_id), "responder_name": responder_name}
        payload = {"user_id": str(victim_id), "title": title, "body": body, "data": data,
                   "delivered": await cls._send(fcm_token, title, body, data)}
        DISPATCHED_NOTIFICATIONS.append(payload)
        return payload


notification_service = NotificationService()
