# Screen Instruction: V-P3 — Emergency Services Escalation (108/112) Bottom Sheet

> **Pop-up ID**: `V_P3_ESCALATION_SHEET`  
> **Persona**: Victim / Bystander Escalation  
> **UI Type**: Action Bottom Sheet Modal  
> **File/Composable Name**: `EmergencyEscalationSheet.kt`  
> **Invoked From**: `V5_ACTIVE_SOS_HUB`  

---

## 1. Purpose & UX Objective
Community volunteers provide immediate bridge care, but severe trauma, cardiac arrests, fires, and violent crimes require professional EMS (108/102/112/101). This bottom sheet provides a **one-tap direct dialer shortcut** to professional emergency hotlines without losing track of the ongoing NearHelp community response.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Drag Handle ───]                               │
│  🚨 Contact Professional Emergency Services      │
│  "NearHelp dispatches community volunteers.      │
│   For critical emergencies, call official EMS."  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🚑 CALL 108 — AMBULANCE & MEDICAL HELPLINE │  │
│  │    Direct 24x7 Government Emergency Call   │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🚓 CALL 112 / 100 — POLICE & NATIONAL HELP │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🚒 CALL 101 — FIRE & RESCUE DISPATCH       │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [x] Mark status: "Ambulance 108 Contacted"      │
│      (Notifies en-route volunteers that EMS is en-route)
│                                                  │
│  [ Dismiss / Return to Live Map ]                │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Sheet Container
* Material3 Modal Bottom Sheet with rounded top corners (`24dp`).
* Background: `#FFFFFF` / Dark `#1E1E1E`.

### 3.2 3 Hotline Action Buttons (Oversized 60dp Touch Targets)
1. **🚑 108 — Ambulance / Medical Helpline**:
   * Container: Emergency Red (`#D32F2F`), Bold White Text.
   * Icon: `Icons.Default.MedicalServices`.
   * Action: Triggers Android `Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))`.
2. **🚓 112 / 100 — Police Helpline**:
   * Container: `#1565C0` (Dark Blue), Bold White Text.
   * Icon: `Icons.Default.LocalPolice`.
   * Action: Triggers `Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))`.
3. **🚒 101 — Fire & Rescue Dispatch**:
   * Container: `#D84315` (Deep Orange), Bold White Text.
   * Icon: `Icons.Default.LocalFireDepartment`.
   * Action: Triggers `Intent(Intent.ACTION_DIAL, Uri.parse("tel:101"))`.

### 3.3 EMS Status Synchronization Checkbox
* Checkbox: `[x] Notify responders that 108 ambulance has been contacted`.
* When checked, sends a broadcast event via WebSocket: updates the timeline in Tab 3 (*"18:44:05 — Victim phoned 108 Ambulance"*), alerting incoming community responders to prepare for hospital handover.

---

## 4. State Management & Navigation
* On dialing, system marks `isAmbulanceContacted = true`.
* Closes sheet and returns user directly to `V5_ACTIVE_SOS_HUB`.
