import React from 'react';
import Card from '../common/Card';

export const AuthCard = ({
  title,
  subtitle,
  children,
  footer,
  className = '',
}) => {
  return (
    <div className={`w-full max-w-md mx-auto ${className}`}>
      {/* Brand Header */}
      <div className="text-center mb-6">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-tr from-brand-700 to-brand-500 text-white shadow-lg shadow-brand-500/30 mb-3">
          {/* Decision Matrix Icon */}
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        </div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
          Decision<span className="text-brand-600">Hub</span>
        </h1>
        {title && (
          <h2 className="text-lg font-semibold text-slate-800 mt-2">
            {title}
          </h2>
        )}
        {subtitle && (
          <p className="text-sm text-slate-500 mt-1 max-w-xs mx-auto">
            {subtitle}
          </p>
        )}
      </div>

      {/* Main Form Card */}
      <Card padding="p-7 sm:p-8" className="shadow-lg shadow-slate-200/50 border-slate-200">
        {children}
      </Card>

      {/* Optional Card Footer Links */}
      {footer && (
        <div className="mt-6 text-center text-sm text-slate-600">
          {footer}
        </div>
      )}
    </div>
  );
};

export default AuthCard;
