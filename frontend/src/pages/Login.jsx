
import { Link } from 'react-router-dom'

function Login() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-10">

      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60 sm:p-10">

        {/* Logo */}
        <div className="mb-7 flex flex-col items-center">

          <Link to="/" aria-label="DecisionHub home">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-600 text-2xl font-bold text-white">
              D
            </div>
          </Link>

          <h1 className="mt-4 text-2xl font-bold text-slate-900">
            Welcome back
          </h1>

          <p className="mt-2 text-center text-sm text-slate-500">
            Sign in to continue to DecisionHub
          </p>

        </div>

        {/* Social Login */}
        <div className="space-y-3">

          {/* Google */}
          <button
            type="button"
            disabled
            title="Google sign-in will be available after OAuth configuration"
            className="flex w-full cursor-not-allowed items-center justify-center gap-3 rounded-lg border border-slate-300 bg-white px-4 py-3 font-medium text-slate-700 opacity-70"
          >
            <svg viewBox="0 0 48 48" className="h-5 w-5" aria-hidden="true">
              <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 3.01 13.22l7.98 6.19C12.88 13.72 18.04 9.5 24 9.5Z" transform="translate(0 5)"/>
              <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.25 5.48-4.73 7.18l7.28 5.65c4.25-3.92 7.49-9.72 7.49-17.3Z"/>
              <path fill="#FBBC05" d="M10.99 28.41a14.4 14.4 0 0 1 0-8.82l-7.98-6.19a24 24 0 0 0 0 21.2l7.98-6.19Z"/>
              <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.49-5.8l-7.28-5.65c-2.02 1.36-4.61 2.16-8.21 2.16-5.96 0-11.12-4.22-13.01-9.91l-7.98 6.19C6.51 43.62 14.62 48 24 48Z"/>
            </svg>

            Continue with Google
          </button>

          {/* LinkedIn */}
          <button
            type="button"
            disabled
            title="LinkedIn sign-in will be available after OAuth configuration"
            className="flex w-full cursor-not-allowed items-center justify-center gap-3 rounded-lg border border-slate-300 bg-white px-4 py-3 font-medium text-slate-700 opacity-70"
          >
            <svg viewBox="0 0 24 24" className="h-5 w-5" aria-hidden="true">
              <path fill="#0A66C2" d="M20.45 2H3.55C2.69 2 2 2.69 2 3.55v16.9c0 .86.69 1.55 1.55 1.55h16.9c.86 0 1.55-.69 1.55-1.55V3.55C22 2.69 21.31 2 20.45 2ZM8 18H5V9h3v9ZM6.5 7.73a1.75 1.75 0 1 1 0-3.5 1.75 1.75 0 0 1 0 3.5ZM19 18h-3v-4.37c0-1.04-.02-2.37-1.45-2.37-1.45 0-1.67 1.13-1.67 2.29V18h-3V9h2.88v1.23h.04c.4-.75 1.38-1.54 2.84-1.54C18.68 8.69 19 10.64 19 13.17V18Z"/>
            </svg>

            Continue with LinkedIn
          </button>

        </div>

        {/* Divider */}
        <div className="my-6 flex items-center gap-4">
          <div className="h-px flex-1 bg-slate-200"></div>
          <span className="text-sm font-medium text-slate-400">OR</span>
          <div className="h-px flex-1 bg-slate-200"></div>
        </div>

        {/* Email Login */}
        <form className="space-y-5">

          <div>
            <label
              htmlFor="email"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Email address
            </label>

            <input
              id="email"
              name="email"
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
              required
              className="w-full rounded-lg border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            />
          </div>

          <div>
            <div className="mb-2 flex items-center justify-between">

              <label
                htmlFor="password"
                className="text-sm font-medium text-slate-700"
              >
                Password
              </label>

              <button
                type="button"
                className="text-sm font-medium text-indigo-600 hover:text-indigo-700"
              >
                Forgot password?
              </button>

            </div>

            <input
              id="password"
              name="password"
              type="password"
              placeholder="Enter your password"
              autoComplete="current-password"
              required
              className="w-full rounded-lg border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            />
          </div>

          <button
            type="submit"
            className="w-full rounded-lg bg-indigo-600 py-3 font-semibold text-white shadow-md shadow-indigo-200 transition hover:bg-indigo-700"
          >
            Sign In
          </button>

        </form>

        {/* Register Link */}
        <p className="mt-7 text-center text-sm text-slate-600">
          Don't have an account?{' '}
          <Link
            to="/register"
            className="font-semibold text-indigo-600 hover:text-indigo-700"
          >
            Create account
          </Link>
        </p>

        {/* Home Link */}
        <div className="mt-8 border-t border-slate-100 pt-6 text-center">
          <Link
            to="/"
            className="text-sm text-slate-500 hover:text-indigo-600"
          >
            ← Back to home
          </Link>
        </div>

      </div>
    </div>
  )
}

export default Login
