import React, { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Spinner from '../components/common/Spinner';

/**
 * Landing page for Google (OAuth2) sign-in.
 * The backend redirects here as: /oauth2/redirect?token=<jwt>
 * (or ?error=<message> if sign-in failed).
 */
export const OAuth2RedirectPage = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithToken, setAuthError } = useAuth();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return; // avoid double-run in React StrictMode
    handled.current = true;

    const token = params.get('token');
    const error = params.get('error');

    if (!token) {
      setAuthError(error || 'Google sign-in failed. Please try again.');
      navigate('/', { replace: true });
      return;
    }

    loginWithToken(token)
      .then(() => navigate('/dashboard', { replace: true }))
      .catch(() => navigate('/', { replace: true }));
  }, [params, navigate, loginWithToken, setAuthError]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="text-center space-y-3">
        <Spinner size="lg" className="text-brand-600" />
        <p className="text-xs text-slate-500 font-medium">Completing Google sign-in...</p>
      </div>
    </div>
  );
};

export default OAuth2RedirectPage;
