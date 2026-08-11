import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const custodyService = {
  initiateTransfer: async (transferData) => {
    return await apiClient.post(API_ENDPOINTS.CUSTODY.TRANSFER, transferData);
  },

  acceptOrRejectTransfer: async (id, acceptData) => {
    return await apiClient.post(API_ENDPOINTS.CUSTODY.ACCEPT(id), acceptData);
  },

  getCustodyById: async (id) => {
    return await apiClient.get(API_ENDPOINTS.CUSTODY.BY_ID(id));
  },

  getCustodyByEvidenceId: async (evidenceId, params = {}) => {
    return await apiClient.get(API_ENDPOINTS.CUSTODY.BY_EVIDENCE(evidenceId), { params });
  },

  getCustodyTimeline: async (evidenceId) => {
    return await apiClient.get(API_ENDPOINTS.CUSTODY.HISTORY(evidenceId));
  },

  searchCustodyRecords: async (searchParams = {}) => {
    return await apiClient.get(API_ENDPOINTS.CUSTODY.SEARCH, { params: searchParams });
  },
};

export default custodyService;
