/* ==========================================================================
   NearHelp AI — Central Demo State Store & Simulation Engine
   File: src/store/DemoContext.tsx (Medical Emergency Intake & Multimodal)
   ========================================================================== */

import React, { createContext, useContext, useState, useEffect, useRef, useCallback, useMemo } from 'react';
import type { 
  EmergencyScenario, 
  IncidentStatus, 
  PersonaMode, 
  ScreenMode,
  ViewLayout, 
  PresentationZoom,
  TourPaceMode,
  SystemTelemetry,
  MedicalConditionId,
  MultimodalInputMode,
  VictimSubScreen,
  ResponderSubScreen,
  CrisisCategory,
  BystanderChatMessage,
  IncidentChatMessage,
  TimelineEventItem,
  MapLayerKey,
  MapLayerFilters,
  SelectedMapEntity,
  IncidentFeedItem,
  ClinicalHandoverReport,
  SeverityLevel
} from '../mock/types';
import { 
  ALL_SCENARIOS, 
  SCENARIO_A, 
  INITIAL_TELEMETRY, 
  MEDICAL_CONDITIONS,
  MOCK_INCIDENT_FEED,
  MASTER_SLIDES_SYNC,
  generateClinicalHandoverReport
} from '../mock/scenarios';
import { soundEngine } from '../utils/audio';

const INITIAL_AI_CHAT_MESSAGES: BystanderChatMessage[] = [
  {
    id: 'msg-1',
    sender: 'gemini',
    text: 'NearHelp Gemini Medical Assistant online. What clinical guidance or first-aid clarification do you need while emergency responders are en-route?',
    timestamp: 'Just now',
    highlightText: 'Clinical Protocol Active'
  }
];

const INITIAL_INCIDENT_CHAT_MESSAGES: IncidentChatMessage[] = [
  {
    id: 'chat-1',
    sender: 'system',
    senderName: 'System Beacon',
    senderRole: 'NearHelp Dispatch Gateway',
    text: '🚨 Emergency Incident Created: Level 5 Cardiac Arrest at Godrej Waterside Tower 1. 108 Ambulance Unit WB-01-AMB-4421 dispatched.',
    timestamp: 'T+00:00',
    isMilestone: true,
    badgeColor: 'var(--color-emergency-red-bright)'
  },
  {
    id: 'chat-2',
    sender: 'victim',
    senderName: 'Mousumi S. (Bystander)',
    senderRole: 'On-Scene Bystander',
    text: 'মাটিতে পড়ে গেছেন, শ্বাস নিচ্ছেন না! খুব দ্রুত কেউ আসুন!',
    translatedText: 'Collapsed on the floor, not breathing! Please someone come fast!',
    originalLanguage: 'Bengali',
    timestamp: 'T+00:04'
  },
  {
    id: 'chat-3',
    sender: 'dispatcher_108',
    senderName: 'Control Room 108',
    senderRole: 'Municipal EMS Dispatcher',
    text: 'Ambulance dispatched from Salt Lake Sub-Divisional Hospital (ETA 7.5 mins). Dr. Ananya Mukherjee is responding locally (ETA 2.5 mins).',
    timestamp: 'T+00:09'
  }
];

interface DemoContextType {
  // Scenario & Screen Mode
  currentScenario: EmergencyScenario;
  screenMode: ScreenMode;
  personaMode: PersonaMode;
  viewLayout: ViewLayout;
  incidentStatus: IncidentStatus;
  selectedMedicalCondition: MedicalConditionId;

  // Phase 2 Victim Experience Sub-Views & Features
  victimSubScreen: VictimSubScreen;
  selectedCrisisCategory: CrisisCategory;
  anonymousEmergencyMode: boolean;
  activeRagStepIndex: number;
  completedRagSteps: number[];
  isAiChatDrawerOpen: boolean;
  aiChatMessages: BystanderChatMessage[];

  // Phase 3 Responder Experience Sub-Views & Features
  responderSubScreen: ResponderSubScreen;
  activeResponderIndex: number;
  aedAttached: boolean;
  responderDeclined: boolean;
  responderChatMessages: IncidentChatMessage[];
  timelineEvents: TimelineEventItem[];
  turnByTurnStepIndex: number;

  // Multimodal Medical Intake
  intakeInputMode: MultimodalInputMode;
  voiceTranscript: string;
  textInputNotes: string;
  isVoiceRecording: boolean;
  photoAttached: boolean;
  photoUrl: string | null;
  
  // Guardian & Locality details
  localityName: string;
  safetyIndexScore: number;
  streetAddress: string;
  subAddress: string;

  // Countdown & Dispatch State
  countdownSeconds: number;
  isCountingDown: boolean;
  
  // Simulation & Timing
  elapsedSeconds: number;
  searchRadiusKm: number;
  isAutoSimulating: boolean;
  simulationSpeed: number; // 1, 2, 5
  
  // Audio & Metronome
  audioMuted: boolean;
  cprMetronomeActive: boolean;
  cprBeatTick: number;
  
  // Telemetry
  telemetry: SystemTelemetry;
  offlineMeshActive: boolean;

  // Phase 4 Map & Command Center Features
  mapLayerFilters: MapLayerFilters;
  selectedMapEntity: SelectedMapEntity | null;
  incidentFeed: IncidentFeedItem[];
  incidentFilterSeverity: SeverityLevel | 'ALL';
  incidentFilterStatus: string | 'ALL';
  selectedIncidentId: string | null;
  isClinicalReportModalOpen: boolean;
  activeHandoverReport: ClinicalHandoverReport | null;
  
  // Phase 5 Projector Mode, Presentation Zoom & Slide HUD
  projectorMode: boolean;
  presentationZoom: PresentationZoom;
  isSlideSyncOpen: boolean;
  isEventDrawerOpen: boolean;
  quickNotification: string | null;

  // Phase 6 Automated Presentation Dry Run Tour
  isTourActive: boolean;
  tourSlideIndex: number;
  tourElapsedSeconds: number;
  tourPaceMode: TourPaceMode;
  isTourPaused: boolean;

  // Actions
  setScenario: (id: 'scenario-a' | 'scenario-b' | 'scenario-c') => void;
  setScreenMode: (mode: ScreenMode) => void;
  setPersonaMode: (mode: PersonaMode) => void;
  setViewLayout: (layout: ViewLayout) => void;
  setIncidentStatus: (status: IncidentStatus) => void;
  selectMedicalCondition: (conditionId: MedicalConditionId) => void;
  setVictimSubScreen: (subScreen: VictimSubScreen) => void;
  setSelectedCrisisCategory: (category: CrisisCategory) => void;
  toggleAnonymousEmergencyMode: () => void;
  toggleRagStep: (stepNumber: number) => void;
  setActiveRagStepIndex: (index: number) => void;
  setAiChatDrawerOpen: (open: boolean) => void;
  sendBystanderQuestion: (question: string) => void;
  resetRagChecklist: () => void;

  // Phase 3 Responder Actions
  setResponderSubScreen: (subScreen: ResponderSubScreen) => void;
  setActiveResponderIndex: (index: number) => void;
  toggleAedAttached: () => void;
  declineDispatch: () => void;
  sendResponderChatMessage: (text: string, senderRole?: 'responder' | 'victim' | 'dispatcher_108') => void;
  setTurnByTurnStepIndex: (index: number) => void;
  nextTurnByTurnStep: () => void;
  prevTurnByTurnStep: () => void;

