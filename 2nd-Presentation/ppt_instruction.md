# NearHelp AI — PPT Instruction Guide

## Purpose of This Presentation

This is the **second academic project presentation** for **NearHelp AI**.  
The goal of this deck is to show that the project has moved beyond the proposal stage into a **working application**, while still maintaining an academic and research-oriented structure.

The presentation should focus primarily on the features that are currently functional.

### Important presentation rule

Do **not** present unimplemented features as completed.

For example:
- RAG-based medical knowledge retrieval is part of the larger project vision, but if it is not implemented yet, present it only under **Future Scope**.
- Advanced responder ranking, full real-time rescue coordination, mass disaster mode, analytics, and similar planned modules should also be presented only as future extensions unless they are demonstrably working.
- Google Maps integration, Gemini API chatbot functionality, working application screens, navigation, location-based functionality, and other completed features should be demonstrated clearly.

---

# Recommended 10-Slide Structure

1. NearHelp AI
2. Problem
3. Objectives
4. App Flow
5. Core Features
6. AI & Maps
7. Architecture
8. Demo & Results
9. Research
10. Future Scope

---

# Slide 1 — NearHelp AI

## Purpose

Introduce the project professionally and establish the project identity.

## Main content

Keep this slide minimal.

Include:

**NearHelp AI**

**AI-Powered Community Emergency Response Application**

Optional one-line tagline:

> Connecting people, location intelligence, and AI during emergencies.

Add:
- Team members
- Guide / faculty mentor name
- Department
- College / university
- Academic year

## Screenshot / Visual

Use either:

### Option A — Best choice
A clean screenshot of the **main application home screen** displayed inside a phone mockup.

### Option B
NearHelp AI logo in the center with a subtle emergency-map background.

Do not place multiple screenshots on this slide.

## Diagram

None.

## Research

None.

## Design guidance

Keep this slide visually clean.

Suggested layout:

- Left: Project title + subtitle
- Right: One phone mockup containing the home screen
- Bottom: Team / department details

---

# Slide 2 — Problem

## Purpose

Explain the real-world problem that motivated the project.

## Core message

Emergency response is often delayed because professional responders may not be physically close to the victim during the first critical minutes.

Nearby people may be available, but current systems generally do not provide an intelligent way to:

- identify the emergency quickly,
- guide the victim or bystander,
- use the victim's location effectively,
- connect nearby help,
- provide AI-assisted emergency information.

## Recommended slide content

Use 3–4 short points only:

- Critical help may be delayed during the first minutes of an emergency.
- Victims or bystanders may not know the correct immediate action.
- Existing emergency systems mainly depend on centralized professional response.
- Nearby assistance and intelligent digital guidance are often underutilized.

## Screenshot / Visual

Do not use app screenshots here.

Use one conceptual visual such as:

**Emergency occurs → Help requested → Response delay**

or a simple illustration showing:

Victim → Mobile App → Nearby Assistance / Emergency Service

## Diagram

A small problem-flow diagram:

```text
Emergency
    ↓
Victim / Bystander
    ↓
Uncertainty + Response Delay
    ↓
Need for Faster Digital Assistance
```

## Research

Keep detailed research for Slide 9.

You may add only one small factual statement or cited observation here if required by your faculty.

Do not overload this slide with statistics.

---

# Slide 3 — Objectives

## Purpose

Explain what the current version of NearHelp AI is designed to achieve.

## Recommended content

Use 4–5 objectives:

1. Develop a simple mobile interface for emergency assistance.
2. Integrate location and map-based emergency information.
3. Provide AI-assisted emergency interaction using Gemini.
4. Build a foundation for community-based emergency coordination.
5. Design the system so advanced features can be added later.

## Important distinction

Make the objectives realistic according to the current implementation.

Do not list features such as fully functional RAG, responder ranking, or live WebSocket rescue coordination as achieved objectives unless they are actually implemented.

## Screenshot / Visual

Use small icons beside each objective.

Examples:
- Phone icon
- Map pin
- AI / chatbot icon
- Emergency icon
- Expansion / architecture icon

## Diagram

None required.

A circular objective diagram can be used, but avoid complicated graphics.

## Research

None required.

---

# Slide 4 — App Flow

## Purpose

Show how a user interacts with NearHelp AI.

This should be one of the most important slides.

## Diagram to create

Create a **user-flow diagram**.

Recommended flow:

```text
Launch App
    ↓
Home Screen
    ↓
Choose Emergency Assistance
    ↓
Share / Detect Location
    ↓
View Nearby Emergency Resources
    ↓
Use AI Assistant
    ↓
Receive Guidance / Next Action
```

Modify this flow according to the exact navigation of your current application.

## Diagram style

