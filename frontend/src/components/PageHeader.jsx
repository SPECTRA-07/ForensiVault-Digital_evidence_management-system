import React from 'react';

export const PageHeader = ({
  title,
  subtitle,
  actions,
  badge,
}) => {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <h1 style={{ margin: 0 }}>{title}</h1>
          {badge}
        </div>
        {subtitle && <p style={{ color: 'var(--color-slate-500)', margin: '0.25rem 0 0 0', fontSize: '0.875rem' }}>{subtitle}</p>}
      </div>
      {actions && <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>{actions}</div>}
    </div>
  );
};

export default PageHeader;
