# Screen Instruction: R-P3 — Emergency Handover & Resolution Bottom Sheet

> **Pop-up ID**: `R_P3_HANDOVER_RESOLUTION_SHEET`  
> **Persona**: Community Medical Responder  
> **UI Type**: Structured Bottom Sheet Modal  
> **File/Composable Name**: `EmergencyResolutionSheet.kt`  
> **Invoked From**: `R6_ON_SCENE_ASSISTANCE`  

---

## 1. Purpose & UX Objective
Before a community responder stands down, the app must capture the clinical outcome of the incident (e.g. handed over to official paramedics, patient stabilized, or false alarm). This ensures medical continuity of care and provides an auditable timestamp for emergency services.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Drag Handle ───]                               │
│  🏁 Conclude Emergency Response                  │
│  "Select final outcome to complete mission"      │
│                                                  │
│  Clinical Outcome (Select one):                  │
│  (o) Handed over to 108 Ambulance / Paramedics   │
│  ( ) Patient stabilized on-scene (No transfer)   │
│  ( ) Patient transported via private vehicle     │
│  ( ) Patient refused care / Non-compliant        │
│  ( ) False Alarm / Patient not found at location │
│                                                  │
│  Clinical Handover Notes (Optional):             │
│  ┌────────────────────────────────────────────┐  │
│  │ "Administered 3 cycles CPR. Return of      │  │
│  │  spontaneous circulation achieved before   │  │
│  │  108 crew took over with defibrillator."   │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ 🏁 SUBMIT & COMPLETE EMERGENCY RESPONSE ]     │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Outcome Radio Selection Group
* High-contrast selectable options:
  1. `Handed over to 108 Ambulance / Paramedics` (Default).
  2. `Patient stabilized on-scene (No hospital transfer needed)`.
  3. `Patient transported via private vehicle to hospital`.
  4. `Patient refused care`.
  5. `False Alarm / Patient not found`.

### 3.2 Handover Clinical Notes Box
* Outlined text area with placeholder: *"Briefly note interventions made (e.g. CPR duration, bleeding control, AED shocks)..."*.
* Speech-to-text microphone button inside input box for rapid voice dictation.

### 3.3 Completion Trigger
* Button: `[ 🏁 SUBMIT & COMPLETE EMERGENCY RESPONSE ]`
* Container: Safe Green (`#2E7D32`), 54dp height, Bold White Text.
* Action:
  * Emits WebSocket event `COMPLETE_INCIDENT_HANDOVER(outcome, notes)`.
  * Closes sheet.
  * Transitions victim to `V6_EMERGENCY_COMPLETED`.
  * Navigates responder to **`R7: Response Completed & Debrief Screen`**.
