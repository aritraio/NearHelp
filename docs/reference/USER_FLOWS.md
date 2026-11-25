# User Flows

This document traces the primary user journeys implemented in NearHelp AI across the Android app and Admin Web panel. See [docs/diagrams/06-user-workflows.md](../architecture/diagrams/06-user-workflows.md) and [flow.md](../architecture/DATA_FLOW.md) for sequence and state diagrams.

---

## 1. Onboarding, Authentication & Guest Bypass

Files: [`SplashScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SplashScreen.kt), [`LoginScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/LoginScreen.kt), [`SignUpScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SignUpScreen.kt), [`AuthViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/auth/AuthViewModel.kt)

1. **Splash Check:** `SplashScreen` checks `TokenStorage.isLoggedIn()` (non-blank `access_token`). If logged in, navigates directly to `HomeNavKey` (`VictimMainScreen`); otherwise navigates to `LoginNavKey`.
2. **Email Sign-In / Sign-Up:** `AuthViewModel` validates email regex and password length (`>= 6` chars), calls `AuthRepository.login` or `register`, and stores `access_token`, `refresh_token`, and user profile in `TokenStorage`.
3. **Google Sign-In:** `LoginScreen` checks `isFirebaseGoogleSignInConfigured(context)`. If configured (`google-services.json` present), `googleFirebaseToken()` invokes AndroidX Credential Manager + `FirebaseAuth` to obtain a Firebase ID token and calls `POST /api/v1/auth/google`. If not configured, it falls back to `AccountManager` / `"google_oauth_mock_id_token_demo"` (which works only when backend `ENVIRONMENT` is `development`/`test`).
4. **Zero-Barrier Emergency Bypass:** Tapping **"Get emergency help"** on `LoginScreen` calls `AuthViewModel.bypassAnonymousEmergency()`, creating a disposable server user via `POST /api/v1/auth/anonymous`, setting `emergencyEntryPending = true`, and navigating to `HomeNavKey` followed immediately by `SosCreateNavKey` (`IncidentScreen(createMode = true)`).
5. **Server Settings Dialog:** `LoginScreen` includes a **Server Settings** link that lets users test `/health` and override `RetrofitClient` base URL in `TokenStorage`.

---

## 2. Victim Flow: Triggering & Tracking a Medical SOS

Files: [`HomeScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/home/HomeScreen.kt), [`IncidentScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentScreen.kt), [`MedicalSosPromptViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/incident/MedicalSosPromptViewModel.kt), [`IncidentViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentViewModel.kt)

1. **Hold-to-Trigger SOS:** On `HomeScreen`, the victim holds `CareSosHoldButton` for **2 seconds** (preventing accidental taps).
2. **5-Second Medical Prompt:** `IncidentScreen(createMode = true)` opens `MedicalEmergencyDialog` with a 5-second countdown (`MEDICAL_PROMPT_DURATION_MILLIS = 5000L`, surviving screen rotation via `SavedStateHandle`).
   - Quick symptom chips: `"Chest pain"`, `"Breathing trouble"`, `"Injury / bleeding"`, `"Fainting"`, `"Seizure"`, `"Other / not sure"`, plus free-text notes (max 180 chars).
   - If the timer expires without cancel/tap, it submits `GENERIC_MEDICAL_SOS`.
3. **Fresh GPS Gate & Durable Pending Payload:**
   - `IncidentViewModel.create` checks `usableLocation(...)` (requires permission, a fix, and fix age `< 120s`). If GPS is not ready, state transitions to `WAITING_FOR_LOCATION` with a button to open location settings.
   - Before sending HTTP, Android generates a `UUID` idempotency key and persists `pending_sos_key` + `pending_sos_payload` synchronously to `TokenStorage`.
4. **Dispatch & Confirmation:**
   - Sends `POST /api/v1/sos/create` with `Idempotency-Key`.
   - Backend awaits AI severity prediction, commits `SOSEvent` + `TimelineEvent`s, runs PostGIS `ST_DWithin` for responders with availability `< 900s`, ranks top-N, and dispatches FCM alerts.
   - Android clears `pending_sos_*`, saves `active_incident_id`, records the incident in local history, and opens the live tracking view.
   - If HTTP fails or times out, `HomeScreen` shows **"Resume pending SOS"** so the victim retries with the exact same `Idempotency-Key`.
5. **Waiting, Live Tracking & Resolution:**
   - `IncidentScreen` displays a non-scrolling `CareMap`, `"Call 112"` button, progress bar (`SOS sent -> Accepted -> On the way -> Arrived`), inline AI crisis chat (`CareAiConversation`), responder ETA cards, and a participant message panel.
   - Only the owner (`broadcasterId == userId`) sees **"Mark incident resolved"** and **"Cancel SOS"** (with confirmation dialog). Resolving calls `PUT /api/v1/sos/{id}/resolve`.

---

## 3. Responder Flow: Discovery, Acceptance & Navigation

Files: [`HomeScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/home/HomeScreen.kt), [`IncidentScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/incident/IncidentScreen.kt), [`NearHelpMessagingService.kt`](../../android/app/src/main/java/com/example/nearhelp/notifications/NearHelpMessagingService.kt)

