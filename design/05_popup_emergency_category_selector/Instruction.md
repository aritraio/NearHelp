# Screen Instruction: V-P1 — Emergency Category Selector (5s Auto-Dispatch Pop-up)

> **Pop-up ID**: `V_P1_CATEGORY_SELECTOR`  
> **Persona**: Victim / Citizen Distress  
> **UI Type**: Modal Pop-up Dialog  
> **File/Composable Name**: `EmergencyCategoryDialog.kt`  
> **Invoked From**: `V4_HOME_SOS`  

---

## 1. Purpose & UX Objective
When the victim taps the SOS button, NearHelp immediately asks: *"What kind of emergency is this?"* to route the alert to the proper specialists (e.g. CPR doctors for cardiac arrest vs volunteer fire safety teams). 

**Life-Safety Critical Rule**: If the victim is choking, passing out, or under attack and cannot tap an option, a **5-second animated countdown timer** automatically completes and dispatches the alert as a **Critical General/Medical Emergency**.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│             [ Dimmed Dark Background ]           │
│                                                  │
│      ┌────────────────────────────────────┐      │
│      │   What kind of emergency is this?  │      │
│      │                                    │      │
│      │        [ ( 5s ) CIRCLE TIMER ]     │      │
│      │      "Auto-dispatching in 5s..."   │      │
│      │                                    │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🩺 Medical / Cardiac / Trauma │  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🚗 Road Accident / Crash     │  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🔥 Fire / Gas Leak / Hazard  │  │      │
│      │  └──────────────────────────────┘  │      │
│      │  ┌──────────────────────────────┐  │      │
│      │  │ 🛡️ Crime / Assault / Threat  │  │      │
│      │  └──────────────────────────────┘  │      │
│      │                                    │      │
│      │       [ Cancel (False Alarm) ]     │      │
│      └────────────────────────────────────┘      │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Modal Container
* **Background Scrim**: `#000000` with 65% opacity.
* **Card Surface**: `#FFFFFF` / Dark: `#1E1E1E`, Rounded Corners: `24dp`, Padding: `20dp`.
* **Dismiss On Backpress**: Disabled (must explicitly tap Cancel).

### 3.2 5-Second Circular Auto-Dispatch Timer
* **Diameter**: $64\text{dp}$.
* **Stroke**: 6dp thick circular progress indicator (`CircularProgressIndicator`).
* **Timer Color**: Animated from Safe Green $\rightarrow$ Action Amber $\rightarrow$ Emergency Red as seconds tick down from 5 to 0.
* **Center Number**: `5`, `4`, `3`, `2`, `1` (20sp, Bold, Inter).
* **Subtext**: *"Auto-dispatching in 5s if untouched..."* (12sp, Medium, `#5F6368`).
* **Haptic Cue**: Short tick vibration on every second (`HapticFeedbackType.TextHandleMove`).

### 3.3 4 Category Cards (Large 56dp Touch Targets)
1. 🩺 **Medical / Cardiac / Unconscious**:
   * Container: `#FFEBEE` / Dark: `#2C1517`.
   * Border: 1.5dp `#D32F2F`.
   * Text: `🩺 Medical / Cardiac / Trauma` (16sp, Semi-Bold, `#D32F2F`).
2. 🚗 **Road Accident / Crash**:
   * Container: `#FFF3E0` / Dark: `#2E1E0F`.
   * Border: 1.5dp `#ED6C02`.
   * Text: `🚗 Road Accident / Vehicle Crash` (16sp, Semi-Bold, `#ED6C02`).
3. 🔥 **Fire / Hazard / Burn**:
   * Container: `#FBE9E7` / Dark: `#2C1814`.
   * Border: 1.5dp `#D84315`.
   * Text: `🔥 Fire / Gas Leak / Explosion` (16sp, Semi-Bold, `#D84315`).
4. 🛡️ **Crime / Physical Threat**:
   * Container: `#EDE7F6` / Dark: `#1D172A`.
   * Border: 1.5dp `#512DA8`.
   * Text: `🛡️ Crime / Assault / Threat` (16sp, Semi-Bold, `#512DA8`).

### 3.4 Cancel Action
* **Text Button**: `Cancel (False Alarm)` (14sp, `#757575`, dismisses dialog and returns to `V4_HOME_SOS`).

---

## 4. State Management & Dispatch Triggers
* **Branch A (User selects a category)**:
  * Cancel countdown timer.
  * Dismiss Pop-up.
  * Immediately open **`V-P2: Multi-Modal Emergency Description Bottom Sheet`** with selected category pre-filled.
* **Branch B (Countdown reaches 0s)**:
  * System marks category as `General Medical (Critical)`.
  * Dismiss Pop-up.
  * Immediately transition to **`V-S1: AI Processing & Triage`** and auto-dispatch to nearby responders.
