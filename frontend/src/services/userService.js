import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const userService = {
  createUser: async (userData) => {
    return await apiClient.post(API_ENDPOINTS.USERS.BASE, userData);
  },

  getAllUsers: async () => {
    return await apiClient.get(API_ENDPOINTS.USERS.BASE);
  },

  getUserById: async (id) => {
    return await apiClient.get(API_ENDPOINTS.USERS.BY_ID(id));
  },

  updateUser: async (id, userData) => {
    return await apiClient.put(API_ENDPOINTS.USERS.BY_ID(id), userData);
  },

  setUserStatus: async (id, active) => {
    return await apiClient.patch(API_ENDPOINTS.USERS.STATUS(id), null, {
      params: { active },
    });
  },
};

export default userService;
