/* ==========================================================================
   NearHelp AI — Responder Experience Master Container (Screens 4 & 5)
   File: src/components/responder/ResponderMobilePreview.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { ResponderAlertScreen } from './ResponderAlertScreen';
import { RescueNavigationScreen } from './RescueNavigationScreen';
import { ResponderTimelineChatScreen } from './ResponderTimelineChatScreen';
import type { ResponderSubScreen } from '../../mock/types';
import { 
  BellRing, 
  Navigation, 
  MessageSquare, 
  ShieldCheck, 
  Radio, 
  AlertTriangle 
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

export const ResponderMobilePreview: React.FC = () => {
  const { 
    currentScenario, 
    incidentStatus, 
    responderSubScreen, 
    setResponderSubScreen,
    activeResponderIndex,
    triggerSos
  } = useDemoStore();

  const isEmergencyActive = incidentStatus !== 'IDLE' && incidentStatus !== 'COUNTDOWN';
  const activeResponder = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];

  const subTabs: { id: ResponderSubScreen; label: string; icon: React.FC<{ size?: number; color?: string }> }[] = [
    { id: 'ALERT', label: '1: Alert Modal', icon: BellRing },
    { id: 'NAVIGATION', label: '2: Rescue Nav & ID', icon: Navigation },
    { id: 'TIMELINE_CHAT', label: '3: Timeline & Comms', icon: MessageSquare },
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
      {/* Top Header: Responder Identity & Trust Verification Badge */}
      <div style={{
        backgroundColor: '#07090D',
        padding: '8px 12px',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{ position: 'relative' }}>
            <img 
              src={activeResponder.avatar} 
              alt={activeResponder.name}
              style={{
                width: '28px',
                height: '28px',
                borderRadius: '50%',
                border: '1.5px solid #00E676',
                objectFit: 'cover'
              }}
            />
            <div style={{
              position: 'absolute',
              bottom: '-1px',
              right: '-1px',
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: '#00E676',
              border: '1.5px solid #000000'
            }} />
          </div>

          <div>
            <div style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span>{activeResponder.name}</span>
              <ShieldCheck size={12} color="#00E676" />
            </div>
            <div style={{ fontSize: '10px', color: '#94A3B8' }}>
              {activeResponder.role}
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <div style={{
            fontSize: '10px',
            padding: '2px 7px',
            borderRadius: 'var(--radius-full)',
            backgroundColor: 'rgba(0, 230, 118, 0.15)',
            color: '#00E676',
            fontWeight: 800,
            border: '1px solid rgba(0, 230, 118, 0.3)'
          }}>
            TRUST {activeResponder.trustScore}%
          </div>
        </div>
      </div>

      {/* Presentation Flow Sub-Tabs */}
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
          const isActive = responderSubScreen === tab.id;
          const TabIcon = tab.icon;
          const isAlertTab = tab.id === 'ALERT' && isEmergencyActive;

          return (
            <button
              key={tab.id}
              onClick={() => {
                soundEngine.playClick();
                setResponderSubScreen(tab.id);
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '4px',
                padding: '6px 4px',
                borderRadius: '8px',
                backgroundColor: isActive 
                  ? (tab.id === 'ALERT' ? 'rgba(255, 42, 68, 0.2)' : tab.id === 'NAVIGATION' ? 'rgba(0, 230, 118, 0.2)' : 'rgba(0, 229, 255, 0.2)')
                  : 'transparent',
                color: isActive 
                  ? (tab.id === 'ALERT' ? '#FF2A44' : tab.id === 'NAVIGATION' ? '#00E676' : '#00E5FF')
                  : '#94A3B8',
                fontWeight: isActive ? 800 : 600,
                fontSize: '10px',
                border: isActive 
                  ? `1px solid ${tab.id === 'ALERT' ? '#FF2A44' : tab.id === 'NAVIGATION' ? '#00E676' : '#00E5FF'}`
                  : '1px solid transparent',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <TabIcon size={12} color={isActive ? (tab.id === 'ALERT' ? '#FF2A44' : tab.id === 'NAVIGATION' ? '#00E676' : '#00E5FF') : '#94A3B8'} />
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

      {/* Main Sub-Screen Render */}
      <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {/* If IDLE and Alert Screen is selected, provide Standby Radar + Trigger Test */}
        {!isEmergencyActive && responderSubScreen === 'ALERT' ? (
          <div style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '24px 16px',
            textAlign: 'center',
            gap: '14px'
          }}>
            {/* Standby Pulsing Radar Icon */}
            <div style={{
              width: '84px',
              height: '84px',
              borderRadius: '50%',
              backgroundColor: 'rgba(0, 230, 118, 0.08)',
              border: '2px solid rgba(0, 230, 118, 0.3)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              position: 'relative'
            }}>
              <Radio size={36} color="#00E676" />
              <div style={{
                position: 'absolute',
                inset: '-6px',
                borderRadius: '50%',
                border: '1px dashed rgba(0, 230, 118, 0.4)',
                animation: 'radarSweep 4s linear infinite'
              }} />
            </div>

            <div>
              <div style={{ fontSize: '15px', fontWeight: 800, color: '#FFFFFF', marginBottom: '4px' }}>
                Community Standby Radar Active
              </div>
              <p style={{ fontSize: '11.5px', color: '#94A3B8', maxWidth: '260px', margin: '0 auto', lineHeight: '1.4' }}>
                Listening for high-priority PostGIS spatial emergency alerts within 3.0 km.
              </p>
            </div>

            <div style={{
              display: 'flex',
              gap: '6px',
              fontSize: '10px',
              color: '#64748B',
              backgroundColor: '#0C0D10',
              padding: '6px 12px',
              borderRadius: 'var(--radius-full)',
              border: '1px solid rgba(255, 255, 255, 0.06)'
            }}>
              <span>🛰️ GPS Lock: Salt Lake Sector V</span>
              <span>•</span>
              <span>🔋 BLE Mesh Ready</span>
            </div>

            <button
              onClick={() => triggerSos()}
              style={{
                marginTop: '10px',
                padding: '12px 20px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'rgba(255, 42, 68, 0.2)',
                border: '1px solid #FF2A44',
                color: '#FF2A44',
                fontWeight: 800,
                fontSize: '12.5px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(255, 42, 68, 0.25)'
              }}
            >
              <AlertTriangle size={15} color="#FF2A44" />
              <span>Simulate Incoming Emergency Alert</span>
            </button>
          </div>
        ) : (
          <>
            {responderSubScreen === 'ALERT' && <ResponderAlertScreen />}
            {responderSubScreen === 'NAVIGATION' && <RescueNavigationScreen />}
            {responderSubScreen === 'TIMELINE_CHAT' && <ResponderTimelineChatScreen />}
          </>
        )}
      </div>
    </div>
  );
};
