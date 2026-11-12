import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import DashboardLayout from "./components/dashboard/DashboardLayout.jsx";
import UserDashboard from "./pages/dashboard/UserDashboard.jsx";
import CreateBoard from "./pages/dashboard/CreateBoard.jsx";
import CreatePoll from "./pages/dashboard/CreatePoll.jsx";
import CompareOptions from "./pages/dashboard/CompareOptions.jsx";
import OpenDiscussion from "./pages/dashboard/OpenDiscussion.jsx";
import MyVotes from "./pages/dashboard/MyVotes.jsx";
import Compare from "./pages/dashboard/Compare.jsx";
import Discuss from "./pages/dashboard/Discuss.jsx";
import AllBoards from "./pages/dashboard/AllBoards.jsx";

function DashboardIndex() {
    const { user } = useAuth();
    if (!user) return <Navigate to="/login" replace />;
    // Route to role-specific dashboard
    if (user.role === "admin")     return <Navigate to="/dashboard/admin" replace />;
    if (user.role === "moderator") return <Navigate to="/dashboard/moderator" replace />;
    return <UserDashboard />;
}

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<Navigate to="/login" replace />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Dashboard — all roles share the layout shell */}
                    <Route path="/dashboard" element={<DashboardLayout />}>
                        <Route index element={<DashboardIndex />} />
                        {/* Placeholder sub-routes — add real pages later */}
                        <Route path="boards"      element={<AllBoards />} />
                        <Route path="boards/new"  element={<CreateBoard />} />
                        <Route path="polls/new"     element={<CreatePoll />} />
                        <Route path="compare/new"   element={<CompareOptions />} />
                        <Route path="discuss/new"   element={<OpenDiscussion />} />
                        <Route path="votes"   element={<MyVotes />} />
                        <Route path="compare" element={<Compare />} />
                        <Route path="discuss" element={<Discuss />} />

                        {/* Admin & Moderator dashboards — to be built next */}
                        <Route path="admin"         element={<div className="p-4 text-plum font-semibold">Admin Dashboard — coming soon</div>} />
                        <Route path="moderator"     element={<div className="p-4 text-plum font-semibold">Moderator Dashboard — coming soon</div>} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
