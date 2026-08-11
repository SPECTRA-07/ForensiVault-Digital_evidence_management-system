import React from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import Button from '../components/Button';
import LoadingSpinner from '../components/LoadingSpinner';

export const ProtectedRoute = ({ children, requiredRoles = [] }) => {
  const { isAuthenticated, isLoading, hasAnyRole, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: 'var(--color-slate-100)' }}>
        <LoadingSpinner message="Verifying ForensiVault Security Credentials..." />
      </div>
    );
  }

  // Redirect unauthenticated user to /login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Role checking if required roles are specified
  if (requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    const isDashboardRoute = location.pathname.includes('/dashboard');

    return (
      <div style={{ padding: '3rem 1.5rem', maxWidth: '640px', margin: '2rem auto' }}>
        <div className="card" style={{ borderLeft: '4px solid var(--color-danger-600)', padding: '2rem', textAlign: 'center' }}>
          <div style={{ display: 'inline-flex', padding: '0.875rem', borderRadius: '50%', backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-600)', marginBottom: '1rem' }}>
            <ShieldAlert size={40} />
          </div>
          <h2 style={{ color: 'var(--color-navy-900)', margin: '0 0 0.5rem 0' }}>403 - Access Restricted</h2>
          <p style={{ color: 'var(--color-slate-600)', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
            {isDashboardRoute
              ? 'Dashboard access is restricted to Administrators and Forensic Experts.'
              : `Your account role (${user?.role || 'UNAUTHORIZED'}) is not permitted to access this module.`}
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem' }}>
            <Button variant="primary" icon={ArrowLeft} onClick={() => navigate('/cases', { replace: true })}>
              Return to My Workspace
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;
