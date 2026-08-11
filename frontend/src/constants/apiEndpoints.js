/**
 * Centralized DEMS API Endpoint Registry
 * Synchronized with existing Spring Boot Controller `@RequestMapping` path definitions.
 */
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
  },
  CASES: {
    BASE: '/cases',
    MY_CASES: '/cases/my-cases',
    BY_ID: (id) => `/cases/${id}`,
    STATUS: (id) => `/cases/${id}/status`,
    ASSIGN_OFFICER: (id) => `/cases/${id}/assign-officer`,
    SEARCH: '/cases/search',
  },
  EVIDENCE: {
    BASE: '/evidence',
    UPLOAD: '/evidence/upload',
    BY_ID: (id) => `/evidence/${id}`,
    DOWNLOAD: (id) => `/evidence/download/${id}`,
    STATUS: (id) => `/evidence/${id}/status`,
    BY_CASE: (caseId) => `/evidence/case/${caseId}`,
    SEARCH: '/evidence/search',
    INTEGRITY: {
      VERIFY: (id) => `/evidence/${id}/verify`,
      STORED: (id) => `/evidence/${id}/integrity`,
      REPORT: '/evidence/integrity/report',
    },
  },
  CUSTODY: {
    BASE: '/custody',
    TRANSFER: '/custody/transfer',
    ACCEPT: (id) => `/custody/${id}/accept`,
    BY_ID: (id) => `/custody/${id}`,
    BY_EVIDENCE: (evidenceId) => `/custody/evidence/${evidenceId}`,
    HISTORY: (evidenceId) => `/custody/history/${evidenceId}`,
    SEARCH: '/custody/search',
  },
  AUDIT: {
    BASE: '/audit',
    BY_ID: (id) => `/audit/${id}`,
    SEARCH: '/audit/search',
    DASHBOARD: '/audit/dashboard',
    BY_USER: (userId) => `/audit/user/${userId}`,
    BY_ENTITY: (entityType, entityId) => `/audit/entity/${entityType}/${entityId}`,
  },
  DASHBOARD: {
    SUMMARY: '/dashboard/summary',
    CASES: '/dashboard/cases',
    EVIDENCE: '/dashboard/evidence',
    INTEGRITY: '/dashboard/integrity',
    CUSTODY: '/dashboard/custody',
    AUDIT: '/dashboard/audit',
    RECENT_ACTIVITIES: '/dashboard/recent-activities',
    SYSTEM_HEALTH: '/dashboard/system-health',
  },
  QR: {
    INFO: (evidenceId) => `/qr/evidence/${evidenceId}`,
    IMAGE: (evidenceId) => `/qr/evidence/${evidenceId}/image`,
    REGENERATE: (evidenceId) => `/qr/evidence/${evidenceId}/regenerate`,
    RESOLVE: (evidenceNumber) => `/qr/resolve/${evidenceNumber}`,
  },
  USERS: {
    BASE: '/users',
    BY_ID: (id) => `/users/${id}`,
    STATUS: (id) => `/users/${id}/status`,
  },
};
