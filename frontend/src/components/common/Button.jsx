import React from 'react';
import Spinner from './Spinner';

export const Button = ({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  disabled = false,
  loading = false,
  icon: Icon,
  className = '',
  onClick,
  ...props
}) => {
  const baseClasses =
    'relative inline-flex items-center justify-center font-semibold transition duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed select-none rounded-[10px] active:translate-y-px';

  const sizeClasses = {
    sm: 'text-xs px-3 h-8 gap-1.5',
    md: 'text-[15px] px-5 h-11 gap-2',
    lg: 'text-base px-6 h-12 gap-2.5',
  }[size] || 'text-[15px] px-5 h-11 gap-2';

  const variantClasses = {
    primary:
      'bg-brand-800 hover:bg-brand-900 text-white shadow-sm focus-visible:ring-brand-600 focus-visible:ring-offset-paper',
    secondary:
      'bg-ink hover:bg-black text-white shadow-sm focus-visible:ring-ink',
    outline:
      'border border-brand-900/20 bg-white hover:bg-brand-50 text-ink focus-visible:ring-brand-600',
    ghost:
      'bg-transparent hover:bg-brand-900/5 text-ink/70 hover:text-ink focus-visible:ring-brand-600',
    danger:
      'bg-red-700 hover:bg-red-800 text-white shadow-sm focus-visible:ring-red-600',
  }[variant] || 'bg-brand-800 hover:bg-brand-900 text-white';

  const widthClass = fullWidth ? 'w-full' : '';

  return (
    <button
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={`${baseClasses} ${sizeClasses} ${variantClasses} ${widthClass} ${className}`}
      {...props}
    >
      {loading && <Spinner size={size === 'lg' ? 'md' : 'sm'} className="mr-1.5" />}
      {!loading && Icon && <Icon className="w-4 h-4 shrink-0" />}
      <span>{children}</span>
    </button>
  );
};

export default Button;
