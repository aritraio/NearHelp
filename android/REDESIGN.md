# NearHelp Android redesign

The Compose screens implement the approved NearHelp mockups using a shared visual system in `ui/design/CareComponents.kt`.

- Four saved themes: Pure White, Soft White, OLED Teal and OLED Blue. The OLED page surfaces are true black. Appearance is under Profile → Settings → Appearance. Status/navigation-bar icons also follow this preference.
- The CarePin logo combines a location pin and medical pulse. Launcher, round/monochrome icons, notifications, splash and in-app branding share the same geometry. Editable artwork is in `design/nearhelp-logo.svg`.
- Profile → Settings → Log out clears the local session and saved sign-in fields, clears Google provider state, dismisses current notifications and returns to sign-in with the app back stack cleared. Session refresh cannot restore a session after logout. Appearance is preserved.
- 20 dp page margins, 28 sp headings, 16 sp body/input text, 14 sp supporting copy, 56 dp minimum action/field heights, restrained dividers and icon-only back arrows.
- Home shows the actual device location map; until a fix exists it shows a location placeholder rather than a default Kolkata map. Medical SOS opens a five-second modal question with symptoms, optional details, explicit send and small cancel controls. Tapping a symptom sends immediately; otherwise a generic description is selected at five seconds. Location and notification permission dialogs do not consume the question timer. Dispatch waits for a permission-granted, recent GPS fix. The saved deadline survives recreation. The exact request and idempotency key are persisted before sending so retries and process restoration recover the same SOS.
- After server confirmation, the screen shows Searching for nearby responders and an inline AI conversation. The map and Call 112 control stay outside the scrolling conversation; Cancel SOS is a small text action with confirmation. Real acceptance, journey start and arrival are distinct states. Names appear before GPS is available; ETA requires both recent telemetry and recent receipt. Stationary fixes remain fresh. Socket tracking has a five-second REST fallback. Responder journey/arrival actions also work over REST. Arrival makes resolution prominent. Cancel/resolve only finish locally after server confirmation; an interrupted POST retains cancellation intent and cannot silently create a second request. Home offers Resume pending SOS for interrupted sends. The AI receives the reported symptoms as context without changing the displayed user question, and a generic SOS does not preselect a cardiac protocol.
- Nearby uses Google Places Nearby Search (New), restricted to 3 km around a permission-granted device fix less than two minutes old. Both Google and the client enforce the radius; results are sorted by straight-line distance. The map frames that local radius instead of fitting citywide directory pins. Searches refresh on 200 m movement, category/language changes, explicit refresh or after five minutes. Cached citywide listings and invented opening hours have been removed. Failure and empty results never widen the search area.
- Profile → Settings → Communication separates App language (English, Bengali, Hindi) from 31 spoken-language preferences (English plus 30 Indian languages). App language applies immediately and persists locally, including Android 13 per-app language settings. Native-script labels remain visible so users can recover a familiar language. Voice recognition uses the interface locale. Translation resources cover navigation, forms, emergency actions/countdowns, tracking states, settings and assistant controls; user messages and provider content retain their original text.
- Profile includes editable personal/medical data, contacts, languages, certificate submission, verification, recorded history and appearance. History and impact use actual incidents recorded on the device rather than demo people or statistics.
- Assistant includes topics, step-focused protocols, conversation history, handover, voice entry, and image/PDF/text attachments. PDF attachments analyze the first page. Images use the existing classification endpoint before their description is passed to chat. No attachment content is fabricated on failure.
- Old dispatch/tracking/navigation entry points share the production incident screen. Phone/OTP sign-in is removed; old saved phone navigation keys return to email/Google login.

## Google sign-in setup

The Google button now uses Credential Manager and exchanges the Google credential for the Firebase ID token required by the backend. Add the Firebase project's `google-services.json` to `android/app`, enable its Google provider and register the app's signing fingerprint. The generated `default_web_client_id` must belong to that project. No provider configuration is included in this checkout; email sign-in remains usable without it. See the [Firebase Android Google sign-in guide](https://firebase.google.com/docs/auth/android/google-signin).

## Google nearby places setup

Enable **Places API (New)** and billing in the Google Cloud project used by the existing Android Maps key. The key must permit Places API (New), with the package/signing-certificate restrictions for the installed build. The native Places SDK reuses the map key; no web-service key is placed in URLs. See [Google's setup guide](https://developers.google.com/maps/documentation/places/android-sdk/cloud-setup). Showing a working base map does not establish that Places is enabled. This checkout cannot verify the project's Cloud configuration or real on-device search results.

Opening hours are not fabricated or assumed to be 24 hours. Google phone numbers, addresses and Maps place IDs are used for details, calling and directions. Provider attributions are displayed with the Google map.

## Language resources

Edit `localization/ui-translations.tsv` and run `python3 tools/generate_ui_strings.py` to update `values/ui_strings.xml`, `values-bn/ui_strings.xml` and `values-hi/ui_strings.xml`. The existing Care component API uses a resource-backed presentation adapter so internal option IDs and API payloads stay stable. The generator validates that all translated format arguments match the English source.

The accompanying backend change permits ISO 639-3 codes used by the additional Indian spoken-language choices. Deploy that backend change for all 31 choices to save to the server. App-language selection is independent and works locally.

## Validation

Run from `android` with Java 17 or a compatible Android Studio runtime:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Unit coverage includes the SOS deadline, cancellation, single-dispatch behavior, restored countdown state, image analysis failure handling and foreground/button contrast in all four themes. Nearby tests cover Barasat versus Kolkata, radius boundaries, stale/unauthorized locations, GPS request throttling, category/language changes, provider failure and late responses. Robolectric tests cover Bengali/Hindi resource resolution, formatted countdowns, 31 language choices, saved preferences and Android 13 system per-app locales. SOS regression tests cover the exact five-second fallback, fresh-location gating, duplicate suppression, idempotent retries/restoration, cancellation during send, acknowledgement-driven completion, acceptance versus journey/arrival, sparse network responses, and REST tracking/arrival while the socket is disconnected. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

No Android emulator/system image or attached device was available in the workspace, so device screenshots and instrumented UI execution remain a manual verification step. The layout is designed to match the approved mockups; pixel-level equivalence has not been verified on a device.

## SOS server compatibility

Deploy the accompanying tracking timestamp changes for reliable stale-GPS detection: snapshots preserve the actual telemetry receipt time instead of renewing it on every poll, and newer REST GPS updates supersede an older socket cache. No responder position, ETA or phone number is supplied from demo defaults. The Android polling fallback uses the existing `/api/v1/sos/{id}/tracking` endpoint.
