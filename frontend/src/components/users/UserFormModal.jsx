import React, { useState, useEffect } from 'react';
import { UserPlus, Edit3, AlertCircle, Save } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Select from '../Select';
import userService from '../../services/userService';

export const UserFormModal = ({ isOpen, onClose, editUser = null, onSuccess }) => {
  const [employeeId, setEmployeeId] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('POLICE_OFFICER');
  const [department, setDepartment] = useState('');
  const [designation, setDesignation] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const roleOptions = [
    { value: 'POLICE_OFFICER', label: 'Police Officer / Investigator' },
    { value: 'FORENSIC_EXPERT', label: 'Forensic Lab Expert' },
    { value: 'COURT_OFFICIAL', label: 'Court Official / Prosecutor' },
    { value: 'ADMIN', label: 'System Administrator' },
  ];

  useEffect(() => {
    if (editUser) {
      setEmployeeId(editUser.employeeId || '');
      setFullName(editUser.fullName || '');
      setEmail(editUser.email || '');
      setPassword('');
      setRole(editUser.role || 'POLICE_OFFICER');
      setDepartment(editUser.department || '');
      setDesignation(editUser.designation || '');
      setPhoneNumber(editUser.phoneNumber || '');
    } else {
      setEmployeeId('');
      setFullName('');
      setEmail('');
      setPassword('');
      setRole('POLICE_OFFICER');
      setDepartment('');
      setDesignation('');
      setPhoneNumber('');
    }
    setError(null);
  }, [editUser, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!fullName.trim()) {
      setError('Full name is required.');
      return;
    }

    if (!editUser) {
      if (!employeeId.trim()) {
        setError('Employee ID is required.');
        return;
      }
      if (!email.trim()) {
        setError('Email address is required.');
        return;
      }
      if (!password || password.length < 8) {
        setError('Password must be at least 8 characters long.');
        return;
      }
    }

    setIsSubmitting(true);
    setError(null);

    try {
      let response;
      if (editUser && editUser.id) {
        const updatePayload = {
          fullName: fullName.trim(),
          role,
          department: department.trim() || undefined,
          designation: designation.trim() || undefined,
          phoneNumber: phoneNumber.trim() || undefined,
        };
        response = await userService.updateUser(editUser.id, updatePayload);
      } else {
        const createPayload = {
          employeeId: employeeId.trim(),
          fullName: fullName.trim(),
          email: email.trim(),
          password,
          role,
          department: department.trim() || undefined,
          designation: designation.trim() || undefined,
          phoneNumber: phoneNumber.trim() || undefined,
        };
        response = await userService.createUser(createPayload);
      }

      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to save user account.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={editUser ? `Edit User Profile — ${editUser.fullName}` : 'Create New System User Account'}
      maxWidth="560px"
    >
      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {!editUser && (
          <Input
            label="Employee ID / Badge Number"
            placeholder="e.g. EMP-2049"
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            required
            disabled={isSubmitting}
          />
        )}

        <Input
          label="Full Name"
          placeholder="e.g. Det. Sarah Connor"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
          disabled={isSubmitting}
        />

        {!editUser && (
          <Input
            label="Official Email Address"
            type="email"
            placeholder="e.g. sarah.connor@dems.gov"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={isSubmitting}
          />
        )}

        {!editUser && (
          <Input
            label="Account Password"
            type="password"
            placeholder="Enter initial password (min 8 chars)..."
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            helperText="Password must be at least 8 characters"
            required
            disabled={isSubmitting}
          />
        )}

        <Select
          label="System Authorization Role"
          value={role}
          onChange={(e) => setRole(e.target.value)}
          options={roleOptions}
          required
          disabled={isSubmitting}
        />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Input
            label="Department / Unit"
            placeholder="e.g. Cyber Crime Division"
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            label="Designation / Rank"
            placeholder="e.g. Lead Analyst"
            value={designation}
            onChange={(e) => setDesignation(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <Input
          label="Contact Phone Number"
          placeholder="e.g. +15550192834"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          disabled={isSubmitting}
        />

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" icon={Save} disabled={isSubmitting}>
            {isSubmitting ? 'Saving User...' : editUser ? 'Update Profile' : 'Create User Account'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default UserFormModal;
