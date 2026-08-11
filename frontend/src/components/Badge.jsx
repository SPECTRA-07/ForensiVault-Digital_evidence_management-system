import React from 'react';

export const Badge = ({ children, status = 'default', variant, className = '' }) => {
  let badgeType = 'badge-secondary';

  const normalized = (status || variant || '').toString().toUpperCase();

  if (['SECURE', 'VERIFIED', 'ACTIVE', 'SUCCESS', 'ACCEPTED', 'CLOSED', 'COMPLETED'].includes(normalized)) {
    badgeType = 'badge-success';
  } else if (['PENDING', 'IN_PROGRESS', 'UNDER_INVESTIGATION', 'TRANSFERRING', 'WARNING'].includes(normalized)) {
    badgeType = 'badge-warning';
  } else if (['TAMPERED', 'TAMPERED_WARNING', 'FAILED', 'REJECTED', 'CRITICAL', 'HIGH', 'INACTIVE'].includes(normalized)) {
    badgeType = 'badge-danger';
  } else if (['SUBMITTED', 'ARCHIVED', 'INFO', 'MEDIUM', 'LOW'].includes(normalized)) {
    badgeType = 'badge-info';
  }

  return (
    <span className={`badge ${badgeType} ${className}`}>
      {children || status}
    </span>
  );
};

export default Badge;
