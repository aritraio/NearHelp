# Screen Instruction: R-P1 — Decline Reason Modal Pop-up

> **Pop-up ID**: `R_P1_DECLINE_REASON_POPUP`  
> **Persona**: Community Medical Responder  
> **UI Type**: 1-Tap Modal Dialog  
> **File/Composable Name**: `DeclineReasonDialog.kt`  
> **Invoked From**: `R3_RESPONDER_INCOMING_ALERT`  

---

## 1. Purpose & UX Objective
When a volunteer passes on an alert, speed is paramount. The system must immediately understand *why* (e.g., too far, not equipped, busy) in order to adjust dispatch heuristics and re-route the SOS to the next closest responder in fractions of a second without blocking the victim.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│             [ Dimmed Background ]                │
│                                                  │
│      ┌────────────────────────────────────┐      │
│      │  Why can't you respond right now?  │      │
│      │  (1-Tap to immediately re-dispatch)│      │
│      │                                    │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🚗 Too far / Traffic blocked │  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 💼 In work / Busy right now  │  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🩺 Lack required medical gear│  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🚶 Far from vehicle / on foot│  │      │
│      │  └──────────────────────────────┘  │      │
│      │                                    │      │
│      │  [ Cancel — Return to Alert ]      │      │
│      └────────────────────────────────────┘      │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 1-Tap Reason Action Cards
* 4 Full-Width Tappable Cards ($52\text{dp}$ height each):
  1. `🚗 Too far / Traffic blockage`
  2. `💼 In work / Personal emergency`
  3. `🩺 Lack required medical equipment (e.g. AED / Tourniquet)`
  4. `🚶 Currently on foot / No vehicle available`

### 3.2 Instant Re-Dispatch Trigger
* Tapping ANY of the 4 cards:
  * Instantly emits WebSocket event `DECLINE_DISPATCH(reason)`.
  * The backend AI dispatch service re-allocates the alert to the next ranked volunteer.
  * Dismisses the dialog and stops the siren.
  * Returns responder to `R2_RESPONDER_HOME_READINESS` with a subtle toast: *"Alert passed to Dr. Mukherjee"*.

### 3.3 Backout Option
* Text button: `Cancel — Return to Alert` (allows responder to change their mind and accept if tapped accidentally).
