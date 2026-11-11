import { useState } from 'react'
import './LoginPage.css'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validateField(name, value) {
  const trimmedValue = value.trim()

  if (!trimmedValue) {
    return name === 'email'
      ? 'Enter your email address.'
      : 'Enter your password.'
  }

  if (name === 'email' && !emailPattern.test(trimmedValue)) {
    return 'Enter a valid email address.'
  }

  return ''
}

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState({})
  const [submitMessage, setSubmitMessage] = useState('')

  function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = {
      email: validateField('email', email),
      password: validateField('password', password),
    }

    setErrors(nextErrors)
    setSubmitMessage(
      nextErrors.email || nextErrors.password
        ? ''
        : 'Your details look valid. Sign-in is not connected yet.',
    )
  }

  function handleEmailChange(event) {
    const value = event.target.value
    setEmail(value)
    setSubmitMessage('')

    if (errors.email) {
      setErrors((currentErrors) => ({
        ...currentErrors,
        email: validateField('email', value),
      }))
    }
  }

  function handlePasswordChange(event) {
    const value = event.target.value
    setPassword(value)
    setSubmitMessage('')

    if (errors.password) {
      setErrors((currentErrors) => ({
        ...currentErrors,
        password: validateField('password', value),
      }))
    }
  }

  return (
    <main className="login-page">
      <section className="login-shell" aria-label="DecisionHub login">
        <aside className="login-intro">
          <a className="login-brand" href="#login" aria-label="DecisionHub home">
            <span className="login-brand-mark" aria-hidden="true">
              <span />
              <span />
            </span>
            <span>DecisionHub</span>
          </a>

          <div className="login-intro-copy">
            <p className="login-eyebrow">Better decisions, together</p>
            <h1>Make your voice part of the decision.</h1>
            <p className="login-intro-description">
              Find common ground, share perspectives, and move forward as a
              community.
            </p>
          </div>

          <div className="login-quote" aria-hidden="true">
            <span className="login-quote-mark">“</span>
            <p>Good decisions start with listening.</p>
            <span className="login-quote-rule" />
          </div>
          <span className="login-decoration login-decoration-one" aria-hidden="true" />
          <span className="login-decoration login-decoration-two" aria-hidden="true" />
        </aside>

        <div className="login-form-panel" id="login">
          <div className="login-form-wrap">
            <div className="login-heading">
              <p className="login-mobile-brand">DecisionHub</p>
              <p className="login-eyebrow">Welcome back</p>
              <h2>Sign in to your account</h2>
              <p className="login-subtitle">
                Pick up where your community left off.
              </p>
            </div>

            <form className="login-form" onSubmit={handleSubmit} noValidate>
              <div className="login-field">
                <label htmlFor="login-email">Email address</label>
                <input
                  autoComplete="email"
                  aria-invalid={Boolean(errors.email)}
                  aria-describedby={errors.email ? 'login-email-error' : undefined}
                  id="login-email"
                  name="email"
                  onChange={handleEmailChange}
                  placeholder="you@example.com"
                  type="email"
                  value={email}
                />
                {errors.email && (
                  <p className="login-error" id="login-email-error" role="alert">
                    {errors.email}
                  </p>
                )}
              </div>

              <div className="login-field">
                <div className="login-password-label">
                  <label htmlFor="login-password">Password</label>
                  <a
                    className="login-text-button"
                    onClick={(event) => event.preventDefault()}
                    href="#forgot-password"
                  >
                    Forgot password?
                  </a>
                </div>
                <div className="login-password-input">
                  <input
                    autoComplete="current-password"
                    aria-invalid={Boolean(errors.password)}
                    aria-describedby={
                      errors.password ? 'login-password-error' : undefined
                    }
                    id="login-password"
                    name="password"
                    onChange={handlePasswordChange}
                    placeholder="Enter your password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                  />
                  <button
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                    aria-pressed={showPassword}
                    className="login-visibility-button"
                    onClick={() => setShowPassword((isVisible) => !isVisible)}
                    type="button"
                  >
                    {showPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
                {errors.password && (
                  <p
                    className="login-error"
                    id="login-password-error"
                    role="alert"
                  >
                    {errors.password}
                  </p>
                )}
              </div>

              <button className="login-submit" type="submit">
                Log in
                <span aria-hidden="true">→</span>
              </button>
            </form>

            {submitMessage && (
              <p className="login-status" role="status">
                {submitMessage}
              </p>
            )}

            <p className="login-form-note">
              Your community is better when you&apos;re part of it.
            </p>
          </div>
          <p className="login-footer">Thoughtful decisions start here.</p>
        </div>
      </section>
    </main>
  )
}

export default LoginPage
