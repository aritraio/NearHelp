/* ==========================================================================
   NearHelp AI — Screen 5B: Two-Way Real-Time Incident Chat & Event Timeline
   File: src/components/responder/ResponderTimelineChatScreen.tsx
   ========================================================================== */

import React, { useState, useRef, useEffect } from 'react';
import { useDemoStore } from '../../store/DemoContext';
import { 
  Clock, 
  Send, 
  Globe, 
  Sparkles, 
  MessageSquare 
} from 'lucide-react';
import { soundEngine } from '../../utils/audio';

const QUICK_TRANSMISSIONS = [
  "En route with CPR kit — 2 mins away",
  "Bring AED from Webel security gate immediately",
  "Arrived at Tower 1 lobby elevator",
  "Starting chest compressions at 110 BPM",
  "Patient pulse palpated, preparing 108 handover"
];

export const ResponderTimelineChatScreen: React.FC = () => {
  const { 
    timelineEvents, 
    responderChatMessages, 
    sendResponderChatMessage,
    currentScenario,
    activeResponderIndex
  } = useDemoStore();

  const [activeTab, setActiveTab] = useState<'CHAT' | 'TIMELINE'>('CHAT');
  const [inputMessage, setInputMessage] = useState<string>('');
  const chatBottomRef = useRef<HTMLDivElement>(null);

  const activeResponder = currentScenario.responders[activeResponderIndex] || currentScenario.responders[0];

  // Auto-scroll to bottom of chat
  useEffect(() => {
    if (activeTab === 'CHAT' && chatBottomRef.current) {
      chatBottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [responderChatMessages, activeTab]);

  const handleSendMessage = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputMessage.trim()) return;
    sendResponderChatMessage(inputMessage.trim(), 'responder');
    setInputMessage('');
  };

  const handleSendCanned = (text: string) => {
    soundEngine.playClick();
    sendResponderChatMessage(text, 'responder');
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: '#000000',
      color: 'var(--text-primary)',
      overflow: 'hidden'
    }}>
      {/* 1. Header Sub-Switch: Live Chat vs Automated Milestone Timeline */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '4px',
        backgroundColor: '#0A0C10',
        padding: '6px 10px',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)'
      }}>
        <button
          onClick={() => {
            soundEngine.playClick();
            setActiveTab('CHAT');
          }}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
            padding: '6px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: activeTab === 'CHAT' ? 'rgba(0, 230, 118, 0.2)' : 'transparent',
            color: activeTab === 'CHAT' ? '#00E676' : '#94A3B8',
            fontWeight: activeTab === 'CHAT' ? 800 : 600,
            fontSize: '11px',
            border: activeTab === 'CHAT' ? '1px solid #00E676' : '1px solid transparent',
            cursor: 'pointer'
          }}
        >
          <MessageSquare size={13} />
          <span>Incident Comms ({responderChatMessages.length})</span>
        </button>

        <button
          onClick={() => {
            soundEngine.playClick();
            setActiveTab('TIMELINE');
          }}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
            padding: '6px',
            borderRadius: 'var(--radius-xs)',
            backgroundColor: activeTab === 'TIMELINE' ? 'rgba(0, 229, 255, 0.2)' : 'transparent',
            color: activeTab === 'TIMELINE' ? '#00E5FF' : '#94A3B8',
            fontWeight: activeTab === 'TIMELINE' ? 800 : 600,
            fontSize: '11px',
            border: activeTab === 'TIMELINE' ? '1px solid #00E5FF' : '1px solid transparent',
            cursor: 'pointer'
          }}
        >
          <Clock size={13} />
          <span>Milestone Timeline</span>
        </button>
      </div>

      {/* 2. Chat Feed Tab */}
      {activeTab === 'CHAT' && (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          minHeight: 0
        }}>
          {/* Translation Notice Banner */}
          <div style={{
            backgroundColor: 'rgba(0, 229, 255, 0.08)',
            borderBottom: '1px solid rgba(0, 229, 255, 0.2)',
            padding: '5px 10px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontSize: '10px',
            color: '#00E5FF'
          }}>
            <Globe size={11} />
            <span>AI Real-time Translation Active: Bengali (বাংলা) ⇄ English (English)</span>
          </div>

          {/* Messages Scroll Area */}
          <div style={{
            flex: 1,
            overflowY: 'auto',
            padding: '10px',
            display: 'flex',
            flexDirection: 'column',
            gap: '8px'
          }}>
            {responderChatMessages.map((msg) => {
              const isMe = msg.sender === 'responder';
              const isSystem = msg.sender === 'system';
              const is108 = msg.sender === 'dispatcher_108';

              if (isSystem) {
                return (
                  <div
                    key={msg.id}
                    style={{
                      backgroundColor: 'rgba(255, 255, 255, 0.04)',
                      border: '1px solid rgba(255, 255, 255, 0.08)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '8px 10px',
                      fontSize: '11px',
                      color: msg.badgeColor || '#FFA000',
                      lineHeight: '1.4'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2px' }}>
                      <strong style={{ fontSize: '10px', letterSpacing: '0.04em' }}>
                        🤖 {msg.senderName.toUpperCase()}
                      </strong>
                      <span style={{ fontSize: '9px', color: '#64748B' }}>{msg.timestamp}</span>
                    </div>
                    <div>{msg.text}</div>
                  </div>
                );
              }

              return (
                <div
                  key={msg.id}
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: isMe ? 'flex-end' : 'flex-start',
                    maxWidth: '88%',
                    alignSelf: isMe ? 'flex-end' : 'flex-start'
                  }}
                >
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    marginBottom: '2px',
                    fontSize: '10px',
                    color: '#94A3B8'
                  }}>
                    <span style={{
                      color: isMe ? '#00E676' : is108 ? '#00E5FF' : '#FF2A44',
                      fontWeight: 700
                    }}>
                      {msg.senderName}
                    </span>
                    <span>•</span>
                    <span style={{ fontSize: '9px' }}>{msg.timestamp}</span>
                  </div>

                  <div style={{
                    backgroundColor: isMe 
                      ? 'rgba(0, 230, 118, 0.15)' 
                      : is108 
                      ? 'rgba(0, 229, 255, 0.15)' 
                      : 'rgba(255, 42, 68, 0.12)',
                    border: `1px solid ${isMe ? '#00E676' : is108 ? '#00E5FF' : '#FF2A44'}`,
                    borderRadius: isMe ? '12px 12px 2px 12px' : '12px 12px 12px 2px',
                    padding: '8px 10px',
                    color: '#FFFFFF',
                    fontSize: '12px',
                    lineHeight: '1.4'
                  }}>
                    <div>{msg.text}</div>
                    {msg.translatedText && (
                      <div style={{
                        marginTop: '4px',
                        paddingTop: '4px',
                        borderTop: '1px dashed rgba(255, 255, 255, 0.2)',
                        fontSize: '10.5px',
                        color: '#00E5FF',
                        fontStyle: 'italic'
                      }}>
                        Translated: "{msg.translatedText}"
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
            <div ref={chatBottomRef} />
          </div>

          {/* Canned Quick Response Chips */}
          <div style={{
            backgroundColor: '#0C0D10',
            padding: '6px 8px',
            borderTop: '1px solid rgba(255, 255, 255, 0.08)',
            display: 'flex',
            gap: '5px',
            overflowX: 'auto',
            whiteSpace: 'nowrap'
          }}>
            {QUICK_TRANSMISSIONS.map((text, idx) => (
              <button
                key={idx}
                onClick={() => handleSendCanned(text)}
                style={{
                  padding: '4px 8px',
                  borderRadius: 'var(--radius-xs)',
                  backgroundColor: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                  color: '#CBD5E1',
                  fontSize: '10.5px',
                  cursor: 'pointer',
                  flexShrink: 0
                }}
              >
                + {text}
              </button>
            ))}
          </div>

          {/* Interactive Chat Input */}
          <form
            onSubmit={handleSendMessage}
            style={{
              backgroundColor: '#0A0C10',
              padding: '8px 10px',
              borderTop: '1px solid rgba(255, 255, 255, 0.08)',
              display: 'flex',
              gap: '6px',
              alignItems: 'center'
            }}
          >
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder={`Message scene as ${activeResponder.name.split(' ')[0]}...`}
              style={{
                flex: 1,
                padding: '8px 10px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: '#121418',
                border: '1px solid rgba(255, 255, 255, 0.15)',
                color: '#FFFFFF',
                fontSize: '11.5px',
                outline: 'none'
              }}
            />
            <button
              type="submit"
              disabled={!inputMessage.trim()}
              style={{
                padding: '8px 12px',
                borderRadius: 'var(--radius-xs)',
                backgroundColor: inputMessage.trim() ? '#00E676' : 'rgba(255, 255, 255, 0.05)',
                border: 'none',
                color: inputMessage.trim() ? '#000000' : '#64748B',
                fontWeight: 800,
                fontSize: '11.5px',
                cursor: inputMessage.trim() ? 'pointer' : 'not-allowed',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Send size={12} />
              <span>Send</span>
            </button>
          </form>
        </div>
      )}

      {/* 3. Automated Milestone Timeline Tab */}
      {activeTab === 'TIMELINE' && (
        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '12px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px'
        }}>
          <div style={{
            fontSize: '11px',
            color: '#94A3B8',
            marginBottom: '4px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}>
            <Sparkles size={12} color="#00E5FF" />
            <span>Automated Blockchain/PostGIS Milestone Audit Trail:</span>
          </div>

          <div style={{ position: 'relative', paddingLeft: '18px' }}>
            {/* Vertical timeline line */}
            <div style={{
              position: 'absolute',
              left: '6px',
              top: '4px',
              bottom: '4px',
              width: '2px',
              backgroundColor: 'rgba(255, 255, 255, 0.1)'
            }} />

            {timelineEvents.map((event) => {
              return (
                <div
                  key={event.id}
                  style={{
                    position: 'relative',
                    marginBottom: '14px',
                    opacity: event.isComplete ? 1 : 0.45
                  }}
                >
                  {/* Timeline bullet dot */}
                  <div style={{
                    position: 'absolute',
                    left: '-18px',
                    top: '2px',
                    width: '14px',
                    height: '14px',
                    borderRadius: '50%',
                    backgroundColor: event.isComplete ? '#00E676' : '#1E232F',
                    border: `2px solid ${event.isComplete ? '#00E676' : '#64748B'}`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    {event.isComplete && (
                      <div style={{ width: '4px', height: '4px', borderRadius: '50%', backgroundColor: '#000000' }} />
                    )}
                  </div>

                  <div style={{
                    backgroundColor: '#0C0D10',
                    border: `1px solid ${event.isComplete ? 'rgba(0, 230, 118, 0.25)' : 'rgba(255, 255, 255, 0.06)'}`,
                    borderRadius: 'var(--radius-sm)',
                    padding: '8px 10px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '3px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: '10px', fontWeight: 800, color: event.isComplete ? '#00E676' : '#64748B' }}>
                        {event.timestampOffset} ({event.timeIso})
                      </span>
                      <span style={{
                        fontSize: '9px',
                        padding: '1px 5px',
                        borderRadius: 'var(--radius-xs)',
                        backgroundColor: 'rgba(255, 255, 255, 0.05)',
                        color: '#94A3B8',
                        fontWeight: 600
                      }}>
                        {event.author}
                      </span>
                    </div>

                    <div style={{ fontSize: '12px', fontWeight: 700, color: '#FFFFFF' }}>
                      {event.title}
                    </div>

                    <div style={{ fontSize: '11px', color: '#94A3B8', lineHeight: '1.35' }}>
                      {event.description}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
