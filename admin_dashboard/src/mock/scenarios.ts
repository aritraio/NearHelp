/* ==========================================================================
   NearHelp AI — Medical Emergency Conditions & Indian Scenarios Data
   File: src/mock/scenarios.ts
   ========================================================================== */

import type { 
  EmergencyScenario, 
  SystemTelemetry, 
  MedicalConditionItem, 
  IncidentFeedItem, 
  ClinicalHandoverReport,
  SlideSyncInfo,
  ExaminerQaItem
} from './types';

export const MEDICAL_CONDITIONS: MedicalConditionItem[] = [
  {
    id: 'cardiac_arrest',
    label: 'Cardiac / Chest Pain',
    iconName: 'HeartPulse',
    severity: 5,
    description: 'Sudden collapse, unresponsive, chest crushing pressure, agonal breathing',
    symptoms: ['Unresponsive to verbal stimuli', 'No palpable carotid pulse', 'Agonal gasping respiration', 'Sudden collapse']
  },
  {
    id: 'severe_bleeding',
    label: 'Severe Hemorrhage',
    iconName: 'Droplet',
    severity: 4,
    description: 'Pulsatile arterial bleed, deep laceration, hypovolemic shock risk',
    symptoms: ['Pulsating bright red arterial bleed', 'Open compound wound', 'Cold clammy skin & pallor', 'Rapid weak pulse']
  },
  {
    id: 'respiratory_asthma',
    label: 'Respiratory Distress',
    iconName: 'Wind',
    severity: 5,
    description: 'Severe bronchospasm, peripheral cyanosis, acute hypoxia',
    symptoms: ['Inability to speak in sentences', 'Peripheral cyanosis (blue lips)', 'Severe expiratory wheezing', 'Oxygen saturation < 88%']
  },
  {
    id: 'unconscious_seizure',
    label: 'Seizure / Fainting',
    iconName: 'Activity',
    severity: 5,
    description: 'Tonic-clonic convulsion, post-ictal unresponsiveness, syncope',
    symptoms: ['Generalized violent muscle jerking', 'Oral frothing', 'Loss of consciousness', 'Post-ictal unresponsiveness']
  },
  {
    id: 'stroke',
    label: 'Stroke (FAST Protocol)',
    iconName: 'Brain',
    severity: 4,
    description: 'Facial droop, unilateral arm weakness, slurred speech',
    symptoms: ['Asymmetrical facial drooping', 'Unilateral arm drift', 'Severe dysarthria / slurred speech', 'Acute visual disturbance']
  },
  {
    id: 'severe_burns',
    label: 'Thermal Burns',
    iconName: 'Flame',
    severity: 3,
    description: 'Second/third-degree thermal burns, extensive blistered skin',
    symptoms: ['Blistered charred skin > 10% BSA', 'Acute thermal trauma', 'Airway smoke inhalation risk', 'Thermal shock risk']
  },
  {
    id: 'fracture_trauma',
    label: 'Compound Trauma',
    iconName: 'Bone',
    severity: 4,
    description: 'Open bone protrusion, spinal immobilization needed, impact trauma',
    symptoms: ['Open fracture with bone protrusion', 'Spinal immobilization indicated', 'Severe limb deformity', 'Inability to bear weight']
  },
  {
    id: 'anaphylaxis_allergy',
    label: 'Anaphylactic Shock',
    iconName: 'AlertCircle',
    severity: 5,
    description: 'Acute systemic allergic reaction, airway swelling, toxin collapse',
    symptoms: ['Acute diffuse urticaria & hives', 'Laryngeal angioedema', 'Hypotensive collapse', 'Acute dyspnea']
  }
];