Use:
- rounded rectangles for screens,
- arrows for navigation,
- small icons,
- 5–7 nodes maximum.

Do not create an overly technical UML diagram here.

## Screenshots

Use 3–4 small screenshots below or beside the flow.

Recommended screenshots:

1. Home screen
2. Emergency feature screen
3. Map screen
4. AI chatbot screen

## Screenshot rules

- Crop unnecessary status bars if possible.
- Use the same phone frame for every screenshot.
- Keep screenshots large enough to read.
- Do not use more than four screenshots.

## Research

None.

---

# Slide 5 — Core Features

## Purpose

Show the features that actually work in the current application.

## Recommended layout

Use a **2 × 2 or 2 × 3 feature grid**.

Possible feature cards:

### Emergency Interface
Quick access to emergency-related actions.

### Location Access
Uses the user's location for context-aware emergency assistance.

### Map Integration
Displays maps and relevant location information.

### AI Assistant
Gemini-powered conversational emergency assistant.

### Navigation
Smooth movement between major application screens.

### User-Friendly UI
Mobile interface designed for fast interaction during urgent situations.

Only include features that are actually functional.

## Screenshots

Use one representative screenshot for each major feature.

Best combination:
- Home / emergency screen
- Map screen
- Chatbot screen
- Any location / resource screen

## Diagram

None required.

## Research

None.

## What to avoid

Do not add:
- RAG
- Mass disaster mode
- Digital twin
- Full responder matching
- Reputation engine
- Analytics dashboard

unless those modules have been implemented.

---

# Slide 6 — AI & Maps

## Purpose

Explain the two strongest technical integrations in the current application.

Divide the slide into two halves.

---

## Left Side — Gemini AI

### Explain

- The application connects to the Gemini API.
- User queries are sent to the AI service.
- Gemini generates contextual responses.
- The response is shown inside the chatbot interface.

### Diagram

```text
User Message
    ↓
NearHelp App
    ↓
Gemini API
    ↓
AI Response
    ↓
Chat Interface
```

### Screenshot

Use:
- Chatbot screen
- A meaningful emergency-related question
- AI response visible on screen

Example test query:

> What should I do immediately if someone suddenly collapses?

Use only content you are comfortable demonstrating in class.

---

## Right Side — Google Maps

### Explain

- Google Maps SDK / map integration is used inside the application.
- The application can display the user's geographic context.
- Map functionality supports location-oriented emergency assistance.

### Diagram

```text
Device Location
    ↓
Location Permission
    ↓
Google Maps
    ↓
Map Display / Nearby Context
```

### Screenshot

Use:
- Actual map screen from the application
- User marker or location marker
- Relevant map UI visible

If nearby hospitals, police stations, fire stations, or resources are displayed in the current build, show them.

## Research

None required.

---

# Slide 7 — Architecture

## Purpose

Explain how the application components communicate.

This slide should look technical but remain understandable.

## Architecture diagram

Create a simple architecture diagram based on what is actually connected.

Recommended structure:

```text
                 ┌────────────────────┐
                 │   Android App      │
                 │ Kotlin / Compose   │
                 └─────────┬──────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ↓                ↓                ↓
   Google Maps        Gemini API       Backend / API
      SDK                                  Layer
                                             │
                                             ↓
                                      Database / Data
```

If the backend or database is not being used in the current demo, visually mark it as:

**Backend Foundation / Planned Integration**

rather than pretending that it is fully operational.

## Technology labels

Possible labels:

- Kotlin
- Jetpack Compose
- Google Maps SDK
- Gemini API
- FastAPI
- PostgreSQL / PostGIS
- Firebase / Authentication

Include only technologies actually used or clearly mark planned technologies.

## Screenshot

No screenshot is necessary.

Use the full slide for architecture.

## Research

None.

---

# Slide 8 — Demo & Results

## Purpose

Provide evidence that the application works.

This should not be just a gallery of screenshots.

Combine screenshots with a small testing table.

## Screenshots

Recommended screenshots:

1. App launch / home
2. Emergency screen
3. Working map
4. AI chatbot interaction

Arrange them as a visual sequence.

## Testing table

Create a small table:

| Test | Expected | Result |
|---|---|---|
| App Launch | App opens correctly | Passed |
| Screen Navigation | User can move between major screens | Passed |
| Maps | Map loads correctly | Passed |
| Location | Device location is detected | Passed |
| Gemini API | User query receives AI response | Passed |

Only mark something as Passed if you actually tested it.

## Optional metrics

If available, add simple values such as:

- Number of main screens completed
- Number of APIs integrated
- Number of test cases passed

Do not invent performance metrics.

## Diagram

No additional diagram needed.

## Research

None.

---

# Slide 9 — Research

## Purpose

