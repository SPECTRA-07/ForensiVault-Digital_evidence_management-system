import React, { createContext, useState, useEffect, useCallback } from 'react';
import authService from '../services/authService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from local storage on startup
  useEffect(() => {
    try {
      const storedToken = localStorage.getItem('dems_auth_token');
      const storedUser = localStorage.getItem('dems_user_info');

      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      }
    } catch (error) {
      console.warn('Failed to restore authentication state from storage:', error);
      localStorage.removeItem('dems_auth_token');
      localStorage.removeItem('dems_user_info');
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Perform user authentication against backend AuthController (/auth/login).
   * @param {string} email
   * @param {string} password
   */
  const login = async (email, password) => {
    const response = await authService.login({ email, password });
    
    // Spring Boot response format: ApiResponse<LoginResponse> => response.data contains LoginResponse payload
    if (response && response.data && response.data.token) {
      const loginData = response.data;
      const { token: jwtToken, ...userInfo } = loginData;

      setToken(jwtToken);
      setUser(userInfo);

      localStorage.setItem('dems_auth_token', jwtToken);
      localStorage.setItem('dems_user_info', JSON.stringify(userInfo));

      return response;
    } else {
      throw new Error(response?.message || 'Authentication failed: Invalid response format from server.');
    }
  };

  /**
   * Clear session state and remove stored tokens.
   */
  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    authService.logout();
  }, []);

  /**
   * Check if current user has a specific security role.
   * Handles roles with or without 'ROLE_' prefix.
   */
  const hasRole = useCallback((requiredRole) => {
    if (!user || !user.role) return false;
    const cleanRequired = requiredRole.replace(/^ROLE_/, '').toUpperCase();
    const cleanUserRole = user.role.replace(/^ROLE_/, '').toUpperCase();
    return cleanUserRole === cleanRequired;
  }, [user]);

  /**
   * Check if current user has any of the specified security roles.
   */
  const hasAnyRole = useCallback((roles = []) => {
    if (!roles || roles.length === 0) return true;
    return roles.some((role) => hasRole(role));
  }, [hasRole]);

  const value = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    login,
    logout,
    hasRole,
    hasAnyRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
