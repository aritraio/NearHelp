/* ==========================================================================
   NearHelp AI — Screen 5A: Active Rescue Navigation & Encrypted Medical ID Reveal
   File: src/components/responder/RescueNavigationScreen.tsx
   ========================================================================== */

import React, { useState, useEffect } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Navigation, 
  MapPin, 
  CornerUpRight, 
  Phone, 
  MessageSquare, 
  HeartPulse, 
  Zap, 
  Ambulance, 
  CheckCircle2, 
  Lock, 
  Eye, 
  EyeOff, 
  ChevronRight, 
  ChevronLeft 
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

const NAVIGATION_STEPS = [
  {
    instruction: "Head North-East on Ring Rd toward Webel Bhavan",
    distance: "120m",
    turn: "straight",
    eta: "2.5 mins",
    landmark: "Pass Sector V Metro Pillar #104"
  },
  {
    instruction: "Turn right onto Godrej Waterside Access Road",
    distance: "180m",
    turn: "right",
    eta: "1.8 mins",
    landmark: "AED Station on right at Webel Security Gate"
  },
  {
    instruction: "Proceed into Tower 1 Ground Concourse",
    distance: "80m",
    turn: "straight",
    eta: "0.8 mins",
    landmark: "Security desk checkpoint"
  },
  {
    instruction: "Arrive at Elevator Bank B — Victim on floor",
    distance: "40m",
    turn: "arrive",
    eta: "Arrived",
    landmark: "Ground floor elevator lobby"
  }
];

