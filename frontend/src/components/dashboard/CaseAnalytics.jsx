import React from 'react';
import { Briefcase, BarChart2 } from 'lucide-react';
import Badge from '../Badge';

export const CaseAnalytics = ({ analytics }) => {
  if (!analytics) return null;

  const { casesByStatus = {}, casesByCrimeType = {}, casesBySeverity = {}, casesCreatedPerMonth = [] } = analytics;

  const statusEntries = Object.entries(casesByStatus);
  const crimeTypeEntries = Object.entries(casesByCrimeType);
  const severityEntries = Object.entries(casesBySeverity);

  const totalStatusCount = statusEntries.reduce((acc, [, val]) => acc + val, 0);

  return (
    <div className="card" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <Briefcase size={20} style={{ color: 'var(--color-primary-600)' }} />
        <h3 style={{ margin: 0 }}>Case Analytics & Crime Distribution</h3>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
        {/* Status Distribution */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Cases by Status
          </h4>
          {statusEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
              {statusEntries.map(([status, count]) => {
                const pct = totalStatusCount > 0 ? Math.round((count / totalStatusCount) * 100) : 0;
                return (
                  <div key={status}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8125rem', marginBottom: '0.25rem' }}>
                      <Badge status={status}>{status}</Badge>
                      <span style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>{count} ({pct}%)</span>
                    </div>
                    <div style={{ height: '6px', backgroundColor: 'var(--color-slate-200)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${pct}%`, backgroundColor: 'var(--color-primary-600)', borderRadius: '3px' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No case status data available.</p>
          )}
        </div>

        {/* Crime Severity Breakdown */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Crime Severity Breakdown
          </h4>
          {severityEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {severityEntries.map(([severity, count]) => (
                <div key={severity} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <Badge status={severity}>{severity}</Badge>
                  <strong style={{ fontSize: '0.875rem' }}>{count} cases</strong>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No severity data available.</p>
          )}
        </div>

        {/* Crime Type Breakdown */}
        <div>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Crime Classification Breakdown
          </h4>
          {crimeTypeEntries.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {crimeTypeEntries.map(([crimeType, count]) => (
                <div key={crimeType} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.5rem 0.75rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                  <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-navy-800)' }}>{crimeType}</span>
                  <span style={{ fontSize: '0.875rem', fontWeight: 700 }}>{count}</span>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)' }}>No crime type data available.</p>
          )}
        </div>
      </div>

      {/* Monthly Trend Bars */}
      {casesCreatedPerMonth.length > 0 && (
        <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--color-slate-200)' }}>
          <h4 style={{ fontSize: '0.875rem', color: 'var(--color-slate-600)', marginBottom: '0.75rem' }}>
            Monthly Case Creation Trend
          </h4>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: '0.75rem', height: '100px', padding: '0.5rem 0' }}>
            {casesCreatedPerMonth.map((item, idx) => {
              const maxCount = Math.max(...casesCreatedPerMonth.map((m) => m.count), 1);
              const heightPct = Math.max(Math.round((item.count / maxCount) * 100), 10);
              return (
                <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%' }}>
                  <span style={{ fontSize: '0.7rem', fontWeight: 600, marginBottom: '0.2rem' }}>{item.count}</span>
                  <div style={{ width: '100%', height: `${heightPct}%`, backgroundColor: 'var(--color-primary-600)', borderRadius: '3px 3px 0 0' }} />
                  <span style={{ fontSize: '0.65rem', color: 'var(--color-slate-500)', marginTop: '0.25rem', textTransform: 'uppercase' }}>
                    {item.monthName ? item.monthName.substring(0, 3) : `M${item.month}`}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default CaseAnalytics;
