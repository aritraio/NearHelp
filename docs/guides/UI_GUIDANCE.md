# NearHelp AI — Mobile App UI/UX Guidance

> **Target Platform**: Android (Kotlin + Jetpack Compose)  
> **UI Lead**: Dishari  
> **Design Philosophy**: High-Urgency, Stress-Resilient, One-Tap Accessibility, High Contrast  

---

## 📱 1. Design System & Aesthetics

In emergency situations, users experience panic, reduced dexterity, and stress. The UI must prioritize **extreme clarity, instant comprehension, and large touch targets**.

### 🎨 Color Palette
| Token | Hex Code | Usage / Context |
| :--- | :--- | :--- |
| **Emergency Red (Primary)** | `#E53935` | Main SOS Button, Critical Alerts, Level 5 Emergency |
| **Action Amber (Secondary)** | `#FF9800` | Warning, Medium Urgency, Pending Dispatch |
| **Safe Green** | `#4CAF50` | Responder Arrived, Safe Status, Verified Skill Badges |
| **AI Info Blue** | `#2196F3` | AI Guidance Card, Chat Bubble, System Notifications |
| **Background (Dark Default)**| `#121212` | Dark background for high contrast & OLED battery savings |
| **Card Surface** | `#1E1E1E` | Card containers, dialog backgrounds |
| **Text High Contrast** | `#FFFFFF` | Primary headers, critical instruction text |
| **Text Medium Contrast** | `#B0BEC5` | Metadata, timestamps, secondary labels |

### 🔤 Typography & Touch Targets
* **Font Family**: Inter or Roboto (Clean sans-serif for fast legibility).
* **Minimum Touch Target**: 48dp for standard buttons, **72dp+ for SOS triggers**.
* **Visual Hierarchy**: Heavy bold headers for action steps (e.g. **"APPLY PRESSURE TO WOUND"**), 16sp+ for AI guidance text.

---

## 🧭 2. App Navigation & State Architecture

NearHelp AI operates in two primary modes: **Victim Mode** (Requesting help) and **Responder Mode** (Providing help).

```
                      ┌───────────────────────────┐
                      │   NearHelp Navigation    │
                      └─────────────┬─────────────┘
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌──────────────────────┐                          ┌──────────────────────┐
│     Victim Mode      │                          │    Responder Mode    │
├──────────────────────┤                          ├──────────────────────┤
│ 1. One-Tap SOS Screen│                          │ 1. High-Priority Alert│
│ 2. AI Triage & Direct│                          │ 2. Victim Location & │
│ 3. Live Tracking Map │                          │    Medical Card View │
│ 4. Emergency Chat    │                          │ 3. Rescue Navigation │
└──────────────────────┘                          └──────────────────────┘
```

### 📍 Bottom Navigation Bar (5 Core Tabs)
1. **🚨 SOS / Home**: Instant emergency creation and active emergency status.
2. **🗺️ Live Map**: Dynamic map displaying responders, hospitals, AEDs, and police stations.
3. **🤖 AI Crisis Assist**: Instant first-aid protocol lookup & AI chat assistant.
4. **💬 Incident Timeline & Chat**: Multi-party chat room with automated milestone logs.
5. **👤 Profile & Medical ID**: Verified badges, medical profile, emergency contacts, legal info.

---

## 🖼️ 3. Key Screen Wireframes & UI Layouts

### 1️⃣ Screen 1: Main SOS Trigger Screen (`HomeScreen.kt`)
* **Central SOS Button**: Large red circular button with pulse ripple animation (`Animatable`). Includes haptic feedback on tap.
* **Emergency Category Chips**:
  * 🩺 `Medical` | 🔥 `Fire` | 🛡️ `Crime` | 🚗 `Accident`
* **Multi-Modal Intake Toolbar**:
  * 🎙️ **Hold-to-Voice SOS**: Hold to record voice, showing animated audio visualizer waveform.
  * 📸 **Photo Intake**: Attach photo of injury/scene for AI detection.
* **Anonymous Emergency Toggle**: Simple switch for privacy-conscious users.
* **Offline Status Banner**: Displays fallback indicator (e.g. *"Cellular Network Only — SMS Backup Active"*) when offline.

```
┌──────────────────────────────────────────────────┐
│  NearHelp AI                     [👤 Profile]    │
│  ──────────────────────────────────────────────  │
│  Select Crisis Type:                             │
│  [🩺 Medical] [🔥 Fire] [🛡️ Crime] [🚗 Accident] │
│                                                  │
│                                                  │
│                    (  SOS  )                     │
│                [ PULSING BUTTON ]                │
│                                                  │
│                                                  │
│  [🎙️ Hold to Speak]         [📸 Take Photo]      │
│  ──────────────────────────────────────────────  │
│  [🔒 Anonymous Emergency Mode        (ON/OFF) ]  │
└──────────────────────────────────────────────────┘
```

---

