# Screen Instruction: R2 — Responder Home & Readiness Screen

> **Screen ID**: `R2_RESPONDER_HOME_READINESS`  
> **Persona**: Community Medical Responder Main Dashboard  
> **UI Type**: Full Screen  
> **File/Composable Name**: `ResponderHomeScreen.kt`  
> **Route**: `/responder_home`  

---

## 1. Purpose & UX Objective
This is the operational readiness hub for verified medical responders. It provides an immediate **Available / Unavailable Master Switch**, allowing doctors and volunteers to toggle when they are on-call or off-duty. It also allows responders to customize their active dispatch radius and track their rescue history.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  NearHelp Responder             [ Dr. Rahul Sen ]│
│  Badge: Verified Physician (ACLS Certified)      │
│  ──────────────────────────────────────────────  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │  STATUS: 🟢 AVAILABLE TO RESPOND           │  │
│  │  [ MASTER ON-DUTY TOGGLE SWITCH: ON ]      │  │
│  │  "You will receive urgent emergency alerts │  │
│  │   for incidents within your coverage zone."│  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  Dispatch Radius: [ Slider: 2.0 km ]             │
│  [ 500m ──●────────────────────────────── 5km ]  │
│                                                  │
│  Transit Mode:                                   │
│  [ 🚶 Walking ] [ 🛵 Scooter (Active) ] [ 🚗 Car]│
│                                                  │
│  Current Location:                               │
│  📍 Camac Street, Kolkata (High Accuracy GPS)    │
│                                                  │
│  ──────────────────────────────────────────────  │
│  🏆 Your Response Impact & Statistics            │
│  [ 18 Incidents Responded ] [ 3 mins Avg Arrival]│
│  [ ⭐ 4.9 Rating (24 Reviews) ]                  │
│                                                  │
│  Recent Responses:                               │
│  • Yesterday: Cardiac Arrest (Handed to 108) ✓   │
│  • 3 days ago: Road Traffic Accident ✓           │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Top Header & Profile
* Title: `NearHelp Responder` (20sp, Bold).
* Verification Shield: Doctor's avatar with green badge (`Verified Physician`).

### 3.2 Master On-Duty Readiness Card
* Container: Safe Green Surface (`#E8F5E9` / Dark: `#1B5E20`), Border: 1.5dp `#2E7D32`.
* Header: `🟢 AVAILABLE TO RESPOND` (18sp, Bold, `#2E7D32`).
* Master Switch: Material3 Large Switch.
* When toggled OFF:
  * Container turns neutral gray (`#F5F5F5` / Dark: `#2C2C2C`).
  * Text: `⚪ OFF-DUTY (UNAVAILABLE) — No dispatches will sound`.

### 3.3 Coverage Radius & Transit Selector
* **Slider**: Range $500\text{m}$ to $5.0\text{km}$ (Step $500\text{m}$).
* **Transit Mode Toggle Group**:
  * Single-choice selectable chips: `🚶 Walking (0-800m)`, `🛵 Two-Wheeler (0-3km)`, `🚗 Car (0-5km)`.
  * Informs the dispatch algorithm regarding realistic ETA calculation.

### 3.4 Impact & Response History Card
* Metrics Row (3 compact statistic cards):
  * `18`: Incidents Responded.
  * `3m 12s`: Average Arrival Speed.
  * `4.9 ★`: Community Rating.
* Recent Incident List: Expandable list showing timestamp, category, and outcome.

---

## 4. State Management & Dispatch Listener
* Runs background service `ResponderLocationService` reporting live GPS coordinates to backend Redis every 15 seconds.
* Listens on WebSocket channel `responder:<user_id>:dispatch`.
* When an incident is assigned, triggers **`R3: Full-Screen Urgent Emergency Alert`**.
