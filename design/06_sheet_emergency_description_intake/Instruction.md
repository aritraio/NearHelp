# Screen Instruction: V-P2 — Multi-Modal Emergency Description Bottom Sheet

> **Pop-up ID**: `V_P2_INTAKE_SHEET`  
> **Persona**: Victim / Bystander Intake  
> **UI Type**: Interactive Modal Bottom Sheet  
> **File/Composable Name**: `EmergencyIntakeBottomSheet.kt`  
> **Invoked From**: `V-P1_CATEGORY_SELECTOR`  

---

## 1. Purpose & UX Objective
After selecting an emergency category (or choosing to provide more context), this bottom sheet enables victims or bystanders to communicate the exact situation using three natural modalities: **Voice recording** (with animated audio waveform), **Photo capture** (for wound/scene classification), or **Quick symptom chips / text**. 

This multi-modal data is analyzed by the backend AI (Gemini 2.5) to determine severity (Level 1–5), identify required medical skills (e.g. CPR vs Tourniquet), and prepare approaching responders before they arrive.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Drag Handle ───]                               │
│  Selected: 🩺 Medical Emergency         [Cancel] │
│  "Provide details to guide approaching doctors"  │
│                                                  │
│  ┌────────────────────────────────────────────┐  │
│  │         🎙️ HOLD TO RECORD VOICE            │  │
│  │     |||||||||||||||||||||||||||||||        │  │
│  │   "Release to transcribe & analyze..."     │  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  [ 📸 Take Photo of Scene/Injury ] [🖼️ Gallery]  │
│  [ Thumbnail: wound_preview.jpg  (Uploaded ✓) ]  │
│                                                  │
│  Quick Symptoms (Tap to add):                    │
│  [ Chest Pain ] [ Not Breathing ] [ Severe Bleed]│
│  [ Unconscious] [ Seizure ]       [ Deep Cut ]   │
│                                                  │
│  [ Optional: Type specific notes...            ] │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [ 🚨 DISPATCH & FIND NEARBY HELP NOW ]         │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Sheet Configuration
* **Container**: Modal Bottom Sheet covering ~65% of screen height (`ModalBottomSheet` in Material3).
* **Shape**: Rounded Top Corners (`28dp`).
* **Handle**: Gray pill centered at top (`#BDBDBD`, width: 36dp, height: 4dp).

### 3.2 Voice SOS Recording Component
* **Action**: Press and Hold gesture (`Modifier.pointerInput`).
* **Audio Waveform Visualizer**:
  * Animated bar heights (16 vertical bars pulsing dynamically based on real-time microphone RMS amplitude).
  * Color: Emergency Red (`#D32F2F`).
* **Label State**:
  * Idle: *"🎙️ Hold to Speak (e.g., 'Grandfather collapsed on floor, not breathing')"*.
  * Recording: *"🔴 Recording audio... Release to analyze"*.
  * Finished: *"Transcribed: 'Male collapsed, gasping' ✓"*.

### 3.3 Photo Intake Toolbar
* **Camera Button**: Large rectangular button with camera icon (`Icons.Default.PhotoCamera`).
* **Instant Capture**: Opens camera viewfinder; captures photo; displays small thumbnail with green checkmark.

### 3.4 Symptom Quick-Chips
* Horizontal scrollable / flowing chips row.
* Clickable chips with high-contrast borders:
  * `Chest Pain`, `Unconscious`, `Not Breathing`, `Heavy Bleeding`, `Severe Burn`, `Choking`, `Broken Bone`.

### 3.5 Primary Action Button
* **Button Text**: `[ 🚨 DISPATCH & FIND HELP NOW ]`
* **Style**: Full-width, 56dp height, Color: Emergency Red (`#D32F2F`), Bold White Text.
* **Haptics**: Long press vibration.

---

## 4. State Management & Navigation
* On tapping **Dispatch**:
  * Packages Voice Audio + Photo + Tags + GPS coordinates into `EmergencyIntakePayload`.
  * Closes bottom sheet.
  * Navigates to **`V-S1: AI Processing & Triage Analysis`**, which transitions smoothly into **`V5: Active SOS Hub`**.