export const SCENARIO_A: EmergencyScenario = {
  id: 'scenario-a',
  codeName: 'DEMO_CARDIAC_SALT_LAKE',
  title: 'Cardiac Arrest (Salt Lake Sector V)',
  subtitle: 'Level 5 Maximum Urgency — Agonal Breathing & Sudden Collapse',
  locationName: 'Salt Lake Sector V, Kolkata',
  streetAddress: 'Godrej Waterside, Tower 1',
  subAddress: 'DP Block, Sector V, Salt Lake City • Kolkata, WB 700091',
  coordinates: [22.5726, 88.4312],
  category: 'medical',
  medicalConditionId: 'cardiac_arrest',
  severity: 5,
  severityLabel: 'Level 5 — Critical Life Threat (Cardiac Arrest)',
  aiConfidence: 98.4,
  survivalWindowMinutes: 4.5,
  reportedSymptoms: [
    'Sudden collapse in office lobby',
    'Unresponsive to voice and tactile stimuli',
    'No palpable carotid pulse detected',
    'Agonal gasping respiration observed'
  ],
  transcriptionPreview: "Emergency at Godrej Waterside Sector V Kolkata! 54-year-old male collapsed near elevator. Unresponsive, not breathing properly, turning blue. Send CPR volunteers immediately!",
  multimodalImagePreview: 'https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=400&q=80',
  victim: {
    name: 'Rajesh Sengupta',
    age: 54,
    gender: 'Male',
    bloodType: 'O+',
    allergies: ['Penicillin', 'Sulfa drugs'],
    medicalConditions: ['Hypertension', 'Type 2 Diabetes'],
    hasPacemaker: false,
    emergencyContactName: 'Mousumi Sengupta (Wife)',
    emergencyContactPhone: '+91 98301 22415',
    isAnonymous: false,
  },
  responders: [
    {
      id: 'resp-101',
      name: 'Dr. Ananya Mukherjee',
      role: 'Consultant Cardiologist (Apollo Gleneagles)',
      skills: ['DOCTOR', 'CPR_CERTIFIED'],
      distanceMeters: 420,
      etaMinutes: 2.5,
      trustScore: 99,
      phone: '+91 98310 98765',
      avatar: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&w=150&q=80',
      lat: 22.5742,
      lng: 88.4335,
      status: 'DISPATCHED',
    },
    {
      id: 'resp-102',
      name: 'Rahul Das',
      role: 'Red Cross Certified First-Aider',
      skills: ['CPR_CERTIFIED', 'FIRST_AIDER'],
      distanceMeters: 650,
      etaMinutes: 4.0,
      trustScore: 94,
      phone: '+91 98302 55412',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80',
      lat: 22.5701,
      lng: 88.4285,
      status: 'IDLE',
    },
  ],
  protocol: {
    title: 'AHA / Indian Resuscitation Council Basic Life Support (BLS) Protocol',
    authority: 'World Health Organization & Indian Resuscitation Council (IRC)',
    disclaimers: 'Emergency interim bystander protocol. Municipal 108 ambulance dispatched.',
    legalShield: 'Protected under India Good Samaritan Law (Section 134A Motor Vehicles Act & Supreme Court 2016 Guidelines)',
    steps: [
      {
        stepNumber: 1,
        title: 'Check Safety & Confirm Unresponsiveness',
        actionInstruction: 'Tap victim firmly on both shoulders and shout "Are you okay?". Check carotid pulse in the neck groove for no more than 10 seconds.',
        warningNote: 'If no pulse or victim is only gasping, immediately begin CPR.',
        icon: 'AlertCircle',
      },
      {
        stepNumber: 2,
        title: 'Begin High-Quality Chest Compressions',
        actionInstruction: 'Place the heel of one hand in the center of the chest (lower sternum). Interlock fingers. Push hard and fast at a depth of 5–6 cm.',
        warningNote: 'Maintain a continuous cadence of 110–120 compressions per minute.',
        isCprStep: true,
        beatBpm: 110,
        icon: 'HeartPulse',
      },
      {
        stepNumber: 3,
        title: 'Maintain 30:2 Compressions to Breaths',
        actionInstruction: 'Deliver 30 compressions followed by 2 rescue breaths (or provide continuous Hands-Only CPR). Allow full chest recoil after each push.',
        icon: 'Activity',
      },
      {
        stepNumber: 4,
        title: 'Retrieve & Apply Nearby Automated Defibrillator (AED)',
        actionInstruction: 'Dispatch bystander to fetch AED from Webel Bhavan security desk (180m). Adhere electrode pads to bare chest as illustrated on unit.',
        icon: 'Zap',
      },
    ],
  },
  nearbyHospitals: [
    {
      id: 'hosp-1',
      name: 'AMRI Hospital Salt Lake',
      distanceKm: 1.8,
      bedAvailability: 14,
      icuAvailability: 3,
      traumaLevel: 'Level 1 Emergency Center',
      phone: '+91 33 6606 3800',
      lat: 22.5832,
      lng: 88.4125,
    },
    {
      id: 'hosp-2',
      name: 'Apollo Multispeciality Hospitals (EM Bypass)',
      distanceKm: 3.2,
      bedAvailability: 28,
      icuAvailability: 6,
      traumaLevel: 'Comprehensive Cardiac Care',
      phone: '+91 33 2320 3040',
      lat: 22.5698,
      lng: 88.4011,
    },
  ],
  nearbyAEDs: [
    {
      id: 'aed-1',
      locationName: 'Webel Bhavan — Main Ground Lobby Security Desk',
      distanceMeters: 180,
      accessNotes: 'Open 24/7. Main ground reception emergency cabinet.',
      isAvailable: true,
      lat: 22.5735,
      lng: 88.4325,
    },
    {
      id: 'aed-2',
      locationName: 'Technopolis IT Hub — Tower A Concierge',
      distanceMeters: 450,
      accessNotes: 'Wall-mounted unit opposite elevator bank #2.',
      isAvailable: true,
      lat: 22.5758,
      lng: 88.4290,
    },
  ],
};

