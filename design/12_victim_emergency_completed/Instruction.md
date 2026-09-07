# Screen Instruction: V6 — Emergency Completed Screen

> **Screen ID**: `V6_EMERGENCY_COMPLETED`  
> **Persona**: Victim / Post-Crisis Wrap-Up  
> **UI Type**: Full Screen  
> **File/Composable Name**: `EmergencyCompletedScreen.kt`  
> **Route**: `/emergency_completed`  

---

## 1. Purpose & UX Objective
Once the responder marks the emergency resolved (or the patient is handed over to a professional 108 ambulance), the victim's app transitions to this closing screen. It brings psychological closure, summarizes key incident metrics (response time, responders who mobilized), and transitions smoothly into feedback and safety verification.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│                                                  │
│           [ Large Green Checkmark Shield ]       │
│           "Emergency Response Completed"         │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 📋 Incident Summary Report                 │  │
│  │                                            │  │
│  │ Incident ID: #NH-2026-8891                │  │
│  │ Category: Medical (Cardiac Arrest)         │  │
│  │ Total Duration: 14 minutes 20 seconds      │  │
│  │ Primary Responder: Dr. Rahul Sen (MD)      │  │
│  │ Outcome: Handed over to 108 Ambulance      │  │
│  │ Destination: Kolkata Medical College       │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  NearHelp volunteers have been stood down.       │
│  A copy of this emergency report is saved to     │
│  your Medical Profile.                           │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ ⭐ RATE RESPONDER & REVIEW INCIDENT ]        │
│  [ Return to Home Screen ]                       │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Hero Graphic
* Icon: `Icons.Default.CheckCircle` (Size: 72dp, Tint: `#2E7D32` Safe Green).
* Headline: `Emergency Response Completed` (22sp, Bold, Inter).
* Subtitle: *"Assistance successfully rendered"* (14sp, Color: `#5F6368`).

### 3.2 Incident Summary Card
* Container: Surface Card (`#FFFFFF` / Dark `#1E1E1E`), Border: 1dp `#E0E0E0`, Corner Radius: 16dp.
* Key-Value Summary Table:
  * `Incident ID`: `#NH-2026-8891` (monospace).
  * `Category & Severity`: `Level 4 Critical (Medical)`.
  * `Response Timeline`: `SOS: 18:42` $\rightarrow$ `Arrived: 18:45` $\rightarrow$ `Completed: 18:56`.
  * `Primary Responder`: `Dr. Rahul Sen (Verified Doctor)`.
  * `Final Outcome`: `Patient stabilized & transferred to 108 EMS`.

### 3.3 Navigation Actions
* **Primary Button**: `[ ⭐ RATE RESPONDER & REVIEW INCIDENT ]` (Full-width, 52dp height, Color: `#1A1C1E` / Dark: `#4CAF50`).
  * Action: Navigates to `V7_POST_INCIDENT_RATING`.
* **Secondary Text Button**: `Return to Home Screen` (Navigates directly to `V4_HOME_SOS`).

---

## 4. State Management & Navigation
* Stores incident archive record in local SQLite database.
* Clears active emergency cache and geofence tracking services.
