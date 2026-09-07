# Screen Instruction: V1 — Welcome / Authentication Screen

> **Screen ID**: `V1_WELCOME_LOGIN`  
> **Persona**: Victim / Public Citizen  
> **UI Type**: Full Screen  
> **File/Composable Name**: `WelcomeScreen.kt`  
> **Route**: `/welcome`  

---

## 1. Purpose & UX Objective
The Welcome / Authentication screen is the entry point for citizen users. In emergency response software, authentication must **never** become an obstacle between an incapacitated or panicked victim and life-saving assistance. The primary objective is to offer instant, zero-barrier **Guest Emergency Access** while providing frictionless one-tap Google and Phone OTP logins for non-crisis onboarding.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [Status Bar]                                    │
│                                                  │
│               🚨 NearHelp AI                     │
│    "Community Emergency Response Network"        │
│                                                  │
│       [ Animated Pulse Heartbeat / Shield ]      │
│                                                  │
│   Help is seconds away. Connect with nearby      │
│   verified doctors, CPR volunteers, and EMS.     │
│                                                  │
│  ──────────────────────────────────────────────  │
│  [🚨 EMERGENCY ACCESS (NO LOGIN REQUIRED)]       │
│  ──────────────────────────────────────────────  │
│                                                  │
│        ─── or sign in for Medical ID ───         │
│                                                  │
│  [ 🇬 Continue with Google                 ]     │
│  [ 📱 Continue with Phone Number           ]     │
│                                                  │
│  By continuing, you agree to Good Samaritan T&C. │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Header & Branding
* **App Logo / Beacon**: Heartbeat + Community Shield icon with subtle pulse ring.
* **App Title**: `NearHelp AI` (26sp, Bold, Inter, Color: `#1A1C1E` / Dark: `#FFFFFF`).
* **Tagline**: *"Community Emergency Response Network"* (14sp, Regular, Color: `#5F6368`).
* **Value Proposition Subtext**: *"Help is seconds away. Connecting you with nearby off-duty medical volunteers and community responders before 108/112 arrives."* (13sp, Regular, Center aligned).

### 3.2 Primary Emergency Button (Zero-Friction Trigger)
* **Button Text**: `🚨 EMERGENCY ACCESS (NO LOGIN REQUIRED)`
* **Touch Target**: Height $60\text{dp}$, Full Width (Padding: $16\text{dp}$).
* **Container Color**: Emergency Red (`#D32F2F` / Dark: `#E53935`).
* **Text Color**: Pure White (`#FFFFFF`), 16sp, Bold, All-Caps.
* **Elevation**: 4dp with subtle ripple feedback on tap.
* **Action / Navigation**: Instantly generates an ephemeral anonymous guest user session and navigates directly to `V4_HOME_SOS`.

### 3.3 Secondary Authentication Actions
* **Divider**: Subtle line with text *"or sign in for personalized Medical ID"* (12sp, `#757575`).
* **Google Sign-In Button**:
  * Style: Outlined Card with Google "G" logo.
  * Text: `Continue with Google` (15sp, Medium).
  * Height: 50dp.
* **Phone Number OTP Button**:
  * Style: Light Gray Surface (`#F1F3F4` / Dark: `#2C2C2C`).
  * Icon: Phone handset icon (`Icons.Default.Phone`).
  * Text: `Continue with Phone Number`.
  * Height: 50dp.

### 3.4 Footer Legal Text
* **Disclaimer**: *"By continuing, you agree to NearHelp Terms of Service and Good Samaritan Guidelines (India, 2016)."* (11sp, Color: `#757575`).

---

## 4. State Management & Navigation
* **`onEmergencyGuestClick()`** $\rightarrow$ Navigate to `V4_HOME_SOS` (`launchSingleTop = true`).
* **`onGoogleAuthSuccess()`** $\rightarrow$ Check if profile exists; if first time navigate to `V2_PERMISSIONS_SETUP`, else `V4_HOME_SOS`.
* **`onPhoneAuthClick()`** $\rightarrow$ Show Phone OTP Bottom Sheet modal.

---

## 5. Accessibility & Edge Cases
* **Screen Reader**: `contentDescription = "Instant Emergency Access without login"`.
* **Offline Handling**: Emergency Guest Access must operate **100% locally** even if the phone has zero internet connection, immediately landing on `HomeScreen` with cellular/SMS fallback active.
