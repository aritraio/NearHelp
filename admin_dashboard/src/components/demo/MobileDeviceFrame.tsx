/* ==========================================================================
   NearHelp AI — Photorealistic Smartphone Device Frame
   File: src/components/demo/MobileDeviceFrame.tsx
   ========================================================================== */

import React from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { Wifi, BatteryMedium, Signal } from 'lucide-react';

interface MobileDeviceFrameProps {
  children: React.ReactNode;
  title?: string;
  badgeText?: string;
}

export const MobileDeviceFrame: React.FC<MobileDeviceFrameProps> = ({ 
  children, 
  title = 'NearHelp Client', 
  badgeText 
}) => {
  const { incidentStatus, offlineMeshActive } = useDemoStore();

  const currentTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      padding: '16px 8px',
    }}>
      {/* Title / Persona Header above device */}
      {title && (
        <div style={{
          marginBottom: '10px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontSize: '13px',
          fontWeight: 700,
          color: 'var(--text-secondary)'
        }}>
          <span>{title}</span>
          {badgeText && (
            <span style={{
              fontSize: '11px',
              padding: '2px 8px',
              borderRadius: 'var(--radius-full)',
              backgroundColor: 'var(--bg-surface-elevated)',
              color: 'var(--color-safe-green-bright)',
              border: '1px solid var(--border-safe)'
            }}>
              {badgeText}
            </span>
          )}
        </div>
      )}

      {/* Smartphone Chassis */}
      <div style={{
        position: 'relative',
        width: '390px',
        height: '790px',
        maxWidth: '100%',
        backgroundColor: '#000000',
        borderRadius: '50px',
        padding: '12px',
        boxShadow: '0 25px 60px -12px rgba(0, 0, 0, 0.9), 0 0 0 1px rgba(255, 255, 255, 0.12), inset 0 0 0 3px #1a1a1c',
        border: '3px solid #33363d',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
      }}>
        {/* Device Inner Screen */}
        <div style={{
          position: 'relative',
          width: '100%',
          height: '100%',
          backgroundColor: 'var(--bg-base)',
          borderRadius: '40px',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          border: '1px solid rgba(255, 255, 255, 0.04)'
        }}>
          {/* Status Bar */}
          <div style={{
            height: '42px',
            width: '100%',
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: '13px',
            fontWeight: 600,
            color: 'var(--text-primary)',
            zIndex: 50,
            userSelect: 'none',
            flexShrink: 0
          }}>
            <span>{currentTime}</span>

            {/* Dynamic Island Pill */}
            <div style={{
              position: 'absolute',
              top: '8px',
              left: '50%',
              transform: 'translateX(-50%)',
              height: '26px',
              minWidth: '105px',
              padding: '0 10px',
              backgroundColor: '#000000',
              borderRadius: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              boxShadow: '0 0 0 1px rgba(255,255,255,0.1)',
              transition: 'all var(--transition-normal)'
            }}>
              {incidentStatus !== 'IDLE' ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '6px', height: '6px' }} />
                  <span style={{ fontSize: '10px', color: 'var(--color-emergency-red-bright)', fontWeight: 700 }}>
                    SOS ACTIVE
                  </span>
                </div>
              ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <div style={{ width: '9px', height: '9px', borderRadius: '50%', backgroundColor: '#111', border: '1px solid #222' }} />
                  <div style={{ width: '7px', height: '7px', borderRadius: '50%', backgroundColor: '#092147' }} />
                </div>
              )}
            </div>

            {/* Right Status Icons */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              {offlineMeshActive ? (
                <span style={{ fontSize: '10px', color: 'var(--color-action-amber-bright)', fontWeight: 700 }}>
                  MESH
                </span>
              ) : (
                <>
                  <Signal size={12} />
                  <Wifi size={12} />
                </>
              )}
              <BatteryMedium size={14} />
            </div>
          </div>

          {/* Screen Content Container (Scrollable) */}
          <div style={{
            flex: 1,
            overflowY: 'auto',
            overflowX: 'hidden',
            display: 'flex',
            flexDirection: 'column',
            position: 'relative'
          }}>
            {children}
          </div>

          {/* Home Indicator Bar */}
          <div style={{
            height: '20px',
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 50,
            flexShrink: 0
          }}>
            <div style={{
              width: '110px',
              height: '4px',
              backgroundColor: 'rgba(255, 255, 255, 0.4)',
              borderRadius: '2px'
            }} />
          </div>
        </div>
      </div>
    </div>
  );
};
