import React, { createContext, useState, useEffect, useCallback } from 'react';
import authApi from '../api/auth';

export const AuthContext = createContext(null);

const STORAGE_KEYS = {
  TOKEN: 'dh_token',
  USER: 'dh_user',
  REMEMBER: 'dh_remember',
};

/**
 * Returns true if the JWT is malformed or past its `exp` claim.
 * (Client-side check only; the backend remains the source of truth.)
 */
const isTokenExpired = (jwt) => {
  try {
    const payload = JSON.parse(atob(jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    if (!payload.exp) return false;
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  // isLoading: true only while restoring a saved session on first page load.
  // Routes use this to decide when it is safe to render or redirect.
  const [isLoading, setIsLoading] = useState(true);

  // isSubmitting: true while a login/register request is in flight.
  // Forms use this for the button spinner; it must NOT unmount the page.
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [authError, setAuthError] = useState(null);

  /**
   * Helper to persist or clear session tokens
   */
  const persistSession = (authToken, authUser, rememberMe = true) => {
    const storage = rememberMe ? localStorage : sessionStorage;
    const alternate = rememberMe ? sessionStorage : localStorage;

    // Clear alternate storage to avoid stale sync
    alternate.removeItem(STORAGE_KEYS.TOKEN);
    alternate.removeItem(STORAGE_KEYS.USER);

    if (authToken && authUser) {
      storage.setItem(STORAGE_KEYS.TOKEN, authToken);
      storage.setItem(STORAGE_KEYS.USER, JSON.stringify(authUser));
      if (rememberMe) {
        localStorage.setItem(STORAGE_KEYS.REMEMBER, 'true');
      } else {
        localStorage.removeItem(STORAGE_KEYS.REMEMBER);
      }
    } else {
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      localStorage.removeItem(STORAGE_KEYS.REMEMBER);
      sessionStorage.removeItem(STORAGE_KEYS.TOKEN);
      sessionStorage.removeItem(STORAGE_KEYS.USER);
    }
  };

  /**
   * Boot-time rehydration
   */
  useEffect(() => {
    try {
      const storedToken =
        localStorage.getItem(STORAGE_KEYS.TOKEN) ||
        sessionStorage.getItem(STORAGE_KEYS.TOKEN);
      const storedUser =
        localStorage.getItem(STORAGE_KEYS.USER) ||
        sessionStorage.getItem(STORAGE_KEYS.USER);

      if (storedToken && storedUser) {
        if (isTokenExpired(storedToken)) {
          // Saved session has expired: discard it
          persistSession(null, null);
        } else {
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
        }
      }
    } catch (err) {
      console.error('Failed to restore auth session:', err);
      persistSession(null, null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Listen for unauthorized 401 events from Axios interceptor
   */
  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setToken(null);
      setAuthError('Your session has expired. Please log in again.');
    };

    window.addEventListener('dh:auth_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('dh:auth_unauthorized', handleUnauthorized);
  }, []);

  /**
   * Log In
   */
  const login = async (email, password, rememberMe = true) => {
    setIsSubmitting(true);
    setAuthError(null);
    try {
      const result = await authApi.login({ email: email.trim(), password });
      persistSession(result.token, result.user, rememberMe);
      setToken(result.token);
      setUser(result.user);
      return result;
    } catch (err) {
      setAuthError(err.message || 'Login failed. Please check your credentials.');
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Register (logs the user in immediately if the backend returns a token)
   */
  const register = async (userData, rememberMe = true) => {
    setIsSubmitting(true);
    setAuthError(null);
    try {
      const result = await authApi.register({
        ...userData,
        displayName: userData.displayName.trim(),
        email: userData.email.trim(),
      });
      persistSession(result.token, result.user, rememberMe);
      setToken(result.token);
      setUser(result.user);
      return result;
    } catch (err) {
      setAuthError(err.message || 'Registration failed.');
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Complete a Google (OAuth2) sign-in.
   * The backend redirects to /oauth2/redirect?token=<jwt>; we store the token,
   * then load the user profile from /api/auth/me.
   */
  const loginWithToken = async (oauthToken, rememberMe = true) => {
    setAuthError(null);
    // Token must be in storage first so the Axios interceptor can attach it
    localStorage.setItem(STORAGE_KEYS.TOKEN, oauthToken);
    try {
      const profile = await authApi.getProfile();
      persistSession(oauthToken, profile, rememberMe);
      setToken(oauthToken);
      setUser(profile);
      return profile;
    } catch (err) {
      persistSession(null, null);
      setAuthError(err.message || 'Google sign-in failed. Please try again.');
      throw err;
    }
  };

  /**
   * Log Out
   */
  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    setAuthError(null);
    persistSession(null, null);
  }, []);

  /**
   * Forgot Password Request
   */
  const forgotPassword = async (email) => {
    return await authApi.forgotPassword(email);
  };

  /**
   * Reset Password
   */
  const resetPassword = async (resetToken, newPassword) => {
    return await authApi.resetPassword(resetToken, newPassword);
  };

  const value = {
    user,
    token,
    role: user?.role || null,
    isAuthenticated: !!token && !!user,
    isLoading,
    isSubmitting,
    authError,
    setAuthError,
    login,
    register,
    loginWithToken,
    logout,
    forgotPassword,
    resetPassword,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
