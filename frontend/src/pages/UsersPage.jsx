import React, { useState, useEffect, useCallback } from 'react';
import { UserPlus, Search, User, Shield, CheckCircle2, XCircle, Eye, Edit3, Power, RefreshCw } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Table from '../components/Table';
import Pagination from '../components/Pagination';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import UserDetailsModal from '../components/users/UserDetailsModal';
import UserFormModal from '../components/users/UserFormModal';
import UserStatusModal from '../components/users/UserStatusModal';
import userService from '../services/userService';
import { useAuth } from '../hooks/useAuth';

export const UsersPage = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals State
  const [detailsModalOpen, setDetailsModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editUserItem, setEditUserItem] = useState(null);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusTargetUser, setStatusTargetUser] = useState(null);

  const fetchUsers = useCallback(async () => {
    if (!isAdmin) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await userService.getAllUsers();
      if (response && response.data) {
        setUsersList(response.data);
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleOpenDetails = (u) => {
    setSelectedUser(u);
    setDetailsModalOpen(true);
  };

  const handleOpenCreate = () => {
    setEditUserItem(null);
    setFormModalOpen(true);
  };

  const handleOpenEdit = (u) => {
    setEditUserItem(u);
    setFormModalOpen(true);
  };

  const handleOpenStatus = (u) => {
    setStatusTargetUser(u);
    setStatusModalOpen(true);
  };

  const filteredUsers = usersList.filter((u) => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    return (
      (u.employeeId && u.employeeId.toLowerCase().includes(term)) ||
      (u.fullName && u.fullName.toLowerCase().includes(term)) ||
      (u.email && u.email.toLowerCase().includes(term)) ||
      (u.role && u.role.toLowerCase().includes(term)) ||
      (u.department && u.department.toLowerCase().includes(term))
    );
  });

  const columns = [
    {
      header: 'Employee ID',
      accessor: 'employeeId',
      render: (row) => <strong className="font-mono">{row.employeeId || '--'}</strong>,
    },
    {
      header: 'Full Name',
      accessor: 'fullName',
      render: (row) => <span>{row.fullName}</span>,
    },
    {
      header: 'Email Address',
      accessor: 'email',
      render: (row) => <span className="font-mono" style={{ fontSize: '0.8125rem' }}>{row.email}</span>,
    },
    {
      header: 'Role',
      accessor: 'role',
      render: (row) => <Badge status={row.role}>{row.role}</Badge>,
    },
    {
      header: 'Department',
      accessor: 'department',
      render: (row) => <span>{row.department || '--'}</span>,
    },
    {
      header: 'Account Status',
      accessor: 'active',
      render: (row) => (
        <Badge status={row.active ? 'ACTIVE' : 'DISABLED'}>
          {row.active ? 'ACTIVE' : 'DISABLED'}
        </Badge>
      ),
    },
    {
      header: 'Created At',
      accessor: 'createdAt',
      render: (row) => (
        <span className="font-mono" style={{ fontSize: '0.75rem' }}>
          {row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '--'}
        </span>
      ),
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
          <Button
            variant={row.active ? 'danger' : 'secondary'}
            size="sm"
            icon={Power}
            onClick={() => handleOpenStatus(row)}
          >
            {row.active ? 'Disable' : 'Enable'}
          </Button>
        </div>
      ),
    },
  ];

  if (!isAdmin) {
    return (
      <div>
        <PageHeader title="User Account Management" subtitle="System user administration & security authorization control." />
        <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--color-slate-600)' }}>
          <Shield size={48} style={{ color: 'var(--color-danger-600)', marginBottom: '1rem' }} />
          <h2>Access Restricted — Administrator Permission Required</h2>
          <p style={{ maxWidth: '480px', margin: '0.5rem auto 0 auto', fontSize: '0.875rem' }}>
            You do not have administrative privileges to access user management. Please contact a system administrator if you require account role modifications.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="User Account Administration"
        subtitle="Manage system accounts, authorization roles, and security access controls."
        actions={
          <Button variant="primary" icon={UserPlus} onClick={handleOpenCreate}>
            Create New User Account
          </Button>
        }
      />

      {/* Filter Bar */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem' }}>
        <Input
          placeholder="Filter users by employee ID, name, email, role, or department..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          icon={Search}
        />
      </div>

      {error && <ErrorMessage error={error} onRetry={fetchUsers} />}

      {loading ? (
        <LoadingSpinner message="Retrieving registered user accounts from backend..." />
      ) : (
        <Table
          columns={columns}
          data={filteredUsers}
          keyField="id"
          emptyMessage="No registered user accounts found matching filter criteria."
        />
      )}

      {/* Modals */}
      <UserDetailsModal
        isOpen={detailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        userItem={selectedUser}
      />

      <UserFormModal
        isOpen={formModalOpen}
        onClose={() => setFormModalOpen(false)}
        editUser={editUserItem}
        onSuccess={fetchUsers}
      />

      <UserStatusModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        userItem={statusTargetUser}
        onSuccess={fetchUsers}
      />
    </div>
  );
};

export default UsersPage;
