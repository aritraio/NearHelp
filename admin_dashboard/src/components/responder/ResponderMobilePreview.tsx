/* ==========================================================================
   NearHelp AI — Responder Mobile Screen Foundation
   File: src/components/responder/ResponderMobilePreview.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  MapPin, 
  Navigation
} from 'lucide-react';

export const ResponderMobilePreview: React.FC = () => {
  const { 
    currentScenario, 
    incidentStatus, 
    acceptDispatch, 
    simulateArrival, 
    handoverTo108,
    resolveEmergency 
  } = useDemoStore();

  const responder = currentScenario.responders[0] || {
    name: 'Dr. Ananya Mukherjee',
    role: 'Cardiologist',
    distanceMeters: 420,
    etaMinutes: 2.5,
    trustScore: 99
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      padding: '16px',
      gap: '14px',
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div className="telemetry-dot" />
          <div>
            <div style={{ fontSize: '15px', fontWeight: 800, color: 'var(--color-safe-green-bright)' }}>
              Responder Active
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
              {responder.name} ({responder.role})
            </div>
          </div>
        </div>

        <div style={{
          fontSize: '11px',
          padding: '3px 8px',
          borderRadius: 'var(--radius-full)',
          backgroundColor: 'rgba(0, 230, 118, 0.15)',
          color: 'var(--color-safe-green-bright)',
          fontWeight: 700,
          border: '1px solid var(--border-safe)'
        }}>
          TRUST {responder.trustScore}%
        </div>
      </div>

      {/* Incident Alert / Navigation View */}
      {incidentStatus === 'IDLE' ? (
        <div style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '12px',
          textAlign: 'center'
        }}>
          <div style={{
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            backgroundColor: 'var(--bg-surface)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Navigation size={28} color="var(--color-safe-green-bright)" />
          </div>
          <div style={{ fontSize: '15px', fontWeight: 700 }}>Network Standby</div>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', maxWidth: '240px' }}>
            Listening for high-priority spatial emergency alerts within 3.0 km.
          </p>
        </div>
      ) : (
        <div style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
          overflowY: 'auto'
        }}>
          {/* High-Priority Alert Banner */}
          <div style={{
            backgroundColor: 'rgba(255, 23, 68, 0.15)',
            border: '1px solid var(--border-emergency)',
            borderRadius: 'var(--radius-md)',
            padding: '12px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
              <span style={{ fontSize: '11px', fontWeight: 800, color: 'var(--color-emergency-red-bright)' }}>
                🚨 RESCUE DISPATCH ALERT
              </span>
              <span style={{ fontSize: '11px', color: 'var(--color-action-amber-bright)', fontWeight: 700 }}>
                {responder.distanceMeters}m ({responder.etaMinutes}m ETA)
              </span>
            </div>
            <div style={{ fontSize: '13px', fontWeight: 700, marginBottom: '4px' }}>
              {currentScenario.title}
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <MapPin size={12} />
              <span>{currentScenario.locationName}</span>
            </div>
          </div>

          {/* Encrypted Medical Profile Summary */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '12px'
          }}>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-ai-cyan)', marginBottom: '6px' }}>
              ENCRYPTED MEDICAL ID REVEAL
            </div>
            <div style={{ fontSize: '12px', display: 'flex', flexDirection: 'column', gap: '3px' }}>
              <div><strong>Victim:</strong> {currentScenario.victim.name} ({currentScenario.victim.age}y/o)</div>
              <div><strong>Blood:</strong> <span style={{ color: 'var(--color-emergency-red-bright)', fontWeight: 700 }}>{currentScenario.victim.bloodType}</span></div>
              <div><strong>Allergies:</strong> {currentScenario.victim.allergies.join(', ') || 'None'}</div>
            </div>
          </div>

          {/* Action Buttons based on status */}
          <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {incidentStatus !== 'RESPONDER_ACCEPTED' && incidentStatus !== 'RESPONDER_ARRIVED' && incidentStatus !== 'HANDOVER_108' && incidentStatus !== 'RESOLVED' && (
              <button
                onClick={acceptDispatch}
                style={{
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--color-safe-green)',
                  color: '#ffffff',
                  fontWeight: 800,
                  fontSize: '13px',
                  boxShadow: 'var(--glow-safe)'
                }}
              >
                ✅ Accept Dispatch &amp; Route
              </button>
            )}

            {incidentStatus === 'RESPONDER_ACCEPTED' && (
              <button
                onClick={simulateArrival}
                style={{
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--color-action-amber)',
                  color: '#000000',
                  fontWeight: 800,
                  fontSize: '13px'
                }}
              >
                📍 Confirm Arrival On-Scene
              </button>
            )}

            {incidentStatus === 'RESPONDER_ARRIVED' && (
              <button
                onClick={handoverTo108}
                style={{
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--color-ai-blue)',
                  color: '#ffffff',
                  fontWeight: 800,
                  fontSize: '13px'
                }}
              >
                🚑 Handover to 108 Ambulance
              </button>
            )}

            {incidentStatus === 'HANDOVER_108' && (
              <button
                onClick={resolveEmergency}
                style={{
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--color-safe-green)',
                  color: '#ffffff',
                  fontWeight: 800,
                  fontSize: '13px'
                }}
              >
                ✨ Mark Incident Resolved
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
