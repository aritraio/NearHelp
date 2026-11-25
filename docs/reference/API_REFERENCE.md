# API Reference

All Backend endpoints are served at `http://localhost:8000` under `/api/v1/*` (canonical) and `/api/*` (compatibility aliases). AI Microservice endpoints are served at `http://localhost:8001` under `/api/v1/*` and `/*`.

---

## 1. Health & Static Assets (Backend `:8000`)

| Method | Path | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | None | Returns service status JSON |
| `GET` | `/health`, `/api/v1/health` | None | Executes `SELECT 1` (and `PostGIS_Lib_Version()` on Postgres); returns `200` or `503` |
| `GET` | `/admin`, `/admin/` | None (HTML); API calls require Admin JWT | Serves the Admin Command Center SPA (`index.html`) |
| `GET` | `/admin/assets/{file}` | None | Serves `style.css` and `app.js` |
| `GET` | `/uploads/{subpath}` | **None (Public)** | Serves uploaded avatars and skill certificates |

---

## 2. Authentication & Identity (`/api/v1/auth`)

Source: [`backend/app/api/auth.py`](../../backend/app/api/auth.py) | Rate-limited: `30 req/min` per IP on `POST` routes.

| Method | Path | Auth | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/register` | None | `RegisterRequest` (`email`, `password` min 6, `name`, `phone?`, `blood_group?`) | `201` `TokenResponse` |
| `POST` | `/login` | None | `LoginRequest` (`email`, `password`) | `200` `TokenResponse` |
| `POST` | `/google` | None | `GoogleAuthRequest` (`id_token`) | `200` `TokenResponse` |
| `POST` | `/phone/send-otp` | None | `PhoneSendOtpRequest` (`phone_number`) | `200` `MessageResponse` (stub acknowledgement) |
| `POST` | `/phone/verify` | None | `PhoneVerifyRequest` (`phone_number`, `otp_code?`, `id_token?`, `name?`) | `200` `TokenResponse` |
| `POST` | `/anonymous` | None | `AnonymousAuthRequest` (`device_id?`, `temp_name?`) | `201` `TokenResponse` |
| `POST` | `/refresh` | None | `TokenRefreshRequest` (`refresh_token`) | `200` `TokenResponse` |
| `POST` | `/device` | Bearer | `DeviceRegisterRequest` (`fcm_token`, `device_id?`, `platform?`, `device_info?`) | `200` `MessageResponse` |
| `GET` | `/me` | Bearer | — | `200` `UserResponse` |

---

## 3. User Profile, Medical ID, Contacts & Skills (`/api/v1/users`)

Source: [`backend/app/api/users.py`](../../backend/app/api/users.py) | All routes require active Bearer JWT (`get_current_active_user`).

| Method | Path | Body / Params | Response | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/me` | — | `UserResponse` | Decrypts medical fields |
| `PUT`, `PATCH` | `/me` | `UserUpdateRequest` | `UserResponse` | Encrypts conditions, allergies, notes |
| `GET` | `/me/medical` | — | `MedicalIDResponse` | Decrypted Medical ID card |
| `PUT`, `PATCH` | `/me/medical` | `MedicalIDUpdateRequest` | `MedicalIDResponse` | Encrypts sensitive fields |
| `GET` | `/me/contacts` | — | `list[EmergencyContactSchema]` | Max 5 contacts |
| `POST` | `/me/contacts` | `EmergencyContactCreateSchema` | `201` `EmergencyContactSchema` | 400 if >= 5 contacts |
| `PUT` | `/me/contacts/{contact_id}` | `EmergencyContactUpdateSchema` | `EmergencyContactSchema` | Updates by UUID string |
| `DELETE` | `/me/contacts/{contact_id}` | — | `MessageResponse` | Promotes next contact if primary deleted |
| `POST` | `/me/photo`, `/me/avatar` | Multipart `file` (JPEG/PNG/WebP, <= 5 MB) | `PhotoUploadResponse` | Saves to `uploads/avatars/` |
| `PUT` | `/me/languages` | `LanguagePreferencesRequest` (`languages[]`) | `UserResponse` | Validates against `ALLOWED_LANGUAGES` |
| `POST` | `/me/fcm-token` | `DeviceRegisterRequest` | `MessageResponse` | Updates `users.fcm_token` |
| `POST` | `/me/skills` | `SkillClaimRequest` (`skill_type`, `certificate_url`, `notes?`) | `201` `SkillVerificationResponse` | Creates `PENDING` claim |
| `POST` | `/me/skills/upload` | Multipart `file` (PDF/JPEG/PNG/WebP, <= 10 MB) | `SkillCertificateUploadResponse` | Saves to `uploads/certificates/` |
| `POST` | `/me/skills/form` | Form `skill_type`, `file`, `notes?` | `201` `SkillVerificationResponse` | Upload + claim in one call |
| `GET` | `/me/skills` | — | `list[SkillVerificationResponse]` | User's claim history |
| `PUT` | `/me/location` | `DeviceLocationRequest` (`latitude`, `longitude`, `available`) | `MessageResponse` | Updates availability heartbeat & PostGIS point |

