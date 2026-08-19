/* ==========================================================================
   NearHelp AI — Screen 7: Real-Time Incident Feed Table (Command Center)
   File: src/components/command/IncidentFeedTable.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import type { SeverityLevel, IncidentFeedItem } from '../../mock/types';
import { 
  Filter, 
  AlertTriangle, 
  Clock, 
  FileText, 
  Ambulance, 
  CheckCircle, 
  Eye 
} from 'lucide-react';

export const IncidentFeedTable: React.FC = () => {
  const {
    incidentFeed,
    incidentFilterSeverity,
    incidentFilterStatus,
    selectedIncidentId,
    setIncidentFilterSeverity,
    setIncidentFilterStatus,
    setSelectedIncidentId,
    openClinicalReport,
    setScreenMode,
  } = useDemoStore();

  // Filter incidents
  const filteredIncidents = incidentFeed.filter((item: IncidentFeedItem) => {
    if (incidentFilterSeverity !== 'ALL' && item.severity !== incidentFilterSeverity) {
      return false;
    }
    if (incidentFilterStatus !== 'ALL') {
      if (incidentFilterStatus === 'ACTIVE' && ['IDLE', 'RESOLVED'].includes(item.status)) return false;
      if (incidentFilterStatus === 'RESOLVED' && item.status !== 'RESOLVED') return false;
      if (incidentFilterStatus === 'EN_ROUTE' && !['RESPONDER_ACCEPTED', 'RESPONDER_EN_ROUTE'].includes(item.status)) return false;
    }
    return true;
  });

  const renderSeverityBadge = (level: SeverityLevel) => {
    switch (level) {
      case 5:
        return (
          <span style={{
            fontSize: '10px',
            fontWeight: 800,
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(255, 42, 68, 0.2)',
            color: '#FF2A44',
            border: '1px solid #FF2A44',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '3px'
          }}>
            <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '5px', height: '5px' }} />
            <span>L5 CRITICAL</span>
          </span>
        );
      case 4:
        return (
          <span style={{
            fontSize: '10px',
            fontWeight: 800,
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(255, 160, 0, 0.2)',
            color: '#FFA000',
            border: '1px solid #FFA000'
          }}>
            L4 URGENT
          </span>
        );
      case 3:
        return (
          <span style={{
            fontSize: '10px',
            fontWeight: 800,
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(0, 229, 255, 0.15)',
            color: '#00E5FF',
            border: '1px solid #00E5FF'
          }}>
            L3 MODERATE
          </span>
        );
      default:
        return (
          <span style={{
            fontSize: '10px',
            fontWeight: 800,
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(0, 230, 118, 0.15)',
            color: '#00E676',
            border: '1px solid #00E676'
          }}>
            L{level} MINOR
          </span>
        );
    }
  };

  const renderStatusBadge = (status: string) => {
    if (status === 'RESOLVED') {
      return (
        <span style={{ fontSize: '10.5px', color: '#00E676', fontWeight: 800, display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
          <CheckCircle size={11} />
          <span>RESOLVED</span>
        </span>
      );
    }
    if (status === 'HANDOVER_108') {
      return (
        <span style={{ fontSize: '10.5px', color: '#00E5FF', fontWeight: 800, display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
          <Ambulance size={11} />
          <span>108 HANDOVER</span>
        </span>
      );
    }
    if (status === 'RESPONDER_ARRIVED') {
      return (
        <span style={{ fontSize: '10.5px', color: '#00E676', fontWeight: 800 }}>
          📍 ON-SCENE
        </span>
      );
    }
    if (status === 'RESPONDER_ACCEPTED' || status === 'RESPONDER_EN_ROUTE') {
      return (
        <span style={{ fontSize: '10.5px', color: '#FFA000', fontWeight: 800 }}>
          🚑 RESPONDING
        </span>
      );
    }
    return (
      <span style={{ fontSize: '10.5px', color: '#FF2A44', fontWeight: 800, display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
        <AlertTriangle size={11} />
        <span>ACTIVE SOS</span>
      </span>
    );
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      flex: 1,
      backgroundColor: '#07090D',
      overflow: 'hidden'
    }}>
      {/* Table Filter Toolbar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '10px 14px',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        backgroundColor: '#0C0E14',
        flexWrap: 'wrap',
        gap: '8px'
      }}>
        {/* Severity Filter Pills */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#64748B', display: 'flex', alignItems: 'center', gap: '3px' }}>
            <Filter size={11} />
            <span>SEVERITY:</span>
          </span>

          {[
            { id: 'ALL' as const, label: 'All' },
            { id: 5 as SeverityLevel, label: 'L5 Critical' },
            { id: 4 as SeverityLevel, label: 'L4 Urgent' },
            { id: 3 as SeverityLevel, label: 'L3 Moderate' },
          ].map((item) => {
            const isSelected = incidentFilterSeverity === item.id;
            return (
              <button
                key={String(item.id)}
                onClick={() => setIncidentFilterSeverity(item.id)}
                style={{
                  padding: '3px 8px',
                  borderRadius: 'var(--radius-full)',
                  backgroundColor: isSelected ? 'rgba(255, 42, 68, 0.2)' : '#14171F',
                  color: isSelected ? '#FF2A44' : '#94A3B8',
                  border: isSelected ? '1px solid #FF2A44' : '1px solid rgba(255, 255, 255, 0.08)',
                  fontSize: '10.5px',
                  fontWeight: isSelected ? 800 : 500,
                  cursor: 'pointer'
                }}
              >
                {item.label}
              </button>
            );
          })}
        </div>

        {/* Status Filter Pills */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 800, color: '#64748B' }}>STATUS:</span>
          {[
            { id: 'ALL', label: 'All' },
            { id: 'ACTIVE', label: 'Active' },
            { id: 'EN_ROUTE', label: 'In Transit' },
            { id: 'RESOLVED', label: 'Resolved' },
          ].map((item) => {
            const isSelected = incidentFilterStatus === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setIncidentFilterStatus(item.id)}
                style={{
                  padding: '3px 8px',
                  borderRadius: 'var(--radius-full)',
                  backgroundColor: isSelected ? 'rgba(0, 230, 118, 0.2)' : '#14171F',
                  color: isSelected ? '#00E676' : '#94A3B8',
                  border: isSelected ? '1px solid #00E676' : '1px solid rgba(255, 255, 255, 0.08)',
                  fontSize: '10.5px',
                  fontWeight: isSelected ? 800 : 500,
                  cursor: 'pointer'
                }}
              >
                {item.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Real-time Incidents Table */}
      <div style={{ flex: 1, overflowX: 'auto', overflowY: 'auto' }}>
        <table style={{
          width: '100%',
          borderCollapse: 'collapse',
          fontSize: '11.5px',
          textAlign: 'left'
        }}>
          <thead>
            <tr style={{
              backgroundColor: '#0A0D12',
              color: '#64748B',
              fontSize: '10px',
              fontWeight: 800,
              textTransform: 'uppercase',
              letterSpacing: '0.04em',
              borderBottom: '1px solid rgba(255, 255, 255, 0.08)'
            }}>
              <th style={{ padding: '10px 14px' }}>Incident Code</th>
              <th style={{ padding: '10px 10px' }}>Severity</th>
              <th style={{ padding: '10px 12px' }}>Location &amp; Locality</th>
              <th style={{ padding: '10px 12px' }}>Condition &amp; AI Triage</th>
              <th style={{ padding: '10px 12px' }}>Assigned Volunteer</th>
              <th style={{ padding: '10px 10px' }}>108 EMS</th>
              <th style={{ padding: '10px 10px' }}>Status</th>
              <th style={{ padding: '10px 14px', textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>

          <tbody>
            {filteredIncidents.map((incident: IncidentFeedItem) => {
              const isSelected = selectedIncidentId === incident.id;

              return (
                <tr
                  key={incident.id}
                  onClick={() => setSelectedIncidentId(incident.id)}
                  style={{
                    backgroundColor: isSelected ? 'rgba(0, 229, 255, 0.06)' : 'transparent',
                    borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
                    cursor: 'pointer',
                    transition: 'background-color 0.15s ease'
                  }}
                >
                  {/* Incident Code & Time */}
                  <td style={{ padding: '10px 14px' }}>
                    <div className="font-mono" style={{ fontWeight: 800, color: isSelected ? '#00E5FF' : '#FFFFFF' }}>
                      {incident.incidentNumber}
                    </div>
                    <div style={{ fontSize: '10px', color: '#64748B', display: 'flex', alignItems: 'center', gap: '3px' }}>
                      <Clock size={9} />
                      <span>{incident.timestamp} ({incident.timeAgo})</span>
                    </div>
                  </td>

                  {/* Severity Badge */}
                  <td style={{ padding: '10px 10px' }}>
                    {renderSeverityBadge(incident.severity)}
                  </td>

                  {/* Location & Locality */}
                  <td style={{ padding: '10px 12px' }}>
                    <div style={{ fontWeight: 700, color: '#FFFFFF' }}>{incident.locationName}</div>
                    <div style={{ fontSize: '10px', color: '#94A3B8' }}>{incident.locality}</div>
                  </td>

                  {/* Condition & AI Triage */}
                  <td style={{ padding: '10px 12px' }}>
                    <div style={{ fontWeight: 700, color: '#CBD5E1' }}>{incident.conditionTitle}</div>
                    <div style={{ fontSize: '10px', color: '#00E5FF' }}>AI Confidence: {incident.aiConfidence}%</div>
                  </td>

                  {/* Assigned Volunteer */}
                  <td style={{ padding: '10px 12px' }}>
                    {incident.responderName ? (
                      <div>
                        <div style={{ fontWeight: 700, color: '#00E676' }}>{incident.responderName}</div>
                        <div style={{ fontSize: '10px', color: '#94A3B8' }}>{incident.responderRole}</div>
                      </div>
                    ) : (
                      <span style={{ fontSize: '10px', color: '#64748B' }}>Searching Radius...</span>
                    )}
                  </td>

                  {/* 108 EMS Ambulance Status */}
                  <td style={{ padding: '10px 10px' }}>
                    {incident.ambulanceDispatched ? (
                      <span style={{
                        fontSize: '9.5px',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        backgroundColor: 'rgba(0, 229, 255, 0.15)',
                        color: '#00E5FF',
                        fontWeight: 700
                      }}>
                        {incident.ambulanceUnit || 'Dispatched'}
                      </span>
                    ) : (
                      <span style={{ fontSize: '10px', color: '#64748B' }}>Local Care</span>
                    )}
                  </td>

                  {/* Status */}
                  <td style={{ padding: '10px 10px' }}>
                    {renderStatusBadge(incident.status)}
                  </td>

                  {/* Action Buttons */}
                  <td style={{ padding: '10px 14px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '5px' }}>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedIncidentId(incident.id);
                          setScreenMode('MAP');
                        }}
                        style={{
                          padding: '4px 7px',
                          borderRadius: '4px',
                          backgroundColor: '#121620',
                          border: '1px solid rgba(255,255,255,0.1)',
                          color: '#00E5FF',
                          fontSize: '10px',
                          fontWeight: 700,
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '3px'
                        }}
                        title="Focus Live Map on Incident"
                      >
                        <Eye size={11} />
                        <span>Map</span>
                      </button>

                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedIncidentId(incident.id);
                          openClinicalReport();
                        }}
                        style={{
                          padding: '4px 7px',
                          borderRadius: '4px',
                          backgroundColor: 'rgba(255, 160, 0, 0.15)',
                          border: '1px solid rgba(255, 160, 0, 0.3)',
                          color: '#FFA000',
                          fontSize: '10px',
                          fontWeight: 700,
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '3px'
                        }}
                        title="View AI Clinical Handover Certificate"
                      >
                        <FileText size={11} />
                        <span>Report</span>
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