  // Phase 4 Map & Command Center Actions
  toggleMapLayer: (layerKey: MapLayerKey) => void;
  resetMapLayers: () => void;
  setSelectedMapEntity: (entity: SelectedMapEntity | null) => void;
  setIncidentFilterSeverity: (sev: SeverityLevel | 'ALL') => void;
  setIncidentFilterStatus: (status: string | 'ALL') => void;
  setSelectedIncidentId: (id: string | null) => void;
  setIsClinicalReportModalOpen: (open: boolean) => void;
  openClinicalReport: () => void;
  trigger108Escalation: () => void;
  broadcastAlert: (message?: string) => void;

  // Phase 5 Presentation & Tuning Actions
  toggleProjectorMode: () => void;
  setPresentationZoom: (zoom: PresentationZoom) => void;
  setIsSlideSyncOpen: (open: boolean) => void;
  toggleSlideSync: () => void;
  setIsEventDrawerOpen: (open: boolean) => void;
  jumpToSlideView: (slideNumber: number) => void;
  triggerLifecycleEvent: (status: IncidentStatus) => void;
  showToast: (msg: string) => void;

  // Phase 6 Tour / Rehearsal Actions
  startTour: (pace?: TourPaceMode) => void;
  stopTour: () => void;
  toggleTourPause: () => void;
  nextTourSlide: () => void;
  prevTourSlide: () => void;
  setTourPaceMode: (pace: TourPaceMode) => void;

  setIntakeInputMode: (mode: MultimodalInputMode) => void;
  toggleVoiceRecording: () => void;
  setTextInputNotes: (text: string) => void;
  attachSamplePhoto: () => void;
  removePhoto: () => void;
  startCountdown: () => void;
  cancelCountdown: () => void;
  confirmAddress: () => void;
  triggerSos: () => void;
  cancelSos: () => void;
  advanceStep: () => void;
  resetDemo: () => void;
  toggleAutoSimulation: () => void;
  setSimulationSpeed: (speed: number) => void;
  toggleAudioMute: () => void;
  toggleCprMetronome: () => void;
  acceptDispatch: () => void;
  simulateArrival: () => void;
  handoverTo108: () => void;
  resolveEmergency: () => void;
}

const DemoContext = createContext<DemoContextType | undefined>(undefined);

