import React from 'react';
import { ClipboardList, UserCheck } from 'lucide-react';
import Badge from '../Badge';

export const AuditAnalytics = ({ analytics }) => {
  if (!analytics) return null;

  const {
    eventsByStatus = {},
    eventsByAction = {},
    eventsByModule = {},
    topActiveUsers = [],
  } = analytics;

  const statusEntries = Object.entries(eventsByStatus);
  const actionEntries = Object.entries(eventsByAction);
  const moduleEntries = Object.entries(eventsByModule);

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <ClipboardList size={20} style={{ color: 'var(--color-primary-600)' }} />
        <h3 style={{ margin: 0 }}>System Audit Trail & Officer Activity Analytics</h3>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
        {/* Status breakdown */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Audit Event Outcomes
          </h4>
          {statusEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {statusEntries.map(([status, count]) => (
                <div key={status} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <Badge status={status}>{status}</Badge>
                  <strong style={{ fontSize: '0.875rem' }}>{count} events</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No audit status data available.</p>
          )}
        </div>

        {/* Action breakdown */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Audit Action Distribution
          </h4>
          {actionEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {actionEntries.map(([action, count]) => (
                <div key={action} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-navy-800)' }}>{action}</span>
                  <strong style={{ fontSize: '0.875rem' }}>{count}</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No action distribution data available.</p>
          )}
        </div>

        {/* Top Active Users */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Top Active System Officers
          </h4>
          {topActiveUsers.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {topActiveUsers.map((user, idx) => (
                <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <UserCheck size={16} style={{ color: 'var(--color-primary-600)' }} />
                    <span style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{user.username}</span>
                  </div>
                  <strong style={{ fontSize: '0.8125rem', color: 'var(--color-slate-600)' }}>{user.activityCount} actions</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No active user stats available.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default AuditAnalytics;
