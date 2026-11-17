import React, { forwardRef } from 'react';

export const Input = forwardRef(
  (
    {
      label,
      id,
      name,
      type = 'text',
      placeholder,
      value,
      defaultValue,
      onChange,
      onBlur,
      error,
      helperText,
      required = false,
      disabled = false,
      startIcon: StartIcon,
      endIcon: EndIcon,
      className = '',
      ...props
    },
    ref
  ) => {
    const inputId = id || name || `input-${Math.random().toString(36).substring(2, 9)}`;

    return (
      <div className={`w-full ${className}`}>
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-semibold text-ink/85 mb-1.5"
          >
            {label}
          </label>
        )}

        <div className="relative">
          {StartIcon && (
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
              <StartIcon className="h-5 w-5" aria-hidden="true" />
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            name={name}
            type={type}
            placeholder={placeholder}
            value={value}
            defaultValue={defaultValue}
            onChange={onChange}
            onBlur={onBlur}
            disabled={disabled}
            required={required}
            aria-invalid={!!error}
            aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-desc` : undefined}
            className={`block w-full h-11 rounded-[10px] text-[15px] bg-white transition duration-150 border placeholder:text-ink/35 focus:outline-none focus:ring-4 disabled:bg-paper disabled:text-ink/40 disabled:cursor-not-allowed ${
              StartIcon ? 'pl-10' : 'pl-3.5'
            } ${EndIcon ? 'pr-10' : 'pr-3.5'} ${
              error
                ? 'border-red-600 text-ink focus:border-red-600 focus:ring-red-100'
                : 'border-brand-900/20 text-ink hover:border-brand-900/35 focus:border-brand-600 focus:ring-brand-100'
            }`}
            {...props}
          />

          {EndIcon && (
            <div className="absolute inset-y-0 right-0 flex items-center pr-3">
              {EndIcon}
            </div>
          )}
        </div>

        {error && (
          <p id={`${inputId}-error`} className="mt-1.5 text-[13px] font-medium text-red-700 flex items-center gap-1.5">
            <svg className="w-3.5 h-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            <span>{error}</span>
          </p>
        )}

        {!error && helperText && (
          <p id={`${inputId}-desc`} className="mt-1.5 text-[13px] text-ink/55">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