export const DemoProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentScenario, setCurrentScenarioState] = useState<EmergencyScenario>(SCENARIO_A);
  const [screenMode, setScreenModeState] = useState<ScreenMode>('GUARDIAN');
  const [personaMode, setPersonaModeState] = useState<PersonaMode>('VICTIM');
  const [viewLayout, setViewLayout] = useState<ViewLayout>('SPLIT_SCREEN');
  const [incidentStatus, setIncidentStatusState] = useState<IncidentStatus>('IDLE');
  const [selectedMedicalCondition, setSelectedMedicalCondition] = useState<MedicalConditionId>('cardiac_arrest');

  // Phase 2 Victim Experience Sub-Views & Features
  const [victimSubScreen, setVictimSubScreenState] = useState<VictimSubScreen>('TRIGGER');
  const [selectedCrisisCategory, setSelectedCrisisCategoryState] = useState<CrisisCategory>('medical');
  const [anonymousEmergencyMode, setAnonymousEmergencyMode] = useState<boolean>(false);
  const [activeRagStepIndex, setActiveRagStepIndexState] = useState<number>(0);
  const [completedRagSteps, setCompletedRagSteps] = useState<number[]>([]);
  const [isAiChatDrawerOpen, setIsAiChatDrawerOpen] = useState<boolean>(false);
  const [aiChatMessages, setAiChatMessages] = useState<BystanderChatMessage[]>(INITIAL_AI_CHAT_MESSAGES);

  // Phase 3 Responder Experience Sub-Views & Features
  const [responderSubScreen, setResponderSubScreenState] = useState<ResponderSubScreen>('ALERT');
  const [activeResponderIndex, setActiveResponderIndexState] = useState<number>(0);
  const [aedAttached, setAedAttachedState] = useState<boolean>(false);
  const [responderDeclined, setResponderDeclined] = useState<boolean>(false);
  const [responderChatMessages, setResponderChatMessages] = useState<IncidentChatMessage[]>(INITIAL_INCIDENT_CHAT_MESSAGES);
  const [turnByTurnStepIndex, setTurnByTurnStepIndexState] = useState<number>(0);

  // Phase 4 Map & Command Center State
  const [mapLayerFilters, setMapLayerFilters] = useState<MapLayerFilters>({
    victim: true,
    responders: true,
    hospitals: true,
    aeds: true,
    postgis_wave: true,
    routes: true,
  });
  const [selectedMapEntity, setSelectedMapEntityState] = useState<SelectedMapEntity | null>(null);
  const [incidentFilterSeverity, setIncidentFilterSeverityState] = useState<SeverityLevel | 'ALL'>('ALL');
  const [incidentFilterStatus, setIncidentFilterStatusState] = useState<string | 'ALL'>('ALL');
  const [selectedIncidentId, setSelectedIncidentIdState] = useState<string | null>('inc-01');
  const [isClinicalReportModalOpen, setIsClinicalReportModalOpenState] = useState<boolean>(false);

  // Phase 5 Presentation & Projector Tuning state
  const [projectorMode, setProjectorMode] = useState<boolean>(false);
  const [presentationZoom, setPresentationZoomState] = useState<PresentationZoom>(100);
  const [isSlideSyncOpen, setIsSlideSyncOpenState] = useState<boolean>(false);
  const [isEventDrawerOpen, setIsEventDrawerOpenState] = useState<boolean>(false);
  const [quickNotification, setQuickNotification] = useState<string | null>(null);
  const toastTimerRef = useRef<number | null>(null);

  // Phase 6 Automated Presentation Dry Run Tour state
  const [isTourActive, setIsTourActiveState] = useState<boolean>(false);
  const [tourSlideIndex, setTourSlideIndexState] = useState<number>(0);
  const [tourElapsedSeconds, setTourElapsedSeconds] = useState<number>(0);
  const [tourPaceMode, setTourPaceModeState] = useState<TourPaceMode>('LIGHTNING_60S');
  const [isTourPaused, setIsTourPausedState] = useState<boolean>(false);
  const tourTimerRef = useRef<number | null>(null);

  // Multimodal Medical Intake state
  const [intakeInputMode, setIntakeInputModeState] = useState<MultimodalInputMode>('PRESETS');
  const [voiceTranscript, setVoiceTranscript] = useState<string>(
    "Emergency! 54-year-old male collapsed near elevator at Godrej Waterside. Unconscious, not breathing properly, turning blue..."
  );
  const [textInputNotes, setTextInputNotesState] = useState<string>('Sudden loss of consciousness, agonal gasping, no pulse detected');
  const [isVoiceRecording, setIsVoiceRecording] = useState<boolean>(false);
  const [photoAttached, setPhotoAttached] = useState<boolean>(false);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);

  const [localityName, setLocalityName] = useState<string>('Salt Lake Sector V, Kolkata');
  const [safetyIndexScore, setSafetyIndexScore] = useState<number>(91);
  const [streetAddress, setStreetAddress] = useState<string>('Godrej Waterside, Tower 1');
  const [subAddress, setSubAddress] = useState<string>('DP Block, Sector V, Salt Lake City • Kolkata, WB 700091');

  const [countdownSeconds, setCountdownSeconds] = useState<number>(3);
  const [isCountingDown, setIsCountingDown] = useState<boolean>(false);
  
  const [elapsedSeconds, setElapsedSeconds] = useState<number>(0);
  const [searchRadiusKm, setSearchRadiusKm] = useState<number>(0.5);
  const [isAutoSimulating, setIsAutoSimulating] = useState<boolean>(false);
  const [simulationSpeed, setSimulationSpeedState] = useState<number>(1);
  
  const [audioMuted, setAudioMuted] = useState<boolean>(false);
  const [cprMetronomeActive, setCprMetronomeActive] = useState<boolean>(false);
  const [cprBeatTick, setCprBeatTick] = useState<number>(0);
  
  const [telemetry, setTelemetry] = useState<SystemTelemetry>(INITIAL_TELEMETRY);
  const [offlineMeshActive, setOfflineMeshActive] = useState<boolean>(false);

  const timerRef = useRef<number | null>(null);
  const countdownTimerRef = useRef<number | null>(null);
  const recordingTimerRef = useRef<number | null>(null);

  // Toast Notification helper
  const showToast = useCallback((msg: string) => {
    if (toastTimerRef.current) {
      clearTimeout(toastTimerRef.current);
    }
    setQuickNotification(msg);
    toastTimerRef.current = window.setTimeout(() => {
      setQuickNotification(null);
    }, 2400);
  }, []);

  const toggleProjectorMode = useCallback(() => {
    soundEngine.playClick();
    setProjectorMode(prev => {
      const next = !prev;
      showToast(next ? '📽️ Projector High-Contrast Mode Activated' : '🌑 AMOLED Dark Mode Activated');
      return next;
    });
  }, [showToast]);

  const setPresentationZoom = useCallback((zoom: PresentationZoom) => {
    soundEngine.playClick();
    setPresentationZoomState(zoom);
    showToast(`🔍 Scale: ${zoom}%`);
  }, [showToast]);

  const toggleSlideSync = useCallback(() => {
    soundEngine.playClick();
    setIsSlideSyncOpenState(prev => !prev);
  }, []);

  const setIsSlideSyncOpen = useCallback((open: boolean) => {
    soundEngine.playClick();
    setIsSlideSyncOpenState(open);
  }, []);

  const setIsEventDrawerOpen = useCallback((open: boolean) => {
    soundEngine.playClick();
    setIsEventDrawerOpenState(open);
  }, []);

  // Sync mute with sound engine
  const toggleAudioMute = useCallback(() => {
    setAudioMuted(prev => {
      const next = !prev;
      soundEngine.setMuted(next);
      return next;
    });
  }, []);

  // CPR Metronome Toggle
  const toggleCprMetronome = useCallback(() => {
    setCprMetronomeActive(prev => {
      const next = !prev;
      if (next) {
        soundEngine.startCprMetronome(110, () => {
          setCprBeatTick(t => t + 1);
        });
      } else {
        soundEngine.stopCprMetronome();
      }
      return next;
    });
  }, []);

  // Scenario Selection
  const setScenario = useCallback((id: 'scenario-a' | 'scenario-b' | 'scenario-c') => {
    const selected = ALL_SCENARIOS.find(s => s.id === id) || SCENARIO_A;
    soundEngine.playClick();
    setCurrentScenarioState(selected);
    setSelectedMedicalCondition(selected.medicalConditionId);
    setStreetAddress(selected.streetAddress);
    setSubAddress(selected.subAddress);
    setLocalityName(selected.locationName.split(',')[0] || 'Salt Lake Sector V');
    setSafetyIndexScore(selected.id === 'scenario-a' ? 91 : selected.id === 'scenario-b' ? 84 : 72);
    setOfflineMeshActive(selected.id === 'scenario-c');
    setVoiceTranscript(selected.transcriptionPreview);
    setTextInputNotesState(selected.reportedSymptoms.join(' • '));
    setPhotoAttached(!!selected.multimodalImagePreview);
    setPhotoUrl(selected.multimodalImagePreview || null);
    
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('IDLE');
    setElapsedSeconds(0);
    setSearchRadiusKm(0.5);
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setIsVoiceRecording(false);
  }, []);

  // Screen Mode Switching
  const setScreenMode = useCallback((mode: ScreenMode) => {
    soundEngine.playClick();
    setScreenModeState(mode);
    if (mode === 'RESPONDER') {
      setPersonaModeState('RESPONDER');
    } else if (mode === 'GUARDIAN' || mode === 'CRISIS_MATRIX') {
      setPersonaModeState('VICTIM');
    } else if (mode === 'MAP') {
      setPersonaModeState('MAP');
    } else if (mode === 'COMMAND_CENTER') {
      setPersonaModeState('COMMAND_CENTER');
    }
  }, []);

  // Persona switching
  const setPersonaMode = useCallback((mode: PersonaMode) => {
    soundEngine.playClick();
    setPersonaModeState(mode);
    if (mode === 'VICTIM') {
      setScreenModeState('CRISIS_MATRIX');
    } else if (mode === 'RESPONDER') {
      setScreenModeState('RESPONDER');
    } else if (mode === 'MAP') {
      setScreenModeState('MAP');
    } else if (mode === 'COMMAND_CENTER') {
      setScreenModeState('COMMAND_CENTER');
    }
  }, []);

  // Phase 4 Map layer toggles
  const toggleMapLayer = useCallback((layerKey: MapLayerKey) => {
    soundEngine.playClick();
    setMapLayerFilters(prev => ({
      ...prev,
      [layerKey]: !prev[layerKey]
    }));
  }, []);

  const resetMapLayers = useCallback(() => {
    soundEngine.playClick();
    setMapLayerFilters({
      victim: true,
      responders: true,
      hospitals: true,
      aeds: true,
      postgis_wave: true,
      routes: true,
    });
  }, []);

  const setSelectedMapEntity = useCallback((entity: SelectedMapEntity | null) => {
    soundEngine.playClick();
    setSelectedMapEntityState(entity);
  }, []);

  const setIncidentFilterSeverity = useCallback((sev: SeverityLevel | 'ALL') => {
    soundEngine.playClick();
    setIncidentFilterSeverityState(sev);
  }, []);

  const setIncidentFilterStatus = useCallback((status: string | 'ALL') => {
    soundEngine.playClick();
    setIncidentFilterStatusState(status);
  }, []);

  const setSelectedIncidentId = useCallback((id: string | null) => {
    soundEngine.playClick();
    setSelectedIncidentIdState(id);
    const matchedItem = MOCK_INCIDENT_FEED.find(i => i.id === id);
    if (matchedItem && matchedItem.scenarioId) {
      setScenario(matchedItem.scenarioId);
    }
  }, [setScenario]);

  const activeHandoverReport: ClinicalHandoverReport = useMemo(() => {
    return generateClinicalHandoverReport(currentScenario, incidentStatus, aedAttached, activeResponderIndex);
  }, [currentScenario, incidentStatus, aedAttached, activeResponderIndex]);

  const openClinicalReport = useCallback(() => {
    soundEngine.playSuccessChime();
    setIsClinicalReportModalOpenState(true);
  }, []);

  const trigger108Escalation = useCallback(() => {
    soundEngine.playEmergencyAlert();
    setIncidentStatusState('HANDOVER_108');
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'dispatcher_108',
        senderName: '108 Paramedic Team Leader',
        senderRole: 'Ambulance WB-01-AMB-4421',
        text: `🚑 108 Advanced Life Support Unit on scene. Assuming patient care, ROSC achieved. Transferring to AMRI Hospital ICU.`,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-ai-cyan)'
      }
    ]);
  }, []);

  const broadcastAlert = useCallback((message?: string) => {
    soundEngine.playEmergencyAlert();
    const alertText = message || `🚨 High-Priority Spatial Beacon: ${currentScenario.severityLabel} at ${currentScenario.streetAddress}. All nearby responders alerted.`;
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'system',
        senderName: 'Command Dispatch',
        senderRole: 'Municipal Priority Broadcast',
        text: alertText,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-emergency-red-bright)'
      }
    ]);
  }, [currentScenario]);

  const incidentFeed: IncidentFeedItem[] = useMemo(() => {
    const activeResp = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
    const liveItem: IncidentFeedItem = {
      id: 'inc-01',
      incidentNumber: `NH-KOL-${currentScenario.id.toUpperCase()}-01`,
      timestamp: '19:20:10',
      timeAgo: `${elapsedSeconds}s ago`,
      locationName: currentScenario.streetAddress,
      locality: currentScenario.locationName,
      coordinates: currentScenario.coordinates,
      category: currentScenario.category,
      conditionTitle: `${currentScenario.protocol.title.split(' ')[0]} / ${currentScenario.severityLabel}`,
      severity: currentScenario.severity,
      status: incidentStatus,
      responderName: activeResp.name,
      responderRole: activeResp.role,
      responderEta: activeResp.etaMinutes,
      ambulanceDispatched: ['RESPONDER_ACCEPTED', 'RESPONDER_EN_ROUTE', 'RESPONDER_ARRIVED', 'HANDOVER_108', 'RESOLVED'].includes(incidentStatus),
      ambulanceUnit: 'WB-01-AMB-4421',
      aiConfidence: currentScenario.aiConfidence,
      scenarioId: currentScenario.id
    };

    return [liveItem, ...MOCK_INCIDENT_FEED.slice(1)];
  }, [currentScenario, incidentStatus, elapsedSeconds, activeResponderIndex]);

  // Phase 2 Victim Actions
  const setVictimSubScreen = useCallback((subScreen: VictimSubScreen) => {
    soundEngine.playClick();
    setVictimSubScreenState(subScreen);
  }, []);

  const setSelectedCrisisCategory = useCallback((category: CrisisCategory) => {
    soundEngine.playClick();
    setSelectedCrisisCategoryState(category);
  }, []);

  const toggleAnonymousEmergencyMode = useCallback(() => {
    soundEngine.playClick();
    setAnonymousEmergencyMode(prev => !prev);
  }, []);

  const toggleRagStep = useCallback((stepNumber: number) => {
    soundEngine.playClick();
    setCompletedRagSteps(prev => {
      if (prev.includes(stepNumber)) {
        return prev.filter(s => s !== stepNumber);
      } else {
        soundEngine.playSuccessChime();
        return [...prev, stepNumber];
      }
    });
  }, []);

  const setActiveRagStepIndex = useCallback((index: number) => {
    soundEngine.playClick();
    setActiveRagStepIndexState(index);
  }, []);

  const setAiChatDrawerOpen = useCallback((open: boolean) => {
    soundEngine.playClick();
    setIsAiChatDrawerOpen(open);
  }, []);

  const resetRagChecklist = useCallback(() => {
    soundEngine.playClick();
    setCompletedRagSteps([]);
    setActiveRagStepIndexState(0);
  }, []);

  const sendBystanderQuestion = useCallback((questionText: string) => {
    soundEngine.playClick();
    const userMsg: BystanderChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: questionText,
      timestamp: 'Just now'
    };

    let aiReplyText = "🤖 NearHelp AI Clinical Engine: Ensure the patient is on a flat, firm surface. Check carotid pulse for no more than 10 seconds. If no pulse or agonal breathing, immediately start CPR (30 compressions : 2 breaths).";
    let highlight = "Grounded Protocol Step";

    const qLower = questionText.toLowerCase();
    if (qLower.includes('water') || qLower.includes('liquid') || qLower.includes('drink') || qLower.includes('medicine') || qLower.includes('oral')) {
      aiReplyText = "❌ NO. NEVER administer water, fluids, or oral medications to an unconscious or gasping victim. Doing so can cause fatal airway obstruction and pulmonary aspiration.";
      highlight = "Contraindicated Action";
    } else if (qLower.includes('deep') || qLower.includes('compress') || qLower.includes('chest') || qLower.includes('fast') || qLower.includes('rate') || qLower.includes('bpm')) {
      aiReplyText = "✅ Compress 5 to 6 cm (approx 2 inches) deep at a cadence of 110–120 compressions/minute in the center of the lower sternum. Allow complete recoil between compressions.";
      highlight = "AHA / IRC Guideline (110 BPM)";
    } else if (qLower.includes('aed') || qLower.includes('defibrillator') || qLower.includes('shock') || qLower.includes('pad')) {
      aiReplyText = "⚡ Turn ON the AED immediately upon arrival. Follow voice prompts and adhere electrode pads to the bare chest (upper right / lower left). Stand clear during rhythm analysis and shock.";
      highlight = "Immediate AED Action";
    } else if (qLower.includes('rib') || qLower.includes('crack') || qLower.includes('pop') || qLower.includes('break')) {
      aiReplyText = "⚠️ Costochondral cartilage popping or rib cracking is common during effective adult CPR. DO NOT STOP compressions. Continue CPR immediately; restoring cerebral blood flow is the sole priority.";
      highlight = "Do Not Stop CPR";
    } else if (qLower.includes('legal') || qLower.includes('police') || qLower.includes('samaritan') || qLower.includes('liability') || qLower.includes('law')) {
      aiReplyText = "🛡️ You are 100% legally protected under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court 2016 Good Samaritan Guidelines. You cannot be detained, harassed, or held liable.";
      highlight = "Section 134A MV Act Shield";
    }

    const aiMsg: BystanderChatMessage = {
      id: `ai-${Date.now() + 1}`,
      sender: 'gemini',
      text: aiReplyText,
      timestamp: 'Just now',
      highlightText: highlight
    };

    setAiChatMessages(prev => [...prev, userMsg, aiMsg]);
  }, []);

  // Medical Condition Selection
  const selectMedicalCondition = useCallback((conditionId: MedicalConditionId) => {
    soundEngine.playClick();
    setSelectedMedicalCondition(conditionId);
    const conditionInfo = MEDICAL_CONDITIONS.find(c => c.id === conditionId);
    if (conditionInfo) {
      setCurrentScenarioState(prev => ({
        ...prev,
        medicalConditionId: conditionId,
        severity: conditionInfo.severity,
        severityLabel: `Level ${conditionInfo.severity} — ${conditionInfo.label}`,
        reportedSymptoms: conditionInfo.symptoms
      }));
      setTextInputNotesState(conditionInfo.symptoms.join(' • '));
    }
  }, []);

  // Multimodal Mode
  const setIntakeInputMode = useCallback((mode: MultimodalInputMode) => {
    soundEngine.playClick();
    setIntakeInputModeState(mode);
  }, []);

  // Voice Recording Toggle
  const toggleVoiceRecording = useCallback(() => {
    soundEngine.playClick();
    setIsVoiceRecording(prev => {
      const next = !prev;
      if (next) {
        soundEngine.playCountdownBeep(880);
        recordingTimerRef.current = window.setTimeout(() => {
          setIsVoiceRecording(false);
          soundEngine.playSuccessChime();
        }, 4000);
      } else {
        if (recordingTimerRef.current) {
          clearTimeout(recordingTimerRef.current);
          recordingTimerRef.current = null;
        }
      }
      return next;
    });
  }, []);

  const setTextInputNotes = useCallback((text: string) => {
    setTextInputNotesState(text);
  }, []);

  const attachSamplePhoto = useCallback(() => {
    soundEngine.playSuccessChime();
    setPhotoAttached(true);
    setPhotoUrl('https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=400&q=80');
  }, []);

  const removePhoto = useCallback(() => {
    soundEngine.playClick();
    setPhotoAttached(false);
    setPhotoUrl(null);
  }, []);

  // Trigger SOS Flow
  const triggerSos = useCallback(() => {
    soundEngine.playEmergencyAlert();
    setIsCountingDown(false);
    setIncidentStatusState('SOS_TRIGGERED');
    setVictimSubScreenState('TRIAGE');
    setElapsedSeconds(0);
    setIsAutoSimulating(true);
  }, []);

  // Countdown flow
  const startCountdown = useCallback(() => {
    soundEngine.playCountdownBeep(880);
    setIsCountingDown(true);
    setCountdownSeconds(3);
    setIncidentStatusState('COUNTDOWN');
  }, []);

  const cancelCountdown = useCallback(() => {
    soundEngine.playClick();
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setIncidentStatusState('IDLE');
  }, []);

  const confirmAddress = useCallback(() => {
    soundEngine.playSuccessChime();
    startCountdown();
  }, [startCountdown]);

  // Cancel SOS
  const cancelSos = useCallback(() => {
    soundEngine.playClick();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('IDLE');
    setVictimSubScreenState('TRIGGER');
    setElapsedSeconds(0);
    setIsAutoSimulating(false);
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setSearchRadiusKm(0.5);
  }, []);

  // Phase 3 Responder Actions
  const setResponderSubScreen = useCallback((subScreen: ResponderSubScreen) => {
    soundEngine.playClick();
    setResponderSubScreenState(subScreen);
  }, []);

  const setActiveResponderIndex = useCallback((index: number) => {
    soundEngine.playClick();
    setActiveResponderIndexState(index);
  }, []);

  const toggleAedAttached = useCallback(() => {
    soundEngine.playClick();
    setAedAttachedState(prev => {
      const next = !prev;
      if (next) {
        soundEngine.playSuccessChime();
        setResponderChatMessages(msgs => [
          ...msgs,
          {
            id: `chat-${Date.now()}`,
            sender: 'system',
            senderName: 'Defibrillator Unit',
            senderRole: 'AED Telemetry Gateway',
            text: '⚡ Automated External Defibrillator (AED) attached. Analyzing cardiac rhythm... Shock advised & delivered. Resume CPR compressions at 110 BPM.',
            timestamp: 'Just now',
            isMilestone: true,
            badgeColor: 'var(--color-action-amber-bright)'
          }
        ]);
      }
      return next;
    });
  }, []);

  const declineDispatch = useCallback(() => {
    soundEngine.playClick();
    setResponderDeclined(true);
    // Auto-switch to next available responder (Rahul Das)
    setActiveResponderIndexState(1);
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'system',
        senderName: 'Dispatch Engine',
        senderRole: 'PostGIS Spatial Router',
        text: '⚠️ Primary responder Dr. Ananya Mukherjee unavailable. Auto-rerouting to nearest CPR-verified responder: Rahul Das (650m away, ETA 4.0 mins).',
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-action-amber-bright)'
      }
    ]);
  }, []);

  const sendResponderChatMessage = useCallback((text: string, senderRole: 'responder' | 'victim' | 'dispatcher_108' = 'responder') => {
    soundEngine.playClick();
    const activeResp = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
    const newMsg: IncidentChatMessage = {
      id: `chat-${Date.now()}`,
      sender: senderRole,
      senderName: senderRole === 'responder' ? activeResp.name : senderRole === 'victim' ? 'Mousumi S. (Bystander)' : 'Control Room 108',
      senderRole: senderRole === 'responder' ? activeResp.role : senderRole === 'victim' ? 'On-Scene Bystander' : 'Municipal Dispatcher',
      text: text,
      timestamp: 'Just now'
    };
    setResponderChatMessages(prev => [...prev, newMsg]);
  }, [currentScenario, activeResponderIndex]);

  const setTurnByTurnStepIndex = useCallback((index: number) => {
    soundEngine.playClick();
    setTurnByTurnStepIndexState(index);
  }, []);

  const nextTurnByTurnStep = useCallback(() => {
    soundEngine.playClick();
    setTurnByTurnStepIndexState(prev => Math.min(prev + 1, 3));
  }, []);

  const prevTurnByTurnStep = useCallback(() => {
    soundEngine.playClick();
    setTurnByTurnStepIndexState(prev => Math.max(prev - 1, 0));
  }, []);

  // Responder Actions
  const acceptDispatch = useCallback(() => {
    soundEngine.playSuccessChime();
    setIncidentStatusState('RESPONDER_ACCEPTED');
    setResponderSubScreenState('NAVIGATION');
    const activeResp = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'responder',
        senderName: activeResp.name,
        senderRole: activeResp.role,
        text: `✅ Dispatch accepted. Navigating to ${currentScenario.streetAddress} (ETA ${activeResp.etaMinutes} mins). Initiating CPR kit preparation.`,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-safe-green-bright)'
      }
    ]);
  }, [currentScenario, activeResponderIndex]);

  const simulateArrival = useCallback(() => {
    soundEngine.playSuccessChime();
    setIncidentStatusState('RESPONDER_ARRIVED');
    const activeResp = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'responder',
        senderName: activeResp.name,
        senderRole: activeResp.role,
        text: `📍 Arrived on scene at ${currentScenario.streetAddress}. Commencing chest compressions & vital check.`,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-safe-green-bright)'
      }
    ]);
  }, [currentScenario, activeResponderIndex]);

  const handoverTo108 = useCallback(() => {
    soundEngine.playClick();
    setIncidentStatusState('HANDOVER_108');
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'dispatcher_108',
        senderName: '108 Paramedic Team Leader',
        senderRole: 'Ambulance WB-01-AMB-4421',
        text: `🚑 108 Advanced Life Support Unit on scene. Assuming patient care, ROSC achieved. Transferring to AMRI Hospital ICU.`,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-ai-cyan)'
      }
    ]);
  }, []);

  const resolveEmergency = useCallback(() => {
    soundEngine.playSuccessChime();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('RESOLVED');
    setIsAutoSimulating(false);
    setIsCountingDown(false);
    setResponderChatMessages(msgs => [
      ...msgs,
      {
        id: `chat-${Date.now()}`,
        sender: 'system',
        senderName: 'NearHelp Dispatch Gateway',
        senderRole: 'Clinical Audit Engine',
        text: `✨ Incident marked RESOLVED. Total bystander intervention time: 4m 18s. Handover certificate generated under Section 134A Good Samaritan Law.`,
        timestamp: 'Just now',
        isMilestone: true,
        badgeColor: 'var(--color-safe-green-bright)'
      }
    ]);
  }, []);

  // Reset entire demo cleanly
  const resetDemo = useCallback(() => {
    soundEngine.playClick();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('IDLE');
    setVictimSubScreenState('TRIGGER');
    setSelectedCrisisCategoryState('medical');
    setAnonymousEmergencyMode(false);
    setCompletedRagSteps([]);
    setActiveRagStepIndexState(0);
    setIsAiChatDrawerOpen(false);
    setAiChatMessages(INITIAL_AI_CHAT_MESSAGES);
    setResponderSubScreenState('ALERT');
    setActiveResponderIndexState(0);
    setAedAttachedState(false);
    setResponderDeclined(false);
    setResponderChatMessages(INITIAL_INCIDENT_CHAT_MESSAGES);
    setTurnByTurnStepIndexState(0);
    setElapsedSeconds(0);
    setSearchRadiusKm(0.5);
    setIsAutoSimulating(false);
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setPersonaModeState('VICTIM');
    setScreenModeState('GUARDIAN');
    setIsVoiceRecording(false);
    showToast('🔄 Demo Reset to Clean Standby State');
  }, [showToast]);

  // Jump directly to view associated with an 8-slide deck slide
  const jumpToSlideView = useCallback((slideNumber: number) => {
    const slide = MASTER_SLIDES_SYNC.find(s => s.slideNumber === slideNumber);
    if (!slide) return;
    soundEngine.playSuccessChime();

    setScenario(slide.targetScenarioId);
    setPersonaModeState(slide.targetPersona);
    setScreenModeState(slide.targetScreen);

    if (slide.targetVictimSubScreen) {
      setVictimSubScreenState(slide.targetVictimSubScreen);
    }
    if (slide.targetResponderSubScreen) {
      setResponderSubScreenState(slide.targetResponderSubScreen);
    }

    if (slideNumber === 1 || slideNumber === 2) {
      setIncidentStatusState('IDLE');
    } else if (slideNumber === 3) {
      setIncidentStatusState('AI_TRIAGED');
    } else if (slideNumber === 4 || slideNumber === 5) {
      setIncidentStatusState('SEARCHING_RESPONDERS');
    } else if (slideNumber === 6) {
      setIncidentStatusState('RESPONDER_ACCEPTED');
    } else if (slideNumber === 7) {
      setIncidentStatusState('RESPONDER_EN_ROUTE');
    } else if (slideNumber === 8) {
      setIncidentStatusState('RESOLVED');
    }

    showToast(`🎯 Slide ${slideNumber}: ${slide.title} (${slide.presenter})`);
  }, [setScenario, showToast]);

  // Jump to specific lifecycle milestone
  const triggerLifecycleEvent = useCallback((status: IncidentStatus) => {
    if (status === 'SOS_TRIGGERED') {
      soundEngine.playEmergencyAlert();
      setIsCountingDown(false);
      setVictimSubScreenState('TRIAGE');
    } else if (status === 'AI_TRIAGING' || status === 'AI_TRIAGED') {
      soundEngine.playClick();
      setVictimSubScreenState('TRIAGE');
    } else if (status === 'SEARCHING_RESPONDERS') {
      soundEngine.playClick();
      setSearchRadiusKm(2.0);
    } else if (status === 'RESPONDER_ACCEPTED') {
      soundEngine.playSuccessChime();
      setResponderSubScreenState('NAVIGATION');
    } else if (status === 'RESPONDER_ARRIVED') {
      soundEngine.playSuccessChime();
      setResponderSubScreenState('NAVIGATION');
    } else if (status === 'HANDOVER_108') {
      soundEngine.playClick();
    } else if (status === 'RESOLVED') {
      soundEngine.playSuccessChime();
      soundEngine.stopCprMetronome();
      setCprMetronomeActive(false);
    } else {
      soundEngine.playClick();
    }

    setIncidentStatusState(status);
    showToast(`⚡ Jumped to: ${status.replace(/_/g, ' ')}`);
  }, [showToast]);

  // Phase 6 Tour helper to get slide duration in seconds
  const getSlideDuration = useCallback((slideIdx: number, pace: TourPaceMode) => {
    if (pace === 'LIGHTNING_60S') return 8;
    if (pace === 'EXPRESS_3M') return 22;
    const durations = [60, 75, 90, 75, 75, 75, 120, 90];
    return durations[slideIdx] || 75;
  }, []);

  // Start Tour
  const startTour = useCallback((pace: TourPaceMode = 'LIGHTNING_60S') => {
    soundEngine.playSuccessChime();
    setTourPaceModeState(pace);
    setIsTourActiveState(true);
    setIsTourPausedState(false);
    setTourSlideIndexState(0);
    setTourElapsedSeconds(0);
    jumpToSlideView(1);
    showToast(`🎬 Review Tour Started (${pace === 'LIGHTNING_60S' ? '60s Lightning' : pace === 'EXPRESS_3M' ? '3m Express' : '11m Full Review'})`);
  }, [jumpToSlideView, showToast]);

  const stopTour = useCallback(() => {
    soundEngine.playClick();
    setIsTourActiveState(false);
    setIsTourPausedState(false);
    setTourSlideIndexState(0);
    setTourElapsedSeconds(0);
    showToast('⏹️ Review Tour Stopped');
  }, [showToast]);

  const toggleTourPause = useCallback(() => {
    soundEngine.playClick();
    setIsTourPausedState(prev => {
      const next = !prev;
      showToast(next ? '⏸️ Tour Paused' : '▶️ Tour Resumed');
      return next;
    });
  }, [showToast]);

  const nextTourSlide = useCallback(() => {
    soundEngine.playClick();
    setTourSlideIndexState(curr => {
      const nextIdx = Math.min(curr + 1, MASTER_SLIDES_SYNC.length - 1);
      setTourElapsedSeconds(0);
      jumpToSlideView(nextIdx + 1);
      return nextIdx;
    });
  }, [jumpToSlideView]);

  const prevTourSlide = useCallback(() => {
    soundEngine.playClick();
    setTourSlideIndexState(curr => {
      const prevIdx = Math.max(curr - 1, 0);
      setTourElapsedSeconds(0);
      jumpToSlideView(prevIdx + 1);
      return prevIdx;
    });
  }, [jumpToSlideView]);

  const setTourPaceMode = useCallback((pace: TourPaceMode) => {
    soundEngine.playClick();
    setTourPaceModeState(pace);
    setTourElapsedSeconds(0);
    showToast(`⏱️ Tour Pace: ${pace}`);
  }, [showToast]);

  // Milestone Events memo
  const timelineEvents: TimelineEventItem[] = useMemo(() => {
    const activeResp = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
    return [
      {
        id: 'tl-1',
        timestampOffset: 'T+00:00',
        timeIso: '19:20:10',
        title: 'SOS Beacon Dispatched',
        description: `Emergency intake generated at ${currentScenario.streetAddress}. Multimodal audio transcript & GPS lock acquired.`,
        badgeType: 'SOS',
        author: 'NearHelp Android Client',
        isComplete: incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN'
      },
      {
        id: 'tl-2',
        timestampOffset: 'T+00:03',
        timeIso: '19:20:13',
        title: `AI Clinical Triage: ${currentScenario.severityLabel}`,
        description: `Gemini Clinical Model identified ${currentScenario.reportedSymptoms[0] || 'critical symptoms'}. Survival window: ${currentScenario.survivalWindowMinutes} mins.`,
        badgeType: 'AI_TRIAGE',
        author: 'Gemini 1.5 Flash Triage Engine',
        isComplete: ['AI_TRIAGED', 'SEARCHING_RESPONDERS', 'RESPONDER_ACCEPTED', 'RESPONDER_EN_ROUTE', 'RESPONDER_ARRIVED', 'HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-3',
        timestampOffset: 'T+00:08',
        timeIso: '19:20:18',
        title: 'PostGIS Spatial Query Executed',
        description: `High-priority dispatch beacon transmitted to CPR-verified volunteers within ${searchRadiusKm.toFixed(1)}km radius.`,
        badgeType: 'DISPATCH',
        author: 'Spatial Dispatch Engine',
        isComplete: ['SEARCHING_RESPONDERS', 'RESPONDER_ACCEPTED', 'RESPONDER_EN_ROUTE', 'RESPONDER_ARRIVED', 'HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-4',
        timestampOffset: 'T+00:12',
        timeIso: '19:20:22',
        title: `${activeResp.name} Accepted Dispatch`,
        description: `Turn-by-turn rescue navigation active. ETA ${activeResp.etaMinutes} mins (${activeResp.distanceMeters}m). Encrypted Medical ID unlocked.`,
        badgeType: 'ACCEPTED',
        author: activeResp.name,
        isComplete: ['RESPONDER_ACCEPTED', 'RESPONDER_EN_ROUTE', 'RESPONDER_ARRIVED', 'HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-5',
        timestampOffset: 'T+00:26',
        timeIso: '19:20:36',
        title: 'Responder Arrived On-Scene',
        description: `${activeResp.name} arrived at ${currentScenario.streetAddress}. Commenced active BLS emergency protocol.`,
        badgeType: 'ARRIVAL',
        author: 'GPS Geofence Trigger',
        isComplete: ['RESPONDER_ARRIVED', 'HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-6',
        timestampOffset: 'T+00:34',
        timeIso: '19:20:44',
        title: 'Automated External Defibrillator (AED) Deployed',
        description: aedAttached 
          ? 'AED electrode pads attached from Webel Bhavan security desk. Rhythm analyzed: shock advised and delivered.'
          : 'Nearby AED localized at Webel Bhavan Security Desk (180m).',
        badgeType: 'AED',
        author: 'Community AED Mesh',
        isComplete: aedAttached || ['HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-7',
        timestampOffset: 'T+00:45',
        timeIso: '19:20:55',
        title: 'Handover to 108 Emergency Paramedics',
        description: `Incident transferred to Ambulance Unit WB-01-AMB-4421. Patient vital signs stabilized for transport.`,
        badgeType: 'AMBULANCE',
        author: '108 Paramedic Team Leader',
        isComplete: ['HANDOVER_108', 'RESOLVED'].includes(incidentStatus)
      },
      {
        id: 'tl-8',
        timestampOffset: 'T+01:10',
        timeIso: '19:21:20',
        title: 'Rescue Incident Successfully Resolved',
        description: 'Post-incident clinical handover PDF generated. Volunteer Good Samaritan legal immunity log archived.',
        badgeType: 'RESOLVED',
        author: 'System Auto-Audit',
        isComplete: incidentStatus === 'RESOLVED'
      }
    ];
  }, [currentScenario, activeResponderIndex, incidentStatus, searchRadiusKm, aedAttached]);

  // Step advancement
  const advanceStep = useCallback(() => {
    soundEngine.playClick();
    setIncidentStatusState(current => {
      switch (current) {
        case 'IDLE':
          soundEngine.playEmergencyAlert();
          return 'SOS_TRIGGERED';
        case 'COUNTDOWN':
          soundEngine.playEmergencyAlert();
          return 'SOS_TRIGGERED';
        case 'SOS_TRIGGERED':
          return 'AI_TRIAGING';
        case 'AI_TRIAGING':
          return 'AI_TRIAGED';
        case 'AI_TRIAGED':
          setSearchRadiusKm(1.5);
          return 'SEARCHING_RESPONDERS';
        case 'SEARCHING_RESPONDERS':
          soundEngine.playSuccessChime();
          setSearchRadiusKm(3.0);
          return 'RESPONDER_ACCEPTED';
        case 'RESPONDER_ACCEPTED':
          return 'RESPONDER_EN_ROUTE';
        case 'RESPONDER_EN_ROUTE':
          soundEngine.playSuccessChime();
          return 'RESPONDER_ARRIVED';
        case 'RESPONDER_ARRIVED':
          return 'HANDOVER_108';
        case 'HANDOVER_108':
          soundEngine.playSuccessChime();
          return 'RESOLVED';
        case 'RESOLVED':
          return 'IDLE';
        default:
          return 'IDLE';
      }
    });
  }, []);

  const toggleAutoSimulation = useCallback(() => {
    soundEngine.playClick();
    setIsAutoSimulating(prev => !prev);
  }, []);

  const setSimulationSpeed = useCallback((speed: number) => {
    soundEngine.playClick();
    setSimulationSpeedState(speed);
  }, []);

  // 3-Second Grace Countdown ticker
  useEffect(() => {
    if (!isCountingDown) {
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current);
        countdownTimerRef.current = null;
      }
      return;
    }

    countdownTimerRef.current = window.setInterval(() => {
      setCountdownSeconds(prev => {
        if (prev > 1) {
          soundEngine.playCountdownBeep(750 + (4 - prev) * 120);
          return prev - 1;
        } else {
          if (countdownTimerRef.current) {
            clearInterval(countdownTimerRef.current);
            countdownTimerRef.current = null;
          }
          setIsCountingDown(false);
          triggerSos();
          return 0;
        }
      });
    }, 1000);

    return () => {
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current);
        countdownTimerRef.current = null;
      }
    };
  }, [isCountingDown, triggerSos]);

  // Stopwatch & Auto-Simulation Progression Hook
  useEffect(() => {
    if (incidentStatus === 'IDLE' || incidentStatus === 'COUNTDOWN' || incidentStatus === 'RESOLVED') {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      return;
    }

    const interval = 1000 / simulationSpeed;
    timerRef.current = window.setInterval(() => {
      setElapsedSeconds(prev => {
        const nextTime = prev + 1;

        setTelemetry(t => ({
          ...t,
          spatialQueryLatencyMs: +(11 + Math.random() * 2).toFixed(1),
          websocketConnectionsCount: t.websocketConnectionsCount + (Math.random() > 0.6 ? 1 : 0)
        }));

        if (isAutoSimulating) {
          if (nextTime === 2 && incidentStatus === 'SOS_TRIGGERED') {
            setIncidentStatusState('AI_TRIAGING');
          } else if (nextTime === 5 && incidentStatus === 'AI_TRIAGING') {
            setIncidentStatusState('AI_TRIAGED');
            setSearchRadiusKm(1.2);
          } else if (nextTime === 8 && incidentStatus === 'AI_TRIAGED') {
            setIncidentStatusState('SEARCHING_RESPONDERS');
            setSearchRadiusKm(2.0);
          } else if (nextTime === 14 && (incidentStatus === 'SEARCHING_RESPONDERS' || incidentStatus === 'AI_TRIAGED')) {
            soundEngine.playSuccessChime();
            setIncidentStatusState('RESPONDER_ACCEPTED');
            setSearchRadiusKm(3.0);
          } else if (nextTime === 26 && incidentStatus === 'RESPONDER_ACCEPTED') {
            setIncidentStatusState('RESPONDER_EN_ROUTE');
          } else if (nextTime === 38 && incidentStatus === 'RESPONDER_EN_ROUTE') {
            soundEngine.playSuccessChime();
            setIncidentStatusState('RESPONDER_ARRIVED');
          }
        }

        return nextTime;
      });
    }, interval);

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };
  }, [incidentStatus, isAutoSimulating, simulationSpeed]);

  // Phase 6 Automated Presentation Dry Run Tour ticker
  useEffect(() => {
    if (!isTourActive || isTourPaused) {
      if (tourTimerRef.current) {
        clearInterval(tourTimerRef.current);
        tourTimerRef.current = null;
      }
      return;
    }

    tourTimerRef.current = window.setInterval(() => {
      setTourElapsedSeconds(prev => {
        const nextSec = prev + 1;
        const targetDuration = getSlideDuration(tourSlideIndex, tourPaceMode);

        if (nextSec >= targetDuration) {
          if (tourSlideIndex < MASTER_SLIDES_SYNC.length - 1) {
            const nextIdx = tourSlideIndex + 1;
            setTourSlideIndexState(nextIdx);
            jumpToSlideView(nextIdx + 1);
            return 0;
          } else {
            soundEngine.playSuccessChime();
            setIsTourActiveState(false);
            showToast('🎉 Review Tour Complete! Ready for Examiner Q&A Defense');
            return 0;
          }
        }
        return nextSec;
      });
    }, 1000);

    return () => {
      if (tourTimerRef.current) {
        clearInterval(tourTimerRef.current);
        tourTimerRef.current = null;
      }
    };
  }, [isTourActive, isTourPaused, tourSlideIndex, tourPaceMode, getSlideDuration, jumpToSlideView, showToast]);

  // Presentation Keyboard Shortcuts Hook
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ignore keystrokes when focused inside interactive form fields
      const target = e.target as HTMLElement;
      if (
        target && 
        (target.tagName === 'INPUT' || 
         target.tagName === 'TEXTAREA' || 
         target.isContentEditable)
      ) {
        return;
      }

      if (e.code === 'Space') {
        e.preventDefault();
        if (isTourActive) {
          toggleTourPause();
        } else {
          toggleAutoSimulation();
        }
      } else if (e.code === 'ArrowRight') {
        e.preventDefault();
        if (isTourActive) {
          nextTourSlide();
        } else {
          advanceStep();
        }
      } else if (e.code === 'ArrowLeft') {
        if (isTourActive) {
          e.preventDefault();
          prevTourSlide();
        }
      } else if (e.key === '1') {
        setScenario('scenario-a');
      } else if (e.key === '2') {
        setScenario('scenario-b');
      } else if (e.key === '3') {
        setScenario('scenario-c');
      } else if (e.key === 'v' || e.key === 'V') {
        setPersonaModeState('VICTIM');
        setScreenModeState('CRISIS_MATRIX');
        showToast('🧑 Switched to Victim View');
      } else if (e.key === 'r' || e.key === 'R') {
        setPersonaModeState('RESPONDER');
        setScreenModeState('RESPONDER');
        showToast('🚑 Switched to Responder View');
      } else if (e.key === 'c' || e.key === 'C') {
        setPersonaModeState('COMMAND_CENTER');
        setScreenModeState('COMMAND_CENTER');
        showToast('🛰️ Switched to Command Center View');
      } else if (e.key === 'm' || e.key === 'M') {
        setPersonaModeState('MAP');
        setScreenModeState('MAP');
        showToast('🗺️ Switched to Spatial Live Map');
      } else if (e.key === 'g' || e.key === 'G') {
        setPersonaModeState('VICTIM');
        setScreenModeState('GUARDIAN');
        showToast('🛡️ Switched to Guardian Radar Screen');
      } else if (e.key === 'p' || e.key === 'P') {
        toggleProjectorMode();
      } else if (e.key === 's' || e.key === 'S') {
        toggleSlideSync();
      } else if (e.key === 't' || e.key === 'T') {
        if (isTourActive) {
          stopTour();
        } else {
          startTour('LIGHTNING_60S');
        }
      } else if (e.key === 'x' || e.key === 'X') {
        if (isTourActive) stopTour();
        resetDemo();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [
    isTourActive,
    toggleTourPause,
    nextTourSlide,
    prevTourSlide,
    startTour,
    stopTour,
    toggleAutoSimulation, 
    advanceStep, 
    setScenario, 
    toggleProjectorMode, 
    toggleSlideSync, 
    resetDemo, 
    showToast
  ]);

  return (
    <DemoContext.Provider
      value={{
        currentScenario,
        screenMode,
        personaMode,
        viewLayout,
        incidentStatus,
        selectedMedicalCondition,
        victimSubScreen,
        selectedCrisisCategory,
        anonymousEmergencyMode,
        activeRagStepIndex,
        completedRagSteps,
        isAiChatDrawerOpen,
        aiChatMessages,
        responderSubScreen,
        activeResponderIndex,
        aedAttached,
        responderDeclined,
        responderChatMessages,
        timelineEvents,
        turnByTurnStepIndex,
        intakeInputMode,
        voiceTranscript,
        textInputNotes,
        isVoiceRecording,
        photoAttached,
        photoUrl,
        localityName,
        safetyIndexScore,
        streetAddress,
        subAddress,
        countdownSeconds,
        isCountingDown,
        elapsedSeconds,
        searchRadiusKm,
        isAutoSimulating,
        simulationSpeed,
        audioMuted,
        cprMetronomeActive,
        cprBeatTick,
        telemetry,
        offlineMeshActive,
        mapLayerFilters,
        selectedMapEntity,
        incidentFeed,
        incidentFilterSeverity,
        incidentFilterStatus,
        selectedIncidentId,
        isClinicalReportModalOpen,
        activeHandoverReport,
        projectorMode,
        presentationZoom,
        isSlideSyncOpen,
        isEventDrawerOpen,
        quickNotification,
        isTourActive,
        tourSlideIndex,
        tourElapsedSeconds,
        tourPaceMode,
        isTourPaused,
        setScenario,
        setScreenMode,
        setPersonaMode,
        setViewLayout,
        setIncidentStatus: setIncidentStatusState,
        selectMedicalCondition,
        setVictimSubScreen,
        setSelectedCrisisCategory,
        toggleAnonymousEmergencyMode,
        toggleRagStep,
        setActiveRagStepIndex,
        setAiChatDrawerOpen,
        sendBystanderQuestion,
        resetRagChecklist,
        setResponderSubScreen,
        setActiveResponderIndex,
        toggleAedAttached,
        declineDispatch,
        sendResponderChatMessage,
        setTurnByTurnStepIndex,
        nextTurnByTurnStep,
        prevTurnByTurnStep,
        toggleMapLayer,
        resetMapLayers,
        setSelectedMapEntity,
        setIncidentFilterSeverity,
        setIncidentFilterStatus,
        setSelectedIncidentId,
        setIsClinicalReportModalOpen: setIsClinicalReportModalOpenState,
        openClinicalReport,
        trigger108Escalation,
        broadcastAlert,
        toggleProjectorMode,
        setPresentationZoom,
        setIsSlideSyncOpen,
        toggleSlideSync,
        setIsEventDrawerOpen,
        jumpToSlideView,
        triggerLifecycleEvent,
        showToast,
        startTour,
        stopTour,
        toggleTourPause,
        nextTourSlide,
        prevTourSlide,
        setTourPaceMode,
        setIntakeInputMode,
        toggleVoiceRecording,
        setTextInputNotes,
        attachSamplePhoto,
        removePhoto,
        startCountdown,
        cancelCountdown,
        confirmAddress,
        triggerSos,
        cancelSos,
        advanceStep,
        resetDemo,
        toggleAutoSimulation,
        setSimulationSpeed,
        toggleAudioMute,
        toggleCprMetronome,
        acceptDispatch,
        simulateArrival,
        handoverTo108,
        resolveEmergency,
      }}
    >
      {children}
    </DemoContext.Provider>
  );
};

export const useDemoStore = (): DemoContextType => {
  const context = useContext(DemoContext);
  if (!context) {
    throw new Error('useDemoStore must be used within a DemoProvider');
  }
  return context;
};
