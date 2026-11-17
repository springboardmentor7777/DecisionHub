import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "./auth/useAuth.js";
import {
  requestPasswordReset,
  resetPassword,
} from "./services/authApi.js";
import "./Login.css";

function PasswordField({
  autoComplete,
  id,
  label,
  minLength,
  name,
  onChange,
  required = true,
  showLabel = true,
  value,
}) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <>
      {showLabel && <label htmlFor={id}>{label}</label>}
      <div className="login-password-field">
        <input
          autoComplete={autoComplete}
          id={id}
          maxLength={72}
          minLength={minLength}
          name={name}
          onChange={onChange}
          placeholder="Enter your password"
          required={required}
          type={showPassword ? "text" : "password"}
          value={value}
        />
        <button
          aria-label={showPassword ? "Hide password" : "Show password"}
          aria-pressed={showPassword}
          className="login-text-button"
          onClick={() => setShowPassword((visible) => !visible)}
          type="button"
        >
          {showPassword ? "Hide" : "Show"}
        </button>
      </div>
    </>
  );
}

function Login({ mode }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { authError, signIn, signUp } = useAuth();
  const resetToken = new URLSearchParams(location.search).get("token") || "";
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [notice, setNotice] = useState(() => {
    if (location.state?.message) {
      return { kind: "success", text: location.state.message };
    }
    return authError ? { kind: "error", text: authError } : null;
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isRegister = mode === "register";
  const isForgot = mode === "forgot";
  const isReset = mode === "reset";
  const title = isRegister
    ? "Create your account"
    : isForgot
      ? "Reset your password"
      : isReset
        ? "Choose a new password"
        : "Sign in to your account";
  const subtitle = isRegister
    ? "Join your community and start making decisions together."
    : isForgot
      ? "We’ll send a reset link if the account exists."
      : isReset
        ? "Choose a new password for your account."
        : "Pick up where your community left off.";

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    setNotice(null);

    try {
      if (mode === "login") {
        await signIn({ email, password });
        navigate("/Dashboard.html", { replace: true });
      } else if (isRegister) {
        if (password !== confirmPassword) {
          throw new Error("Passwords do not match.");
        }
        await signUp({ name: name.trim(), email: email.trim(), password });
        navigate("/Dashboard.html", { replace: true });
      } else if (isForgot) {
        const result = await requestPasswordReset(email.trim());
        setNotice({ kind: "success", text: result.message });
      } else if (isReset) {
        if (!resetToken) {
          throw new Error("This password reset link is invalid or incomplete.");
        }
        if (password !== confirmPassword) {
          throw new Error("Passwords do not match.");
        }
        const result = await resetPassword(resetToken, password);
        navigate("/login", {
          replace: true,
          state: { message: result.message },
        });
      }
    } catch (error) {
      setNotice({
        kind: "error",
        text:
          error instanceof Error
            ? error.message
            : "The request could not be completed. Please try again.",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card" aria-label="DecisionHub authentication">
        <div className="login-brand-panel">
          <div className="login-brand">
            <span className="login-mark" aria-hidden="true">
              <span />
              <span />
            </span>
            <span>DecisionHub</span>
          </div>

          <div className="login-pitch">
            <p className="login-eyebrow">Better decisions, together</p>
            <h1>Make your voice part of the decision.</h1>
            <p className="login-description">
              Find common ground, share perspectives, and move forward as a
              community.
            </p>
          </div>

          <blockquote className="login-quote">
            <span aria-hidden="true">“</span>
            <p>Good decisions start with listening.</p>
            <i aria-hidden="true" />
          </blockquote>
        </div>

        <div className="login-form-panel">
          <div className="login-form-wrap">
            <p className="login-eyebrow">
              {isRegister ? "Get started" : isForgot || isReset ? "Account help" : "Welcome back"}
            </p>
            <h2>{title}</h2>
            <p className="login-subtitle">{subtitle}</p>

            <form className="login-form" onSubmit={handleSubmit}>
              {isRegister && (
                <>
                  <label htmlFor="login-name">Full name</label>
                  <input
                    autoComplete="name"
                    id="login-name"
                    maxLength={100}
                    name="name"
                    onChange={(event) => setName(event.target.value)}
                    placeholder="Your name"
                    required
                    value={name}
                  />
                </>
              )}

              {!isReset && (
                <>
                  <label htmlFor="login-email">Email address</label>
                  <input
                    autoComplete="email"
                    id="login-email"
                    maxLength={150}
                    name="email"
                    onChange={(event) => setEmail(event.target.value)}
                    placeholder="you@example.com"
                    required
                    type="email"
                    value={email}
                  />
                </>
              )}

              {!isForgot && (
                <div className="login-password-group">
                  {!isRegister && !isReset && (
                    <div className="login-password-label">
                      <label htmlFor="login-password">Password</label>
                      <Link
                        className="login-text-button"
                        to="/forgot-password"
                      >
                        Forgot password?
                      </Link>
                    </div>
                  )}
                  {isRegister ? (
                    <PasswordField
                      autoComplete="new-password"
                      id="login-password"
                      label="Password"
                      minLength={8}
                      name="password"
                      onChange={(event) => setPassword(event.target.value)}
                      value={password}
                    />
                  ) : (
                    <PasswordField
                      autoComplete={isReset ? "new-password" : "current-password"}
                      id="login-password"
                      label={isReset ? "New password" : "Password"}
                      minLength={isReset ? 8 : undefined}
                      name="password"
                      onChange={(event) => setPassword(event.target.value)}
                      showLabel={isReset}
                      value={password}
                    />
                  )}
                  {(isRegister || isReset) && (
                    <PasswordField
                      autoComplete="new-password"
                      id="login-confirm-password"
                      label="Confirm password"
                      minLength={8}
                      name="confirmPassword"
                      onChange={(event) =>
                        setConfirmPassword(event.target.value)
                      }
                      value={confirmPassword}
                    />
                  )}
                </div>
              )}

              <button
                className="login-submit"
                disabled={isSubmitting || (isReset && !resetToken)}
                type="submit"
              >
                {isSubmitting
                  ? "Please wait..."
                  : isRegister
                    ? "Create account"
                    : isForgot
                      ? "Send reset link"
                      : isReset
                        ? "Update password"
                        : "Log in"}
                {!isSubmitting && !isForgot && (
                  <span aria-hidden="true">→</span>
                )}
              </button>
              {notice && (
                <p
                  aria-live="polite"
                  className={`login-notice is-${notice.kind}`}
                  role={notice.kind === "error" ? "alert" : "status"}
                >
                  {notice.text}
                </p>
              )}
            </form>

            <p className="login-community">
              {isRegister ? (
                <>
                  Already have an account?{" "}
                  <Link className="login-text-button" to="/login">
                    Sign in
                  </Link>
                </>
              ) : isForgot || isReset ? (
                <Link className="login-text-button" to="/login">
                  Return to sign in
                </Link>
              ) : (
                <>
                  New to DecisionHub?{" "}
                  <Link className="login-text-button" to="/register">
                    Create an account
                  </Link>
                </>
              )}
            </p>
            <p className="login-footer">Thoughtful decisions start here.</p>
          </div>
        </div>
      </section>
    </main>
  );
}

export default Login;
