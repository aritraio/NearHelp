# Screen Instruction: R5 — Live Turn-by-Turn Navigation Screen (En-Route)

> **Screen ID**: `R5_LIVE_NAVIGATION_ENROUTE`  
> **Persona**: Community Medical Responder (In Transit)  
> **UI Type**: Dedicated Full-Screen GPS Navigation  
> **File/Composable Name**: `ResponderNavigationScreen.kt`  
> **Route**: `/responder_navigation`  

---

## 1. Purpose & UX Objective
While the responder is travelling to the patient (running or riding a scooter), they cannot interact with complicated clinical menus. 

This dedicated full-screen view is strictly optimized for **hands-free situational driving/walking navigation**: high-contrast turn maneuver arrows, live ETA/distance header, external Google Maps intent shortcut, floating Call/Chat icons, and an unmissable **`[ 🟢 I HAVE ARRIVED / PATIENT REACHED ]`** swipe slider.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  ┌────────────────────────────────────────────┐  │
│  │ ➡️ In 120m, Turn Right onto Park Street    │  │
│  │    ⏱️ ETA: 2 mins • 320m remaining         │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│                                                  │
│              [ FULL-SCREEN GPS MAP ]             │
│                                                  │
│         Moving Blue Pin (You on Scooter)         │
│                     │                            │
│           ══════════╧════════════                │
│                 Route Polyline                   │
│                     ▼                            │
│         Pulsing Red Marker (Victim)              │
│                                                  │
│  [ Open Google Maps ↗️ ]      [ 📞 Call Victim ]  │
│  [ 🩸 Blood: O+ | Asthma ]   [ 💬 Chat (1) ]    │
│                                                  │
│  ──────────────────────────────────────────────  │
│  ┌────────────────────────────────────────────┐  │
│  │ >>> SWIPE TO CONFIRM ARRIVAL AT PATIENT    │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Turn-by-Turn Guidance Banner
* Top Card: Dark charcoal container (`#1E1E1E`), Pure White text.
* Maneuver Icon: Large white turn arrow (`Icons.Default.TurnRight`, 36dp).
* Next Step: `In 120m, Turn Right onto Park Street`.
* Live ETA Pill: `ETA: 2 mins • 320m remaining` (Action Amber `#FF9800` or Safe Green `#4CAF50`).

### 3.2 Full-Screen Map (Mapbox / Google Maps SDK)
* 3D Perspective Tilt ($45^\circ$) showing direction of travel.
* Camera auto-rotates with responder bearing.
* Displays victim's location pin with animated pulse.

### 3.3 Quick Heads-Up Floating Shortcuts
* `[ Open Google Maps ↗️ ]`: Chip to launch external turn-by-turn navigation app if desired.
* `[ 🩸 Blood: O+ | Asthma ]`: Quick pill showing victim's critical medical tags.
* `[ 📞 Call Victim ]`: Floating action button to phone the victim/bystander.
* `[ 💬 Chat ]`: Floating bubble to message bystander.

### 3.4 Arrival Confirmation Swipe Slider (`SlideToArriveBar.kt`)
* Track: Full-width green container (`#E8F5E9` / Dark: `#1B5E20`), Height: 60dp.
* Slider Thumb: Large circular green button with double arrow `>>>`.
* Text: *"SWIPE TO CONFIRM ARRIVAL AT PATIENT"*.
* Interaction: Swiping past 80% width prompts **`R-P2: Confirm Patient Reached Dialog`** to verify on-scene arrival before switching to clinical mode.

---

## 4. State Management & Navigation
* Telemetry: Sends location every 5 seconds over WebSocket to update victim's map.
* Geofence Auto-Trigger: If within 25m, the arrival slider glows bright green.
* On swipe: Launches **`R-P2: Confirm Patient Reached Dialog`**, which advances to **`R6: On-Scene Assistance Screen`**.