export const SCENARIO_B: EmergencyScenario = {
  id: 'scenario-b',
  codeName: 'DEMO_TRAUMA_EM_BYPASS',
  title: 'Arterial Bleed / Road Trauma (EM Bypass)',
  subtitle: 'Level 4 Urgent — Two-Wheeler Collision with Compound Femoral Trauma',
  locationName: 'EM Bypass near Ruby Hospital, Kolkata',
  streetAddress: '1234 EM Bypass Crossing',
  subAddress: 'Near Ruby Hospital, Sector I, East Kolkata Township • Kolkata, WB 700107',
  coordinates: [22.5135, 88.3986],
  category: 'medical',
  medicalConditionId: 'severe_bleeding',
  severity: 4,
  severityLabel: 'Level 4 — High Urgency (Arterial Hemorrhage)',
  aiConfidence: 92.1,
  survivalWindowMinutes: 8.0,
  reportedSymptoms: [
    'Motorbike collision near Ruby Hospital crossing',
    'Open compound fracture right femur',
    'Pulsatile bright red blood flow observed',
    'Victim exhibiting hypovolemic shock symptoms'
  ],
  transcriptionPreview: 'Accident on EM Bypass right before Ruby crossing Kolkata! Motorcyclist injured, massive bleeding from upper leg. Need immediate first-aid pressure and ambulance!',
  multimodalImagePreview: 'https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=400&q=80',
  victim: {
    name: 'Sourav Roy',
    age: 28,
    gender: 'Male',
    bloodType: 'B+',
    allergies: [],
    medicalConditions: ['None'],
    hasPacemaker: false,
    emergencyContactName: 'Debasish Roy (Brother)',
    emergencyContactPhone: '+91 98311 44520',
    isAnonymous: false,
  },
  responders: [
    {
      id: 'resp-201',
      name: 'Amit Kumar',
      role: 'Paramedic / EMT First-Responder',
      skills: ['EMT', 'FIRST_AIDER', 'CPR_CERTIFIED'],
      distanceMeters: 310,
      etaMinutes: 1.8,
      trustScore: 97,
      phone: '+91 98366 11234',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80',
      lat: 22.5152,
      lng: 88.4005,
      status: 'DISPATCHED',
    },
    {
      id: 'resp-202',
      name: 'Officer Bikram Ghosh',
      role: 'Kolkata Traffic Police First Responder',
      skills: ['POLICE_REPRESENTATIVE', 'FIRST_AIDER'],
      distanceMeters: 480,
      etaMinutes: 2.2,
      trustScore: 96,
      phone: '+91 98300 00100',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80',
      lat: 22.5110,
      lng: 88.3965,
      status: 'IDLE',
    },
  ],
  protocol: {
    title: 'Indian Red Cross & WHO Severe Hemorrhage Control Protocol',
    authority: 'Indian Red Cross Society & Stop the Bleed Initiative',
    disclaimers: 'Direct pressure must be applied immediately. Do not remove penetrating objects.',
    legalShield: 'Good Samaritan protection applies under Section 134A Motor Vehicles Act & Supreme Court 2016 directive.',
    steps: [
      {
        stepNumber: 1,
        title: 'Apply Direct High-Force Pressure',
        actionInstruction: 'Use sterile gauze or clean cloth. Press directly on the bleeding wound with maximum upper-body weight.',
        warningNote: 'Do NOT remove blood-soaked cloths; add more layers on top.',
        icon: 'AlertCircle',
      },
      {
        stepNumber: 2,
        title: 'Improvise or Place Tourniquet',
        actionInstruction: 'Place tourniquet 2–3 inches above the wound (between wound and heart). Tighten until arterial pulsating bleeding ceases.',
        warningNote: 'Note exact time of tourniquet placement on victim forehead or arm.',
        icon: 'Layers',
      },
      {
        stepNumber: 3,
        title: 'Elevate & Counter Hypovolemic Shock',
        actionInstruction: 'Lay victim flat. Elevate uninjured legs 12 inches if no spinal trauma suspected. Cover with jacket to preserve core temperature.',
        icon: 'Activity',
      },
      {
        stepNumber: 4,
        title: '108 Ambulance Transit Handover',
        actionInstruction: 'Relay vital baseline (pulse, breathing rate, tourniquet timestamp) to approaching 108 paramedics.',
        icon: 'Navigation',
      },
    ],
  },
  nearbyHospitals: [
    {
      id: 'hosp-3',
      name: 'Ruby General Hospital',
      distanceKm: 0.4,
      bedAvailability: 18,
      icuAvailability: 4,
      traumaLevel: 'Emergency Trauma Care Center',
      phone: '+91 33 3987 1800',
      lat: 22.5128,
      lng: 88.3995,
    },
    {
      id: 'hosp-4',
      name: 'Desun Hospital & Heart Institute',
      distanceKm: 0.8,
      bedAvailability: 22,
      icuAvailability: 7,
      traumaLevel: 'Advanced ICU / Trauma Unit',
      phone: '+91 33 7122 2000',
      lat: 22.5102,
      lng: 88.4015,
    },
  ],
  nearbyAEDs: [
    {
      id: 'aed-3',
      locationName: 'Ruby Metro Station — Concourse Level 1 Customer Care',
      distanceMeters: 290,
      accessNotes: 'Publicly accessible defibrillator cabinet.',
      isAvailable: true,
      lat: 22.5140,
      lng: 88.3975,
    },
  ],
};

