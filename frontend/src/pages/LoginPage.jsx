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
      errors.email = 'Email address is required.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Please provide a valid email format (e.g. name@domain.com).';
    }

    if (!formData.password) {
      errors.password = 'Password is required.';
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
      subtitle="Sign in to your decision boards and community votes"
      footer={
        <p>
          Don&apos;t have an account yet?{' '}
          <Link
            to="/register"
            className="font-semibold text-brand-600 hover:text-brand-700 underline underline-offset-2"
          >
            Create an account
          </Link>
        </p>
      }
    >
      {authError && (
        <Alert
          type="error"
          title="Authentication Failed"
          className="mb-4"
          onClose={() => setAuthError && setAuthError(null)}
        >
          {authError}
        </Alert>
      )}

      {/* Social Login Button */}
      <div className="mb-5">
        <SocialAuth dividerText="Or sign in with email" />
      </div>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {/* Email Input */}
        <Input
          label="Email Address"
          id="login-email"
          name="email"
          type="email"
          placeholder="you@domain.com"
          autoComplete="email"
          value={formData.email}
          onChange={(e) => {
            setFormData({ ...formData, email: e.target.value });
            if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: null });
          }}
          error={fieldErrors.email}
          required
        />

        {/* Password Input */}
        <div>
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
        </div>

        {/* Remember Me & Forgot Password */}
        <div className="flex items-center justify-between pt-1 text-sm">
          <Checkbox
            id="remember-me"
            name="rememberMe"
            label="Remember me"
            checked={formData.rememberMe}
            onChange={(e) =>
              setFormData({ ...formData, rememberMe: e.target.checked })
            }
          />

          <Link
            to="/forgot-password"
            className="text-xs font-medium text-brand-600 hover:text-brand-700 hover:underline"
          >
            Forgot password?
          </Link>
        </div>

        {/* Submit Button */}
        <div className="pt-2">
          <Button
            type="submit"
            variant="primary"
            size="md"
            fullWidth
            loading={isSubmitting}
          >
            Sign in to DecisionHub
          </Button>
        </div>
      </form>
    </AuthCard>
  );
};

export default LoginPage;
