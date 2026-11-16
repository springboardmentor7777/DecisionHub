import { useEffect } from "react";
import { useAuth } from "../auth/useAuth.js";
import Dashboard from "./dashboard.jsx";

function DashboardEntry() {
  const { signOut, status, user } = useAuth();

  useEffect(() => {
    if (status === "anonymous") {
      window.location.replace("/login");
    }
  }, [status]);

  if (status === "loading") {
    return <main className="dh-auth-message">Restoring your session...</main>;
  }

  if (status === "error") {
    return (
      <main className="dh-auth-message" role="alert">
        <p>
          We could not verify your saved session. Check the backend connection.
        </p>
        <a href="/login">Return to sign in</a>
      </main>
    );
  }

  if (status === "anonymous") {
    return (
      <main className="dh-auth-message">
        Redirecting to sign in...
        <a href="/login">Continue to sign in</a>
      </main>
    );
  }

  function handleLogout() {
    signOut();
    window.location.replace("/login");
  }

  return <Dashboard onLogout={handleLogout} user={user} />;
}

export default DashboardEntry;
