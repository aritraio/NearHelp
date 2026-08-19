/* ==========================================================================
   NearHelp AI — Screen 4: High-Priority Emergency Dispatch Alert Modal
   File: src/components/responder/ResponderAlertScreen.tsx
   ========================================================================== */

import React, { useState, useEffect } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  AlertTriangle, 
  MapPin, 
  Activity, 
  ShieldCheck, 
  Volume2, 
  VolumeX, 
  CheckCircle2, 
  XCircle, 
  UserCheck,
  Radio,
  Zap
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const ResponderAlertScreen: React.FC = () => {
  const { 
    currentScenario, 
    incidentStatus, 
    activeResponderIndex,
    setActiveResponderIndex,
    acceptDispatch, 
    declineDispatch,
    responderDeclined
  } = useDemoStore();

  const [sirenPlaying, setSirenPlaying] = useState<boolean>(false);
  const [pulseCount, setPulseCount] = useState<number>(0);

  const activeResponder = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];
  const isDeclined = responderDeclined && activeResponderIndex === 1;

  // Auto-pulse siren sound on initial alert if not declined
  useEffect(() => {
    if (incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN' && !responderDeclined) {
      soundEngine.playEmergencyAlert();
      const interval = window.setInterval(() => {
        setPulseCount(p => p + 1);
      }, 1200);
      return () => clearInterval(interval);
    }
  }, [incidentStatus, responderDeclined]);

  const toggleSirenSound = () => {
    soundEngine.playClick();
    if (!sirenPlaying) {
      soundEngine.playEmergencyAlert();
      setSirenPlaying(true);
      setTimeout(() => setSirenPlaying(false), 2000);
    } else {
      setSirenPlaying(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: 'var(--text-primary)',
      padding: '14px',
      gap: '12px',
      overflowY: 'auto',
      position: 'relative'
    }}>
      {/* 1. Full-Screen Emergency Flashing Banner & DND Override Status */}
      <div style={{
        backgroundColor: 'rgba(255, 42, 68, 0.12)',
        border: '1px solid rgba(255, 42, 68, 0.45)',
        borderRadius: 'var(--radius-md)',
        padding: '10px 12px',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        boxShadow: '0 0 20px rgba(255, 42, 68, 0.25)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{
              display: 'inline-flex',
              padding: '2px 6px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: '#FF2A44',
              color: '#FFFFFF',
              fontSize: '10px',
              fontWeight: 900,
              letterSpacing: '0.05em'
            }}>
              🚨 HIGH PRIORITY SOS
            </span>
            <span style={{ fontSize: '11px', color: '#94A3B8', fontWeight: 600 }}>
              FCM Channel 1
            </span>
          </div>

          <button
            onClick={toggleSirenSound}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              backgroundColor: sirenPlaying ? 'rgba(255, 42, 68, 0.3)' : 'rgba(255, 255, 255, 0.08)',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              borderRadius: 'var(--radius-xs)',
              padding: '3px 8px',
              color: sirenPlaying ? '#FF2A44' : '#FFFFFF',
              fontSize: '10.5px',
              fontWeight: 700,
              cursor: 'pointer'
            }}
            title="Simulate audible emergency siren tone"
          >
            {sirenPlaying ? <Volume2 size={12} /> : <VolumeX size={12} />}
            <span>{sirenPlaying ? 'Siren Active' : 'Test Siren'}</span>
          </button>
        </div>

        <div style={{
          fontSize: '10.5px',
          color: '#94A3B8',
          display: 'flex',
          alignItems: 'center',
          gap: '6px'
        }}>
          <Zap size={12} color="#00E5FF" />
          <span>Android DND Bypassed • Maximum Audible Priority • PostGIS Spatial Lock</span>
        </div>
      </div>

      {/* 2. Responder Persona Toggle Pills (Demonstrate Multi-Volunteer Routing) */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        backgroundColor: '#0C0D10',
        padding: '8px 10px',
        borderRadius: 'var(--radius-sm)',
        border: '1px solid rgba(255, 255, 255, 0.06)'
      }}>
        <div style={{ fontSize: '10.5px', fontWeight: 700, color: '#64748B', display: 'flex', alignItems: 'center', gap: '4px' }}>
          <UserCheck size={12} />
          <span>DISPATCH TARGET VOLUNTEER (SELECT TO TEST):</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px' }}>
          {currentScenario.responders.map((resp, idx) => {
            const isSelected = activeResponderIndex === idx;
            return (
              <button
                key={resp.id}
                onClick={() => {
                  soundEngine.playClick();
                  setActiveResponderIndex(idx);
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  padding: '6px 8px',
                  borderRadius: 'var(--radius-xs)',
                  backgroundColor: isSelected ? 'rgba(0, 230, 118, 0.15)' : 'rgba(255, 255, 255, 0.04)',
                  border: isSelected ? '1px solid #00E676' : '1px solid rgba(255, 255, 255, 0.08)',
                  color: isSelected ? '#00E676' : '#94A3B8',
                  fontSize: '11px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  textAlign: 'left'
                }}
              >
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: isSelected ? '#00E676' : '#64748B'
                }} />
                <div style={{ overflow: 'hidden' }}>
                  <div style={{ whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>{resp.name}</div>
                  <div style={{ fontSize: '9.5px', color: '#64748B', fontWeight: 500 }}>{resp.distanceMeters}m ({resp.etaMinutes}m ETA)</div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Auto-reroute toast if declined */}
      {isDeclined && (
        <div style={{
          backgroundColor: 'rgba(255, 160, 0, 0.15)',
          border: '1px solid rgba(255, 160, 0, 0.4)',
          borderRadius: 'var(--radius-sm)',
          padding: '8px 10px',
          fontSize: '11px',
          color: '#FFA000',
          display: 'flex',
          alignItems: 'center',
          gap: '6px'
        }}>
          <Radio size={13} color="#FFA000" />
          <span>Primary responder unavailable. Auto-rerouted to volunteer #2 ({activeResponder.name}).</span>
        </div>
      )}

      {/* 3. Primary Incident Summary Card */}
      <div style={{
        backgroundColor: '#0C0D10',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        borderRadius: 'var(--radius-md)',
        padding: '12px',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
      }}>
        {/* Severity Badge & Emergency Category */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{
            fontSize: '11px',
            padding: '3px 8px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: currentScenario.severity === 5 ? 'rgba(255, 42, 68, 0.2)' : 'rgba(255, 160, 0, 0.2)',
            color: currentScenario.severity === 5 ? '#FF2A44' : '#FFA000',
            fontWeight: 800,
            border: `1px solid ${currentScenario.severity === 5 ? 'rgba(255, 42, 68, 0.4)' : 'rgba(255, 160, 0, 0.4)'}`
          }}>
            {currentScenario.severityLabel}
          </span>

          <span style={{
            fontSize: '11px',
            color: '#00E5FF',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: '4px'
          }}>
            <Activity size={12} />
            <span>AI Conf: {currentScenario.aiConfidence}%</span>
          </span>
        </div>

        {/* Victim & Distance Highlights */}
        <div>
          <div style={{ fontSize: '14px', fontWeight: 800, color: '#FFFFFF', marginBottom: '2px' }}>
            {currentScenario.victim.name} ({currentScenario.victim.age}y/o {currentScenario.victim.gender})
          </div>
          <div style={{ fontSize: '12px', color: '#94A3B8', display: 'flex', alignItems: 'flex-start', gap: '4px' }}>
            <MapPin size={13} color="#FF2A44" style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{currentScenario.streetAddress}, {currentScenario.locationName}</span>
          </div>
        </div>

        {/* Spatial Distance & Estimated Time Matrix */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: '6px',
          backgroundColor: '#121418',
          padding: '8px',
          borderRadius: 'var(--radius-sm)',
          border: '1px solid rgba(255, 255, 255, 0.05)'
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '9.5px', color: '#64748B', fontWeight: 700 }}>DISTANCE</div>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#00E676' }}>{activeResponder.distanceMeters}m</div>
          </div>
          <div style={{ textAlign: 'center', borderLeft: '1px solid rgba(255,255,255,0.06)', borderRight: '1px solid rgba(255,255,255,0.06)' }}>
            <div style={{ fontSize: '9.5px', color: '#64748B', fontWeight: 700 }}>WALK ETA</div>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#FFA000' }}>{activeResponder.etaMinutes} mins</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '9.5px', color: '#64748B', fontWeight: 700 }}>SURVIVAL</div>
            <div style={{ fontSize: '13px', fontWeight: 800, color: '#FF2A44' }}>{currentScenario.survivalWindowMinutes}m limit</div>
          </div>
        </div>

        {/* Required Volunteer Skills Badges */}
        <div>
          <div style={{ fontSize: '10.5px', fontWeight: 700, color: '#64748B', marginBottom: '5px' }}>
            REQUIRED CLINICAL CAPABILITIES:
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
            <span style={{
              fontSize: '10px',
              padding: '3px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(0, 230, 118, 0.12)',
              color: '#00E676',
              fontWeight: 700,
              border: '1px solid rgba(0, 230, 118, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <ShieldCheck size={11} />
              <span>CPR / BLS Certified</span>
            </span>
            <span style={{
              fontSize: '10px',
              padding: '3px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(0, 229, 255, 0.12)',
              color: '#00E5FF',
              fontWeight: 700,
              border: '1px solid rgba(0, 229, 255, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <Zap size={11} />
              <span>AED Deployable</span>
            </span>
            <span style={{
              fontSize: '10px',
              padding: '3px 7px',
              borderRadius: 'var(--radius-xs)',
              backgroundColor: 'rgba(255, 160, 0, 0.12)',
              color: '#FFA000',
              fontWeight: 700,
              border: '1px solid rgba(255, 160, 0, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <AlertTriangle size={11} />
              <span>Emergency First-Aid</span>
            </span>
          </div>
        </div>

        {/* AI Diagnostic Extract */}
        <div style={{
          backgroundColor: 'rgba(0, 229, 255, 0.05)',
          border: '1px dashed rgba(0, 229, 255, 0.3)',
          borderRadius: 'var(--radius-sm)',
          padding: '8px 10px',
          fontSize: '11px',
          color: '#CBD5E1',
          lineHeight: '1.4'
        }}>
          <span style={{ color: '#00E5FF', fontWeight: 800 }}>🤖 Gemini AI Clinical Summary: </span>
          <span>{currentScenario.reportedSymptoms.join(' • ')}. Bystander reported agonal breathing; cardiac compressions required within 180 seconds.</span>
        </div>
      </div>

      {/* 4. Mini Spatial Vector Visualizer (Responder -> Victim Pinpoint) */}
      <div style={{
        backgroundColor: '#0C0D10',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: 'var(--radius-md)',
        padding: '10px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'relative'
      }}>
        {/* Responder Node */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            width: '32px',
            height: '32px',
            borderRadius: '50%',
            backgroundColor: 'rgba(0, 230, 118, 0.2)',
            border: '2px solid #00E676',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#00E676',
            fontWeight: 800,
            fontSize: '12px'
          }}>
            YOU
          </div>
          <div>
            <div style={{ fontSize: '11.5px', fontWeight: 700, color: '#FFFFFF' }}>{activeResponder.name}</div>
            <div style={{ fontSize: '10px', color: '#00E676' }}>GPS Fixed (±3m)</div>
          </div>
        </div>

        {/* Dynamic Vector Distance Line */}
        <div style={{
          flex: 1,
          margin: '0 12px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '2px'
        }}>
          <span style={{ fontSize: '10px', fontWeight: 700, color: '#FFA000' }}>
            {activeResponder.distanceMeters}m (34° NE)
          </span>
          <div style={{
            width: '100%',
            height: '2px',
            background: 'linear-gradient(90deg, #00E676 0%, #FFA000 50%, #FF2A44 100%)',
            position: 'relative'
          }}>
            <div style={{
              width: '6px',
              height: '6px',
              borderRadius: '50%',
              backgroundColor: '#FFA000',
              position: 'absolute',
              top: '-2px',
              left: `${(pulseCount * 15) % 90}%`,
              transition: 'left 0.8s ease'
            }} />
          </div>
        </div>

        {/* Victim Node */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '11.5px', fontWeight: 700, color: '#FFFFFF' }}>Victim Pin</div>
            <div style={{ fontSize: '10px', color: '#FF2A44' }}>Godrej Lobby</div>
          </div>
          <div style={{
            width: '32px',
            height: '32px',
            borderRadius: '50%',
            backgroundColor: 'rgba(255, 42, 68, 0.2)',
            border: '2px solid #FF2A44',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FF2A44',
            fontWeight: 800,
            fontSize: '12px'
          }}>
            SOS
          </div>
        </div>
      </div>

      {/* 5. Legal Good Samaritan Shield Notice */}
      <div style={{
        fontSize: '10px',
        color: '#64748B',
        display: 'flex',
        alignItems: 'center',
        gap: '5px',
        padding: '0 4px'
      }}>
        <ShieldCheck size={12} color="#00E676" />
        <span>Legal Protection: 100% Civil & Criminal Immunity under Section 134A Motor Vehicles Act 2019.</span>
      </div>

      {/* 6. Emergency Action Buttons (Accept & Decline) */}
      <div style={{
        marginTop: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        paddingTop: '8px'
      }}>
        <button
          onClick={acceptDispatch}
          style={{
            padding: '14px',
            borderRadius: 'var(--radius-md)',
            backgroundColor: '#00E676',
            color: '#000000',
            fontWeight: 900,
            fontSize: '14px',
            letterSpacing: '0.02em',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            boxShadow: '0 4px 16px rgba(0, 230, 118, 0.4)',
            border: 'none',
            cursor: 'pointer',
            transition: 'transform 0.15s ease'
          }}
        >
          <CheckCircle2 size={18} color="#000000" />
          <span>ACCEPT DISPATCH &amp; START ROUTE</span>
        </button>

        <button
          onClick={declineDispatch}
          style={{
            padding: '10px',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'rgba(255, 255, 255, 0.05)',
            color: '#94A3B8',
            fontWeight: 700,
            fontSize: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            cursor: 'pointer'
          }}
        >
          <XCircle size={14} color="#94A3B8" />
          <span>Decline (Auto Re-Route to Next Volunteer)</span>
        </button>
      </div>
    </div>
  );
};
