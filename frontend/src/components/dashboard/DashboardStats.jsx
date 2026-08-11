import React from 'react';
import { Briefcase, FileCheck2, ShieldCheck, GitCommit, ClipboardList } from 'lucide-react';
import StatCard from '../StatCard';

export const DashboardStats = ({ summary }) => {
  if (!summary) return null;

  return (
    <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
      <StatCard
        title="Total Cases"
        value={summary.totalCases ?? 0}
        subtitle={`${summary.openCases ?? 0} open • ${summary.underInvestigationCases ?? 0} active`}
        icon={Briefcase}
        variant="primary"
      />
      <StatCard
        title="Digital Evidence Items"
        value={summary.totalEvidence ?? 0}
        subtitle={`${summary.verifiedEvidence ?? 0} verified • ${summary.tamperedEvidence ?? 0} tampered`}
        icon={FileCheck2}
        variant="info"
      />
      <StatCard
        title="Cryptographic Verifications"
        value={summary.verifiedEvidence ?? 0}
        subtitle="SHA-256 intact payload records"
        icon={ShieldCheck}
        variant="success"
      />
      <StatCard
        title="Pending Handshakes"
        value={summary.pendingCustodyTransfers ?? 0}
        subtitle="Custody transfers awaiting acceptance"
        icon={GitCommit}
        variant={summary.pendingCustodyTransfers > 0 ? 'warning' : 'primary'}
      />
      <StatCard
        title="Today's Audit Events"
        value={summary.todaysAuditEvents ?? 0}
        subtitle={`${summary.successfulAuditEvents ?? 0} ok • ${summary.failedAuditEvents ?? 0} failed`}
        icon={ClipboardList}
        variant="info"
      />
    </div>
  );
};

export default DashboardStats;
