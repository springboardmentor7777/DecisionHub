import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import AuthCard from '../components/auth/AuthCard';
import Input from '../components/common/Input';
import PasswordInput from '../components/auth/PasswordInput';
import Checkbox from '../components/common/Checkbox';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import SocialAuth from '../components/auth/SocialAuth';

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isSubmitting, authError, setAuthError } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: true,
  });

  const [fieldErrors, setFieldErrors] = useState({});

  // Where to redirect after successful login
  const from = location.state?.from?.pathname || '/dashboard';

  const validate = () => {
    const errors = {};
    if (!formData.email.trim()) {
      errors.email = 'Enter your email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Enter a valid email address, like name@example.com.';
    }

    if (!formData.password) {
      errors.password = 'Enter your password.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await login(formData.email, formData.password, formData.rememberMe);
      navigate(from, { replace: true });
    } catch (err) {
      // Error handled by AuthContext state (authError)
    }
  };

  return (
    <AuthCard
      title="Welcome back"
      subtitle="Sign in to see your boards and the votes that came in."
      footer={
        <p>
          New to DecisionHub?{' '}
          <Link
            to="/register"
            className="font-bold text-brand-700 underline-offset-4 hover:underline"
          >
            Create an account
          </Link>
        </p>
      }
    >
      {authError && (
        <Alert
          type="error"
          title="Couldn't sign you in"
          className="mb-5"
          onClose={() => setAuthError && setAuthError(null)}
        >
          {authError}
        </Alert>
      )}

      <SocialAuth dividerText="or sign in with email" />

      <form onSubmit={handleSubmit} className="space-y-5" noValidate>
        <Input
          label="Email"
          id="login-email"
          name="email"
          type="email"
          placeholder="you@example.com"
          autoComplete="email"
          maxLength={254}
          value={formData.email}
          onChange={(e) => {
            setFormData({ ...formData, email: e.target.value });
            if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: null });
          }}
          error={fieldErrors.email}
          required
        />

        <PasswordInput
          label="Password"
          id="login-password"
          name="password"
          autoComplete="current-password"
          value={formData.password}
          onChange={(e) => {
            setFormData({ ...formData, password: e.target.value });
            if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: null });
          }}
          error={fieldErrors.password}
          required
        />

        <div className="flex items-center justify-between text-sm">
          <Checkbox
            id="remember-me"
            name="rememberMe"
            label="Keep me signed in"
            checked={formData.rememberMe}
            onChange={(e) => setFormData({ ...formData, rememberMe: e.target.checked })}
          />

          <Link
            to="/forgot-password"
            className="text-sm font-semibold text-brand-700 underline-offset-4 hover:underline"
          >
            Forgot password?
          </Link>
        </div>

        <Button type="submit" variant="primary" size="md" fullWidth loading={isSubmitting}>
          Sign in
        </Button>
      </form>
    </AuthCard>
  );
};

export default LoginPage;
