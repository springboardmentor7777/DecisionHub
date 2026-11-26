import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Button from '../components/common/Button';

const AppShell = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  const navClass = ({ isActive }) =>
    `rounded-lg px-3 py-2 text-sm font-semibold transition ${
      isActive ? 'bg-brand-100 text-brand-900' : 'text-ink/70 hover:bg-brand-50 hover:text-ink'
    }`;

  return (
    <div className="min-h-screen bg-paper text-ink">
      <header className="border-b border-brand-900/10 bg-white">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6">
          <Link to="/dashboard" className="font-display text-xl font-extrabold tracking-tight text-brand-900">
            Decision<span className="text-brand-600">Hub</span>
          </Link>
          <nav aria-label="Main navigation" className="order-3 flex w-full items-center gap-1 sm:order-none sm:w-auto">
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
            <NavLink to="/boards" className={navClass}>Boards</NavLink>
            <NavLink to="/communities" className={navClass}>Communities</NavLink>
          </nav>
          <div className="flex items-center gap-3">
            <span className="hidden max-w-40 truncate text-sm font-medium text-ink/70 sm:block">
              {user?.displayName || 'My account'}
            </span>
            <Button size="sm" variant="outline" onClick={signOut}>Sign out</Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
};

export default AppShell;
