import React, { useState, useEffect, useCallback } from 'react';
import { GitCommit, ArrowRightLeft, Search, Clock, History, FileText, CheckCircle2 } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Table from '../components/Table';
import Pagination from '../components/Pagination';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import PendingTransferCard from '../components/custody/PendingTransferCard';
import CustodyTransferModal from '../components/custody/CustodyTransferModal';
import CustodyTimelineModal from '../components/custody/CustodyTimelineModal';
import CustodyDetailsModal from '../components/custody/CustodyDetailsModal';
import custodyService from '../services/custodyService';
import { useAuth } from '../hooks/useAuth';

export const CustodyPage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('all'); // 'all', 'pending'
  const [custodyPage, setCustodyPage] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  const [pendingRecords, setPendingRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals state
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [selectedEvidenceForTransfer, setSelectedEvidenceForTransfer] = useState(null);

  const [timelineModalOpen, setTimelineModalOpen] = useState(false);
  const [timelineEvidenceId, setTimelineEvidenceId] = useState(null);

  const [detailsModalOpen, setDetailsModalOpen] = useState(false);
  const [selectedCustodyRecord, setSelectedCustodyRecord] = useState(null);

  const fetchCustodyData = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);

    try {
      // Search all custody records and pending transfers concurrently
      const [allRes, pendingRes] = await Promise.allSettled([
        custodyService.searchCustodyRecords({ page, size: 10 }),
        custodyService.searchCustodyRecords({ transferStatus: 'PENDING', size: 50 }),
      ]);

      if (allRes.status === 'fulfilled' && allRes.value?.data) {
        setCustodyPage(allRes.value.data);
      }
      if (pendingRes.status === 'fulfilled' && pendingRes.value?.data) {
        setPendingRecords(pendingRes.value.data.content || []);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCustodyData(0);
  }, [fetchCustodyData]);

  const handleOpenTimeline = (evidenceId) => {
    setTimelineEvidenceId(evidenceId);
    setTimelineModalOpen(true);
  };

  const handleInspectRecord = (record) => {
    setSelectedCustodyRecord(record);
    setDetailsModalOpen(true);
  };

  const columns = [
    {
      header: 'Transfer ID',
      accessor: 'custodyNumber',
      render: (row) => <strong className="font-mono">{row.custodyNumber || `#CUST-${row.id}`}</strong>,
    },
    {
      header: 'Evidence Number',
      accessor: 'evidenceNumber',
      render: (row) => <span className="font-mono">{row.evidenceNumber || `#EV-${row.evidenceId}`}</span>,
    },
    {
      header: 'From Custodian',
      accessor: 'transferredBy',
      render: (row) => (
        <span>{row.transferredBy?.fullName || row.transferredBy?.email || row.transferredByName || '--'}</span>
      ),
    },
    {
      header: 'To Custodian',
      accessor: 'transferredTo',
      render: (row) => (
        <span>{row.transferredTo?.fullName || row.transferredTo?.email || row.transferredToName || '--'}</span>
      ),
    },
    {
      header: 'Purpose',
      accessor: 'transferPurpose',
      render: (row) => (row.transferPurpose ? <Badge status="SUBMITTED">{row.transferPurpose}</Badge> : '--'),
    },
    {
      header: 'Location',
      accessor: 'transferLocation',
      render: (row) => <span>{row.transferLocation || '--'}</span>,
    },
    {
      header: 'Status',
      accessor: 'transferStatus',
      render: (row) => <Badge status={row.transferStatus}>{row.transferStatus}</Badge>,
    },
    {
      header: 'Transferred At',
      accessor: 'transferredAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.transferredAt || row.createdAt ? new Date(row.transferredAt || row.createdAt).toLocaleString() : '--'}
        </span>
      ),
    },
    {
      header: 'Accepted At',
      accessor: 'acceptedAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.acceptedAt ? new Date(row.acceptedAt).toLocaleString() : '--'}
        </span>
      ),
    },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Button variant="outline" size="sm" onClick={() => handleInspectRecord(row)}>
            Details
          </Button>
          <Button variant="secondary" size="sm" icon={History} onClick={() => handleOpenTimeline(row.evidenceId)}>
            Timeline
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Chain of Custody Management"
        subtitle="Legally auditable evidence transfer handshakes & forensic movement history."
        actions={
          <Button variant="primary" icon={ArrowRightLeft} onClick={() => { setSelectedEvidenceForTransfer(null); setTransferModalOpen(true); }}>
            Initiate Transfer
          </Button>
        }
      />

      {/* Tab Navigation */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--color-slate-200)', paddingBottom: '0.5rem' }}>
        <button
          onClick={() => setActiveTab('all')}
          style={{
            padding: '0.5rem 1rem',
            border: 'none',
            borderRadius: 'var(--border-radius)',
            backgroundColor: activeTab === 'all' ? 'var(--color-navy-900)' : 'transparent',
            color: activeTab === 'all' ? '#ffffff' : 'var(--color-slate-600)',
            fontWeight: 600,
            fontSize: '0.8125rem',
            cursor: 'pointer',
          }}
        >
          All Custody Records ({custodyPage.totalElements || 0})
        </button>
        <button
          onClick={() => setActiveTab('pending')}
          style={{
            padding: '0.5rem 1rem',
            border: 'none',
            borderRadius: 'var(--border-radius)',
            backgroundColor: activeTab === 'pending' ? 'var(--color-warning-600)' : 'transparent',
            color: activeTab === 'pending' ? '#ffffff' : 'var(--color-slate-600)',
            fontWeight: 600,
            fontSize: '0.8125rem',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.35rem',
          }}
        >
          Pending Handshakes
          {pendingRecords.length > 0 && (
            <span style={{ backgroundColor: '#ffffff', color: 'var(--color-warning-700)', borderRadius: '9999px', padding: '0.1rem 0.4rem', fontSize: '0.7rem', fontWeight: 800 }}>
              {pendingRecords.length}
            </span>
          )}
        </button>
      </div>

      {error && <ErrorMessage error={error} onRetry={() => fetchCustodyData(0)} />}

      {/* Pending Handshakes Section */}
      {activeTab === 'pending' && (
        <div>
          {pendingRecords.length > 0 ? (
            pendingRecords.map((record) => (
              <PendingTransferCard key={record.id} record={record} onActionComplete={() => fetchCustodyData(0)} />
            ))
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--color-slate-500)' }}>
              <CheckCircle2 size={36} style={{ color: 'var(--color-success-600)', marginBottom: '0.5rem' }} />
              <h3>No Pending Custody Handshakes</h3>
              <p style={{ fontSize: '0.875rem' }}>All evidence custody transfers have been processed and resolved.</p>
            </div>
          )}
        </div>
      )}

      {/* All Custody Records Section */}
      {activeTab === 'all' && (
        <>
          <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem' }}>
            <Input
              placeholder="Search custody log by transfer ID, evidence number, or custodian..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              icon={Search}
            />
          </div>

          {loading ? (
            <LoadingSpinner message="Retrieving chain of custody handshake logs..." />
          ) : (
            <>
              <Table
                columns={columns}
                data={custodyPage.content}
                keyField="id"
                emptyMessage="No custody transfer handshakes recorded."
              />
              <Pagination pageObj={custodyPage} onPageChange={(page) => fetchCustodyData(page)} />
            </>
          )}
        </>
      )}

      {/* Modals */}
      <CustodyTransferModal
        isOpen={transferModalOpen}
        onClose={() => setTransferModalOpen(false)}
        evidenceItem={selectedEvidenceForTransfer}
        onTransferSuccess={() => fetchCustodyData(0)}
      />

      <CustodyTimelineModal
        isOpen={timelineModalOpen}
        onClose={() => setTimelineModalOpen(false)}
        evidenceId={timelineEvidenceId}
      />

      <CustodyDetailsModal
        isOpen={detailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        record={selectedCustodyRecord}
      />
    </div>
  );
};

export default CustodyPage;
