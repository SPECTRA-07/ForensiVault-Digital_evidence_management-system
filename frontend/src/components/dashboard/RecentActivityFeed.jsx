import React from 'react';
import { Activity, Clock, User, Hash } from 'lucide-react';
import Badge from '../Badge';

export const RecentActivityFeed = ({ activities = [] }) => {
  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <Activity size={20} style={{ color: 'var(--color-primary-600)' }} />
        <h3 style={{ margin: 0 }}>Consolidated Activity Stream</h3>
      </div>

      {activities && activities.length > 0 ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {activities.map((item, idx) => (
            <div
              key={idx}
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                justifyContent: 'space-between',
                padding: '0.875rem 1rem',
                backgroundColor: 'var(--color-slate-50)',
                borderRadius: 'var(--border-radius)',
                border: '1px solid var(--color-slate-200)',
                gap: '1rem',
                flexWrap: 'wrap',
              }}
            >
              <div style={{ flex: 1, minWidth: '240px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <Badge status={item.activityType || 'SUBMITTED'}>{item.activityType}</Badge>
                  {item.reference && (
                    <span className="font-mono" style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-600)' }}>
                      #{item.reference}
                    </span>
                  )}
                </div>
                <div style={{ fontSize: '0.875rem', color: 'var(--color-navy-900)', fontWeight: 500 }}>
                  {item.description}
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.2rem', fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <User size={12} />
                  <span>{item.performedBy || 'System'}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <Clock size={12} />
                  <span>{item.timestamp ? new Date(item.timestamp).toLocaleString() : '--'}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--color-slate-500)', fontSize: '0.875rem' }}>
          No recent activity stream entries available.
        </div>
      )}
    </div>
  );
};

export default RecentActivityFeed;