---

## 4. Smart SOS Engine (`/api/v1/sos`)

Source: [`backend/app/api/sos.py`](../../backend/app/api/sos.py)

| Method | Path | Auth | Body / Query | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/create`, `/` | Optional Bearer | Header `Idempotency-Key?`; Body `SOSCreateRequest` | `201` `SOSCreateResponse` (`409` on key conflict) |
| `GET` | `/active` | Optional Bearer | Query `lat?`, `lon?`, `limit=50` | `200` `SOSActiveListResponse` |
| `GET` | `/{id}` | **None** | Path `id: UUID` | `200` `SOSDetailResponse` |
| `POST` | `/{id}/respond` | Bearer | `SOSResponseRequest` (`status`: `ACCEPTED\|DECLINED\|EN_ROUTE\|ARRIVED`, `eta_minutes?`, `current_latitude?`, `current_longitude?`) | `200` `SOSResponseItem` (`400` if self-response or ended) |
| `POST` | `/{id}/escalate` | **None** | `SOSEscalateRequest` (`elapsed_seconds?`, `force_layer?`) | `200` `SOSEscalationStatus` |
| `PUT` | `/{id}/resolve` | Bearer (Owner or Admin) | `SOSResolveRequest` (`resolution_notes?`, `feedback_score?`, `resolved_by`) | `200` `SOSResolveResponse` (`403` / `409`) |
| `GET` | `/{id}/timeline` | **None** | Path `id: UUID` | `200` `list[TimelineEventItem]` |
| `PUT` | `/{id}/cancel` | Bearer (Owner or Admin) | Path `id: UUID` | `200` `{"id": "...", "status": "CANCELLED"}` |

---

## 5. Live Tracking & WebSockets

Source: [`backend/app/api/tracking_ws.py`](../../backend/app/api/tracking_ws.py)

### REST Tracking Endpoints
| Method | Path | Auth | Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/sos/{incident_id}/tracking` | **None** | — | `200` `TrackingSnapshot` |
| `POST` | `/api/v1/sos/{incident_id}/tracking/location` | Bearer | `LocationUpdateMessage` | `200` `ResponderTrackingUpdate` (also broadcasts to WS room) |

### Tracking WebSocket: `ws://.../ws/tracking/{incident_id}?token=<JWT>` (Alias: `/ws/sos/{incident_id}`)
- **On Connect:** Sends `connection_ack` (`role`: `victim | responder | admin | dispatcher | guest`) and `tracking_snapshot`.
- **Client -> Server Frames:**
  - `{"type": "ping" | "heartbeat", "timestamp": ...}` -> replies `pong`
  - `{"type": "location_update", "latitude": 22.58, "longitude": 88.43, "heading": 90.0, "speed_mps": 1.3, "accuracy": 5.0}` (requires authenticated `user_id` & active `SOSResponse`) -> broadcasts `responder_update` (and `timeline_event` if `distance <= 35m`)
  - `{"type": "status_update", "status": "EN_ROUTE"|"ARRIVED"|"ON_SCENE"|"HANDOVER_108"|"DECLINED", "note": "..."}` -> broadcasts `timeline_event` + refreshed `tracking_snapshot`
  - `{"type": "action_log", "action_type": "...", "details": {...}}` (roles `victim`, `responder`, `admin`) -> broadcasts `timeline_event`
  - `{"type": "chat_message", "text": "...", "language": "en"}` (roles `victim`, `responder`, `admin`; fails with `INCIDENT_ENDED` if resolved/cancelled) -> persists `TimelineEvent` and broadcasts `new_message`
  - `{"type": "get_snapshot"}` -> sends `tracking_snapshot`

---

## 6. Facilities & Rescue Routing

Source: [`backend/app/api/facilities.py`](../../backend/app/api/facilities.py), [`backend/app/api/routing.py`](../../backend/app/api/routing.py)

