import React, { useState, forwardRef } from 'react';
import Input from '../common/Input';
import PasswordStrengthMeter from './PasswordStrengthMeter';

export const PasswordInput = forwardRef(
  (
    {
      label = 'Password',
      id,
      name = 'password',
      value,
      onChange,
      placeholder = '••••••••',
      showStrength = false,
      error,
      required = true,
      className = '',
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);

    const toggleIcon = (
      <button
        type="button"
        tabIndex={-1}
        onClick={() => setShowPassword((prev) => !prev)}
        className="text-slate-400 hover:text-slate-600 focus:outline-none focus:text-slate-700 p-1 rounded"
        aria-label={showPassword ? 'Hide password' : 'Show password'}
      >
        {showPassword ? (
          // Eye Off Icon
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
          </svg>
        ) : (
          // Eye Icon
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
        )}
      </button>
    );

    return (
      <div className={className}>
        <Input
          ref={ref}
          id={id || name}
          name={name}
          type={showPassword ? 'text' : 'password'}
          label={label}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          error={error}
          required={required}
          endIcon={toggleIcon}
          {...props}
        />
        {showStrength && <PasswordStrengthMeter password={value} />}
      </div>
    );
  }
);

PasswordInput.displayName = 'PasswordInput';
export default PasswordInput;