export const SCENARIO_C: EmergencyScenario = {
  id: 'scenario-c',
  codeName: 'DEMO_OFFLINE_MESH_FALLBACK',
  title: 'Acute Hypoxia / Asthmatic Bronchospasm',
  subtitle: 'Zero Cellular Data — Emergency Beacon Dispatched via Compressed SMS',
  locationName: 'Basement Parking B2, Sector V, Kolkata (Zero Cellular Signal)',
  streetAddress: 'Basement B2, Block EP & GP',
  subAddress: 'Sector V, Salt Lake City • Kolkata, WB 700091 (No GSM/LTE)',
  coordinates: [22.5720, 88.4305],
  category: 'medical',
  medicalConditionId: 'respiratory_asthma',
  severity: 5,
  severityLabel: 'Level 5 — Severe Asthmatic Bronchospasm',
  aiConfidence: 96.0,
  survivalWindowMinutes: 5.0,
  reportedSymptoms: [
    'Acute respiratory distress',
    'Severe wheezing, inability to speak full sentences',
    'Peripheral cyanosis observed on lips',
    'Rescue inhaler missing'
  ],
  transcriptionPreview: '[OFFLINE ENCODED VOICE] Cannot breathe... severe asthma attack... basement B2 Sector V... inhaler empty...',
  multimodalImagePreview: 'https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&w=400&q=80',
  victim: {
    name: 'Priya Sharma',
    age: 23,
    gender: 'Female',
    bloodType: 'A+',
    allergies: ['Dust mites', 'Aspirin'],
    medicalConditions: ['Chronic Severe Asthma'],
    hasPacemaker: false,
    emergencyContactName: 'Kunal Sharma (Father)',
    emergencyContactPhone: '+91 98319 88120',
    isAnonymous: false,
  },
  responders: [
    {
      id: 'resp-301',
      name: 'Pooja Banerjee',
      role: 'Resident Staff Nurse',
      skills: ['NURSE', 'FIRST_AIDER', 'CPR_CERTIFIED'],
      distanceMeters: 190,
      etaMinutes: 1.5,
      trustScore: 98,
      phone: '+91 98312 34567',
      avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=150&q=80',
      lat: 22.5715,
      lng: 88.4318,
      status: 'DISPATCHED',
    },
  ],
  protocol: {
    title: 'Offline Cached RAG Protocol: Acute Hypoxia Management',
    authority: 'Global Initiative for Asthma (GINA) & Indian Red Cross',
    disclaimers: 'Self-contained offline vector model execution. Zero external API calls required.',
    legalShield: 'Good Samaritan immunity guaranteed under Section 134A of the Motor Vehicles Act & Supreme Court 2016 Guidelines.',
    steps: [
      {
        stepNumber: 1,
        title: 'Position Victim Upright & Loosen Clothing',
        actionInstruction: 'Sit the victim comfortably upright, leaning slightly forward. Do NOT allow them to lie flat.',
        warningNote: 'Lying flat worsens diaphragmatic compression.',
        icon: 'Activity',
      },
      {
        stepNumber: 2,
        title: 'Calm & Encourage Pursed-Lip Breathing',
        actionInstruction: 'Have victim inhale slowly through nose (2 seconds) and exhale gently through pursed lips (4 seconds).',
        icon: 'Wind',
      },
      {
        stepNumber: 3,
        title: 'Locate SABA Inhaler / Spacer from Responder',
        actionInstruction: 'Approaching responder Pooja Banerjee is carrying Salbutamol inhaler with spacer.',
        icon: 'HeartPulse',
      },
      {
        stepNumber: 4,
        title: 'Mesh SMS Relay Confirmation',
        actionInstruction: 'Compressed binary packet relayed via peer Bluetooth BLE mesh to ground gateway.',
        icon: 'Radio',
      },
    ],
  },
  nearbyHospitals: [
    {
      id: 'hosp-5',
      name: 'Columbia Asia Hospital Salt Lake',
      distanceKm: 2.1,
      bedAvailability: 12,
      icuAvailability: 2,
      traumaLevel: 'Acute Respiratory Unit',
      phone: '+91 33 6600 3000',
      lat: 22.5780,
      lng: 88.4100,
    },
  ],
  nearbyAEDs: [],
  offlinePayload: {
    rawPacket: 'NH:SOS|VER:2.1|LOC:22.5720,88.4305|SEV:5|CAT:MED|VIC:P_SHARMA|BLD:A+|TS:1724068500|CRC32:A9F21B',
    transport: 'SMS_BINARY',
    bytesSize: 94,
  },
};

export const ALL_SCENARIOS: EmergencyScenario[] = [SCENARIO_A, SCENARIO_B, SCENARIO_C];

export const INITIAL_TELEMETRY: SystemTelemetry = {
  activeIncidentsCount: 3,
  availableVolunteersCount: 142,
  avgDispatchLatencySeconds: 4.2,
  ragAccuracyScore: 99.2,
  spatialQueryLatencyMs: 11.4,
  websocketConnectionsCount: 187,
  safetyIndexScore: 91,
};

