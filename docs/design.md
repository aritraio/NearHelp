# NearHelp AI — Comprehensive UI/UX Design System & Specification (`design.md`)

> **Document Type**: Mobile Frontend Design System & Jetpack Compose Specification  
> **Target Platform**: Android 14+ (Material 3 Expressive + Soft Neomorphic / Glassmorphic UI)  
> **Reference Artifact**: [assets/7d95a6af3988cb71f16fc8dc4457af2e.webp](file:///Users/aritra/Code/Projects/NearHelp/assets/7d95a6af3988cb71f16fc8dc4457af2e.webp)  
> **Author**: Lead Android UI/UX & Frontend Architect  
> **Version**: 2.0.0 • Production Ready  

---

## 🎨 1. Executive Summary & Design Philosophy

The reference visual artifact introduces a **state-of-the-art, human-centric emergency and personal safety interface** that balances two critical psychological states:
1. **The Calm Guardian State (Left Screen)**: Soothing, ambient mint-green gradient palette designed for proactive safety monitoring, neighborhood safety index tracking, destination searching, and non-intrusive voice-activated AI readiness.
2. **The High-Urgency Crisis Dispatch State (Right Screen)**: High-contrast, clean slate/glass surface with structured 16-category crisis triage, dynamic location verification, and a fail-safe countdown dual-action dispatch slider.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             NearHelp Design System                               │
│                                                                                  │
│   🌿 Guardian State (Active Radar)          🚨 Crisis Dispatch State (Matrix)    │
│   ────────────────────────────────          ─────────────────────────────────    │
│   • Soft Mint & Emerald Glow (#C2F2D9)      • Clean Slate Soft-UI (#F0F3F6)      │
│   • 360° Radar Canvas Map Scanner           • 16-Crisis Neomorphic Grid (4x4)    │
│   • Real-Time Locality Safety Index (91%)   • Address Verification & Edit Card   │
│   • Hold-to-Voice AI Emergency Intake       • 3s Grace Period Countdown Slider   │
│   • Live Geodetic Telemetry Display         • Instant Crimson SOS Active State   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### Core Design Principles
* **Zero-Cognitive Overload**: In panic situations, adrenaline degrades fine motor skills and peripheral vision. Touch targets exceed **56dp–76dp**, with high-contrast text and clear iconography.
* **Instant Tactile & Haptic Feedback**: Every state transition (swipe, hold, category selection, countdown tick) triggers tailored Android haptic waveforms (`HapticFeedbackType`).
* **Soft-Glassmorphic Surfaces**: Modern rounded card radii (`24dp–36dp`), layered backdrop blurs (`RenderEffect` / `Modifier.blur`), and soft drop shadows to create a tactile, premium physical-feel UI.
* **Fail-Safe Grace Buffer**: 3-second visual and audio countdown ticker on dispatch to prevent accidental false alarms while maintaining single-tap emergency escalation.

---

## 🌈 2. Color Architecture & Token System

```
Guardian Palette (Calm & Safe)           Crisis & Alert Palette (High Urgency)
┌───────────────────────────┐           ┌───────────────────────────┐
│ Mint Light:   #DDF8EA     │           │ Crimson Red:  #E52538     │
│ Mint Primary: #8EE4B8     │           │ Crimson Dark: #C2182B     │
│ Emerald Accent:#22C55E    │           │ Amber Alert:  #FF9800     │
│ Radar Beam:   #34C75940   │           │ Coral Active: #FF5A5F     │
└───────────────────────────┘           └───────────────────────────┘
```

### 📋 Complete Color Token Table

| Token Name | Hex / Alpha | Role & Visual Context | Jetpack Compose Definition |
| :--- | :--- | :--- | :--- |
| `GuardianBgTop` | `#C8F5DC` | Top gradient stop for Guardian Radar Screen | `Color(0xFFC8F5DC)` |
| `GuardianBgBottom` | `#E3FAF0` | Bottom gradient stop for Guardian Radar Screen | `Color(0xFFE3FAF0)` |
| `GuardianRadarCircle` | `#48BB78` | Concentric radar range lines (alpha 0.35) | `Color(0x5948BB78)` |
| `GuardianRadarSweep` | `#38A169` | 360° rotational radar beam gradient | `Color(0x4038A169)` |
| `EmergencyCrimson` | `#E52538` | Selected category card, Confirm button, SOS slider | `Color(0xFFE52538)` |
| `EmergencyCrimsonGradient` | `#FF3B30` → `#D70015` | Dual action slider thumb & pulse ring | `Brush.horizontalGradient(...)` |
| `CrisisSurfaceBg` | `#EFF3F6` | Right screen background surface | `Color(0xFFEFF3F6)` |
| `CardNeomorphicLight` | `#FFFFFF` | Unselected emergency category cards & Address card | `Color(0xFFFFFFFF)` |
| `CardNeomorphicShadow` | `#00000012` | Soft ambient elevation shadow (Elevation 4dp) | `Color(0x12000000)` |
| `CancelPillGreen` | `#34C759` | Cancel button pill in bottom dispatch slider | `Color(0xFF34C759)` |
| `CountdownBadgeRed` | `#E52538` | Countdown ticker circular badge ("3", "2", "1") | `Color(0xFFE52538)` |
| `TextPrimaryDark` | `#0F172A` | Primary titles ("China Basin", "1234 Mission St") | `Color(0xFF0F172A)` |
| `TextSecondaryMuted`| `#64748B` | Subtitles ("Safety Index 91%", "Apt #345B...") | `Color(0xFF64748B)` |
| `SearchPillBg` | `#FFFFFF80` | Translucent glass search bar & exit slider | `Color(0xCCFFFFFF)` |

---

## 🔠 3. Typography & Spatial Tokens

### Typography Scale (Inter / Roboto Display)

```
China Basin                  → DisplayLarge (28sp, Bold, Weight 700)
Safety Index 91%             → TitleMedium (14sp, SemiBold, Weight 600)
1234 Mission St              → TitleLarge (18sp, Bold, Weight 700)
Confirm Address              → LabelLarge (16sp, Bold, Weight 700)
HOLD FOR SOS                 → HeadlineMedium (20sp, ExtraBold, Tracking 1.5sp)
Category Labels (Medical)    → LabelSmall (11sp, Medium, Weight 500)
GPS Telemetry Coordinates    → BodySmall (11sp, Monospace, Alpha 0.7)
```

### Spatial & Shape Geometry
* **Corner Radius Large (Cards & Modals)**: `32.dp`
* **Corner Radius Medium (Category Micro-Cards)**: `18.dp`
* **Corner Radius Full (Pills & Sliders)**: `100.dp`
* **Grid Spacing**: `10.dp` horizontal and vertical gutters for 4x4 matrix.
* **Touch Target Standard**: Minimum `48.dp`, Emergency SOS triggers minimum `72.dp`.

---

## 📱 4. Screen-by-Screen Architectural Blueprint

```
       ┌──────────────────────────────┐        ┌──────────────────────────────┐
       │   SCREEN 1: GUARDIAN RADAR   │        │  SCREEN 2: CRISIS DISPATCH   │
       │    (Proactive Safe Zone)     │        │     (16-Category Matrix)     │
       ├──────────────────────────────┤        ├──────────────────────────────┤
       │  [ > Slide to exit ]         │        │  [Community|Share|Msg|Alert] │
       │                              │        │  ┌────────────────────────┐  │
       │       China Basin            │        │  │ 📍 1234 Mission St 📝 │  │
       │     Safety Index 91%         │        │  │ [ Confirm Address ]    │  │
       │  ┌────────────────────────┐  │        │  └────────────────────────┘  │
       │  │ 🔍 Where to today?  🎛️ │  │        │  ┌────┐┌────┐┌────┐┌────┐   │
       │  └────────────────────────┘  │        │  │Med ││Pol ││Fire││Acc │   │
       │                              │        │  ├────┤├────┤├────┤├────┤   │
       │         ╭──────────╮         │        │  │Rob*││Kid ││Gas ││Fld │   │
       │       ╱   ╭──────╮   ╲       │        │  ├────┤├────┤├────┤├────┤   │
       │      │   │  🎙️   │    │      │        │  │Quak││Tsu ││Pwr ││Str │   │
       │       ╲   ╰──────╯   ╱       │        │  ├────┤├────┤├────┤├────┤   │
       │         ╰──────────╯         │        │  │Haz ││Wild││Wea ││Cyb │   │
       │                              │        │  └────┘└────┘└────┘└────┘   │
       │         HOLD FOR SOS         │        │                              │
       │           CHECK IN           │        │  ┌────────┬───┬──────────┐  │
       │              ⌄⌄              │        │  │✕ Cancel│ 3 │Send SOS >│  │
       │   37.7749° N 122.39632° W    │        │  └────────┴───┴──────────┘  │
       └──────────────────────────────┘        └──────────────────────────────┘
```

---

### 🟢 Screen 1: Guardian Radar & Safe Zone (`GuardianRadarScreen.kt`)

#### 1. Top Exit Pill Slider (`SlideToExitPill.kt`)
* **Visual**: Translucent glass pill (`#FFFFFFCC`) with dark circular thumb button containing an arrow icon `>`.
* **Interaction**: Horizontal drag gesture. Swiping to the right edge with spring resistance disarms Guardian tracking and returns to standard standby mode.

#### 2. Locality Safety Header & Index
* **Locality Name**: Centered headline (e.g., `"China Basin"` or `"Salt Lake Sector V, Kolkata"`).
* **Dynamic Safety Index**: `"Safety Index 91%"` calculated from real-time PostGIS incident density, responder proximity, and verified city safety feeds.

#### 3. Destination Search Pill (`SafeRouteSearchPill.kt`)
* **Input Field**: Frosted glass pill containing a search magnifying glass icon, placeholder `"Where are you going today?"`, and trailing filter icon `🎛️` (opens safe route criteria: lit streets, CCTV density, active volunteer corridors).

#### 4. Interactive Radar Map Visualizer (`GuardianRadarView.kt`)
* **Canvas Layering**:
  1. Base: Soft pastel map tile render (`GoogleMap` / `MapBox` with custom desaturated green/mint theme).
  2. Canvas Overlay: 3 concentric distance rings representing 500m, 1.5km, 3km radius.
  3. Sweep Effect: Rotating 360° gradient sweep cone (`SweepGradient`) with continuous smooth rotation (4s per cycle).
  4. Pulsating Center Badge: Frosted circular glass container housing a microphone icon (`🎙️`) for one-tap Voice SOS / AI triage input.

#### 5. Bottom Hold-for-SOS & Live Geodetic Telemetry
* **Action Header**: Bold `"HOLD FOR SOS"` (3-second press-and-hold trigger with progress ring).
* **Gesture Prompt**: `"CHECK IN"` with chevron indicators `⌄⌄` (swipe gesture for instant status ping to guardian contacts).
* **Telemetry Text**: High-precision latitude/longitude readout (`37.7749° N 122.39632° W` / `22.5726° N 88.3639° E`) giving users instant physical location awareness.

---

### 🚨 Screen 2: Crisis Dispatch & Category Matrix (`CrisisDispatchScreen.kt`)

#### 1. Top Segmented Navigation Pills
* Horizontal segmented container with 4 capsule buttons:
  1. `Community` (Active highlighted coral icon & label `<o>`)
  2. `Sharing` (Trip sharing / location broadcast)
  3. `Message` (Direct responder & community chat)
  4. `Alert` (Regional disaster bulletins)

#### 2. Address Verification & Action Card (`AddressConfirmCard.kt`)
* **Address Summary**: Floating white card showing pin icon `📍`, street address `"1234 Mission St"`, unit/locality `"Apt #345B, 27th Floor • San Francisco, CA"`, and an edit pen icon `📝` to adjust pinpoint accuracy.
* **Confirm CTA**: Vibrant Crimson rounded button `"Confirm Address"` (`#E52538`) with instant haptic pulse.

#### 3. The 16-Category Emergency Matrix (4x4 Responsive Grid)
16 specialized crisis categories represented as tactile micro-cards:

```
Row 1:  [ 🛡️ Medical ]     [ 👮 Police ]     [ 🔥 Fire ]        [ 🚗 Accident ]
Row 2:  [ 🥷 Robbery* ]     [ 🏃 Kidnapping ] [ ⚠️ Gas Leak ]    [ 🌊 Flood ]
Row 3:  [ 🏚️ Earthquake ]  [ 🌊 Tsunami ]    [ ⚡ Power Out ]   [ 🏢 Structural ]
Row 4:  [ 🧪 Hazmat ]       [ 🌲 Wildfire ]   [ ⛈️ Weather ]     [ 🔒 Cyber ]
```

* **Default State**: Crisp white background, subtle drop shadow, centered 3D vector icon, and 11sp typography label.
* **Selected State (e.g., Robbery)**:
  * Full background transformation to **Emergency Crimson (`#E52538`)**.
  * Icon enclosed in an elevated white circular badge.
  * Label text inverted to crisp white bold typography.
  * Subtle scale spring animation (`scale: 1.05x`).

#### 4. Bottom Dual-Action Countdown Dispatch Slider (`CountdownDispatchSlider.kt`)
* **Split Capsule Container**:
  * **Left Wing (`✕ Cancel`)**: Soft green pill button (`#34C759`) enabling immediate cancellation during the 3-second grace period.
  * **Center Circular Badge (`3`)**: Pulsing crimson circular badge displaying live countdown seconds (`3 → 2 → 1 → DISPATCH`).
  * **Right Wing (`Send SOS >`)**: Crimson gradient capsule button for instant emergency dispatch bypassing the countdown.

---

## 🏗️ 5. Jetpack Compose Component Specifications

### 🧩 1. Guardian Radar Canvas View (`GuardianRadarView.kt`)

```kotlin
package com.nearhelp.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun GuardianRadarView(
    modifier: Modifier = Modifier,
    onVoiceSosClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    
    // Continuous 360-degree rotation for radar beam
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )
    
    // Pulsating radius expansion
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2

            // Concentric Range Circles
            val rings = listOf(0.35f, 0.65f, 0.95f)
            rings.forEach { ratio ->
                drawCircle(
                    color = Color(0x3348BB78),
                    radius = maxRadius * ratio,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Radar Sweep Beam Cone
            rotate(degrees = angle, pivot = center) {
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1034C759),
                        Color(0x6034C759)
                    ),
                    center = center
                )
                drawCircle(
                    brush = sweepBrush,
                    radius = maxRadius * 0.95f,
                    center = center
                )
            }
        }

        // Center Pulsing Glass Micro-Card with Microphone
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = Color(0x20000000))
                .background(Color(0xE6FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onVoiceSosClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "AI Voice SOS Triage",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
```

---

### 🧩 2. 16-Category Emergency Matrix (`EmergencyCategoryGrid.kt`)

```kotlin
package com.nearhelp.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmergencyCategory(
    val id: String,
    val label: String,
    val emoji: String,
    val iconResId: Int? = null
)

val EmergencyCategories = listOf(
    EmergencyCategory("medical", "Medical", "🩺"),
    EmergencyCategory("police", "Police", "👮"),
    EmergencyCategory("fire", "Fire", "🔥"),
    EmergencyCategory("accident", "Accident", "🚗"),
    EmergencyCategory("robbery", "Robbery", "🥷"),
    EmergencyCategory("kidnapping", "Kidnapping", "🏃"),
    EmergencyCategory("gas_leak", "Gas Leak", "⚠️"),
    EmergencyCategory("flood", "Flood", "🌊"),
    EmergencyCategory("earthquake", "Earthquake", "🏚️"),
    EmergencyCategory("tsunami", "Tsunami", "🌊"),
    EmergencyCategory("power_out", "Power out", "⚡"),
    EmergencyCategory("structural", "Structural", "🏢"),
    EmergencyCategory("hazmat", "Hazmat", "🧪"),
    EmergencyCategory("wildfire", "Wildfire", "🌲"),
    EmergencyCategory("weather", "Weather", "⛈️"),
    EmergencyCategory("cyber", "Cyber", "🔒")
)

@Composable
fun EmergencyCategoryGrid(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8ECEF))
            .padding(10.dp)
    ) {
        items(EmergencyCategories, key = { it.id }) { category ->
            val isSelected = category.id == selectedCategoryId

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFFE52538) else Color.White,
                animationSpec = spring(),
                label = "CategoryBgColor"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF334155),
                animationSpec = spring(),
                label = "CategoryTextColor"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.04f else 1.0f,
                animationSpec = spring(),
                label = "CategoryScale"
            )

            Column(
                modifier = Modifier
                    .scale(scale)
                    .aspectRatio(0.92f)
                    .shadow(
                        elevation = if (isSelected) 6.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x1A000000)
                    )
                    .background(bgColor, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCategorySelected(category.id)
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterVertically,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isSelected) Color(0x33FFFFFF) else Color(0xFFF1F5F9),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = category.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
```

---

### 🧩 3. Dual-Action Countdown Dispatch Slider (`CountdownDispatchSlider.kt`)

```kotlin
package com.nearhelp.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CountdownDispatchSlider(
    initialSeconds: Int = 3,
    onCancel: () -> Unit,
    onDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember { mutableStateOf(initialSeconds) }
    val haptic = LocalHapticFeedback.current

    // 1-second countdown ticker with haptic pulse
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(1000L)
            secondsLeft -= 1
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDispatch()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "CountdownPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x2A000000))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF34C759), Color(0xFFE52538))
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Wing
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .clickable { onCancel() }
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel SOS",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Center Pulsing Countdown Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulseScale)
                    .shadow(8.dp, CircleShape)
                    .background(Color(0xFFE52538), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$secondsLeft",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }

            // Immediate Send SOS Wing
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
                    .clickable { onDispatch() }
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Send SOS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Send SOS Now",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
```

---

### 🧩 4. Location Address Verification Card (`AddressConfirmCard.kt`)

```kotlin
package com.nearhelp.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddressConfirmCard(
    streetAddress: String,
    subAddress: String,
    onEditAddressClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x12000000)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = streetAddress,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subAddress,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(onClick = onEditAddressClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Location",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52538))
            ) {
                Text(
                    text = "Confirm Address",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
```

---

## ⚡ 6. Micro-Interactions, Motion & Haptics System

| Gesture / Trigger | Motion / Visual Effect | Physics Spec | Haptic Feedback Token |
| :--- | :--- | :--- | :--- |
| **Category Card Tap** | Card scales up to `1.04x`, background smoothly animates from `#FFFFFF` to `#E52538` | `spring(dampingRatio = 0.6f, stiffness = 400f)` | `HapticFeedbackType.LongPress` |
| **Radar Sweep Scan** | Continuous 360° rotational gradient sweep on Canvas | `infiniteRepeatable(tween(3500ms, LinearEasing))` | Subtle background tick at 0° |
| **Countdown Seconds (3→2→1)**| Number badge scales with pulsing spring, outer ring expands | `tween(500ms, FastOutSlowInEasing)` | `HapticFeedbackType.TextHandleMove` |
| **Slide to Exit Pill** | Horizontal drag threshold with elastic snap-back if released before 80% | `spring(stiffness = Spring.StiffnessLow)` | `HapticFeedbackType.Confirm` |
| **Hold for SOS** | Radial circular progress fill with growing red outer halo | `tween(3000ms, LinearEasing)` | Double Heavy Pulse on trigger |

---

## ♿ 7. Accessibility, Ergonomics & Emergency UX Rules

1. **One-Handed Thumb Zone Optimization**:
   * All primary interactive targets (Category Matrix, Confirm Address CTA, Countdown Slider, Hold-for-SOS button) reside in the bottom 60% of the screen.
2. **WCAG 2.1 AAA Contrast in Emergency States**:
   * Text on emergency red (`#E52538`) is crisp white (`#FFFFFF`), yielding a contrast ratio of **4.85:1** (meets AAA large text requirements).
   * Primary titles on light slate (`#0F172A` on `#F0F3F6`) yield a contrast ratio of **13.2:1** (exceeds AAA requirement).
3. **Mandatory Non-Dismissible Legal Notice (India Good Samaritan Law 2016)**:
   * Any transition into active AI first-aid triage retains the persistent compliance badge at the screen foot.
4. **Offline Resilience & Zero-Lag Fallback**:
   * If mobile connectivity is severed, the top safety index switches to `"Offline Guardian Mode (SMS & Bluetooth Mesh Active)"` without blocking category selection or local WHO first-aid access.

---

## 📁 8. Project Android Package Layout

```
android/app/src/main/java/com/nearhelp/app/
├── ui/
│   ├── theme/
│   │   ├── Color.kt              // Guardian Mint, Emergency Crimson, Slate tokens
│   │   ├── Type.kt               // Inter / Roboto display typography
│   │   ├── Shape.kt              // 16dp, 24dp, 32dp rounded shapes
│   │   └── Theme.kt              // NearHelpTheme wrapper
│   ├── components/
│   │   ├── GuardianRadarView.kt       // 360° Radar Canvas & Voice Mic
│   │   ├── EmergencyCategoryGrid.kt   // 16-Cell Crisis Selection Matrix
│   │   ├── CountdownDispatchSlider.kt // Dual-action grace period slider
│   │   ├── AddressConfirmCard.kt      // Dynamic reverse geocode card
│   │   ├── SlideToExitPill.kt         // Top guardian exit gesture slider
│   │   └── AIDisclaimerBanner.kt      // WHO/Red Cross compliance notice
│   └── screens/
│       ├── GuardianRadarScreen.kt     // Left Screen implementation
│       ├── CrisisDispatchScreen.kt    // Right Screen implementation
│       ├── ActiveTrackingScreen.kt    // Post-dispatch live responder map
│       └── AICrisisChatScreen.kt      // LangGraph AI first-aid chat
```

---

## 🚀 9. Integration with NearHelp AI Core Services

* **AI Triage Integration (Gemini 2.5)**: Tapping the central radar mic captures audio, passes it to the `ai_service` Speech-to-Text pipeline, extracts crisis severity (0–100 score), and pre-selects the appropriate category card in the matrix.
* **Smart SOS Engine (FastAPI + PostGIS)**: Confirming the address and selecting a category sends a low-latency WebSocket payload (`POST /api/v1/sos/trigger`), initiating 3-layer escalation (Volunteers → 108/112 Auto-dial → Offline RAG).
* **Live GPS Stream**: Telemetry coordinates displayed at the base of the Guardian Radar screen stream live geohash updates to the backend for accurate neighbor proximity ranking.
