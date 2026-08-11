import React, { useState } from 'react';
import { ArrowRightLeft, CheckCircle2, XCircle, User, MapPin, Calendar, Clock, AlertTriangle } from 'lucide-react';
import Button from '../Button';
import Badge from '../Badge';
import Modal from '../Modal';
import custodyService from '../../services/custodyService';

export const PendingTransferCard = ({ record, onActionComplete }) => {
  const [showResponseModal, setShowResponseModal] = useState(false);
  const [isAccepting, setIsAccepting] = useState(true);
  const [remarks, setRemarks] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!record) return null;

  const handleOpenModal = (accepting) => {
    setIsAccepting(accepting);
    setRemarks('');
    setError(null);
    setShowResponseModal(true);
  };

  const handleConfirmResponse = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      const payload = {
        accepted: isAccepting,
        acceptanceRemarks: remarks.trim() || (isAccepting ? 'Custody handshake accepted.' : 'Custody handshake rejected.'),
      };

      await custodyService.acceptOrRejectTransfer(record.id, payload);
      setShowResponseModal(false);
      if (onActionComplete) {
        onActionComplete();
      }
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to record custody response.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <div
        className="card"
        style={{
          borderLeft: '4px solid var(--color-warning-600)',
          backgroundColor: 'var(--color-warning-50)',
          marginBottom: '1rem',
          padding: '1.25rem',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <span className="font-mono" style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--color-navy-900)' }}>
                #CUST-{record.id}
              </span>
              <Badge status={record.transferStatus}>{record.transferStatus}</Badge>
              <Badge status="SUBMITTED">{record.transferPurpose}</Badge>
            </div>
            <h4 style={{ margin: '0.25rem 0', fontSize: '1rem', color: 'var(--color-navy-900)' }}>
              Evidence #{record.evidenceNumber || record.evidenceId}: {record.evidenceName || 'Digital Payload'}
            </h4>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <Button
              variant="primary"
              size="sm"
              icon={CheckCircle2}
              onClick={() => handleOpenModal(true)}
              style={{ backgroundColor: 'var(--color-success-600)', borderColor: 'var(--color-success-600)' }}
            >
              Accept Transfer
            </Button>
            <Button
              variant="danger"
              size="sm"
              icon={XCircle}
              onClick={() => handleOpenModal(false)}
            >
              Reject Transfer
            </Button>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid rgba(217, 119, 6, 0.2)', fontSize: '0.8125rem' }}>
          <div>
            <span style={{ color: 'var(--color-slate-600)', display: 'block', fontSize: '0.75rem' }}>Transferring Custodian:</span>
            <strong>{record.transferredBy?.fullName || record.transferredBy?.email || 'Officer'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-600)', display: 'block', fontSize: '0.75rem' }}>Designated Recipient:</span>
            <strong>{record.transferredTo?.fullName || record.transferredTo?.email || 'Me'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-600)', display: 'block', fontSize: '0.75rem' }}>Handover Location:</span>
            <strong>{record.transferLocation || 'Vault'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-600)', display: 'block', fontSize: '0.75rem' }}>Initiated At:</span>
            <span className="font-mono">{record.createdAt ? new Date(record.createdAt).toLocaleString() : '--'}</span>
          </div>
        </div>

        {record.transferRemarks && (
          <div style={{ marginTop: '0.75rem', fontSize: '0.8125rem', color: 'var(--color-navy-900)', backgroundColor: 'rgba(255, 255, 255, 0.6)', padding: '0.5rem 0.75rem', borderRadius: '4px' }}>
            <strong>Transfer Notes:</strong> {record.transferRemarks}
          </div>
        )}
      </div>

      {/* Confirmation Modal */}
      <Modal
        isOpen={showResponseModal}
        onClose={() => setShowResponseModal(false)}
        title={isAccepting ? 'Confirm Custody Acceptance' : 'Confirm Custody Rejection'}
        maxWidth="480px"
      >
        {error && (
          <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem' }}>
            {error}
          </div>
        )}

        <p style={{ fontSize: '0.875rem', marginBottom: '1rem', color: 'var(--color-navy-800)' }}>
          {isAccepting
            ? `You are about to accept formal custody of Evidence #${record.evidenceNumber || record.evidenceId}. You will become the legal custodian of this digital asset.`
            : `You are about to reject the custody transfer for Evidence #${record.evidenceNumber || record.evidenceId}. Current custodian will remain unchanged.`}
        </p>

        <div className="form-group">
          <label className="form-label">{isAccepting ? 'Acceptance Remarks' : 'Rejection Reason'}</label>
          <textarea
            className="form-textarea"
            rows={3}
            placeholder={isAccepting ? 'Enter receipt notes, vault placement, or verification notes...' : 'State reason for rejecting custody handover...'}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.25rem' }}>
          <Button variant="outline" onClick={() => setShowResponseModal(false)} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button
            variant={isAccepting ? 'primary' : 'danger'}
            onClick={handleConfirmResponse}
            disabled={isSubmitting}
            style={isAccepting ? { backgroundColor: 'var(--color-success-600)' } : {}}
          >
            {isSubmitting ? 'Recording Handshake...' : isAccepting ? 'Accept Custody' : 'Reject Transfer'}
          </Button>
        </div>
      </Modal>
    </>
  );
};

export default PendingTransferCard;
