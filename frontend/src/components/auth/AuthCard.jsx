import React from 'react';

/**
 * Heading + body + footer for the auth screens.
 * (Kept under its old name so the forgot/reset password pages keep working.)
 */
export const AuthCard = ({ title, subtitle, children, footer, className = '' }) => {
  return (
    <section className={`w-full ${className}`}>
      {title && (
        <h1 className="font-display text-[1.95rem] font-bold leading-tight tracking-tight text-ink">
          {title}
        </h1>
      )}
      {subtitle && (
        <p className="mt-2 text-[15px] leading-relaxed text-ink/65">{subtitle}</p>
      )}

      <div className="mt-8">{children}</div>

      {footer && <div className="mt-8 text-sm text-ink/70">{footer}</div>}
    </section>
  );
};

export default AuthCard;
