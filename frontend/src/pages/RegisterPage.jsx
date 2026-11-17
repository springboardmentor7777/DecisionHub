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
import { isPasswordAcceptable, MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH } from '../components/auth/PasswordStrengthMeter';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, isSubmitting, authError, setAuthError } = useAuth();

  const [formData, setFormData] = useState({
    displayName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  });

  const [fieldErrors, setFieldErrors] = useState({});

  const validate = () => {
    const errors = {};

    if (!formData.displayName.trim()) {
      errors.displayName = 'Enter your full name.';
    } else if (formData.displayName.trim().length < 2) {
      errors.displayName = 'Your name needs at least 2 characters.';
    } else if (formData.displayName.trim().length > 80) {
      errors.displayName = 'Your name can be at most 80 characters.';
    }

    if (!formData.email.trim()) {
      errors.email = 'Enter your email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Enter a valid email address, like name@example.com.';
    }

    if (!formData.password) {
      errors.password = 'Create a password.';
    } else {
      if (formData.password.length < MIN_PASSWORD_LENGTH) {
        errors.password = `Use at least ${MIN_PASSWORD_LENGTH} characters.`;
      } else if (formData.password.length > MAX_PASSWORD_LENGTH) {
        errors.password = `Use at most ${MAX_PASSWORD_LENGTH} characters.`;
      } else if (!isPasswordAcceptable(formData.password)) {
        errors.password =
          'Too weak. Mix upper and lower case letters with numbers or symbols.';
      }
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Enter your password again.';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = "The passwords don't match.";
    }

    if (!formData.acceptTerms) {
      errors.acceptTerms = 'Accept the terms to create your account.';
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
      subtitle="It takes a minute. You can start a board right after."
      footer={
        <p>
          Already have an account?{' '}
          <Link
            to="/"
            className="font-bold text-brand-700 underline-offset-4 hover:underline"
          >
            Sign in
          </Link>
        </p>
      }
    >
      {authError && (
        <Alert
          type="error"
          title="Couldn't create your account"
          className="mb-5"
          onClose={() => setAuthError && setAuthError(null)}
        >
          {authError}
        </Alert>
      )}

      <SocialAuth dividerText="or sign up with email" />

      <form onSubmit={handleSubmit} className="space-y-5" noValidate>
        <Input
          label="Full name"
          id="register-name"
          name="displayName"
          type="text"
          placeholder="Alex Morgan"
          autoComplete="name"
          maxLength={80}
          value={formData.displayName}
          onChange={(e) => {
            setFormData({ ...formData, displayName: e.target.value });
            if (fieldErrors.displayName) setFieldErrors({ ...fieldErrors, displayName: null });
          }}
          error={fieldErrors.displayName}
          required
        />

        <Input
          label="Email"
          id="register-email"
          name="email"
          type="email"
          placeholder="alex@example.com"
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
          id="register-password"
          name="password"
          autoComplete="new-password"
          maxLength={MAX_PASSWORD_LENGTH}
          value={formData.password}
          onChange={(e) => {
            setFormData({ ...formData, password: e.target.value });
            if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: null });
          }}
          showStrength={true}
          error={fieldErrors.password}
          required
        />

        <PasswordInput
          label="Confirm password"
          id="register-confirm-password"
          name="confirmPassword"
          autoComplete="new-password"
          maxLength={MAX_PASSWORD_LENGTH}
          value={formData.confirmPassword}
          onChange={(e) => {
            setFormData({ ...formData, confirmPassword: e.target.value });
            if (fieldErrors.confirmPassword)
              setFieldErrors({ ...fieldErrors, confirmPassword: null });
          }}
          error={fieldErrors.confirmPassword}
          required
        />

        <Checkbox
          id="register-terms"
          name="acceptTerms"
          checked={formData.acceptTerms}
          onChange={(e) => {
            setFormData({ ...formData, acceptTerms: e.target.checked });
            if (fieldErrors.acceptTerms) setFieldErrors({ ...fieldErrors, acceptTerms: null });
          }}
          label="I agree to the Terms of Service and Privacy Policy"
          error={fieldErrors.acceptTerms}
        />

        <Button type="submit" variant="primary" size="md" fullWidth loading={isSubmitting}>
          Create account
        </Button>
      </form>
    </AuthCard>
  );
};

export default RegisterPage;
