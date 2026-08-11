/**
 * Centralized DEMS API Error Handler
 * Sanitizes backend exception responses and maps HTTP status codes to user-friendly messages.
 * Preserves specific backend exception messages verbatim without masking them behind generic defaults.
 */
export const handleApiError = (error) => {
  if (!error.response) {
    if (error.request) {
      return {
        status: 0,
        message: 'Unable to connect to ForensiVault backend server. Please verify backend service availability.',
        error: 'Network Error',
      };
    }
    return {
      status: 500,
      message: error.message || 'An unexpected client-side error occurred.',
      error: 'Client Error',
    };
  }

  const { status, data } = error.response;

  // Robust backend message extraction across ErrorResponse, ApiResponse, and stringified JSON
  const extractBackendMessage = (payload) => {
    if (!payload) return null;
    if (typeof payload === 'string') {
      try {
        const parsed = JSON.parse(payload);
        return parsed.message || parsed.error || payload;
      } catch (e) {
        return payload;
      }
    }
    return payload.message || payload.error || payload.detail || null;
  };

  const backendMessage = extractBackendMessage(data);
  const validationErrors = data?.validationErrors || null;

  switch (status) {
    case 400:
      return {
        status,
        message: backendMessage || 'Invalid request parameter or payload validation failed.',
        validationErrors,
        error: 'Bad Request',
      };
    case 401:
      return {
        status,
        message: backendMessage || 'Authentication session expired or invalid credentials.',
        error: 'Unauthorized',
      };
    case 403:
      return {
        status,
        message: backendMessage || 'Access denied: You do not possess the required security role for this operation.',
        error: 'Forbidden',
      };
    case 404:
      return {
        status,
        message: backendMessage || 'The requested evidence or system record was not found.',
        error: 'Not Found',
      };
    case 409:
      return {
        status,
        message: backendMessage || 'State conflict encountered. Record may already exist or status transition is invalid.',
        error: 'Conflict',
      };
    case 500:
      return {
        status,
        message: backendMessage || 'Internal server error processing forensic request. Operation recorded in audit trail.',
        error: 'Internal Server Error',
      };
    default:
      return {
        status,
        message: backendMessage || `Request failed with HTTP status ${status}.`,
        error: data?.error || 'HTTP Error',
      };
  }
};
