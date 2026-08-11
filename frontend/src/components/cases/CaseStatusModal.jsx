import React, { useState, useEffect } from 'react';
import { RefreshCw, AlertCircle, CheckCircle2 } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Select from '../Select';
import Badge from '../Badge';
import caseService from '../../services/caseService';

export const CaseStatusModal = ({ isOpen, onClose, caseItem, onSuccess }) => {
  const [status, setStatus] = useState('UNDER_INVESTIGATION');
  const [statusRemarks, setStatusRemarks] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const statusOptions = [
    { value: 'OPEN', label: 'Open Investigation' },
    { value: 'UNDER_INVESTIGATION', label: 'Under Active Investigation' },
    { value: 'EVIDENCE_COLLECTION', label: 'Evidence Collection Phase' },
    { value: 'PENDING_FORENSIC_ANALYSIS', label: 'Pending Forensic Analysis' },
    { value: 'COURT_PROCEEDINGS', label: 'Courtroom Proceedings' },
    { value: 'CLOSED', label: 'Closed Investigation' },
    { value: 'ARCHIVED', label: 'Archived Record' },
  ];

  useEffect(() => {
    if (caseItem) {
      setStatus(caseItem.status || 'UNDER_INVESTIGATION');
      setStatusRemarks('');
      setError(null);
    }
  }, [caseItem, isOpen]);

  if (!isOpen || !caseItem) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();

    setIsSubmitting(true);
    setError(null);

    try {
      const payload = {
        status,
        statusRemarks: statusRemarks.trim() || undefined,
      };

      const response = await caseService.updateCaseStatus(caseItem.id, payload);
      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to update case status.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Update Case Investigation Status State" maxWidth="500px">
      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <span className="font-mono" style={{ fontWeight: 800, fontSize: '0.95rem' }}>
              {caseItem.caseNumber}
            </span>
            <div style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--color-navy-900)' }}>
              {caseItem.caseName}
            </div>
          </div>
          <Badge status={caseItem.status}>{caseItem.status}</Badge>
        </div>
      </div>

      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <Select
          label="New Investigation State"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          options={statusOptions}
          required
          disabled={isSubmitting}
        />

        <div className="form-group">
          <label className="form-label">Status Transition Remarks</label>
          <textarea
            className="form-textarea"
            rows={3}
            placeholder="State rationale for updating investigation status state..."
            value={statusRemarks}
            onChange={(e) => setStatusRemarks(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" icon={RefreshCw} disabled={isSubmitting}>
            {isSubmitting ? 'Updating Status...' : 'Update Case Status'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CaseStatusModal;
