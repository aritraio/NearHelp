/* ==========================================================================
   NearHelp AI — Central Demo State Store & Simulation Engine
   File: src/store/DemoContext.tsx
   ========================================================================== */

import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import type { 
  EmergencyScenario, 
  IncidentStatus, 
  PersonaMode, 
  ViewLayout, 
  SystemTelemetry 
} from '../mock/types';
import { ALL_SCENARIOS, SCENARIO_A, INITIAL_TELEMETRY } from '../mock/scenarios';
import { soundEngine } from '../utils/audio';

interface DemoContextType {
  // Scenario & Mode
  currentScenario: EmergencyScenario;
  personaMode: PersonaMode;
  viewLayout: ViewLayout;
  incidentStatus: IncidentStatus;
  
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
  setPersonaMode: (mode: PersonaMode) => void;
  setViewLayout: (layout: ViewLayout) => void;
  setIncidentStatus: (status: IncidentStatus) => void;
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
  const [personaMode, setPersonaModeState] = useState<PersonaMode>('VICTIM');
  const [viewLayout, setViewLayout] = useState<ViewLayout>('MOBILE_FRAME');
  const [incidentStatus, setIncidentStatusState] = useState<IncidentStatus>('IDLE');
  
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
    setOfflineMeshActive(selected.id === 'scenario-c');
    // Stop metronome on scenario switch
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    // Reset state to IDLE for fresh scenario demonstration
    setIncidentStatusState('IDLE');
    setElapsedSeconds(0);
    setSearchRadiusKm(0.5);
  }, []);

  // Persona switching with subtle audio cue
  const setPersonaMode = useCallback((mode: PersonaMode) => {
    soundEngine.playClick();
    setPersonaModeState(mode);
  }, []);

  // Trigger SOS Flow
  const triggerSos = useCallback(() => {
    soundEngine.playEmergencyAlert();
    setIncidentStatusState('SOS_TRIGGERED');
    setElapsedSeconds(0);
    setIsAutoSimulating(true);
  }, []);

  // Cancel SOS
  const cancelSos = useCallback(() => {
    soundEngine.playClick();
    soundEngine.stopCprMetronome();
    setCprMetronomeActive(false);
    setIncidentStatusState('IDLE');
    setElapsedSeconds(0);
    setIsAutoSimulating(false);
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
    setPersonaModeState('VICTIM');
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

  // Stopwatch & Auto-Simulation Progression Hook
  useEffect(() => {
    if (incidentStatus === 'IDLE' || incidentStatus === 'RESOLVED') {
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

        // Telemetry subtle jitter for realism
        setTelemetry(t => ({
          ...t,
          spatialQueryLatencyMs: +(11 + Math.random() * 2).toFixed(1),
          websocketConnectionsCount: t.websocketConnectionsCount + (Math.random() > 0.6 ? 1 : 0)
        }));

        // Auto progression timeline
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
        personaMode,
        viewLayout,
        incidentStatus,
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
        setPersonaMode,
        setViewLayout,
        setIncidentStatus: setIncidentStatusState,
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
