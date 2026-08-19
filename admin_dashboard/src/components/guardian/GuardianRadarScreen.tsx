/* ==========================================================================
   NearHelp AI — Screen 1: Guardian Radar & Safe Zone (GuardianRadarScreen.kt)
   File: src/components/guardian/GuardianRadarScreen.tsx
   Design Spec: docs/design.md (Left Screen Reference)
   ========================================================================== */

import React, { useState, useRef, useEffect } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Mic, 
  Search, 
  SlidersHorizontal, 
  ChevronRight, 
  ChevronDown, 
  ShieldCheck, 
  Sparkles,
  Zap
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const GuardianRadarScreen: React.FC = () => {
  const {
    currentScenario,
    incidentStatus,
    localityName,
    safetyIndexScore,
    triggerSos,
    cancelSos,
    setScreenMode,
    startCountdown
  } = useDemoStore();

  const [slideOffset, setSlideOffset] = useState<number>(0);
  const [isHoldingSos, setIsHoldingSos] = useState<boolean>(false);
  const [holdProgress, setHoldProgress] = useState<number>(0);
  const holdIntervalRef = useRef<number | null>(null);

  const isEmergencyActive = incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN';

  // Handle 3-second Hold for SOS
  const handleHoldStart = () => {
    soundEngine.playClick();
    setIsHoldingSos(true);
    setHoldProgress(0);

    const startTime = Date.now();
    holdIntervalRef.current = window.setInterval(() => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(100, (elapsed / 2500) * 100);
      setHoldProgress(progress);

      if (progress >= 100) {
        if (holdIntervalRef.current) {
          clearInterval(holdIntervalRef.current);
          holdIntervalRef.current = null;
        }
        setIsHoldingSos(false);
        setHoldProgress(0);
        triggerSos();
      }
    }, 50);
  };

  const handleHoldEnd = () => {
    if (holdIntervalRef.current) {
      clearInterval(holdIntervalRef.current);
      holdIntervalRef.current = null;
    }
    setIsHoldingSos(false);
    setHoldProgress(0);
  };

  useEffect(() => {
    return () => {
      if (holdIntervalRef.current) {
        clearInterval(holdIntervalRef.current);
      }
    };
  }, []);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      background: 'var(--guardian-gradient)',
      color: 'var(--text-primary-dark)',
      position: 'relative',
      padding: '12px 18px 16px 18px',
      overflowX: 'hidden',
      userSelect: 'none'
    }}>
      {/* 1. Top Exit Pill Slider ("Slide to exit") */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: '10px'
      }}>
        <div style={{
          position: 'relative',
          width: '180px',
          height: '38px',
          backgroundColor: 'rgba(255, 255, 255, 0.75)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: 'var(--radius-full)',
          padding: '3px',
          display: 'flex',
          alignItems: 'center',
          boxShadow: '0 2px 10px rgba(0, 0, 0, 0.05)',
          border: '1px solid rgba(255, 255, 255, 0.9)'
        }}>
          {/* Draggable/Clickable Thumb */}
          <div
            onClick={() => {
              soundEngine.playClick();
              setSlideOffset(prev => (prev === 0 ? 134 : 0));
              if (slideOffset === 0) {
                setTimeout(() => setSlideOffset(0), 1200);
              }
            }}
            style={{
              position: 'absolute',
              left: `${3 + slideOffset}px`,
              width: '32px',
              height: '32px',
              borderRadius: '50%',
              backgroundColor: '#0F172A',
              color: '#FFFFFF',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              boxShadow: '0 2px 8px rgba(15, 23, 42, 0.3)',
              transition: 'left 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)'
            }}
          >
            <ChevronRight size={18} />
          </div>

          <span style={{
            width: '100%',
            textAlign: 'center',
            fontSize: '12px',
            fontWeight: 600,
            color: 'var(--text-secondary-muted)',
            paddingLeft: '32px'
          }}>
            Slide to exit
          </span>
        </div>
      </div>

      {/* 2. Locality Safety Header & Index */}
      <div style={{ textAlign: 'center', marginBottom: '14px' }}>
        <h1 style={{
          fontSize: '26px',
          fontWeight: 800,
          letterSpacing: '-0.03em',
          color: 'var(--text-primary-dark)',
          marginBottom: '2px',
          lineHeight: 1.2
        }}>
          {localityName}
        </h1>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '5px',
          fontSize: '13px',
          fontWeight: 700,
          color: '#15803D'
        }}>
          <ShieldCheck size={15} />
          <span>Safety Index {safetyIndexScore}%</span>
        </div>
      </div>

      {/* 3. Destination Search Pill ("Where to today?") */}
      <div style={{
        backgroundColor: 'var(--guardian-search-pill)',
        backdropFilter: 'blur(12px)',
        WebkitBackdropFilter: 'blur(12px)',
        borderRadius: 'var(--radius-full)',
        padding: '10px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 4px 18px rgba(0, 0, 0, 0.04)',
        border: '1px solid rgba(255, 255, 255, 0.95)',
        marginBottom: '16px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1 }}>
          <Search size={17} color="var(--text-secondary-muted)" />
          <span style={{ fontSize: '13px', color: 'var(--text-secondary-muted)', fontWeight: 500 }}>
            Where to today?
          </span>
        </div>
        <button 
          onClick={() => setScreenMode('CRISIS_MATRIX')}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--text-secondary-muted)',
            padding: '2px'
          }}
          title="Open Crisis Matrix"
        >
          <SlidersHorizontal size={16} />
        </button>
      </div>

      {/* 4. Interactive Radar Map Visualizer with Center Voice SOS Mic */}
      <div style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        minHeight: '230px'
      }}>
        {/* Radar Canvas Container */}
        <div style={{
          position: 'relative',
          width: '230px',
          height: '230px',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          {/* Concentric Distance Rings */}
          <div style={{
            position: 'absolute',
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            border: '1.5px solid rgba(52, 199, 89, 0.45)',
          }} className="radar-ring-pulse" />

          <div style={{
            position: 'absolute',
            width: '68%',
            height: '68%',
            borderRadius: '50%',
            border: '1.5px solid rgba(52, 199, 89, 0.55)',
          }} />

          <div style={{
            position: 'absolute',
            width: '38%',
            height: '38%',
            borderRadius: '50%',
            border: '1.5px solid rgba(52, 199, 89, 0.65)',
          }} />

          {/* Rotating 360° Radar Gradient Cone */}
          <div style={{
            position: 'absolute',
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            background: 'conic-gradient(from 0deg at 50% 50%, rgba(52, 199, 89, 0) 0deg, rgba(52, 199, 89, 0.1) 270deg, rgba(52, 199, 89, 0.45) 360deg)',
          }} className="guardian-radar-sweep" />

          {/* Nearby Responder/AED Pins on Radar */}
          <div style={{
            position: 'absolute',
            top: '32px',
            right: '48px',
            width: '10px',
            height: '10px',
            borderRadius: '50%',
            backgroundColor: '#0F172A',
            border: '2px solid #ffffff',
            boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
          }} title="Dr. Ananya (420m away)" />

          <div style={{
            position: 'absolute',
            bottom: '44px',
            left: '52px',
            width: '10px',
            height: '10px',
            borderRadius: '50%',
            backgroundColor: 'var(--emergency-crimson)',
            border: '2px solid #ffffff',
            boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
          }} title="Webel Bhavan AED (180m)" />

          {/* Center Pulsing Frosted Glass Micro-Card with Microphone */}
          <button
            onClick={() => {
              soundEngine.playEmergencyAlert();
              setScreenMode('CRISIS_MATRIX');
              startCountdown();
            }}
            style={{
              position: 'relative',
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              boxShadow: '0 8px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(255, 255, 255, 0.9)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 10,
              cursor: 'pointer',
              transition: 'transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1)'
            }}
            title="Tap for AI Voice SOS Triage"
          >
            <Mic size={28} color="#0F172A" />
          </button>
        </div>

        {/* Voice Triage Prompt */}
        <div style={{
          marginTop: '8px',
          fontSize: '11px',
          fontWeight: 700,
          color: 'var(--text-secondary-muted)',
          display: 'flex',
          alignItems: 'center',
          gap: '4px'
        }}>
          <Sparkles size={12} color="#15803D" />
          <span>Tap Mic for AI Voice SOS Triage</span>
        </div>
      </div>

      {/* 5. Bottom HOLD FOR SOS & Live Geodetic Telemetry */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '6px',
        marginTop: 'auto'
      }}>
        {/* Hold for SOS trigger button */}
        {!isEmergencyActive ? (
          <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <button
              onMouseDown={handleHoldStart}
              onMouseUp={handleHoldEnd}
              onTouchStart={handleHoldStart}
              onTouchEnd={handleHoldEnd}
              onClick={() => {
                // If single tapped, start 3s countdown on Crisis Screen
                setScreenMode('CRISIS_MATRIX');
                startCountdown();
              }}
              style={{
                position: 'relative',
                width: '210px',
                height: '52px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: isHoldingSos ? 'var(--emergency-crimson)' : 'var(--emergency-crimson)',
                color: '#ffffff',
                fontWeight: 800,
                fontSize: '15px',
                letterSpacing: '0.08em',
                boxShadow: isHoldingSos ? 'var(--card-neomorphic-active)' : '0 6px 20px rgba(229, 37, 56, 0.35)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                overflow: 'hidden',
                cursor: 'pointer'
              }}
            >
              {/* Progress Fill during hold */}
              {isHoldingSos && (
                <div style={{
                  position: 'absolute',
                  left: 0,
                  top: 0,
                  height: '100%',
                  width: `${holdProgress}%`,
                  backgroundColor: 'rgba(255, 255, 255, 0.3)',
                  transition: 'width 0.05s linear'
                }} />
              )}
              <Zap size={18} />
              <span>HOLD FOR SOS</span>
            </button>
            <div style={{ fontSize: '10px', color: 'var(--text-secondary-muted)', marginTop: '4px' }}>
              Hold 2.5s or tap to open Crisis Matrix
            </div>
          </div>
        ) : (
          <div style={{
            width: '100%',
            backgroundColor: 'rgba(229, 37, 56, 0.12)',
            border: '1px solid rgba(229, 37, 56, 0.4)',
            borderRadius: 'var(--radius-md)',
            padding: '8px 12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div>
              <div style={{ fontSize: '11px', fontWeight: 800, color: 'var(--emergency-crimson)' }}>
                🚨 SOS ACTIVE — SCANNING 1.5KM
              </div>
              <div style={{ fontSize: '10px', color: 'var(--text-secondary-muted)' }}>
                {currentScenario.responders.length} nearby responders dispatched
              </div>
            </div>
            <button
              onClick={cancelSos}
              style={{
                fontSize: '11px',
                fontWeight: 700,
                color: 'var(--emergency-crimson)',
                padding: '4px 8px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: '#ffffff'
              }}
            >
              Cancel
            </button>
          </div>
        )}

        {/* Check In prompt */}
        <div 
          onClick={() => {
            soundEngine.playSuccessChime();
          }}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            cursor: 'pointer',
            marginTop: '4px'
          }}
        >
          <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em', color: 'var(--text-primary-dark)' }}>
            CHECK IN
          </span>
          <ChevronDown size={14} color="var(--text-secondary-muted)" style={{ marginTop: '-2px' }} />
          <ChevronDown size={14} color="var(--text-secondary-muted)" style={{ marginTop: '-8px' }} />
        </div>

        {/* Live Geodetic Telemetry Coordinates */}
        <div className="font-mono" style={{
          fontSize: '10.5px',
          color: 'var(--text-secondary-muted)',
          opacity: 0.85,
          letterSpacing: '0.02em'
        }}>
          {currentScenario.coordinates[0].toFixed(4)}° N {currentScenario.coordinates[1].toFixed(5)}° E
        </div>
      </div>
    </div>
  );
};
