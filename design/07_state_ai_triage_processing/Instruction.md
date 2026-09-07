# Screen Instruction: V-S1 — AI Processing & Triage Analysis State

> **State ID**: `V_S1_AI_TRIAGE_STATE`  
> **Persona**: Victim / Active Dispatch Transition  
> **UI Type**: Full-Screen Transitional State Overlay  
> **File/Composable Name**: `AITriageOverlay.kt`  
> **Invoked From**: `V-P1_CATEGORY_SELECTOR` or `V-P2_INTAKE_SHEET`  

---

## 1. Purpose & UX Objective
Between the moment the SOS is confirmed and the responders are mobilized, there is a 2 to 4 second window where the AI microservice processes multimodal inputs (transcribing audio, evaluating photos, matching emergency guidelines, and calculating severity). 

Rather than showing a generic loading spinner, this screen displays an active **Life-Saving Dispatch Radar** and reveals the real-time **AI Classification & Severity Triage Badge** (Levels 1 to 5), reassuring the user that the system is actively working on their rescue.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│                                                  │
│                                                  │
│              [ PULSING RADAR RINGS ]             │
│            ((((   📡 AI TRIAGE   ))))            │
│                                                  │
│         Analyzing scene & dispatching...         │
│                                                  │
│    ┌────────────────────────────────────────┐    │
│    │ 🏷️ AI Emergency Assessment:            │    │
│    │                                        │    │
│    │ [ BADGE: LEVEL 4 — CRITICAL EMERGENCY ]│    │
│    │ Category: Suspected Cardiac Arrest     │    │
│    │ Required Skills: CPR Certified Doctor   │    │
│    │ Recommended Protocol: Adult CPR (WHO)  │    │
│    └────────────────────────────────────────┘    │
│                                                  │
│      Pinging 8 verified responders within 2km    │
│      Radius expanding: 500m -> 1.5km -> 3km      │
│                                                  │
│      [ Auto-advances to Active SOS Hub ]         │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Animated Radar Graphic
* **Central Icon**: AI Sparkle + Emergency Beacon (`Icons.Default.AutoAwesome`).
* **Radar Sweep Animation**: Circular scan line rotating $360^\circ$ infinitely with translucent green/red ripple wave.
* **Duration**: Runs until backend WebSocket emits `EMERGENCY_DISPATCH_CONFIRMED` (typically $1.5\text{s} - 3.0\text{s}$).

### 3.2 AI Severity & Classification Card
* **Card Container**: `#FFFFFF` / Dark `#1E1E1E` with `#0288D1` (AI Blue) border.
* **Severity Badges (Color Tokens)**:
  * `Level 5 (Extreme/Fatal)`: Dark Red `#B71C1C` (e.g. Cardiac arrest, Arterial bleeding).
  * `Level 4 (Critical)`: Emergency Red `#D32F2F` (e.g. Unconscious, Major fracture).
  * `Level 3 (Urgent)`: Action Amber `#ED6C02` (e.g. Deep laceration, Moderate burn).
  * `Level 2 (Semi-Urgent)`: Blue `#0288D1` (e.g. Sprain, Mild burn).
  * `Level 1 (Non-Urgent)`: Green `#2E7D32` (e.g. Minor abrasion).
* **Protocol Grounding Label**: *"Matched WHO / Red Cross Adult CPR Guidelines"* (12sp, `#5F6368`).

### 3.3 Dynamic Dispatch Counter
* **Text**: *"Pinging 8 verified responders nearby..."*
* Shows dynamic animated ping markers expanding on a subtle minimap.

---

## 4. State Management & Navigation
* Upon receiving WebSocket event `DISPATCH_ACTIVE`:
  * Seamlessly crossfades directly into **`V5: Active SOS Hub`**.
