
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import Dashboard from "./Dashboard/dashboard.jsx";
import "./dashboard/dashboard.css";

createRoot(document.getElementById("dashboard-root")).render(
  <StrictMode>
    <Dashboard />
  </StrictMode>
);
