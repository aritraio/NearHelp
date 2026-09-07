# Screen Instruction: R1 — Responder Onboarding & Credential Verification

> **Screen ID**: `R1_RESPONDER_ONBOARDING`  
> **Persona**: Community Medical Responder / Volunteer  
> **UI Type**: Full Screen  
> **File/Composable Name**: `ResponderOnboardingScreen.kt`  
> **Route**: `/responder_onboarding`  

---

## 1. Purpose & UX Objective
To protect patients and maintain clinical accountability, responders who receive priority dispatches must declare their clinical competency and provide verifiable credentials. This screen guides doctors, paramedics, certified first-aiders, and volunteers through credential submission and shows their real-time verification review status.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [<-]  Responder Verification Setup              │
│                                                  │
│  Join our certified community emergency network  │
│                                                  │
│  1. Select Medical Qualification Tier:           │
│  (o) Tier 1: Medical Doctor (MBBS / MD / DO)     │
│  ( ) Tier 2: Paramedic / Nurse / EMT             │
│  ( ) Tier 3: Certified First-Aider (Red Cross)   │
│  ( ) Tier 4: Community Good Samaritan Volunteer  │
│                                                  │
│  2. Professional Registration / License:         │
│  [ Medical Council Registration: WBMC-129482   ] │
│                                                  │
│  3. Upload Verification Documents:               │
│  ┌────────────────────────────────────────────┐  │
│  │ 📄 Medical Degree / Registration Cert      │  │
│  │    [ Uploaded: dr_rahul_reg.pdf (✓) ]      │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │ 🪪 Government Photo ID (Aadhaar / Passport)│  │
│  │    [ 📸 Take Photo of ID ]                 │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  Verification Status:                            │
│  [ ⏳ UNDER VERIFICATION BY MEDICAL BOARD ]       │
│                                                  │
│  [ SUBMIT CREDENTIALS FOR VERIFICATION ]        │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Medical Qualification Tier Selection
* Radio cards representing 4 distinct skill levels:
  1. `Tier 1: Doctor / Physician` (Full ACLS/BLS, emergency medicine authorization).
  2. `Tier 2: Nurse / Paramedic / EMT` (BLS, trauma, airway stabilization).
  3. `Tier 3: Certified First-Aider` (CPR/AED certified via St. John Ambulance, Red Cross, or NDMA).
  4. `Tier 4: Trained Citizen Volunteer` (Basic bystander response, crowd control, AED fetcher).

### 3.2 Document Upload Cards
* **License Number Input**: Outlined text field with formatting.
* **Document Upload Surface**:
  * Action: Upload PDF or Camera Scan.
  * Preview: Thumbnail with file name and size badge.

### 3.3 Verification Status Badge
* **States**:
  * `Unsubmitted`: Gray pill.
  * `Under Review`: Amber pill (`#ED6C02`) with animated clock.
  * `Verified`: Safe Green pill (`#2E7D32`) with shield checkmark icon.

### 3.4 Primary Action
* Button: `[ SUBMIT CREDENTIALS FOR VERIFICATION ]` (Full width, Height: 52dp, Color: `#1A1C1E` / Dark: `#4CAF50`).
* Navigates to `R2_RESPONDER_HOME_READINESS`.