1. **Availability Opt-In:** Responder opens **"Nearby medical requests"** (`IncidentScreen` in browse mode) and toggles **"Available to help nearby"**. While this screen is open with a fresh GPS fix, `IncidentViewModel.updateLocation` sends `PUT /api/v1/users/me/location` (`available = true`) throttled to 15s.
2. **Alert Reception (Two Paths):**
   - **FCM Push:** `NearHelpMessagingService` receives high-priority data message containing `sos_event_id` and displays an alarm notification; tapping opens `MainActivity` -> `LiveTrackingNavKey(incidentId)`.
   - **Foreground Polling:** `HomeScreen` polls `GET /api/v1/sos/active` every 4s and shows `EmergencyAlertOverlay`; `IncidentScreen` browse mode polls every 10s.
3. **Acceptance & Live GPS Streaming:**
   - Tapping **"I'm responding"** calls `POST /api/v1/sos/{id}/respond` (`status="ACCEPTED"` + current coordinates), then connects `/ws/tracking/{id}?token=<JWT>`.
   - While `IncidentScreen` remains open, `updateLocation` streams `location_update` frames over the WebSocket.
   - Responder can tap **"I'm on the way"** (`EN_ROUTE`), **"Open directions"** (launches external Google Maps directions intent), **"I have arrived"** (`ARRIVED`), or trigger auto-arrival at `<= 35m`.
   - Victim and responder exchange persisted messages via `chat_message` WebSocket frames.

---

## 4. AI Crisis Assistant Flow

Files: [`AiCrisisAssistantScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantScreen.kt), [`AiCrisisAssistantViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantViewModel.kt), [`AiAgentRepository.kt`](../../android/app/src/main/java/com/example/nearhelp/data/repository/AiAgentRepository.kt)

1. **Protocol Browser:** Users select from 18 fallback protocol topics (`getAllProtocols()`), view step-by-step checklists, and toggle completed steps locally.
2. **Text-Only Crisis Chat:** Users ask questions or tap quick-question chips. `AiAgentRepository.chatWithAgent` attempts:
   1. Direct Gemini REST API call (when a user-configured Gemini key is stored in `TokenStorage`),
   2. Backend `POST /api/v1/ai/agent/chat` (proxying to `ai_service` LangGraph agent or backend fallback),
   3. Android local 27-condition rule engine.
3. **Contraindication & Handover:** Server/local responses include `citations`, `contraindications` banners, and a **Clinical Handover Summary** modal (`POST /api/v1/ai/agent/handover`). Note that voice/photo attachments in assistant chat were removed in commit `8c9b13f`.

---

## 5. Profile, Medical ID & Skill Verification Flow

Files: [`ProfileScreen.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/profile/ProfileScreen.kt), [`ProfileViewModel.kt`](../../android/app/src/main/java/com/example/nearhelp/ui/profile/ProfileViewModel.kt)

1. **Medical ID:** Users edit blood group, conditions, allergies, pacemaker/organ donor flags, and notes via `PUT /api/v1/users/me/medical` (encrypted at rest on server).
2. **Emergency Contacts:** Up to 5 contacts via `/api/v1/users/me/contacts`.
3. **Skill Certificate Submission:** Users pick a qualification (`CPR_CERTIFIED`, `FIRST_AID`, `EMT`, `DOCTOR`, `NURSE`, etc.), enter issuing organization & certificate ID, and attach a PDF or image (images are downscaled to max 1600px JPEG 82% in `ProfileViewModel.submitQualification`). Sent via multipart `POST /api/v1/users/me/skills/form`.
4. **Guardian Mode & Nearby Map:**
   - `GuardianRadarScreen.kt` loads emergency contacts and launches an `smsto:` intent with a Google Maps link to the user's current coordinates.
   - `CommunityGeoMapScreen.kt` + `NearbyPlacesViewModel` searches hospitals/pharmacies/etc. via Google Places SDK (New) within 3 km.

---

## 6. Administrator Web Flow (`/admin`)

Files: [`backend/app/admin_web/index.html`](../../backend/app/admin_web/index.html), [`app.js`](../../backend/app/admin_web/app.js), [`admin.py`](../../backend/app/api/admin.py), [`admin_users.py`](../../backend/app/api/admin_users.py)

1. Admin signs in at `/admin` via `POST /api/v1/auth/login` + `GET /api/v1/admin/me` (tokens held only in JS memory).
2. Dashboard polls `/api/v1/admin/stats` and the active tab every **10 seconds**.
3. **Overview & Users tab:** Filter by search query, account type (`real` / `all` / `anonymous`), status (`all` / `online` / `active` / `inactive`), and role; create/edit/suspend/delete users; or click **"Clean demo logins"** (`POST /api/v1/admin/maintenance/purge-demo-users`).
4. **Skill Verifications tab:** Filter `PENDING`/`APPROVED`/`REJECTED`/`ALL`, inspect uploaded certificate links (`/uploads/certificates/...`), and Approve (`+5.0` trust score + badge + FCM) or Reject (with required reason).
