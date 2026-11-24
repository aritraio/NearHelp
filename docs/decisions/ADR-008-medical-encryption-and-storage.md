# ADR-008: AES-256-GCM Medical Encryption & Device Storage

- **Status:** Implemented (With Key-Coupling & Fallback Caveats)
- **Date:** 2026-10-26; verified 2026-11-25 against code

## Context

User medical records (`medical_conditions`, `known_allergies`, `medical_notes`) and mobile authentication tokens are sensitive PII/PHI requiring protection at rest.

## Decision

- **Backend:** Encrypt `medical_conditions`, `known_allergies`, and `medical_notes` using **AES-256-GCM** (`cryptography.hazmat.primitives.ciphers.aead.AESGCM`) with a 256-bit key derived via `SHA-256(settings.SECRET_KEY)` and a random 12-byte nonce, stored as `aes256gcm:<urlsafe_b64>`.
- **Android:** Store tokens, cached user profile, pending SOS payload/key, active incident ID, and optional user Gemini key in **`EncryptedSharedPreferences`** (`AES256_SIV` keys, `AES256_GCM` values).

## Evidence in Code

- [`backend/app/core/crypto.py`](../../backend/app/core/crypto.py)
- [`backend/app/services/user_service.py`](../../backend/app/services/user_service.py)
- [`android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt`](../../android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt)

## Consequences & Trade-offs

- **Key Coupling:** Server encryption key is derived directly from `SECRET_KEY`. Rotating `SECRET_KEY` to invalidate JWTs will break decryption of existing medical records unless re-encrypted.
- **Partial Coverage:** `blood_group`, `has_pacemaker`, `is_organ_donor`, `emergency_contacts`, `fcm_token`, and coordinates are stored unencrypted in PostgreSQL.
- **Fallback Risk:** `MedicalDataEncryption.decrypt` returns non-prefixed strings/JSON as-is (legacy plaintext compatibility), and Android `TokenStorage` silently falls back to unencrypted `MODE_PRIVATE` `SharedPreferences` (`nearhelp_prefs_fallback`) if KeyStore fails.
