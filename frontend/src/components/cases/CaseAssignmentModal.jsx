import React, { useState, useEffect } from 'react';
import { UserPlus, UserCheck, AlertCircle } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Select from '../Select';
import caseService from '../../services/caseService';
import userService from '../../services/userService';

export const CaseAssignmentModal = ({ isOpen, onClose, caseItem, onSuccess }) => {
  const [assignedOfficerId, setAssignedOfficerId] = useState('');
  const [assignmentRemarks, setAssignmentRemarks] = useState('');
  const [officersList, setOfficersList] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (caseItem && isOpen) {
      setAssignedOfficerId(caseItem.assignedOfficerId || caseItem.assignedOfficer?.id || '');
      setAssignmentRemarks('');
      setError(null);

      const fetchOfficers = async () => {
        try {
          const res = await userService.getAllUsers();
          if (res && res.data) {
            // Include police officers and active user accounts
            const allUsers = res.data.content || res.data || [];
            const policeOfficers = allUsers.filter((u) => u.role === 'POLICE_OFFICER' || u.role === 'ADMIN');
            setOfficersList(policeOfficers.length > 0 ? policeOfficers : allUsers);
          }
        } catch (e) {
          setOfficersList([]);
        }
      };
      fetchOfficers();
    }
  }, [caseItem, isOpen]);

  if (!isOpen || !caseItem) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!assignedOfficerId) {
      setError('Please select a Police Officer to assign.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      // Contract: CaseAssignOfficerRequest has field @NotNull private Long officerId;
      const payload = {
        officerId: Number(assignedOfficerId),
      };

      const response = await caseService.assignOfficer(caseItem.id, payload);
      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || err.error || 'Failed to assign officer to case.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Assign / Reassign Police Officer to Case" maxWidth="500px">
      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)' }}>
        <div className="font-mono" style={{ fontWeight: 800, fontSize: '0.95rem' }}>
          {caseItem.caseNumber}
        </div>
        <div style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--color-navy-900)' }}>
          {caseItem.caseName}
        </div>
        <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginTop: '0.2rem' }}>
          Current Assigned Lead: <strong>{caseItem.assignedOfficerName || caseItem.assignedOfficer?.fullName || 'Unassigned'}</strong>
        </div>
      </div>

      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {officersList.length > 0 ? (
          <Select
            label="Designated Police Officer / Lead Investigator"
            value={assignedOfficerId}
            onChange={(e) => setAssignedOfficerId(e.target.value)}
            options={officersList.map((u) => ({
              value: u.id,
              label: `${u.fullName || u.email} (${u.role}${u.employeeId ? ` - ${u.employeeId}` : ''})`,
            }))}
            placeholder="Select lead officer..."
            required
            disabled={isSubmitting}
          />
        ) : (
          <Input
            label="Officer Database User ID"
            type="number"
            placeholder="Enter numeric User ID of Police Officer..."
            value={assignedOfficerId}
            onChange={(e) => setAssignedOfficerId(e.target.value)}
            required
            disabled={isSubmitting}
          />
        )}

        <div className="form-group">
          <label className="form-label">Assignment Directives / Remarks</label>
          <textarea
            className="form-textarea"
            rows={3}
            placeholder="Enter assignment notes, handover instructions, or directive..."
            value={assignmentRemarks}
            onChange={(e) => setAssignmentRemarks(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem' }}>
          <Button variant="outline" size="sm" type="button" onClick={onClose} disabled={isSubmitting}>
            Skip Assignment
          </Button>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <Button variant="outline" size="sm" type="button" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" icon={UserCheck} disabled={isSubmitting}>
              {isSubmitting ? 'Assigning Officer...' : 'Assign Officer'}
            </Button>
          </div>
        </div>
      </form>
    </Modal>
  );
};

export default CaseAssignmentModal;
