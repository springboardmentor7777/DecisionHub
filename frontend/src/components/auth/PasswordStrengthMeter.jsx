import React from 'react';

export const MIN_PASSWORD_LENGTH = 8;
export const MAX_PASSWORD_LENGTH = 72; // backend limit

/**
 * Calculates password strength score (0 to 4)
 */
export const calculatePasswordScore = (password = '') => {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  return score;
};

/**
 * A password is acceptable only if it is long enough AND has
 * at least 3 of the 4 strength criteria.
 */
export const isPasswordAcceptable = (password = '') =>
  password.length >= MIN_PASSWORD_LENGTH && calculatePasswordScore(password) >= 3;

const LEVELS = [
  { label: '', bar: 'bg-brand-900/10', text: 'text-ink/50' },
  { label: 'Too weak', bar: 'bg-red-600', text: 'text-red-700' },
  { label: 'Weak', bar: 'bg-marigold-500', text: 'text-marigold-600' },
  { label: 'Good', bar: 'bg-brand-400', text: 'text-brand-700' },
  { label: 'Strong', bar: 'bg-brand-600', text: 'text-brand-700' },
];

export const PasswordStrengthMeter = ({ password = '' }) => {
  if (!password) return null;

  const score = calculatePasswordScore(password);
  const level = LEVELS[score];

  const rules = [
    { label: '8 or more characters', met: password.length >= 8 },
    { label: 'Upper and lower case', met: /[A-Z]/.test(password) && /[a-z]/.test(password) },
    { label: 'A number', met: /[0-9]/.test(password) },
    { label: 'A symbol', met: /[^A-Za-z0-9]/.test(password) },
  ];

  return (
    <div className="mt-3" aria-live="polite">
      <div className="flex items-center gap-3">
        <div className="flex flex-1 gap-1.5">
          {[1, 2, 3, 4].map((tier) => (
            <div
              key={tier}
              className={`h-1.5 flex-1 rounded-full transition-colors duration-300 ${
                score >= tier ? level.bar : 'bg-brand-900/10'
              }`}
            />
          ))}
        </div>
        <span className={`w-16 text-right text-xs font-bold ${level.text}`}>{level.label}</span>
      </div>

      <ul className="mt-2.5 grid grid-cols-2 gap-x-3 gap-y-1">
        {rules.map((rule) => (
          <li
            key={rule.label}
            className={`flex items-center gap-1.5 text-xs ${
              rule.met ? 'font-semibold text-brand-700' : 'text-ink/50'
            }`}
          >
            {rule.met ? (
              <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            ) : (
              <span className="ml-1 mr-0.5 h-1.5 w-1.5 shrink-0 rounded-full bg-ink/25" />
            )}
            <span>{rule.label}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PasswordStrengthMeter;
