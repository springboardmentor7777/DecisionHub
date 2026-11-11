import apiClient, { handleAxiosError } from './client';

/**
 * Authentication API Service
 * Talks directly to the Spring Boot backend.
 */

export const authApi = {
  /**
   * Log in user with email & password
   * Expected response: { token, user: { id, email, displayName, role, avatarUrl } }
   */
  async login(credentials) {
    try {
      const response = await apiClient.post('/api/auth/login', credentials);
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /**
   * Register a new user
   * Expected response: { token, user: { ... } }
   */
  async register(userData) {
    try {
      const response = await apiClient.post('/api/auth/register', userData);
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /**
   * Request password reset email
   */
  async forgotPassword(email) {
    try {
      const response = await apiClient.post('/api/auth/forgot-password', { email });
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /**
   * Set new password with reset token
   */
  async resetPassword(token, newPassword) {
    try {
      const response = await apiClient.post('/api/auth/reset-password', {
        token,
        newPassword,
      });
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /**
   * Get current authenticated user profile
   */
  async getProfile() {
    try {
      const response = await apiClient.get('/api/auth/me');
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },
};

export default authApi;
