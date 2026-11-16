import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import DashboardLayout from "./components/dashboard/DashboardLayout.jsx";
import UserDashboard from "./pages/dashboard/UserDashboard.jsx";
import AdminDashboard, { AdminUsers, AdminAnalytics, AdminReports } from "./pages/dashboard/AdminDashboard.jsx";
import ModeratorDashboard, { ModeratorFlagged, ModeratorDiscussions, ModeratorDecisions } from "./pages/dashboard/ModeratorDashboard.jsx";
import CreateBoard from "./pages/dashboard/CreateBoard.jsx";
import CreatePoll from "./pages/dashboard/CreatePoll.jsx";
import CompareOptions from "./pages/dashboard/CompareOptions.jsx";
import OpenDiscussion from "./pages/dashboard/OpenDiscussion.jsx";
import MyVotes from "./pages/dashboard/MyVotes.jsx";
import Compare from "./pages/dashboard/Compare.jsx";
import Discuss from "./pages/dashboard/Discuss.jsx";
import AllBoards from "./pages/dashboard/AllBoards.jsx";

function RequireAuth({ children }) {
    const { user } = useAuth();
    return user ? children : <Navigate to="/login" replace />;
}

function DashboardIndex() {
    const { user } = useAuth();
    if (!user) return <Navigate to="/login" replace />;
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
                    <Route path="/login"    element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    <Route path="/dashboard" element={<RequireAuth><DashboardLayout /></RequireAuth>}>
                        <Route index element={<DashboardIndex />} />

                        {/* User routes */}
                        <Route path="boards"      element={<AllBoards />} />
                        <Route path="boards/new"  element={<CreateBoard />} />
                        <Route path="polls/new"   element={<CreatePoll />} />
                        <Route path="compare/new" element={<CompareOptions />} />
                        <Route path="discuss/new" element={<OpenDiscussion />} />
                        <Route path="votes"       element={<MyVotes />} />
                        <Route path="compare"     element={<Compare />} />
                        <Route path="discuss"     element={<Discuss />} />

                        {/* Admin routes — each sidebar link → its own page */}
                        <Route path="admin"           element={<AdminDashboard />} />
                        <Route path="admin/users"     element={<AdminUsers />} />
                        <Route path="admin/analytics" element={<AdminAnalytics />} />
                        <Route path="admin/reports"   element={<AdminReports />} />

                        {/* Moderator routes — each sidebar link → its own page */}
                        <Route path="moderator"                element={<ModeratorDashboard />} />
                        <Route path="moderator/flagged"        element={<ModeratorFlagged />} />
                        <Route path="moderator/discussions"    element={<ModeratorDiscussions />} />
                        <Route path="moderator/decisions"      element={<ModeratorDecisions />} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
