import React, { useState, useEffect, useCallback } from 'react';
import { ClipboardList, Search, Filter, Eye, ShieldCheck, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Select from '../components/Select';
import Table from '../components/Table';
import Pagination from '../components/Pagination';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import AuditDetailsModal from '../components/audit/AuditDetailsModal';
import auditService from '../services/auditService';

export const AuditPage = () => {
  const [auditPage, setAuditPage] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters State
  const [actionFilter, setActionFilter] = useState('');
  const [entityTypeFilter, setEntityTypeFilter] = useState('');
  const [usernameFilter, setUsernameFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [startDateFilter, setStartDateFilter] = useState('');
  const [endDateFilter, setEndDateFilter] = useState('');
  const [entityRefFilter, setEntityRefFilter] = useState('');

  // Modal State
  const [detailsModalOpen, setDetailsModalOpen] = useState(false);
  const [selectedAuditId, setSelectedAuditId] = useState(null);

  const actionOptions = [
    { value: '', label: 'All Actions' },
    { value: 'LOGIN', label: 'USER_LOGIN' },
    { value: 'LOGOUT', label: 'USER_LOGOUT' },
    { value: 'CREATE', label: 'ENTITY_CREATE' },
    { value: 'UPDATE', label: 'ENTITY_UPDATE' },
    { value: 'TRANSFER_INITIATED', label: 'TRANSFER_INITIATED' },
    { value: 'TRANSFER_ACCEPTED', label: 'TRANSFER_ACCEPTED' },
    { value: 'TRANSFER_REJECTED', label: 'TRANSFER_REJECTED' },
    { value: 'INTEGRITY_VERIFICATION', label: 'INTEGRITY_VERIFICATION' },
    { value: 'EVIDENCE_DOWNLOAD', label: 'EVIDENCE_DOWNLOAD' },
    { value: 'ACCESS_DENIED', label: 'ACCESS_DENIED' },
  ];

  const entityTypeOptions = [
    { value: '', label: 'All Entity Types' },
    { value: 'USER', label: 'USER' },
    { value: 'CASE', label: 'CASE' },
    { value: 'EVIDENCE', label: 'EVIDENCE' },
    { value: 'CUSTODY', label: 'CUSTODY' },
    { value: 'SYSTEM', label: 'SYSTEM' },
  ];

  const statusOptions = [
    { value: '', label: 'All Audit Statuses' },
    { value: 'SUCCESS', label: 'SUCCESS' },
    { value: 'FAILURE', label: 'FAILURE' },
    { value: 'WARNING', label: 'WARNING' },
    { value: 'ATTEMPTED', label: 'ATTEMPTED' },
  ];

  const fetchAuditLogs = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);

    try {
      const searchParams = {
        page,
        size: 10,
        action: actionFilter || undefined,
        entityType: entityTypeFilter || undefined,
        username: usernameFilter.trim() || undefined,
        status: statusFilter || undefined,
        startDate: startDateFilter ? `${startDateFilter}T00:00:00Z` : undefined,
        endDate: endDateFilter ? `${endDateFilter}T23:59:59Z` : undefined,
        entityReference: entityRefFilter.trim() || undefined,
      };

      let response;
      const hasFilter = Object.values(searchParams).some((v) => v !== undefined && v !== page && v !== 10);

      if (hasFilter) {
        response = await auditService.searchAuditLogs(searchParams);
      } else {
        response = await auditService.getAllAuditLogs({ page, size: 10 });
      }

      if (response && response.data) {
        setAuditPage(response.data);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [actionFilter, entityTypeFilter, usernameFilter, statusFilter, startDateFilter, endDateFilter, entityRefFilter]);

  useEffect(() => {
    fetchAuditLogs(0);
  }, [fetchAuditLogs]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchAuditLogs(0);
  };

  const handleResetFilters = () => {
    setActionFilter('');
    setEntityTypeFilter('');
    setUsernameFilter('');
    setStatusFilter('');
    setStartDateFilter('');
    setEndDateFilter('');
    setEntityRefFilter('');
  };

  const handleInspectAudit = (id) => {
    setSelectedAuditId(id);
    setDetailsModalOpen(true);
  };

  const columns = [
    {
      header: 'Audit ID',
      accessor: 'auditNumber',
      render: (row) => <strong className="font-mono">{row.auditNumber || `#AUD-${row.id}`}</strong>,
    },
    {
      header: 'Action',
      accessor: 'action',
      render: (row) => <Badge status="INFO">{row.action}</Badge>,
    },
    {
      header: 'Entity Type',
      accessor: 'entityType',
      render: (row) => <span className="font-mono" style={{ fontWeight: 600 }}>{row.entityType || '--'}</span>,
    },
    {
      header: 'Module',
      accessor: 'moduleName',
      render: (row) => <Badge status="SUBMITTED">{row.moduleName || 'CORE'}</Badge>,
    },
    {
      header: 'Reference',
      accessor: 'entityReference',
      render: (row) => <span className="font-mono" style={{ fontSize: '0.75rem' }}>{row.entityReference || '--'}</span>,
    },
    {
      header: 'Performer Username',
      accessor: 'username',
      render: (row) => <span>{row.username || 'System / Guest'}</span>,
    },
    {
      header: 'IP Address',
      accessor: 'ipAddress',
      render: (row) => <span className="font-mono" style={{ fontSize: '0.75rem' }}>{row.ipAddress || '127.0.0.1'}</span>,
    },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => <Badge status={row.status}>{row.status}</Badge>,
    },
    {
      header: 'Timestamp',
      accessor: 'actionTimestamp',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.actionTimestamp ? new Date(row.actionTimestamp).toLocaleString() : '--'}
        </span>
      ),
    },
    {
      header: 'Inspect',
      render: (row) => (
        <Button variant="outline" size="sm" icon={Eye} onClick={() => handleInspectAudit(row.id)}>
          Inspect
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Immutable System Audit Logs"
        subtitle="Centralized forensic activity tracking and tamper-evident audit records."
        actions={
          <Button variant="outline" icon={RefreshCw} onClick={() => fetchAuditLogs(0)}>
            Refresh Logs
          </Button>
        }
      />

      {/* Multi-Parameter Search & Filter Bar */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem' }}>
        <form onSubmit={handleSearchSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem', marginBottom: '0.75rem' }}>
            <Select
              label="Action"
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              options={actionOptions}
            />

            <Select
              label="Entity Type"
              value={entityTypeFilter}
              onChange={(e) => setEntityTypeFilter(e.target.value)}
              options={entityTypeOptions}
            />

            <Select
              label="Status"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              options={statusOptions}
            />

            <Input
              label="Username"
              placeholder="e.g. admin@dems.gov"
              value={usernameFilter}
              onChange={(e) => setUsernameFilter(e.target.value)}
            />

            <Input
              label="Entity Reference"
              placeholder="e.g. CASE-2026-001"
              value={entityRefFilter}
              onChange={(e) => setEntityRefFilter(e.target.value)}
            />

            <Input
              label="Start Date"
              type="date"
              value={startDateFilter}
              onChange={(e) => setStartDateFilter(e.target.value)}
            />

            <Input
              label="End Date"
              type="date"
              value={endDateFilter}
              onChange={(e) => setEndDateFilter(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
            <Button type="button" variant="outline" size="sm" onClick={handleResetFilters}>
              Reset Filters
            </Button>
            <Button type="submit" variant="primary" size="sm" icon={Search}>
              Search Audit Trail
            </Button>
          </div>
        </form>
      </div>

      {error && <ErrorMessage error={error} onRetry={() => fetchAuditLogs(0)} />}

      {loading ? (
        <LoadingSpinner message="Querying centralized immutable audit trail..." />
      ) : (
        <>
          <Table
            columns={columns}
            data={auditPage.content}
            keyField="id"
            emptyMessage="No audit log records found matching search filters."
          />
          <Pagination pageObj={auditPage} onPageChange={(page) => fetchAuditLogs(page)} />
        </>
      )}

      {/* Immutable Audit Inspection Modal */}
      <AuditDetailsModal
        isOpen={detailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        auditId={selectedAuditId}
      />
    </div>
  );
};

export default AuditPage;
