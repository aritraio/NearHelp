# Screen Instruction: R-P2 — Confirm Patient Reached Pop-up Dialog

> **Pop-up ID**: `R_P2_CONFIRM_REACHED_DIALOG`  
> **Persona**: Community Medical Responder  
> **UI Type**: Verification Modal Dialog  
> **File/Composable Name**: `ConfirmPatientReachedDialog.kt`  
> **Invoked From**: `R5_LIVE_NAVIGATION_ENROUTE`  

---

## 1. Purpose & UX Objective
Arriving at an emergency scene transitions the mobile interface from transit navigation to on-scene medical care. This modal dialog verifies that the responder has physically made contact with the victim, preventing premature screen transitions while parking or walking up stairs.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│             [ Dimmed Background ]                │
│                                                  │
│      ┌────────────────────────────────────┐      │
│      │     🟢 CONFIRM PATIENT CONTACT     │      │
│      │                                    │      │
│      │  Are you physically with the       │      │
│      │  patient right now?                │      │
│      │                                    │      │
│      │  Confirming will notify the victim │      │
│      │  and switch your app into clinical │      │
│      │  assistance mode.                  │      │
│      │                                    │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ ✅ YES, I AM WITH THE PATIENT │  │      │
│      │  └──────────────────────────────┘  │      │
│      │                                    │      │
│      │  [ No, Still Finding Exact Spot ]  │      │
│      └────────────────────────────────────┘      │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Confirmation Action
* Primary Button: `[ ✅ YES, I AM WITH THE PATIENT ]`
* Container: Safe Green (`#2E7D32`), Height: 52dp, Bold White Text.
* Action:
  * Emits WebSocket event `RESPONDER_ARRIVED_CONFIRMED`.
  * Wakes victim's app with `V-P5: Responder Arrival Notice`.
  * Closes dialog and transitions responder immediately to **`R6: On-Scene Assistance Screen`**.

### 3.2 Dismiss Action
* Button: `No, Still Finding Exact Spot` (Outlined button, returns to navigation map).
