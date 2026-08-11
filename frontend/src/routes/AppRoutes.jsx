import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import CasesPage from '../pages/CasesPage';
import EvidencePage from '../pages/EvidencePage';
import CustodyPage from '../pages/CustodyPage';
import IntegrityPage from '../pages/IntegrityPage';
import AuditPage from '../pages/AuditPage';
import QrPage from '../pages/QrPage';
import UsersPage from '../pages/UsersPage';
import { useAuth } from '../hooks/useAuth';

// Helper component for dynamic index route redirection based on role
const RoleBasedIndexRedirect = () => {
  const { user } = useAuth();
  const canViewDashboard = user?.role === 'ADMIN' || user?.role === 'FORENSIC_EXPERT';
  return <Navigate to={canViewDashboard ? '/dashboard' : '/cases'} replace />;
};

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Authentication Route */}
      <Route path="/login" element={<LoginPage />} />

      {/* Main Layout Protected Routes */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<RoleBasedIndexRedirect />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'FORENSIC_EXPERT']}>
              <DashboardPage />
            </ProtectedRoute>
          }
        />

        <Route path="/cases" element={<CasesPage />} />
        <Route path="/evidence" element={<EvidencePage />} />
        <Route path="/custody" element={<CustodyPage />} />
        <Route path="/integrity" element={<IntegrityPage />} />
        <Route path="/audit" element={<AuditPage />} />
        
        <Route
          path="/users"
          element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <UsersPage />
            </ProtectedRoute>
          }
        />

        <Route path="/qr" element={<QrPage />} />
      </Route>

      {/* Fallback redirect */}
      <Route path="*" element={<RoleBasedIndexRedirect />} />
    </Routes>
  );
};

export default AppRoutes;
