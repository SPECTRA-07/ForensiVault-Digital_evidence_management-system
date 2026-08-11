import React from 'react';
import { ShieldCheck, AlertTriangle, CheckCircle2, FileQuestion } from 'lucide-react';
import Badge from '../Badge';
import Table from '../Table';

export const IntegrityAnalytics = ({ analytics }) => {
  if (!analytics) return null;

  const {
    verifiedCount = 0,
    tamperedCount = 0,
    hashMissingCount = 0,
    verificationFailedCount = 0,
    latestVerifications = [],
  } = analytics;

  const totalChecks = verifiedCount + tamperedCount + hashMissingCount + verificationFailedCount;
  const successRatePct = totalChecks > 0 ? ((verifiedCount / totalChecks) * 100).toFixed(1) : '100.0';

  const verificationColumns = [
    {
      header: 'Evidence #',
      accessor: 'evidenceNumber',
      render: (row) => <strong className="font-mono">{row.evidenceNumber || `#EV-${row.evidenceId}`}</strong>,
    },
    {
      header: 'Verification Status',
      accessor: 'integrityStatus',
      render: (row) => (
        <Badge status={row.integrityStatus || row.status}>
          {row.integrityStatus || row.status || 'UNVERIFIED'}
        </Badge>
      ),
    },
    {
      header: 'SHA-256 Checksum',
      accessor: 'currentHash',
      render: (row) => {
        const hash = row.currentHash || row.storedHash || row.calculatedSha256;
        return (
          <span className="font-mono" style={{ fontSize: '0.75rem' }}>
            {hash ? `${hash.substring(0, 16)}...` : '--'}
          </span>
        );
      },
    },
    {
      header: 'Verifier Officer',
      accessor: 'verifiedBy',
      render: (row) => <span>{row.verifiedBy || row.verifierEmail || '--'}</span>,
    },
    {
      header: 'Diagnostic Message',
      accessor: 'message',
      render: (row) => <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-600)' }}>{row.message || '--'}</span>,
    },
    {
      header: 'Verified At',
      accessor: 'verifiedAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.verifiedAt ? new Date(row.verifiedAt).toLocaleString() : '--'}
        </span>
      ),
    },
  ];

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <ShieldCheck size={20} style={{ color: 'var(--color-success-600)' }} />
          <h3 style={{ margin: 0 }}>Cryptographic SHA-256 Integrity Analytics</h3>
        </div>
        <Badge status={tamperedCount > 0 ? 'TAMPERED_WARNING' : 'SECURE'}>
          {successRatePct}% INTACT RATE
        </Badge>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-success-50)', border: '1px solid rgba(5, 150, 105, 0.2)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-success-700)', marginBottom: '0.25rem' }}>
            <CheckCircle2 size={16} />
            <span>Verified Intact</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-success-700)' }}>{verifiedCount}</div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: tamperedCount > 0 ? 'var(--color-danger-50)' : 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: tamperedCount > 0 ? 'var(--color-danger-700)' : 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <AlertTriangle size={16} />
            <span>Tampered Mismatches</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: tamperedCount > 0 ? 'var(--color-danger-700)' : 'var(--color-navy-900)' }}>{tamperedCount}</div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <FileQuestion size={16} />
            <span>Hash Missing</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-navy-900)' }}>{hashMissingCount}</div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <AlertTriangle size={16} />
            <span>Verification Failed</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-navy-900)' }}>{verificationFailedCount}</div>
        </div>
      </div>

      {latestVerifications.length > 0 && (
        <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--color-slate-200)' }}>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Recent Cryptographic Audit Logs
          </h4>
          <Table columns={verificationColumns} data={latestVerifications} keyField="id" emptyMessage="No verification logs recorded." />
        </div>
      )}
    </div>
  );
};

export default IntegrityAnalytics;
