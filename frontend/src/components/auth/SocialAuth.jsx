import React from 'react';
import { API_BASE_URL } from '../../api/client';

export const SocialAuth = ({ dividerText = 'or use your email', onGoogleClick }) => {
  const handleGoogleSignIn = () => {
    if (onGoogleClick) {
      onGoogleClick();
    } else {
      // Spring Boot OAuth2 entry point
      window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
    }
  };

  return (
    <div>
      <button
        type="button"
        onClick={handleGoogleSignIn}
        className="flex h-11 w-full items-center justify-center gap-3 rounded-[10px] border border-brand-900/20 bg-white text-[15px] font-semibold text-ink transition hover:bg-brand-50 active:translate-y-px focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-600 focus-visible:ring-offset-2 focus-visible:ring-offset-paper"
      >
        <svg className="h-[18px] w-[18px] shrink-0" viewBox="0 0 24 24" aria-hidden="true">
          <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z" />
          <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.25v3.15C3.25 21.36 7.34 24 12 24z" />
          <path fill="#FBBC05" d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.13-1.55.38-2.27V6.58H1.25C.45 8.18 0 10.03 0 12s.45 3.82 1.25 5.42l4.03-3.15z" />
          <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.34 0 3.25 2.64 1.25 6.58l4.03 3.15c.95-2.83 3.6-4.98 6.72-4.98z" />
        </svg>
        <span>Continue with Google</span>
      </button>

      {dividerText && (
        <div className="my-6 flex items-center gap-4" role="separator">
          <span className="h-px flex-1 bg-brand-900/15" />
          <span className="text-[13px] text-ink/50">{dividerText}</span>
          <span className="h-px flex-1 bg-brand-900/15" />
        </div>
      )}
    </div>
  );
};

export default SocialAuth;
