"""NearHelp AI — Cryptographic Services & AES-256-GCM Medical Data Encryption."""

import base64
import hashlib
import json
import logging
import os
from typing import Any

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

from app.core.config import settings

logger = logging.getLogger(__name__)

CIPHERTEXT_PREFIX = "aes256gcm:"


def _get_encryption_key() -> bytes:
    """Derive a 256-bit (32-byte) cryptographic key from application SECRET_KEY using SHA-256."""
    secret = settings.SECRET_KEY.encode("utf-8")
    return hashlib.sha256(secret).digest()


class MedicalDataEncryption:
    """AES-256-GCM Authenticated Encryption for PII/PHI Medical ID data at rest."""

    @classmethod
    def encrypt(cls, data: Any) -> str | None:
        """Encrypt any serializable Python object (list, dict, str) using AES-256-GCM.

        Returns a string formatted as: `aes256gcm:<base64(12-byte nonce + ciphertext + 16-byte tag)>`.
        If data is None or empty list/dict, returns formatted ciphertext or None for None input.
        """
        if data is None:
            return None

        # Convert to serialized JSON bytes
        json_bytes = json.dumps(data, ensure_ascii=False, default=str).encode("utf-8")

        key = _get_encryption_key()
        aesgcm = AESGCM(key)
        # Standard 12-byte (96-bit) nonce for AES-GCM
        nonce = os.urandom(12)

        ciphertext = aesgcm.encrypt(nonce, json_bytes, None)
        payload = nonce + ciphertext
        encoded = base64.urlsafe_b64encode(payload).decode("ascii")

        return f"{CIPHERTEXT_PREFIX}{encoded}"

    @classmethod
    def decrypt(cls, encrypted_payload: Any) -> Any:
        """Decrypt AES-256-GCM encrypted payload back to Python data structure (list, dict, str).

        If data is not a string or doesn't start with `aes256gcm:`, gracefully returns the input as-is (plaintext fallback).
        """
        if encrypted_payload is None:
            return None

        if not isinstance(encrypted_payload, str):
            return encrypted_payload

        if not encrypted_payload.startswith(CIPHERTEXT_PREFIX):
            # Check if it might be valid JSON or plain string
            try:
                return json.loads(encrypted_payload)
            except Exception:
                return encrypted_payload

        b64_str = encrypted_payload[len(CIPHERTEXT_PREFIX) :]
        try:
            raw_payload = base64.urlsafe_b64decode(b64_str.encode("ascii"))
            if len(raw_payload) < 28:  # 12-byte nonce + minimum 16-byte GCM tag
                logger.warning("Encrypted payload too short to be valid AES-GCM ciphertext.")
                return encrypted_payload

            nonce = raw_payload[:12]
            ciphertext = raw_payload[12:]

            key = _get_encryption_key()
            aesgcm = AESGCM(key)
            decrypted_bytes = aesgcm.decrypt(nonce, ciphertext, None)
            decrypted_str = decrypted_bytes.decode("utf-8")
            return json.loads(decrypted_str)
        except Exception as exc:
            logger.error("Failed to decrypt medical data payload: %s", exc)
            return encrypted_payload
