import React from 'react';
import { RefreshCw, Clock } from 'lucide-react';
import Button from '../Button';
import Badge from '../Badge';

export const DashboardHeader = ({ lastRefreshed, onRefresh, isRefreshing }) => {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-navy-900)' }}>
            Executive Dashboard
          </h1>
          <Badge status="SECURE">LIVE ANALYTICS</Badge>
        </div>
        <p style={{ color: 'var(--color-slate-500)', margin: '0.25rem 0 0 0', fontSize: '0.875rem' }}>
          Real-time operational summary, forensic evidence metrics, and system audit trail.
        </p>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {lastRefreshed && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
            <Clock size={14} />
            <span>Updated: {lastRefreshed.toLocaleTimeString()}</span>
          </div>
        )}

        <Button
          variant="secondary"
          size="sm"
          onClick={onRefresh}
          disabled={isRefreshing}
          icon={RefreshCw}
          style={isRefreshing ? { animation: 'spin 1s linear infinite' } : {}}
        >
          {isRefreshing ? 'Refreshing...' : 'Refresh Data'}
        </Button>
      </div>
    </div>
  );
};

export default DashboardHeader;
