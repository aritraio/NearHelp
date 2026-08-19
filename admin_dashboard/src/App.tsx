/* ==========================================================================
   NearHelp AI — Main Application Shell (Aligned with docs/design.md)
   File: src/App.tsx
   ========================================================================== */

import React from 'react';
import { DemoProvider, useDemoStore } from './store/DemoContext';
import { ScenarioController } from './components/demo/ScenarioController';
import { MobileDeviceFrame } from './components/demo/MobileDeviceFrame';
import { Phase1Showcase } from './components/demo/Phase1Showcase';
import { GuardianRadarScreen } from './components/guardian/GuardianRadarScreen';
import { CrisisDispatchScreen } from './components/crisis/CrisisDispatchScreen';
import { ResponderMobilePreview } from './components/responder/ResponderMobilePreview';

const MainContent: React.FC = () => {
  const { screenMode, viewLayout } = useDemoStore();

  return (
    <main style={{
      minHeight: 'calc(100vh - 58px)',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--bg-base)',
      position: 'relative'
    }}>
      {/* View Switcher based on viewLayout & screenMode */}
      {viewLayout === 'DESKTOP_FULL' ? (
        <Phase1Showcase />
      ) : viewLayout === 'SPLIT_SCREEN' ? (
        /* Dual Screen Showcase matching docs/design.md exact 2-screen layout */
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
          gap: '36px',
          padding: '24px 16px',
          flexWrap: 'wrap',
          maxWidth: '1280px',
          margin: '0 auto',
          width: '100%'
        }}>
          {/* Left Screen: Guardian Radar & Safe Zone */}
          <MobileDeviceFrame 
            title="Screen 1: Guardian Radar (Safe Zone)" 
            badgeText="91% Safety Index"
          >
            <GuardianRadarScreen />
          </MobileDeviceFrame>

          {/* Right Screen: Medical Emergency Intake & Multimodal Triage */}
          <MobileDeviceFrame 
            title="Screen 2: Medical Emergency Intake" 
            badgeText="Multimodal Triage"
          >
            <CrisisDispatchScreen />
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
            title={
              screenMode === 'GUARDIAN' ? 'Screen 1: Guardian Radar' : 
              screenMode === 'CRISIS_MATRIX' ? 'Screen 2: Medical Emergency Intake' : 
              screenMode === 'RESPONDER' ? 'Responder Rescue Navigation' : 
              'NearHelp Mobile Client'
            }
            badgeText={
              screenMode === 'GUARDIAN' ? 'Safe Zone' : 
              screenMode === 'CRISIS_MATRIX' ? 'Multimodal AI' : 
              screenMode === 'RESPONDER' ? 'Rescue Mode' : 
              'Admin'
            }
          >
            {screenMode === 'GUARDIAN' && <GuardianRadarScreen />}
            {screenMode === 'CRISIS_MATRIX' && <CrisisDispatchScreen />}
            {screenMode === 'RESPONDER' && <ResponderMobilePreview />}
            {screenMode === 'COMMAND_CENTER' && <CrisisDispatchScreen />}
          </MobileDeviceFrame>
        </div>
      )}
    </main>
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
