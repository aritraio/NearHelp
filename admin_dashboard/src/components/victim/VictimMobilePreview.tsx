/* ==========================================================================
   NearHelp AI — Victim Experience Master Container (Screens 1, 2, & 3)
   File: src/components/victim/VictimMobilePreview.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { SosTriggerScreen } from './SosTriggerScreen';
import { ActiveTriageScreen } from './ActiveTriageScreen';
import { FirstAidRagScreen } from './FirstAidRagScreen';
import type { VictimSubScreen } from '../../mock/types';
import { Zap, Sparkles, HeartPulse } from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const VictimMobilePreview: React.FC = () => {
  const { 
    victimSubScreen, 
    setVictimSubScreen, 
    incidentStatus 
  } = useDemoStore();

  const isEmergencyActive = incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN';

  const subTabs: { id: VictimSubScreen; label: string; icon: React.FC<{ size?: number; color?: string }> }[] = [
    { id: 'TRIGGER', label: '1: SOS Intake', icon: Zap },
    { id: 'TRIAGE', label: '2: AI Triage', icon: Sparkles },
    { id: 'FIRST_AID', label: '3: RAG Guide', icon: HeartPulse },
  ];

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: 'var(--text-primary)',
      overflow: 'hidden'
    }}>
      {/* Top Navigation Sub-Tabs Bar (Victim Presentation Flow) */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '4px',
        backgroundColor: '#0A0C10',
        padding: '6px 10px',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        flexShrink: 0
      }}>
        {subTabs.map((tab) => {
          const isActive = victimSubScreen === tab.id;
          const TabIcon = tab.icon;
          const isAlertTab = tab.id === 'TRIAGE' && isEmergencyActive;

          return (
            <button
              key={tab.id}
              onClick={() => {
                soundEngine.playClick();
                setVictimSubScreen(tab.id);
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '4px',
                padding: '6px 4px',
                borderRadius: '8px',
                backgroundColor: isActive 
                  ? (tab.id === 'TRIGGER' ? '#FF2A44' : tab.id === 'TRIAGE' ? 'rgba(0, 229, 255, 0.2)' : 'rgba(0, 230, 118, 0.2)')
                  : 'transparent',
                color: isActive 
                  ? (tab.id === 'TRIGGER' ? '#FFFFFF' : tab.id === 'TRIAGE' ? '#00E5FF' : '#00E676')
                  : '#94A3B8',
                fontWeight: isActive ? 800 : 600,
                fontSize: '10.5px',
                border: isActive 
                  ? `1px solid ${tab.id === 'TRIGGER' ? 'rgba(255,255,255,0.3)' : tab.id === 'TRIAGE' ? '#00E5FF' : '#00E676'}`
                  : '1px solid transparent',
                boxShadow: isActive ? '0 2px 8px rgba(0, 0, 0, 0.4)' : 'none',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <TabIcon size={12} color={isActive ? (tab.id === 'TRIGGER' ? '#FFFFFF' : tab.id === 'TRIAGE' ? '#00E5FF' : '#00E676') : '#94A3B8'} />
              <span>{tab.label}</span>
              {isAlertTab && (
                <div style={{
                  width: '5px',
                  height: '5px',
                  borderRadius: '50%',
                  backgroundColor: '#FF2A44'
                }} />
              )}
            </button>
          );
        })}
      </div>

      {/* Sub-Screen Dynamic Render */}
      <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {victimSubScreen === 'TRIGGER' && <SosTriggerScreen />}
        {victimSubScreen === 'TRIAGE' && <ActiveTriageScreen />}
        {victimSubScreen === 'FIRST_AID' && <FirstAidRagScreen />}
      </div>
    </div>
  );
};
