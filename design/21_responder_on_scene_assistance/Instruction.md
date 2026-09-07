# Screen Instruction: R6 — On-Scene Assistance Screen (Arrived)

> **Screen ID**: `R6_ON_SCENE_ASSISTANCE`  
> **Persona**: Community Medical Responder (On Scene)  
> **UI Type**: Dedicated Clinical On-Scene Assistance Screen  
> **File/Composable Name**: `OnSceneAssistanceScreen.kt`  
> **Route**: `/on_scene_assistance`  

---

## 1. Purpose & UX Objective
Once the responder is on scene, navigation is no longer relevant. The responder requires instant access to the patient's full **Emergency Medical ID** (blood group, allergies, medications), specialized **AI Clinical First-Aid Guidance** (e.g. CPR cadence metronome tool), and **EMS 108 Coordination** for handing over the patient to incoming ambulances.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Status Bar]      🟢 ON-SCENE CLINICAL CARE     │
│  Incident: #NH-2026-8891 • Cardiac Arrest        │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ 👤 PATIENT MEDICAL ID & ALLERGIES          │  │
│  │ Name: Mr. Subhash Sen (Age ~52)            │  │
│  │ 🩸 Blood Group: O+ Positive                │  │
│  │ ⚠️ ALLERGIES: Penicillin, Sulfa Drugs      │  │
│  │ 🩺 Conditions: Hypertension, Chronic Asthma│  │
│  │ 📞 ICE Contact: Ananya Sen (+91 9830012345)│  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │ ⚡ CPR CADENCE METRONOME (110 BPM)         │  │
│  │ [ 🟢 ACTIVE BEAT — AUDIO CLICK & FLASH ]   │  │
│  │ Compressions: 30 : Breaths: 2              │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  Clinical Protocol Steps (WHO Guidelines):       │
│  [x] Verify unresponsiveness & absent pulse      │
│  [x] Call for AED / Defibrillator nearby         │
│  [ ] Continue CPR until 108 Ambulance arrives    │
│                                                  │
│  ──────────────────────────────────────────────  │
│  🚑 EMS Coordination: 108 Ambulance En Route     │
│  ETA: 5 mins • Vehicle WB-02-EMS-108             │
│  [ 📞 Contact 108 Paramedic Dispatcher ]         │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ 🏁 MARK EMERGENCY AS RESOLVED (Launches R-P3)]│
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Patient Medical ID Card
* Container: Surface Variant with high-contrast borders.
* Prominently highlights **Critical Drug Allergies** (Red pill: `⚠️ ALLERGIES: Penicillin`) to ensure responders do not administer contraindicated medications.
* Displays ICE next-of-kin contact with direct call button.

### 3.2 Clinical Tools — CPR Metronome
* **Visual Pulsing Heart**: Flashes red/green at exactly **110 beats per minute** (conforming to American Heart Association / WHO CPR guidelines).
* **Audio Beep**: Emits rhythmic acoustic clicks through phone speaker to keep bystanders compressing at the exact rhythm.
* **Ratio Counter**: Shows `30 compressions : 2 rescue breaths`.

### 3.3 EMS 108 Ambulance Tracking Strip
* Connects with official city ambulance dispatch data:
  * Shows Ambulance vehicle number and live ETA.
  * `[ 📞 Contact 108 Paramedic ]` button for responder-to-paramedic pre-arrival clinical briefing.

### 3.4 Resolution Action
* Primary Button: `[ 🏁 MARK EMERGENCY AS RESOLVED ]`
* Container: Deep Navy / Dark Surface (`#1A1C1E` / Dark: `#4CAF50`), Height: 56dp.
* Action: Launches **`R-P3: Emergency Handover & Resolution Bottom Sheet`**.

---

## 4. State Management & Lifecycle
* Keeps screen awake: `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`.
* Logs on-scene timestamps for audit and medico-legal compliance under India's Good Samaritan Law.
