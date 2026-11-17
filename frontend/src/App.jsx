import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthProvider.jsx";
import { useAuth } from "./auth/useAuth.js";
import Login from "./Login.jsx";

function AuthRoute({ mode }) {
  const { status } = useAuth();

  if (status === "loading") {
    return (
      <main className="login-page">
        <p role="status">Restoring your session...</p>
      </main>
    );
  }

  if (status === "authenticated") {
    return <Navigate replace to="/Dashboard.html" />;
  }

  return <Login mode={mode} />;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AuthRoute mode="login" />} path="/" />
          <Route element={<AuthRoute mode="login" />} path="/login" />
          <Route element={<AuthRoute mode="register" />} path="/register" />
          <Route
            element={<AuthRoute mode="forgot" />}
            path="/forgot-password"
          />
          <Route
            element={<AuthRoute mode="reset" />}
            path="/reset-password"
          />
          <Route element={<Navigate replace to="/" />} path="*" />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;