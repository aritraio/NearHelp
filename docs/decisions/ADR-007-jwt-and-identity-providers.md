# ADR-007: Backend-Issued HS256 JWTs + Multi-Provider Identity

- **Status:** Partial (Email & Guest Implemented; Firebase/OTP Partial with Dev Fallbacks)
- **Date:** 2026-08-12 (original); updated 2026-11-25 against code

## Context

The app needs a unified session mechanism across Email/Password, Google Sign-In, Phone OTP, and frictionless Anonymous Emergency sessions.

## Decision

Issue **HS256-signed JWTs** (`python-jose`) from the backend (`15m` access token, `7d` refresh token) for all identity providers. Hash email passwords with **bcrypt** (`backend/app/core/security.py`). Verify Google/Phone Firebase ID tokens in [`firebase_service.py`](../../backend/app/services/firebase_service.py), with development-mode fallbacks.

## Evidence in Code

- [`backend/app/core/security.py`](../../backend/app/core/security.py)
- [`backend/app/core/config.py`](../../backend/app/core/config.py)
- [`backend/app/services/auth_service.py`](../../backend/app/services/auth_service.py)
- [`backend/app/services/firebase_service.py`](../../backend/app/services/firebase_service.py)
- [`backend/app/core/dependencies.py`](../../backend/app/core/dependencies.py)

## Consequences & Trade-offs

- **Divergence from Docs:** `docs/architecture.md` specifies **RS256** JWTs and `ANONYMOUS_TOKEN_EXPIRE_MINUTES=60`. The actual implementation uses **HS256** (`SECRET_KEY`), and `_build_token_response()` sets `expires_delta=None` even for anonymous users, giving anonymous users the standard **15-minute** access token + **7-day** refresh token.
- **Security Risks:**
  - `/api/v1/auth/phone/send-otp` is a no-op acknowledgement, and `/api/v1/auth/phone/verify` accepts any `otp_code` with `len >= 4` without verifying an issued OTP.
  - When `ENVIRONMENT` is `development`, `test`, or `testing`, `verify_firebase_id_token` accepts mock prefixes (`mock_`, `test_`, `dev_`, `google_`, `demo_`), email strings, and **unverified JWT claims**.
  - Logout is client-side only; there is no server-side JWT blocklist/revocation (though user deactivation/deletion blocks subsequent HTTP API calls via DB lookup in `get_current_user`).
