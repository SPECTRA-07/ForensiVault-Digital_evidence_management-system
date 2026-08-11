import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import DashboardHeader from '../components/dashboard/DashboardHeader';
import DateRangeFilter from '../components/dashboard/DateRangeFilter';
import DashboardStats from '../components/dashboard/DashboardStats';
import SystemHealthCard from '../components/dashboard/SystemHealthCard';
import CaseAnalytics from '../components/dashboard/CaseAnalytics';
import EvidenceAnalytics from '../components/dashboard/EvidenceAnalytics';
import IntegrityAnalytics from '../components/dashboard/IntegrityAnalytics';
import CustodyAnalytics from '../components/dashboard/CustodyAnalytics';
import AuditAnalytics from '../components/dashboard/AuditAnalytics';
import RecentActivityFeed from '../components/dashboard/RecentActivityFeed';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import Button from '../components/Button';
import dashboardService from '../services/dashboardService';
import { useAuth } from '../hooks/useAuth';

export const DashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const canViewDashboard = user?.role === 'ADMIN' || user?.role === 'FORENSIC_EXPERT';

  const [summary, setSummary] = useState(null);
  const [caseAnalytics, setCaseAnalytics] = useState(null);
  const [evidenceAnalytics, setEvidenceAnalytics] = useState(null);
  const [integrityAnalytics, setIntegrityAnalytics] = useState(null);
  const [custodyAnalytics, setCustodyAnalytics] = useState(null);
  const [auditAnalytics, setAuditAnalytics] = useState(null);
  const [recentActivities, setRecentActivities] = useState([]);
  const [systemHealth, setSystemHealth] = useState(null);

  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [lastRefreshed, setLastRefreshed] = useState(null);
  const [dateParams, setDateParams] = useState({});

  const fetchDashboardData = useCallback(async (params = dateParams) => {
    if (!canViewDashboard) {
      setLoading(false);
      return;
    }

    setIsRefreshing(true);
    setError(null);

    try {
      const results = await Promise.allSettled([
        dashboardService.getSummary(params),
        dashboardService.getCaseAnalytics(params),
        dashboardService.getEvidenceAnalytics(params),
        dashboardService.getIntegrityAnalytics(params),
        dashboardService.getCustodyAnalytics(params),
        dashboardService.getAuditAnalytics(params),
        dashboardService.getRecentActivities(),
        dashboardService.getSystemHealth(),
      ]);

      // Check for role access restriction (403) across promises
      const forbiddenError = results.find(
        (r) => r.status === 'rejected' && r.reason?.status === 403
      );

      if (forbiddenError) {
        setError(forbiddenError.reason);
        return;
      }

      // Unpack successful responses
      if (results[0].status === 'fulfilled' && results[0].value?.data) {
        setSummary(results[0].value.data);
      }
      if (results[1].status === 'fulfilled' && results[1].value?.data) {
        setCaseAnalytics(results[1].value.data);
      }
      if (results[2].status === 'fulfilled' && results[2].value?.data) {
        setEvidenceAnalytics(results[2].value.data);
      }
      if (results[3].status === 'fulfilled' && results[3].value?.data) {
        setIntegrityAnalytics(results[3].value.data);
      }
      if (results[4].status === 'fulfilled' && results[4].value?.data) {
        setCustodyAnalytics(results[4].value.data);
      }
      if (results[5].status === 'fulfilled' && results[5].value?.data) {
        setAuditAnalytics(results[5].value.data);
      }
      if (results[6].status === 'fulfilled' && results[6].value?.data) {
        setRecentActivities(results[6].value.data);
      }
      if (results[7].status === 'fulfilled' && results[7].value?.data) {
        setSystemHealth(results[7].value.data);
      }

      setLastRefreshed(new Date());
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
      setIsRefreshing(false);
    }
  }, [dateParams, canViewDashboard]);

  useEffect(() => {
    fetchDashboardData(dateParams);
  }, [fetchDashboardData, dateParams]);

  if (!canViewDashboard) {
    return (
      <div style={{ padding: '3rem 1.5rem', maxWidth: '640px', margin: '2rem auto' }}>
        <div className="card" style={{ borderLeft: '4px solid var(--color-danger-600)', padding: '2rem', textAlign: 'center' }}>
          <div style={{ display: 'inline-flex', padding: '0.875rem', borderRadius: '50%', backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-600)', marginBottom: '1rem' }}>
            <ShieldAlert size={40} />
          </div>
          <h2 style={{ color: 'var(--color-navy-900)', margin: '0 0 0.5rem 0' }}>403 - Dashboard Access Restricted</h2>
          <p style={{ color: 'var(--color-slate-600)', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
            Dashboard access is restricted to Administrators and Forensic Experts.
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

  const handleApplyFilter = (newParams) => {
    setDateParams(newParams);
  };

  const handleResetFilter = () => {
    setDateParams({});
  };

  return (
    <div>
      <DashboardHeader
        lastRefreshed={lastRefreshed}
        onRefresh={() => fetchDashboardData(dateParams)}
        isRefreshing={isRefreshing}
      />

      <DateRangeFilter onApplyFilter={handleApplyFilter} onResetFilter={handleResetFilter} />

      {error && <ErrorMessage error={error} onRetry={() => fetchDashboardData(dateParams)} />}

      {loading ? (
        <LoadingSpinner message="Querying executive analytics and system health from Spring Boot backend..." />
      ) : (
        <>
          <DashboardStats summary={summary} />
          <SystemHealthCard health={systemHealth} />

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <CaseAnalytics analytics={caseAnalytics} />
            <EvidenceAnalytics analytics={evidenceAnalytics} />
            <IntegrityAnalytics analytics={integrityAnalytics} />
            <CustodyAnalytics analytics={custodyAnalytics} />
            <AuditAnalytics analytics={auditAnalytics} />
            <RecentActivityFeed activities={recentActivities} />
          </div>
        </>
      )}
    </div>
  );
};

export default DashboardPage;