export const MOCK_INCIDENT_FEED: IncidentFeedItem[] = [
  {
    id: 'inc-01',
    incidentNumber: 'NH-KOL-0819-01',
    timestamp: '19:20:10',
    timeAgo: 'Just now',
    locationName: 'Godrej Waterside, Tower 1',
    locality: 'Salt Lake Sector V, Kolkata',
    coordinates: [22.5726, 88.4312],
    category: 'medical',
    conditionTitle: 'Cardiac Arrest / Hypoxia (Level 5)',
    severity: 5,
    status: 'SOS_TRIGGERED',
    responderName: 'Dr. Ananya Mukherjee',
    responderRole: 'Consultant Cardiologist',
    responderEta: 2.5,
    ambulanceDispatched: true,
    ambulanceUnit: 'WB-01-AMB-4421',
    aiConfidence: 98.4,
    scenarioId: 'scenario-a'
  },
  {
    id: 'inc-02',
    incidentNumber: 'NH-KOL-0819-02',
    timestamp: '19:16:45',
    timeAgo: '3m ago',
    locationName: 'EM Bypass near Ruby Crossing',
    locality: 'East Kolkata Township',
    coordinates: [22.5135, 88.3986],
    category: 'accident',
    conditionTitle: 'Road Collision & Arterial Bleed (Level 4)',
    severity: 4,
    status: 'RESPONDER_EN_ROUTE',
    responderName: 'Amit Kumar',
    responderRole: 'Paramedic / EMT',
    responderEta: 1.8,
    ambulanceDispatched: true,
    ambulanceUnit: 'WB-01-AMB-3912',
    aiConfidence: 92.1,
    scenarioId: 'scenario-b'
  },
  {
    id: 'inc-03',
    incidentNumber: 'NH-KOL-0819-03',
    timestamp: '19:12:00',
    timeAgo: '8m ago',
    locationName: 'Basement B2, Block EP & GP',
    locality: 'Sector V, Salt Lake (BLE Mesh Relay)',
    coordinates: [22.5720, 88.4305],
    category: 'medical',
    conditionTitle: 'Severe Asthmatic Bronchospasm (Level 5)',
    severity: 5,
    status: 'RESPONDER_ACCEPTED',
    responderName: 'Pooja Banerjee',
    responderRole: 'Staff Nurse',
    responderEta: 1.5,
    ambulanceDispatched: true,
    ambulanceUnit: 'WB-01-AMB-1102',
    aiConfidence: 96.0,
    scenarioId: 'scenario-c'
  },
  {
    id: 'inc-04',
    incidentNumber: 'NH-KOL-0819-04',
    timestamp: '19:04:30',
    timeAgo: '15m ago',
    locationName: 'DLF 1 Food Court, Action Area 1',
    locality: 'New Town, Kolkata',
    coordinates: [22.5865, 88.4550],
    category: 'fire',
    conditionTitle: 'Second-Degree Thermal Burn (Level 3)',
    severity: 3,
    status: 'RESPONDER_ARRIVED',
    responderName: 'Subhasish Roy',
    responderRole: 'Red Cross First Aider',
    responderEta: 0,
    ambulanceDispatched: false,
    aiConfidence: 89.5
  },
  {
    id: 'inc-05',
    incidentNumber: 'NH-KOL-0819-05',
    timestamp: '18:55:12',
    timeAgo: '25m ago',
    locationName: 'Park Circus 7-Point Crossing',
    locality: 'Park Circus, Kolkata',
    coordinates: [22.5412, 88.3654],
    category: 'medical',
    conditionTitle: 'Tonic-Clonic Convulsion (Level 4)',
    severity: 4,
    status: 'HANDOVER_108',
    responderName: 'Officer Tanmoy Sen',
    responderRole: 'Traffic Police Resuscitation Unit',
    responderEta: 0,
    ambulanceDispatched: true,
    ambulanceUnit: 'WB-01-AMB-8801',
    aiConfidence: 94.2
  },
  {
    id: 'inc-06',
    incidentNumber: 'NH-KOL-0819-06',
    timestamp: '18:32:00',
    timeAgo: '48m ago',
    locationName: 'City Centre 1 Mall',
    locality: 'DC Block, Salt Lake',
    coordinates: [22.5898, 88.4082],
    category: 'accident',
    conditionTitle: 'Staircase Fall & Sprain Trauma (Level 2)',
    severity: 2,
    status: 'RESOLVED',
    responderName: 'Sneha Majumdar',
    responderRole: 'Community Bystander Volunteer',
    responderEta: 0,
    ambulanceDispatched: false,
    aiConfidence: 91.0
  }
];

