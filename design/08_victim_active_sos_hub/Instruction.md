# Screen Instruction: V5 — Active SOS Split-Screen Hub

> **Screen ID**: `V5_ACTIVE_SOS_HUB`  
> **Persona**: Victim / Active Distress Core Screen  
> **UI Type**: Split-Screen Layout (Top 40% Map / Bottom 60% Swipeable 3-Tab Container)  
> **File/Composable Name**: `ActiveSOSHubScreen.kt`  
> **Route**: `/active_sos`  

---

## 1. Purpose & UX Objective
The Active SOS Hub is the mission-critical operational cockpit for the victim. During an ongoing crisis, switching between different screens causes panic and disorientation. 

To solve this, the screen uses a **40/60 Split-Screen Architecture**:
* **Top 40% (Fixed)**: Interactive Live GPS Map continuously tracking the approaching responder and nearby medical resources.
* **Bottom 60% (Swipeable 3 Tabs)**:
  * **Tab 1: `Responder Status`** — Radar search, responder card, ETA, and direct VoIP call.
  * **Tab 2: `AI First-Aid`** — Step-by-step checklist, voice audio guidance, and legal disclaimer.
  * **Tab 3: `Incident Chat`** — Real-time chat with responder, automated timeline logs, and translation.
* **Bottom Escalation Bar**: Persistent `[ 📞 CALL 108 AMBULANCE ]` and `[ Cancel SOS ]` buttons.

---

## 2. Visual Layout & Component Hierarchy

```
┌────────────────────────────────────────────────────────┐
│ [TOP 40% - FIXED LIVE GPS MAP]:                        │
│  📍 You (Red Beacon)           🩺 Dr. Rahul (Blue 450m)│
│  🏥 Medical College (1.2km)    ⚡ AED Station (200m)   │
│  [Recenter Button]             [Traffic Polyline Path] │
├────────────────────────────────────────────────────────┤
│ [BOTTOM 60% - SWIPEABLE TABS]:                         │
│  ┌──────────────────┬──────────────────┬─────────────┐ │
│  │ 👤 RESPONDER     │ 🤖 AI FIRST-AID  │ 💬 CHAT (2) │ │
│  └──────────────────┴──────────────────┴─────────────┘ │
│                                                        │
│  ── [TAB 1: RESPONDER STATUS ACTIVE] ──                │
│  ┌──────────────────────────────────────────────────┐  │
│  │ [Avatar] Dr. Rahul Sen (Verified MD / ACLS)      │  │
│  │ 🛵 On Two-Wheeler • 📍 450m away • ETA: 3 MINS   │  │
│  │ [ 📞 Call Responder ]    [ 💬 Message ]          │  │
│  └──────────────────────────────────────────────────┘  │
│  Status: [● Accepted] ──> [● En-Route] ──> [○ Arrived] │
│                                                        │
│  ── [TAB 2: AI FIRST-AID CHECKLIST (WHEN ACTIVE)] ──   │
│  [🔊 Read Aloud Voice Guidance] [ CPR Metronome 110]   │
│  [x] Step 1: Check Responsiveness & Airway             │
│  [ ] Step 2: Begin Chest Compressions (Push Hard/Fast) │
│  ⚠️ Non-Dismissible Good Samaritan Legal Disclaimer    │
│                                                        │
│  ── [TAB 3: INCIDENT CHAT (WHEN ACTIVE)] ──            │
│  18:42 — System: SOS Triggered (Category: Medical)     │
│  18:43 — Dr. Rahul: "I'm on a bike, 3 mins away!"     │
│  [ Type message...                          ] [ Send ] │
├────────────────────────────────────────────────────────┤
│ [PERSISTENT ESCALATION BAR]:                           │
│ [ 📞 CALL 108 AMBULANCE (Red) ]   [ Cancel SOS (Gray) ]│
└────────────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Top 40% Map Container (`LiveRescueMap.kt`)
* **Height**: $40\%$ of total window height (`Modifier.weight(0.4f)`).
* **Map Elements**:
  * Victim Marker: Pulsing Red Beacon (`#D32F2F`) showing location accuracy radius.
  * Responder Marker: Custom Blue Marker (`#0288D1`) with transit icon (Scooter/Car/Walking) and ETA bubble (`"3m"`).
  * Facility Layer: Toggles for nearest Hospital, AED, and Police station pins.
  * Route Line: High-contrast blue polyline with turn route.

### 3.2 Bottom 60% Tabbed Container (`TabRow` / `HorizontalPager`)
* **Tab Indicator**: 3 Tabs with clear icons and badge counters:
  1. `👤 Responder` (Shows green pulsing dot when responder is en-route).
  2. `🤖 AI First-Aid` (Shows protocol name, e.g. *"CPR Protocol"*).
  3. `💬 Chat` (Shows unread message badge count).

#### Tab 1: Responder Status Details
* When searching: Radar radar card with expanding radius (`1km -> 3km`).
* When accepted:
  * **Card Container**: `#FFFFFF` / Dark `#1E1E1E`, 2dp elevation.
  * **Photo & Verification**: Circular avatar with green checkmark shield (`Verified Doctor`).
  * **ETA & Distance**: `ETA: 3 mins (450m)` (Bold, 18sp, Color: `#2E7D32`).
  * **Action Buttons**:
    * `📞 Call`: Masked VoIP in-app audio call.
    * `💬 Message`: Switches active tab to Tab 3.

#### Tab 2: AI First-Aid Instructions Details
* Step-by-step checklist based on WHO/Red Cross:
  * Interactive Checkbox Cards with bold action titles (e.g. **"APPLY FIRM PRESSURE"**).
* `🔊 Play Voice Coach`: Activates Android Text-To-Speech (TTS) reading instructions step-by-step through loudspeaker.
* CPR Metronome button: Plays 110 BPM rhythmic audio pulses to pace chest compressions.
* **Statutory Disclaimer (Persistent at Bottom of Tab)**:
  ```
  ⚠️ DISCLAIMER: Based on WHO/Red Cross protocols. Not a substitute for professional
  medical advice. Protected under India's Good Samaritan Law (2016). Always call 108.
  ```

#### Tab 3: Incident Chat Details
* Real-time WebSocket chat room.
* Injects automated system timeline events (e.g. `18:42:01 — Dr. Rahul Accepted Alert`).
* Automatic translation pill: *"Bengali -> English"*.

### 3.3 Persistent Escalation Bottom Bar
* Fixed at screen bottom:
  * `[ 📞 CALL 108 AMBULANCE ]`: Extra-large red button (Launches `V-P3: Escalation Sheet`).
  * `[ Cancel SOS ]`: Outlined gray button (Launches `V-P4: Cancel Dialog`).

---

## 4. State Management & Lifecycle
* Listens to real-time WebSockets:
  * `RESPONDER_ACCEPTED` $\rightarrow$ Updates Tab 1 with responder card.
  * `RESPONDER_LOCATION_UPDATE` $\rightarrow$ Animates responder marker on top map.
  * `RESPONDER_ARRIVED` $\rightarrow$ Shows **`V-P5: Responder Arrival Alert Pop-up`**.
  * `INCIDENT_RESOLVED` $\rightarrow$ Navigates to **`V6: Emergency Completed Screen`**.
