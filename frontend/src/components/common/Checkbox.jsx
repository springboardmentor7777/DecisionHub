import React from 'react';

export const Checkbox = ({
  id,
  name,
  label,
  description,
  checked,
  onChange,
  disabled = false,
  error,
  className = '',
  ...props
}) => {
  const checkboxId = id || name || `chk-${Math.random().toString(36).substring(2, 9)}`;

  return (
    <div className={`relative flex items-start ${className}`}>
      <div className="flex h-5 items-center">
        <input
          id={checkboxId}
          name={name}
          type="checkbox"
          checked={checked}
          onChange={onChange}
          disabled={disabled}
          className="h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500 disabled:opacity-50 cursor-pointer"
          {...props}
        />
      </div>
      {(label || description) && (
        <div className="ml-2.5 text-sm leading-5">
          {label && (
            <label
              htmlFor={checkboxId}
              className={`font-medium select-none cursor-pointer ${
                disabled ? 'text-slate-400' : 'text-slate-700'
              }`}
            >
              {label}
            </label>
          )}
          {description && (
            <p className="text-xs text-slate-500 mt-0.5">{description}</p>
          )}
          {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
        </div>
      )}
    </div>
  );
};

export default Checkbox;
