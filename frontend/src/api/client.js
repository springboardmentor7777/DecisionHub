import axios from 'axios';

/**
 * DecisionHub Central Axios Client
 * Configured with baseURL, request interceptors for JWT, and standard error handling.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request Interceptor: Attach JWT Bearer Token if present
apiClient.interceptors.request.use(
  (config) => {
    // Check localStorage (Remember Me) first, then sessionStorage
    const token = localStorage.getItem('dh_token') || sessionStorage.getItem('dh_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Global error formatter and 401 handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // A 401 from the login/register endpoints just means bad credentials,
    // not an expired session, so don't treat it as a forced logout.
    const requestUrl = error.config?.url || '';
    const isCredentialRequest =
      requestUrl.includes('/api/auth/login') || requestUrl.includes('/api/auth/register');

    if (error.response?.status === 401 && !isCredentialRequest) {
      // Clear expired / invalid tokens
      localStorage.removeItem('dh_token');
      localStorage.removeItem('dh_user');
      sessionStorage.removeItem('dh_token');
      sessionStorage.removeItem('dh_user');
      
      // Dispatch custom event to let AuthContext react if needed
      window.dispatchEvent(new CustomEvent('dh:auth_unauthorized'));
    }
    return Promise.reject(error);
  }
);

/**
 * Standard error extractor
 */
export const handleAxiosError = (error) => {
  if (error.response) {
    // Server responded with an error status
    const data = error.response.data;
    if (data?.message) return data.message;
    if (typeof data === 'string') return data;
    if (data?.errors) {
      const firstKey = Object.keys(data.errors)[0];
      return data.errors[firstKey];
    }
    return `Server error (${error.response.status}). Please try again.`;
  }
  if (error.request) {
    return 'Unable to connect to DecisionHub server. Please check your network or try again.';
  }
  return error.message || 'An unexpected error occurred.';
};

export default apiClient;
