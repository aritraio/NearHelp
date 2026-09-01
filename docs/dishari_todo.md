# NearHelp AI — Dishari's Master TODO List & UI/UX Task Specification

> **Owner**: Dishari (Android App UI/UX Lead)  
> **Platform**: Android (Kotlin + Jetpack Compose)  
> **Design Philosophy**: High-Urgency, Stress-Resilient, One-Tap Accessibility, OLED Dark Mode  
> **Status**: 🟢 **Phase 1 UI Screens & Design Tokens Complete**

---

## 🎨 UI/UX Design System Specifications

### Color Tokens
| Token | Hex Code | Purpose & Context |
| :--- | :--- | :--- |
| **Emergency Red (Primary)** | `#E53935` | Main SOS Button, Level 5 Emergency, Critical Alerts |
| **Action Amber (Secondary)** | `#FF9800` | Warning, Pending Dispatch, Medium Urgency |
| **Safe Green** | `#4CAF50` | Responder Arrived, Safe Status, Verified Badges |
| **AI Info Blue** | `#2196F3` | AI Guidance Card, Chat Bubbles, System Notes |
| **Background (OLED Dark)**| `#121212` | Main background (high contrast & battery savings) |
| **Card Surface** | `#1E1E1E` | Card containers, bottom sheets, dialogs |
| **Text High Contrast** | `#FFFFFF` | Primary headings, critical instruction text |
| **Text Medium Contrast** | `#B0BEC5` | Metadata, timestamps, secondary labels |

### Ergonomics & Typography Rules
* **Font Family**: Inter or Roboto (Clean sans-serif for high legibility under stress).
* **Minimum Touch Target**: `48dp` for standard buttons, **`72dp+` for SOS triggers**.
* **Visual Hierarchy**: Heavy bold headers for action steps (e.g. **"APPLY PRESSURE TO WOUND"**), `16sp+` for AI guidance text.

---

## 📱 Detailed Task Allocations by Module

### 🔴 Phase 1 — Review 1 & MVP Core UI (Months 1–2)

#### Module 1 — Auth & Onboarding UI
- [x] 🔴 **Anonymous Emergency Bypass**: Skip login button allowing 1-tap immediate SOS trigger.
- [x] 🟡 **Splash Screen**: Build `SplashScreen.kt` with NearHelp logo branding and pulsing launch animation.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SplashScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SplashScreen.kt)
- [x] 🟡 **Login Screen**: Build `LoginScreen.kt` with email/password fields, Google sign-in button, phone OTP link, and emergency bypass.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/auth/screens/LoginScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/LoginScreen.kt)
- [x] 🟡 **Sign-Up Screen**: Build `SignUpScreen.kt` with validation, blood group chips, and terms agreement check.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SignUpScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/SignUpScreen.kt)
- [x] 🟡 **Phone OTP Input Screen**: Build `PhoneOtpScreen.kt` with 6-digit code entry and countdown resend timer.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/auth/screens/PhoneOtpScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/auth/screens/PhoneOtpScreen.kt)
- [x] 🟢 **Token Storage**: Store Auth JWT tokens using Android Encrypted SharedPreferences (`TokenStorage.kt`).

#### Module 2 — User Profile & Encrypted Medical ID UI
- [x] 🟡 **Medical ID Reveal Component**: Build encrypted Medical ID sheet in `RescueNavigationScreen.kt`.
- [x] 🟡 **Emergency Contacts Quick Actions**: Tap-to-call / SMS quick action buttons for emergency contacts.
- [x] 🟡 **Native Profile Screen**: Jetpack Compose user profile view showing personal details and verified badges.
- [x] 🟡 **Medical ID Card View & Edit Sheet**: Interface for blood group, known allergies, pacemaker, and emergency notes.

#### Module 6 — Main SOS Trigger Screen
- [x] 🔴 **Pulsing SOS Button**: Large red circular SOS trigger (`#E53935`) with radial breathing animation.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/sos/SosTriggerScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/sos/SosTriggerScreen.kt)
- [x] 🔴 **Countdown Abort Ring**: 3-second hold / 5-second cancel ring preventing accidental triggers.
- [x] 🟡 **Emergency Category Selector**: Quick selection chips (Medical, Fire, Crime, Accident).
- [x] 🟡 **Hold-to-Voice SOS Button**: Voice SOS recording trigger with audio waveform visualizer animation.
- [x] 🟡 **Photo Attach Preview**: Camera/gallery image preview with AI scene object detection overlay.
- [x] 🟡 **Anonymous Mode Toggle**: Switch between logged-in identity and anonymous emergency mode.

#### Module 7 — Dynamic Community Geo-Map UI
- [x] 🔴 **Interactive Geo-Map Component**: Build `CommunityGeoMapScreen.kt` using Google Maps SDK in Jetpack Compose.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/map/CommunityGeoMapScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/map/CommunityGeoMapScreen.kt)
- [x] 🔴 **Victim Beacon Marker**: Red pulsing beacon marker indicating emergency origin location.
- [x] 🟡 **Responder Badged Pins**: Green beacon pins displaying verified skill badges (CPR, Doctor, EMT).
- [x] 🟡 **Facility Markers**: Map pins for nearby hospitals (with live bed counts) and AED locators.
- [x] 🟡 **Radial Expansion Circle**: Animated PostGIS spatial dispatch wave ring (500m → 1.5km → 3km).

