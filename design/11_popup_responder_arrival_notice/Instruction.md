# Screen Instruction: V-P5 — Responder Arrival Alert Pop-up

> **Pop-up ID**: `V_P5_RESPONDER_ARRIVAL_POPUP`  
> **Persona**: Victim / Bystander On-Scene Alert  
> **UI Type**: High-Contrast Floating Modal Alert  
> **File/Composable Name**: `ResponderArrivalDialog.kt`  
> **Invoked From**: `V5_ACTIVE_SOS_HUB` (Triggered via geofence or responder status)  

---

## 1. Purpose & UX Objective
When an en-route responder approaches within 30 meters of the victim or swipes "I Have Arrived", the victim needs immediate visual and haptic confirmation that help is right outside. This eliminates confusion on crowded streets, dark alleys, or residential buildings, allowing the victim or bystander to identify the responder instantly.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│             [ Dimmed Background ]                │
│                                                  │
│      ┌────────────────────────────────────┐      │
│      │      🟢 RESPONDER HAS ARRIVED!     │      │
│      │                                    │      │
│      │    [ Dr. Rahul Sen (Doctor) ]      │      │
│      │    Wearing: Yellow High-Vis Jacket │      │
│      │    Vehicle: Blue Activa (WB-01-XX) │      │
│      │                                    │      │
│      │  "Please look out for the doctor   │      │
│      │   and wave your hands or shout."   │      │
│      │                                    │      │
│      │  [ 📢 SOUND LOUD LOCATOR BEEP ]    │      │
│      │                                    │      │
│      │  [ ✅ CONFIRM CONTACT MADE ]        │      │
│      └────────────────────────────────────┘      │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Alert Visuals & Haptics
* **Header**: Large Safe Green Badge with beacon icon (`#2E7D32`).
* **Title**: `🟢 RESPONDER HAS ARRIVED!` (20sp, Bold, Inter).
* **Haptics**: Tri-pulse staccato vibration pattern (`HapticFeedbackType.LongPress` $\times 3$).

### 3.2 Responder Identification Card
* Display Photo: Circular avatar of the responder with verified checkmark.
* Name & Title: `Dr. Rahul Sen (Verified MD)`.
* Visual Identifiers (Supplied by responder profile):
  * Attire/Gear: *"Carrying Red First-Aid Kit"*
  * Vehicle: *"Blue Two-Wheeler (Registration: WB-01-XX)"*

### 3.3 Audio Locator Beeper
* Button: `[ 📢 SOUND LOUD LOCATOR BEEP ]`
* When tapped, makes the phone emit a high-pitched acoustic locator pulse to help the responder locate the victim in a crowded building or at night.

### 3.4 Confirmation Action
* Primary Button: `[ ✅ CONFIRM CONTACT MADE ]` (Color: `#2E7D32`, 52dp height).
* Action: Transitions active SOS state into on-scene clinical assistance.

---

## 4. State Management & Navigation
* On tapping "Confirm Contact Made":
  * Dismisses alert.
  * Updates Tab 1 on `V5_ACTIVE_SOS_HUB` to `Status: On-Scene Assistance Active`.