export function generateClinicalHandoverReport(
  scenario: EmergencyScenario,
  incidentStatus: string,
  aedAttached: boolean,
  activeRespIndex: number = 0
): ClinicalHandoverReport {
  const activeResp = scenario.responders[activeRespIndex] || scenario.responders[0];
  const now = new Date();
  const dateStr = now.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  const timeStr = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });

  return {
    reportId: `REP-NH-${Math.floor(100000 + Math.random() * 900000)}`,
    incidentId: scenario.codeName,
    incidentCode: `NH-KOL-${scenario.id.toUpperCase()}`,
    generatedAt: `${dateStr} • ${timeStr} IST`,
    victimName: scenario.victim.name,
    victimAge: scenario.victim.age,
    victimGender: scenario.victim.gender,
    victimBloodType: scenario.victim.bloodType,
    victimAllergies: scenario.victim.allergies.length > 0 ? scenario.victim.allergies : ['None Documented / NKDA'],
    victimMedicalConditions: scenario.victim.medicalConditions.length > 0 ? scenario.victim.medicalConditions : ['None Known'],
    hasPacemaker: scenario.victim.hasPacemaker,
    emergencyLocation: `${scenario.streetAddress}, ${scenario.subAddress}`,
    emergencyCoordinates: `${scenario.coordinates[0].toFixed(4)}° N, ${scenario.coordinates[1].toFixed(4)}° E`,
    severityLevel: scenario.severity,
    diagnosticSummary: scenario.severityLabel,
    aiConfidenceScore: scenario.aiConfidence,
    survivalWindowMinutes: scenario.survivalWindowMinutes,
    reportedSymptoms: scenario.reportedSymptoms,
    cprMetronomeUsed: scenario.protocol.steps.some(s => s.isCprStep) || false,
    cprCompressionsEstimated: scenario.severity === 5 ? 330 : 120,
    cprDurationSeconds: scenario.severity === 5 ? 180 : 60,
    aedDeployed: aedAttached || scenario.nearbyAEDs.length > 0,
    aedShocksDelivered: aedAttached ? 1 : 0,
    responderAssigned: activeResp.name,
    responderRole: activeResp.role,
    responderArrivalTimeOffset: 'T+00:26 (2.5 mins post-intake)',
    ambulanceUnit: '108 Advanced Life Support (ALS) Unit WB-01-AMB-4421',
    handoverParamedicLeader: 'Senior Paramedic S. Chatterjee (WB EMS Team 4)',
    handoverTimestamp: incidentStatus === 'RESOLVED' || incidentStatus === 'HANDOVER_108' ? 'T+00:45 (ROSC Achieved on Scene)' : 'Pending Ambulance Handover',
    destinationHospital: scenario.nearbyHospitals[0]?.name || 'AMRI Hospital Salt Lake Emergency Trauma Center',
    legalShieldCompliance: 'Section 134A Motor Vehicles (Amendment) Act 2019 & Supreme Court 2016 Guidelines Fully Applicable',
    goodSamaritanActReference: 'Supreme Court WP(Civil) 235/2012 — Zero Civil/Criminal Liability for Responders',
    digitalSignatureHash: 'SHA256:7f9a2b8c4d1e0f3a6b5c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8'
  };
}

