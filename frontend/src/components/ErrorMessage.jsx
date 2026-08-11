import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import Button from './Button';

export const ErrorMessage = ({
  title = 'API Communication Error',
  error,
  onRetry,
}) => {
  const message = typeof error === 'string' ? error : error?.message || 'An error occurred while fetching forensic data.';

  return (
    <div className="card" style={{ borderLeft: '4px solid var(--color-danger-600)', backgroundColor: 'var(--color-danger-50)', padding: '1.25rem' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
        <AlertTriangle size={20} style={{ color: 'var(--color-danger-600)', marginTop: '0.15rem', flexShrink: 0 }} />
        <div style={{ flex: 1 }}>
          <h4 style={{ color: 'var(--color-danger-700)', margin: '0 0 0.25rem 0' }}>{title}</h4>
          <p style={{ color: 'var(--color-navy-800)', margin: 0, fontSize: '0.875rem' }}>{message}</p>
          {error?.validationErrors && (
            <ul style={{ marginTop: '0.5rem', paddingLeft: '1.25rem', color: 'var(--color-danger-700)', fontSize: '0.8125rem' }}>
              {Object.entries(error.validationErrors).map(([field, msg]) => (
                <li key={field}>
                  <strong>{field}</strong>: {msg}
                </li>
              ))}
            </ul>
          )}
          {onRetry && (
            <div style={{ marginTop: '0.75rem' }}>
              <Button variant="secondary" size="sm" onClick={onRetry} icon={RefreshCw}>
                Retry Operation
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ErrorMessage;
