import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Search, Filter, Eye, Edit3, RefreshCw, UserCheck } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Table from '../components/Table';
import Pagination from '../components/Pagination';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import CaseDetailsModal from '../components/cases/CaseDetailsModal';
import CaseFormModal from '../components/cases/CaseFormModal';
import CaseStatusModal from '../components/cases/CaseStatusModal';
import CaseAssignmentModal from '../components/cases/CaseAssignmentModal';
import EvidenceDetailsModal from '../components/evidence/EvidenceDetailsModal';
import caseService from '../services/caseService';
import { useAuth } from '../hooks/useAuth';

export const CasesPage = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [casesPage, setCasesPage] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals state
  const [detailsModalOpen, setDetailsModalOpen] = useState(false);
  const [selectedCaseId, setSelectedCaseId] = useState(null);

  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editCaseItem, setEditCaseItem] = useState(null);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusTargetCase, setStatusTargetCase] = useState(null);

  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [assignTargetCase, setAssignTargetCase] = useState(null);

  // Evidence Modal state for cross-navigation
  const [evidenceDetailsOpen, setEvidenceDetailsOpen] = useState(false);
  const [selectedEvidenceItem, setSelectedEvidenceItem] = useState(null);

  const fetchCases = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (searchTerm.trim()) {
        response = await caseService.searchCases({ searchTerm: searchTerm.trim(), page, size: 10 });
      } else {
        response = await caseService.getAllCases({ page, size: 10 });
      }
      if (response && response.data) {
        setCasesPage(response.data);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [searchTerm]);

  useEffect(() => {
    fetchCases(0);
  }, [fetchCases]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchCases(0);
  };

  const handleOpenDetails = (row) => {
    setSelectedCaseId(row.id);
    setDetailsModalOpen(true);
  };

  const handleOpenCreate = () => {
    setEditCaseItem(null);
    setFormModalOpen(true);
  };

  const handleOpenEdit = (row) => {
    setEditCaseItem(row);
    setFormModalOpen(true);
  };

  const handleOpenStatus = (row) => {
    setStatusTargetCase(row);
    setStatusModalOpen(true);
  };

  const handleOpenAssign = (row) => {
    setAssignTargetCase(row);
    setAssignModalOpen(true);
  };

  const handleOpenEvidenceDetails = (evidenceItem) => {
    setSelectedEvidenceItem(evidenceItem);
    setEvidenceDetailsOpen(true);
  };

  const columns = [
    {
      header: 'Case Number',
      accessor: 'caseNumber',
      render: (row) => <strong className="font-mono">{row.caseNumber}</strong>,
    },
    {
      header: 'Crime #',
      accessor: 'crimeNumber',
      render: (row) => <span className="font-mono">{row.crimeNumber || '--'}</span>,
    },
    {
      header: 'Case Name / Title',
      accessor: 'caseName',
      render: (row) => <span>{row.caseName}</span>,
    },
    {
      header: 'Type',
      accessor: 'crimeType',
      render: (row) => <Badge status="SUBMITTED">{row.crimeType}</Badge>,
    },
    {
      header: 'Severity',
      accessor: 'severity',
      render: (row) => <Badge status={row.severity}>{row.severity}</Badge>,
    },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => <Badge status={row.status}>{row.status}</Badge>,
    },
    {
      header: 'Lead Officer',
      accessor: 'assignedOfficerName',
      render: (row) => <span>{row.assignedOfficerName || row.assignedOfficer?.fullName || 'Unassigned'}</span>,
    },
    {
      header: 'Incident Date',
      accessor: 'incidentDate',
      render: (row) => <span className="font-mono" style={{ fontSize: '0.75rem' }}>{row.incidentDate || '--'}</span>,
    },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Button variant="outline" size="sm" icon={Eye} onClick={() => handleOpenDetails(row)}>
            Details
          </Button>
          <Button variant="secondary" size="sm" icon={Edit3} onClick={() => handleOpenEdit(row)}>
            Edit
          </Button>
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={() => handleOpenStatus(row)}>
            Status
          </Button>
          {isAdmin && (
            <Button variant="secondary" size="sm" icon={UserCheck} onClick={() => handleOpenAssign(row)}>
              Assign
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Criminal Case Management"
        subtitle="Create, search, inspect, and assign criminal investigation case files."
        actions={
          isAdmin && (
            <Button variant="primary" icon={Plus} onClick={handleOpenCreate}>
              Create New Case
            </Button>
          )
        }
      />

      {/* Search & Filter Bar */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '240px' }}>
            <Input
              placeholder="Search cases by case number, FIR crime number, or title..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              icon={Search}
            />
          </div>
          <Button type="submit" variant="primary" icon={Search}>
            Search Cases
          </Button>
        </form>
      </div>

      {error && <ErrorMessage error={error} onRetry={() => fetchCases(0)} />}

      {loading ? (
        <LoadingSpinner message="Retrieving criminal investigation cases from backend..." />
      ) : (
        <>
          <Table
            columns={columns}
            data={casesPage.content}
            keyField="id"
            emptyMessage="No criminal cases found matching query criteria."
          />
          <Pagination pageObj={casesPage} onPageChange={(page) => fetchCases(page)} />
        </>
      )}

      {/* Case Management Modals */}
      <CaseDetailsModal
        isOpen={detailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        caseId={selectedCaseId}
        onOpenEvidenceDetails={handleOpenEvidenceDetails}
      />

      <CaseFormModal
        isOpen={formModalOpen}
        onClose={() => setFormModalOpen(false)}
        editCase={editCaseItem}
        onSuccess={(createdOrUpdatedCase) => {
          fetchCases(0);
          // If Admin created a new case, auto-launch CaseAssignmentModal for the new case
          if (isAdmin && !editCaseItem && createdOrUpdatedCase && createdOrUpdatedCase.id) {
            setAssignTargetCase(createdOrUpdatedCase);
            setAssignModalOpen(true);
          }
        }}
      />

      <CaseStatusModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        caseItem={statusTargetCase}
        onSuccess={() => fetchCases(0)}
      />

      <CaseAssignmentModal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        caseItem={assignTargetCase}
        onSuccess={() => fetchCases(0)}
      />

      <EvidenceDetailsModal
        isOpen={evidenceDetailsOpen}
        onClose={() => setEvidenceDetailsOpen(false)}
        evidence={selectedEvidenceItem}
      />
    </div>
  );
};

export default CasesPage;
