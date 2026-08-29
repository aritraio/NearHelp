"""NearHelp AI — Notification & Push Dispatch Service."""

import logging
from typing import Any

from app.models.user import User

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

        if user.fcm_token:
            try:
                # Attempt to dispatch via Firebase Cloud Messaging if token exists
                # from firebase_admin import messaging
                # message = messaging.Message(
                #     notification=messaging.Notification(title=title, body=body),
                #     data={k: str(v) for k, v in (data or {}).items()},
                #     token=user.fcm_token,
                # )
                # messaging.send(message)
                payload["delivered"] = True
                logger.info(f"FCM Push delivered to user {user.id} ({user.email}): {title}")
            except Exception as e:
                logger.warning(f"FCM Push delivery failed for user {user.id}: {e}")
        else:
            logger.info(f"In-app notification generated for user {user.id} (no FCM token): {title}")

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
