import React, { useState, useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import {
    Users, Flag, BarChart3, ShieldCheck, TrendingUp,
    AlertTriangle, Clock, CheckCircle2, XCircle,
} from "lucide-react";
import { Bar, Line, Doughnut } from "react-chartjs-2";
import {
    Chart as ChartJS, CategoryScale, LinearScale, BarElement,
    PointElement, LineElement, ArcElement, Filler, Tooltip, Legend,
} from "chart.js";
import { useAuth } from "../../context/AuthContext";
import api from "../../api/axios.js";

ChartJS.register(
    CategoryScale, LinearScale, BarElement,
    PointElement, LineElement, ArcElement, Filler, Tooltip, Legend
);

const CHART_OPTS = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
        x: { grid: { display: false }, ticks: { color: "#3b0d4580", font: { size: 11 } } },
        y: { grid: { color: "#3b0d4508" }, ticks: { color: "#3b0d4580", font: { size: 11 }, stepSize: 1 } },
    },
};

const ROLE_BADGE = {
    USER:      "bg-sunset/10 text-sunset",
    ADMIN:     "bg-burgundy/10 text-burgundy",
    MODERATOR: "bg-orange/10 text-orange",
};

function StatCard({ icon: Icon, label, value, color }) {
    return (
        <div className="relative overflow-hidden rounded-2xl bg-white p-5 shadow-sm border border-plum/6 hover:shadow-md transition-shadow">
            <div className={`absolute -right-4 -top-4 h-20 w-20 rounded-full bg-gradient-to-br ${color} opacity-10`} />
            <div className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${color} shadow-md`}>
                <Icon className="h-5 w-5 text-white" />
            </div>
            <p className="mt-4 text-2xl font-extrabold text-plum">{value ?? "—"}</p>
            <p className="text-sm font-medium text-plum/70">{label}</p>
        </div>
    );
}

function useUsers() {
    const [users, setUsers]     = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState("");
    useEffect(() => {
        api.get("/api/admin/users")
            .then(({ data }) => setUsers(Array.isArray(data) ? data : []))
            .catch(() => setError("Could not load users from server."))
            .finally(() => setLoading(false));
    }, []);
    return { users, loading, error };
}

function Overview({ users }) {
    const { user } = useAuth();
    const hour = new Date().getHours();
    const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

    const admins  = users.filter((u) => u.role === "ADMIN").length;
    const mods    = users.filter((u) => u.role === "MODERATOR").length;
    const members = users.filter((u) => u.role === "USER").length;

    return (
        <div className="space-y-8 pb-10">
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-burgundy via-plum to-burgundy p-6 text-white shadow-lg sm:p-8">
                <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-sunset/20 blur-3xl" />
                <div className="relative z-10">
                    <p className="text-sm font-medium text-ivory/70">{greeting} 👋</p>
                    <h1 className="mt-1 text-2xl font-extrabold tracking-tight sm:text-3xl">
                        Admin Panel, <span className="text-coral">{user?.name?.split(" ")[0]}!</span>
                    </h1>
                    <p className="mt-2 text-sm text-ivory/70">Manage users, monitor platform activity and review reports.</p>
                </div>
            </div>

            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Platform Overview</h2>
                <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                    <StatCard icon={Users}       label="Total Users" value={users.length} color="from-sunset to-orange" />
                    <StatCard icon={ShieldCheck} label="Admins"      value={admins}       color="from-burgundy to-plum" />
                    <StatCard icon={Flag}        label="Moderators"  value={mods}         color="from-orange to-coral" />
                    <StatCard icon={TrendingUp}  label="Members"     value={members}      color="from-coral to-sunset" />
                </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-3">
                <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm sm:col-span-1">
                    <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Users by Role</p>
                    <div className="h-44">
                        <Doughnut
                            data={{
                                labels: ["Members", "Moderators", "Admins"],
                                datasets: [{ data: [members, mods, admins], backgroundColor: ["#ff4f81", "#ff8c42", "#7a1f3d"], borderWidth: 0 }],
                            }}
                            options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { display: true, position: "bottom", labels: { font: { size: 11 }, color: "#3b0d45", boxWidth: 10, padding: 8 } } } }}
                        />
                    </div>
                </div>
                <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm sm:col-span-2">
                    <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Recent Members</p>
                    <div className="space-y-2">
                        {users.slice(0, 5).map((u) => (
                            <div key={u.userId ?? u.id} className="flex items-center justify-between rounded-xl border border-plum/6 px-4 py-2.5">
                                <div>
                                    <p className="text-sm font-semibold text-plum">{u.fullName ?? u.name}</p>
                                    <p className="text-xs text-plum/40">{u.email}</p>
                                </div>
                                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase ${ROLE_BADGE[u.role] ?? "bg-plum/10 text-plum"}`}>{u.role}</span>
                            </div>
                        ))}
                        {users.length === 0 && <p className="py-8 text-center text-sm text-plum/30">No users yet.</p>}
                    </div>
                </div>
            </div>
        </div>
    );
}

