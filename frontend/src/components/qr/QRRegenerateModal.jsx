import React, { useState } from 'react';
import { RefreshCw, AlertTriangle, CheckCircle2 } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import qrService from '../../services/qrService';

export const QRRegenerateModal = ({ isOpen, onClose, evidenceId, evidenceNumber, onSuccess }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !evidenceId) return null;

  const handleConfirmRegenerate = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      const response = await qrService.regenerateQRCode(evidenceId);
      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to regenerate QR code barcode tag.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Regenerate Physical QR Barcode Tag" maxWidth="480px">
      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem' }}>
          {error}
        </div>
      )}

      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)', fontSize: '0.875rem' }}>
        <div className="font-mono" style={{ fontWeight: 800, color: 'var(--color-navy-900)' }}>
          {evidenceNumber || `#EV-${evidenceId}`}
        </div>
        <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginTop: '0.2rem' }}>
          Evidence ID: {evidenceId} | Action: ADMIN QR Regeneration
        </div>
      </div>

      <div style={{ backgroundColor: 'var(--color-warning-50)', borderLeft: '4px solid var(--color-warning-600)', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1.25rem', fontSize: '0.8125rem', color: 'var(--color-warning-700)' }}>
        <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
          <AlertTriangle size={16} />
          <span>Regeneration Rationale</span>
        </div>
        <div style={{ marginTop: '0.25rem' }}>
          Regenerating the QR code updates the physical barcode tracking tag. This action does not create a new evidence record.
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
        <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button variant="primary" icon={RefreshCw} onClick={handleConfirmRegenerate} disabled={isSubmitting}>
          {isSubmitting ? 'Regenerating QR Tag...' : 'Confirm QR Regeneration'}
        </Button>
      </div>
    </Modal>
  );
};

export default QRRegenerateModal;
