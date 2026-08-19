/* ==========================================================================
   NearHelp AI — Floating Master Demo Controller Bar (Phase 5 Showcase Ready)
   File: src/components/demo/ScenarioController.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Play, 
  Pause, 
  RotateCcw, 
  FastForward, 
  Volume2, 
  VolumeX, 
  Heart, 
  Smartphone, 
  Columns, 
  Monitor, 
  Activity, 
  ShieldAlert, 
  Radio, 
  Radar,
  Grid,
  Ambulance, 
  Layers,
  Map,
  Tv,
  Presentation,
  Zap,
  ChevronDown,
  Sparkles,
  CheckCircle2,
  Stethoscope,
  Share2
} from 'lucide-react';
import type { IncidentStatus, PresentationZoom } from '../../mock/types';

export const ScenarioController: React.FC = () => {
  const {
    currentScenario,
    screenMode,
    viewLayout,
    incidentStatus,
    elapsedSeconds,
    isAutoSimulating,
    simulationSpeed,
    audioMuted,
    cprMetronomeActive,
    projectorMode,
    presentationZoom,
    isTourActive,
    startTour,
    stopTour,
    setScenario,
    setScreenMode,
    setPersonaMode,
    setViewLayout,
    advanceStep,
    resetDemo,
    toggleAutoSimulation,
    setSimulationSpeed,
    toggleAudioMute,
    toggleCprMetronome,
    toggleProjectorMode,
    setPresentationZoom,
    toggleSlideSync,
    triggerLifecycleEvent,
    showToast
  } = useDemoStore();

  const [isLifecycleMenuOpen, setIsLifecycleMenuOpen] = useState<boolean>(false);

  const formatTimer = (sec: number) => {
    const mins = Math.floor(sec / 60);
    const secs = sec % 60;
    return `T+${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const lifecycleStages: { status: IncidentStatus; label: string; icon: React.ReactNode; color: string }[] = [
    { status: 'IDLE', label: '1. Standby (Idle)', icon: <RotateCcw size={12} />, color: 'var(--text-muted)' },
    { status: 'SOS_TRIGGERED', label: '2. 1-Tap SOS Dispatched', icon: <Zap size={12} />, color: 'var(--color-emergency-red-bright)' },
    { status: 'AI_TRIAGING', label: '3. Multimodal AI Ingestion', icon: <Sparkles size={12} />, color: 'var(--color-ai-cyan)' },
    { status: 'AI_TRIAGED', label: '4. Level 5 Urgency Classified', icon: <Stethoscope size={12} />, color: 'var(--color-emergency-red-bright)' },
    { status: 'SEARCHING_RESPONDERS', label: '5. PostGIS Spatial Radial Query', icon: <Map size={12} />, color: 'var(--color-ai-cyan)' },
    { status: 'RESPONDER_ACCEPTED', label: '6. Volunteer Accepted Rescue', icon: <CheckCircle2 size={12} />, color: 'var(--color-safe-green-bright)' },
    { status: 'RESPONDER_EN_ROUTE', label: '7. Turn-by-Turn Route Active', icon: <Ambulance size={12} />, color: 'var(--color-safe-green-bright)' },
    { status: 'RESPONDER_ARRIVED', label: '8. Arrived on Scene (CPR)', icon: <Heart size={12} />, color: 'var(--color-safe-green-bright)' },
    { status: 'HANDOVER_108', label: '9. 108 Ambulance Handover', icon: <Share2 size={12} />, color: 'var(--color-action-amber-bright)' },
    { status: 'RESOLVED', label: '10. Incident Audit Sealed', icon: <CheckCircle2 size={12} />, color: 'var(--color-safe-green-bright)' }
  ];

  return (
    <header 
      aria-label="Demo Controller Bar" 
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        width: '100%',
        backgroundColor: projectorMode ? 'rgba(5, 7, 10, 0.96)' : 'var(--bg-glass-heavy)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderBottom: `1.5px solid ${projectorMode ? 'rgba(255, 255, 255, 0.28)' : 'var(--border-medium)'}`,
        padding: '8px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        boxShadow: '0 8px 30px rgba(0, 0, 0, 0.85)'
      }}
    >
      {/* Top Primary Bar */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '10px'
      }}>
        {/* Left: Brand Identity & Scenario Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div className="telemetry-dot telemetry-dot-emergency" />
            <span style={{ 
              fontWeight: 800, 
              fontSize: '14px', 
              letterSpacing: '-0.02em',
              color: 'var(--text-primary)',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}>
              <span style={{ color: 'var(--color-emergency-red-bright)' }}>NearHelp</span> AI
              <span style={{ 
                fontSize: '10px', 
                padding: '2px 7px', 
                borderRadius: 'var(--radius-xs)', 
                backgroundColor: 'var(--color-ai-subtle)', 
                color: 'var(--color-ai-cyan)',
                border: '1px solid var(--border-ai)',
                fontWeight: 800,
                letterSpacing: '0.04em'
              }}>
                SHOW MODE
              </span>
            </span>
          </div>

          {/* Scenario Preset Buttons */}
          <div style={{ 
            display: 'flex', 
            backgroundColor: 'var(--bg-surface)', 
            padding: '2px', 
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)',
            gap: '2px'
          }}>
            <button
              onClick={() => {
                setScenario('scenario-a');
                showToast('🫀 Scenario A Loaded: Cardiac Arrest in Salt Lake');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: currentScenario.id === 'scenario-a' ? 700 : 500,
                backgroundColor: currentScenario.id === 'scenario-a' ? 'var(--color-emergency-red)' : 'transparent',
                color: currentScenario.id === 'scenario-a' ? '#fff' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                transition: 'all var(--transition-fast)'
              }}
              title="Scenario 1 (Slide 2/3): Cardiac Arrest in Salt Lake Sector V (Level 5)"
            >
              <Activity size={13} />
              <span>1: Cardiac</span>
            </button>

            <button
              onClick={() => {
                setScenario('scenario-b');
                showToast('💥 Scenario B Loaded: Severe Arterial Bleed on EM Bypass');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: currentScenario.id === 'scenario-b' ? 700 : 500,
                backgroundColor: currentScenario.id === 'scenario-b' ? 'var(--color-action-amber)' : 'transparent',
                color: currentScenario.id === 'scenario-b' ? '#000' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                transition: 'all var(--transition-fast)'
              }}
              title="Scenario 2: Severe Bleed on EM Bypass (Level 4 Tourniquet)"
            >
              <ShieldAlert size={13} />
              <span>2: Bleed</span>
            </button>

            <button
              onClick={() => {
                setScenario('scenario-c');
                showToast('📵 Scenario C Loaded: Offline BLE Mesh & Binary SMS Fallback');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: currentScenario.id === 'scenario-c' ? 700 : 500,
                backgroundColor: currentScenario.id === 'scenario-c' ? 'var(--color-ai-blue)' : 'transparent',
                color: currentScenario.id === 'scenario-c' ? '#fff' : 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                transition: 'all var(--transition-fast)'
              }}
              title="Scenario 3: Offline Mesh/SMS Packet Preview (Zero Internet)"
            >
              <Radio size={13} />
              <span>3: Offline</span>
            </button>
          </div>
        </div>

        {/* Center: Fast Persona Switcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          <div style={{ 
            display: 'flex', 
            backgroundColor: 'var(--bg-surface)', 
            padding: '2px', 
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)',
            gap: '2px'
          }}>
            <button
              onClick={() => {
                setPersonaMode('VICTIM');
                setScreenMode('GUARDIAN');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: screenMode === 'GUARDIAN' ? 700 : 500,
                backgroundColor: screenMode === 'GUARDIAN' ? 'rgba(52, 199, 89, 0.2)' : 'transparent',
                color: screenMode === 'GUARDIAN' ? '#22C55E' : 'var(--text-secondary)',
                border: screenMode === 'GUARDIAN' ? '1px solid #22C55E' : '1px solid transparent',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              title="Screen 1: Guardian Radar & Safe Zone (Key: G)"
            >
              <Radar size={13} />
              <span>Guardian</span>
            </button>

            <button
              onClick={() => {
                setPersonaMode('VICTIM');
                setScreenMode('CRISIS_MATRIX');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: screenMode === 'CRISIS_MATRIX' ? 700 : 500,
                backgroundColor: screenMode === 'CRISIS_MATRIX' ? 'rgba(255, 42, 68, 0.2)' : 'transparent',
                color: screenMode === 'CRISIS_MATRIX' ? 'var(--color-emergency-red-bright)' : 'var(--text-secondary)',
                border: screenMode === 'CRISIS_MATRIX' ? '1px solid var(--emergency-crimson)' : '1px solid transparent',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              title="Screens 2 & 3: Victim SOS & First-Aid RAG (Key: V)"
            >
              <Grid size={13} />
              <span>Victim SOS</span>
            </button>

            <button
              onClick={() => {
                setPersonaMode('RESPONDER');
                setScreenMode('RESPONDER');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: screenMode === 'RESPONDER' ? 700 : 500,
                backgroundColor: screenMode === 'RESPONDER' ? 'rgba(0, 230, 118, 0.2)' : 'transparent',
                color: screenMode === 'RESPONDER' ? 'var(--color-safe-green-bright)' : 'var(--text-secondary)',
                border: screenMode === 'RESPONDER' ? '1px solid var(--color-safe-green)' : '1px solid transparent',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              title="Screens 4 & 5: Responder Rescue & Medical ID (Key: R)"
            >
              <Ambulance size={13} />
              <span>Responder</span>
            </button>

            <button
              onClick={() => {
                setPersonaMode('MAP');
                setScreenMode('MAP');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: screenMode === 'MAP' ? 700 : 500,
                backgroundColor: screenMode === 'MAP' ? 'rgba(0, 229, 255, 0.2)' : 'transparent',
                color: screenMode === 'MAP' ? 'var(--color-ai-cyan)' : 'var(--text-secondary)',
                border: screenMode === 'MAP' ? '1px solid var(--color-ai-cyan)' : '1px solid transparent',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              title="Screen 6: Dynamic Community Geo-Map with PostGIS Waves (Key: M)"
            >
              <Map size={13} />
              <span>Live Map</span>
            </button>

            <button
              onClick={() => {
                setPersonaMode('COMMAND_CENTER');
                setScreenMode('COMMAND_CENTER');
              }}
              style={{
                padding: '5px 9px',
                borderRadius: 'var(--radius-xs)',
                fontSize: '12px',
                fontWeight: screenMode === 'COMMAND_CENTER' ? 700 : 500,
                backgroundColor: screenMode === 'COMMAND_CENTER' ? 'rgba(255, 160, 0, 0.2)' : 'transparent',
                color: screenMode === 'COMMAND_CENTER' ? 'var(--color-action-amber-bright)' : 'var(--text-secondary)',
                border: screenMode === 'COMMAND_CENTER' ? '1px solid var(--color-action-amber)' : '1px solid transparent',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              title="Screen 7: Command Center Telemetry & Incident Audit (Key: C)"
            >
              <Layers size={13} />
              <span>Command</span>
            </button>
          </div>

          {/* Incident Status Badge & Timer with Dropdown */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setIsLifecycleMenuOpen(prev => !prev)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '5px 10px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-medium)',
                fontSize: '12px'
              }}
              title="Click to jump directly to any emergency milestone"
            >
              <span className="font-mono" style={{ color: 'var(--color-action-amber-bright)', fontWeight: 800 }}>
                {formatTimer(elapsedSeconds)}
              </span>
              <span style={{ color: 'var(--border-highlight)' }}>|</span>
              <span style={{ 
                color: incidentStatus === 'IDLE' ? 'var(--text-muted)' : 'var(--color-emergency-red-bright)',
                fontWeight: 700,
                textTransform: 'uppercase',
                fontSize: '11px',
                letterSpacing: '0.04em'
              }}>
                {incidentStatus.replace(/_/g, ' ')}
              </span>
              <ChevronDown size={12} style={{ color: 'var(--text-muted)' }} />
            </button>

            {/* Direct Milestone Jump Dropdown */}
            {isLifecycleMenuOpen && (
              <div 
                className="animate-slide-in"
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  marginTop: '6px',
                  width: '260px',
                  backgroundColor: 'var(--bg-card-dark)',
                  border: '1px solid var(--border-medium)',
                  borderRadius: 'var(--radius-md)',
                  padding: '6px',
                  zIndex: 1100,
                  boxShadow: '0 10px 30px rgba(0,0,0,0.9)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '3px'
                }}
              >
                <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', padding: '4px 8px' }}>
                  Direct Milestone Jump (Examiner Demo)
                </div>
                {lifecycleStages.map(st => {
                  const isCurrent = incidentStatus === st.status;
                  return (
                    <button
                      key={st.status}
                      onClick={() => {
                        triggerLifecycleEvent(st.status);
                        setIsLifecycleMenuOpen(false);
                      }}
                      style={{
                        padding: '6px 10px',
                        borderRadius: 'var(--radius-xs)',
                        fontSize: '11px',
                        fontWeight: isCurrent ? 700 : 500,
                        backgroundColor: isCurrent ? 'var(--bg-surface-elevated)' : 'transparent',
                        color: isCurrent ? st.color : 'var(--text-secondary)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        textAlign: 'left'
                      }}
                    >
                      <span style={{ color: st.color }}>{st.icon}</span>
                      <span>{st.label}</span>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Right: Simulation Controls, Projector Tuning, Slide HUD */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
          {/* Layout Mode */}
          <div style={{ 
            display: 'flex', 
            backgroundColor: 'var(--bg-surface)', 
            padding: '2px', 
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)',
            gap: '2px'
          }}>
            <button
              onClick={() => {
                setViewLayout('SPLIT_SCREEN');
                showToast('📱 📱 Dual-Persona Split View Activated');
              }}
              title="Dual Persona Split Screen (Victim + Responder Synchronized)"
              style={{
                padding: '4px 7px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: viewLayout === 'SPLIT_SCREEN' ? 'var(--bg-surface-elevated)' : 'transparent',
                color: viewLayout === 'SPLIT_SCREEN' ? 'var(--text-primary)' : 'var(--text-muted)'
              }}
            >
              <Columns size={13} />
            </button>

            <button
              onClick={() => {
                setViewLayout('MOBILE_FRAME');
                showToast('📱 Single Mobile Phone Chassis Mode');
              }}
              title="Single Mobile Smartphone Frame"
              style={{
                padding: '4px 7px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: viewLayout === 'MOBILE_FRAME' ? 'var(--bg-surface-elevated)' : 'transparent',
                color: viewLayout === 'MOBILE_FRAME' ? 'var(--text-primary)' : 'var(--text-muted)'
              }}
            >
              <Smartphone size={13} />
            </button>

            <button
              onClick={() => {
                setViewLayout('DESKTOP_FULL');
                showToast('🖥️ Fullscreen Desktop Command Center Mode');
              }}
              title="Full Architecture & Token Visualizer"
              style={{
                padding: '4px 7px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: viewLayout === 'DESKTOP_FULL' ? 'var(--bg-surface-elevated)' : 'transparent',
                color: viewLayout === 'DESKTOP_FULL' ? 'var(--text-primary)' : 'var(--text-muted)'
              }}
            >
              <Monitor size={13} />
            </button>
          </div>

          {/* Projector Mode High-Contrast Toggle */}
          <button
            onClick={toggleProjectorMode}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 8px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: projectorMode ? 'rgba(0, 229, 255, 0.2)' : 'var(--bg-surface)',
              color: projectorMode ? 'var(--color-ai-cyan)' : 'var(--text-secondary)',
              border: `1px solid ${projectorMode ? 'var(--color-ai-cyan)' : 'var(--border-subtle)'}`
            }}
            title="Toggle 1080p High-Contrast Projector Tuning (Key: P)"
          >
            <Tv size={13} />
            <span>{projectorMode ? 'Projector ON' : 'Projector'}</span>
          </button>

          {/* Viewport Zoom / Scale Toggle */}
          <button
            onClick={() => {
              const nextScale: PresentationZoom = presentationZoom === 100 ? 110 : presentationZoom === 110 ? 125 : 100;
              setPresentationZoom(nextScale);
            }}
            style={{
              padding: '4px 7px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              fontFamily: 'var(--font-mono)',
              backgroundColor: 'var(--bg-surface)',
              color: presentationZoom > 100 ? 'var(--color-action-amber-bright)' : 'var(--text-muted)',
              border: '1px solid var(--border-subtle)'
            }}
            title="Toggle Presentation Zoom Scale (100% -> 110% -> 125%)"
          >
            {presentationZoom}%
          </button>

          {/* Slide Synchronizer HUD Button */}
          <button
            onClick={toggleSlideSync}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: 'var(--color-ai-subtle)',
              color: 'var(--color-ai-cyan)',
              border: '1px solid var(--border-ai)'
            }}
            title="Open Master 8-Slide Synchronizer & Examiner Defense (Key: S)"
          >
            <Presentation size={13} />
            <span>Slide Sync</span>
          </button>

          {/* Automated Rehearsal Tour Button */}
          <button
            onClick={() => {
              if (isTourActive) {
                stopTour();
              } else {
                startTour('LIGHTNING_60S');
              }
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: isTourActive ? 'var(--color-emergency-red)' : 'var(--bg-surface)',
              color: isTourActive ? '#fff' : 'var(--color-emergency-red-bright)',
              border: `1px solid ${isTourActive ? 'var(--color-emergency-red-bright)' : 'var(--border-crimson)'}`
            }}
            title="Launch Automated 8-Slide Presentation Rehearsal Tour (Key: T)"
          >
            <Zap size={13} />
            <span>{isTourActive ? 'Stop Tour' : 'Tour'}</span>
          </button>

          {/* CPR Metronome Toggle */}
          <button
            onClick={toggleCprMetronome}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--bg-surface)',
              color: cprMetronomeActive ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${cprMetronomeActive ? 'var(--color-emergency-red-bright)' : 'var(--border-subtle)'}`,
            }}
            title="Toggle 110 BPM CPR Rhythmic Metronome"
          >
            <Heart size={12} className={cprMetronomeActive ? 'cpr-beat-active' : ''} />
            <span>110 BPM</span>
          </button>

          {/* Audio Mute */}
          <button
            onClick={toggleAudioMute}
            style={{
              padding: '5px 7px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--bg-surface)',
              color: audioMuted ? 'var(--color-action-amber)' : 'var(--text-secondary)',
              border: '1px solid var(--border-subtle)'
            }}
            title={audioMuted ? 'Unmute Audio' : 'Mute Audio'}
          >
            {audioMuted ? <VolumeX size={13} /> : <Volume2 size={13} />}
          </button>

          {/* Simulation Auto Play/Pause */}
          <button
            onClick={toggleAutoSimulation}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: isAutoSimulating ? 'var(--color-safe-green-subtle)' : 'var(--bg-surface)',
              color: isAutoSimulating ? 'var(--color-safe-green-bright)' : 'var(--text-secondary)',
              border: `1px solid ${isAutoSimulating ? 'var(--color-safe-green)' : 'var(--border-subtle)'}`
            }}
            title="Toggle Auto-Progression Simulation (Space)"
          >
            {isAutoSimulating ? <Pause size={12} /> : <Play size={12} />}
            <span>{isAutoSimulating ? 'Pause' : 'Auto'}</span>
          </button>

          {/* Simulation Speed */}
          <button
            onClick={() => setSimulationSpeed(simulationSpeed === 1 ? 2 : simulationSpeed === 2 ? 5 : 1)}
            style={{
              padding: '4px 7px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              fontFamily: 'var(--font-mono)',
              backgroundColor: 'var(--bg-surface)',
              color: simulationSpeed > 1 ? 'var(--color-ai-cyan)' : 'var(--text-muted)',
              border: '1px solid var(--border-subtle)'
            }}
            title="Toggle Simulation Progression Speed (1x, 2x, 5x)"
          >
            {simulationSpeed}x
          </button>

          {/* Step Forward */}
          <button
            onClick={advanceStep}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: 'var(--color-ai-subtle)',
              color: 'var(--color-ai-cyan)',
              border: '1px solid var(--border-ai)'
            }}
            title="Advance to next emergency milestone (Right Arrow)"
          >
            <FastForward size={12} />
            <span>Step</span>
          </button>

          {/* Reset Clean */}
          <button
            onClick={resetDemo}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '5px 9px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              fontWeight: 700,
              backgroundColor: 'var(--bg-surface)',
              color: 'var(--color-action-amber-bright)',
              border: '1px solid var(--border-subtle)'
            }}
            title="Reset Simulation to Initial Standby State (Key: X)"
          >
            <RotateCcw size={12} />
            <span>Reset</span>
          </button>
        </div>
      </div>
    </header>
  );
};
