import React, { useState, useEffect, useCallback } from 'react';
import { Upload, Search, Filter, Eye, ShieldCheck, ArrowRightLeft, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Select from '../components/Select';
import Table from '../components/Table';
import Pagination from '../components/Pagination';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EvidenceDetailsModal from '../components/evidence/EvidenceDetailsModal';
import EvidenceUploadModal from '../components/evidence/EvidenceUploadModal';
import IntegrityVerificationModal from '../components/integrity/IntegrityVerificationModal';
import CustodyTransferModal from '../components/custody/CustodyTransferModal';
import CustodyTimelineModal from '../components/custody/CustodyTimelineModal';
import evidenceService from '../services/evidenceService';

export const EvidencePage = () => {
  const [evidencePage, setEvidencePage] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters State
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  // Modals State
  const [uploadModalOpen, setUploadModalOpen] = useState(false);

  const [detailsModalOpen, setDetailsModalOpen] = useState(false);
  const [selectedEvidence, setSelectedEvidence] = useState(null);

  const [verifyModalOpen, setVerifyModalOpen] = useState(false);
  const [verifyTargetId, setVerifyTargetId] = useState(null);

  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [transferTargetEvidence, setTransferTargetEvidence] = useState(null);

  const [timelineModalOpen, setTimelineModalOpen] = useState(false);
  const [timelineTargetId, setTimelineTargetId] = useState(null);

  const statusOptions = [
    { value: '', label: 'All Evidence Statuses' },
    { value: 'REGISTERED', label: 'REGISTERED' },
    { value: 'TRANSFERRED', label: 'TRANSFERRED' },
    { value: 'ANALYZED', label: 'ANALYZED' },
    { value: 'PRESENTED_IN_COURT', label: 'PRESENTED_IN_COURT' },
    { value: 'ARCHIVED', label: 'ARCHIVED' },
    { value: 'DISPOSED', label: 'DISPOSED' },
  ];

  const typeOptions = [
    { value: '', label: 'All Categories' },
    { value: 'DOCUMENT', label: 'DOCUMENT' },
    { value: 'IMAGE', label: 'IMAGE' },
    { value: 'VIDEO', label: 'VIDEO' },
    { value: 'AUDIO', label: 'AUDIO' },
    { value: 'FORENSIC_IMAGE', label: 'FORENSIC_IMAGE' },
    { value: 'DISK_DUMP', label: 'DISK_DUMP' },
    { value: 'MEMORY_DUMP', label: 'MEMORY_DUMP' },
    { value: 'NETWORK_TRACE', label: 'NETWORK_TRACE' },
    { value: 'LOG_FILE', label: 'LOG_FILE' },
    { value: 'DATABASE_EXPORT', label: 'DATABASE_EXPORT' },
    { value: 'OTHER', label: 'OTHER' },
  ];

  const fetchEvidence = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);

    try {
      const searchParams = {
        page,
        size: 10,
        searchTerm: searchTerm.trim() || undefined,
        status: statusFilter || undefined,
        evidenceType: typeFilter || undefined,
      };

      let response;
      const hasFilter = searchParams.searchTerm || searchParams.status || searchParams.evidenceType;

      if (hasFilter) {
        response = await evidenceService.searchEvidence(searchParams);
      } else {
        response = await evidenceService.getAllEvidence({ page, size: 10 });
      }

      if (response && response.data) {
        setEvidencePage(response.data);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [searchTerm, statusFilter, typeFilter]);

  useEffect(() => {
    fetchEvidence(0);
  }, [fetchEvidence]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchEvidence(0);
  };

  const handleResetFilters = () => {
    setSearchTerm('');
    setStatusFilter('');
    setTypeFilter('');
  };

  const handleOpenDetails = (item) => {
    setSelectedEvidence(item);
    setDetailsModalOpen(true);
  };

  const handleOpenVerify = (item) => {
    setVerifyTargetId(item.id);
    setVerifyModalOpen(true);
  };

  const handleOpenTransfer = (item) => {
    setTransferTargetEvidence(item);
    setTransferModalOpen(true);
  };

  const handleOpenTimeline = (item) => {
    setTimelineTargetId(item.id);
    setTimelineModalOpen(true);
  };

  const formatBytes = (bytes) => {
    if (bytes === null || bytes === undefined || isNaN(bytes)) return '--';
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getCustodianDisplay = (row) => {
    if (row.currentCustodian?.fullName) {
      return `${row.currentCustodian.fullName}${row.currentCustodian.employeeId ? ` (${row.currentCustodian.employeeId})` : ''}`;
    }
    if (row.currentCustodian?.email) {
      return row.currentCustodian.email;
    }
    if (row.currentCustodianName) {
      return `${row.currentCustodianName}${row.currentCustodianId ? ` (ID: ${row.currentCustodianId})` : ''}`;
    }
    return '--';
  };

  const columns = [
    { header: 'Evidence #', accessor: 'evidenceNumber', render: (row) => <strong className="font-mono">{row.evidenceNumber}</strong> },
    { header: 'Title', accessor: 'evidenceName', render: (row) => <span>{row.evidenceName}</span> },
    { header: 'Type', accessor: 'evidenceType', render: (row) => <Badge status="INFO">{row.evidenceType}</Badge> },
    { header: 'Status', accessor: 'status', render: (row) => <Badge status={row.status}>{row.status}</Badge> },
    { header: 'File Size', accessor: 'fileSize', render: (row) => <span className="font-mono">{formatBytes(row.fileSize)}</span> },
    { header: 'Custodian', accessor: 'currentCustodian', render: (row) => <span>{getCustodianDisplay(row)}</span> },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Button variant="outline" size="sm" icon={Eye} onClick={() => handleOpenDetails(row)}>
            Details
          </Button>
          <Button variant="secondary" size="sm" icon={ShieldCheck} onClick={() => handleOpenVerify(row)}>
            Verify
          </Button>
          <Button variant="secondary" size="sm" icon={ArrowRightLeft} onClick={() => handleOpenTransfer(row)}>
            Transfer
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Digital Evidence Management"
        subtitle="Register, inspect payload details, verify SHA-256 hashes, and manage chain of custody."
        actions={
          <Button variant="primary" icon={Upload} onClick={() => setUploadModalOpen(true)}>
            Upload Evidence File
          </Button>
        }
      />

      {/* Search & Multi-Parameter Filter Bar */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem' }}>
        <form onSubmit={handleSearchSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginBottom: '0.75rem' }}>
            <Input
              placeholder="Search by evidence # or title..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              icon={Search}
            />

            <Select
              label="Evidence Status"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              options={statusOptions}
            />

            <Select
              label="Evidence Category"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              options={typeOptions}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
            <Button type="button" variant="outline" size="sm" onClick={handleResetFilters}>
              Reset Filters
            </Button>
            <Button type="submit" variant="primary" size="sm" icon={Search}>
              Search Evidence Vault
            </Button>
          </div>
        </form>
      </div>

      {error && <ErrorMessage error={error} onRetry={() => fetchEvidence(0)} />}

      {loading ? (
        <LoadingSpinner message="Retrieving digital evidence records from central vault..." />
      ) : (
        <>
          <Table
            columns={columns}
            data={evidencePage.content}
            keyField="id"
            emptyMessage="No digital evidence items registered matching query criteria."
          />
          <Pagination pageObj={evidencePage} onPageChange={(page) => fetchEvidence(page)} />
        </>
      )}

      {/* Comprehensive Modals */}
      <EvidenceUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onSuccess={() => fetchEvidence(0)}
      />

      <EvidenceDetailsModal
        isOpen={detailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        evidence={selectedEvidence}
        onVerifyIntegrity={(item) => { setDetailsModalOpen(false); handleOpenVerify(item); }}
        onInitiateTransfer={(item) => { setDetailsModalOpen(false); handleOpenTransfer(item); }}
        onViewTimeline={(item) => { setDetailsModalOpen(false); handleOpenTimeline(item); }}
      />

      <IntegrityVerificationModal
        isOpen={verifyModalOpen}
        onClose={() => setVerifyModalOpen(false)}
        evidenceId={verifyTargetId}
      />

      <CustodyTransferModal
        isOpen={transferModalOpen}
        onClose={() => setTransferModalOpen(false)}
        evidenceItem={transferTargetEvidence}
        onTransferSuccess={() => fetchEvidence(0)}
      />

      <CustodyTimelineModal
        isOpen={timelineModalOpen}
        onClose={() => setTimelineModalOpen(false)}
        evidenceId={timelineTargetId}
      />
    </div>
  );
};

export default EvidencePage;
