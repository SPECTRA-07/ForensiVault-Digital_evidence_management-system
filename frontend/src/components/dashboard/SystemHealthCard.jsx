import React from 'react';
import { Server, Database, HardDrive, Users, ShieldCheck, CheckCircle2 } from 'lucide-react';
import Badge from '../Badge';

export const SystemHealthCard = ({ health }) => {
  if (!health) return null;

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Server size={20} style={{ color: 'var(--color-primary-600)' }} />
          <h3 style={{ margin: 0 }}>Operational System Health</h3>
        </div>
        <Badge status={health.status === 'UP' ? 'SECURE' : health.status}>{health.status || 'OPERATIONAL'}</Badge>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <Database size={14} style={{ color: 'var(--color-success-600)' }} />
            <span>Database Connection</span>
          </div>
          <div style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>
            {health.database || 'PostgreSQL Active'}
          </div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <HardDrive size={14} style={{ color: 'var(--color-info-600)' }} />
            <span>Vault Storage Footprint</span>
          </div>
          <div style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>
            {health.formattedStorageSize || '0 B'} ({health.totalEvidenceFiles ?? 0} files)
          </div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <Users size={14} style={{ color: 'var(--color-primary-600)' }} />
            <span>Active Officers / Users</span>
          </div>
          <div style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>
            {health.activeUsersCount ?? 0} Registered Personnel
          </div>
        </div>

        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: 'var(--color-slate-50)', border: '1px solid var(--color-slate-200)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-500)', marginBottom: '0.25rem' }}>
            <ShieldCheck size={14} style={{ color: 'var(--color-success-600)' }} />
            <span>Integrity Verification Rate</span>
          </div>
          <div style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>
            {health.integritySuccessRatePercentage ? `${health.integritySuccessRatePercentage.toFixed(1)}%` : '100.0%'} Intact
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemHealthCard;
