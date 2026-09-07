# Screen Instruction: R7 — Response Completed & Debrief Screen

> **Screen ID**: `R7_RESPONDER_COMPLETED_DEBRIEF`  
> **Persona**: Community Medical Responder (Post-Mission)  
> **UI Type**: Full Screen  
> **File/Composable Name**: `ResponderDebriefScreen.kt`  
> **Route**: `/responder_debrief`  

---

## 1. Purpose & UX Objective
After high-intensity emergency interventions, medical volunteers experience adrenaline crash and emotional fatigue. This debrief screen provides positive psychological validation, summarizes the mission impact, updates the volunteer's community service record, and smoothly restores the responder to an Available (or Off-Duty) status.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│                                                  │
│          [ Gold Community Hero Shield ]          │
│          "THANK YOU FOR SAVING A LIFE!"          │
│          Your rapid response made the difference │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ ⏱️ Mission Statistics: #NH-2026-8891       │  │
│  │                                            │  │
│  │ • Total Response Time: 14 mins 20 secs     │  │
│  │ • Arrival Speed: 2 mins 45 secs (Fastest!) │  │
│  │ • Distance Traveled: 650 meters            │  │
│  │ • Key Interventions: CPR Administered      │  │
│  │ • Handover: Ambulance 108 (Paramedic Team) │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  🏆 +50 Community Rescue Points Earned           │
│  New Badge Unlocked: [ 🏅 Rapid Lifesaver ]      │
│                                                  │
│  Post-Response Availability:                     │
│  [x] Keep me AVAILABLE for new emergencies       │
│  [ ] Set me to OFF-DUTY for 30 minutes rest      │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ RETURN TO RESPONDER DASHBOARD ]               │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Hero Recognition Banner
* Graphic: Animated gold star/shield with ribbon.
* Title: `THANK YOU FOR SAVING A LIFE!` (22sp, Bold, Inter).
* Subtitle: *"Your community is safer because of you."* (14sp, Safe Green `#2E7D32`).

### 3.2 Mission Metrics Card
* Key performance metrics:
  * Total Incident Duration.
  * Time from Alert to Scene Arrival.
  * Recorded Clinical Intervention.
  * Verified EMS Handover Recipient.

### 3.3 Rest & Readiness Toggle
* Radio/Checkbox selection for responder wellness:
  * `Keep me Available for new emergencies` (Default).
  * `Set me to Off-Duty for 30 minutes rest (Cooldown)`.

### 3.4 Return Action
* Button: `[ RETURN TO RESPONDER DASHBOARD ]` (Full width, 52dp height, Dark `#1A1C1E` / Dark: `#4CAF50`).
* Action: Navigates back to `R2_RESPONDER_HOME_READINESS`.
