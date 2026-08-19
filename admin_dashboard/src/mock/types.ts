/* ==========================================================================
   NearHelp AI — Data Types & Contracts
   File: src/mock/types.ts
   ========================================================================== */

export type PersonaMode = 'VICTIM' | 'RESPONDER' | 'COMMAND_CENTER';

export type ViewLayout = 'MOBILE_FRAME' | 'SPLIT_SCREEN' | 'DESKTOP_FULL';

export type IncidentStatus = 
  | 'IDLE'
  | 'COUNTDOWN'
  | 'SOS_TRIGGERED'
  | 'AI_TRIAGING'
  | 'AI_TRIAGED'
  | 'SEARCHING_RESPONDERS'
  | 'RESPONDER_ACCEPTED'
  | 'RESPONDER_EN_ROUTE'
  | 'RESPONDER_ARRIVED'
  | 'HANDOVER_108'
  | 'RESOLVED';

export type SeverityLevel = 1 | 2 | 3 | 4 | 5;

export type CrisisCategory = 'MEDICAL' | 'FIRE' | 'CRIME' | 'ACCIDENT';

export type ResponderSkill = 
  | 'CPR_CERTIFIED' 
  | 'DOCTOR' 
  | 'NURSE' 
  | 'EMT' 
  | 'FIRST_AIDER' 
  | 'POLICE_REPRESENTATIVE';

export interface Responder {
  id: string;
  name: string;
  role: string;
  skills: ResponderSkill[];
  distanceMeters: number;
  etaMinutes: number;
  trustScore: number; // 0-100
  phone: string;
  avatar: string;
  lat: number;
  lng: number;
  status: 'IDLE' | 'DISPATCHED' | 'ACCEPTED' | 'ARRIVED';
}

export interface VictimProfile {
  name: string;
  age: number;
  gender: 'Male' | 'Female' | 'Other';
  bloodType: string;
  allergies: string[];
  medicalConditions: string[];
  hasPacemaker: boolean;
  emergencyContactName: string;
  emergencyContactPhone: string;
  isAnonymous: boolean;
}

export interface RAGProtocolStep {
  stepNumber: number;
  title: string;
  actionInstruction: string;
  warningNote?: string;
  isCprStep?: boolean;
  beatBpm?: number;
  icon?: string;
}

export interface HospitalNode {
  id: string;
  name: string;
  distanceKm: number;
  bedAvailability: number;
  icuAvailability: number;
  traumaLevel: string;
  phone: string;
  lat: number;
  lng: number;
}

export interface AEDNode {
  id: string;
  locationName: string;
  distanceMeters: number;
  accessNotes: string;
  isAvailable: boolean;
  lat: number;
  lng: number;
}

export interface EmergencyScenario {
  id: 'scenario-a' | 'scenario-b' | 'scenario-c';
  codeName: string;
  title: string;
  subtitle: string;
  locationName: string;
  coordinates: [number, number]; // [lat, lng]
  category: CrisisCategory;
  severity: SeverityLevel;
  severityLabel: string;
  aiConfidence: number; // e.g. 98.4
  survivalWindowMinutes: number;
  reportedSymptoms: string[];
  transcriptionPreview: string;
  multimodalImagePreview?: string;
  victim: VictimProfile;
  responders: Responder[];
  protocol: {
    title: string;
    authority: string;
    steps: RAGProtocolStep[];
    disclaimers: string;
    legalShield: string;
  };
  nearbyHospitals: HospitalNode[];
  nearbyAEDs: AEDNode[];
  offlinePayload?: {
    rawPacket: string;
    transport: 'BLE_MESH' | 'SMS_BINARY';
    bytesSize: number;
  };
}

export interface SystemTelemetry {
  activeIncidentsCount: number;
  availableVolunteersCount: number;
  avgDispatchLatencySeconds: number;
  ragAccuracyScore: number;
  spatialQueryLatencyMs: number;
  websocketConnectionsCount: number;
}
