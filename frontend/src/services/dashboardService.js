import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

/**
 * Service for fetching executive dashboard and module analytics from Spring Boot DashboardController.
 */
export const dashboardService = {
  getSummary: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.SUMMARY, { params });
  },

  getCaseAnalytics: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.CASES, { params });
  },

  getEvidenceAnalytics: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.EVIDENCE, { params });
  },

  getIntegrityAnalytics: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.INTEGRITY, { params });
  },

  getCustodyAnalytics: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.CUSTODY, { params });
  },

  getAuditAnalytics: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.AUDIT, { params });
  },

  getRecentActivities: async () => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.RECENT_ACTIVITIES);
  },

  getSystemHealth: async () => {
    return await apiClient.get(API_ENDPOINTS.DASHBOARD.SYSTEM_HEALTH);
  },
};

export default dashboardService;
