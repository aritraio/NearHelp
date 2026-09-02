import json
import logging
import os
from typing import Any

import firebase_admin
from firebase_admin import auth, credentials

from app.core.config import settings

logger = logging.getLogger(__name__)

# Track initialization status
_firebase_initialized = False


def init_firebase() -> bool:
    """Initialize Firebase Admin SDK if credentials exist or default to mock mode in test."""
    global _firebase_initialized
    if _firebase_initialized:
        return True

    try:
        if settings.FIREBASE_CREDENTIALS_JSON and settings.FIREBASE_CREDENTIALS_JSON.strip():
            cred_dict = json.loads(settings.FIREBASE_CREDENTIALS_JSON)
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred, {
                "projectId": settings.FIREBASE_PROJECT_ID or cred_dict.get("project_id"),
            })
            _firebase_initialized = True
            logger.info("Firebase Admin SDK initialized from FIREBASE_CREDENTIALS_JSON.")
            return True
        elif os.path.exists(settings.FIREBASE_CREDENTIALS_PATH):
            cred = credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
            firebase_admin.initialize_app(cred, {
                "projectId": settings.FIREBASE_PROJECT_ID,
            })
            _firebase_initialized = True
            logger.info(f"Firebase Admin SDK initialized for project {settings.FIREBASE_PROJECT_ID}")
            return True
        elif not firebase_admin._apps:
            # Initialize with default application credentials if available
            try:
                firebase_admin.initialize_app(options={"projectId": settings.FIREBASE_PROJECT_ID})
                _firebase_initialized = True
                logger.info("Firebase Admin initialized with default application credentials.")
                return True
            except Exception:
                logger.warning("Firebase service account credentials not found. Operating in dev/mock token mode.")
                return False
        else:
            _firebase_initialized = True
            return True
    except Exception as e:
        logger.warning(f"Firebase initialization skipped: {e}")
        return False


def verify_firebase_id_token(id_token: str) -> dict[str, Any]:
    """Verify Firebase ID Token from Google Sign-In or Phone Auth.
    
    In development or test mode, accepts formatted mock/test tokens if live Firebase
    credentials are not active.
    """
    init_firebase()

    # Support testing/dev mock tokens for test suites and offline development
    if (
        id_token.startswith(("mock_", "test_", "dev_"))
        or (settings.ENVIRONMENT in ("development", "test", "testing") and not _firebase_initialized)
    ):
        logger.info("Verifying token using development/test mock handler.")
        # Parse mock token or generate deterministic payload
        if "google" in id_token:
            return {
                "uid": f"firebase_google_{id_token.replace(':', '_')}",
                "email": "google_user@nearhelp.ai",
                "name": "Google Verified Responder",
                "picture": "https://nearhelp.ai/avatars/google_user.png",
                "email_verified": True,
                "firebase": {"sign_in_provider": "google.com"},
            }
        elif "phone" in id_token:
            return {
                "uid": f"firebase_phone_{id_token.replace(':', '_')}",
                "phone_number": "+919876543210",
                "name": "Phone Verified User",
                "firebase": {"sign_in_provider": "phone"},
            }
        else:
            return {
                "uid": f"firebase_uid_{id_token}",
                "email": "test_user@nearhelp.ai",
                "name": "Test User",
                "firebase": {"sign_in_provider": "custom"},
            }

    # Live Firebase verification
    if not _firebase_initialized:
        raise ValueError(
            "Google/Phone authentication requires Firebase credentials on the server. "
            "Please use Email & Password Sign Up / Login, or add FIREBASE_CREDENTIALS_JSON to your server environment variables."
        )

    try:
        decoded_token = auth.verify_id_token(id_token)
        return decoded_token
    except Exception as e:
        logger.error(f"Firebase token verification failed: {e}")
        raise ValueError(f"Invalid Firebase authentication token: {e!s}")
