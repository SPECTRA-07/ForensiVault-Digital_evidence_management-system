import React, { useState, useEffect } from 'react';
import { ClipboardList, Shield, User, Clock, Terminal, CheckCircle2, AlertTriangle, FileCode } from 'lucide-react';
import Modal from '../Modal';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import auditService from '../../services/auditService';

export const AuditDetailsModal = ({ isOpen, onClose, auditId }) => {
  const [logData, setLogData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && auditId) {
      const fetchLog = async () => {
        setLoading(true);
        setError(null);
        try {
          const res = await auditService.getAuditLogById(auditId);
          if (res && res.data) {
            setLogData(res.data);
          }
        } catch (err) {
          setError(err);
        } finally {
          setLoading(false);
        }
      };
      fetchLog();
    }
  }, [isOpen, auditId]);

  const formatJsonStr = (str) => {
    if (!str) return '--';
    try {
      const parsed = JSON.parse(str);
      return JSON.stringify(parsed, null, 2);
    } catch (e) {
      return str;
    }
  };

  if (!isOpen) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Immutable Forensic Audit Trail Inspection Record" maxWidth="720px">
      {loading ? (
        <LoadingSpinner message="Querying tamper-evident immutable audit log..." />
      ) : logData ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', fontSize: '0.875rem' }}>
          {/* Header Summary Banner */}
          <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
              <div>
                <span className="font-mono" style={{ fontWeight: 800, fontSize: '1.15rem', color: 'var(--color-navy-900)' }}>
                  {logData.auditNumber || `#AUD-${logData.id}`}
                </span>
                <div style={{ fontWeight: 600, marginTop: '0.2rem', color: 'var(--color-navy-800)' }}>
                  {logData.description || 'System Audit Event'}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <Badge status="INFO">{logData.action}</Badge>
                <Badge status={logData.status}>{logData.status}</Badge>
              </div>
            </div>
          </div>

          {/* Audit Metadata Overview */}
          <div className="card" style={{ padding: '1rem' }}>
            <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
              Audit Execution Metadata
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Domain Entity Type:</span>
                <div className="font-mono" style={{ fontWeight: 600 }}>{logData.entityType || '--'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Module Name:</span>
                <div><Badge status="SUBMITTED">{logData.moduleName || 'CORE'}</Badge></div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Entity Reference:</span>
                <div className="font-mono">{logData.entityReference || (logData.entityId ? `#${logData.entityId}` : '--')}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Performer Username:</span>
                <div style={{ fontWeight: 600 }}>{logData.username || logData.performedBy?.email || 'System'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>User Role / Security Context:</span>
                <div><Badge status="INFO">{logData.userRole || logData.performedBy?.role || 'SYSTEM'}</Badge></div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Client IP Address:</span>
                <div className="font-mono">{logData.ipAddress || '127.0.0.1'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Correlation Trace ID:</span>
                <div className="font-mono" style={{ fontSize: '0.75rem', wordBreak: 'break-all' }}>{logData.correlationId || '--'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Execution Duration:</span>
                <div className="font-mono">{logData.executionTimeMs ? `${logData.executionTimeMs} ms` : '--'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Action Timestamp:</span>
                <div className="font-mono" style={{ fontSize: '0.75rem' }}>{logData.actionTimestamp ? new Date(logData.actionTimestamp).toLocaleString() : '--'}</div>
              </div>
            </div>

            {logData.userAgent && (
              <div style={{ marginTop: '0.75rem', fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
                <strong>User Agent:</strong> {logData.userAgent}
              </div>
            )}

            {logData.failureReason && (
              <div style={{ marginTop: '0.75rem', backgroundColor: 'var(--color-danger-50)', padding: '0.625rem 0.75rem', borderRadius: '4px', borderLeft: '4px solid var(--color-danger-600)', color: 'var(--color-danger-700)', fontSize: '0.8125rem' }}>
                <strong>Failure Diagnostic Reason:</strong> {logData.failureReason}
              </div>
            )}
          </div>

          {/* State Diff Section: Previous vs New Value */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="card" style={{ padding: '0.875rem' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-600)', textTransform: 'uppercase', display: 'block', marginBottom: '0.35rem' }}>
                Previous State Snapshot
              </span>
              <pre
                className="font-mono"
                style={{
                  fontSize: '0.75rem',
                  backgroundColor: 'var(--color-slate-900)',
                  color: '#38bdf8',
                  padding: '0.625rem',
                  borderRadius: '4px',
                  maxHeight: '180px',
                  overflowY: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {formatJsonStr(logData.previousValue)}
              </pre>
            </div>

            <div className="card" style={{ padding: '0.875rem' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-600)', textTransform: 'uppercase', display: 'block', marginBottom: '0.35rem' }}>
                New State Snapshot
              </span>
              <pre
                className="font-mono"
                style={{
                  fontSize: '0.75rem',
                  backgroundColor: 'var(--color-slate-900)',
                  color: '#4ade80',
                  padding: '0.625rem',
                  borderRadius: '4px',
                  maxHeight: '180px',
                  overflowY: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {formatJsonStr(logData.newValue)}
              </pre>
            </div>
          </div>

          {/* Strictly Read-Only Footer */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '0.5rem', borderTop: '1px solid var(--color-slate-200)' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <Shield size={14} color="var(--color-success-600)" />
              <span>Tamper-Evident Immutable Audit Log Record</span>
            </span>
            <Button variant="outline" size="sm" onClick={onClose}>
              Close Inspector
            </Button>
          </div>
        </div>
      ) : null}
    </Modal>
  );
};

export default AuditDetailsModal;
