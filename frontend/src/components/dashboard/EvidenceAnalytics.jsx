import React from 'react';
import { FileCheck2, HardDrive, FileText } from 'lucide-react';
import Badge from '../Badge';
import Table from '../Table';

export const EvidenceAnalytics = ({ analytics }) => {
  if (!analytics) return null;

  const {
    evidenceByType = {},
    evidenceByStatus = {},
    largestFiles = [],
    latestUploads = [],
    evidenceUploadedPerMonth = [],
  } = analytics;

  const typeEntries = Object.entries(evidenceByType);
  const statusEntries = Object.entries(evidenceByStatus);

  const formatBytes = (bytes) => {
    if (bytes === null || bytes === undefined || isNaN(bytes)) return '--';
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const largestColumns = [
    {
      header: 'Evidence #',
      accessor: 'evidenceNumber',
      render: (row) => <strong className="font-mono">{row.evidenceNumber}</strong>,
    },
    {
      header: 'File Name / Title',
      accessor: 'evidenceName',
      render: (row) => <span>{row.evidenceName || row.originalFileName || '--'}</span>,
    },
    {
      header: 'Type',
      accessor: 'evidenceType',
      render: (row) => <Badge status="INFO">{row.evidenceType}</Badge>,
    },
    {
      header: 'File Size',
      accessor: 'fileSize',
      render: (row) => <span className="font-mono">{formatBytes(row.fileSize)}</span>,
    },
    {
      header: 'Case #',
      accessor: 'caseNumber',
      render: (row) => <span className="font-mono">{row.caseNumber || '--'}</span>,
    },
  ];

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <FileCheck2 size={20} style={{ color: 'var(--color-info-600)' }} />
        <h3 style={{ margin: 0 }}>Digital Evidence Payload & File Vault Analytics</h3>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Evidence by Type */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Evidence Payload Classification
          </h4>
          {typeEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {typeEntries.map(([type, count]) => (
                <div key={type} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-navy-800)' }}>{type}</span>
                  <strong style={{ fontSize: '0.875rem' }}>{count} items</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No evidence type data available.</p>
          )}
        </div>

        {/* Evidence Status Breakdown */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Evidence Vault Status
          </h4>
          {statusEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {statusEntries.map(([status, count]) => (
                <div key={status} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <Badge status={status}>{status}</Badge>
                  <strong style={{ fontSize: '0.875rem' }}>{count} items</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No evidence status data available.</p>
          )}
        </div>
      </div>

      {/* Largest Vault Files */}
      {largestFiles.length > 0 && (
        <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--color-slate-200)' }}>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Largest Registered Vault Payloads
          </h4>
          <Table columns={largestColumns} data={largestFiles} keyField="id" emptyMessage="No evidence files registered." />
        </div>
      )}
    </div>
  );
};

export default EvidenceAnalytics;