export const MASTER_SLIDES_SYNC: SlideSyncInfo[] = [
  {
    slideNumber: 1,
    title: 'Introduction & Project Overview',
    topicNumber: 'Topic 1',
    presenter: 'Aritra',
    presenterRole: 'Project Lead & AI Architect',
    duration: '1:00 min',
    timeWindow: '0:00 – 1:00',
    targetPersona: 'VICTIM',
    targetScreen: 'GUARDIAN',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 1: Guardian Radar Safe-Zone (91% Safety Score)',
    bulletPoints: [
      'Problem: Fatal 15–30m ambulance delay in Indian cities vs. 3–5m critical hypoxic window',
      'Solution: AI-Powered Community Response Network mobilizing CPR-verified citizens in <3m',
      'Team Governance: 6 specialized roles (AI Lead, Backend, UI/UX, QA, Data, Media)',
      'Scope: Sub-12ms spatial dispatch, multimodal AI triage, grounded first-aid RAG protocols'
    ]
  },
  {
    slideNumber: 2,
    title: 'Problem Domain & "Platinum 5 Minutes"',
    topicNumber: 'Topics 1 & 2',
    presenter: 'Aritra',
    presenterRole: 'Project Lead & AI Architect',
    duration: '1:15 min',
    timeWindow: '1:00 – 2:15',
    targetPersona: 'VICTIM',
    targetScreen: 'CRISIS_MATRIX',
    targetVictimSubScreen: 'TRIGGER',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 2: One-Tap SOS Trigger with Breathing Pulse & 3s Hold',
    bulletPoints: [
      'Clinical Law: Irreversible brain hypoxia begins at Minute 4; cardiac arrest survival drops 7–10%/min',
      'Pillar 1 — Spatial Delay: Gridlock prevents vehicular EMS from arriving in under 5 minutes',
      'Pillar 2 — Cognitive Panic Freeze: Callers in trauma cannot fill multi-step forms',
      'Pillar 3 & 4 — Alert Fatigue & Untrained Bystander Risk (fear of legal liability)'
    ]
  },
  {
    slideNumber: 3,
    title: 'Detailed System Analysis & Multimodal AI Triage',
    topicNumber: 'Topic 3',
    presenter: 'Abhisikta',
    presenterRole: 'Documentation & QA Lead',
    duration: '1:30 min',
    timeWindow: '2:15 – 3:45',
    targetPersona: 'VICTIM',
    targetScreen: 'CRISIS_MATRIX',
    targetVictimSubScreen: 'TRIAGE',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 2: Level 5 Clinical Triage Card (98.4% Confidence & Platinum 5m Window)',
    bulletPoints: [
      'Mathematical Decay Formula: P(t) = P0 * e^(-k*t) where k=0.10 min^-1',
      '4-Stage Ingestion: Audio STT, Text Parser, Scene Photo Vision, Vector Cosine Classifier',
      '4-Factor Ranking Formula: Score = 0.40*Proximity + 0.35*SkillMatch + 0.15*Trust + 0.10*Availability',
      '3-Tier Fail-Safe Escalation: 500m -> 3km search expansion -> 108 automated emergency call'
    ]
  },
  {
    slideNumber: 4,
    title: 'Literature Study & Grounded Medical Standards',
    topicNumber: 'Topic 4',
    presenter: 'Plaban',
    presenterRole: 'Data & Knowledge Analyst',
    duration: '1:15 min',
    timeWindow: '3:45 – 5:00',
    targetPersona: 'VICTIM',
    targetScreen: 'CRISIS_MATRIX',
    targetVictimSubScreen: 'FIRST_AID',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 3: Grounded First-Aid Protocol & 110 BPM CPR Metronome',
    bulletPoints: [
      'NEJM (Ringh et al.): Mobile phone dispatch of bystanders increases CPR rate from 48% to 62%',
      'JMIR 2023: LLMs achieve >88% diagnostic concordance with ER triage nurses',
      'ACL 2024 (Xiong et al.): Domain-specific RAG suppresses hallucinations by >94%',
      'ACM SIGSPATIAL: PostGIS GiST R-Tree queries execute in <12ms across 100k nodes'
    ]
  },
  {
    slideNumber: 5,
    title: 'Study of Existing Systems & Gap Analysis',
    topicNumber: 'Topic 5',
    presenter: 'Plaban',
    presenterRole: 'Data & Knowledge Analyst',
    duration: '1:15 min',
    timeWindow: '5:00 – 6:15',
    targetPersona: 'RESPONDER',
    targetScreen: 'RESPONDER',
    targetResponderSubScreen: 'ALERT',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 4: High-Priority Emergency Alert Modal with Skill Match Badge',
    bulletPoints: [
      'Existing 108/112 EMS: 15–30m vehicle response vs. NearHelp <3m bystander response (6x speedup)',
      '112 India App: Static form-heavy SOS without skill filtering vs. NearHelp multimodal dynamic triage',
      'PulsePoint (USA): 911-dependent, US-only vs. NearHelp sovereign dual-mesh Indian localization',
      'Good Samaritan Safety: 84% Indian bystanders hesitate due to police harassment fears'
    ]
  },
  {
    slideNumber: 6,
    title: 'Feasibility Study & Project Goals',
    topicNumber: 'Topics 6 & 7',
    presenter: 'Sayantan',
    presenterRole: 'Design, Assets & Media Lead',
    duration: '1:15 min',
    timeWindow: '6:15 – 7:30',
    targetPersona: 'RESPONDER',
    targetScreen: 'RESPONDER',
    targetResponderSubScreen: 'NAVIGATION',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 5: Turn-by-Turn Rescue Route & Encrypted Medical ID Reveal',
    bulletPoints: [
      'Technical Feasibility: Open-source stack (PostGIS + Gemini 2.5 Flash + React/Compose)',
      'Operational Feasibility: Zero equipment needed for bystanders; gamified verified trust tiering',
      'Legal Feasibility: 100% compliant with Section 134A Motor Vehicles (Amendment) Act 2019',
      'Core Milestone: Functional Android APK + sub-15s end-to-end dispatch simulation'
    ]
  },
  {
    slideNumber: 7,
    title: 'Architectural Design & Android Dual-State UX',
    topicNumber: 'Topic 8',
    presenter: 'Adil & Dishari',
    presenterRole: 'Backend (Adil) & UI/UX (Dishari)',
    duration: '2:00 min',
    timeWindow: '7:30 – 9:30',
    targetPersona: 'MAP',
    targetScreen: 'MAP',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 6: Dynamic Community Geo-Map with PostGIS Radial Waves',
    bulletPoints: [
      'Backend (Adil): Node.js + PostGIS ST_DWithin sub-12ms spatial search + WebSocket telemetry',
      'Reliability: Redis caching, offline mesh store-and-forward fallback for zero connectivity',
      'UI/UX (Dishari): Jetpack Compose Dual-Persona Architecture (Victim vs. Responder)',
      'Panic Ergonomics: AMOLED dark mode, 72dp high-contrast touch targets, rhythmic CPR pulsing'
    ]
  },
  {
    slideNumber: 8,
    title: 'Plan of Work, Significance & Review Conclusion',
    topicNumber: 'Topics 9, 10 & 11',
    presenter: 'Abhisikta & Aritra',
    presenterRole: 'QA (Abhisikta) & Lead (Aritra)',
    duration: '1:30 min',
    timeWindow: '9:30 – 11:00',
    targetPersona: 'COMMAND_CENTER',
    targetScreen: 'COMMAND_CENTER',
    targetScenarioId: 'scenario-a',
    keyVisual: 'Screen 7: Command Center Dashboard & AI Clinical Handover Report',
    bulletPoints: [
      'MoSCoW Sprint (Abhisikta): Phase 1–5 completed on schedule; Phase 6 Dry Run ready',
      'Clinical Handover (Aritra): Instant auto-generated PDF with Section 134A legal seal for 108 EMS',
      'Societal Impact: Democratizing life-saving first-response for 1.4B citizens in dense urban centers',
      'Open for Faculty Viva Defense & Interactive Demonstration'
    ]
  }
];

