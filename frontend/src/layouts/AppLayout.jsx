import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  ShieldCheck,
  LayoutDashboard,
  Briefcase,
  FileCheck2,
  GitCommit,
  ShieldAlert,
  ClipboardList,
  QrCode,
  Users,
  LogOut,
  Search,
  Lock,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import Badge from '../components/Badge';

export const AppLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const canViewDashboard = user?.role === 'ADMIN' || user?.role === 'FORENSIC_EXPERT';
  const canViewUsers = user?.role === 'ADMIN';

  const navItems = [
    ...(canViewDashboard ? [{ label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard }] : []),
    { label: 'Cases', path: '/cases', icon: Briefcase },
    { label: 'Evidence', path: '/evidence', icon: FileCheck2 },
    { label: 'Chain of Custody', path: '/custody', icon: GitCommit },
    { label: 'Integrity Verification', path: '/integrity', icon: ShieldAlert },
    { label: 'Audit Logs', path: '/audit', icon: ClipboardList },
    ...(canViewUsers ? [{ label: 'User Management', path: '/users', icon: Users }] : []),
    { label: 'QR Tracking', path: '/qr', icon: QrCode },
  ];

  return (
    <div className="app-container">
      {/* Sidebar Navigation */}
      <aside
        style={{
          width: 'var(--sidebar-width)',
          backgroundColor: 'var(--color-navy-900)',
          color: 'var(--color-white)',
          display: 'flex',
          flexDirection: 'column',
          flexShrink: 0,
          borderRight: '1px solid var(--color-navy-800)',
        }}
      >
        {/* DEMS Brand Header */}
        <div
          style={{
            height: 'var(--header-height)',
            padding: '0 1.25rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            borderBottom: '1px solid var(--color-navy-800)',
          }}
        >
          <div
            style={{
              padding: '0.4rem',
              borderRadius: '6px',
              backgroundColor: 'var(--color-primary-600)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <ShieldCheck size={22} color="#ffffff" />
          </div>
          <div>
            <div style={{ fontWeight: 800, fontSize: '1.1rem', letterSpacing: '0.04em', lineHeight: 1 }}>
              ForensiVault
            </div>
            <div style={{ fontSize: '0.65rem', color: 'var(--color-slate-400)', textTransform: 'uppercase', letterSpacing: '0.08em', marginTop: '0.25rem' }}>
              Forensic Portal
            </div>
          </div>
        </div>

        {/* Sidebar Nav Links */}
        <nav style={{ flex: 1, padding: '1rem 0.75rem', overflowY: 'auto' }}>
          <div style={{ fontSize: '0.65rem', fontWeight: 700, color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.1em', padding: '0 0.5rem 0.5rem 0.5rem' }}>
            System Navigation
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                style={({ isActive }) => ({
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--border-radius)',
                  fontSize: '0.875rem',
                  fontWeight: isActive ? 600 : 400,
                  color: isActive ? 'var(--color-white)' : 'var(--color-slate-400)',
                  backgroundColor: isActive ? 'var(--color-primary-600)' : 'transparent',
                  marginBottom: '0.25rem',
                  textDecoration: 'none',
                  transition: 'var(--transition)',
                })}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Security Info Footer */}
        <div
          style={{
            padding: '1rem 1.25rem',
            borderTop: '1px solid var(--color-navy-800)',
            backgroundColor: 'var(--color-navy-950)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-slate-400)' }}>
            <Lock size={14} style={{ color: 'var(--color-success-600)' }} />
            <span>JWT Session Active</span>
          </div>
        </div>
      </aside>

      {/* Main Content Wrapper */}
      <div className="main-wrapper">
        {/* Top Header Bar */}
        <header
          style={{
            height: 'var(--header-height)',
            backgroundColor: 'var(--color-white)',
            borderBottom: '1px solid var(--color-slate-200)',
            padding: '0 1.75rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: 'var(--shadow-sm)',
          }}
        >
          {/* Header Global Search Bar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: 1, maxWidth: '480px' }}>
            <div style={{ position: 'relative', width: '100%' }}>
              <Search size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-slate-400)' }} />
              <input
                type="text"
                placeholder="Search case #, evidence ID, or custody log..."
                className="form-input"
                style={{ paddingLeft: '2.25rem', height: '36px', fontSize: '0.8125rem' }}
              />
            </div>
          </div>

          {/* User Profile & Logout Pill */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.35rem 0.85rem', borderRadius: '9999px', backgroundColor: 'var(--color-slate-100)', border: '1px solid var(--color-slate-200)' }}>
              <div style={{ width: '30px', height: '30px', borderRadius: '50%', backgroundColor: 'var(--color-primary-600)', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: '0.8125rem' }}>
                {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <div style={{ fontSize: '0.8125rem' }}>
                <div style={{ fontWeight: 600, color: 'var(--color-navy-900)', lineHeight: 1.2 }}>
                  {user?.fullName || user?.email || 'Officer'}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', marginTop: '0.1rem' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--color-slate-500)' }}>
                    ID: {user?.employeeId || '--'}
                  </span>
                  <Badge status={user?.role}>{user?.role || 'USER'}</Badge>
                </div>
              </div>
            </div>

            <button
              onClick={handleLogout}
              title="Sign Out of ForensiVault"
              style={{
                background: 'none',
                border: '1px solid var(--color-slate-300)',
                borderRadius: 'var(--border-radius)',
                padding: '0.45rem 0.75rem',
                cursor: 'pointer',
                color: 'var(--color-slate-700)',
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                fontSize: '0.8125rem',
                fontWeight: 500,
              }}
            >
              <LogOut size={16} />
              <span>Logout</span>
            </button>
          </div>
        </header>

        {/* Dynamic Outlet Page Content */}
        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
