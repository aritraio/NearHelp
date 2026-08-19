/* ==========================================================================
   NearHelp AI — Screen 2: Live AI Triage & Active SOS Screen
   File: src/components/victim/ActiveTriageScreen.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Clock, 
  Sparkles, 
  Radio, 
  Heart, 
  ArrowRight, 
  Send, 
  MessageSquare
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const ActiveTriageScreen: React.FC = () => {
  const {
    currentScenario,
    elapsedSeconds,
    searchRadiusKm,
    anonymousEmergencyMode,
    cancelSos,
    setVictimSubScreen
  } = useDemoStore();

  // Survival window countdown calculation
  const totalWindowSeconds = Math.round(currentScenario.survivalWindowMinutes * 60);
  const remainingSeconds = Math.max(0, totalWindowSeconds - elapsedSeconds);
  const remMins = Math.floor(remainingSeconds / 60);
  const remSecs = remainingSeconds % 60;
  const formattedWindow = `${remMins.toString().padStart(2, '0')}:${remSecs.toString().padStart(2, '0')}`;

  // 3-Tier Escalation States
  const tier1Active = true;
  const tier2Active = elapsedSeconds >= 12;
  const tier3Active = true;

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: '#FFFFFF',
      padding: '12px 14px 14px 14px',
      gap: '10px',
      overflowY: 'auto',
      overflowX: 'hidden',
      userSelect: 'none',
      position: 'relative'
    }}>
      {/* 1. Live Incident Status Banner */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '8px 12px',
        borderRadius: '12px',
        backgroundColor: 'rgba(255, 42, 68, 0.18)',
        border: '1px solid rgba(255, 42, 68, 0.45)',
        boxShadow: '0 4px 14px rgba(255, 42, 68, 0.25)',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div className="telemetry-dot telemetry-dot-emergency" />
          <div>
            <div style={{ fontSize: '12px', fontWeight: 900, color: '#FF2A44', letterSpacing: '0.04em' }}>
              EMERGENCY SOS BROADCAST ACTIVE
            </div>
            <div style={{ fontSize: '10px', color: '#CBD5E1' }}>
              {anonymousEmergencyMode ? 'Anonymous Protected Transmission' : 'Encrypted Community Dispatch'}
            </div>
          </div>
        </div>

        <div className="font-mono" style={{
          fontSize: '12px',
          fontWeight: 800,
          color: '#FFA000',
          backgroundColor: '#0D0F14',
          padding: '3px 8px',
          borderRadius: 'var(--radius-xs)',
          border: '1px solid rgba(255, 160, 0, 0.3)'
        }}>
          T+{elapsedSeconds}s
        </div>
      </div>

      {/* 2. AI Diagnostic Badge & Clinical Urgency Metrics */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '12px',
        border: '1px solid rgba(255, 42, 68, 0.35)',
        boxShadow: '0 6px 20px rgba(0, 0, 0, 0.6)',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{
            fontSize: '10px',
            fontWeight: 800,
            textTransform: 'uppercase',
            letterSpacing: '0.06em',
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(255, 42, 68, 0.25)',
            color: '#FF4D63',
            border: '1px solid rgba(255, 42, 68, 0.5)'
          }}>
            LEVEL {currentScenario.severity} TRIAGE CLASSIFICATION
          </span>

          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '10.5px', color: '#00E5FF', fontWeight: 700 }}>
            <Sparkles size={12} />
            <span>{currentScenario.aiConfidence}% Clinical Confidence</span>
          </div>
        </div>

        <div>
          <h2 style={{ fontSize: '15px', fontWeight: 900, color: '#FFFFFF', lineHeight: 1.2, margin: 0 }}>
            {currentScenario.severityLabel}
          </h2>
          <div style={{ fontSize: '11px', color: '#94A3B8', marginTop: '2px' }}>
            Suspected acute pathology based on multimodal intake telemetry
          </div>
        </div>

        {/* Clinical Urgency Metrics Bar */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '6px',
          marginTop: '2px'
        }}>
          {/* Platinum 5-Mins Countdown */}
          <div style={{
            backgroundColor: '#14171F',
            borderRadius: '8px',
            padding: '8px 10px',
            border: '1px solid rgba(255, 42, 68, 0.25)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '10px', color: '#94A3B8', marginBottom: '2px' }}>
              <Clock size={11} color="#FF2A44" />
              <span>Platinum Window</span>
            </div>
            <div className="font-mono" style={{ fontSize: '15px', fontWeight: 900, color: '#FF2A44' }}>
              {formattedWindow} <span style={{ fontSize: '9.5px', fontWeight: 600, color: '#CBD5E1' }}>remaining</span>
            </div>
          </div>

          {/* PostGIS Spatial SLA */}
          <div style={{
            backgroundColor: '#14171F',
            borderRadius: '8px',
            padding: '8px 10px',
            border: '1px solid rgba(0, 230, 118, 0.25)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '10px', color: '#94A3B8', marginBottom: '2px' }}>
              <Radio size={11} color="#00E676" />
              <span>Spatial Sweep</span>
            </div>
            <div className="font-mono" style={{ fontSize: '15px', fontWeight: 900, color: '#00E676' }}>
              {searchRadiusKm} km <span style={{ fontSize: '9.5px', fontWeight: 600, color: '#CBD5E1' }}>(PostGIS GiST)</span>
            </div>
          </div>
        </div>

        {/* Extracted Clinical Symptoms Tags */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginTop: '2px' }}>
          {currentScenario.reportedSymptoms.map((symp, idx) => (
            <span
              key={idx}
              style={{
                fontSize: '9.5px',
                padding: '2px 7px',
                borderRadius: '4px',
                backgroundColor: '#1E232E',
                color: '#CBD5E1',
                border: '1px solid rgba(255, 255, 255, 0.08)'
              }}
            >
              • {symp}
            </span>
          ))}
        </div>
      </div>

      {/* 3. 3-Tier Spatial Escalation Bar */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        flexShrink: 0
      }}>
        <div style={{ fontSize: '10px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
          3-TIER SPATIAL DISPATCH ESCALATION
        </div>

        {/* Tier 1 */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 8px',
          borderRadius: '8px',
          backgroundColor: tier1Active ? 'rgba(0, 230, 118, 0.12)' : '#14171E',
          border: `1px solid ${tier1Active ? 'rgba(0, 230, 118, 0.3)' : 'rgba(255, 255, 255, 0.05)'}`
        }}>
          <div style={{
            width: '20px',
            height: '20px',
            borderRadius: '50%',
            backgroundColor: tier1Active ? '#00E676' : '#2A303C',
            color: '#000',
            fontWeight: 800,
            fontSize: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            1
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '11px', fontWeight: 800, color: tier1Active ? '#00E676' : '#CBD5E1' }}>
              0–30s: Community Network Broadcast
            </div>
            <div style={{ fontSize: '9.5px', color: '#94A3B8' }}>
              Scanning radius 500m → 1.5km • {currentScenario.responders.length} CPR-certified volunteers alerted
            </div>
          </div>
        </div>

        {/* Tier 2 */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 8px',
          borderRadius: '8px',
          backgroundColor: tier2Active ? 'rgba(255, 160, 0, 0.12)' : '#14171E',
          border: `1px solid ${tier2Active ? 'rgba(255, 160, 0, 0.3)' : 'rgba(255, 255, 255, 0.05)'}`
        }}>
          <div style={{
            width: '20px',
            height: '20px',
            borderRadius: '50%',
            backgroundColor: tier2Active ? '#FFA000' : '#2A303C',
            color: '#000',
            fontWeight: 800,
            fontSize: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            2
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '11px', fontWeight: 800, color: tier2Active ? '#FFA000' : '#94A3B8' }}>
              30–60s: 108/112 Municipal Ambulance Gateway
            </div>
            <div style={{ fontSize: '9.5px', color: '#94A3B8' }}>
              {tier2Active ? 'Auto-routed to Salt Lake Ambulance Dispatch (Ambulance #WB-02-E-4921)' : 'Standby auto-trigger if bystander acceptance > 30s'}
            </div>
          </div>
        </div>

        {/* Tier 3 */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 8px',
          borderRadius: '8px',
          backgroundColor: tier3Active ? 'rgba(0, 229, 255, 0.12)' : '#14171E',
          border: `1px solid ${tier3Active ? 'rgba(0, 229, 255, 0.3)' : 'rgba(255, 255, 255, 0.05)'}`
        }}>
          <div style={{
            width: '20px',
            height: '20px',
            borderRadius: '50%',
            backgroundColor: '#00E5FF',
            color: '#000',
            fontWeight: 800,
            fontSize: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            3
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '11px', fontWeight: 800, color: '#00E5FF' }}>
              Active: Grounded AI Protocol &amp; CPR Metronome
            </div>
            <div style={{ fontSize: '9.5px', color: '#94A3B8' }}>
              Real-time RAG guidance active • 110 BPM CPR beat generator ready
            </div>
          </div>
        </div>
      </div>

      {/* 4. Emergency Contact Status (SMS & WhatsApp Beacon) */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Send size={12} color="#00E676" />
            <span>FAMILY EMERGENCY BEACON STATUS</span>
          </span>
          <span style={{ fontSize: '9.5px', color: '#00E676', fontWeight: 700 }}>Delivered</span>
        </div>

        <div style={{
          backgroundColor: '#14171E',
          borderRadius: '8px',
          padding: '8px 10px',
          border: '1px solid rgba(255, 255, 255, 0.06)',
          display: 'flex',
          flexDirection: 'column',
          gap: '4px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '11px' }}>
            <div style={{ fontWeight: 800, color: '#FFFFFF' }}>
              {anonymousEmergencyMode ? 'Confidential Guardian Contact' : currentScenario.victim.emergencyContactName}
            </div>
            <div className="font-mono" style={{ color: '#94A3B8', fontSize: '10px' }}>
              {anonymousEmergencyMode ? '+91 ••••• ••••5' : currentScenario.victim.emergencyContactPhone}
            </div>
          </div>

          <div style={{ fontSize: '10px', color: '#CBD5E1', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <MessageSquare size={11} color="#00E676" />
            <span>SMS &amp; WhatsApp beacon transmitted with live GPS stream link: </span>
            <span style={{ color: '#00E5FF', textDecoration: 'underline' }}>nearhelp.ai/live/sos-78a</span>
          </div>
        </div>
      </div>

      {/* 5. Matched Responders Preview */}
      <div style={{
        backgroundColor: '#0C0E12',
        borderRadius: '14px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        flexShrink: 0
      }}>
        <div style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8' }}>
          ALERTED FIRST-RESPONDERS IN SECTOR
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
          {currentScenario.responders.map((resp) => (
            <div
              key={resp.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '6px 8px',
                borderRadius: '8px',
                backgroundColor: '#14171F',
                border: '1px solid rgba(255, 255, 255, 0.06)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <img
                  src={resp.avatar}
                  alt={resp.name}
                  style={{ width: '28px', height: '28px', borderRadius: '50%', objectFit: 'cover' }}
                />
                <div>
                  <div style={{ fontSize: '11px', fontWeight: 800, color: '#FFFFFF' }}>{resp.name}</div>
                  <div style={{ fontSize: '9.5px', color: '#94A3B8' }}>{resp.role}</div>
                </div>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '11px', fontWeight: 800, color: '#00E676' }}>
                  {resp.distanceMeters}m ({resp.etaMinutes}m ETA)
                </div>
                <div style={{ fontSize: '9px', color: '#00E5FF', fontWeight: 700 }}>
                  CPR Verified • Trust {resp.trustScore}%
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 6. Primary Action: Open Grounded First-Aid Protocol (RAG Assist) */}
      <button
        onClick={() => {
          soundEngine.playClick();
          setVictimSubScreen('FIRST_AID');
        }}
        style={{
          width: '100%',
          height: '46px',
          borderRadius: 'var(--radius-full)',
          background: 'linear-gradient(135deg, #00E5FF 0%, #2979FF 100%)',
          color: '#000000',
          fontWeight: 900,
          fontSize: '13px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '8px',
          boxShadow: '0 4px 16px rgba(0, 229, 255, 0.4)',
          border: '1px solid rgba(255, 255, 255, 0.3)',
          cursor: 'pointer',
          flexShrink: 0
        }}
      >
        <Heart size={16} fill="#000000" />
        <span>Open First-Aid Guide &amp; CPR Beat</span>
        <ArrowRight size={16} strokeWidth={2.8} />
      </button>

      {/* Cancel SOS */}
      <button
        onClick={cancelSos}
        style={{
          padding: '8px',
          borderRadius: '10px',
          backgroundColor: '#12151C',
          color: '#FF2A44',
          border: '1px solid rgba(255, 42, 68, 0.3)',
          fontSize: '11px',
          fontWeight: 800,
          cursor: 'pointer',
          flexShrink: 0
        }}
      >
        Cancel SOS (False Alarm)
      </button>
    </div>
  );
};