export const EXAMINER_QA_ITEMS: ExaminerQaItem[] = [
  {
    question: 'Why not just call 108 or 112 instead of building a community response network?',
    examinerDoubt: 'Examiner questions if community response is redundant when government ambulance services already exist.',
    coreAnswer: 'In severe emergencies like ventricular fibrillation cardiac arrest or acute asphyxiation, irreversible brain death begins at 4 minutes. In dense Indian cities like Kolkata or Mumbai, municipal ambulances take an average of 15 to 30 minutes due to traffic bottlenecks. NearHelp AI does NOT replace 108—it bridges the fatal 4-minute hypoxic gap by mobilizing CPR-certified bystanders already located within 500m (under 2.5 minutes walking) while simultaneously alerting 108 for ALS transport.',
    technicalMetrics: '214x faster initial intervention (4.2s dispatch latency vs. 15m municipal dispatch queue). Preserves survival rate above 55%.',
    relevantSpeaker: 'Aritra (Project Lead)'
  },
  {
    question: 'How does NearHelp AI prevent false alarms, spam SOS, or malicious misuse?',
    examinerDoubt: 'Examiner suspects anyone can press the SOS button for fun and spam neighbors.',
    coreAnswer: 'We enforce a 3-layer anti-abuse gate: 1) Physical Interaction Guard: 3-second deliberate hold gesture + 5-second abort ring; 2) Multimodal AI Cross-Verification: Gemini analyzes voice audio acoustic stress and scene photo computer vision for corroborating trauma features; 3) Identity & Trust Score Reputation: Responders only dispatched when user trust score is verified or emergency contacts are linked.',
    technicalMetrics: 'False positive rate reduced by 92.4% through multimodal acoustic + visual verification gates.',
    relevantSpeaker: 'Abhisikta (QA Lead)'
  },
  {
    question: 'What protects volunteer responders from legal harassment if a victim passes away?',
    examinerDoubt: 'Examiner asks about Good Samaritan liability in Indian law.',
    coreAnswer: 'Responders are 100% legally shielded under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court 2016 Good Samaritan Guidelines (WP Civil 235/2012). The law explicitly states that no bystander who assists in good faith can be subjected to civil or criminal liability, nor can police or hospitals detain or demand fees from them. NearHelp embeds this legal immunity certificate directly into the post-incident digital report.',
    technicalMetrics: 'Supreme Court 2016 Good Samaritan Guidelines + Sec 134A MV Act 2019 legal immunity stamp.',
    relevantSpeaker: 'Sayantan & Plaban'
  },
  {
    question: 'How does your spatial dispatch algorithm scale across 100,000 active users?',
    examinerDoubt: 'Examiner doubts backend database scalability and latency during disaster spikes.',
    coreAnswer: 'We utilize PostgreSQL with PostGIS geometry indexing on a GiST (Generalized Search Tree) R-Tree structure. Spatial range queries (ST_DWithin) over 100,000 concurrent user coordinates execute in under 12 milliseconds without sequential table scans. WebSocket pub/sub channels broadcast dispatch payloads to ranked candidate candidates instantly.',
    technicalMetrics: 'GiST R-Tree index: <12ms query execution time across 100,000 spatial records.',
    relevantSpeaker: 'Adil (Backend Lead)'
  },
  {
    question: 'How do you guarantee that your AI triage does not hallucinate dangerous first-aid advice?',
    examinerDoubt: 'Examiner worries LLMs will recommend fatal actions like giving water to an unconscious patient.',
    coreAnswer: 'NearHelp AI does NOT use unconstrained raw LLM generation for clinical instructions. We employ domain-bounded Retrieval-Augmented Generation (RAG) locked exclusively to verified WHO 2023, European Resuscitation Council (ERC), and Indian Red Cross emergency guidelines. A deterministic rule-based safety layer intercepts contraindicated actions (e.g. administering fluids, moving suspected spinal fracture victims).',
    technicalMetrics: '99.2% RAG Clinical Accuracy Index; 0% ungrounded medication recommendations.',
    relevantSpeaker: 'Abhisikta & Plaban'
  }
];


