import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import AuthLayout from '../layout/AuthLayout';

import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import DashboardPage from '../pages/DashboardPage';
import OAuth2RedirectPage from '../pages/OAuth2RedirectPage';
import RequireAuth from './RequireAuth';

// Public route wrapper
const PublicOnlyRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) return null;

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <AuthLayout>{children}</AuthLayout>;
};

/**
 * /dashboard opens the dashboard that matches the logged-in account's role.
 * (Stored roles: 'administrator', 'communityModerator', 'user'.)
 */
const DashboardIndex = () => {
  const { role } = useAuth();

  if (role === 'administrator') return <DashboardPage role="admin" />;
  if (role === 'communityModerator') return <DashboardPage role="moderator" />;
  return <DashboardPage role="user" />;
};

export const AppRoutes = () => {
  return (
    <Routes>

      {/* Login */}
      <Route
        path="/"
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />

      <Route
        path="/login"
        element={<Navigate to="/" replace />}
      />

      {/* Registration */}
      <Route
        path="/register"
        element={
          <PublicOnlyRoute>
            <RegisterPage />
          </PublicOnlyRoute>
        }
      />

      {/* Forgot Password */}
      <Route
        path="/forgot-password"
        element={
          <PublicOnlyRoute>
            <ForgotPasswordPage />
          </PublicOnlyRoute>
        }
      />

      {/* Reset Password */}
      <Route
        path="/reset-password"
        element={
          <PublicOnlyRoute>
            <ResetPasswordPage />
          </PublicOnlyRoute>
        }
      />

      {/* Google OAuth */}
      <Route
        path="/oauth2/redirect"
        element={<OAuth2RedirectPage />}
      />

      {/* Dashboards: login required, and the role decides what you can open */}
      <Route
        path="/dashboard"
        element={
          <RequireAuth>
            <DashboardIndex />
          </RequireAuth>
        }
      />

      <Route
        path="/dashboard/user"
        element={
          <RequireAuth>
            <DashboardPage role="user" />
          </RequireAuth>
        }
      />

      <Route
        path="/dashboard/admin"
        element={
          <RequireAuth allowedRoles={['administrator']}>
            <DashboardPage role="admin" />
          </RequireAuth>
        }
      />

      <Route
        path="/dashboard/moderator"
        element={
          <RequireAuth allowedRoles={['communityModerator', 'administrator']}>
            <DashboardPage role="moderator" />
          </RequireAuth>
        }
      />

      {/* Fallback */}
      <Route
        path="*"
        element={<Navigate to="/dashboard" replace />}
      />

    </Routes>
  );
};

export default AppRoutes;
