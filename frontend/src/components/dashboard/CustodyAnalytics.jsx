import React from 'react';
import { GitCommit, Clock, ArrowRightLeft, MapPin } from 'lucide-react';
import Badge from '../Badge';
import Table from '../Table';

export const CustodyAnalytics = ({ analytics }) => {
  if (!analytics) return null;

  const {
    pendingTransfers = 0,
    acceptedTransfers = 0,
    rejectedTransfers = 0,
    averageTransferTimeMinutes = 0,
    latestCustodyTransfers = [],
  } = analytics;

  const custodyColumns = [
    {
      header: 'Custody / Transfer #',
      accessor: 'custodyNumber',
      render: (row) => <strong className="font-mono">{row.custodyNumber || `#CUST-${row.id}`}</strong>,
    },
    {
      header: 'Evidence #',
      accessor: 'evidenceNumber',
      render: (row) => <span className="font-mono">{row.evidenceNumber || `#EV-${row.evidenceId}`}</span>,
    },
    {
      header: 'From Custodian',
      accessor: 'transferredBy',
      render: (row) => (
        <span>{row.transferredBy?.fullName || row.transferredBy?.email || row.transferredByName || '--'}</span>
      ),
    },
    {
      header: 'To Custodian',
      accessor: 'transferredTo',
      render: (row) => (
        <span>{row.transferredTo?.fullName || row.transferredTo?.email || row.transferredToName || '--'}</span>
      ),
    },
    {
      header: 'Purpose',
      accessor: 'transferPurpose',
      render: (row) => (row.transferPurpose ? <Badge status="SUBMITTED">{row.transferPurpose}</Badge> : '--'),
    },
    {
      header: 'Location',
      accessor: 'transferLocation',
      render: (row) => <span>{row.transferLocation || '--'}</span>,
    },
    {
      header: 'Status',
      accessor: 'transferStatus',
      render: (row) => <Badge status={row.transferStatus}>{row.transferStatus}</Badge>,
    },
    {
      header: 'Transferred At',
      accessor: 'transferredAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.transferredAt || row.createdAt ? new Date(row.transferredAt || row.createdAt).toLocaleString() : '--'}
        </span>
      ),
    },
    {
      header: 'Accepted At',
      accessor: 'acceptedAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.acceptedAt ? new Date(row.acceptedAt).toLocaleString() : '--'}
        </span>
      ),
    },
  ];

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <GitCommit size={20} style={{ color: 'var(--color-warning-600)' }} />
        <h3 style={{ margin: 0 }}>Chain of Custody Handshake Analytics</h3>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        {/* Pending Transfers - Prominently Highlighted */}
        <div
          style={{
            padding: '1rem',
            borderRadius: 'var(--border-radius)',
            backgroundColor: pendingTransfers > 0 ? 'var(--color-warning-50)' : 'var(--color-slate-50)',
            border: pendingTransfers > 0 ? '2px solid var(--color-warning-600)' : '1px solid var(--color-slate-200)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-warning-700)', marginBottom: '0.25rem' }}>
            <ArrowRightLeft size={16} />
            <span>Pending Handshakes</span>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-warning-700)' }}>
            {pendingTransfers}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--color-warning-700)' }}>Awaiting custodian sign-off</span>
        </div>

        <div style={{ padding: '1rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            Accepted Transfers
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-success-700)' }}>
            {acceptedTransfers}
          </div>
        </div>

        <div style={{ padding: '1rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            Rejected Handshakes
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-danger-700)' }}>
            {rejectedTransfers}
          </div>
        </div>

        <div style={{ padding: '1rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <Clock size={14} />
            <span>Avg Resolution Time</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-navy-900)' }}>
            {averageTransferTimeMinutes ? `${averageTransferTimeMinutes.toFixed(1)}m` : 'N/A'}
          </div>
        </div>
      </div>

      {latestCustodyTransfers.length > 0 && (
        <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--color-slate-200)' }}>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Recent Custody Handshakes
          </h4>
          <Table columns={custodyColumns} data={latestCustodyTransfers} keyField="id" emptyMessage="No recent custody transfers." />
        </div>
      )}
    </div>
  );
};

export default CustodyAnalytics;
