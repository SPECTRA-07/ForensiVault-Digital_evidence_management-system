import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner = ({ message = 'Loading forensic records...', size = 32 }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '3rem 1rem', width: '100%' }}>
      <Loader2 size={size} className="spin-animation" style={{ color: 'var(--color-primary-600)', marginBottom: '0.75rem', animation: 'spin 1s linear infinite' }} />
      <span style={{ color: 'var(--color-slate-600)', fontSize: '0.875rem', fontWeight: 500 }}>{message}</span>
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default LoadingSpinner;
