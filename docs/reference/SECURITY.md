# Security Architecture & Audit Findings

This document covers NearHelp AI's implemented security controls and **critical security gaps observed directly in the source code**. See [docs/diagrams/05-authentication-workflow.md](../architecture/diagrams/05-authentication-workflow.md) for the authentication flow.

---

## 1. Implemented Security Controls

| Control | Implementation | Source File |
| :--- | :--- | :--- |
| **Password Hashing** | `bcrypt` with per-password random salt (`bcrypt.gensalt()`), truncated to 72 UTF-8 bytes | [`backend/app/core/security.py`](../../backend/app/core/security.py) |
| **JWT Signing & Validation** | HS256 JWTs (`python-jose`), explicit `type` claim (`access` vs `refresh`), `jti` UUID, `sub` user UUID; `get_current_user` re-queries DB and rejects `is_active=False` | [`backend/app/core/security.py`](../../backend/app/core/security.py), [`backend/app/core/dependencies.py`](../../backend/app/core/dependencies.py) |
| **Medical Data Encryption at Rest** | AES-256-GCM (`AESGCM`) with 12-byte random nonce and 256-bit key (`SHA-256(SECRET_KEY)`) for `medical_conditions`, `known_allergies`, and `medical_notes` | [`backend/app/core/crypto.py`](../../backend/app/core/crypto.py) |
| **Android Local Token Storage** | `EncryptedSharedPreferences` (`MasterKey.AES256_GCM`, `AES256_SIV` keys, `AES256_GCM` values) | [`TokenStorage.kt`](../../android/app/src/main/java/com/example/nearhelp/data/local/TokenStorage.kt) |
| **HTTP Body Log Suppression** | `HttpLoggingInterceptor.Level.NONE` in `RetrofitClient` | [`RetrofitClient.kt`](../../android/app/src/main/java/com/example/nearhelp/data/api/RetrofitClient.kt) |
| **Auth Rate Limiting** | Sliding window: 30 `POST` requests / 60s per IP on `/auth/` routes | [`backend/app/core/middleware.py`](../../backend/app/core/middleware.py) |
| **Admin RBAC & Self-Lockout Guard** | `/api/v1/admin/*` requires `is_superuser=True`; admins cannot delete, suspend, or demote themselves (`409 Conflict`) | [`backend/app/api/admin_users.py`](../../backend/app/api/admin_users.py) |
| **Admin Web UI Hardening** | Strict CSP (`default-src 'self'; script-src 'self'; frame-ancestors 'none'`), `Cache-Control: no-store`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, tokens stored in JS memory only (not `localStorage`) | [`backend/app/main.py`](../../backend/app/main.py), [`backend/app/admin_web/app.js`](../../backend/app/admin_web/app.js) |
| **Incident Mutation Guards** | `resolve_sos` & `cancel_sos` enforce owner/admin checks with DB row locking; WS `chat_message` & `action_log` restricted to `victim`/`responder`/`admin` | [`backend/app/services/sos_service.py`](../../backend/app/services/sos_service.py), [`backend/app/api/tracking_ws.py`](../../backend/app/api/tracking_ws.py) |

---

## 2. Verified Security Vulnerabilities & Gaps

Inspecting the repository revealed the following concrete risks that must be addressed before any production deployment:

### Critical / High Severity
1. **Committed API Key in Client Source:**
   - [`android/app/src/main/res/values/strings.xml`](../../android/app/src/main/res/values/strings.xml) (line 3) contains a committed `google_maps_key` string literal.
   - *Action required:* Restrict/rotate the Maps key and inject Android keys via `local.properties` / CI secrets instead of committing XML literals.
2. **Default Hardcoded Admin Auto-Bootstrap:**
   - [`backend/app/core/config.py`](../../backend/app/core/config.py) sets default values for `INITIAL_ADMIN_EMAIL` (`admin@nearhelp.ai`) and `INITIAL_ADMIN_PASSWORD`, and [`backend/app/db/session.py`](../../backend/app/db/session.py) `init_db()` automatically creates/promotes that superuser account on every startup if those fields are non-empty. (This also contradicts `README.md`'s earlier claim that no default admin credentials exist.)
3. **Hardcoded Fallback `SECRET_KEY` Coupled to Encryption:**
   - `Settings.SECRET_KEY` defaults to a known development string. Moreover, `crypto.py` derives the AES-256-GCM key directly via `SHA-256(SECRET_KEY)`. Rotating `SECRET_KEY` invalidates both JWTs and the ability to decrypt existing medical records.
4. **Insecure Phone OTP Stub:**
   - [`AuthService.verify_phone_auth`](../../backend/app/services/auth_service.py) accepts any `otp_code` of length `>= 4` when `id_token` is omitted, without verifying any sent OTP.
5. **Development Firebase Bypass:**
   - [`verify_firebase_id_token`](../../backend/app/services/firebase_service.py) accepts mock token prefixes (`mock_`, `test_`, `dev_`, `google_`, `demo_`), raw email strings, and **unverified JWT claims** whenever `ENVIRONMENT` is `development`, `test`, or `testing`. Deploying with `ENVIRONMENT=development` allows trivial account takeover via `/api/v1/auth/google`.

### Medium Severity
6. **Public Static Serving of Uploaded Certificates & Avatars:**
   - [`backend/app/main.py`](../../backend/app/main.py) mounts `backend/uploads` at `/uploads` via `StaticFiles` with zero authentication. Anyone who guesses or enumerates `/uploads/certificates/...` URLs can download medical credential PDFs/images.
7. **Unauthenticated Incident, Tracking, Facility Seed & Hazard Endpoints:**
   - `GET /api/v1/sos/{id}`, `GET /api/v1/sos/{id}/timeline`, `GET /api/v1/sos/{id}/tracking`, and `WS /ws/tracking/{id}` (without token) allow read access without proving incident participation, exposing victim/responder coordinates and chat messages to anyone with the UUID.
   - `POST /api/v1/sos/{id}/escalate`, `POST /api/v1/facilities/seed`, `POST /api/v1/routing/hazards`, `DELETE /api/v1/routing/hazards/{id}`, and all `/api/v1/ai/*` endpoints require no authentication.
8. **Permissive CORS & Cleartext Traffic:**
   - Both `backend` and `ai_service` set `CORS_ORIGINS` containing `"*"` alongside `allow_credentials=True`.
   - `AndroidManifest.xml` sets `android:usesCleartextTraffic="true"` and `android:allowBackup="true"`.
9. **Verbose Exception Leakage & Query Logging:**
   - `global_exception_handler` in `backend/app/main.py` returns `f"Internal server error: {exc!s}"` in HTTP `500` responses.
   - `AiAgentRepository.kt` logs partial user query strings via `android.util.Log`.
10. **Partial Encryption & Silent Device Fallback:**
    - `blood_group`, `has_pacemaker`, `is_organ_donor`, `emergency_contacts`, `fcm_token`, and coordinates are stored unencrypted in DB.
    - `TokenStorage.kt` silently falls back to unencrypted `SharedPreferences` (`nearhelp_prefs_fallback`) if Android KeyStore initialization throws an exception.
    - `MedicalDataEncryption.decrypt` returns non-`aes256gcm:` payloads as plaintext.
11. **No Server-Side Token Revocation on Logout:**
    - Logout clears client preferences only; issued JWTs remain valid until expiration (`15m` access / `7d` refresh) unless the user is deactivated or deleted.
