import React from 'react';

/**
 * DecisionHub logo: two poll bars (one clearly ahead) + wordmark.
 * tone="light" is for dark backgrounds, tone="dark" for light ones.
 */
export const LogoMark = ({ className = 'w-9 h-9' }) => (
  <svg className={className} viewBox="0 0 32 32" aria-hidden="true">
    <rect width="32" height="32" rx="8" fill="#123a37" />
    <rect x="7" y="16" width="7" height="9" rx="2" fill="#afe0d9" />
    <rect x="18" y="8" width="7" height="17" rx="2" fill="#f6bb3b" />
  </svg>
);

export const Logo = ({ tone = 'dark', className = '' }) => (
  <span className={`inline-flex items-center gap-2.5 ${className}`}>
    <LogoMark className="w-8 h-8 shrink-0" />
    <span
      className={`font-display text-xl font-bold tracking-tight ${
        tone === 'light' ? 'text-white' : 'text-ink'
      }`}
    >
      DecisionHub
    </span>
  </span>
);

export default Logo;
