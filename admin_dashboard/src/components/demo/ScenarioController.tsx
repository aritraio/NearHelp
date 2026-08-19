/* ==========================================================================
   NearHelp AI — Floating Demo Controller Bar
   File: src/components/demo/ScenarioController.tsx
   ========================================================================== */

import React from 'react';
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
  UserCheck, 
  Ambulance, 
  Layers
} from 'lucide-react';

export const ScenarioController: React.FC = () => {
  const {
    currentScenario,
    personaMode,
    viewLayout,
    incidentStatus,
    elapsedSeconds,
    isAutoSimulating,
    simulationSpeed,
    audioMuted,
    cprMetronomeActive,
    setScenario,
    setPersonaMode,
    setViewLayout,
    advanceStep,
    resetDemo,
    toggleAutoSimulation,
    setSimulationSpeed,
    toggleAudioMute,
    toggleCprMetronome,
  } = useDemoStore();

  const formatTimer = (sec: number) => {
    const mins = Math.floor(sec / 60);
    const secs = sec % 60;
    return `T+${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <aside aria-label="Demo Controller Bar" style={{
      position: 'sticky',
      top: 0,
      zIndex: 1000,
      width: '100%',
      backgroundColor: 'var(--bg-glass-heavy)',
      backdropFilter: 'blur(16px)',
      WebkitBackdropFilter: 'blur(16px)',
      borderBottom: '1px solid var(--border-medium)',
      padding: '8px 16px',
      display: 'flex',
      flexWrap: 'wrap',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '12px',
      boxShadow: 'var(--shadow-md)'
    }}>
      {/* Left: Brand & Scenario Selector */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
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
              padding: '2px 6px', 
              borderRadius: 'var(--radius-xs)', 
              backgroundColor: 'var(--color-ai-subtle)', 
              color: 'var(--color-ai-cyan)',
              border: '1px solid var(--border-ai)',
              fontWeight: 700
            }}>
              DEMO ENGINE
            </span>
          </span>
        </div>

        {/* Scenario Buttons */}
        <div style={{ 
          display: 'flex', 
          backgroundColor: 'var(--bg-surface)', 
          padding: '2px', 
          borderRadius: 'var(--radius-sm)',
          border: '1px solid var(--border-subtle)',
          gap: '2px'
        }}>
          <button
            onClick={() => setScenario('scenario-a')}
            style={{
              padding: '5px 10px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: currentScenario.id === 'scenario-a' ? 700 : 500,
              backgroundColor: currentScenario.id === 'scenario-a' ? 'var(--color-emergency-red)' : 'transparent',
              color: currentScenario.id === 'scenario-a' ? '#fff' : 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
            title="Scenario A: Cardiac Arrest in Salt Lake Sector V"
          >
            <Activity size={13} />
            <span>1: Cardiac (Sector V)</span>
          </button>

          <button
            onClick={() => setScenario('scenario-b')}
            style={{
              padding: '5px 10px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: currentScenario.id === 'scenario-b' ? 700 : 500,
              backgroundColor: currentScenario.id === 'scenario-b' ? 'var(--color-action-amber)' : 'transparent',
              color: currentScenario.id === 'scenario-b' ? '#000' : 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
            title="Scenario B: Severe Arterial Bleed on EM Bypass"
          >
            <ShieldAlert size={13} />
            <span>2: Bleed (EM Bypass)</span>
          </button>

          <button
            onClick={() => setScenario('scenario-c')}
            style={{
              padding: '5px 10px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: currentScenario.id === 'scenario-c' ? 700 : 500,
              backgroundColor: currentScenario.id === 'scenario-c' ? 'var(--color-ai-blue)' : 'transparent',
              color: currentScenario.id === 'scenario-c' ? '#fff' : 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
            title="Scenario C: Offline Mesh/SMS Simulation"
          >
            <Radio size={13} />
            <span>3: Offline Mesh</span>
          </button>
        </div>
      </div>

      {/* Center: Persona Toggle & Status Badge */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
        {/* Persona Selector */}
        <div style={{ 
          display: 'flex', 
          backgroundColor: 'var(--bg-surface)', 
          padding: '2px', 
          borderRadius: 'var(--radius-sm)',
          border: '1px solid var(--border-subtle)',
          gap: '2px'
        }}>
          <button
            onClick={() => setPersonaMode('VICTIM')}
            style={{
              padding: '5px 12px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: personaMode === 'VICTIM' ? 700 : 500,
              backgroundColor: personaMode === 'VICTIM' ? 'rgba(255, 23, 68, 0.2)' : 'transparent',
              color: personaMode === 'VICTIM' ? 'var(--color-emergency-red-bright)' : 'var(--text-secondary)',
              border: personaMode === 'VICTIM' ? '1px solid var(--color-emergency-red)' : '1px solid transparent',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <UserCheck size={13} />
            <span>🧑 Victim View</span>
          </button>

          <button
            onClick={() => setPersonaMode('RESPONDER')}
            style={{
              padding: '5px 12px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: personaMode === 'RESPONDER' ? 700 : 500,
              backgroundColor: personaMode === 'RESPONDER' ? 'rgba(0, 230, 118, 0.2)' : 'transparent',
              color: personaMode === 'RESPONDER' ? 'var(--color-safe-green-bright)' : 'var(--text-secondary)',
              border: personaMode === 'RESPONDER' ? '1px solid var(--color-safe-green)' : '1px solid transparent',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <Ambulance size={13} />
            <span>🚑 Responder View</span>
          </button>

          <button
            onClick={() => setPersonaMode('COMMAND_CENTER')}
            style={{
              padding: '5px 12px',
              borderRadius: 'var(--radius-xs)',
              fontSize: '12px',
              fontWeight: personaMode === 'COMMAND_CENTER' ? 700 : 500,
              backgroundColor: personaMode === 'COMMAND_CENTER' ? 'rgba(0, 229, 255, 0.2)' : 'transparent',
              color: personaMode === 'COMMAND_CENTER' ? 'var(--color-ai-cyan)' : 'var(--text-secondary)',
              border: personaMode === 'COMMAND_CENTER' ? '1px solid var(--color-ai-cyan)' : '1px solid transparent',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <Layers size={13} />
            <span>🛰️ Command Center</span>
          </button>
        </div>

        {/* Live Status & Timer Badge */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '4px 10px',
          borderRadius: 'var(--radius-sm)',
          backgroundColor: 'var(--bg-surface-elevated)',
          border: '1px solid var(--border-subtle)',
          fontSize: '12px'
        }}>
          <span className="font-mono" style={{ color: 'var(--color-action-amber-bright)', fontWeight: 700 }}>
            {formatTimer(elapsedSeconds)}
          </span>
          <span style={{ color: 'var(--border-highlight)' }}>|</span>
          <span style={{ 
            color: incidentStatus === 'IDLE' ? 'var(--text-muted)' : 'var(--color-emergency-red-bright)',
            fontWeight: 600,
            textTransform: 'uppercase',
            fontSize: '11px',
            letterSpacing: '0.04em'
          }}>
            {incidentStatus.replace(/_/g, ' ')}
          </span>
        </div>
      </div>

      {/* Right: Simulation Controls & Layout Switcher */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
        {/* Layout Switcher */}
        <div style={{ 
          display: 'flex', 
          backgroundColor: 'var(--bg-surface)', 
          padding: '2px', 
          borderRadius: 'var(--radius-sm)',
          border: '1px solid var(--border-subtle)',
          gap: '2px'
        }}>
          <button
            onClick={() => setViewLayout('MOBILE_FRAME')}
            title="Mobile Smartphone Frame"
            style={{
              padding: '4px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: viewLayout === 'MOBILE_FRAME' ? 'var(--bg-surface-elevated)' : 'transparent',
              color: viewLayout === 'MOBILE_FRAME' ? 'var(--text-primary)' : 'var(--text-muted)'
            }}
          >
            <Smartphone size={14} />
          </button>
          <button
            onClick={() => setViewLayout('SPLIT_SCREEN')}
            title="Dual Persona Split Screen"
            style={{
              padding: '4px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: viewLayout === 'SPLIT_SCREEN' ? 'var(--bg-surface-elevated)' : 'transparent',
              color: viewLayout === 'SPLIT_SCREEN' ? 'var(--text-primary)' : 'var(--text-muted)'
            }}
          >
            <Columns size={14} />
          </button>
          <button
            onClick={() => setViewLayout('DESKTOP_FULL')}
            title="Full Width Projector View"
            style={{
              padding: '4px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: viewLayout === 'DESKTOP_FULL' ? 'var(--bg-surface-elevated)' : 'transparent',
              color: viewLayout === 'DESKTOP_FULL' ? 'var(--text-primary)' : 'var(--text-muted)'
            }}
          >
            <Monitor size={14} />
          </button>
        </div>

        {/* CPR Metronome Audio Toggle */}
        <button
          onClick={toggleCprMetronome}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            padding: '5px 10px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '12px',
            fontWeight: 600,
            backgroundColor: cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--bg-surface)',
            color: cprMetronomeActive ? '#ffffff' : 'var(--text-secondary)',
            border: `1px solid ${cprMetronomeActive ? 'var(--color-emergency-red-bright)' : 'var(--border-subtle)'}`,
          }}
          title="Toggle 110 BPM CPR Metronome (Sound & Visual Flash)"
        >
          <Heart size={13} className={cprMetronomeActive ? 'cpr-beat-active' : ''} />
          <span>110 BPM</span>
        </button>

        {/* Audio Mute Toggle */}
        <button
          onClick={toggleAudioMute}
          style={{
            padding: '5px 8px',
            borderRadius: 'var(--radius-sm)',
            backgroundColor: 'var(--bg-surface)',
            color: audioMuted ? 'var(--color-action-amber)' : 'var(--text-secondary)',
            border: '1px solid var(--border-subtle)'
          }}
          title={audioMuted ? 'Unmute Sound Synthesizer' : 'Mute Sounds'}
        >
          {audioMuted ? <VolumeX size={14} /> : <Volume2 size={14} />}
        </button>

        {/* Auto Simulation Play/Pause */}
        <button
          onClick={toggleAutoSimulation}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            padding: '5px 10px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '12px',
            fontWeight: 600,
            backgroundColor: isAutoSimulating ? 'var(--color-safe-green-subtle)' : 'var(--bg-surface)',
            color: isAutoSimulating ? 'var(--color-safe-green-bright)' : 'var(--text-secondary)',
            border: `1px solid ${isAutoSimulating ? 'var(--color-safe-green)' : 'var(--border-subtle)'}`
          }}
          title="Toggle Auto-Progression Simulation"
        >
          {isAutoSimulating ? <Pause size={13} /> : <Play size={13} />}
          <span>{isAutoSimulating ? 'Pause' : 'Auto'}</span>
        </button>

        {/* Speed multiplier */}
        <button
          onClick={() => setSimulationSpeed(simulationSpeed === 1 ? 2 : simulationSpeed === 2 ? 5 : 1)}
          style={{
            padding: '4px 8px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '11px',
            fontWeight: 700,
            fontFamily: 'var(--font-mono)',
            backgroundColor: 'var(--bg-surface)',
            color: simulationSpeed > 1 ? 'var(--color-ai-cyan)' : 'var(--text-muted)',
            border: '1px solid var(--border-subtle)'
          }}
          title="Toggle Simulation Speed (1x / 2x / 5x)"
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
            fontSize: '12px',
            fontWeight: 600,
            backgroundColor: 'var(--color-ai-subtle)',
            color: 'var(--color-ai-cyan)',
            border: '1px solid var(--border-ai)'
          }}
          title="Manually Trigger Next Emergency Milestone"
        >
          <FastForward size={13} />
          <span>Step</span>
        </button>

        {/* Reset */}
        <button
          onClick={resetDemo}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            padding: '5px 9px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '12px',
            fontWeight: 600,
            backgroundColor: 'var(--bg-surface)',
            color: 'var(--color-action-amber-bright)',
            border: '1px solid var(--border-subtle)'
          }}
          title="Reset Simulation to IDLE"
        >
          <RotateCcw size={13} />
          <span>Reset</span>
        </button>
      </div>
    </aside>
  );
};
