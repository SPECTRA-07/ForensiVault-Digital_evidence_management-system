import React, { useState, useEffect } from 'react';
import { ShieldCheck, ShieldAlert, CheckCircle2, AlertTriangle, RefreshCw, Search } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import StatCard from '../components/StatCard';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import IntegrityVerificationModal from '../components/integrity/IntegrityVerificationModal';
import integrityService from '../services/integrityService';

export const IntegrityPage = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchEvidenceId, setSearchEvidenceId] = useState('');

  // Modal State
  const [verifyModalOpen, setVerifyModalOpen] = useState(false);
  const [targetEvidenceId, setTargetEvidenceId] = useState(null);

  const fetchIntegrityReport = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await integrityService.getIntegritySummaryReport();
      if (response && response.data) {
        setReport(response.data);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIntegrityReport();
  }, []);

  const handleRunVerify = (id) => {
    if (!id) return;
    setTargetEvidenceId(id);
    setVerifyModalOpen(true);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchEvidenceId.trim()) {
      handleRunVerify(searchEvidenceId.trim());
    }
  };

  return (
    <div>
      <PageHeader
        title="Evidence Integrity & Tamper Detection"
        subtitle="Cryptographic SHA-256 hash verification and tamper auditing engine."
        actions={
          <Button variant="primary" icon={RefreshCw} onClick={fetchIntegrityReport}>
            Refresh Audit Summary
          </Button>
        }
      />

      {/* Verification Tool Search Card */}
      <div className="card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid var(--color-primary-600)' }}>
        <h3 style={{ margin: '0 0 0.5rem 0' }}>Live Forensic SHA-256 Verification Engine</h3>
        <p style={{ color: 'var(--color-slate-600)', fontSize: '0.875rem', marginBottom: '1rem' }}>
          Enter a Digital Evidence ID to execute a live SHA-256 checksum comparison against the vault original payload.
        </p>

        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.75rem', maxWidth: '520px' }}>
          <div style={{ flex: 1 }}>
            <Input
              placeholder="Enter Evidence ID (e.g. 10)..."
              type="number"
              value={searchEvidenceId}
              onChange={(e) => setSearchEvidenceId(e.target.value)}
              icon={Search}
              required
            />
          </div>
          <Button type="submit" variant="primary" icon={ShieldCheck}>
            Verify Evidence
          </Button>
        </form>
      </div>

      {/* Summary KPI Cards */}
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
        <StatCard
          title="Verified Intact Evidence"
          value={report?.verifiedCount ?? 0}
          subtitle="Cryptographically intact"
          icon={CheckCircle2}
          variant="success"
        />
        <StatCard
          title="Tamper Mismatches"
          value={report?.tamperedCount ?? 0}
          subtitle="Hash mismatch alerts"
          icon={AlertTriangle}
          variant="danger"
        />
      </div>

      {error && <ErrorMessage error={error} onRetry={fetchIntegrityReport} />}

      {loading ? (
        <LoadingSpinner message="Calculating SHA-256 integrity report across evidence vault..." />
      ) : (
        <div className="card">
          <h3 style={{ marginBottom: '1rem' }}>Stored Baseline Integrity Status Overview</h3>
          <p style={{ color: 'var(--color-slate-600)', fontSize: '0.875rem' }}>
            The ForensiVault forensic engine continuously logs every cryptographic verification event. Select any evidence record in the system to run an instant SHA-256 verification.
          </p>
        </div>
      )}

      {/* Verification Modal */}
      <IntegrityVerificationModal
        isOpen={verifyModalOpen}
        onClose={() => setVerifyModalOpen(false)}
        evidenceId={targetEvidenceId}
      />
    </div>
  );
};

export default IntegrityPage;
