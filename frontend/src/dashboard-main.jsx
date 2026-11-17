
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "./auth/AuthProvider.jsx";
import DashboardEntry from "./Dashboard/DashboardEntry.jsx";
import "./dashboard/dashboard.css";

createRoot(document.getElementById("dashboard-root")).render(
  <StrictMode>
    <AuthProvider>
      <DashboardEntry />
    </AuthProvider>
  </StrictMode>
);
