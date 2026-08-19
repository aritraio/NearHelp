/* ==========================================================================
   NearHelp AI — Main Application Shell
   File: src/App.tsx
   ========================================================================== */

import React from 'react';
import { DemoProvider, useDemoStore } from './store/DemoContext';
import { ScenarioController } from './components/demo/ScenarioController';
import { MobileDeviceFrame } from './components/demo/MobileDeviceFrame';
import { Phase1Showcase } from './components/demo/Phase1Showcase';
import { VictimMobilePreview } from './components/victim/VictimMobilePreview';
import { ResponderMobilePreview } from './components/responder/ResponderMobilePreview';

const MainContent: React.FC = () => {
  const { personaMode, viewLayout } = useDemoStore();

  return (
    <div style={{
      minHeight: 'calc(100vh - 58px)',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--bg-base)',
      position: 'relative'
    }}>
      {/* View Switcher based on viewLayout & personaMode */}
      {viewLayout === 'DESKTOP_FULL' ? (
        <Phase1Showcase />
      ) : viewLayout === 'SPLIT_SCREEN' ? (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
          gap: '32px',
          padding: '24px',
          flexWrap: 'wrap',
          maxWidth: '1200px',
          margin: '0 auto',
          width: '100%'
        }}>
          <MobileDeviceFrame title="Victim Persona (1-Tap SOS)" badgeText="Caller">
            <VictimMobilePreview />
          </MobileDeviceFrame>

          <MobileDeviceFrame title="Responder Persona (Spatial Alert)" badgeText="Volunteer">
            <ResponderMobilePreview />
          </MobileDeviceFrame>
        </div>
      ) : (
        /* Single Mobile Frame view */
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '20px 0',
          width: '100%'
        }}>
          <MobileDeviceFrame 
            title={personaMode === 'VICTIM' ? 'Victim Experience View' : personaMode === 'RESPONDER' ? 'Responder Rescue View' : 'NearHelp Mobile'}
            badgeText={personaMode === 'VICTIM' ? 'SOS Mode' : personaMode === 'RESPONDER' ? 'Rescue Mode' : 'Admin'}
          >
            {personaMode === 'VICTIM' && <VictimMobilePreview />}
            {personaMode === 'RESPONDER' && <ResponderMobilePreview />}
            {personaMode === 'COMMAND_CENTER' && <VictimMobilePreview />}
          </MobileDeviceFrame>
        </div>
      )}
    </div>
  );
};

export function App() {
  return (
    <DemoProvider>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <ScenarioController />
        <MainContent />
      </div>
    </DemoProvider>
  );
}

export default App;
