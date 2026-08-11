import React from 'react';
import { User, Mail, Shield, Building, Phone, Calendar, CheckCircle2, XCircle } from 'lucide-react';
import Modal from '../Modal';
import Badge from '../Badge';

export const UserDetailsModal = ({ isOpen, onClose, userItem }) => {
  if (!isOpen || !userItem) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="User Account Profile Details" maxWidth="560px">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', fontSize: '0.875rem' }}>
        {/* Profile Header */}
        <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div style={{ width: '42px', height: '42px', borderRadius: '50%', backgroundColor: 'var(--color-primary-600)', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '1.1rem' }}>
                {userItem.fullName ? userItem.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.05rem', color: 'var(--color-navy-900)' }}>{userItem.fullName}</h3>
                <span className="font-mono" style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
                  Employee ID: {userItem.employeeId || '--'}
                </span>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '0.35rem' }}>
              <Badge status={userItem.role}>{userItem.role}</Badge>
              <Badge status={userItem.active ? 'ACTIVE' : 'DISABLED'}>
                {userItem.active ? 'ACTIVE' : 'DISABLED'}
              </Badge>
            </div>
          </div>
        </div>

        {/* User Details Grid */}
        <div className="card" style={{ padding: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.875rem', fontSize: '0.8125rem' }}>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Email Address:</span>
              <div style={{ fontWeight: 600 }}>{userItem.email || '--'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>System Role:</span>
              <div><Badge status={userItem.role}>{userItem.role || '--'}</Badge></div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Department / Unit:</span>
              <div style={{ fontWeight: 500 }}>{userItem.department || '--'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Designation / Rank:</span>
              <div style={{ fontWeight: 500 }}>{userItem.designation || '--'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Phone Number:</span>
              <div className="font-mono">{userItem.phoneNumber || '--'}</div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Account Status:</span>
              <div style={{ fontWeight: 600, color: userItem.active ? 'var(--color-success-700)' : 'var(--color-danger-700)' }}>
                {userItem.active ? 'Active Account' : 'Deactivated / Disabled'}
              </div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Created Timestamp:</span>
              <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                {userItem.createdAt ? new Date(userItem.createdAt).toLocaleString() : '--'}
              </div>
            </div>
            <div>
              <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Last Updated:</span>
              <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                {userItem.updatedAt ? new Date(userItem.updatedAt).toLocaleString() : '--'}
              </div>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default UserDetailsModal;
