/* ==========================================================================
   NearHelp AI — Screen 7: Master Emergency Dispatch Command Center Screen
   File: src/components/command/CommandCenterScreen.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { soundEngine } from '../../utils/audio';
import { TelemetryDashboard } from './TelemetryDashboard';
import { IncidentFeedTable } from './IncidentFeedTable';
import { ClinicalReportModal } from './ClinicalReportModal';
import { CommunityGeoMap } from '../map/CommunityGeoMap';
import { 
  Radio, 
  Ambulance, 
  FileText, 
  Map, 
  ListOrdered
} from 'lucide-react';

export const CommandCenterScreen: React.FC = () => {
  const { 
    currentScenario,
    openClinicalReport, 
    trigger108Escalation, 
    broadcastAlert
  } = useDemoStore();

  const [activeViewTab, setActiveViewTab] = useState<'INCIDENTS' | 'MAP' | 'REPORT'>('INCIDENTS');
  const [broadcastNotified, setBroadcastNotified] = useState<boolean>(false);

  const handleBroadcast = () => {
    soundEngine.playEmergencyAlert();
    broadcastAlert(`🚨 Grid Alert: Critical ${currentScenario.severityLabel} at ${currentScenario.streetAddress}. All nearby responders mobilized.`);
    setBroadcastNotified(true);
    setTimeout(() => setBroadcastNotified(false), 3000);
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#040608',
      color: '#FFFFFF',
      overflow: 'hidden',
      position: 'relative'
    }}>
      {/* 1. Top Telemetry Banner */}
      <TelemetryDashboard />

      {/* 2. Sub-Navigation Tabs Bar for Command Center */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '8px 14px',
        backgroundColor: '#090B10',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        flexShrink: 0
      }}>
        {/* Left: View Tabs */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          {[
            { id: 'INCIDENTS' as const, label: 'Live Incident Feed', icon: ListOrdered },
            { id: 'MAP' as const, label: 'Community Geo-Map', icon: Map },
            { id: 'REPORT' as const, label: 'Clinical Handover Audit', icon: FileText },
          ].map((tab) => {
            const isSelected = activeViewTab === tab.id;
            const TabIcon = tab.icon;

            return (
              <button
                key={tab.id}
                onClick={() => {
                  soundEngine.playClick();
                  setActiveViewTab(tab.id);
                  if (tab.id === 'REPORT') {
                    openClinicalReport();
                  }
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '5px',
                  padding: '6px 12px',
                  borderRadius: '8px',
                  backgroundColor: isSelected ? '#161C28' : 'transparent',
                  color: isSelected ? '#00E5FF' : '#94A3B8',
                  fontWeight: isSelected ? 800 : 600,
                  fontSize: '11px',
                  border: isSelected ? '1px solid rgba(0, 229, 255, 0.4)' : '1px solid transparent',
                  boxShadow: isSelected ? '0 2px 8px rgba(0,0,0,0.4)' : 'none',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <TabIcon size={13} color={isSelected ? '#00E5FF' : '#94A3B8'} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Right: Quick Action Buttons for Examiners / Viva */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {broadcastNotified && (
            <span style={{ fontSize: '10.5px', color: '#00E676', fontWeight: 800, animation: 'fadeIn 0.2s ease' }}>
              ✓ Broadcast Dispatched!
            </span>
          )}

          <button
            onClick={handleBroadcast}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              padding: '6px 12px',
              borderRadius: '6px',
              backgroundColor: 'rgba(255, 42, 68, 0.18)',
              border: '1px solid #FF2A44',
              color: '#FF2A44',
              fontSize: '11px',
              fontWeight: 800,
              cursor: 'pointer'
            }}
            title="Broadcast emergency notification to all nearby community app users"
          >
            <Radio size={13} />
            <span>Mass Broadcast</span>
          </button>

          <button
            onClick={() => trigger108Escalation()}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              padding: '6px 12px',
              borderRadius: '6px',
              backgroundColor: 'rgba(0, 229, 255, 0.18)',
              border: '1px solid #00E5FF',
              color: '#00E5FF',
              fontSize: '11px',
              fontWeight: 800,
              cursor: 'pointer'
            }}
            title="Force Escalate Dispatch to Municipal 108 ALS Ambulance"
          >
            <Ambulance size={13} />
            <span>108 Escalation</span>
          </button>

          <button
            onClick={openClinicalReport}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              padding: '6px 12px',
              borderRadius: '6px',
              backgroundColor: 'rgba(255, 160, 0, 0.18)',
              border: '1px solid #FFA000',
              color: '#FFA000',
              fontSize: '11px',
              fontWeight: 800,
              cursor: 'pointer'
            }}
            title="Generate & View AI Clinical Handover Certificate (Section 134A MV Act)"
          >
            <FileText size={13} />
            <span>Handover PDF</span>
          </button>
        </div>
      </div>

      {/* 3. Main Center Content View */}
      <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {activeViewTab === 'INCIDENTS' && <IncidentFeedTable />}
        {activeViewTab === 'MAP' && <CommunityGeoMap />}
        {activeViewTab === 'REPORT' && (
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '12px' }}>
            <FileText size={48} color="#00E5FF" />
            <div style={{ fontSize: '15px', fontWeight: 800, color: '#FFFFFF' }}>AI Clinical Handover Certificate Active</div>
            <p style={{ fontSize: '12px', color: '#94A3B8', maxWidth: '420px', textAlign: 'center' }}>
              Audited under Section 134A of the Motor Vehicles (Amendment) Act 2019.
            </p>
            <button
              onClick={openClinicalReport}
              style={{
                padding: '10px 18px',
                borderRadius: '8px',
                backgroundColor: '#00E5FF',
                color: '#000000',
                fontWeight: 800,
                fontSize: '12.5px',
                cursor: 'pointer'
              }}
            >
              Open Full-Screen Clinical Report
            </button>
          </div>
        )}
      </div>

      {/* 4. Modal Overlay */}
      <ClinicalReportModal />
    </div>
  );
};