function UsersPage({ users, loading, error }) {
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");

    const filtered = users.filter((u) => {
        const matchRole = roleFilter === "ALL" || u.role === roleFilter;
        const matchSearch = (u.fullName ?? u.name ?? "").toLowerCase().includes(search.toLowerCase())
            || u.email.toLowerCase().includes(search.toLowerCase());
        return matchRole && matchSearch;
    });

    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Users</h1>
                <p className="mt-1 text-sm text-plum/50">All registered users on the platform.</p>
            </div>

            <div className="flex flex-wrap items-center gap-3">
                <input
                    type="search"
                    placeholder="Search by name or email…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="flex-1 min-w-[200px] rounded-xl border border-plum/10 bg-white px-4 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                />
                {["ALL", "USER", "ADMIN", "MODERATOR"].map((r) => (
                    <button
                        key={r}
                        onClick={() => setRoleFilter(r)}
                        className={`rounded-xl px-4 py-2.5 text-xs font-bold uppercase tracking-wide transition
                            ${roleFilter === r ? "bg-plum text-white" : "bg-white border border-plum/10 text-plum/60 hover:border-plum/30"}`}
                    >
                        {r}
                    </button>
                ))}
            </div>

            <div className="rounded-2xl border border-plum/8 bg-white shadow-sm overflow-hidden">
                {loading ? (
                    <div className="flex items-center justify-center py-16 text-sm text-plum/40">Loading users…</div>
                ) : error ? (
                    <div className="flex items-center justify-center gap-2 py-16 text-sm text-burgundy">
                        <AlertTriangle className="h-4 w-4" /> {error}
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="flex items-center justify-center py-16 text-sm text-plum/40">No users found.</div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b border-plum/8 bg-plum/3">
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">#</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Name</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Email</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Role</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Joined</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-plum/5">
                                {filtered.map((u, i) => (
                                    <tr key={u.userId ?? u.id} className="hover:bg-plum/2 transition-colors">
                                        <td className="px-5 py-3.5 text-plum/30 text-xs">{i + 1}</td>
                                        <td className="px-5 py-3.5 font-medium text-plum">{u.fullName ?? u.name}</td>
                                        <td className="px-5 py-3.5 text-plum/60">{u.email}</td>
                                        <td className="px-5 py-3.5">
                                            <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase tracking-wide ${ROLE_BADGE[u.role] ?? "bg-plum/10 text-plum"}`}>
                                                {u.role}
                                            </span>
                                        </td>
                                        <td className="px-5 py-3.5 text-xs text-plum/40">
                                            <span className="flex items-center gap-1">
                                                <Clock className="h-3 w-3" />
                                                {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "—"}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

function AnalyticsPage({ users }) {
    const admins  = users.filter((u) => u.role === "ADMIN").length;
    const mods    = users.filter((u) => u.role === "MODERATOR").length;
    const members = users.filter((u) => u.role === "USER").length;

    const last7Labels = Array.from({ length: 7 }, (_, i) => {
        const d = new Date(); d.setDate(d.getDate() - (6 - i));
        return d.toLocaleDateString("en-US", { weekday: "short" });
    });
    const last7Dates = Array.from({ length: 7 }, (_, i) => {
        const d = new Date(); d.setDate(d.getDate() - (6 - i));
        return d.toDateString();
    });

    const barData = {
        labels: ["Members", "Moderators", "Admins"],
        datasets: [{ data: [members, mods, admins], backgroundColor: ["#ff4f81", "#ff8c42", "#7a1f3d"], borderRadius: 8, borderSkipped: false }],
    };
    const lineData = {
        labels: last7Labels,
        datasets: [{
            label: "Registrations",
            data: last7Dates.map((d) => users.filter((u) => u.createdAt && new Date(u.createdAt).toDateString() === d).length),
            borderColor: "#ff4f81", backgroundColor: "rgba(255,79,129,0.08)",
            borderWidth: 2, pointBackgroundColor: "#ff4f81", pointRadius: 4, tension: 0.4, fill: true,
        }],
    };

    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Analytics</h1>
                <p className="mt-1 text-sm text-plum/50">Platform usage and growth statistics.</p>
            </div>

            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <StatCard icon={Users}       label="Total Users" value={users.length} color="from-sunset to-orange" />
                <StatCard icon={ShieldCheck} label="Admins"      value={admins}       color="from-burgundy to-plum" />
                <StatCard icon={Flag}        label="Moderators"  value={mods}         color="from-orange to-coral" />
                <StatCard icon={TrendingUp}  label="Members"     value={members}      color="from-coral to-sunset" />
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm">
                    <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Users by Role</p>
                    <div className="h-52"><Bar data={barData} options={CHART_OPTS} /></div>
                </div>
                <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm">
                    <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Registrations (Last 7 Days)</p>
                    <div className="h-52"><Line data={lineData} options={CHART_OPTS} /></div>
                </div>
            </div>
        </div>
    );
}

function ReportsPage() {
    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Reports</h1>
                <p className="mt-1 text-sm text-plum/50">User-submitted reports and flagged content.</p>
            </div>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                <StatCard icon={Flag}         label="Total Reports"    value="—" color="from-sunset to-orange" />
                <StatCard icon={AlertTriangle} label="Pending Review"  value="—" color="from-orange to-coral" />
                <StatCard icon={CheckCircle2}  label="Resolved"        value="—" color="from-coral to-sunset" />
            </div>
            <div className="rounded-2xl border border-dashed border-plum/15 bg-white p-12 text-center shadow-sm">
                <Flag className="mx-auto h-10 w-10 text-plum/15" />
                <p className="mt-3 text-sm font-semibold text-plum/40">Reports endpoint not yet available</p>
                <p className="mt-1 text-xs text-plum/25">Will populate once the backend team adds <code className="font-mono">GET /api/admin/reports</code></p>
            </div>
        </div>
    );
}

export default function AdminDashboard() {
    const { users, loading, error } = useUsers();
    return <Overview users={users} />;
}

export function AdminUsers() {
    const { users, loading, error } = useUsers();
    return <UsersPage users={users} loading={loading} error={error} />;
}

export function AdminAnalytics() {
    const { users } = useUsers();
    return <AnalyticsPage users={users} />;
}

export function AdminReports() {
    return <ReportsPage />;
}


