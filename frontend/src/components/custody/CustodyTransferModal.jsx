import React, { useState, useEffect } from 'react';
import { ArrowRightLeft, User, MapPin, FileText, Send, AlertCircle } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Select from '../Select';
import custodyService from '../../services/custodyService';
import userService from '../../services/userService';
import { useAuth } from '../../hooks/useAuth';

export const CustodyTransferModal = ({
  isOpen,
  onClose,
  evidenceItem = null,
  onTransferSuccess,
}) => {
  const { user: currentUser } = useAuth();
  const [evidenceId, setEvidenceId] = useState('');
  const [transferredToId, setTransferredToId] = useState('');
  const [transferPurpose, setTransferPurpose] = useState('FORENSIC_ANALYSIS');
  const [transferLocation, setTransferLocation] = useState('');
  const [transferRemarks, setTransferRemarks] = useState('');
  const [usersList, setUsersList] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const purposeOptions = [
    { value: 'FORENSIC_ANALYSIS', label: 'Forensic Analysis' },
    { value: 'COURT_SUBMISSION', label: 'Courtroom Submission' },
    { value: 'SAFE_STORAGE', label: 'Vault / Safe Storage' },
    { value: 'EVIDENCE_COLLECTION', label: 'Evidence Collection & Intake' },
    { value: 'RETURN_TO_STORAGE', label: 'Return to Evidence Vault' },
    { value: 'OTHER', label: 'Other Operational Purpose' },
  ];

  useEffect(() => {
    if (evidenceItem) {
      setEvidenceId(evidenceItem.id || '');
    }
  }, [evidenceItem]);

  // Attempt to load registered officers for selection if user has permissions
  useEffect(() => {
    if (isOpen) {
      const fetchUsers = async () => {
        setLoadingUsers(true);
        try {
          const res = await userService.getAllUsers();
          if (res && res.data) {
            // Filter out self
            const available = res.data.filter((u) => u.email !== currentUser?.email);
            setUsersList(available);
          }
        } catch (e) {
          // If 403 or unavailable, fallback to manual recipient ID input
          setUsersList([]);
        } finally {
          setLoadingUsers(false);
        }
      };
      fetchUsers();
    }
  }, [isOpen, currentUser]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!evidenceId) {
      setError('Target Evidence ID is required.');
      return;
    }
    if (!transferredToId) {
      setError('Recipient Officer must be selected.');
      return;
    }
    if (!transferLocation.trim()) {
      setError('Transfer Location is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const payload = {
        evidenceId: Number(evidenceId),
        transferredToId: Number(transferredToId),
        transferPurpose,
        transferLocation: transferLocation.trim(),
        transferRemarks: transferRemarks.trim() || undefined,
      };

      const response = await custodyService.initiateTransfer(payload);
      if (onTransferSuccess) {
        onTransferSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to initiate custody transfer handshake.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Initiate Evidence Custody Transfer" maxWidth="560px">
      {evidenceItem && (
        <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Target Digital Evidence
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
            <span className="font-mono" style={{ fontWeight: 700, fontSize: '0.95rem' }}>
              {evidenceItem.evidenceNumber || `#EV-${evidenceItem.id}`}
            </span>
            <span style={{ fontWeight: 500, fontSize: '0.875rem', color: 'var(--color-navy-900)' }}>
              {evidenceItem.evidenceName}
            </span>
          </div>
        </div>
      )}

      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {!evidenceItem && (
          <Input
            label="Evidence ID"
            type="number"
            placeholder="Enter Numeric Evidence ID (e.g. 10)..."
            value={evidenceId}
            onChange={(e) => setEvidenceId(e.target.value)}
            required
            disabled={isSubmitting}
          />
        )}

        {usersList.length > 0 ? (
          <Select
            label="Recipient Officer / Official"
            value={transferredToId}
            onChange={(e) => setTransferredToId(e.target.value)}
            options={usersList.map((u) => ({
              value: u.id,
              label: `${u.fullName || u.email} (${u.role} - ID: ${u.employeeId || u.id})`,
            }))}
            placeholder="Select authorized recipient officer..."
            required
            disabled={isSubmitting}
          />
        ) : (
          <Input
            label="Recipient Officer ID"
            type="number"
            placeholder="Enter Recipient User ID (e.g. 2)..."
            value={transferredToId}
            onChange={(e) => setTransferredToId(e.target.value)}
            icon={User}
            helperText="Enter the system User ID of the receiving custodian"
            required
            disabled={isSubmitting}
          />
        )}

        <Select
          label="Transfer Purpose"
          value={transferPurpose}
          onChange={(e) => setTransferPurpose(e.target.value)}
          options={purposeOptions}
          required
          disabled={isSubmitting}
        />

        <Input
          label="Transfer Physical / Virtual Location"
          placeholder="e.g. Forensics Vault Locker B3, Courtroom 4"
          value={transferLocation}
          onChange={(e) => setTransferLocation(e.target.value)}
          icon={MapPin}
          required
          disabled={isSubmitting}
        />

        <div className="form-group">
          <label className="form-label">Chain of Custody Remarks</label>
          <textarea
            className="form-textarea"
            rows={3}
            placeholder="Enter mandatory transfer notes, seal condition, or handover instructions..."
            value={transferRemarks}
            onChange={(e) => setTransferRemarks(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" icon={Send} disabled={isSubmitting}>
            {isSubmitting ? 'Initiating Handshake...' : 'Initiate Custody Transfer'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CustodyTransferModal;
