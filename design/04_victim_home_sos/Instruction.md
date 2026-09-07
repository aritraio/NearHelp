# Screen Instruction: V4 — Home / SOS Main Screen

> **Screen ID**: `V4_HOME_SOS`  
> **Persona**: Victim / Citizen Main View  
> **UI Type**: Full Screen  
> **File/Composable Name**: `HomeScreen.kt`  
> **Route**: `/home_sos`  

---

## 1. Purpose & UX Objective
This is the primary distress interface of the NearHelp app. It is designed to be triggered under acute terror, trembling hands, and cognitive tunnel vision. The single, central, pulsating SOS button must be unmissable, oversized ($\ge 120\text{dp}$), and immediately accessible upon launch.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  NearHelp AI                    [👤 Medical ID]  │
│  📍 Park Street, Kolkata (Accurate to 4m)        │
│  ──────────────────────────────────────────────  │
│                                                  │
│                                                  │
│               ((((   SOS   ))))                  │
│             [ PULSING RED BUTTON ]               │
│               ((((  120dp  ))))                  │
│                                                  │
│          "Tap once for emergency help"           │
│                                                  │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [🤖 Ask AI Medical Assistant (Non-Urgent)]      │
│  [🔒 Anonymous Emergency Mode        (ON/OFF) ]  │
│  ──────────────────────────────────────────────  │
│  [📶 Cellular Active • 14 Responders Near You]   │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Top Bar & Location Indicator
* **App Brand Title**: `NearHelp AI` (20sp, Bold, `#D32F2F`).
* **Profile / Medical ID Avatar**: Top-right circular icon (taps navigate to `V3_MEDICAL_ID_ICE`).
* **Location Card**:
  * Icon: `Icons.Default.Place` (Color: `#D32F2F`).
  * Text: Live street name reverse-geocoded (e.g., *"Park Street, Kolkata"*).
  * Accuracy Badge: `Accurate to ±4m` (11sp, Safe Green text).

### 3.2 Main Central SOS Button
* **Dimensions**: $120\text{dp} \times 120\text{dp}$ circular button.
* **Colors**:
  * Inner Core: Emergency Red (`#D32F2F` / Dark: `#E53935`).
  * Outer Animated Rings: 2 concentric translucent ripple waves with animated scale and opacity:
    * Ring 1: $140\text{dp}$, Alpha $0.4$.
    * Ring 2: $165\text{dp}$, Alpha $0.2$.
* **Typography**: **`SOS`** (32sp, Extra-Bold, Pure White `#FFFFFF`).
* **Touch Target**: Minimum $120\text{dp}$ (Touch area spans $160\text{dp}$ with padding).
* **Feedback**: Heavy double-vibration pulse on tap (`HapticFeedbackType.LongPress`).
* **Action**: Immediately displays **`V-P1: Emergency Category Selector (5s Auto-Dispatch Pop-up)`**.

### 3.3 Bottom Action & Utility Cards
* **AI Medical Assistant Entry Chip**:
  * Container: Surface Variant (`#F1F3F4` / Dark: `#2C2C2C`), Border: `#0288D1`.
  * Text: `🤖 Consult AI First-Aid Guide (Non-Urgent)`.
  * Action: Opens conversational protocol assistant without raising an emergency dispatch alarm.
* **Anonymous Mode Switch**:
  * Text: `Mask My Name (Anonymous Emergency)`.
  * Switch: Material3 Switch. When ON, responders see "Anonymous Citizen".
* **Network & Volunteer Readiness Status Banner**:
  * Displays: `🟢 14 Verified Responders active within 3km`.

---

## 4. State Management & Navigation
* **`onSOSClick()`** $\rightarrow$ Show **`V-P1: Emergency Category Selector Pop-up`**.
* **`onAIAssistantClick()`** $\rightarrow$ Navigate to protocol search / AI assistant.
* **`onProfileClick()`** $\rightarrow$ Navigate to `V3_MEDICAL_ID_ICE`.
