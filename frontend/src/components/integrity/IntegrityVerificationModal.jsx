import React, { useState, useEffect } from 'react';
import { ShieldCheck, ShieldAlert, CheckCircle2, AlertTriangle, Copy, Check, FileQuestion, RefreshCw } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import ErrorMessage from '../ErrorMessage';
import integrityService from '../../services/integrityService';

export const IntegrityVerificationModal = ({ isOpen, onClose, evidenceId, evidenceNumber, evidenceName }) => {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copiedField, setCopiedField] = useState(null);

  const runVerification = async () => {
    if (!evidenceId) return;
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await integrityService.verifyEvidenceIntegrity(evidenceId);
      if (response && response.data) {
        setResult(response.data);
      } else {
        setError('Received unexpected response format from verification engine.');
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && evidenceId) {
      runVerification();
    }
  }, [isOpen, evidenceId]);

  const copyToClipboard = (text, fieldName) => {
    if (navigator.clipboard && text) {
      navigator.clipboard.writeText(text);
      setCopiedField(fieldName);
      setTimeout(() => setCopiedField(null), 2000);
    }
  };

  if (!isOpen) return null;

  const getStatusAlertStyle = (status) => {
    switch (status) {
      case 'VERIFIED':
        return {
          bg: 'var(--color-success-50)',
          border: 'var(--color-success-600)',
          color: 'var(--color-success-700)',
          icon: CheckCircle2,
          defaultMsg: 'File integrity verified successfully. Cryptographic SHA-256 matches stored baseline.',
        };
      case 'TAMPERED':
        return {
          bg: 'var(--color-danger-50)',
          border: 'var(--color-danger-600)',
          color: 'var(--color-danger-700)',
          icon: AlertTriangle,
          defaultMsg: 'Tampering detected! Stored hash does not match current file payload.',
        };
      case 'FILE_MISSING':
        return {
          bg: 'var(--color-danger-50)',
          border: 'var(--color-danger-600)',
          color: 'var(--color-danger-700)',
          icon: FileQuestion,
          defaultMsg: 'Evidence file payload is missing from local/S3 vault storage.',
        };
      case 'HASH_MISSING':
        return {
          bg: 'var(--color-warning-50)',
          border: 'var(--color-warning-600)',
          color: 'var(--color-warning-700)',
          icon: AlertTriangle,
          defaultMsg: 'No baseline SHA-256 hash is available for this evidence record.',
        };
      default:
        return {
          bg: 'var(--color-slate-50)',
          border: 'var(--color-slate-400)',
          color: 'var(--color-navy-900)',
          icon: ShieldAlert,
          defaultMsg: 'Integrity check finished with status: ' + status,
        };
    }
  };

  const alertConfig = result ? getStatusAlertStyle(result.integrityStatus) : null;
  const AlertIcon = alertConfig?.icon;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Cryptographic SHA-256 Integrity Check" maxWidth="620px">
      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)' }}>
        <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          Target Evidence Record
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
          <span className="font-mono" style={{ fontWeight: 800, fontSize: '0.95rem' }}>
            {evidenceNumber || `#EV-${evidenceId}`}
          </span>
          <span style={{ fontWeight: 500, fontSize: '0.875rem', color: 'var(--color-navy-900)' }}>
            {evidenceName || 'Vault Item'}
          </span>
        </div>
      </div>

      {error && <ErrorMessage error={error} onRetry={runVerification} />}

      {loading ? (
        <LoadingSpinner message="Calculating SHA-256 checksum and performing cryptographic comparison..." />
      ) : result && alertConfig ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Status Alert Banner */}
          <div
            style={{
              backgroundColor: alertConfig.bg,
              borderLeft: `4px solid ${alertConfig.border}`,
              padding: '1rem',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '0.75rem',
            }}
          >
            {AlertIcon && <AlertIcon size={24} style={{ color: alertConfig.border, flexShrink: 0, marginTop: '0.1rem' }} />}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Badge status={result.integrityStatus}>{result.integrityStatus}</Badge>
                <span className="font-mono" style={{ fontSize: '0.75rem', fontWeight: 600, color: alertConfig.color }}>
                  Algorithm: {result.hashAlgorithm || 'SHA-256'}
                </span>
              </div>
              <p style={{ margin: 0, fontSize: '0.875rem', color: alertConfig.color, fontWeight: 500 }}>
                {result.message || alertConfig.defaultMsg}
              </p>
            </div>
          </div>

          {/* Stored Hash Display */}
          {result.storedHash && (
            <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-600)', textTransform: 'uppercase' }}>
                  Stored Baseline SHA-256 Hash
                </span>
                <button
                  type="button"
                  onClick={() => copyToClipboard(result.storedHash, 'stored')}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem', color: 'var(--color-primary-600)', padding: '0.2rem 0.4rem' }}
                >
                  {copiedField === 'stored' ? <Check size={14} color="green" /> : <Copy size={14} />}
                  <span>{copiedField === 'stored' ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
              <div className="font-mono" style={{ fontSize: '0.75rem', wordBreak: 'break-all', backgroundColor: '#ffffff', padding: '0.5rem 0.75rem', borderRadius: '4px', border: '1px solid var(--color-slate-200)', color: 'var(--color-navy-900)' }}>
                {result.storedHash}
              </div>
            </div>
          )}

          {/* Current Live Hash Display (if backend returns currentHash) */}
          {result.currentHash && (
            <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-600)', textTransform: 'uppercase' }}>
                  Calculated Live File SHA-256 Hash
                </span>
                <button
                  type="button"
                  onClick={() => copyToClipboard(result.currentHash, 'current')}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem', color: 'var(--color-primary-600)', padding: '0.2rem 0.4rem' }}
                >
                  {copiedField === 'current' ? <Check size={14} color="green" /> : <Copy size={14} />}
                  <span>{copiedField === 'current' ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
              <div className="font-mono" style={{ fontSize: '0.75rem', wordBreak: 'break-all', backgroundColor: '#ffffff', padding: '0.5rem 0.75rem', borderRadius: '4px', border: '1px solid var(--color-slate-200)', color: 'var(--color-navy-900)' }}>
                {result.currentHash}
              </div>
            </div>
          )}

          {/* Verification Timestamp & Verifier */}
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--color-slate-500)', borderTop: '1px solid var(--color-slate-200)', paddingTop: '0.75rem' }}>
            <span>Verified By: <strong>{result.verifiedBy || 'Authenticated Officer'}</strong></span>
            <span>Timestamp: <strong className="font-mono">{result.verifiedAt ? new Date(result.verifiedAt).toLocaleString() : '--'}</strong></span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
            <Button variant="secondary" size="sm" icon={RefreshCw} onClick={runVerification}>
              Re-Verify
            </Button>
            <Button variant="outline" size="sm" onClick={onClose}>
              Close
            </Button>
          </div>
        </div>
      ) : null}
    </Modal>
  );
};

export default IntegrityVerificationModal;
