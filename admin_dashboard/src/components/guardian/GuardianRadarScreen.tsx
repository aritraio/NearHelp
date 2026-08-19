/* ==========================================================================
   NearHelp AI — Screen 1: Guardian Radar & Safe Zone (Dark Emerald AMOLED)
   File: src/components/guardian/GuardianRadarScreen.tsx
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

  // Handle 2.5-second Hold for SOS
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
      background: 'var(--guardian-dark-gradient)',
      color: '#FFFFFF',
      position: 'relative',
      padding: '16px 20px 18px 20px',
      overflowX: 'hidden',
      userSelect: 'none'
    }}>
      {/* 1. Top Exit Pill Slider ("Slide to exit") */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: '14px'
      }}>
        <div style={{
          position: 'relative',
          width: '184px',
          height: '38px',
          backgroundColor: 'rgba(10, 32, 22, 0.75)',
          backdropFilter: 'blur(14px)',
          WebkitBackdropFilter: 'blur(14px)',
          borderRadius: 'var(--radius-full)',
          padding: '3px',
          display: 'flex',
          alignItems: 'center',
          boxShadow: '0 4px 14px rgba(0, 0, 0, 0.4)',
          border: '1px solid rgba(0, 230, 118, 0.25)'
        }}>
          {/* Draggable/Clickable Thumb */}
          <div
            onClick={() => {
              soundEngine.playClick();
              setSlideOffset(prev => (prev === 0 ? 138 : 0));
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
              backgroundColor: '#00E676',
              color: '#000000',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              boxShadow: '0 0 12px rgba(0, 230, 118, 0.6)',
              transition: 'left 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)'
            }}
          >
            <ChevronRight size={18} strokeWidth={2.8} />
          </div>

          <span style={{
            width: '100%',
            textAlign: 'center',
            fontSize: '12px',
            fontWeight: 700,
            color: '#86EFAC',
            paddingLeft: '32px',
            letterSpacing: '0.02em'
          }}>
            Slide to exit
          </span>
        </div>
      </div>

      {/* 2. Locality Safety Header & Index */}
      <div style={{ textAlign: 'center', marginBottom: '16px' }}>
        <h1 style={{
          fontSize: '28px',
          fontWeight: 800,
          letterSpacing: '-0.03em',
          color: '#FFFFFF',
          marginBottom: '4px',
          lineHeight: 1.2
        }}>
          {localityName}
        </h1>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          fontSize: '13px',
          fontWeight: 700,
          color: '#00E676',
          padding: '2px 10px',
          borderRadius: 'var(--radius-full)',
          backgroundColor: 'rgba(0, 230, 118, 0.12)',
          border: '1px solid rgba(0, 230, 118, 0.3)'
        }}>
          <ShieldCheck size={15} />
          <span>Safety Index {safetyIndexScore}%</span>
        </div>
      </div>

      {/* 3. Destination Search Pill ("Where to today?") */}
      <div style={{
        backgroundColor: 'rgba(12, 36, 26, 0.85)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        borderRadius: 'var(--radius-full)',
        padding: '11px 18px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 6px 20px rgba(0, 0, 0, 0.5)',
        border: '1px solid rgba(0, 230, 118, 0.25)',
        marginBottom: '16px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1 }}>
          <Search size={17} color="#86EFAC" />
          <span style={{ fontSize: '13px', color: '#94A3B8', fontWeight: 500 }}>
            Where to today?
          </span>
        </div>
        <button 
          onClick={() => setScreenMode('CRISIS_MATRIX')}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#00E676',
            padding: '2px'
          }}
          title="Open Medical Intake"
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
            border: '1.5px solid rgba(0, 230, 118, 0.35)',
            boxShadow: '0 0 16px rgba(0, 230, 118, 0.15)'
          }} className="radar-ring-pulse" />

          <div style={{
            position: 'absolute',
            width: '68%',
            height: '68%',
            borderRadius: '50%',
            border: '1.5px solid rgba(0, 230, 118, 0.45)',
          }} />

          <div style={{
            position: 'absolute',
            width: '38%',
            height: '38%',
            borderRadius: '50%',
            border: '1.5px solid rgba(0, 230, 118, 0.55)',
          }} />

          {/* Rotating 360° Radar Gradient Cone */}
          <div style={{
            position: 'absolute',
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            background: 'var(--guardian-radar-sweep-cone)',
          }} className="guardian-radar-sweep" />

          {/* Nearby Responder/AED Pins on Radar */}
          <div style={{
            position: 'absolute',
            top: '32px',
            right: '48px',
            width: '12px',
            height: '12px',
            borderRadius: '50%',
            backgroundColor: '#00E676',
            border: '2px solid #FFFFFF',
            boxShadow: '0 0 10px #00E676'
          }} title="Dr. Ananya (420m away)" />

          <div style={{
            position: 'absolute',
            bottom: '44px',
            left: '52px',
            width: '12px',
            height: '12px',
            borderRadius: '50%',
            backgroundColor: 'var(--emergency-crimson)',
            border: '2px solid #FFFFFF',
            boxShadow: '0 0 10px var(--emergency-crimson)'
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
              width: '68px',
              height: '68px',
              borderRadius: '50%',
              backgroundColor: '#0B291C',
              boxShadow: '0 0 24px rgba(0, 230, 118, 0.4), inset 0 0 0 1.5px rgba(0, 230, 118, 0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 10,
              cursor: 'pointer',
              transition: 'transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1)'
            }}
            title="Tap for AI Voice SOS Triage"
          >
            <Mic size={28} color="#00E676" />
          </button>
        </div>

        {/* Voice Triage Prompt */}
        <div style={{
          marginTop: '10px',
          fontSize: '11.5px',
          fontWeight: 700,
          color: '#86EFAC',
          display: 'flex',
          alignItems: 'center',
          gap: '5px'
        }}>
          <Sparkles size={13} color="#00E676" />
          <span>Tap Mic for AI Voice SOS Triage</span>
        </div>
      </div>

      {/* 5. Bottom HOLD FOR SOS & Live Geodetic Telemetry */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '8px',
        marginTop: 'auto'
      }}>
        {/* Hold for SOS trigger button */}
        {!isEmergencyActive ? (
          <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
            <button
              onMouseDown={handleHoldStart}
              onMouseUp={handleHoldEnd}
              onTouchStart={handleHoldStart}
              onTouchEnd={handleHoldEnd}
              onClick={() => {
                setScreenMode('CRISIS_MATRIX');
                startCountdown();
              }}
              style={{
                position: 'relative',
                width: '100%',
                maxWidth: '260px',
                height: '52px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: 'var(--emergency-crimson)',
                color: '#FFFFFF',
                fontWeight: 800,
                fontSize: '15px',
                letterSpacing: '0.08em',
                boxShadow: isHoldingSos ? '0 0 30px rgba(255, 42, 68, 0.8)' : '0 6px 24px rgba(255, 42, 68, 0.45)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                overflow: 'hidden',
                cursor: 'pointer',
                border: '1px solid rgba(255, 255, 255, 0.2)'
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
                  backgroundColor: 'rgba(255, 255, 255, 0.35)',
                  transition: 'width 0.05s linear'
                }} />
              )}
              <Zap size={18} fill="#FFFFFF" />
              <span>HOLD FOR SOS</span>
            </button>
            <div style={{ fontSize: '10.5px', color: '#94A3B8', marginTop: '4px' }}>
              Hold 2.5s or tap to open Medical Intake
            </div>
          </div>
        ) : (
          <div style={{
            width: '100%',
            backgroundColor: 'rgba(255, 42, 68, 0.16)',
            border: '1px solid rgba(255, 42, 68, 0.4)',
            borderRadius: 'var(--radius-md)',
            padding: '10px 14px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div>
              <div style={{ fontSize: '12px', fontWeight: 800, color: 'var(--emergency-crimson)' }}>
                SOS ACTIVE — SCANNING 1.5 KM
              </div>
              <div style={{ fontSize: '11px', color: '#94A3B8' }}>
                {currentScenario.responders.length} nearby responders dispatched
              </div>
            </div>
            <button
              onClick={cancelSos}
              style={{
                fontSize: '11px',
                fontWeight: 800,
                color: '#FFFFFF',
                padding: '5px 10px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: 'var(--emergency-crimson)',
                border: 'none'
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
            marginTop: '2px'
          }}
        >
          <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em', color: '#00E676' }}>
            CHECK IN
          </span>
          <ChevronDown size={14} color="#00E676" style={{ marginTop: '-2px' }} />
          <ChevronDown size={14} color="#00E676" style={{ marginTop: '-8px' }} />
        </div>

        {/* Live Geodetic Telemetry Coordinates */}
        <div className="font-mono" style={{
          fontSize: '11px',
          color: '#86EFAC',
          opacity: 0.9,
          letterSpacing: '0.03em'
        }}>
          {currentScenario.coordinates[0].toFixed(4)}° N {currentScenario.coordinates[1].toFixed(5)}° E
        </div>
      </div>
    </div>
  );
};
