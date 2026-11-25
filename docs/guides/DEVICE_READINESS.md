# Two-phone setup and verification

Updated 26 October 2026 after implementing the incident workflow.

## Current status

The app now uses real backend incidents for SOS creation, responder acceptance,
GPS sharing and participant chat. Two clients have completed the workflow over
real HTTP/WebSocket connections against PostgreSQL 16 with PostGIS 3.4.
Installing an APK still requires both phones to reach the **same running backend**.
Actual Android GPS, notification delivery and visual layout still need a physical-device check.

The default Render backend timed out during this review. This change has not been
deployed to that service. Firebase Android configuration and server service-account
credentials are absent from this checkout, so physical push delivery is not yet verified.

## What changed

- Holding SOS opens a real request form. Category, notes, GPS and landmark are sent
  to `/api/v1/sos/create`; server success supplies the incident UUID used by tracking.
- An interrupted request retains its original payload and request key. Retries
  recover the same persisted incident, including after a backend restart.
- Home has a **Community emergencies & active SOS** action. Responders discover
  active incidents through a feed refreshed every 10 seconds, then accept one.
- The incident screen shows server status, accepted responders, device GPS positions,
  estimated ETA, persisted participant chat and the timeline. It refreshes server
  details every 5 seconds and receives live socket updates.
- Cancellation and resolution update the server. Only the owner or an administrator
  may end an incident; repeated resolution cannot award reputation again.
- The responder shares device location while the incident screen is open. Simulated
  waypoints are not used by the live incident screen. Stale device fixes are rejected.
- Availability is explicit and expires after 15 minutes without a fresh location
  heartbeat. Unknown responder coordinates are no longer invented.
- FCM sends actual high-priority messages. Android registers its token, requests
  notification permission, displays alerts and opens their incident on tap.
  A successful server `delivered` flag means FCM accepted the request, not that a
  person received or read the alert. Missing tokens/configuration no longer count
  as successful push delivery.
- Access tokens refresh before requests/reconnection. WebSockets reconnect after
  a broken connection and stay closed after intentional navigation away. ViewModels
  are scoped to navigation entries so different incidents cannot share stale state.
- Automatic cloud/private-IP failover was removed: credentials and SOS writes stay
  on the configured backend. HTTP body logging is disabled.
- PostGIS responder lookup now binds its UUID parameter explicitly and isolates
  query failures in a savepoint. `/health` checks the database and spatial engine
  rather than claiming connectivity without querying them.

## Run a backend for both phones

From the project root:

```sh
docker compose up -d --build
curl http://localhost:8000/health
```

Keep one backend worker/replica for this device trial. The current socket room
registry is process-local; Redis publication alone does not provide cross-worker
subscriptions. Horizontal scaling requires additional work and testing.

For phones on the same Wi-Fi, determine the computer's LAN IP, allow port 8000
through its firewall and build an APK with that address. From `android/`:

```sh
./gradlew :app:assembleDebug -PnearhelpBaseUrl=http://YOUR_COMPUTER_LAN_IP:8000/
```

The APK is `android/app/build/outputs/apk/debug/app-debug.apk`.
Install that same APK on both phones. `localhost` on a phone refers to that phone,
not the computer. Android emulators can use `http://10.0.2.2:8000/` instead.

For phones on different networks, deploy this updated backend to an HTTPS endpoint
with PostgreSQL/PostGIS and build using that endpoint:

```sh
./gradlew :app:assembleDebug -PnearhelpBaseUrl=https://YOUR_BACKEND_HOST/
```

The default build still targets the Render URL in the repository. There is no
hidden fallback to a developer's private IP address. Use separate accounts on the
phones, or an emergency guest session for the victim.

## Enable push notifications

1. Register Android package `com.example.nearhelp` in your Firebase project.
2. Put its downloaded `google-services.json` at `android/app/google-services.json`.
   The build applies the Google Services plugin when this file exists. It is ignored
   by Git. Without it, the foreground incident feed and chat still work.
3. Configure `FIREBASE_PROJECT_ID` and `FIREBASE_CREDENTIALS_JSON` for the backend,
   using a Firebase Admin service account from the same project. Docker Compose
   now forwards these variables. Keep service-account private keys on the server.
4. Enable the Firebase Cloud Messaging API, rebuild the APK, sign in on both phones
   and allow notifications. Open the incident feed on the responder and enable
   **Available to help** with a current location fix before triggering SOS nearby.

Setup references: [Firebase Android messaging](https://firebase.google.com/docs/cloud-messaging/android/get-started),
[Firebase Admin sending](https://firebase.google.com/docs/cloud-messaging/send/admin-sdk).

## Two-phone acceptance check

1. On phone B, open **Community emergencies & active SOS**, allow location and
   notifications, and enable **Available to help**.
2. On phone A, hold SOS, select the category, wait for GPS and choose **Send SOS**.
   Verify that an actual incident UUID and server status appear.
3. Open the incident from phone B's feed or push alert and tap **I'm responding**.
   Phone A should show the responder within the next refresh.
4. Keep phone B's incident screen open and move it. Phone A should receive its
   actual position and estimated ETA. Location is foreground-only in this version.
5. Send a message from each phone. Close/reopen the incident and verify both
   messages are still in the timeline.
6. Interrupt a connection and restore it. Verify reconnection and current server
   state. Resolve/cancel on phone A and verify phone B sees the ended state.
7. Once Firebase is configured, repeat alert reception with phone B backgrounded.
   A foreground feed refresh is not proof of FCM delivery.

## Automated verification

- Android: debug APK assembly, 58 unit tests and lint.
- Backend: 95 tests, including two-client GPS/chat, persistence, cancellation,
  request deduplication, repeated-resolution protection and database health failure.
- A separate network smoke test passed on a fresh PostgreSQL 16/PostGIS 3.4 container
  without SQLite spatial mocks. The AI service was unavailable during that smoke
  test; the backend used its existing severity fallback.

Repeat the network smoke test against a **development/test** backend:

```sh
cd backend
python scripts/device_smoke.py --base-url http://127.0.0.1:8000
```

It creates test accounts and an incident, exchanges GPS/chat on two sockets,
reconnects, resolves the incident and disables the test responder's availability.
It does not verify physical phone GPS, FCM delivery, or AI-service quality.

This is an implemented and integration-tested core workflow, not a claim that
all planned project modules or production deployment are complete. Voice/photo SOS,
offline SMS, continuous background GPS, multi-worker socket fan-out and other
roadmap features still need separate implementation or validation.
