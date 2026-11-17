import React, { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import AuthCard from '../components/auth/AuthCard';
import PasswordInput from '../components/auth/PasswordInput';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import { calculatePasswordScore } from '../components/auth/PasswordStrengthMeter';

export const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { resetPassword } = useAuth();

  const token = searchParams.get('token') || '';

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(
    !token ? 'Password reset token is missing or invalid. Please request a new link.' : null
  );
  const [success, setSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!token) {
      setError('Password reset token is missing or invalid. Please request a new link.');
      return;
    }

    if (!password) {
      setError('Please provide a new password.');
      return;
    }

    const score = calculatePasswordScore(password);
    if (score < 3) {
      setError('Password is too weak. Please meet the required strength criteria.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setIsSubmitting(true);
    try {
      await resetPassword(token, password);
      setSuccess(true);
      setTimeout(() => {
        navigate('/');
      }, 2500);
    } catch (err) {
      setError(err.message || 'Failed to update password. Your reset token may have expired.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthCard
      title="Create new password"
      subtitle="Ensure your new password is at least 8 characters with numbers and symbols."
      footer={
        <p>
          <Link
            to="/"
            className="font-bold text-brand-700 underline-offset-4 hover:underline"
          >
            Return to login
          </Link>
        </p>
      }
    >
      {success ? (
        <div className="text-center py-4 space-y-3">
          <div className="w-12 h-12 bg-brand-100 text-brand-700 rounded-full flex items-center justify-center mx-auto">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h3 className="font-semibold text-ink text-base">Password Updated!</h3>
          <p className="text-xs text-ink/60">
            Redirecting you to the sign-in page...
          </p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          {error && (
            <Alert type="error" onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          <PasswordInput
            label="New Password"
            id="reset-password"
            name="newPassword"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (error) setError(null);
            }}
            showStrength={true}
            required
          />

          <PasswordInput
            label="Confirm New Password"
            id="reset-confirm-password"
            name="confirmPassword"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              if (error) setError(null);
            }}
            required
          />

          <div className="pt-2">
            <Button
              type="submit"
              variant="primary"
              size="md"
              fullWidth
              loading={isSubmitting}
            >
              Update Password & Sign In
            </Button>
          </div>
        </form>
      )}
    </AuthCard>
  );
};

export default ResetPasswordPage;
