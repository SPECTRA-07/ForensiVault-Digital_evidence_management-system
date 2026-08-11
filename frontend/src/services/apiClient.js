import axios from 'axios';
import { handleApiError } from '../utils/errorHandler';

// Read API base URL strictly from Vite environment variable
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

/**
 * Request Interceptor:
 * Attaches Authorization: Bearer <JWT> header dynamically if token exists.
 * Does not expose raw tokens in client logs.
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('dems_auth_token');
    // Attach token if present and not calling public login endpoint
    if (token && !config.url?.endsWith('/auth/login')) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor:
 * Converts Blob error bodies to JSON objects before passing to handleApiError.
 * Handles 401 (Unauthorized) by clearing token and redirecting to /login (safely preventing loops).
 * Handles 403 (Forbidden) without logging the user out.
 */
apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    // If error payload is wrapped in a Blob (from responseType: 'blob' requests), convert blob to JSON
    if (error.response && error.response.data instanceof Blob) {
      try {
        const text = await error.response.data.text();
        if (text) {
          error.response.data = JSON.parse(text);
        }
      } catch (e) {
        // Blob content was not a valid JSON string
      }
    }

    const sanitizedError = handleApiError(error);

    // Handle 401 Unauthorized - Clear session and redirect to /login if not already there
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('dems_auth_token');
      localStorage.removeItem('dems_user_info');

      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // 403 Forbidden - Do NOT log out user, just pass error through for component handling
    return Promise.reject(sanitizedError);
  }
);

export default apiClient;