| Method | Path | Auth | Query / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/facilities/nearby` | None | `latitude`, `longitude`, `radius_km=5.0`, `type?`, `min_beds?`, `min_icu?`, `verified_only=true`, `limit=50` | `NearbyFacilitiesResponse` |
| `POST` | `/api/v1/facilities/seed` | **None** | — | `FacilitySeedResponse` |
| `GET` | `/api/v1/facilities/{facility_id}` | None | — | `FacilityResponse` |
| `GET` | `/api/v1/facilities/` | None | `type?`, `skip=0`, `limit=50` | `list[FacilityResponse]` |
| `POST` | `/api/v1/routing/directions` | None | `DirectionsRequest` | `{"status": "success", "data": {...}}` |
| `POST` | `/api/v1/routing/detour` | None | `DirectionsRequest` | `detour_route`, `aed_pickup_route`, `recommendation` |
| `GET` | `/api/v1/routing/hazards` | None | — | Active road hazards (in-memory) |
| `POST` | `/api/v1/routing/hazards` | **None** | `RoadHazardReportRequest` | `201` Created hazard |
| `DELETE` | `/api/v1/routing/hazards/{hazard_id}` | **None** | — | Removes hazard from memory |

---

## 7. AI Endpoints (Backend Proxy `/api/v1/ai` & AI Service `:8001/api/v1`)

Source: [`backend/app/api/ai.py`](../../backend/app/api/ai.py), [`backend/app/api/ai_ws.py`](../../backend/app/api/ai_ws.py), [`ai_service/app/api/`](../../ai_service/app/api/)

| Method | Backend Path | AI Service Path | Auth | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/ai/classify` | `/api/v1/classify` | None | Multimodal classification (`ClassificationRequest` -> `ClassificationResponse`) |
| `POST` | `/api/v1/ai/severity` | `/api/v1/severity` | None | Severity scoring (`SeverityRequest` -> `SeverityResponse`) |
| `GET` | `/api/v1/ai/taxonomy` | `/api/v1/taxonomy` | None | 6 crisis types & 8 clinical conditions |
| `POST` | `/api/v1/ai/agent/chat` | `/api/v1/agent/chat` | None | LangGraph / fallback dialogue turn (`AgentChatRequest` -> `AgentChatResponse`) |
| `GET` | `/api/v1/ai/agent/protocols` | `/api/v1/agent/protocols` | None | List grounded first-aid protocols |
| `GET` | `/api/v1/ai/agent/protocols/{condition_id}` | `/api/v1/agent/protocols/{condition_id}` | None | Get protocol by condition |
| `POST` | `/api/v1/ai/agent/handover` | `/api/v1/agent/handover` | None | Generate clinical handover summary |
| `POST` | *(Not proxied)* | `/api/v1/agent/init`, `/agent/step` | None | Init session / toggle step progress |
| `POST` | *(Not proxied)* | `/api/v1/transcribe`, `/vision` | None | Direct audio STT / image scene analysis |
| `WS` | `/ws/ai/chat` | `/ws/ai/chat` | None | Actions: `ping`, `init`, `user_message`, `step_toggle`, `set_metronome`, `request_handover` |

---

## 8. Admin APIs (`/api/v1/admin`)

Source: [`backend/app/api/admin.py`](../../backend/app/api/admin.py), [`backend/app/api/admin_users.py`](../../backend/app/api/admin_users.py) | All routes require superuser (`get_current_admin_user`).

| Method | Path | Description |
| :--- | :--- | :--- |
| `GET` | `/me` | Current superuser profile (`AdminUserResponse`) |
| `GET` | `/stats` | Dashboard metrics (`total_users`, `active_users`, `online_users`, `admin_users`, `new_users_today`, `pending_verifications`, `active_emergencies`) |
| `GET` | `/users` | Search/filter (`q`, `state=all\|active\|inactive\|online`, `role=all\|admin\|user`, `user_type=real\|all\|anonymous`, `limit`, `offset`) |
| `POST` | `/users` | Create email/password user (`AdminUserCreate`) |
| `GET` | `/users/{user_id}` | Get user + `last_seen_at` + `is_online` |
| `PATCH` | `/users/{user_id}` | Update name, email, phone, password (email accounts only), `is_active`, `is_superuser`, `trust_score` (blocks self-suspend/demote) |
| `DELETE` | `/users/{user_id}` | Permanently delete user + presence/responses/skills, nullify SOS/timeline FKs (blocks self-delete) |
| `POST` | `/maintenance/purge-demo-users` | Delete all `is_anonymous=True` accounts |
| `GET` | `/verification-queue`, `/verifications` | List skill claims (`status=PENDING\|APPROVED\|REJECTED\|ALL`) |
| `GET` | `/verification-queue/{id}`, `/verifications/{id}` | Single verification detail |
| `POST` | `/verification-queue/{id}/approve` | Approve claim, `+5.0` trust score, add skill & badge, send FCM |
| `POST` | `/verification-queue/{id}/reject` | Reject claim with `rejection_reason`, send FCM |
| `PUT` | `/verifications/{id}`, `/verification-queue/{id}` | Universal approve/reject review handler |
