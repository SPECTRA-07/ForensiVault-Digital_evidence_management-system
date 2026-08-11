import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const caseService = {
  createCase: async (requestData) => {
    return await apiClient.post(API_ENDPOINTS.CASES.BASE, requestData);
  },

  getAllCases: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.CASES.BASE, { params });
  },

  getMyAssignedCases: async (params = {}) => {
    return await apiClient.get(API_ENDPOINTS.CASES.MY_CASES, { params });
  },

  getCaseById: async (id) => {
    return await apiClient.get(API_ENDPOINTS.CASES.BY_ID(id));
  },

  updateCase: async (id, requestData) => {
    return await apiClient.put(API_ENDPOINTS.CASES.BY_ID(id), requestData);
  },

  updateCaseStatus: async (id, statusData) => {
    return await apiClient.patch(API_ENDPOINTS.CASES.STATUS(id), statusData);
  },

  assignOfficer: async (id, assignData) => {
    return await apiClient.patch(API_ENDPOINTS.CASES.ASSIGN_OFFICER(id), assignData);
  },

  searchCases: async (searchParams = {}) => {
    return await apiClient.get(API_ENDPOINTS.CASES.SEARCH, { params: searchParams });
  },
};

export default caseService;
