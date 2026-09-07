# Screen Instruction: V3 — Medical ID & Emergency Contacts (ICE) Setup Screen

> **Screen ID**: `V3_MEDICAL_ID_ICE`  
> **Persona**: Victim / Citizen Profile  
> **UI Type**: Full Screen  
> **File/Composable Name**: `MedicalIDScreen.kt`  
> **Route**: `/medical_id`  

---

## 1. Purpose & UX Objective
When a citizen is involved in an accident or loses consciousness, first responders need immediate access to critical physiological parameters (Blood Group, Drug Allergies, Chronic Illnesses) and next-of-kin contacts (In Case of Emergency - ICE). This screen securely configures this data and automatically generates an SMS distress beacon protocol for registered family members during an SOS.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [<-]  Emergency Medical ID & ICE      [Save]    │
│                                                  │
│  This data is only revealed to verified          │
│  responders during an active emergency.          │
│                                                  │
│  🩸 Blood Group: [ Dropdown: O+ ▼ ]              │
│                                                  │
│  ⚠️ Known Allergies (e.g. Penicillin, Peanuts):   │
│  [ Penicillin × ] [ Latex × ] [ + Add Allergy ]  │
│                                                  │
│  🩺 Chronic Medical Conditions:                  │
│  [ Asthma × ] [ Type-2 Diabetes × ] [ + Add ]    │
│                                                  │
│  💊 Current Medications:                         │
│  [ Inhaler (Albuterol), Metformin 500mg        ] │
│                                                  │
│  ──────────────────────────────────────────────  │
│  📞 Emergency Contacts (ICE - In Case of Emergency)
│                                                  │
│  1. Ananya Sen (Wife) — +91 98300 12345          │
│     [x] Auto-send SMS with live GPS link on SOS  │
│                                                  │
│  2. Rajesh Roy (Brother) — +91 98311 67890       │
│     [x] Auto-send SMS with live GPS link on SOS  │
│                                                  │
│  [ + Add Another ICE Contact ]                   │
│                                                  │
│  [ SAVE MEDICAL ID & FINISH SETUP ]              │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Blood Group Dropdown
* **Label**: `Blood Type (ABO & Rh System)` (14sp, Semi-Bold).
* **Options**: `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`, `Unknown / Don't Know`.
* **Visual**: Outlined card with red droplet icon (`Icons.Default.WaterDrop`, Tint: `#D32F2F`).

### 3.2 Medical Tag Chips (Allergies & Conditions)
* **Allergies Container**:
  * Chips: Amber background (`#FFF3E0` / Dark: `#3E2723`), Text: `#E65100`, with `×` remove icon.
  * `+ Add Allergy` button: Opens quick text dialog with autocomplete suggestions (`Penicillin`, `Latex`, `NSAIDs`, `Sulfa Drugs`, `Peanuts`, `Bee Stings`).
* **Chronic Conditions Container**:
  * Chips: Blue background (`#E1F5FE` / Dark: `#01579B`), Text: `#0277BD`.
  * Common presets: `Asthma`, `Cardiac Disease`, `Epilepsy / Seizures`, `Diabetes`, `Hypertension`.

### 3.3 Emergency Contacts List (ICE)
* Allows up to **3 emergency contacts**.
* Each item displays:
  * Name and Relationship pill (e.g., `Spouse`, `Parent`, `Sibling`, `Friend`).
  * Verified Phone Number.
  * Switch / Checkbox: `Auto-send SOS Distress SMS with live Google Maps coordinate link`.
* `+ Add Contact` button (launches Android Contact Picker intent `ContactsContract.CommonDataKinds.Phone`).

### 3.4 Save Action
* **Button**: `[ SAVE MEDICAL ID & FINISH SETUP ]` (Full width, Height: 52dp, Color: `#2E7D32` / Safe Green).

---

## 4. State Management & Navigation
* Stores data in Encrypted Room / DataStore preferences.
* Navigates to `V4_HOME_SOS`.
