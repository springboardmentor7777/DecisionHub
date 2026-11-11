import React from 'react';
import { Link } from 'react-router-dom';

export const AuthLayout = ({ children }) => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between relative overflow-hidden">
      {/* Background Decorative Gradients */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-96 overflow-hidden pointer-events-none -z-10">
        <div className="absolute -top-32 left-1/4 w-96 h-96 bg-brand-200/40 rounded-full blur-3xl opacity-70" />
        <div className="absolute -top-20 right-1/4 w-96 h-96 bg-indigo-200/40 rounded-full blur-3xl opacity-70" />
      </div>

      {/* Top Simple Header */}
      <header className="w-full max-w-6xl mx-auto px-4 py-6 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-brand-700 to-brand-500 text-white flex items-center justify-center font-bold text-sm shadow-sm group-hover:shadow transition-shadow">
            DH
          </div>
          <span className="font-bold text-lg tracking-tight text-slate-900">
            Decision<span className="text-brand-600">Hub</span>
          </span>
        </Link>

        <div className="flex items-center gap-3 text-xs text-slate-500">
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 font-medium text-slate-600">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            Decision Intelligence v1.0
          </span>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex items-center justify-center px-4 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="w-full max-w-6xl mx-auto px-4 py-6 text-center text-xs text-slate-400">
        <p>
          &copy; {new Date().getFullYear()} DecisionHub — Collaborative Decision Intelligence & Community Polling Platform.
        </p>
      </footer>
    </div>
  );
};

export default AuthLayout;