### 2️⃣ Screen 2: Active SOS & AI Guidance Screen (`ActiveSOSScreen.kt`)
* **3-Layer Escalation Status Bar**:
  * *Layer 1 (0-30s)*: Searching nearby responders (Radius: 1km → 3km → 5km).
  * *Layer 2 (45s)*: Direct 108/112 Ambulance dial card.
  * *Layer 3*: Guided offline self-care active.
* **AI Severity & Classification Card**:
  * Badge: `Level 4 — Critical (Possible Cardiac Arrest)`
* **AI Protocol First-Aid Guidance Card**:
  * Step-by-step checklist formatted with checkmark boxes.
* **Persistent Legal Disclaimer (Mandatory)**:
  ```
  ⚠️ DISCLAIMER: Guidance is based on WHO/Red Cross protocols and is NOT medical advice.
  Always call 108/112 for serious emergencies. Protected under India's Good Samaritan Law (2016).
  ```

---

### 3️⃣ Screen 3: Live Rescue Map & Tracking Screen (`LiveMapScreen.kt`)
* **Map Elements (Google Maps SDK)**:
  * 📍 **Victim Pin**: Red pulsing beacon marker.
  * 🩺 **Responder Pins**: Blue markers with skill icons (Doctor icon for verified MD, CPR badge for volunteer).
  * 🏥 **Facility Markers**: Toggle layers for Hospitals, AEDs, Blood Banks, Fire/Police Stations.
* **Bottom ETA Sheet**:
  * Shows closest responder photo, verified badge, ETA (e.g. *"3 mins away"*), distance (e.g. *"600m"*), and `Call` / `Chat` buttons.

---

### 4️⃣ Screen 4: Responder Incoming SOS Alert Modal (`ResponderAlertActivity.kt`)
* **Full-Screen High-Priority Overlay**: Wakes phone screen and bypasses Do-Not-Disturb (FCM High Priority).
* **Card Details**:
  * Emergency Type & Severity.
  * Distance to victim & calculated ETA.
  * Required skills (e.g. *"CPR Certified Needed"*).
* **Primary Actions**:
  * 🟢 **[ I'M RESPONDING ]** (Large green full-width button).
  * ⚪ **[ Pass / Decline ]**.

---

### 5️⃣ Screen 5: In-App Emergency Chat & Voice Room (`ChatScreen.kt`)
* **Real-time Socket.io Chat Feed**.
* **Auto-Translation Pill**: Messages from non-native speakers show a banner: *"Translated from Bengali → English"*.
* **Automated Timeline Events**: System logs injected directly into chat stream:
  * `00:00 - SOS Triggered`
  * `00:02 - Dr. Rahul Accepted SOS (Doctor Verified)`
  * `00:05 - Responder 200m away`

---

## 🛡️ 4. Safety & UX Rules for Emergency Apps

1. **Zero-Barrier SOS (No Friction)**:
   * Never force a login screen when a user taps SOS. Support **Guest / Anonymous Emergency Mode**.
2. **Non-Dismissible AI Legal Disclaimer**:
   * All screens displaying AI first-aid procedures MUST feature the non-dismissible Good Samaritan Law notice to prevent medical liability.
3. **High Haptic & Sound Feedback**:
   * Triggering SOS requires a strong double-vibration pulse to confirm success without needing the user to look closely at the screen.
4. **Offline Resilience Visual Feedback**:
   * If internet drops, immediately render a yellow offline alert banner and display locally cached WHO/Red Cross first-aid cards.

---

## 🛠️ 5. Jetpack Compose Implementation Structure for Dishari

Organize Jetpack Compose code cleanly into components:

```
ui/
├── theme/
│   ├── Color.kt             // Emergency red, dark backgrounds, severity colors
│   ├── Type.kt              // Inter typography scale
│   └── Theme.kt             // NearHelpTheme wrapper
├── components/
│   ├── SOSButton.kt         // Animated pulse SOS trigger composable
│   ├── AIDisclaimerCard.kt  // Mandatory Good Samaritan disclaimer card
│   ├── SeverityBadge.kt     // Level 1-5 severity pill
│   ├── ResponderETACard.kt  // ETA & bottom sheet component
│   └── WaveformVisualizer.kt// Voice SOS audio wave composable
└── screens/
    ├── HomeScreen.kt        // One-tap SOS screen
    ├── ActiveSOSScreen.kt   // AI triage & step-by-step guidance
    ├── LiveMapScreen.kt     // MapView with custom markers
    ├── ChatScreen.kt        // Live chat & timeline feed
    └── ProfileScreen.kt     // Medical ID & Verified Skills
```

### 💡 Quick Code Example for `AIDisclaimerCard.kt`

```kotlin
@Composable
fun AIDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "⚠️ Guidance based on WHO/Red Cross protocols. Not a substitute for professional medical advice. Protected under India's Good Samaritan Law (2016). Always call 108/112.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
            )
        }
    }
}
```
