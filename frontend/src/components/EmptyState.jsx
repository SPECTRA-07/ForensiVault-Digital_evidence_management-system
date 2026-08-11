import React from 'react';
import { FolderOpen } from 'lucide-react';
import Button from './Button';

export const EmptyState = ({
  icon: Icon = FolderOpen,
  title = 'No records found',
  description = 'There are no digital evidence records available matching your filter criteria.',
  actionLabel,
  onAction,
}) => {
  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '3.5rem 1.5rem', borderStyle: 'dashed' }}>
      <div style={{ padding: '1rem', borderRadius: '50%', backgroundColor: 'var(--color-slate-100)', marginBottom: '1rem', color: 'var(--color-slate-500)' }}>
        <Icon size={40} />
      </div>
      <h3 style={{ marginBottom: '0.5rem', color: 'var(--color-navy-900)' }}>{title}</h3>
      <p style={{ color: 'var(--color-slate-500)', maxWidth: '420px', marginBottom: actionLabel ? '1.5rem' : 0 }}>
        {description}
      </p>
      {actionLabel && onAction && (
        <Button variant="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
};

export default EmptyState;
