/* ==========================================================================
   NearHelp AI — Central Demo State Store & Simulation Engine
   File: src/store/DemoContext.tsx (Medical Emergency Intake & Multimodal)
   ========================================================================== */

import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import type { 
  EmergencyScenario, 
  IncidentStatus, 
  PersonaMode, 
  ScreenMode,
  ViewLayout, 
  SystemTelemetry,
  MedicalConditionId,
  MultimodalInputMode
} from '../mock/types';
import { ALL_SCENARIOS, SCENARIO_A, INITIAL_TELEMETRY, MEDICAL_CONDITIONS } from '../mock/scenarios';
import { soundEngine } from '../utils/audio';

interface DemoContextType {
  // Scenario & Screen Mode
  currentScenario: EmergencyScenario;
  screenMode: ScreenMode;
  personaMode: PersonaMode;
  viewLayout: ViewLayout;
  incidentStatus: IncidentStatus;
  selectedMedicalCondition: MedicalConditionId;

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
  
  // Actions
  setScenario: (id: 'scenario-a' | 'scenario-b' | 'scenario-c') => void;
  setScreenMode: (mode: ScreenMode) => void;
  setPersonaMode: (mode: PersonaMode) => void;
  setViewLayout: (layout: ViewLayout) => void;
  setIncidentStatus: (status: IncidentStatus) => void;
  selectMedicalCondition: (conditionId: MedicalConditionId) => void;
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

  // Multimodal Medical Intake state
  const [intakeInputMode, setIntakeInputModeState] = useState<MultimodalInputMode>('PRESETS');
  const [voiceTranscript, setVoiceTranscript] = useState<string>(
    "Emergency! 54-year-old male collapsed near elevator at Godrej Waterside. Unconscious, not breathing properly, turning blue..."
  );
  const [textInputNotes, setTextInputNotesState] = useState<string>('Sudden loss of consciousness, agonal gasping, no pulse detected');
  const [isVoiceRecording, setIsVoiceRecording] = useState<boolean>(false);
  const [photoAttached, setPhotoAttached] = useState<boolean>(false);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);

  const [localityName, setLocalityName] = useState<string>('China Basin');
  const [safetyIndexScore, setSafetyIndexScore] = useState<number>(91);
  const [streetAddress, setStreetAddress] = useState<string>('1234 Mission St');
  const [subAddress, setSubAddress] = useState<string>('Apt #345B, 27th Floor • San Francisco, CA');

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
    setLocalityName(selected.locationName.split(',')[0] || 'China Basin');
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
    } else if (mode === 'COMMAND_CENTER') {
      setScreenModeState('COMMAND_CENTER');
    }
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
    setElapsedSeconds(0);
    setIsAutoSimulating(false);
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setSearchRadiusKm(0.5);
  }, []);

  // Responder Actions
  const acceptDispatch = useCallback(() => {
    soundEngine.playSuccessChime();
    setIncidentStatusState('RESPONDER_ACCEPTED');
  }, []);

  const simulateArrival = useCallback(() => {
    soundEngine.playSuccessChime();
    setIncidentStatusState('RESPONDER_ARRIVED');
  }, []);

  const handoverTo108 = useCallback(() => {
    soundEngine.playClick();
    setIncidentStatusState('HANDOVER_108');
  }, []);

  const resolveEmergency = useCallback(() => {
    soundEngine.playSuccessChime();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('RESOLVED');
    setIsAutoSimulating(false);
    setIsCountingDown(false);
  }, []);

  // Reset entire demo cleanly
  const resetDemo = useCallback(() => {
    soundEngine.playClick();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('IDLE');
    setElapsedSeconds(0);
    setSearchRadiusKm(0.5);
    setIsAutoSimulating(false);
    setIsCountingDown(false);
    setCountdownSeconds(3);
    setPersonaModeState('VICTIM');
    setScreenModeState('GUARDIAN');
    setIsVoiceRecording(false);
  }, []);

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

  return (
    <DemoContext.Provider
      value={{
        currentScenario,
        screenMode,
        personaMode,
        viewLayout,
        incidentStatus,
        selectedMedicalCondition,
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
        setScenario,
        setScreenMode,
        setPersonaMode,
        setViewLayout,
        setIncidentStatus: setIncidentStatusState,
        selectMedicalCondition,
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
