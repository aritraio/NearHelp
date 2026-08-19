/* ==========================================================================
   NearHelp AI — Victim Mobile Screen Foundation
   File: src/components/victim/VictimMobilePreview.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Heart, 
  Mic, 
  Camera, 
  Zap 
} from 'lucide-react';

export const VictimMobilePreview: React.FC = () => {
  const { 
    currentScenario, 
    incidentStatus, 
    elapsedSeconds, 
    searchRadiusKm,
    cprMetronomeActive,
    triggerSos, 
    cancelSos, 
    toggleCprMetronome 
  } = useDemoStore();

  const isEmergencyActive = incidentStatus !== 'IDLE';

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      padding: '16px',
      gap: '16px',
      color: 'var(--text-primary)'
    }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingBottom: '10px',
        borderBottom: '1px solid var(--border-subtle)'
      }}>
        <div>
          <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--color-emergency-red-bright)' }}>
            NearHelp AI
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
            Community SOS Network
          </div>
        </div>

        <div style={{
          fontSize: '11px',
          padding: '3px 8px',
          borderRadius: 'var(--radius-full)',
          backgroundColor: isEmergencyActive ? 'rgba(255, 23, 68, 0.2)' : 'rgba(0, 230, 118, 0.15)',
          color: isEmergencyActive ? 'var(--color-emergency-red-bright)' : 'var(--color-safe-green-bright)',
          fontWeight: 700,
          border: `1px solid ${isEmergencyActive ? 'var(--border-emergency)' : 'var(--border-safe)'}`
        }}>
          {isEmergencyActive ? 'INCIDENT LIVE' : 'NETWORK READY'}
        </div>
      </div>

      {/* Emergency Category Chips */}
      <div style={{ display: 'flex', gap: '6px', overflowX: 'auto', paddingBottom: '4px' }}>
        {[
          { label: '🩺 Medical', active: currentScenario.category === 'MEDICAL' },
          { label: '🚗 Accident', active: currentScenario.category === 'ACCIDENT' },
          { label: '🔥 Fire', active: currentScenario.category === 'FIRE' },
          { label: '🛡️ Crime', active: currentScenario.category === 'CRIME' },
        ].map((cat, idx) => (
          <div
            key={idx}
            style={{
              padding: '6px 10px',
              borderRadius: 'var(--radius-full)',
              fontSize: '11px',
              fontWeight: 600,
              backgroundColor: cat.active ? 'var(--color-emergency-red)' : 'var(--bg-surface)',
              color: cat.active ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${cat.active ? 'var(--border-emergency)' : 'var(--border-subtle)'}`,
              whiteSpace: 'nowrap'
            }}
          >
            {cat.label}
          </div>
        ))}
      </div>

      {/* Central SOS Button or Active Emergency State */}
      {!isEmergencyActive ? (
        <div style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '20px'
        }}>
          <button
            onClick={triggerSos}
            className="sos-breathing"
            style={{
              width: '180px',
              height: '180px',
              borderRadius: '50%',
              backgroundColor: 'var(--color-emergency-red)',
              color: '#ffffff',
              boxShadow: 'var(--glow-emergency)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px'
            }}
          >
            <Zap size={44} />
            <span style={{ fontSize: '24px', fontWeight: 900, letterSpacing: '0.05em' }}>SOS</span>
            <span style={{ fontSize: '10px', textTransform: 'uppercase', opacity: 0.9 }}>TAP FOR HELP</span>
          </button>

          <div style={{ textAlign: 'center', maxWidth: '240px' }}>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              1-Tap triggers instant Gemini AI triage and dispatches nearest CPR volunteers within 500m.
            </p>
          </div>
        </div>
      ) : (
        <div style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
          overflowY: 'auto'
        }}>
          {/* AI Diagnostic Badge */}
          <div style={{
            backgroundColor: 'rgba(255, 23, 68, 0.15)',
            border: '1px solid var(--border-emergency)',
            borderRadius: 'var(--radius-md)',
            padding: '12px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
              <span style={{ fontSize: '11px', fontWeight: 800, color: 'var(--color-emergency-red-bright)' }}>
                {currentScenario.severityLabel}
              </span>
              <span className="font-mono" style={{ fontSize: '11px', color: 'var(--color-action-amber-bright)', fontWeight: 700 }}>
                T+{elapsedSeconds}s
              </span>
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              AI Confidence: <strong>{currentScenario.aiConfidence}%</strong> • Platinum Window: <strong>{currentScenario.survivalWindowMinutes}m</strong>
            </div>
          </div>

          {/* Spatial Escalation Bar */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '10px 12px'
          }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>
              SPATIAL DISPATCH SCAN
            </div>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-safe-green-bright)' }}>
              Scanning radius: {searchRadiusKm} km (PostGIS GiST)
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
              {currentScenario.responders.length} verified responders alerted
            </div>
          </div>

          {/* CPR Metronome Quick Action */}
          {currentScenario.category === 'MEDICAL' && (
            <div style={{
              backgroundColor: 'var(--bg-surface)',
              border: `1px solid ${cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--border-subtle)'}`,
              borderRadius: 'var(--radius-md)',
              padding: '12px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Heart size={14} color="var(--color-emergency-red-bright)" />
                  <span>CPR Rhythm Metronome</span>
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>110 Compressions / Min</div>
              </div>

              <button
                onClick={toggleCprMetronome}
                className={cprMetronomeActive ? 'cpr-beat-active' : ''}
                style={{
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '11px',
                  fontWeight: 700,
                  backgroundColor: cprMetronomeActive ? 'var(--color-emergency-red)' : 'var(--bg-surface-elevated)',
                  color: '#ffffff',
                  border: '1px solid var(--border-medium)'
                }}
              >
                {cprMetronomeActive ? 'STOP' : 'START BEAT'}
              </button>
            </div>
          )}

          {/* First-Aid Protocol Card */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '12px'
          }}>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-ai-cyan)', marginBottom: '4px' }}>
              RAG FIRST-AID GUIDANCE
            </div>
            <div style={{ fontSize: '12px', fontWeight: 600, marginBottom: '6px' }}>
              {currentScenario.protocol.steps[0]?.title}
            </div>
            <p style={{ fontSize: '11px', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
              {currentScenario.protocol.steps[0]?.actionInstruction}
            </p>
          </div>

          {/* Cancel SOS */}
          <button
            onClick={cancelSos}
            style={{
              marginTop: 'auto',
              padding: '10px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--bg-surface)',
              color: 'var(--color-action-amber-bright)',
              border: '1px solid var(--border-subtle)',
              fontSize: '12px',
              fontWeight: 700
            }}
          >
            Cancel SOS (False Alarm)
          </button>
        </div>
      )}

      {/* Multimodal Intake Toolbar */}
      {!isEmergencyActive && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '8px',
          marginTop: 'auto'
        }}>
          <button
            style={{
              padding: '10px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              fontSize: '12px',
              color: 'var(--text-secondary)'
            }}
          >
            <Mic size={14} color="var(--color-ai-cyan)" />
            <span>Hold to Speak</span>
          </button>

          <button
            style={{
              padding: '10px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              fontSize: '12px',
              color: 'var(--text-secondary)'
            }}
          >
            <Camera size={14} color="var(--color-action-amber-bright)" />
            <span>Scene Photo</span>
          </button>
        </div>
      )}
    </div>
  );
};
