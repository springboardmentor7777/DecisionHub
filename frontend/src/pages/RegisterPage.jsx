import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import AuthCard from '../components/auth/AuthCard';
import Input from '../components/common/Input';
import PasswordInput from '../components/auth/PasswordInput';
import Checkbox from '../components/common/Checkbox';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import SocialAuth from '../components/auth/SocialAuth';
import { isPasswordAcceptable, MIN_PASSWORD_LENGTH } from '../components/auth/PasswordStrengthMeter';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, isSubmitting, authError, setAuthError } = useAuth();

  const [formData, setFormData] = useState({
    displayName: '',
    email: '',
    role: 'user', // 'user' or 'communityModerator'
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  });

  const [fieldErrors, setFieldErrors] = useState({});

  const validate = () => {
    const errors = {};

    if (!formData.displayName.trim()) {
      errors.displayName = 'Full name is required.';
    } else if (formData.displayName.trim().length < 2) {
      errors.displayName = 'Name must be at least 2 characters.';
    }

    if (!formData.email.trim()) {
      errors.email = 'Email address is required.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Please provide a valid email format (e.g. name@domain.com).';
    }

    if (!formData.password) {
      errors.password = 'Password is required.';
    } else {
      if (formData.password.length < MIN_PASSWORD_LENGTH) {
        errors.password = `Password must be at least ${MIN_PASSWORD_LENGTH} characters long.`;
      } else if (!isPasswordAcceptable(formData.password)) {
        errors.password =
          'Password is too weak. Use a mix of uppercase, lowercase, numbers, and symbols.';
      }
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Please confirm your password.';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match.';
    }

    if (!formData.acceptTerms) {
      errors.acceptTerms = 'You must accept the terms of service to create an account.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await register({
        displayName: formData.displayName,
        email: formData.email,
        role: formData.role,
        password: formData.password,
      });
      navigate('/dashboard');
    } catch (err) {
      // Error is set in AuthContext
    }
  };

  return (
    <AuthCard
      title="Create your account"
      subtitle="Join DecisionHub to compare options and build collective intelligence"
      footer={
        <p>
          Already have an account?{' '}
          <Link
            to="/"
            className="font-semibold text-brand-600 hover:text-brand-700 underline underline-offset-2"
          >
            Sign in
          </Link>
        </p>
      }
    >
      {authError && (
        <Alert
          type="error"
          title="Registration Failed"
          className="mb-4"
          onClose={() => setAuthError && setAuthError(null)}
        >
          {authError}
        </Alert>
      )}

      {/* Social Registration */}
      <div className="mb-5">
        <SocialAuth dividerText="Or sign up with email" />
      </div>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {/* Full Name */}
        <Input
          label="Full Name"
          id="register-name"
          name="displayName"
          type="text"
          placeholder="e.g. Alex Morgan"
          value={formData.displayName}
          onChange={(e) => {
            setFormData({ ...formData, displayName: e.target.value });
            if (fieldErrors.displayName) setFieldErrors({ ...fieldErrors, displayName: null });
          }}
          error={fieldErrors.displayName}
          required
        />

        {/* Email Address */}
        <Input
          label="Email Address"
          id="register-email"
          name="email"
          type="email"
          placeholder="alex@domain.com"
          autoComplete="email"
          value={formData.email}
          onChange={(e) => {
            setFormData({ ...formData, email: e.target.value });
            if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: null });
          }}
          error={fieldErrors.email}
          required
        />

        {/* Role Selector (Reflecting DecisionHub spec) */}
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1.5">
            Account Type
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setFormData({ ...formData, role: 'user' })}
              className={`p-2.5 rounded-lg border text-left transition-all ${
                formData.role === 'user'
                  ? 'border-brand-500 bg-brand-50/50 text-brand-900 ring-1 ring-brand-500'
                  : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
              }`}
            >
              <div className="text-xs font-semibold">Standard User</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Vote & create boards</div>
            </button>

            <button
              type="button"
              onClick={() => setFormData({ ...formData, role: 'communityModerator' })}
              className={`p-2.5 rounded-lg border text-left transition-all ${
                formData.role === 'communityModerator'
                  ? 'border-brand-500 bg-brand-50/50 text-brand-900 ring-1 ring-brand-500'
                  : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
              }`}
            >
              <div className="text-xs font-semibold">Community Moderator</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Moderate & pin advice</div>
            </button>
          </div>
        </div>

        {/* Password Input with Real-time Strength Meter */}
        <PasswordInput
          label="Create Password"
          id="register-password"
          name="password"
          autoComplete="new-password"
          value={formData.password}
          onChange={(e) => {
            setFormData({ ...formData, password: e.target.value });
            if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: null });
          }}
          showStrength={true}
          error={fieldErrors.password}
          required
        />

        {/* Confirm Password */}
        <PasswordInput
          label="Confirm Password"
          id="register-confirm-password"
          name="confirmPassword"
          autoComplete="new-password"
          value={formData.confirmPassword}
          onChange={(e) => {
            setFormData({ ...formData, confirmPassword: e.target.value });
            if (fieldErrors.confirmPassword)
              setFieldErrors({ ...fieldErrors, confirmPassword: null });
          }}
          error={fieldErrors.confirmPassword}
          required
        />

        {/* Terms and Conditions Checkbox */}
        <div className="pt-1">
          <Checkbox
            id="register-terms"
            name="acceptTerms"
            checked={formData.acceptTerms}
            onChange={(e) => {
              setFormData({ ...formData, acceptTerms: e.target.checked });
              if (fieldErrors.acceptTerms) setFieldErrors({ ...fieldErrors, acceptTerms: null });
            }}
            label={
              <span>
                I agree to the{' '}
                <a href="#terms" className="text-brand-600 hover:underline">
                  Terms of Service
                </a>{' '}
                and{' '}
                <a href="#privacy" className="text-brand-600 hover:underline">
                  Privacy Policy
                </a>
              </span>
            }
            error={fieldErrors.acceptTerms}
          />
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
            Create DecisionHub Account
          </Button>
        </div>
      </form>
    </AuthCard>
  );
};

export default RegisterPage;