Provide the academic research component of the presentation.

This slide should demonstrate that NearHelp AI is based on a real problem and that you studied existing approaches.

## Recommended research structure

Divide the slide into three sections:

### 1. Existing Approaches

Research examples such as:

- Government emergency response systems
- 112 / 108 emergency services
- Mobile SOS applications
- Location-sharing emergency applications
- AI-based health or emergency chat systems
- Community first-responder systems

Do not spend time listing dozens of applications.

Use approximately 3–5 relevant systems or papers.

---

### 2. Observed Limitations

Possible findings to investigate:

- Dependence on centralized emergency responders
- Response delay caused by physical distance
- Limited community responder coordination
- Lack of intelligent contextual guidance
- Fragmentation between location services and emergency assistance
- Limited integration of AI with local emergency support

These points should be supported by reliable sources.

---

### 3. Research Gap

Present the research gap as a concise statement:

> Existing solutions typically address emergency calling, location sharing, medical guidance, or responder coordination separately. NearHelp AI explores combining location intelligence, AI assistance, and community-oriented emergency support within a single mobile platform.

This is a research positioning statement, not a claim that every planned feature is already implemented.

## What research sources to use

Use academically credible and authoritative sources.

Preferred source types:

1. Peer-reviewed papers
2. WHO publications
3. Red Cross / Red Crescent resources
4. Government of India emergency-response documentation
5. NDMA publications
6. Research articles from IEEE / ACM / Springer / Elsevier
7. Official 112 India documentation
8. Relevant emergency-response studies

Avoid basing the research slide primarily on blogs.

## Minimum research target

Aim for:

- 3–5 research papers or authoritative reports
- 1–2 official emergency-service sources
- approximately 5–7 references overall for the project report

The slide itself should show only the most important 3–4 citations.

Full references can go in the report or an optional appendix slide.

## Recommended visual

Create a comparison table:

| System / Approach | SOS | Maps | AI Guidance | Community Support |
|---|---:|---:|---:|---:|
| Traditional Emergency Call | Yes | Limited | No | No |
| Location SOS App | Yes | Yes | No | Limited |
| AI Health Assistant | No | Limited | Yes | No |
| NearHelp AI Concept | Yes | Yes | Yes | Planned / Extensible |

Do not claim superiority unless supported by evidence.

The purpose is to highlight functional differences.

## Screenshot

Do not use your application screenshot here unless there is empty space.

Research charts, citations, and comparisons are more valuable.

---

# Slide 10 — Future Scope

## Purpose

Clearly separate the current prototype from the complete NearHelp AI vision.

This protects you during the viva because you are being transparent about implementation status.

## Recommended future features

### RAG-Based Emergency Knowledge
Use verified WHO / Red Cross / emergency-protocol documents to ground AI responses.

### Nearby Responder Matching
Identify and rank suitable nearby volunteers or trained responders.

### Real-Time Rescue Tracking
Use real-time location updates for emergency coordination.

### Multilingual Assistance
Support Bengali, Hindi, and English emergency interaction.

### Voice / Image Emergency Input
Allow users to describe emergencies through speech or images.

### Emergency Service Integration
Generate structured emergency summaries for professional responders.

## Important

Use the heading:

**Future Scope**

not:

**Remaining Features**

The first sounds academically planned; the second sounds like unfinished work.

## Diagram

Use a roadmap:

```text
Current Prototype
      ↓
RAG Knowledge
      ↓
Responder Network
      ↓
Real-Time Coordination
      ↓
Advanced Emergency Platform
```

## Screenshot

None necessary.

A roadmap visual is better.

## Research

You may briefly connect future work to findings from Slide 9.

---

# Screenshots You Should Collect Before Making the PPT

Create a dedicated folder containing the following screenshots.

## Essential screenshots

1. App home screen
2. Main emergency screen
3. Google Maps screen
4. Current-location screen
5. Gemini chatbot initial screen
6. Gemini chatbot with a complete conversation
7. Any nearby-resource screen
8. Navigation / feature menu
9. Any successful API-related screen
10. Final working application screen

## Screenshot quality rules

- Use the same Android device or emulator dimensions.
- Avoid mixed screen sizes.
- Remove debugging overlays.
- Avoid visible API keys.
- Avoid personal location information if unnecessary.
- Make sure text is readable.
- Use realistic demo data.
- Keep battery / notification clutter minimal.
- Use phone mockups consistently.

---

# Diagrams You Need to Prepare

Prepare these four diagrams.

## Diagram 1 — Problem Flow

Used on Slide 2.

```text
Emergency
→ Delay / Uncertainty
→ Need for Immediate Assistance
→ NearHelp AI
```

---

## Diagram 2 — User Flow

