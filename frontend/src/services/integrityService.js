import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const integrityService = {
  verifyEvidenceIntegrity: async (id) => {
    return await apiClient.post(API_ENDPOINTS.EVIDENCE.INTEGRITY.VERIFY(id));
  },

  getStoredIntegrityInfo: async (id) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.INTEGRITY.STORED(id));
  },

  getIntegritySummaryReport: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.INTEGRITY.REPORT, { params });
  },
};

export default integrityService;
