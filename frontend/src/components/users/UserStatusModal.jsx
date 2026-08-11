import React, { useState } from 'react';
import { ShieldAlert, CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import userService from '../../services/userService';

export const UserStatusModal = ({ isOpen, onClose, userItem, onSuccess }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !userItem) return null;

  const targetActiveState = !userItem.active;

  const handleConfirmStatusChange = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      await userService.setUserStatus(userItem.id, targetActiveState);
      if (onSuccess) {
        onSuccess();
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to update user account status.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={targetActiveState ? 'Activate User Account' : 'Deactivate User Account'}
      maxWidth="480px"
    >
      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem' }}>
          {error}
        </div>
      )}

      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.875rem 1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)', fontSize: '0.875rem' }}>
        <div style={{ fontWeight: 700, color: 'var(--color-navy-900)' }}>
          {userItem.fullName} ({userItem.email})
        </div>
        <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginTop: '0.2rem' }}>
          Employee ID: {userItem.employeeId || '--'} | Role: {userItem.role}
        </div>
      </div>

      <p style={{ fontSize: '0.875rem', color: 'var(--color-navy-800)', marginBottom: '1.25rem' }}>
        {targetActiveState
          ? `Are you sure you want to activate the account for ${userItem.fullName}? This user will regain access to login and interact with ForensiVault according to their assigned role.`
          : `Are you sure you want to deactivate the account for ${userItem.fullName}? This user will immediately be blocked from logging into ForensiVault.`}
      </p>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
        <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button
          variant={targetActiveState ? 'primary' : 'danger'}
          onClick={handleConfirmStatusChange}
          disabled={isSubmitting}
          style={targetActiveState ? { backgroundColor: 'var(--color-success-600)', borderColor: 'var(--color-success-600)' } : {}}
        >
          {isSubmitting
            ? 'Updating Status...'
            : targetActiveState
            ? 'Confirm Account Activation'
            : 'Confirm Account Deactivation'}
        </Button>
      </div>
    </Modal>
  );
};

export default UserStatusModal;
