# Screen Instruction: R4 — Response Accepted & Route Lock Screen

> **Screen ID**: `R4_RESPONDER_ACCEPTED`  
> **Persona**: Community Medical Responder  
> **UI Type**: Full Screen Transition  
> **File/Composable Name**: `ResponseAcceptedScreen.kt`  
> **Route**: `/response_accepted`  

---

## 1. Purpose & UX Objective
Once the responder taps "Accept", this screen confirms that the dispatch is officially locked to them. The victim is notified that a doctor is on the way, the victim's exact GPS location is rendered on a route preview, transit mode is confirmed, and the responder is launched into live turn-by-turn rescue navigation.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│                                                  │
│          [ Safe Green Checkmark Shield ]         │
│          "RESPONSE CONFIRMED & LOCKED"           │
│          Victim has been notified you're coming  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 📍 Destination: Park Street Crossing       │  │
│  │    Patient: Male, ~50s (Cardiac Arrest)    │  │
│  │    Distance: 650m • Estimated ETA: 3 mins  │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  Confirm Transit Mode for Navigation:            │
│  [ 🛵 Two-Wheeler (Selected) ] [ 🚶 Walking ]    │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ [ Mini Route Map Preview with Blue Path ]  │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ 🚀 START LIVE NAVIGATION (En-Route) ]         │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Confirmation Banner
* Icon: `Icons.Default.Verified` (Size: 64dp, Tint: `#2E7D32`).
* Title: `RESPONSE CONFIRMED` (22sp, Bold, Inter).
* Subtitle: *"Victim and bystanders can see your live location"* (14sp, `#5F6368`).

### 3.2 Patient & Destination Capsule
* Destination Address: Live reverse-geocoded landmark.
* Distance & ETA badge: `650m • 3 mins`.
* Transit Mode Selector: Two-Wheeler, Walking, or Car.

### 3.3 Navigation Launch Action
* Button: `[ 🚀 START LIVE NAVIGATION ]`
* Height: 56dp, Color: Emergency Red (`#D32F2F`) or Action Green (`#2E7D32`), 18sp Bold.
* Action: Navigates to **`R5: Live Turn-by-Turn Navigation Screen`** and starts high-frequency GPS telemetry broadcasting.
