
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

// Public route wrapper
const PublicOnlyRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) return null;

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <AuthLayout>{children}</AuthLayout>;
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

      {/* Dashboard Preview Routes */}
      <Route
        path="/dashboard"
        element={<DashboardPage role="user" />}
      />

      <Route
        path="/dashboard/user"
        element={<DashboardPage role="user" />}
      />

      <Route
        path="/dashboard/admin"
        element={<DashboardPage role="admin" />}
      />

      <Route
        path="/dashboard/moderator"
        element={<DashboardPage role="moderator" />}
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
