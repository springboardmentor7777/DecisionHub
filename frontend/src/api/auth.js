import apiClient, { handleAxiosError } from './client';

/**
 * Authentication API Service
 *
 * The Spring Boot backend returns roles as USER / MODERATOR / ADMIN and a flat
 * auth response: { token, id, email, displayName, role }.
 * The frontend works with { token, user: {...} } and its own role names
 * ('user', 'communityModerator', 'administrator'), so we convert at this boundary.
 */

const ROLE_MAP = {
  USER: 'user',
  MODERATOR: 'communityModerator',
  ADMIN: 'administrator',
};

export const normalizeRole = (role) => ROLE_MAP[String(role || '').toUpperCase()] || 'user';

const toUser = (data) => ({
  id: data.id,
  email: data.email,
  displayName: data.displayName,
  role: normalizeRole(data.role),
  avatarUrl: data.avatarUrl || null,
});

const toSession = (data) => ({ token: data.token, user: toUser(data) });

export const authApi = {
  /** POST /api/auth/login -> { token, user } */
  async login({ email, password }) {
    try {
      const response = await apiClient.post('/api/auth/login', { email, password });
      return toSession(response.data);
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /**
   * POST /api/auth/register -> { token, user }
   * Only displayName, email and password are sent. The backend always creates a
   * standard user; elevated roles are granted separately.
   */
  async register({ displayName, email, password }) {
    try {
      const response = await apiClient.post('/api/auth/register', {
        displayName,
        email,
        password,
      });
      return toSession(response.data);
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  /** GET /api/users/me -> user */
  async getProfile() {
    try {
      const response = await apiClient.get('/api/users/me');
      return toUser(response.data);
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  // The backend does not provide these endpoints yet.
  async forgotPassword(email) {
    try {
      const response = await apiClient.post('/api/auth/forgot-password', { email });
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },

  async resetPassword(token, newPassword) {
    try {
      const response = await apiClient.post('/api/auth/reset-password', { token, newPassword });
      return response.data;
    } catch (error) {
      throw new Error(handleAxiosError(error));
    }
  },
};

export default authApi;
