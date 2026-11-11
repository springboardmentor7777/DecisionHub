/**
 * DecisionHub Design Tokens
 * Central source of truth for colors, typography, spacing, and component styling.
 */

export const theme = {
  colors: {
    primary: {
      light: '#ede9fe',
      main: '#7c3aed',
      hover: '#6d28d9',
      dark: '#4c1d95',
      contrastText: '#ffffff',
    },
    secondary: {
      light: '#f1f5f9',
      main: '#0f172a',
      hover: '#1e293b',
      contrastText: '#ffffff',
    },
    surface: {
      card: '#ffffff',
      background: '#f8fafc',
      muted: '#f1f5f9',
      border: '#e2e8f0',
    },
    text: {
      primary: '#0f172a',
      secondary: '#475569',
      muted: '#94a3b8',
      inverse: '#ffffff',
    },
    status: {
      success: '#10b981',
      warning: '#f59e0b',
      error: '#ef4444',
      info: '#3b82f6',
    },
  },
  roles: {
    user: {
      label: 'Standard User',
      color: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    },
    communityModerator: {
      label: 'Community Moderator',
      color: 'bg-amber-50 text-amber-700 border-amber-200',
    },
    administrator: {
      label: 'Administrator',
      color: 'bg-purple-50 text-purple-700 border-purple-200',
    },
  },
  radii: {
    sm: '0.375rem',
    md: '0.5rem',
    lg: '0.75rem',
    xl: '1rem',
    full: '9999px',
  },
};

export default theme;
