import React from 'react';
import Modal from '../Modal';
import Badge from '../Badge';

export const CustodyDetailsModal = ({ isOpen, onClose, record }) => {
  if (!isOpen || !record) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Custody Transfer Record Details" maxWidth="560px">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.875rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-slate-200)' }}>
          <div>
            <span className="font-mono" style={{ fontWeight: 800, fontSize: '1.1rem', color: 'var(--color-navy-900)' }}>
              #CUST-{record.id}
            </span>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
              Sequence #{record.custodySequence || 1}
            </div>
          </div>
          <Badge status={record.transferStatus}>{record.transferStatus}</Badge>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Number:</span>
            <div className="font-mono" style={{ fontWeight: 600 }}>{record.evidenceNumber || `#EV-${record.evidenceId}`}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Title:</span>
            <div style={{ fontWeight: 500 }}>{record.evidenceName || '--'}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Transferred By:</span>
            <div style={{ fontWeight: 600 }}>{record.transferredBy?.fullName || record.transferredBy?.email || '--'}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Transferred To:</span>
            <div style={{ fontWeight: 600 }}>{record.transferredTo?.fullName || record.transferredTo?.email || '--'}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Transfer Purpose:</span>
            <div><Badge status="SUBMITTED">{record.transferPurpose}</Badge></div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Handover Location:</span>
            <div style={{ fontWeight: 500 }}>{record.transferLocation || '--'}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Initiated Timestamp:</span>
            <div className="font-mono" style={{ fontSize: '0.75rem' }}>{record.createdAt ? new Date(record.createdAt).toLocaleString() : '--'}</div>
          </div>
          <div>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Accepted / Handshake Timestamp:</span>
            <div className="font-mono" style={{ fontSize: '0.75rem' }}>{record.acceptedAt ? new Date(record.acceptedAt).toLocaleString() : '--'}</div>
          </div>
        </div>

        {record.transferRemarks && (
          <div style={{ padding: '0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
            <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem', display: 'block', fontWeight: 600 }}>Transferor Remarks:</span>
            <span style={{ fontSize: '0.8125rem' }}>{record.transferRemarks}</span>
          </div>
        )}

        {record.acceptanceRemarks && (
          <div style={{ padding: '0.75rem', backgroundColor: 'var(--color-success-50)', borderRadius: 'var(--border-radius)', border: '1px solid rgba(5, 150, 105, 0.2)' }}>
            <span style={{ color: 'var(--color-success-700)', fontSize: '0.75rem', display: 'block', fontWeight: 600 }}>Recipient Acceptance Remarks:</span>
            <span style={{ fontSize: '0.8125rem', color: 'var(--color-navy-900)' }}>{record.acceptanceRemarks}</span>
          </div>
        )}
      </div>
    </Modal>
  );
};

export default CustodyDetailsModal;
