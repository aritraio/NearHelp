# Screen Instruction: V-P4 — False Alarm / Cancel SOS Confirmation Dialog

> **Pop-up ID**: `V_P4_CANCEL_SOS_DIALOG`  
> **Persona**: Victim / Bystander  
> **UI Type**: Modal Dialog with Anti-Accidental Safety  
> **File/Composable Name**: `CancelSOSDialog.kt`  
> **Invoked From**: `V5_ACTIVE_SOS_HUB`  

---

## 1. Purpose & UX Objective
Accidentally cancelling an ongoing emergency when a responder is already rushing to the scene could have fatal consequences. Conversely, genuine false alarms or incidents resolved without assistance must be quickly cancelled to release medical volunteers back into duty. 

This dialog uses a **Slide-to-Cancel Confirmation Slider** and requires a **cancellation reason** to prevent unintentional dismissals.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│             [ Dimmed Dark Background ]           │
│                                                  │
│      ┌────────────────────────────────────┐      │
│      │  ⚠️ Cancel Emergency SOS?          │      │
│      │                                    │      │
│      │  Responders currently en route     │      │
│      │  will be stood down immediately.   │      │
│      │                                    │      │
│      │  Select Reason for Cancellation:   │      │
│      │  (o) Accidental SOS trigger        │      │
│      │  ( ) Resolved without help needed  │      │
│      │  ( ) 108 Ambulance already arrived │      │
│      │  ( ) Test / Drill                  │      │
│      │                                    │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ >>> SLIDE TO CONFIRM CANCEL  │  │      │
│      │  └──────────────────────────────┘  │      │
│      │                                    │      │
│      │   [ KEEP EMERGENCY ACTIVE (SAFE) ] │      │
│      └────────────────────────────────────┘      │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Modal Container
* Surface: `#FFFFFF` / Dark: `#1E1E1E`.
* Warning Accent: Border `#D32F2F` (1.5dp).
* Header: Red Alert Icon + Bold Title: `Cancel Emergency SOS?` (18sp, `#D32F2F`).

### 3.2 Cancellation Reason Radio Group
* Mandatory selection (Default selected: `Accidental SOS trigger`):
  * `Accidental SOS trigger`
  * `Situation resolved without help needed`
  * `Official EMS / 108 already arrived`
  * `Test / Simulated emergency`

### 3.3 Slide-to-Confirm Slider (`SlideToCancelBar.kt`)
* Track: Light red surface (`#FFEBEE` / Dark: `#3E1F21`), Height: 54dp, Corner radius: 27dp.
* Thumb Button: Red circular slider ($48\text{dp}$) with double arrows `>>>`.
* Text: *"Slide to confirm cancel"*.
* Interaction: Requires sliding thumb across $\ge 85\%$ of the track width before cancellation is triggered. Releasing prematurely bounces the thumb back to the start.

### 3.4 Dismiss Action (Keep Active)
* Button: `[ KEEP EMERGENCY ACTIVE (SAFE) ]` (Full-width, Safe Green `#2E7D32` or neutral gray, immediately closes dialog).

---

## 4. State Management & Navigation
* Upon successful slide completion:
  * Emits WebSocket event `CANCEL_EMERGENCY(reason)`.
  * Dismisses dialog.
  * Navigates back to `V4_HOME_SOS` with a toast: *"Emergency cancelled. Volunteers stood down."*.
