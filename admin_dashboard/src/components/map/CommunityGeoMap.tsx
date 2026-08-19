/* ==========================================================================
   NearHelp AI — Screen 6: Dynamic Community Geo-Map (Kolkata Spatial Engine)
   File: src/components/map/CommunityGeoMap.tsx
   ========================================================================== */

import React, { useState, useMemo } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { soundEngine } from '../../utils/audio';
import type { MapLayerKey, Responder, HospitalNode, AEDNode } from '../../mock/types';
import { 
  Layers, 
  Radio, 
  X, 
  ZoomIn, 
  ZoomOut, 
  Check, 
  Zap, 
  Ambulance,
  Crosshair
} from 'lucide-react';

const VICTIM_POS = { x: 400, y: 260 };

export const CommunityGeoMap: React.FC = () => {
  const {
    currentScenario,
    elapsedSeconds,
    searchRadiusKm,
    mapLayerFilters,
    selectedMapEntity,
    activeResponderIndex,
    aedAttached,
    toggleMapLayer,
    setSelectedMapEntity,
    setActiveResponderIndex,
    acceptDispatch,
    trigger108Escalation,
    broadcastAlert,
    toggleAedAttached
  } = useDemoStore();

  const [zoomLevel, setZoomLevel] = useState<number>(1);
  const [panOffset, setPanOffset] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [showSqlOverlay, setShowSqlOverlay] = useState<boolean>(true);
  const [, setHoveredEntityId] = useState<string | null>(null);

  // Generate responder positions relative to victim
  const responderPositions = useMemo(() => {
    return currentScenario.responders.map((resp, idx) => {
      // Scale lat/lng delta to pixels
      const dx = (resp.lng - currentScenario.coordinates[1]) * 18000;
      const dy = -(resp.lat - currentScenario.coordinates[0]) * 18000;
      return {
        ...resp,
        x: VICTIM_POS.x + (dx !== 0 ? dx : (idx === 0 ? 120 : -140)),
        y: VICTIM_POS.y + (dy !== 0 ? dy : (idx === 0 ? -110 : 130)),
      };
    });
  }, [currentScenario]);

  // Generate hospital positions
  const hospitalPositions = useMemo(() => {
    return currentScenario.nearbyHospitals.map((hosp, idx) => {
      const dx = (hosp.lng - currentScenario.coordinates[1]) * 12000;
      const dy = -(hosp.lat - currentScenario.coordinates[0]) * 12000;
      return {
        ...hosp,
        x: VICTIM_POS.x + (dx !== 0 ? dx : (idx === 0 ? -220 : 250)),
        y: VICTIM_POS.y + (dy !== 0 ? dy : (idx === 0 ? -160 : -140)),
      };
    });
  }, [currentScenario]);

  // Generate AED positions
  const aedPositions = useMemo(() => {
    return currentScenario.nearbyAEDs.map((aed, idx) => {
      const dx = (aed.lng - currentScenario.coordinates[1]) * 22000;
      const dy = -(aed.lat - currentScenario.coordinates[0]) * 22000;
      return {
        ...aed,
        x: VICTIM_POS.x + (dx !== 0 ? dx : (idx === 0 ? 70 : -90)),
        y: VICTIM_POS.y + (dy !== 0 ? dy : (idx === 0 ? 75 : -80)),
      };
    });
  }, [currentScenario]);

  const handleZoomIn = () => {
    soundEngine.playClick();
    setZoomLevel(z => Math.min(z + 0.2, 1.8));
  };

  const handleZoomOut = () => {
    soundEngine.playClick();
    setZoomLevel(z => Math.max(z - 0.2, 0.8));
  };

  const handleResetView = () => {
    soundEngine.playClick();
    setZoomLevel(1);
    setPanOffset({ x: 0, y: 0 });
    setSelectedMapEntity(null);
  };

  // Convert searchRadiusKm into visual SVG radius pixels (1km approx 90px at zoom 1)
  const radiusPx = (searchRadiusKm * 95) * zoomLevel;

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: '#FFFFFF',
      position: 'relative',
      overflow: 'hidden',
      userSelect: 'none'
    }}>
      {/* Top Map HUD Toolbar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '8px 14px',
        backgroundColor: '#07090D',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        zIndex: 20,
        flexShrink: 0
      }}>
        {/* Left: GIS GPS Telemetry Pill */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            backgroundColor: '#12151D',
            padding: '4px 10px',
            borderRadius: 'var(--radius-full)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            fontSize: '11px'
          }}>
            <div className="telemetry-dot telemetry-dot-emergency" style={{ width: '7px', height: '7px' }} />
            <span style={{ fontWeight: 800, color: '#FFFFFF' }}>{currentScenario.locationName.split(',')[0]}</span>
            <span style={{ color: '#64748B' }}>•</span>
            <span className="font-mono" style={{ color: '#00E5FF', fontSize: '10px' }}>
              {currentScenario.coordinates[0].toFixed(4)}°N, {currentScenario.coordinates[1].toFixed(4)}°E
            </span>
          </div>

          <div style={{
            fontSize: '10px',
            color: '#00E676',
            backgroundColor: 'rgba(0, 230, 118, 0.12)',
            padding: '3px 8px',
            borderRadius: 'var(--radius-full)',
            border: '1px solid rgba(0, 230, 118, 0.25)',
            fontWeight: 700
          }}>
            GPS ±2.8m • 14ms WebSocket
          </div>
        </div>

        {/* Right: Map Controls (Zoom, Reset, SQL Toggle) */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <button
            onClick={() => setShowSqlOverlay(!showSqlOverlay)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: '4px 8px',
              borderRadius: '6px',
              fontSize: '10.5px',
              fontWeight: 700,
              backgroundColor: showSqlOverlay ? 'rgba(0, 229, 255, 0.18)' : '#14171F',
              color: showSqlOverlay ? '#00E5FF' : '#94A3B8',
              border: `1px solid ${showSqlOverlay ? '#00E5FF' : 'rgba(255,255,255,0.08)'}`,
              cursor: 'pointer'
            }}
            title="Toggle PostGIS Spatial SQL Query HUD"
          >
            <Radio size={12} />
            <span>PostGIS SQL</span>
          </button>

          <div style={{ display: 'flex', backgroundColor: '#14171F', borderRadius: '6px', border: '1px solid rgba(255,255,255,0.08)' }}>
            <button
              onClick={handleZoomIn}
              style={{ padding: '5px 8px', color: '#CBD5E1', borderRight: '1px solid rgba(255,255,255,0.08)' }}
              title="Zoom In"
            >
              <ZoomIn size={13} />
            </button>
            <button
              onClick={handleZoomOut}
              style={{ padding: '5px 8px', color: '#CBD5E1', borderRight: '1px solid rgba(255,255,255,0.08)' }}
              title="Zoom Out"
            >
              <ZoomOut size={13} />
            </button>
            <button
              onClick={handleResetView}
              style={{ padding: '5px 8px', color: '#CBD5E1' }}
              title="Reset View"
            >
              <Crosshair size={13} />
            </button>
          </div>
        </div>
      </div>

      {/* Layer Filter Chips Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        padding: '6px 14px',
        backgroundColor: 'rgba(10, 12, 16, 0.95)',
        borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        overflowX: 'auto',
        zIndex: 15,
        flexShrink: 0
      }}>
        <span style={{ fontSize: '10px', fontWeight: 800, color: '#64748B', display: 'flex', alignItems: 'center', gap: '3px' }}>
          <Layers size={11} />
          <span>LAYERS:</span>
        </span>

        {[
          { key: 'victim' as MapLayerKey, label: `📍 Victim SOS`, activeColor: '#FF2A44', count: 1 },
          { key: 'responders' as MapLayerKey, label: `🏃 Responders (${currentScenario.responders.length})`, activeColor: '#00E676', count: currentScenario.responders.length },
          { key: 'hospitals' as MapLayerKey, label: `🏥 Hospitals (${currentScenario.nearbyHospitals.length})`, activeColor: '#00E5FF', count: currentScenario.nearbyHospitals.length },
          { key: 'aeds' as MapLayerKey, label: `⚡ AEDs (${currentScenario.nearbyAEDs.length})`, activeColor: '#FFA000', count: currentScenario.nearbyAEDs.length },
          { key: 'postgis_wave' as MapLayerKey, label: `🌊 PostGIS (${searchRadiusKm}km)`, activeColor: '#00E5FF', count: null },
          { key: 'routes' as MapLayerKey, label: `🛤️ Rescue Routes`, activeColor: '#00E676', count: null },
        ].map((layer) => {
          const isEnabled = mapLayerFilters[layer.key];
          return (
            <button
              key={layer.key}
              onClick={() => toggleMapLayer(layer.key)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                padding: '3px 8px',
                borderRadius: 'var(--radius-full)',
                backgroundColor: isEnabled ? `${layer.activeColor}22` : '#12141A',
                color: isEnabled ? layer.activeColor : '#64748B',
                border: `1px solid ${isEnabled ? layer.activeColor : 'rgba(255, 255, 255, 0.08)'}`,
                fontSize: '10.5px',
                fontWeight: isEnabled ? 800 : 500,
                cursor: 'pointer',
                whiteSpace: 'nowrap',
                transition: 'all 0.15s ease'
              }}
            >
              <div style={{
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                backgroundColor: isEnabled ? layer.activeColor : '#475569'
              }} />
              <span>{layer.label}</span>
            </button>
          );
        })}
      </div>

      {/* Main Interactive Map Viewport (Vector SVG + Cartography Styling) */}
      <div style={{
        flex: 1,
        position: 'relative',
        backgroundColor: '#040608',
        backgroundImage: `
          radial-gradient(circle at 50% 50%, rgba(18, 24, 38, 0.6) 0%, rgba(4, 6, 8, 0.95) 100%),
          linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
          linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px)
        `,
        backgroundSize: '100% 100%, 40px 40px, 40px 40px',
        overflow: 'hidden',
        cursor: 'grab'
      }}>
        {/* SVG Geo Cartography Canvas */}
        <svg
          viewBox="0 0 800 520"
          style={{
            width: '100%',
            height: '100%',
            transform: `scale(${zoomLevel}) translate(${panOffset.x}px, ${panOffset.y}px)`,
            transformOrigin: 'center center',
            transition: 'transform 0.2s ease-out'
          }}
        >
          <defs>
            {/* Pulsing Target Glow Filters */}
            <filter id="victimGlow" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur in="SourceGraphic" stdDeviation="6" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>

            <filter id="responderGlow" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur in="SourceGraphic" stdDeviation="4" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>

            {/* Linear Gradients */}
            <linearGradient id="routeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#00E676" stopOpacity="0.9" />
              <stop offset="100%" stopColor="#FF2A44" stopOpacity="0.9" />
            </linearGradient>

            <radialGradient id="postgisRadarFill" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stopColor="#00E5FF" stopOpacity="0.02" />
              <stop offset="70%" stopColor="#00E5FF" stopOpacity="0.08" />
              <stop offset="100%" stopColor="#00E5FF" stopOpacity="0.25" />
            </radialGradient>
          </defs>

          {/* 1. Base Map Geography (Kolkata Sector V / EM Bypass Stylized Streets & Wetlands) */}
          <g id="mapBaseRoads" opacity="0.6">
            {/* Salt Lake / East Kolkata Wetlands Water feature */}
            <path
              d="M 620 40 Q 680 120, 750 200 T 780 420 L 800 520 L 800 0 L 600 0 Z"
              fill="#061826"
              stroke="rgba(0, 229, 255, 0.2)"
              strokeWidth="1.5"
            />
            <text x="690" y="240" fill="rgba(0, 229, 255, 0.4)" fontSize="11" fontWeight="700" letterSpacing="1">
              SALT LAKE WETLANDS
            </text>

            {/* Major Arteries (Major Arterial Road, EM Bypass, Ring Road) */}
            <path d="M 0 160 L 800 160" stroke="#1E2638" strokeWidth="14" strokeLinecap="round" />
            <path d="M 0 160 L 800 160" stroke="#2B364C" strokeWidth="2" strokeDasharray="8,6" />
            <text x="30" y="152" fill="#64748B" fontSize="9" fontWeight="800">MAJOR ARTERIAL ROAD (SECTOR V)</text>

            <path d="M 400 0 L 400 520" stroke="#1E2638" strokeWidth="12" />
            <path d="M 400 0 L 400 520" stroke="#2B364C" strokeWidth="1.5" strokeDasharray="6,6" />
            <text x="410" y="30" fill="#64748B" fontSize="9" fontWeight="800">RING ROAD / DP BLOCK AVENUE</text>

            <path d="M 120 0 L 680 520" stroke="#182030" strokeWidth="10" />
            <path d="M 60 480 L 760 80" stroke="#182030" strokeWidth="8" />

            {/* Street Grid Blocks (Tech Parks, Godrej Waterside, DLF, Webel, Technopolis) */}
            <rect x="240" y="190" width="130" height="110" rx="6" fill="#0D111A" stroke="rgba(255,255,255,0.06)" strokeWidth="1" />
            <text x="250" y="210" fill="#475569" fontSize="9" fontWeight="700">DP BLOCK (TECH)</text>

            <rect x="420" y="190" width="150" height="110" rx="6" fill="#0F1420" stroke="rgba(255,255,255,0.08)" strokeWidth="1.2" />
            <text x="430" y="210" fill="#94A3B8" fontSize="9.5" fontWeight="800">GODREJ WATERSIDE T1</text>
            <text x="430" y="224" fill="#64748B" fontSize="8">Tower 1 &amp; 2 Corporate Park</text>

            <rect x="240" y="60" width="130" height="80" rx="6" fill="#0D111A" stroke="rgba(255,255,255,0.06)" />
            <text x="250" y="80" fill="#475569" fontSize="9" fontWeight="700">WEBEL BHAVAN BLOCK</text>

            <rect x="420" y="60" width="150" height="80" rx="6" fill="#0D111A" stroke="rgba(255,255,255,0.06)" />
            <text x="430" y="80" fill="#475569" fontSize="9" fontWeight="700">TECHNOPOLIS HUB</text>

            <rect x="100" y="200" width="110" height="140" rx="6" fill="#0D111A" stroke="rgba(255,255,255,0.06)" />
            <text x="110" y="220" fill="#475569" fontSize="9" fontWeight="700">EP &amp; GP BLOCK</text>

            <rect x="420" y="330" width="150" height="130" rx="6" fill="#0D111A" stroke="rgba(255,255,255,0.06)" />
            <text x="430" y="350" fill="#475569" fontSize="9" fontWeight="700">COLLEGE MORE GRID</text>
          </g>

          {/* 2. PostGIS ST_DWithin Expanding Radial Wave Animation Layer */}
          {mapLayerFilters.postgis_wave && (
            <g id="postgisWaveLayer">
              {/* Concentric Search Perimeter Circles */}
              <circle
                cx={VICTIM_POS.x}
                cy={VICTIM_POS.y}
                r={radiusPx}
                fill="url(#postgisRadarFill)"
                stroke="#00E5FF"
                strokeWidth="1.8"
                strokeDasharray="4,4"
                opacity="0.85"
              />

              {/* Animated Pulsing Wave Ring */}
              <circle
                cx={VICTIM_POS.x}
                cy={VICTIM_POS.y}
                r={radiusPx * 0.9}
                fill="none"
                stroke="#00E5FF"
                strokeWidth="1.2"
                className="radar-ring-pulse"
                opacity="0.6"
              />

              <circle
                cx={VICTIM_POS.x}
                cy={VICTIM_POS.y}
                r={radiusPx * 0.4}
                fill="none"
                stroke="rgba(0, 229, 255, 0.4)"
                strokeWidth="1"
              />

              {/* PostGIS Distance Annotation Tag */}
              <g transform={`translate(${VICTIM_POS.x + radiusPx * 0.7}, ${VICTIM_POS.y - radiusPx * 0.7})`}>
                <rect x="0" y="-12" width="105" height="22" rx="11" fill="#091420" stroke="#00E5FF" strokeWidth="1" />
                <text x="8" y="3" fill="#00E5FF" fontSize="9.5" fontWeight="800">
                  ST_DWithin: {searchRadiusKm} km
                </text>
              </g>
            </g>
          )}

          {/* 3. Trajectory Rescue Routes Layer */}
          {mapLayerFilters.routes && (
            <g id="rescueRoutesLayer">
              {responderPositions.map((resp, idx) => {
                const isSelectedResp = idx === activeResponderIndex;
                return (
                  <g key={`route-${resp.id}`}>
                    {/* SVG Vector Path */}
                    <path
                      d={`M ${resp.x} ${resp.y} Q ${(resp.x + VICTIM_POS.x) / 2 + (idx === 0 ? 25 : -25)} ${(resp.y + VICTIM_POS.y) / 2} ${VICTIM_POS.x} ${VICTIM_POS.y}`}
                      fill="none"
                      stroke={isSelectedResp ? '#00E676' : 'rgba(0, 230, 118, 0.4)'}
                      strokeWidth={isSelectedResp ? 3 : 1.8}
                      strokeDasharray={isSelectedResp ? '6,4' : '4,4'}
                    />

                    {/* Midpoint ETA / Distance Badge */}
                    <g transform={`translate(${(resp.x + VICTIM_POS.x) / 2 + (idx === 0 ? 30 : -50)}, ${(resp.y + VICTIM_POS.y) / 2})`}>
                      <rect x="-4" y="-9" width="76" height="18" rx="9" fill="#091811" stroke="#00E676" strokeWidth="1" />
                      <text x="6" y="3" fill="#00E676" fontSize="9" fontWeight="800">
                        {resp.etaMinutes}m ({resp.distanceMeters}m)
                      </text>
                    </g>
                  </g>
                );
              })}
            </g>
          )}

          {/* 4. Hospitals & Trauma Centers Layer */}
          {mapLayerFilters.hospitals && (
            <g id="hospitalsLayer">
              {hospitalPositions.map((hosp) => {
                const isSelected = selectedMapEntity?.type === 'hospital' && selectedMapEntity.id === hosp.id;
                return (
                  <g
                    key={hosp.id}
                    transform={`translate(${hosp.x}, ${hosp.y})`}
                    onClick={() => {
                      soundEngine.playClick();
                      setSelectedMapEntity({ type: 'hospital', id: hosp.id, data: hosp });
                    }}
                    onMouseEnter={() => setHoveredEntityId(hosp.id)}
                    onMouseLeave={() => setHoveredEntityId(null)}
                    style={{ cursor: 'pointer' }}
                  >
                    {/* Hospital Marker Circle */}
                    <circle
                      cx="0"
                      cy="0"
                      r={isSelected ? 18 : 14}
                      fill="#0C1B2A"
                      stroke="#00E5FF"
                      strokeWidth={isSelected ? 2.5 : 1.8}
                    />
                    <text x="-5" y="4" fill="#00E5FF" fontSize="12" fontWeight="900">🏥</text>

                    {/* Hospital Tooltip Label */}
                    <g transform="translate(18, -10)">
                      <rect x="0" y="0" width="130" height="26" rx="6" fill="#081420" stroke="rgba(0, 229, 255, 0.4)" strokeWidth="1" />
                      <text x="8" y="11" fill="#FFFFFF" fontSize="9.5" fontWeight="800">{hosp.name.split(' ')[0]} Hospital</text>
                      <text x="8" y="21" fill="#00E5FF" fontSize="8" fontWeight="700">
                        {hosp.bedAvailability} Beds • {hosp.icuAvailability} ICU ({hosp.distanceKm}km)
                      </text>
                    </g>
                  </g>
                );
              })}
            </g>
          )}

          {/* 5. AED Mesh Nodes Layer */}
          {mapLayerFilters.aeds && (
            <g id="aedsLayer">
              {aedPositions.map((aed) => {
                const isSelected = selectedMapEntity?.type === 'aed' && selectedMapEntity.id === aed.id;
                return (
                  <g
                    key={aed.id}
                    transform={`translate(${aed.x}, ${aed.y})`}
                    onClick={() => {
                      soundEngine.playClick();
                      setSelectedMapEntity({ type: 'aed', id: aed.id, data: aed });
                    }}
                    onMouseEnter={() => setHoveredEntityId(aed.id)}
                    onMouseLeave={() => setHoveredEntityId(null)}
                    style={{ cursor: 'pointer' }}
                  >
                    <circle
                      cx="0"
                      cy="0"
                      r={isSelected ? 16 : 12}
                      fill="#211504"
                      stroke="#FFA000"
                      strokeWidth={isSelected ? 2.2 : 1.5}
                    />
                    <text x="-4" y="4" fill="#FFA000" fontSize="11" fontWeight="900">⚡</text>

                    {/* AED Label */}
                    <g transform="translate(14, -8)">
                      <rect x="0" y="0" width="105" height="18" rx="4" fill="#140E05" stroke="rgba(255, 160, 0, 0.4)" strokeWidth="1" />
                      <text x="6" y="12" fill="#FFA000" fontSize="8.5" fontWeight="800">
                        AED: {aed.distanceMeters}m away
                      </text>
                    </g>
                  </g>
                );
              })}
            </g>
          )}

          {/* 6. Responders Layer */}
          {mapLayerFilters.responders && (
            <g id="respondersLayer">
              {responderPositions.map((resp, idx) => {
                const isSelected = idx === activeResponderIndex || (selectedMapEntity?.type === 'responder' && selectedMapEntity.id === resp.id);
                return (
                  <g
                    key={resp.id}
                    transform={`translate(${resp.x}, ${resp.y})`}
                    onClick={() => {
                      soundEngine.playClick();
                      setActiveResponderIndex(idx);
                      setSelectedMapEntity({ type: 'responder', id: resp.id, data: resp });
                    }}
                    onMouseEnter={() => setHoveredEntityId(resp.id)}
                    onMouseLeave={() => setHoveredEntityId(null)}
                    style={{ cursor: 'pointer' }}
                  >
                    {/* Pulsing Beacon Ring for Active Responder */}
                    {isSelected && (
                      <circle
                        cx="0"
                        cy="0"
                        r="24"
                        fill="none"
                        stroke="#00E676"
                        strokeWidth="1.5"
                        className="radar-ring-pulse"
                        opacity="0.7"
                      />
                    )}

                    {/* Responder Avatar Base */}
                    <circle
                      cx="0"
                      cy="0"
                      r={isSelected ? 16 : 13}
                      fill="#061A0F"
                      stroke="#00E676"
                      strokeWidth={isSelected ? 2.5 : 1.8}
                      filter="url(#responderGlow)"
                    />
                    <text x="-4" y="4" fill="#00E676" fontSize="12">🏃</text>

                    {/* Responder Callout Tag */}
                    <g transform="translate(18, -14)">
                      <rect
                        x="0"
                        y="0"
                        width="128"
                        height="30"
                        rx="6"
                        fill="#07170E"
                        stroke={isSelected ? '#00E676' : 'rgba(0, 230, 118, 0.4)'}
                        strokeWidth={isSelected ? 1.5 : 1}
                      />
                      <text x="7" y="12" fill="#FFFFFF" fontSize="9.5" fontWeight="800">
                        {resp.name.split(' ')[0]} ({resp.role.split(' ')[0]})
                      </text>
                      <text x="7" y="23" fill="#00E676" fontSize="8.5" fontWeight="700">
                        Trust {resp.trustScore}% • ETA {resp.etaMinutes}m
                      </text>
                    </g>
                  </g>
                );
              })}
            </g>
          )}

          {/* 7. Victim SOS Central Target Marker Layer */}
          {mapLayerFilters.victim && (
            <g id="victimMarkerLayer" transform={`translate(${VICTIM_POS.x}, ${VICTIM_POS.y})`}>
              {/* Giant Multi-Ring Radar Ping */}
              <circle cx="0" cy="0" r="32" fill="none" stroke="#FF2A44" strokeWidth="1" className="radar-ring-pulse" opacity="0.4" />
              <circle cx="0" cy="0" r="22" fill="none" stroke="#FF2A44" strokeWidth="1.8" className="sos-breathing" opacity="0.8" />
              
              {/* Solid Center Bullseye */}
              <circle
                cx="0"
                cy="0"
                r="14"
                fill="#FF2A44"
                stroke="#FFFFFF"
                strokeWidth="2.5"
                filter="url(#victimGlow)"
                style={{ cursor: 'pointer' }}
                onClick={() => {
                  soundEngine.playClick();
                  setSelectedMapEntity({
                    type: 'victim',
                    id: 'victim-01',
                    data: {
                      victim: currentScenario.victim,
                      location: currentScenario.streetAddress,
                      condition: currentScenario.severityLabel,
                      confidence: currentScenario.aiConfidence,
                      survivalWindow: currentScenario.survivalWindowMinutes
                    }
                  });
                }}
              />
              <text x="-5" y="4" fill="#FFFFFF" fontSize="12" fontWeight="900">🚨</text>

              {/* High-Contrast Floating Victim Banner */}
              <g transform="translate(-85, -46)">
                <rect
                  x="0"
                  y="0"
                  width="170"
                  height="34"
                  rx="8"
                  fill="#1C060A"
                  stroke="#FF2A44"
                  strokeWidth="1.5"
                  filter="drop-shadow(0 4px 12px rgba(255, 42, 68, 0.4))"
                />
                <text x="10" y="14" fill="#FF2A44" fontSize="10.5" fontWeight="900">
                  SOS: {currentScenario.victim.name} (Level {currentScenario.severity})
                </text>
                <text x="10" y="27" fill="#CBD5E1" fontSize="9" fontWeight="700">
                  {currentScenario.streetAddress} • <tspan fill="#FFA000">T+{elapsedSeconds}s</tspan>
                </text>
              </g>
            </g>
          )}
        </svg>

        {/* Live SQL PostGIS Spatial Query Snippet HUD Overlay (Bottom-Left) */}
        {showSqlOverlay && (
          <div style={{
            position: 'absolute',
            bottom: '12px',
            left: '12px',
            width: '380px',
            backgroundColor: 'rgba(8, 10, 15, 0.94)',
            backdropFilter: 'blur(12px)',
            WebkitBackdropFilter: 'blur(12px)',
            borderRadius: '10px',
            border: '1px solid rgba(0, 229, 255, 0.3)',
            padding: '10px 12px',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.8)',
            zIndex: 10
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
              <span style={{ fontSize: '10px', fontWeight: 800, color: '#00E5FF', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Radio size={11} />
                <span>POSTGIS SPATIAL DISPATCH ENGINE</span>
              </span>
              <span className="font-mono" style={{ fontSize: '9px', color: '#00E676', backgroundColor: 'rgba(0, 230, 118, 0.15)', padding: '1px 5px', borderRadius: '4px' }}>
                11.4ms Latency
              </span>
            </div>

            <pre className="font-mono" style={{
              margin: 0,
              fontSize: '9px',
              lineHeight: 1.35,
              color: '#94A3B8',
              overflowX: 'auto',
              whiteSpace: 'pre-wrap',
              backgroundColor: '#030508',
              padding: '6px 8px',
              borderRadius: '6px',
              border: '1px solid rgba(255, 255, 255, 0.06)'
            }}>
              <span style={{ color: '#F43F5E' }}>SELECT</span> r.id, r.name, r.skills,
  <span style={{ color: '#38BDF8' }}>ST_Distance</span>(r.geom, <span style={{ color: '#38BDF8' }}>ST_MakePoint</span>({currentScenario.coordinates[1]}, {currentScenario.coordinates[0]})::geography) <span style={{ color: '#F43F5E' }}>AS</span> dist_m
<span style={{ color: '#F43F5E' }}>FROM</span> responders r
<span style={{ color: '#F43F5E' }}>WHERE</span> <span style={{ color: '#38BDF8' }}>ST_DWithin</span>(r.geom, <span style={{ color: '#38BDF8' }}>ST_MakePoint</span>({currentScenario.coordinates[1]}, {currentScenario.coordinates[0]})::geography, {(searchRadiusKm * 1000).toFixed(0)})
  <span style={{ color: '#F43F5E' }}>AND</span> <span style={{ color: '#FBBF24' }}>'CPR_CERTIFIED'</span> = <span style={{ color: '#38BDF8' }}>ANY</span>(r.skills)
<span style={{ color: '#F43F5E' }}>ORDER BY</span> dist_m <span style={{ color: '#F43F5E' }}>ASC LIMIT</span> 3;
            </pre>
          </div>
        )}

        {/* Selected Entity Details Drawer Card (Bottom-Right) */}
        {selectedMapEntity && (
          <div style={{
            position: 'absolute',
            bottom: '12px',
            right: '12px',
            width: '320px',
            backgroundColor: 'rgba(12, 14, 20, 0.96)',
            backdropFilter: 'blur(16px)',
            WebkitBackdropFilter: 'blur(16px)',
            borderRadius: '12px',
            border: '1px solid rgba(255, 255, 255, 0.15)',
            padding: '12px 14px',
            boxShadow: '0 12px 36px rgba(0, 0, 0, 0.85)',
            zIndex: 30,
            display: 'flex',
            flexDirection: 'column',
            gap: '8px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span style={{
                fontSize: '10px',
                fontWeight: 800,
                textTransform: 'uppercase',
                padding: '2px 7px',
                borderRadius: '4px',
                backgroundColor: selectedMapEntity.type === 'victim' ? 'rgba(255,42,68,0.2)' : selectedMapEntity.type === 'responder' ? 'rgba(0,230,118,0.2)' : selectedMapEntity.type === 'hospital' ? 'rgba(0,229,255,0.2)' : 'rgba(255,160,0,0.2)',
                color: selectedMapEntity.type === 'victim' ? '#FF2A44' : selectedMapEntity.type === 'responder' ? '#00E676' : selectedMapEntity.type === 'hospital' ? '#00E5FF' : '#FFA000',
                border: '1px solid currentColor'
              }}>
                {selectedMapEntity.type} Telemetry
              </span>

              <button
                onClick={() => setSelectedMapEntity(null)}
                style={{ color: '#94A3B8', padding: '2px', borderRadius: '50%', backgroundColor: 'transparent' }}
              >
                <X size={14} />
              </button>
            </div>

            {/* Entity Specific Body */}
            {selectedMapEntity.type === 'responder' && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <img
                    src={(selectedMapEntity.data as Responder).avatar}
                    alt=""
                    style={{ width: '38px', height: '38px', borderRadius: '50%', border: '2px solid #00E676' }}
                  />
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 800, color: '#FFFFFF' }}>{(selectedMapEntity.data as Responder).name}</div>
                    <div style={{ fontSize: '11px', color: '#94A3B8' }}>{(selectedMapEntity.data as Responder).role}</div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '6px', fontSize: '10.5px' }}>
                  <span style={{ color: '#00E676', fontWeight: 700 }}>ETA: {(selectedMapEntity.data as Responder).etaMinutes} mins</span>
                  <span>•</span>
                  <span style={{ color: '#CBD5E1' }}>Distance: {(selectedMapEntity.data as Responder).distanceMeters}m</span>
                  <span>•</span>
                  <span style={{ color: '#00E5FF' }}>Trust: {(selectedMapEntity.data as Responder).trustScore}%</span>
                </div>

                <div style={{ display: 'flex', gap: '6px', marginTop: '4px' }}>
                  <button
                    onClick={() => acceptDispatch()}
                    style={{
                      flex: 1,
                      padding: '7px',
                      borderRadius: '6px',
                      backgroundColor: '#00E676',
                      color: '#000000',
                      fontWeight: 800,
                      fontSize: '11.5px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '4px'
                    }}
                  >
                    <Check size={13} />
                    <span>Dispatch Volunteer</span>
                  </button>
                </div>
              </>
            )}

            {selectedMapEntity.type === 'hospital' && (
              <>
                <div style={{ fontSize: '13px', fontWeight: 800, color: '#FFFFFF' }}>{(selectedMapEntity.data as HospitalNode).name}</div>
                <div style={{ fontSize: '10.5px', color: '#00E5FF' }}>{(selectedMapEntity.data as HospitalNode).traumaLevel}</div>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '6px',
                  backgroundColor: '#081018',
                  padding: '6px 8px',
                  borderRadius: '6px'
                }}>
                  <div style={{ fontSize: '11px' }}>Available Beds: <strong style={{ color: '#00E676' }}>{(selectedMapEntity.data as HospitalNode).bedAvailability}</strong></div>
                  <div style={{ fontSize: '11px' }}>ICU Beds: <strong style={{ color: '#00E5FF' }}>{(selectedMapEntity.data as HospitalNode).icuAvailability}</strong></div>
                </div>
                <button
                  onClick={() => trigger108Escalation()}
                  style={{
                    padding: '7px',
                    borderRadius: '6px',
                    backgroundColor: '#00E5FF',
                    color: '#000000',
                    fontWeight: 800,
                    fontSize: '11.5px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '4px'
                  }}
                >
                  <Ambulance size={13} />
                  <span>Route 108 ALS to Hospital</span>
                </button>
              </>
            )}

            {selectedMapEntity.type === 'aed' && (
              <>
                <div style={{ fontSize: '13px', fontWeight: 800, color: '#FFA000' }}>Automated External Defibrillator</div>
                <div style={{ fontSize: '11px', color: '#CBD5E1' }}>{(selectedMapEntity.data as AEDNode).locationName}</div>
                <div style={{ fontSize: '10px', color: '#94A3B8', backgroundColor: '#140E05', padding: '5px 8px', borderRadius: '4px' }}>
                  {(selectedMapEntity.data as AEDNode).accessNotes}
                </div>
                <button
                  onClick={() => toggleAedAttached()}
                  style={{
                    padding: '7px',
                    borderRadius: '6px',
                    backgroundColor: '#FFA000',
                    color: '#000000',
                    fontWeight: 800,
                    fontSize: '11.5px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '4px'
                  }}
                >
                  <Zap size={13} />
                  <span>{aedAttached ? 'AED Attached & Active' : 'Simulate AED Deployment'}</span>
                </button>
              </>
            )}

            {selectedMapEntity.type === 'victim' && (
              <>
                <div style={{ fontSize: '13px', fontWeight: 800, color: '#FF2A44' }}>{currentScenario.victim.name} ({currentScenario.victim.age} M)</div>
                <div style={{ fontSize: '11px', color: '#CBD5E1' }}>{currentScenario.severityLabel}</div>
                <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>
                  Blood: <strong style={{ color: '#FFFFFF' }}>{currentScenario.victim.bloodType}</strong> • Kin: <strong style={{ color: '#FFFFFF' }}>{currentScenario.victim.emergencyContactPhone}</strong>
                </div>
                <button
                  onClick={() => broadcastAlert()}
                  style={{
                    padding: '7px',
                    borderRadius: '6px',
                    backgroundColor: '#FF2A44',
                    color: '#FFFFFF',
                    fontWeight: 800,
                    fontSize: '11.5px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '4px'
                  }}
                >
                  <Radio size={13} />
                  <span>Broadcast Priority Community Alarm</span>
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