export const RescueNavigationScreen: React.FC = () => {
  const { 
    currentScenario, 
    incidentStatus, 
    activeResponderIndex,
    aedAttached,
    toggleAedAttached,
    simulateArrival, 
    handoverTo108, 
    resolveEmergency,
    turnByTurnStepIndex,
    setTurnByTurnStepIndex,
    nextTurnByTurnStep,
    prevTurnByTurnStep,
    cprMetronomeActive,
    toggleCprMetronome
  } = useDemoStore();

  const [medicalIdUnlocked, setMedicalIdUnlocked] = useState<boolean>(true);
  const [callToastActive, setCallToastActive] = useState<boolean>(false);
  const [smsToastActive, setSmsToastActive] = useState<boolean>(false);

  const activeResponder = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
  const currentStep = NAVIGATION_STEPS[turnByTurnStepIndex] || NAVIGATION_STEPS[0];

  // Auto-advance step if responder is arrived
  useEffect(() => {
    if (incidentStatus === 'RESPONDER_ARRIVED' || incidentStatus === 'HANDOVER_108' || incidentStatus === 'RESOLVED') {
      setTurnByTurnStepIndex(3);
    }
  }, [incidentStatus, setTurnByTurnStepIndex]);

  const handleSimulateCall = () => {
    soundEngine.playClick();
    setCallToastActive(true);
    setTimeout(() => setCallToastActive(false), 3000);
  };

  const handleSimulateSms = () => {
    soundEngine.playSuccessChime();
    setSmsToastActive(true);
    setTimeout(() => setSmsToastActive(false), 3000);
  };

  // SVG route calculation coordinates
  const stepPositions = [
    { x: 35, y: 155 },
    { x: 100, y: 95 },
    { x: 190, y: 80 },
    { x: 275, y: 45 }
  ];
  const responderPos = stepPositions[turnByTurnStepIndex] || stepPositions[0];

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: 'var(--text-primary)',
      padding: '12px',
      gap: '10px',
      overflowY: 'auto'
    }}>
      {/* 1. Turn-by-Turn Guidance Banner */}
      <div style={{
        backgroundColor: '#0C0D10',
        border: '1px solid rgba(0, 230, 118, 0.3)',
        borderRadius: 'var(--radius-md)',
        padding: '10px 12px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 2px 12px rgba(0, 230, 118, 0.15)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: 'rgba(0, 230, 118, 0.15)',
            border: '1px solid #00E676',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            {currentStep.turn === 'right' ? (
              <CornerUpRight size={20} color="#00E676" />
            ) : currentStep.turn === 'arrive' ? (
              <MapPin size={20} color="#FF2A44" />
            ) : (
              <Navigation size={20} color="#00E676" />
            )}
          </div>

          <div>
            <div style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF', lineHeight: '1.3' }}>
              {currentStep.instruction}
            </div>
            <div style={{ fontSize: '10px', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span>📍 {currentStep.landmark}</span>
              <span>•</span>
              <span style={{ color: '#00E676', fontWeight: 700 }}>{currentStep.distance}</span>
            </div>
          </div>
        </div>

        {/* Step Stepper controls for demo */}
        <div style={{ display: 'flex', gap: '3px', flexShrink: 0 }}>
          <button
            onClick={prevTurnByTurnStep}
            disabled={turnByTurnStepIndex === 0}
            style={{
              padding: '4px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(255,255,255,0.06)',
              border: '1px solid rgba(255,255,255,0.1)',
              color: turnByTurnStepIndex === 0 ? '#334155' : '#FFFFFF',
              cursor: turnByTurnStepIndex === 0 ? 'not-allowed' : 'pointer'
            }}
            title="Previous turn step"
          >
            <ChevronLeft size={14} />
          </button>
          <button
            onClick={nextTurnByTurnStep}
            disabled={turnByTurnStepIndex === NAVIGATION_STEPS.length - 1}
            style={{
              padding: '4px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(255,255,255,0.06)',
              border: '1px solid rgba(255,255,255,0.1)',
              color: turnByTurnStepIndex === NAVIGATION_STEPS.length - 1 ? '#334155' : '#00E676',
              cursor: turnByTurnStepIndex === NAVIGATION_STEPS.length - 1 ? 'not-allowed' : 'pointer'
            }}
            title="Next turn step"
          >
            <ChevronRight size={14} />
          </button>
        </div>
      </div>

      {/* 2. Interactive SVG Vector Route Map */}
      <div style={{
        position: 'relative',
        width: '100%',
        height: '180px',
        backgroundColor: '#07090D',
        borderRadius: 'var(--radius-md)',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        overflow: 'hidden',
        boxShadow: 'inset 0 0 24px rgba(0, 0, 0, 0.8)'
      }}>
        <svg viewBox="0 0 320 180" style={{ width: '100%', height: '100%' }}>
          {/* Background Grid & Road Layers */}
          <defs>
            <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
              <path d="M 20 0 L 0 0 0 20" fill="none" stroke="rgba(255,255,255,0.03)" strokeWidth="1" />
            </pattern>
            <linearGradient id="routeGradient" x1="0%" y1="100%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#00E676" />
              <stop offset="60%" stopColor="#00E5FF" />
              <stop offset="100%" stopColor="#FF2A44" />
            </linearGradient>
          </defs>

          <rect width="320" height="180" fill="url(#grid)" />

          {/* Local Area Road Network (Salt Lake Sector V) */}
          <path d="M 0 160 Q 120 140 180 170 T 320 150" fill="none" stroke="#1E232F" strokeWidth="16" />
          <path d="M 20 180 L 110 20 L 220 20 L 300 180" fill="none" stroke="#171A23" strokeWidth="12" />
          <path d="M 90 100 L 280 40" fill="none" stroke="#1E232F" strokeWidth="14" />
          <path d="M 170 180 L 180 0" fill="none" stroke="#151821" strokeWidth="10" />

          {/* Building Blocks */}
          <rect x="40" y="25" width="50" height="40" rx="3" fill="#10141D" stroke="rgba(255,255,255,0.05)" />
          <text x="65" y="48" fill="#475569" fontSize="6.5" textAnchor="middle" fontWeight="bold">WEBEL BHAVAN</text>

          <rect x="190" y="10" width="70" height="30" rx="3" fill="#141824" stroke="rgba(255,255,255,0.08)" />
          <text x="225" y="28" fill="#64748B" fontSize="6.5" textAnchor="middle" fontWeight="bold">GODREJ WATERSIDE T1</text>

          <rect x="110" y="110" width="60" height="35" rx="3" fill="#10141D" stroke="rgba(255,255,255,0.05)" />
          <text x="140" y="130" fill="#475569" fontSize="6.5" textAnchor="middle" fontWeight="bold">SECTOR V METRO</text>

          {/* Active Navigation Polyline */}
          <path 
            d="M 35 155 L 100 95 L 190 80 L 275 45" 
            fill="none" 
            stroke="url(#routeGradient)" 
            strokeWidth="4" 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            strokeDasharray="6,4"
          />

          {/* Nearby AED Node Marker (Webel Bhavan Gate) */}
          <g transform="translate(90, 42)">
            <circle r="7" fill="rgba(0, 229, 255, 0.2)" stroke="#00E5FF" strokeWidth="1.5" />
            <text x="0" y="3" fill="#00E5FF" fontSize="7" textAnchor="middle" fontWeight="900">⚡</text>
            <text x="0" y="-10" fill="#00E5FF" fontSize="6" textAnchor="middle" fontWeight="bold">AED (120m)</text>
          </g>

          {/* Target Victim Marker */}
          <g transform="translate(275, 45)">
            <circle r="12" fill="rgba(255, 42, 68, 0.2)" stroke="#FF2A44" strokeWidth="1.5" opacity="0.6">
              <animate attributeName="r" values="8;16;8" dur="2s" repeatCount="indefinite" />
              <animate attributeName="opacity" values="0.8;0.2;0.8" dur="2s" repeatCount="indefinite" />
            </circle>
            <circle r="6" fill="#FF2A44" />
            <text x="0" y="2" fill="#FFFFFF" fontSize="6" textAnchor="middle" fontWeight="900">SOS</text>
            <text x="0" y="-14" fill="#FF2A44" fontSize="6.5" textAnchor="middle" fontWeight="bold">VICTIM (LOBBY)</text>
          </g>

          {/* Active Responder Moving Pinpoint */}
          <g transform={`translate(${responderPos.x}, ${responderPos.y})`}>
            <circle r="10" fill="rgba(0, 230, 118, 0.25)" stroke="#00E676" strokeWidth="1.5">
              <animate attributeName="r" values="6;12;6" dur="1.5s" repeatCount="indefinite" />
            </circle>
            <circle r="5" fill="#00E676" />
            <text x="0" y="-12" fill="#00E676" fontSize="6.5" textAnchor="middle" fontWeight="bold">YOU ({activeResponder.name.split(' ')[0]})</text>
          </g>
        </svg>

        {/* Live Telemetry HUD Overlay */}
        <div style={{
          position: 'absolute',
          bottom: '6px',
          left: '6px',
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(6px)',
          padding: '3px 8px',
          borderRadius: 'var(--radius-xs)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          fontSize: '9.5px',
          color: '#94A3B8',
          display: 'flex',
          gap: '8px'
        }}>
          <span>⚡ Speed: <strong style={{ color: '#FFFFFF' }}>4.8 km/h</strong></span>
          <span>🛰️ GPS: <strong style={{ color: '#00E676' }}>±2.5m</strong></span>
          <span>🧭 Bearing: <strong style={{ color: '#00E5FF' }}>34° NE</strong></span>
        </div>

        <div style={{
          position: 'absolute',
          top: '6px',
          right: '6px',
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(6px)',
          padding: '3px 8px',
          borderRadius: 'var(--radius-xs)',
          border: '1px solid rgba(0, 230, 118, 0.3)',
          fontSize: '10px',
          color: '#00E676',
          fontWeight: 700
        }}>
          ETA: {currentStep.eta}
        </div>
      </div>

      {/* Toast Feedback for Phone & SMS simulator */}
      {callToastActive && (
        <div style={{
          backgroundColor: 'rgba(0, 230, 118, 0.2)',
          border: '1px solid #00E676',
          borderRadius: 'var(--radius-sm)',
          padding: '8px 10px',
          fontSize: '11px',
          color: '#00E676',
          display: 'flex',
          alignItems: 'center',
          gap: '6px'
        }}>
          <Phone size={13} />
          <span>Simulated Direct Call to {currentScenario.victim.emergencyContactName} ({currentScenario.victim.emergencyContactPhone})... Connected.</span>
        </div>
      )}

      {smsToastActive && (
        <div style={{
          backgroundColor: 'rgba(0, 229, 255, 0.2)',
          border: '1px solid #00E5FF',
          borderRadius: 'var(--radius-sm)',
          padding: '8px 10px',
          fontSize: '11px',
          color: '#00E5FF',
          display: 'flex',
          alignItems: 'center',
          gap: '6px'
        }}>
          <MessageSquare size={13} />
          <span>Encrypted SMS beacon sent to family: "Dr. Ananya Mukherjee is on-scene providing BLS care."</span>
        </div>
      )}

      {/* 3. Encrypted Medical ID Reveal Card */}
      <div style={{
        backgroundColor: '#0C0D10',
        border: '1px solid rgba(0, 229, 255, 0.25)',
        borderRadius: 'var(--radius-md)',
        padding: '12px',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Lock size={12} color="#00E5FF" />
            <span style={{ fontSize: '11px', fontWeight: 800, color: '#00E5FF', letterSpacing: '0.04em' }}>
              ENCRYPTED MEDICAL ID REVEAL
            </span>
          </div>

          <button
            onClick={() => setMedicalIdUnlocked(prev => !prev)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: 'var(--radius-xs)',
              padding: '2px 6px',
              color: '#94A3B8',
              fontSize: '10px',
              cursor: 'pointer'
            }}
          >
            {medicalIdUnlocked ? <EyeOff size={11} /> : <Eye size={11} />}
            <span>{medicalIdUnlocked ? 'Hide' : 'Reveal'}</span>
          </button>
        </div>

        {medicalIdUnlocked ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {/* Vitals Grid */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: '6px',
              backgroundColor: '#121418',
              padding: '8px',
              borderRadius: 'var(--radius-sm)'
            }}>
              <div>
                <div style={{ fontSize: '9px', color: '#64748B', fontWeight: 700 }}>BLOOD GROUP</div>
                <div style={{ fontSize: '14px', fontWeight: 900, color: '#FF2A44' }}>
                  {currentScenario.victim.bloodType}
                </div>
              </div>
              <div style={{ borderLeft: '1px solid rgba(255,255,255,0.06)', borderRight: '1px solid rgba(255,255,255,0.06)', paddingLeft: '6px' }}>
                <div style={{ fontSize: '9px', color: '#64748B', fontWeight: 700 }}>PACEMAKER</div>
                <div style={{ fontSize: '12px', fontWeight: 800, color: currentScenario.victim.hasPacemaker ? '#FF2A44' : '#00E676' }}>
                  {currentScenario.victim.hasPacemaker ? '⚠️ Active' : 'None'}
                </div>
              </div>
              <div style={{ paddingLeft: '4px' }}>
                <div style={{ fontSize: '9px', color: '#64748B', fontWeight: 700 }}>ALLERGIES</div>
                <div style={{ fontSize: '11px', fontWeight: 700, color: '#FFA000', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {currentScenario.victim.allergies.join(', ') || 'None'}
                </div>
              </div>
            </div>

            {/* Medical Conditions & Kin Emergency Contact */}
            <div style={{ fontSize: '11px', color: '#CBD5E1', display: 'flex', flexDirection: 'column', gap: '3px' }}>
              <div>
                <strong style={{ color: '#94A3B8' }}>Chronic Conditions: </strong>
                <span>{currentScenario.victim.medicalConditions.join(', ') || 'None'}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '2px' }}>
                <div>
                  <strong style={{ color: '#94A3B8' }}>Emergency Kin: </strong>
                  <span>{currentScenario.victim.emergencyContactName} ({currentScenario.victim.emergencyContactPhone})</span>
                </div>
                <div style={{ display: 'flex', gap: '4px' }}>
                  <button
                    onClick={handleSimulateCall}
                    style={{
                      padding: '4px 7px',
                      borderRadius: 'var(--radius-xs)',
                      backgroundColor: 'rgba(0, 230, 118, 0.15)',
                      border: '1px solid #00E676',
                      color: '#00E676',
                      fontSize: '10px',
                      fontWeight: 700,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '3px'
                    }}
                    title="Direct call to family contact"
                  >
                    <Phone size={11} />
                    <span>Call</span>
                  </button>
                  <button
                    onClick={handleSimulateSms}
                    style={{
                      padding: '4px 7px',
                      borderRadius: 'var(--radius-xs)',
                      backgroundColor: 'rgba(0, 229, 255, 0.15)',
                      border: '1px solid #00E5FF',
                      color: '#00E5FF',
                      fontSize: '10px',
                      fontWeight: 700,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '3px'
                    }}
                    title="Send automated rescue status SMS"
                  >
                    <MessageSquare size={11} />
                    <span>SMS</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div style={{ fontSize: '11px', color: '#64748B', fontStyle: 'italic', padding: '6px 0' }}>
            Medical ID encrypted. Tap 'Reveal' to unlock emergency clinical record.
          </div>
        )}
      </div>

      {/* 4. AED Status Bar & Metronome Sync */}
      <div style={{
        backgroundColor: '#0C0D10',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: 'var(--radius-md)',
        padding: '10px 12px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Zap size={16} color={aedAttached ? '#00E676' : '#FFA000'} />
          <div>
            <div style={{ fontSize: '11.5px', fontWeight: 800, color: aedAttached ? '#00E676' : '#FFFFFF' }}>
              {aedAttached ? '⚡ AED Attached — Rhythm: Normal Sinus' : 'Webel Bhavan AED: 120m away'}
            </div>
            <div style={{ fontSize: '10px', color: '#94A3B8' }}>
              {aedAttached ? 'Shock delivered. Resume CPR compressions.' : 'Security guard bringing unit to elevator lobby'}
            </div>
          </div>
        </div>

        <button
          onClick={toggleAedAttached}
          style={{
            padding: '5px 10px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: aedAttached ? 'rgba(0, 230, 118, 0.2)' : 'rgba(255, 160, 0, 0.2)',
            border: `1px solid ${aedAttached ? '#00E676' : '#FFA000'}`,
            color: aedAttached ? '#00E676' : '#FFA000',
            fontSize: '10.5px',
            fontWeight: 800,
            cursor: 'pointer'
          }}
        >
          {aedAttached ? 'AED Active' : 'Attach AED'}
        </button>
      </div>

      {/* 5. Responder Emergency Action Flow Bar */}
      <div style={{
        marginTop: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        paddingTop: '6px'
      }}>
        {incidentStatus !== 'RESPONDER_ARRIVED' && incidentStatus !== 'HANDOVER_108' && incidentStatus !== 'RESOLVED' && (
          <button
            onClick={simulateArrival}
            style={{
              padding: '13px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: '#00E676',
              color: '#000000',
              fontWeight: 900,
              fontSize: '13.5px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              boxShadow: '0 4px 16px rgba(0, 230, 118, 0.35)',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            <MapPin size={17} color="#000000" />
            <span>📍 I HAVE ARRIVED ON SCENE</span>
          </button>
        )}

        {incidentStatus === 'RESPONDER_ARRIVED' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px' }}>
            <button
              onClick={toggleCprMetronome}
              style={{
                padding: '12px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: cprMetronomeActive ? '#FF2A44' : '#1E232F',
                border: `1px solid ${cprMetronomeActive ? '#FF2A44' : 'rgba(255,255,255,0.15)'}`,
                color: '#FFFFFF',
                fontWeight: 800,
                fontSize: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                cursor: 'pointer'
              }}
            >
              <HeartPulse size={15} color="#FFFFFF" />
              <span>{cprMetronomeActive ? 'CPR 110 BPM (ON)' : 'Start CPR Sound'}</span>
            </button>

            <button
              onClick={handoverTo108}
              style={{
                padding: '12px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: '#2979FF',
                color: '#FFFFFF',
                fontWeight: 800,
                fontSize: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                border: 'none',
                boxShadow: '0 4px 12px rgba(41, 121, 255, 0.3)',
                cursor: 'pointer'
              }}
            >
              <Ambulance size={15} color="#FFFFFF" />
              <span>Handover to 108</span>
            </button>
          </div>
        )}

        {incidentStatus === 'HANDOVER_108' && (
          <button
            onClick={resolveEmergency}
            style={{
              padding: '13px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: '#00E676',
              color: '#000000',
              fontWeight: 900,
              fontSize: '13.5px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              boxShadow: '0 4px 16px rgba(0, 230, 118, 0.4)',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            <CheckCircle2 size={17} color="#000000" />
            <span>✨ MARK RESCUE RESOLVED (AMRI ICU)</span>
          </button>
        )}

        {incidentStatus === 'RESOLVED' && (
          <div style={{
            backgroundColor: 'rgba(0, 230, 118, 0.15)',
            border: '1px solid #00E676',
            borderRadius: 'var(--radius-md)',
            padding: '10px 12px',
            textAlign: 'center',
            fontSize: '12px',
            color: '#00E676',
            fontWeight: 800
          }}>
            ✅ Incident Successfully Resolved &amp; Handover Archived.
          </div>
        )}
      </div>
    </div>
  );
};
