import React from 'react';

export const StatCard = ({
  title,
  value,
  subtitle,
  icon: Icon,
  trend,
  variant = 'primary',
}) => {
  const getIconColor = () => {
    switch (variant) {
      case 'success': return 'var(--color-success-600)';
      case 'warning': return 'var(--color-warning-600)';
      case 'danger': return 'var(--color-danger-600)';
      case 'info': return 'var(--color-info-600)';
      default: return 'var(--color-primary-600)';
    }
  };

  const getBgColor = () => {
    switch (variant) {
      case 'success': return 'var(--color-success-50)';
      case 'warning': return 'var(--color-warning-50)';
      case 'danger': return 'var(--color-danger-50)';
      case 'info': return 'var(--color-info-50)';
      default: return 'var(--color-primary-50)';
    }
  };

  return (
    <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: '1 1 220px' }}>
      {Icon && (
        <div style={{ padding: '0.875rem', borderRadius: 'var(--border-radius)', backgroundColor: getBgColor(), color: getIconColor(), display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon size={24} />
        </div>
      )}
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          {title}
        </div>
        <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-navy-900)', marginTop: '0.15rem' }}>
          {value !== undefined && value !== null ? value : '--'}
        </div>
        {(subtitle || trend) && (
          <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginTop: '0.25rem' }}>
            {trend && <span style={{ fontWeight: 600, marginRight: '0.25rem' }}>{trend}</span>}
            {subtitle}
          </div>
        )}
      </div>
    </div>
  );
};

export default StatCard;
