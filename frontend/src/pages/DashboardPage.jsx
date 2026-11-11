import React from 'react';
import useAuth from '../hooks/useAuth';
import Button from '../components/common/Button';

export const DashboardPage = () => {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <span className="font-bold text-slate-900 tracking-tight text-lg">
            Decision<span className="text-brand-600">Hub</span>
          </span>

          <Button variant="outline" size="sm" onClick={logout} className="text-xs">
            Sign Out
          </Button>
        </div>
      </header>

      {/* Dashboard content will be added by the dashboard team */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8" />
    </div>
  );
};

export default DashboardPage;