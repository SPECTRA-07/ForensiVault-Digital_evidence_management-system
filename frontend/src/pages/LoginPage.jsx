import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ShieldCheck, Lock, Mail, AlertCircle, Eye, EyeOff, Loader2 } from 'lucide-react';
import Button from '../components/Button';
import Input from '../components/Input';
import { useAuth } from '../hooks/useAuth';

export const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated, user } = useAuth();

  // Helper to determine role-appropriate landing route
  const getRoleLandingRoute = (userRole, attemptedPath) => {
    const isDashboardRole = userRole === 'ADMIN' || userRole === 'FORENSIC_EXPERT';

    if (attemptedPath && attemptedPath !== '/login' && attemptedPath !== '/') {
      // Prevent POLICE_OFFICER or COURT_OFFICIAL from being sent to restricted /dashboard or /users
      if (attemptedPath === '/dashboard' && !isDashboardRole) {
        return '/cases';
      }
      if (attemptedPath === '/users' && userRole !== 'ADMIN') {
        return '/cases';
      }
      return attemptedPath;
    }

    return isDashboardRole ? '/dashboard' : '/cases';
  };

  // Redirect if already authenticated
  React.useEffect(() => {
    if (isAuthenticated && user) {
      const fromPath = location.state?.from?.pathname;
      const targetRoute = getRoleLandingRoute(user.role, fromPath);
      navigate(targetRoute, { replace: true });
    }
  }, [isAuthenticated, user, navigate, location.state]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setError('Official email address is required.');
      return;
    }
    if (!password) {
      setError('Password is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const loggedUser = await login(trimmedEmail, password);
      const userRole = loggedUser?.role || user?.role;
      const fromPath = location.state?.from?.pathname;
      const targetRoute = getRoleLandingRoute(userRole, fromPath);

      navigate(targetRoute, { replace: true });
    } catch (err) {
      const errorMsg = typeof err === 'string' ? err : err.message || 'Authentication failed. Please verify credentials.';
      setError(errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--color-navy-950)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1.5rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: '440px', padding: '2.5rem', backgroundColor: 'var(--color-white)', boxShadow: 'var(--shadow-xl)', borderRadius: '8px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{ display: 'inline-flex', padding: '0.875rem', borderRadius: '50%', backgroundColor: 'var(--color-primary-50)', color: 'var(--color-primary-600)', marginBottom: '1rem' }}>
            <ShieldCheck size={40} />
          </div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--color-navy-900)', letterSpacing: '-0.02em' }}>
            ForensiVault Security Portal
          </h2>
          <p style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', marginTop: '0.35rem' }}>
            Digital Evidence Management & Chain of Custody System
          </p>
        </div>

        {error && (
          <div
            style={{
              backgroundColor: 'var(--color-danger-50)',
              borderLeft: '4px solid var(--color-danger-600)',
              padding: '0.875rem 1rem',
              marginBottom: '1.5rem',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '0.625rem',
              fontSize: '0.8125rem',
              color: 'var(--color-danger-700)',
            }}
          >
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '0.1rem' }} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <Input
            label="Officer / Official Email"
            type="email"
            placeholder="officer@dems.gov"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            icon={Mail}
            required
            disabled={isSubmitting}
          />

          <div className="form-group" style={{ position: 'relative' }}>
            <label className="form-label">
              Password <span style={{ color: 'var(--color-danger-600)' }}>*</span>
            </label>
            <div style={{ position: 'relative' }}>
              <div style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-slate-400)' }}>
                <Lock size={16} />
              </div>
              <input
                type={showPassword ? 'text' : 'password'}
                className="form-input"
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isSubmitting}
                style={{ paddingLeft: '2.35rem', paddingRight: '2.5rem' }}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '0.75rem',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  color: 'var(--color-slate-400)',
                  padding: '0.2rem',
                  display: 'flex',
                  alignItems: 'center',
                }}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          <Button
            type="submit"
            variant="primary"
            disabled={isSubmitting}
            style={{ width: '100%', marginTop: '1rem', height: '42px' }}
          >
            {isSubmitting ? (
              <>
                <Loader2 size={16} style={{ animation: 'spin 1s linear infinite' }} />
                <span>Authenticating Credentials...</span>
              </>
            ) : (
              'Sign In to Portal'
            )}
          </Button>
        </form>

        <div style={{ marginTop: '2rem', paddingTop: '1.25rem', borderTop: '1px solid var(--color-slate-200)', textAlign: 'center', fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
          <p style={{ fontWeight: 600, color: 'var(--color-navy-900)' }}>Restricted Law Enforcement Network</p>
          <p style={{ marginTop: '0.25rem' }}>All access attempts logged to immutable forensic audit trail.</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
