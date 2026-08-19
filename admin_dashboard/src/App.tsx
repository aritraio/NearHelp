/* ==========================================================================
   NearHelp AI — Main Application Shell (Phase 5 Presentation & Tuning)
   File: src/App.tsx
   ========================================================================== */

import React from 'react';
import { DemoProvider, useDemoStore } from './store/DemoContext';
import { ScenarioController } from './components/demo/ScenarioController';
import { MobileDeviceFrame } from './components/demo/MobileDeviceFrame';
import { Phase1Showcase } from './components/demo/Phase1Showcase';
import { GuardianRadarScreen } from './components/guardian/GuardianRadarScreen';
import { VictimMobilePreview } from './components/victim/VictimMobilePreview';
import { ResponderMobilePreview } from './components/responder/ResponderMobilePreview';
import { CommunityGeoMap } from './components/map/CommunityGeoMap';
import { CommandCenterScreen } from './components/command/CommandCenterScreen';
import { SlideSyncHUD } from './components/demo/SlideSyncHUD';
import { DryRunTourModal } from './components/demo/DryRunTourModal';

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
      {/* Full Desktop Command Center or Showcase Mode */}
      {viewLayout === 'DESKTOP_FULL' ? (
        screenMode === 'COMMAND_CENTER' ? (
          <div style={{ flex: 1, height: 'calc(100vh - 58px)' }}>
            <CommandCenterScreen />
          </div>
        ) : screenMode === 'MAP' ? (
          <div style={{ flex: 1, height: 'calc(100vh - 58px)' }}>
            <CommunityGeoMap />
          </div>
        ) : (
          <Phase1Showcase />
        )
      ) : viewLayout === 'SPLIT_SCREEN' ? (
        /* Dynamic Dual Persona Split Screen for Showcase Presentations */
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
          gap: '36px',
          padding: '24px 16px',
          flexWrap: 'wrap',
          maxWidth: '1440px',
          margin: '0 auto',
          width: '100%'
        }}>
          {screenMode === 'GUARDIAN' ? (
            <>
              {/* Left Screen: Guardian Radar & Safe Zone */}
              <MobileDeviceFrame 
                title="Screen 1: Guardian Radar (Safe Zone)" 
                badgeText="91% Safety Index"
              >
                <GuardianRadarScreen />
              </MobileDeviceFrame>

              {/* Right Screen: Victim Experience */}
              <MobileDeviceFrame 
                title="Screen 2: Victim Experience" 
                badgeText="SOS • Triage • First-Aid"
              >
                <VictimMobilePreview />
              </MobileDeviceFrame>
            </>
          ) : screenMode === 'MAP' ? (
            <>
              {/* Left Screen: Dynamic Community Geo-Map */}
              <MobileDeviceFrame 
                title="Screen 6: Community Geo-Map" 
                badgeText="PostGIS Spatial Engine"
              >
                <CommunityGeoMap />
              </MobileDeviceFrame>

              {/* Right Screen: Responder Experience */}
              <MobileDeviceFrame 
                title="Persona B: Responder Rescue Flow" 
                badgeText="Alert • Nav • Medical ID"
              >
                <ResponderMobilePreview />
              </MobileDeviceFrame>
            </>
          ) : screenMode === 'COMMAND_CENTER' ? (
            <>
              {/* Left Screen: Dynamic Community Geo-Map */}
              <MobileDeviceFrame 
                title="Screen 6: Community Geo-Map" 
                badgeText="Live PostGIS Waves"
              >
                <CommunityGeoMap />
              </MobileDeviceFrame>

              {/* Right Screen: Command Center Telemetry & Feeds */}
              <MobileDeviceFrame 
                title="Screen 7: Command Center" 
                badgeText="Incident Feed • Reports"
              >
                <CommandCenterScreen />
              </MobileDeviceFrame>
            </>
          ) : screenMode === 'RESPONDER' ? (
            <>
              {/* Left Screen: Responder Experience */}
              <MobileDeviceFrame 
                title="Persona B: Responder Rescue Flow" 
                badgeText="Alert • Nav • Medical ID"
              >
                <ResponderMobilePreview />
              </MobileDeviceFrame>

              {/* Right Screen: Dynamic Community Geo-Map */}
              <MobileDeviceFrame 
                title="Screen 6: Community Geo-Map" 
                badgeText="Spatial Dispatch"
              >
                <CommunityGeoMap />
              </MobileDeviceFrame>
            </>
          ) : (
            /* Default Dual-Persona Live Sync: Victim Mode (Left) + Responder Mode (Right) */
            <>
              <MobileDeviceFrame 
                title="Persona A: Victim Experience" 
                badgeText="SOS • Triage • First-Aid"
              >
                <VictimMobilePreview />
              </MobileDeviceFrame>

              <MobileDeviceFrame 
                title="Persona B: Responder Rescue Flow" 
                badgeText="Alert • Nav • Medical ID"
              >
                <ResponderMobilePreview />
              </MobileDeviceFrame>
            </>
          )}
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
              screenMode === 'CRISIS_MATRIX' ? 'Screen 2: Victim SOS Intake' : 
              screenMode === 'RESPONDER' ? 'Screens 4 & 5: Responder Rescue' : 
              screenMode === 'MAP' ? 'Screen 6: Community Geo-Map' :
              'Screen 7: Command Center'
            }
            badgeText={
              screenMode === 'GUARDIAN' ? 'Safe Zone' : 
              screenMode === 'CRISIS_MATRIX' ? 'SOS & First-Aid' : 
              screenMode === 'RESPONDER' ? 'Rescue Mode' : 
              screenMode === 'MAP' ? 'PostGIS Spatial' :
              'Admin Telemetry'
            }
          >
            {screenMode === 'GUARDIAN' && <GuardianRadarScreen />}
            {screenMode === 'CRISIS_MATRIX' && <VictimMobilePreview />}
            {screenMode === 'RESPONDER' && <ResponderMobilePreview />}
            {screenMode === 'MAP' && <CommunityGeoMap />}
            {screenMode === 'COMMAND_CENTER' && <CommandCenterScreen />}
          </MobileDeviceFrame>
        </div>
      )}
    </main>
  );
};

const AppShell: React.FC = () => {
  const { projectorMode, presentationZoom, quickNotification } = useDemoStore();

  return (
    <div 
      className={`${projectorMode ? 'projector-mode' : ''} zoom-${presentationZoom}`}
      style={{ 
        display: 'flex', 
        flexDirection: 'column', 
        minHeight: '100vh',
        backgroundColor: 'var(--bg-base)',
        color: 'var(--text-primary)',
        transition: 'all var(--transition-normal)'
      }}
    >
      <ScenarioController />
      <MainContent />
      <SlideSyncHUD />
      <DryRunTourModal />

      {/* Floating Presentation Toast Notification */}
      {quickNotification && (
        <div 
          className="animate-toast"
          style={{
            position: 'fixed',
            top: '72px',
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 1500,
            padding: '10px 20px',
            backgroundColor: projectorMode ? '#000' : 'rgba(10, 15, 24, 0.95)',
            border: `1.5px solid ${projectorMode ? 'var(--color-ai-cyan)' : 'var(--border-ai)'}`,
            borderRadius: 'var(--radius-full)',
            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.9)',
            fontSize: '13px',
            fontWeight: 700,
            color: 'var(--text-primary)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            pointerEvents: 'none'
          }}
        >
          <span>{quickNotification}</span>
        </div>
      )}
    </div>
  );
};

export function App() {
  return (
    <DemoProvider>
      <AppShell />
    </DemoProvider>
  );
}

export default App;
