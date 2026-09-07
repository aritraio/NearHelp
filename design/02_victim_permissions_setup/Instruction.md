# Screen Instruction: V2 — Permissions & Emergency Access Setup Screen

> **Screen ID**: `V2_PERMISSIONS_SETUP`  
> **Persona**: Victim / Citizen Onboarding  
> **UI Type**: Full Screen  
> **File/Composable Name**: `PermissionsSetupScreen.kt`  
> **Route**: `/permissions_setup`  

---

## 1. Purpose & UX Objective
Emergency applications require several critical OS-level Android permissions (Precise Background Location, Microphone for voice SOS, Camera for triage photos, and Full-Screen Alarm / DND Bypass). If permissions are requested abruptly in the middle of a cardiac arrest or road accident, dispatch fails. This onboarding screen educates the citizen on *why* each permission is indispensable for saving lives and grants them in a single, transparent workflow.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Top Bar]  Setup Emergency Protection  [Skip]  │
│                                                  │
│  To dispatch nearest responders and guide you in │
│  a crisis, NearHelp requires these permissions:  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 📍 Precise Location (Always / Background)   │  │
│  │    Allows nearby responders to locate you.  │  │
│  │    [ STATUS: GRANTED (Green Check) ]       │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🎙️ Microphone (Voice SOS)                  │  │
│  │    Enables hold-to-speak voice distress.   │  │
│  │    [ GRANT PERMISSION BUTTON ]             │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │ 📸 Camera (Injury / Scene Intake)          │  │
│  │    Allows AI triage of bleeding or burns.  │  │
│  │    [ GRANT PERMISSION BUTTON ]             │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🚨 Critical Alert / Bypass DND             │  │
│  │    Wakes device during responder arrival.  │  │
│  │    [ GRANT PERMISSION BUTTON ]             │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  [ ✅ GRANT ALL & PROCEED TO HOME ]            │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Header Section
* **Title**: `Setup Emergency Protection` (20sp, Semi-Bold, `#1A1C1E`).
* **Description**: *"In a critical incident, seconds count. These permissions allow responders and AI dispatch to reach you without delays."* (14sp, Color: `#5F6368`).
* **Skip Action**: Text button (top-right): `Skip for now` (Color: `#757575`).

### 3.2 Permission Card Items (4 Cards)
Each card uses Container Color `#FFFFFF` / Dark `#1E1E1E` with a 1dp border (`#E0E0E0`):
1. **Precise Location (Background & Foreground)**:
   * Icon: `Icons.Default.LocationOn` (Tint: `#D32F2F`).
   * Title: `Precise Live Location`.
   * Description: `Required to geofence and dispatch volunteers within a 500m-3km walking radius.`
2. **Microphone (Audio SOS)**:
   * Icon: `Icons.Default.Mic` (Tint: `#0288D1`).
   * Title: `Microphone (Voice SOS)`.
   * Description: `Enables hands-free voice SOS when fingers are shaking or incapacitated.`
3. **Camera (Scene & Injury Assessment)**:
   * Icon: `Icons.Default.CameraAlt` (Tint: `#ED6C02`).
   * Title: `Camera Access`.
   * Description: `Allows instant photo intake so AI triage can classify severe trauma or hemorrhage.`
4. **Full-Screen Alarm & DND Bypass**:
   * Icon: `Icons.Default.NotificationsActive` (Tint: `#2E7D32`).
   * Title: `Urgent Alarm & DND Bypass`.
   * Description: `Allows response alerts to wake your screen and ring even in Silent/DND mode.`

### 3.3 Status & Action Buttons
* **Granted State**: Card turns light green border with green checkmark pill: `Granted ✓`.
* **Not Granted State**: Outlined button inside card: `Allow`.
* **Bottom Action Button**: `[ ✅ GRANT ALL & PROCEED ]` (Full width, Height: 54dp, Container: `#1A1C1E` / Dark: `#4CAF50`).

---

## 4. State Management & Navigation
* On tapping "Grant All", launch Android's `rememberLauncherForActivityResult(RequestMultiplePermissions)`.
* When all required permissions are granted (or user skips), navigate to `V3_MEDICAL_ID_CONTACTS` (or `V4_HOME_SOS`).
