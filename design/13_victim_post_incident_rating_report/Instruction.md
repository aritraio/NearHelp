# Screen Instruction: V7 — Post-Incident Rating & Safety Report Screen

> **Screen ID**: `V7_POST_INCIDENT_RATING`  
> **Persona**: Victim / Post-Incident Review  
> **UI Type**: Full Screen  
> **File/Composable Name**: `PostIncidentRatingScreen.kt`  
> **Route**: `/post_incident_rating`  

---

## 1. Purpose & UX Objective
Community trust and responder accountability are paramount in emergency networks. This screen allows victims to rate the responder who assisted them, award skill compliment tags, leave structured feedback, and—critically—flag any misconduct, safety violations, or abuse through an immediate report pipeline.

---

## 2. Visual Layout & Component Hierarchy

```
┌──────────────────────────────────────────────────┐
│  [<-]  Review Your Emergency Care                │
│                                                  │
│         [ Photo ] Dr. Rahul Sen (Doctor)         │
│           "How was the care you received?"       │
│                                                  │
│                 ★ ★ ★ ★ ★                        │
│               [ 5-STAR RATING ]                  │
│                                                  │
│  Compliment the responder (Tap to select):       │
│  [ ⚡ Rapid Arrival ]   [ 🩺 Life-Saving CPR ]   │
│  [ 🕊️ Calm & Reassuring ] [ 🏥 Smooth Handover ] │
│                                                  │
│  Additional Feedback (Optional):                 │
│  ┌────────────────────────────────────────────┐  │
│  │ "The doctor arrived in under 3 minutes and │  │
│  │  immediately started chest compressions..."│  │
│  └────────────────────────────────────────────┘  │
│                                                  │
│  ──────────────────────────────────────────────  │
│  🚩 Have a safety concern or misconduct?         │
│  [ Report Incident / Volunteer Misconduct ]      │
│  ──────────────────────────────────────────────  │
│                                                  │
│  [ SUBMIT FEEDBACK & RETURN HOME ]               │
└──────────────────────────────────────────────────┘
```

---

## 3. Component Details & Styling

### 3.1 Responder Showcase
* Large circular avatar with green verified MD badge.
* Name: `Dr. Rahul Sen`.
* Subtitle: *"Assisted during Cardiac Emergency at Park Street"*.

### 3.2 5-Star Interactive Rating Bar
* Interactive Star Row (5 stars, 36dp each).
* Color: Amber Yellow (`#FFB300`).
* Dynamic label below stars:
  * 5 Stars: *"Exceptional & Life-Saving"*
  * 4 Stars: *"Very Helpful"*
  * 3 Stars: *"Adequate"*
  * 1-2 Stars: *"Poor / Needed Improvement"*

### 3.3 Quick Compliment Badges
* Selectable Chip Flow:
  * `⚡ Rapid Arrival`
  * `🩺 Life-Saving First-Aid`
  * `🕊️ Calm & Professional`
  * `🏥 Clear EMS Coordination`
  * `🤝 Polite & Respectful`

### 3.4 Safety & Misconduct Escalation
* Container: Light red border (`#FFCDD2`), Text: `#C62828`.
* Icon: `Icons.Default.Flag`.
* Link: `Report Volunteer Misconduct, Harassment, or False Reporting`.
* Action: Opens structured dispute modal with immediate review flag by safety administrators.

### 3.5 Submit Action
* Button: `[ SUBMIT FEEDBACK & RETURN HOME ]` (Full width, 52dp height, Safe Green `#2E7D32`).
* Action: Submits payload to backend trust & reputation engine, returns to `V4_HOME_SOS`.