#### Module 8 & 9 — Live Tracking & Rescue Navigation UI
- [x] 🔴 **Rescue Navigation Screen**: Turn-by-turn route map and live ETA card in `RescueNavigationScreen.kt`.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/navigation/RescueNavigationScreen.kt)
- [x] 🟡 **WebSocket Live GPS Stream**: Consume `/ws/tracking/{incident_id}` in `LiveTrackingScreen.kt` for live responder movement.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/tracking/LiveTrackingScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/tracking/LiveTrackingScreen.kt)

#### Module 10 — AI Crisis Assistant & CPR Metronome UI
- [x] 🔴 **First-Aid Protocol Screen**: Step-by-step guidance cards in `AiCrisisAssistantScreen.kt`.
  - Path: [`android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantScreen.kt`](../android/app/src/main/java/com/example/nearhelp/ui/assistant/AiCrisisAssistantScreen.kt)
- [x] 🔴 **AHA 110 BPM CPR Metronome**: Visual pulse + audio metronome ticking at 545.45ms period for chest compression pacing.
- [x] 🔴 **Bystander AI Chat Drawer**: Interactive slide-over chat drawer connected to AI Assistant API.
- [x] 🔴 **Section 134A Good Samaritan Legal Immunity Seal**: Visual legal guarantee banner protecting community responders.

---

### 🟡 Phase 2 — Feature Enhancements & Advanced UI (Month 3)

#### Module 13 — Voice SOS Audio UI
- [ ] 🟡 **Voice SOS Wave Visualizer**: Implement real-time mic volume audio visualizer component.
- [ ] 🟡 **Voice Confirmation Sheet**: Display live speech-to-text transcript preview before sending SOS.

#### Module 14 — Emergency Timeline & Audit Trail UI
- [ ] 🟡 **Vertical Incident Timeline**: Build vertical milestone progress feed (SOS Created → Triaged → Responder Accepted → Arrived → Handover → Resolved).
- [ ] 🟡 **Automated Milestone Updates**: Connect WebSocket event stream to auto-append status updates.

#### Module 3 — Skill Upload & Verification UI
- [ ] 🟡 **Certificate Upload Interface**: File picker / camera capture sheet for medical certificate submission.
- [ ] 🟡 **Trust Score & Badge View**: Display verified skills, trust score points (+5 per skill), and verification status (`PENDING`, `APPROVED`).

#### Module 17 — Community Layer & Map Controls
- [ ] 🟢 **Map Filter Drawer**: Layer toggle switches for Responders, Hospitals, Blood Banks, Fire Stations, and AEDs.
- [ ] 🟢 **Hospital Bed Status Card**: Quick card preview showing available ICU & General beds for nearby trauma centers.

#### Settings & Theme Customization
- [ ] 🟡 **App Settings Screen**: Language selector (Bengali ⇄ English), notification preferences, emergency contacts editor.
- [ ] 🟢 **High Contrast Light/Dark Mode Switch**: Smooth theme toggling and OLED battery saving options.

---

### ⚪ Phase 3 — Guardian Mode, Polish & Defense (Month 4)

#### Module 21 — Guardian Safety Mode UI
- [ ] ⚪ **Guardian Radar Screen**: Radar visualization of nearby safety score and safe perimeter indicators.
- [ ] ⚪ **Guardian Contact Manager**: Native interface to add, edit, and toggle emergency circle guardians.

#### Module 22 — Offline SMS Fallback UI
- [ ] ⚪ **SMS Fallback Intent**: Automatic prompt suggesting SMS SOS transmission when cellular data connection is lost.

#### App Visual Polish & Media Assets
- [ ] 🟡 **Vector Assets Export**: Export custom map pins, icons, and SVG tokens into `assets/` and Android `res/drawable/`.
- [ ] 🟡 **Micro-Animations Polish**: Refine transition animations between screens, dialog entries, and bottom sheets.
- [ ] 🟡 **Viva Presentation Walkthrough**: Perform full visual demo walkthrough with Aritra to ensure flawless examiner defense.

---

## 📊 Dishari's Progress Summary

| Phase | Category | Total Tasks | Completed | Pending | Progress |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Phase 1** | Auth, SOS, Map, Tracking & AI UI | 22 | 22 | 0 | 🟢 **100% Complete** |
| **Phase 2** | Voice UI, Timeline, Skills & Settings | 10 | 0 | 10 | 🟡 Next Up |
| **Phase 3** | Guardian Mode, SMS Fallback & Polish | 6 | 0 | 6 | ⚪ Scheduled |
| **Total** | **All Deliverables** | **38** | **22** | **16** | 🟢 **58% Overall** |