Used on Slide 4.

```text
Open App
→ Select Assistance
→ Location
→ Map / Emergency Context
→ AI Assistance
→ Action
```

---

## Diagram 3 — API Integration

Used on Slide 6.

```text
User
→ NearHelp App
→ Gemini API
→ Response
```

and

```text
Device Location
→ Google Maps
→ Map Context
```

---

## Diagram 4 — System Architecture

Used on Slide 7.

Show:

```text
Android Application
       │
 ┌─────┼─────────┐
 │     │         │
Maps  Gemini   Backend
                │
             Database
```

Keep the architecture diagram visually clean.

Do not show every planned microservice in this presentation.

---

# Research Work You Should Complete

Before creating Slide 9, research the following areas.

## Research Topic 1 — Emergency Response Time

Find studies or official documents explaining why the first few minutes of emergencies are critical.

Search terms:

- emergency response time survival
- cardiac arrest response time study
- emergency medical response delay India
- golden minutes emergency medicine

---

## Research Topic 2 — Community First Responders

Find studies evaluating nearby volunteers, trained citizens, first responders, or crowdsourced emergency assistance.

Search terms:

- community first responder emergency system
- smartphone alerted volunteers cardiac arrest
- crowdsourced emergency response
- nearby responder mobile application

---

## Research Topic 3 — Mobile Emergency Applications

Study existing emergency / SOS platforms.

Look for:

- location sharing,
- panic/SOS systems,
- emergency calling,
- emergency contact alerts,
- nearby medical facilities.

---

## Research Topic 4 — AI in Emergency Assistance

Search academic work about:

- conversational AI in emergencies,
- LLM medical guidance,
- AI triage,
- emergency decision support.

Be careful with medical-safety claims.

Do not claim that an LLM can diagnose emergencies.

---

## Research Topic 5 — Location Intelligence

Research how geographic information systems and smartphone location services can support emergency response.

Search terms:

- GIS emergency response
- location based emergency services
- geospatial emergency response mobile system
- nearest emergency facility GIS

---

# Academic References

Use a consistent citation style.

Recommended:

**IEEE citation style**

Example:

```text
[1] World Health Organization, “Title of Report,” Year.
[2] A. Author et al., “Paper Title,” IEEE Journal Name, vol. X, no. X, Year.
```

For the PPT, use small references at the bottom of the research slide.

For the final report, provide complete citations.

---

# Suggested Presentation Story

The overall story of the presentation should be:

```text
A real emergency-response problem exists
            ↓
NearHelp AI proposes a digital solution
            ↓
We developed a functional mobile prototype
            ↓
The application integrates maps and AI
            ↓
The architecture supports future expansion
            ↓
Testing demonstrates the implemented features
            ↓
Research identifies the wider opportunity
            ↓
Future work extends the prototype toward the complete vision
```

This narrative is stronger than presenting the application as a collection of disconnected screens.

---

# What You Should NOT Do

## Do not overcrowd slides

Maximum:
- 4–5 bullets
- 1 major diagram
- 3–4 screenshots

per slide.

## Do not use paragraphs

The presenter should explain details verbally.

The slide should provide visual support.

## Do not claim planned features are finished

Especially avoid presenting the following as currently operational unless they truly are:

- RAG medical guidance
- full volunteer verification
- AI severity scoring
- advanced responder ranking
- full WebSocket rescue coordination
- reputation engine
- mass-disaster coordination
- heatmap analytics
- production monitoring infrastructure

## Do not expose API keys

Never show Gemini API keys, Firebase credentials, tokens, or secrets in screenshots.

## Do not use AI-generated statistics

Every statistic used on the research slide must have a credible source.

---

# Final PPT Preparation Checklist

Before the presentation, verify:

- [ ] Project title is consistent everywhere.
- [ ] Current features and future features are clearly separated.
- [ ] Google Maps functionality can be demonstrated.
- [ ] Gemini chatbot can be demonstrated.
- [ ] Internet connection is available for API-dependent features.
- [ ] Backup screenshots are included in the PPT.
- [ ] No API credentials appear in screenshots.
- [ ] Architecture diagram matches the actual implementation.
- [ ] Research slide contains real citations.
- [ ] Testing results are based on actual tests.
- [ ] App screenshots use consistent dimensions.
- [ ] Demo data does not expose private information.
- [ ] Presentation can still continue if the live demo fails.

---

# Recommended Priority

If time is limited, prepare these items first:

1. Four clean application screenshots
2. User-flow diagram
3. Gemini + Maps integration diagram
4. Architecture diagram
5. Testing table
6. Research comparison table
7. Three to five credible research sources
8. Future-scope roadmap

These materials are enough to produce a strong second presentation for the NearHelp AI academic project.
