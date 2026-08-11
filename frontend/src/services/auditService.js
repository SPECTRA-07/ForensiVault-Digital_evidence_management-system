import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const auditService = {
  getAllAuditLogs: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.BASE, { params });
  },

  getAuditLogById: async (id) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.BY_ID(id));
  },

  searchAuditLogs: async (searchParams = {}) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.SEARCH, { params: searchParams });
  },

  getAuditDashboard: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.DASHBOARD, { params });
  },

  getAuditLogsByUserId: async (userId, params = {}) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.BY_USER(userId), { params });
  },

  getAuditLogsByEntity: async (entityType, entityId, params = {}) => {
    return await apiClient.get(API_ENDPOINTS.AUDIT.BY_ENTITY(entityType, entityId), { params });
  },
};

export default auditService;
