# DecisionHub frontend

## Run locally

1. Copy `.env.example` to `.env.local` and set `VITE_API_BASE_URL` to the Spring
   Boot API origin. The default is `http://localhost:8080`.
2. Install dependencies with `npm ci`.
3. Start the frontend with `npm run dev`.

The backend must be running and allow the frontend origin (`http://localhost:5173`)
through CORS for authentication requests to work.

## Authentication routes

- `/` and `/login` — sign in
- `/register` — create an account
- `/forgot-password` — request a password reset email
- `/reset-password?token=...` — set a new password from the email link
- `/Dashboard.html` — authenticated dashboard; redirects to sign in without a
  valid session

The frontend stores the JWT in `sessionStorage` and validates a saved session
with `GET /api/auth/me`. Login, registration, and password reset use the
corresponding `/api/auth/*` endpoints.
