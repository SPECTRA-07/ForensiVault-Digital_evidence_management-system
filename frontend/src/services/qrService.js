import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const qrService = {
  getQRCodeInfo: async (evidenceId) => {
    return await apiClient.get(API_ENDPOINTS.QR.INFO(evidenceId));
  },

  getQRCodeImage: async (evidenceId) => {
    return await apiClient.get(API_ENDPOINTS.QR.IMAGE(evidenceId), {
      responseType: 'blob',
    });
  },

  regenerateQRCode: async (evidenceId) => {
    return await apiClient.post(API_ENDPOINTS.QR.REGENERATE(evidenceId));
  },

  resolveQRCode: async (evidenceNumber) => {
    return await apiClient.get(API_ENDPOINTS.QR.RESOLVE(evidenceNumber));
  },
};

export default qrService;
