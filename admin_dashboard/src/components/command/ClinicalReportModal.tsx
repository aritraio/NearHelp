/* ==========================================================================
   NearHelp AI — AI Clinical Handover Report & Section 134A Legal Immunity Seal
   File: src/components/command/ClinicalReportModal.tsx
   ========================================================================== */

import React, { useState } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { soundEngine } from '../../utils/audio';
import { 
  FileText, 
  ShieldCheck, 
  Copy, 
  Printer, 
  X, 
  Activity, 
  Ambulance, 
  QrCode, 
  Sparkles,
  Lock
} from 'lucide-react';

export const ClinicalReportModal: React.FC = () => {
  const { 
    isClinicalReportModalOpen, 
    setIsClinicalReportModalOpen, 
    activeHandoverReport
  } = useDemoStore();

  const [copied, setCopied] = useState<boolean>(false);

  if (!isClinicalReportModalOpen || !activeHandoverReport) return null;

  const handleCopyMarkdown = () => {
    soundEngine.playSuccessChime();
    const markdownContent = `
# NearHelp AI — Clinical Handover & Good Samaritan Immunity Certificate
**Report ID**: ${activeHandoverReport.reportId} | **Incident**: ${activeHandoverReport.incidentCode}
**Generated**: ${activeHandoverReport.generatedAt}
**Location**: ${activeHandoverReport.emergencyLocation} (${activeHandoverReport.emergencyCoordinates})

## 1. Patient Demographics & Encrypted Medical ID
- **Name**: ${activeHandoverReport.victimName} (${activeHandoverReport.victimAge} y/o ${activeHandoverReport.victimGender})
- **Blood Group**: ${activeHandoverReport.victimBloodType}
- **Known Allergies**: ${activeHandoverReport.victimAllergies.join(', ')}
- **Medical Conditions**: ${activeHandoverReport.victimMedicalConditions.join(', ')}
- **Pacemaker**: ${activeHandoverReport.hasPacemaker ? 'ACTIVE / PRESENT' : 'None'}

## 2. AI Clinical Triage & Urgency Classification
- **Diagnosis**: ${activeHandoverReport.diagnosticSummary}
- **Severity**: Level ${activeHandoverReport.severityLevel} / 5
- **AI Clinical Confidence**: ${activeHandoverReport.aiConfidenceScore}%
- **Reported Symptoms**: ${activeHandoverReport.reportedSymptoms.join('; ')}

## 3. Bystander Interventions & Resuscitation Log
- **CPR Metronome**: ${activeHandoverReport.cprMetronomeUsed ? 'Active at 110 BPM (AHA/IRC BLS Guidelines)' : 'N/A'}
- **Estimated Compressions**: ${activeHandoverReport.cprCompressionsEstimated} compressions over ${activeHandoverReport.cprDurationSeconds}s
- **AED Deployed**: ${activeHandoverReport.aedDeployed ? `Yes (${activeHandoverReport.aedShocksDelivered} shock delivered)` : 'Not Deployed'}
- **Primary Responder**: ${activeHandoverReport.responderAssigned} (${activeHandoverReport.responderRole})

## 4. 108 Emergency EMS Handover
- **Ambulance Unit**: ${activeHandoverReport.ambulanceUnit}
- **Lead Paramedic**: ${activeHandoverReport.handoverParamedicLeader}
- **Destination**: ${activeHandoverReport.destinationHospital}
- **Handover Time**: ${activeHandoverReport.handoverTimestamp}

## 5. Legal Immunity Compliance Seal
${activeHandoverReport.legalShieldCompliance}
${activeHandoverReport.goodSamaritanActReference}
**Digital Hash**: ${activeHandoverReport.digitalSignatureHash}
`;
    navigator.clipboard?.writeText(markdownContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.85)',
      backdropFilter: 'blur(16px)',
      WebkitBackdropFilter: 'blur(16px)',
      zIndex: 2000,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px'
    }}>
      {/* Modal Container */}
      <div style={{
        width: '100%',
        maxWidth: '820px',
        maxHeight: '90vh',
        backgroundColor: '#0A0C10',
        borderRadius: '16px',
        border: '1px solid rgba(0, 229, 255, 0.3)',
        boxShadow: '0 24px 60px rgba(0, 0, 0, 0.9), 0 0 40px rgba(0, 229, 255, 0.15)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
      }}>
        {/* Modal Top Header */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '14px 18px',
          backgroundColor: '#0F131C',
          borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <FileText size={18} color="#00E5FF" />
            <div>
              <div style={{ fontSize: '14px', fontWeight: 800, color: '#FFFFFF', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span>AI Clinical Handover &amp; Good Samaritan Certificate</span>
                <span style={{ fontSize: '10px', padding: '2px 6px', borderRadius: '4px', backgroundColor: 'rgba(0, 230, 118, 0.15)', color: '#00E676', border: '1px solid #00E676' }}>
                  SEC 134A VERIFIED
                </span>
              </div>
              <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>
                Official Automated Clinical Audit Trail for Emergency Medical Services (EMS 108 Handover)
              </div>
            </div>
          </div>

          <button
            onClick={() => {
              soundEngine.playClick();
              setIsClinicalReportModalOpen(false);
            }}
            style={{
              padding: '6px',
              borderRadius: '50%',
              backgroundColor: '#1A1E29',
              color: '#94A3B8',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              cursor: 'pointer'
            }}
          >
            <X size={16} />
          </button>
        </div>

        {/* Modal Scrollable Document Body */}
        <div style={{
          padding: '18px 22px',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
          backgroundColor: '#060709',
          color: '#E2E8F0',
          fontSize: '12px'
        }}>
          {/* Official Document Banner */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 16px',
            backgroundColor: '#0D111A',
            borderRadius: '10px',
            border: '1px solid rgba(255, 255, 255, 0.08)'
          }}>
            <div>
              <div className="font-mono" style={{ fontSize: '11px', color: '#00E5FF', fontWeight: 800 }}>
                DOCUMENT ID: {activeHandoverReport.reportId}
              </div>
              <div style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF', marginTop: '2px' }}>
                Incident: {activeHandoverReport.incidentCode} • {activeHandoverReport.emergencyLocation}
              </div>
              <div style={{ fontSize: '10.5px', color: '#94A3B8', marginTop: '2px' }}>
                Generated at {activeHandoverReport.generatedAt}
              </div>
            </div>

            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '10px', color: '#FFA000', fontWeight: 800 }}>GPS LOCK</div>
              <div className="font-mono" style={{ fontSize: '10px', color: '#CBD5E1' }}>{activeHandoverReport.emergencyCoordinates}</div>
            </div>
          </div>

          {/* Section 134A Good Samaritan Legal Immunity Seal */}
          <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '12px',
            padding: '12px 14px',
            backgroundColor: 'rgba(0, 230, 118, 0.06)',
            borderRadius: '10px',
            border: '1px solid rgba(0, 230, 118, 0.3)'
          }}>
            <ShieldCheck size={28} color="#00E676" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontSize: '12.5px', fontWeight: 800, color: '#00E676' }}>
                Statutory Good Samaritan Legal Protection Granted (Section 134A MV Act)
              </div>
              <p style={{ fontSize: '11px', color: '#CBD5E1', margin: '3px 0 0 0', lineHeight: 1.4 }}>
                Under Section 134A of the Motor Vehicles (Amendment) Act 2019 and Supreme Court of India 2016 Good Samaritan Directives, 
                all bystander responders (<strong>{activeHandoverReport.responderAssigned}</strong>) are granted absolute civil and criminal immunity. 
                No police questioning, detention, or financial liability may be imposed.
              </p>
            </div>
          </div>

          {/* Grid Layout: Patient ID & AI Diagnostic */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            {/* Left: Patient Profile & Encrypted Medical ID */}
            <div style={{
              backgroundColor: '#0C0F17',
              borderRadius: '10px',
              padding: '12px 14px',
              border: '1px solid rgba(255, 255, 255, 0.08)'
            }}>
              <div style={{ fontSize: '11.5px', fontWeight: 800, color: '#FFFFFF', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '5px' }}>
                <Lock size={13} color="#00E5FF" />
                <span>Encrypted Patient Medical Profile</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', fontSize: '11px' }}>
                <div>Patient: <strong style={{ color: '#FFFFFF' }}>{activeHandoverReport.victimName}</strong> ({activeHandoverReport.victimAge} {activeHandoverReport.victimGender})</div>
                <div>Blood Group: <strong style={{ color: '#FF2A44' }}>{activeHandoverReport.victimBloodType}</strong></div>
                <div>Allergies: <strong style={{ color: '#FFA000' }}>{activeHandoverReport.victimAllergies.join(', ')}</strong></div>
                <div>Pre-existing: <strong style={{ color: '#E2E8F0' }}>{activeHandoverReport.victimMedicalConditions.join(', ')}</strong></div>
                <div>Pacemaker: <strong style={{ color: activeHandoverReport.hasPacemaker ? '#00E676' : '#94A3B8' }}>{activeHandoverReport.hasPacemaker ? 'YES' : 'NO'}</strong></div>
              </div>
            </div>

            {/* Right: AI Triage & Severity Score */}
            <div style={{
              backgroundColor: '#0C0F17',
              borderRadius: '10px',
              padding: '12px 14px',
              border: '1px solid rgba(255, 255, 255, 0.08)'
            }}>
              <div style={{ fontSize: '11.5px', fontWeight: 800, color: '#FFFFFF', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '5px' }}>
                <Sparkles size={13} color="#FF2A44" />
                <span>AI Clinical Triage Diagnostic</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', fontSize: '11px' }}>
                <div>Severity: <strong style={{ color: '#FF2A44' }}>Level {activeHandoverReport.severityLevel} / 5 (Critical)</strong></div>
                <div>Diagnosis: <strong style={{ color: '#FFFFFF' }}>{activeHandoverReport.diagnosticSummary}</strong></div>
                <div>AI Clinical Confidence: <strong style={{ color: '#00E5FF' }}>{activeHandoverReport.aiConfidenceScore}%</strong></div>
                <div>Hypoxic Window: <strong style={{ color: '#FFA000' }}>{activeHandoverReport.survivalWindowMinutes} mins</strong></div>
                <div>Reported Symptoms: <span style={{ color: '#94A3B8' }}>{activeHandoverReport.reportedSymptoms.join('; ')}</span></div>
              </div>
            </div>
          </div>

          {/* Bystander Interventions & Resuscitation Log */}
          <div style={{
            backgroundColor: '#0C0F17',
            borderRadius: '10px',
            padding: '12px 14px',
            border: '1px solid rgba(255, 255, 255, 0.08)'
          }}>
            <div style={{ fontSize: '11.5px', fontWeight: 800, color: '#FFFFFF', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '5px' }}>
              <Activity size={13} color="#00E676" />
              <span>Bystander Resuscitation &amp; BLS Timeline</span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px', fontSize: '11px' }}>
              <div style={{ backgroundColor: '#07090D', padding: '8px', borderRadius: '6px' }}>
                <div style={{ color: '#94A3B8', fontSize: '10px' }}>CPR METRONOME</div>
                <div style={{ fontWeight: 800, color: '#00E676', marginTop: '2px' }}>110 BPM (AHA/IRC)</div>
                <div style={{ color: '#64748B', fontSize: '9.5px' }}>~{activeHandoverReport.cprCompressionsEstimated} compressions ({activeHandoverReport.cprDurationSeconds}s)</div>
              </div>

              <div style={{ backgroundColor: '#07090D', padding: '8px', borderRadius: '6px' }}>
                <div style={{ color: '#94A3B8', fontSize: '10px' }}>AED DEFIBRILLATOR</div>
                <div style={{ fontWeight: 800, color: '#FFA000', marginTop: '2px' }}>
                  {activeHandoverReport.aedDeployed ? `${activeHandoverReport.aedShocksDelivered} Shock Delivered` : 'Not Deployed'}
                </div>
                <div style={{ color: '#64748B', fontSize: '9.5px' }}>Webel Bhavan AED unit</div>
              </div>

              <div style={{ backgroundColor: '#07090D', padding: '8px', borderRadius: '6px' }}>
                <div style={{ color: '#94A3B8', fontSize: '10px' }}>VOLUNTEER ON SCENE</div>
                <div style={{ fontWeight: 800, color: '#FFFFFF', marginTop: '2px' }}>{activeHandoverReport.responderAssigned}</div>
                <div style={{ color: '#00E676', fontSize: '9.5px' }}>{activeHandoverReport.responderArrivalTimeOffset}</div>
              </div>
            </div>
          </div>

          {/* 108 EMS Ambulance Handover Sign-off */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 14px',
            backgroundColor: '#0F1420',
            borderRadius: '10px',
            border: '1px solid rgba(0, 229, 255, 0.25)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Ambulance size={22} color="#00E5FF" />
              <div>
                <div style={{ fontSize: '12px', fontWeight: 800, color: '#FFFFFF' }}>
                  Handover to {activeHandoverReport.ambulanceUnit}
                </div>
                <div style={{ fontSize: '10.5px', color: '#94A3B8' }}>
                  Lead Paramedic: <strong style={{ color: '#FFFFFF' }}>{activeHandoverReport.handoverParamedicLeader}</strong> • Destination: <strong style={{ color: '#00E5FF' }}>{activeHandoverReport.destinationHospital}</strong>
                </div>
              </div>
            </div>

            <div className="font-mono" style={{ fontSize: '9px', color: '#64748B', textAlign: 'right' }}>
              <div>STATUS: {activeHandoverReport.handoverTimestamp}</div>
              <div style={{ color: '#00E676', fontWeight: 700 }}>SIGN-OFF VERIFIED</div>
            </div>
          </div>

          {/* Digital Signature & QR Verification */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '8px 12px',
            backgroundColor: '#040608',
            borderRadius: '8px',
            border: '1px dashed rgba(255, 255, 255, 0.1)',
            fontSize: '9.5px',
            color: '#64748B'
          }}>
            <div className="font-mono">
              DIGITAL HASH: <span style={{ color: '#94A3B8' }}>{activeHandoverReport.digitalSignatureHash}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#00E676', fontWeight: 800 }}>
              <QrCode size={12} />
              <span>NEARHELP BLOCKCHAIN AUDIT LOG</span>
            </div>
          </div>
        </div>

        {/* Modal Footer Actions */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '12px 18px',
          backgroundColor: '#0F131C',
          borderTop: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          <div style={{ fontSize: '11px', color: '#94A3B8' }}>
            Ready for hospital clinical handover and police Good Samaritan certification.
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              onClick={handleCopyMarkdown}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                padding: '7px 12px',
                borderRadius: '6px',
                backgroundColor: '#1A1E29',
                color: '#CBD5E1',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                fontSize: '11.5px',
                fontWeight: 700,
                cursor: 'pointer'
              }}
            >
              <Copy size={13} />
              <span>{copied ? 'Copied Markdown!' : 'Copy Markdown'}</span>
            </button>

            <button
              onClick={() => {
                soundEngine.playSuccessChime();
                window.print?.();
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                padding: '7px 14px',
                borderRadius: '6px',
                backgroundColor: '#00E5FF',
                color: '#000000',
                fontWeight: 800,
                fontSize: '11.5px',
                cursor: 'pointer',
                boxShadow: '0 2px 10px rgba(0, 229, 255, 0.3)'
              }}
            >
              <Printer size={13} />
              <span>Print Certificate</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
