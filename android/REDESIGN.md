# NearHelp Android redesign

The Compose screens implement the approved NearHelp mockups using a shared visual system in `ui/design/CareComponents.kt`.

- Four saved themes: Pure White, Soft White, OLED Teal and OLED Blue. The OLED page surfaces are true black. Appearance is under Profile → Settings → Appearance. Status/navigation-bar icons also follow this preference.
- 20 dp page margins, 28 sp headings, 16 sp body/input text, 14 sp supporting copy, 56 dp minimum action/field heights, restrained dividers and icon-only back arrows.
- Home shows the device location map. Medical SOS opens a map plus medical prompt. Tapping a symptom sends immediately; otherwise a generic description is selected after four seconds. Dispatch waits for a fresh GPS fix. A saved deadline and single dispatch claim prevent rotation from restarting the timer or duplicating the request. Network retries reuse the existing idempotency key.
- Active incidents show real accepted responders, position updates, server route geometry, ETA/distance, arrival, participant messages and AI conversation without stopping tracking. Unavailable/stale updates have explicit labels.
- Nearby includes hospitals, pharmacies and police stations from the existing regional directory, with search, map selection, directions and calling. Locations outside directory coverage offer an external Maps search instead of fabricated facilities. Directory bed counts are not presented as live availability.
- Profile includes editable personal/medical data, contacts, languages, certificate submission, verification, recorded history and appearance. History and impact use actual incidents recorded on the device rather than demo people or statistics.
- Assistant includes topics, step-focused protocols, conversation history, handover, voice entry, and image/PDF/text attachments. PDF attachments analyze the first page. Images use the existing classification endpoint before their description is passed to chat. No attachment content is fabricated on failure.
- Old dispatch/tracking/navigation entry points share the production incident screen. Phone/OTP sign-in is removed; old saved phone navigation keys return to email/Google login.

## Google sign-in setup

The Google button now uses Credential Manager and exchanges the Google credential for the Firebase ID token required by the backend. Add the Firebase project's `google-services.json` to `android/app`, enable its Google provider and register the app's signing fingerprint. The generated `default_web_client_id` must belong to that project. No provider configuration is included in this checkout; email sign-in remains usable without it. See the [Firebase Android Google sign-in guide](https://firebase.google.com/docs/auth/android/google-signin).

## Validation

Run from `android` with Java 17 or a compatible Android Studio runtime:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Unit coverage includes the SOS deadline, cancellation, single-dispatch behavior, restored countdown state, image analysis failure handling and foreground/button contrast in all four themes. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

No Android emulator/system image or attached device was available in the workspace, so device screenshots and instrumented UI execution remain a manual verification step. The layout is designed to match the approved mockups; pixel-level equivalence has not been verified on a device.
