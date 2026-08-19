/* ==========================================================================
   NearHelp AI — Screen 7: Command Center Telemetry Banner
   File: src/components/command/TelemetryDashboard.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Users, 
  Zap, 
  Sparkles, 
  Radio
} from 'lucide-react';

export const TelemetryDashboard: React.FC = () => {
  const { telemetry, incidentStatus, elapsedSeconds, searchRadiusKm } = useDemoStore();

  const isEmergencyActive = incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN';

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: '10px',
      padding: '12px 14px',
      backgroundColor: '#07090D',
      borderBottom: '1px solid rgba(255, 255, 255, 0.08)'
    }}>
      {/* 1. Active Incidents Metric */}
      <div style={{
        backgroundColor: isEmergencyActive ? 'rgba(255, 42, 68, 0.12)' : '#0F1218',
        borderRadius: '10px',
        padding: '10px 12px',
        border: `1px solid ${isEmergencyActive ? 'rgba(255, 42, 68, 0.35)' : 'rgba(255, 255, 255, 0.08)'}`,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
            ACTIVE EMERGENCIES
          </span>
          <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '8px', height: '8px' }} />
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '6px' }}>
          <span className="font-mono" style={{ fontSize: '24px', fontWeight: 900, color: '#FF2A44', lineHeight: 1 }}>
            {telemetry.activeIncidentsCount}
          </span>
          <span style={{ fontSize: '11px', color: '#FFA000', fontWeight: 700 }}>
            {isEmergencyActive ? `Live Active (T+${elapsedSeconds}s)` : '3 Across Kolkata'}
          </span>
        </div>

        <div style={{ fontSize: '10px', color: '#64748B', marginTop: '4px' }}>
          PostGIS Radius: <strong style={{ color: '#00E5FF' }}>{searchRadiusKm} km</strong>
        </div>
      </div>

      {/* 2. Network Bystanders / Responders */}
      <div style={{
        backgroundColor: '#0F1218',
        borderRadius: '10px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
            VERIFIED VOLUNTEERS
          </span>
          <Users size={14} color="#00E676" />
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '6px' }}>
          <span className="font-mono" style={{ fontSize: '24px', fontWeight: 900, color: '#00E676', lineHeight: 1 }}>
            {telemetry.availableVolunteersCount}
          </span>
          <span style={{ fontSize: '11px', color: '#00E676', fontWeight: 700 }}>
            Active Standby
          </span>
        </div>

        <div style={{ fontSize: '10px', color: '#64748B', marginTop: '4px' }}>
          CPR Certified: <strong style={{ color: '#E2E8F0' }}>88</strong> • Doctors: <strong style={{ color: '#E2E8F0' }}>24</strong>
        </div>
      </div>

      {/* 3. Dispatch Latency vs Municipal EMS (214x Speedup) */}
      <div style={{
        backgroundColor: '#0F1218',
        borderRadius: '10px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
            AVG DISPATCH LATENCY
          </span>
          <Zap size={14} color="#FFA000" />
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '6px' }}>
          <span className="font-mono" style={{ fontSize: '24px', fontWeight: 900, color: '#FFA000', lineHeight: 1 }}>
            {telemetry.avgDispatchLatencySeconds}s
          </span>
          <span style={{ fontSize: '10px', padding: '1px 6px', borderRadius: '4px', backgroundColor: 'rgba(0, 230, 118, 0.15)', color: '#00E676', fontWeight: 800 }}>
            214x FASTER
          </span>
        </div>

        <div style={{ fontSize: '10px', color: '#64748B', marginTop: '4px' }}>
          vs. 15.0m municipal EMS average
        </div>
      </div>

      {/* 4. RAG Clinical Protocol Accuracy */}
      <div style={{
        backgroundColor: '#0F1218',
        borderRadius: '10px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
            RAG MEDICAL ACCURACY
          </span>
          <Sparkles size={14} color="#00E5FF" />
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '6px' }}>
          <span className="font-mono" style={{ fontSize: '24px', fontWeight: 900, color: '#00E5FF', lineHeight: 1 }}>
            {telemetry.ragAccuracyScore}%
          </span>
          <span style={{ fontSize: '11px', color: '#00E5FF', fontWeight: 700 }}>
            WHO/IRC Aligned
          </span>
        </div>

        <div style={{ fontSize: '10px', color: '#64748B', marginTop: '4px' }}>
          Zero Hallucination Vector Guardrails
        </div>
      </div>

      {/* 5. Spatial Query & WebSocket Stream */}
      <div style={{
        backgroundColor: '#0F1218',
        borderRadius: '10px',
        padding: '10px 12px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#94A3B8', letterSpacing: '0.04em' }}>
            SPATIAL POSTGIS / WS
          </span>
          <Radio size={14} color="#00E5FF" />
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '6px' }}>
          <span className="font-mono" style={{ fontSize: '24px', fontWeight: 900, color: '#FFFFFF', lineHeight: 1 }}>
            {telemetry.spatialQueryLatencyMs}ms
          </span>
          <span style={{ fontSize: '11px', color: '#00E676', fontWeight: 700 }}>
            {telemetry.websocketConnectionsCount} nodes
          </span>
        </div>

        <div style={{ fontSize: '10px', color: '#64748B', marginTop: '4px' }}>
          Kolkata Geohash: <strong style={{ color: '#00E5FF' }}>tuyn4 (Salt Lake)</strong>
        </div>
      </div>
    </div>
  );
};
