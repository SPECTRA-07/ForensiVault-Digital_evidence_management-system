import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const evidenceService = {
  uploadEvidence: async (formData) => {
    return await apiClient.post(API_ENDPOINTS.EVIDENCE.UPLOAD, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  getAllEvidence: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.BASE, { params });
  },

  getEvidenceById: async (id) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.BY_ID(id));
  },

  downloadEvidence: async (id) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.DOWNLOAD(id), {
      responseType: 'blob',
    });
  },

  updateEvidence: async (id, requestData) => {
    return await apiClient.put(API_ENDPOINTS.EVIDENCE.BY_ID(id), requestData);
  },

  updateEvidenceStatus: async (id, statusData) => {
    return await apiClient.patch(API_ENDPOINTS.EVIDENCE.STATUS(id), statusData);
  },

  getEvidenceByCaseId: async (caseId, params = {}) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.BY_CASE(caseId), { params });
  },

  searchEvidence: async (searchParams = {}) => {
    return await apiClient.get(API_ENDPOINTS.EVIDENCE.SEARCH, { params: searchParams });
  },
};

export default evidenceService;
