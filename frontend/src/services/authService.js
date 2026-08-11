import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const authService = {
  /**
   * Authenticate user credentials with backend AuthController.
   * @param {Object} credentials { email, password }
   */
  login: async (credentials) => {
    return await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
  },

  /**
   * Clear local storage token and user details.
   */
  logout: () => {
    localStorage.removeItem('dems_auth_token');
    localStorage.removeItem('dems_user_info');
  },

  /**
   * Retrieve stored JWT token.
   */
  getToken: () => {
    return localStorage.getItem('dems_auth_token');
  },

  /**
   * Retrieve stored user details payload.
   */
  getUserInfo: () => {
    try {
      const info = localStorage.getItem('dems_user_info');
      return info ? JSON.parse(info) : null;
    } catch (e) {
      return null;
    }
  },
};

export default authService;
