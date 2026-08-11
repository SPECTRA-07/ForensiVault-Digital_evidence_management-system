import React, { useState } from 'react';
import { Search, ShieldCheck, CheckCircle2, AlertCircle, FileCheck2 } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import qrService from '../../services/qrService';

export const QRResolveModal = ({ isOpen, onClose, initialEvidenceNumber = '' }) => {
  const [evidenceNumber, setEvidenceNumber] = useState(initialEvidenceNumber);
  const [resolveResult, setResolveResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleResolve = async (e) => {
    e.preventDefault();
    if (!evidenceNumber.trim()) return;

    setLoading(true);
    setError(null);
    setResolveResult(null);

    try {
      const response = await qrService.resolveQRCode(evidenceNumber.trim());
      if (response && response.data) {
        setResolveResult(response.data);
      }
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Evidence QR tag resolution failed. Record not found.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Physical Barcode Tag Scanner & Resolver" maxWidth="600px">
      <form onSubmit={handleResolve} style={{ marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <div style={{ flex: 1 }}>
            <Input
              label="Scanned Barcode / Evidence Number"
              placeholder="e.g. EVD-1786389900473-B5E1"
              value={evidenceNumber}
              onChange={(e) => setEvidenceNumber(e.target.value)}
              icon={Search}
              required
            />
          </div>
          <div style={{ alignSelf: 'flex-end', marginBottom: '1rem' }}>
            <Button type="submit" variant="primary" disabled={loading}>
              Resolve Tag
            </Button>
          </div>
        </div>
      </form>

      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.875rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem', borderLeft: '4px solid var(--color-danger-600)' }}>
          <div style={{ fontWeight: 600 }}>Tag Resolution Error</div>
          <div>{error}</div>
        </div>
      )}

      {loading && <LoadingSpinner message="Resolving physical barcode payload against central ForensiVault vault..." />}

      {resolveResult && (
        <div className="card" style={{ borderLeft: '4px solid var(--color-success-600)', padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <div>
              <span className="font-mono" style={{ fontWeight: 800, fontSize: '1.1rem', color: 'var(--color-navy-900)' }}>
                {resolveResult.evidenceNumber}
              </span>
              <h3 style={{ margin: '0.2rem 0 0 0', fontSize: '1rem' }}>{resolveResult.evidenceName}</h3>
            </div>
            <Badge status="SECURE">AUTHENTICATED TAG</Badge>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Associated Case #:</span>
              <div className="font-mono" style={{ fontWeight: 600 }}>{resolveResult.caseNumber || '--'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Status:</span>
              <div><Badge status={resolveResult.evidenceStatus}>{resolveResult.evidenceStatus}</Badge></div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Current Custodian:</span>
              <div style={{ fontWeight: 600, color: 'var(--color-primary-700)' }}>{resolveResult.currentCustodian || 'Vault Storage'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Integrity Status:</span>
              <div><Badge status={resolveResult.integrityStatus}>{resolveResult.integrityStatus}</Badge></div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>QR Tag Generated At:</span>
              <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                {resolveResult.qrGeneratedAt ? new Date(resolveResult.qrGeneratedAt).toLocaleString() : '--'}
              </div>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
};

export default QRResolveModal;
