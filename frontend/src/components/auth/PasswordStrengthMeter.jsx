import React from 'react';

export const MIN_PASSWORD_LENGTH = 8;

/**
 * A password is acceptable only if it is long enough AND has
 * at least 3 of the 4 strength criteria.
 */
export const isPasswordAcceptable = (password = '') =>
  password.length >= MIN_PASSWORD_LENGTH && calculatePasswordScore(password) >= 3;

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

export const PasswordStrengthMeter = ({ password = '' }) => {
  const score = calculatePasswordScore(password);

  const levels = [
    { label: 'Very Weak', color: 'bg-slate-200' },
    { label: 'Weak', color: 'bg-red-500' },
    { label: 'Fair', color: 'bg-amber-500' },
    { label: 'Good', color: 'bg-blue-500' },
    { label: 'Strong', color: 'bg-emerald-500' },
  ];

  const currentLevel = levels[score];

  const rules = [
    { label: '8+ characters', met: password.length >= 8 },
    { label: 'Uppercase & Lowercase', met: /[A-Z]/.test(password) && /[a-z]/.test(password) },
    { label: 'At least 1 number', met: /[0-9]/.test(password) },
    { label: '1 special character (!@#$...)', met: /[^A-Za-z0-9]/.test(password) },
  ];

  if (!password) return null;

  return (
    <div className="mt-2 space-y-2">
      {/* 4-bar indicator */}
      <div className="flex items-center gap-1.5">
        {[1, 2, 3, 4].map((tier) => (
          <div
            key={tier}
            className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
              score >= tier ? currentLevel.color : 'bg-slate-200'
            }`}
          />
        ))}
      </div>

      <div className="flex justify-between items-center text-xs">
        <span className="text-slate-500">Security rating:</span>
        <span className={`font-semibold ${score >= 3 ? 'text-emerald-600' : score === 2 ? 'text-amber-600' : 'text-red-600'}`}>
          {currentLevel.label}
        </span>
      </div>

      {/* Criteria checklist */}
      <div className="grid grid-cols-2 gap-1 pt-1 text-[11px]">
        {rules.map((rule, idx) => (
          <div
            key={idx}
            className={`flex items-center gap-1.5 ${
              rule.met ? 'text-emerald-600 font-medium' : 'text-slate-400'
            }`}
          >
            {rule.met ? (
              <svg className="w-3.5 h-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            ) : (
              <span className="w-1.5 h-1.5 rounded-full bg-slate-300 shrink-0 ml-1" />
            )}
            <span>{rule.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PasswordStrengthMeter;
