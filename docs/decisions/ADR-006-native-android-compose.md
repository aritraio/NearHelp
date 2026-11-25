# ADR-006: Native Android (Kotlin + Jetpack Compose + Navigation 3)

- **Status:** Implemented
- **Date:** 2026-08-12 (original); verified 2026-11-25 against code

## Context

NearHelp needs direct access to Android location providers (`LocationManager`), Google Maps Compose, Places SDK (New), Credential Manager, FCM notifications, and responsive emergency UI components.

## Decision

Build a native Android application (`minSdk = 24`, `targetSdk = 36`, `compileSdk = 36`) using **Kotlin 2.3.20**, **Jetpack Compose** (`composeBom 2026.03.01`), **Material 3**, **AndroidX Navigation 3** (`nav3Core 1.0.1`), **Retrofit + Gson**, and **OkHttp**. Use manual dependency injection via `NearHelpApplication` rather than Hilt.

## Evidence in Code

- [`android/gradle/libs.versions.toml`](../../android/gradle/libs.versions.toml)
- [`android/app/build.gradle.kts`](../../android/app/build.gradle.kts)
- [`android/app/src/main/java/com/example/nearhelp/NearHelpApplication.kt`](../../android/app/src/main/java/com/example/nearhelp/NearHelpApplication.kt)
- [`android/app/src/main/java/com/example/nearhelp/Navigation.kt`](../../android/app/src/main/java/com/example/nearhelp/navigation/Navigation.kt)

## Consequences & Trade-offs

- **Pros:** Full native hardware/OS integration; ViewModels scoped to Navigation 3 entries via `rememberViewModelStoreNavEntryDecorator()` so separate incidents do not leak state; 4 persistent themes and EN/BN/HI localization.
- **Cons / Risks:**
  - Android-only (no iOS/web victim client).
  - Older planning docs mention Hilt DI and `domain/usecase/` layers, which are not used in the codebase; data access is mixed (repositories for Auth/User/Routing/AI, and `AndroidIncidentRepository` instantiated directly as a default parameter in `IncidentViewModel`).
  - Leftover template/demo screens (`MainScreen.kt`, `RescueNavigationViewModel.kt`, `LiveTrackingViewModel.kt`) still coexist alongside the production `IncidentScreen` / `IncidentViewModel`.
