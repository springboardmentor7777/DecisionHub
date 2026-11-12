import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
    Vote, GitCompare, MessageSquare, Plus,
    Clock, CheckCircle2, Star, TrendingUp,
} from "lucide-react";
import {
    Chart as ChartJS,
    ArcElement, Tooltip, Legend,
    CategoryScale, LinearScale, BarElement,
    PointElement, LineElement, Filler,
} from "chart.js";
import { Doughnut, Bar, Line } from "react-chartjs-2";
import { useAuth } from "../../context/AuthContext";
import { useBoards } from "../../hooks/useBoards";

ChartJS.register(
    ArcElement, Tooltip, Legend,
    CategoryScale, LinearScale, BarElement,
    PointElement, LineElement, Filler
);

// ── localStorage helpers ──────────────────────────────────────────────────────
const loadKey = (key) => { try { return JSON.parse(localStorage.getItem(key)) ?? []; } catch { return []; } };
const saveKey  = (key, data) => localStorage.setItem(key, JSON.stringify(data));

// ── Quick Actions ─────────────────────────────────────────────────────────────
const QUICK_ACTIONS = [
    { icon: Plus,          label: "New Board",       to: "/dashboard/boards/new",  gradient: "from-sunset to-orange" },
    { icon: Vote,          label: "Start a Poll",    to: "/dashboard/polls/new",   gradient: "from-coral to-sunset" },
    { icon: GitCompare,    label: "Compare Options", to: "/dashboard/compare/new", gradient: "from-orange to-coral" },
    { icon: MessageSquare, label: "Open Discussion", to: "/dashboard/discuss/new", gradient: "from-burgundy to-plum" },
];

// ── Chart defaults ────────────────────────────────────────────────────────────
const CHART_COLORS = ["#ff4f81", "#ff8c42", "#ff6b5b", "#7a1f3d", "#3b0d45", "#ffb347"];

const baseChartOpts = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
};

// ── Sub-components ────────────────────────────────────────────────────────────
function StatCard({ icon: Icon, label, value, color }) {
    return (
        <div className="relative overflow-hidden rounded-2xl bg-white p-5 shadow-sm border border-plum/6 hover:shadow-md transition-shadow">
            <div className={`absolute -right-4 -top-4 h-20 w-20 rounded-full bg-gradient-to-br ${color} opacity-10`} />
            <div className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${color} shadow-md`}>
                <Icon className="h-5 w-5 text-white" />
            </div>
            <p className="mt-4 text-2xl font-extrabold text-plum">{value}</p>
            <p className="text-sm font-medium text-plum/70">{label}</p>
        </div>
    );
}

const STATUS_STYLES = {
    active: "bg-sunset/10 text-sunset",
    closed: "bg-plum/10 text-plum/50",
};

function BoardCard({ title, category, options, status, createdAt }) {
    const totalVotes = options?.reduce((sum, o) => sum + o.votes, 0) ?? 0;
    return (
        <div className="flex items-start justify-between gap-4 rounded-xl border border-plum/8 bg-white p-4 hover:border-sunset/30 hover:shadow-sm transition-all">
            <div className="min-w-0">
                <p className="truncate text-sm font-semibold text-plum">{title}</p>
                <div className="mt-1.5 flex flex-wrap items-center gap-2">
                    <span className="rounded-full bg-plum/6 px-2 py-0.5 text-[11px] font-medium text-plum/60">{category}</span>
                    <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold capitalize ${STATUS_STYLES[status]}`}>{status}</span>
                </div>
            </div>
            <div className="shrink-0 text-right">
                <p className="text-sm font-bold text-plum">{totalVotes}</p>
                <p className="text-[11px] text-plum/40">votes</p>
                <p className="mt-1 flex items-center justify-end gap-1 text-[11px] text-plum/40">
                    <Clock className="h-3 w-3" />{new Date(createdAt).toLocaleDateString()}
                </p>
            </div>
        </div>
    );
}

function PollCard({ poll, onVote }) {
    const totalVotes = poll.options.reduce((s, o) => s + o.votes, 0);
    return (
        <div className="rounded-xl border border-plum/8 bg-white p-4 hover:border-sunset/30 transition-all">
            <div className="flex items-start justify-between gap-2">
                <p className="text-sm font-semibold text-plum leading-snug">{poll.question}</p>
                {poll.voted && (
                    <span className="flex shrink-0 items-center gap-1 rounded-full bg-sunset/10 px-2 py-0.5 text-[11px] font-semibold text-sunset">
                        <CheckCircle2 className="h-3 w-3" /> Voted
                    </span>
                )}
            </div>
            <div className="mt-3 space-y-2">
                {poll.options.map((opt, i) => {
                    const pct = totalVotes > 0 ? Math.round((opt.votes / totalVotes) * 100) : 0;
                    return (
                        <button
                            key={i}
                            onClick={() => !poll.voted && onVote(poll.id, i)}
                            disabled={poll.voted}
                            className={`relative w-full overflow-hidden rounded-lg border px-3 py-2 text-left text-xs font-medium transition
                                ${poll.voted
                                    ? "border-plum/8 cursor-default"
                                    : "border-plum/12 hover:border-sunset hover:bg-sunset/5 cursor-pointer"}`}
                        >
                            {poll.voted && (
                                <span
                                    className="absolute inset-y-0 left-0 bg-sunset/10 transition-all"
                                    style={{ width: `${pct}%` }}
                                />
                            )}
                            <span className="relative flex justify-between">
                                <span className={poll.voted ? "text-plum/70" : "text-plum/70"}>{opt.label}</span>
                                {poll.voted && <span className="font-bold text-sunset">{pct}%</span>}
                            </span>
                        </button>
                    );
                })}
            </div>
            <p className="mt-2 text-[11px] text-plum/30">{totalVotes} vote{totalVotes !== 1 ? "s" : ""}</p>
        </div>
    );
}

function DiscussionCard({ title, category, tags, replies, createdAt }) {
    return (
        <div className="rounded-xl border border-plum/8 bg-white p-4 hover:border-sunset/30 transition-all">
            <p className="text-sm font-semibold text-plum leading-snug">{title}</p>
            <div className="mt-2 flex flex-wrap items-center gap-2">
                <span className="rounded-full bg-plum/6 px-2 py-0.5 text-[11px] font-medium text-plum/60">{category}</span>
                {tags?.slice(0, 2).map((t) => (
                    <span key={t} className="rounded-full bg-sunset/8 px-2 py-0.5 text-[11px] text-sunset">#{t}</span>
                ))}
            </div>
            <div className="mt-2 flex items-center justify-between text-[11px] text-plum/40">
                <span className="flex items-center gap-1"><MessageSquare className="h-3 w-3" />{replies} replies</span>
                <span>{new Date(createdAt).toLocaleDateString()}</span>
            </div>
        </div>
    );
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function UserDashboard() {
    const { user } = useAuth();
    const { boards } = useBoards();
    const [polls, setPolls]             = useState(() => loadKey("dh_polls"));
    const [comparisons]                 = useState(() => loadKey("dh_comparisons"));
    const [discussions]                 = useState(() => loadKey("dh_discussions"));

    const hour     = new Date().getHours();
    const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

    // Vote on a poll — update localStorage + state
    const handleVote = (pollId, optionIndex) => {
        const updated = polls.map((p) => {
            if (p.id !== pollId) return p;
            const options = p.options.map((o, i) =>
                i === optionIndex ? { ...o, votes: o.votes + 1 } : o
            );
            return { ...p, options, voted: true };
        });
        saveKey("dh_polls", updated);
        setPolls(updated);
    };

    // Stats derived from real data
    const stats = [
        { icon: Vote,          label: "Polls Created",    value: polls.length,       color: "from-sunset to-orange" },
        { icon: GitCompare,    label: "Comparisons",      value: comparisons.length, color: "from-coral to-sunset" },
        { icon: MessageSquare, label: "Discussions",      value: discussions.length, color: "from-orange to-coral" },
        { icon: Star,          label: "Boards Created",   value: boards.length,      color: "from-burgundy to-plum" },
    ];

    // ── Chart data ────────────────────────────────────────────────────────────

    // Doughnut — activity breakdown
    const activityData = {
        labels: ["Boards", "Polls", "Comparisons", "Discussions"],
        datasets: [{
            data: [boards.length, polls.length, comparisons.length, discussions.length],
            backgroundColor: CHART_COLORS,
            borderWidth: 0,
            hoverOffset: 6,
        }],
    };

    // Bar — boards by category
    const boardCategories = boards.reduce((acc, b) => {
        acc[b.category] = (acc[b.category] || 0) + 1;
        return acc;
    }, {});
    const barData = {
        labels: Object.keys(boardCategories).length ? Object.keys(boardCategories) : ["No data"],
        datasets: [{
            label: "Boards",
            data: Object.keys(boardCategories).length ? Object.values(boardCategories) : [0],
            backgroundColor: "#ff4f81",
            borderRadius: 8,
            borderSkipped: false,
        }],
    };
    const barOpts = {
        ...baseChartOpts,
        scales: {
            x: { grid: { display: false }, ticks: { color: "#3b0d4580", font: { size: 11 } } },
            y: { grid: { color: "#3b0d4508" }, ticks: { color: "#3b0d4580", font: { size: 11 }, stepSize: 1 } },
        },
    };

    // Line — activity over last 7 days
    const last7 = Array.from({ length: 7 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (6 - i));
        return d.toLocaleDateString("en-US", { weekday: "short" });
    });
    const last7Dates = Array.from({ length: 7 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (6 - i));
        return d.toDateString();
    });
    const allItems = [
        ...boards.map((b) => b.createdAt),
        ...polls.map((p) => p.createdAt),
        ...comparisons.map((c) => c.createdAt),
        ...discussions.map((d) => d.createdAt),
    ];
    const lineData = {
        labels: last7,
        datasets: [{
            label: "Activity",
            data: last7Dates.map((d) => allItems.filter((a) => new Date(a).toDateString() === d).length),
            borderColor: "#ff4f81",
            backgroundColor: "rgba(255,79,129,0.08)",
            borderWidth: 2,
            pointBackgroundColor: "#ff4f81",
            pointRadius: 4,
            tension: 0.4,
            fill: true,
        }],
    };
    const lineOpts = {
        ...baseChartOpts,
        scales: {
            x: { grid: { display: false }, ticks: { color: "#3b0d4580", font: { size: 11 } } },
            y: { grid: { color: "#3b0d4508" }, ticks: { color: "#3b0d4580", font: { size: 11 }, stepSize: 1 } },
        },
    };

    const hasAnyActivity = boards.length + polls.length + comparisons.length + discussions.length > 0;

    return (
        <div className="space-y-8 pb-10">

            {/* Welcome banner */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-plum via-burgundy to-plum p-6 text-white shadow-lg sm:p-8">
                <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-sunset/20 blur-3xl" />
                <div className="pointer-events-none absolute -bottom-10 left-1/3 h-40 w-40 rounded-full bg-orange/15 blur-3xl" />
                <div className="relative z-10">
                    <p className="text-sm font-medium text-ivory/70">{greeting} 👋</p>
                    <h1 className="mt-1 text-2xl font-extrabold tracking-tight sm:text-3xl">
                        Welcome back, <span className="text-coral">{user?.name.split(" ")[0]}!</span>
                    </h1>
                    <p className="mt-2 max-w-lg text-sm text-ivory/70">
                        {hasAnyActivity
                            ? <>You have <span className="font-semibold text-white">{boards.length} board{boards.length !== 1 ? "s" : ""}</span>, <span className="font-semibold text-white">{polls.length} poll{polls.length !== 1 ? "s" : ""}</span> and <span className="font-semibold text-white">{discussions.length} discussion{discussions.length !== 1 ? "s" : ""}</span> created.</>
                            : "You're all set! Start by creating your first board, poll or discussion."}
                    </p>
                    <Link
                        to="/dashboard/boards/new"
                        className="mt-5 inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-sunset to-orange px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-sunset/30 transition hover:brightness-110 active:scale-[0.98]"
                    >
                        <Plus className="h-4 w-4" /> Create New Board
                    </Link>
                </div>
            </div>

            {/* Stats */}
            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Your Activity</h2>
                <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                    {stats.map((s) => <StatCard key={s.label} {...s} />)}
                </div>
            </div>

            {/* Quick Actions */}
            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Quick Actions</h2>
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                    {QUICK_ACTIONS.map(({ icon: Icon, label, to, gradient }) => (
                        <Link key={label} to={to}
                            className="flex flex-col items-center gap-2.5 rounded-2xl border border-plum/8 bg-white p-4 text-center transition hover:border-sunset/30 hover:shadow-md">
                            <span className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${gradient} shadow-md`}>
                                <Icon className="h-5 w-5 text-white" />
                            </span>
                            <span className="text-xs font-semibold text-plum/80">{label}</span>
                        </Link>
                    ))}
                </div>
            </div>

            {/* Charts */}
            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Analytics</h2>
                <div className="grid gap-4 sm:grid-cols-3">

                    {/* Doughnut */}
                    <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm">
                        <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Activity Breakdown</p>
                        {hasAnyActivity ? (
                            <>
                                <div className="h-40"><Doughnut data={activityData} options={{ ...baseChartOpts, plugins: { legend: { display: true, position: "bottom", labels: { font: { size: 11 }, color: "#3b0d45", boxWidth: 10, padding: 8 } } } }} /></div>
                            </>
                        ) : <EmptyChart label="No activity yet" />}
                    </div>

                    {/* Bar */}
                    <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm">
                        <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Boards by Category</p>
                        {boards.length > 0 ? (
                            <div className="h-44"><Bar data={barData} options={barOpts} /></div>
                        ) : <EmptyChart label="No boards yet" />}
                    </div>

                    {/* Line */}
                    <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm">
                        <p className="mb-3 text-xs font-bold uppercase tracking-widest text-plum/40">Activity (Last 7 Days)</p>
                        <div className="h-44"><Line data={lineData} options={lineOpts} /></div>
                    </div>
                </div>
            </div>

            {/* Recent Boards + Active Polls */}
            <div className="grid gap-6 lg:grid-cols-2">

                {/* Recent Boards */}
                <div>
                    <div className="mb-4 flex items-center justify-between">
                        <h2 className="text-base font-bold text-plum">Recent Boards</h2>
                        <Link to="/dashboard/boards" className="text-xs font-semibold text-coral hover:text-burgundy">View all →</Link>
                    </div>
                    {boards.length === 0 ? (
                        <EmptyState icon="📊" label="No boards yet" to="/dashboard/boards/new" cta="Create your first board" />
                    ) : (
                        <div className="space-y-3">
                            {boards.slice(0, 4).map((b) => <BoardCard key={b.id} {...b} />)}
                        </div>
                    )}
                </div>

                {/* Active Polls */}
                <div>
                    <div className="mb-4 flex items-center justify-between">
                        <h2 className="text-base font-bold text-plum">Active Polls</h2>
                        <Link to="/dashboard/polls/new" className="text-xs font-semibold text-coral hover:text-burgundy">New poll →</Link>
                    </div>
                    {polls.length === 0 ? (
                        <EmptyState icon="🗳️" label="No polls yet" to="/dashboard/polls/new" cta="Start your first poll" />
                    ) : (
                        <div className="space-y-3">
                            {polls.slice(0, 3).map((p) => <PollCard key={p.id} poll={p} onVote={handleVote} />)}
                        </div>
                    )}
                </div>
            </div>

            {/* Discussions */}
            {discussions.length > 0 && (
                <div>
                    <div className="mb-4 flex items-center justify-between">
                        <h2 className="text-base font-bold text-plum">Recent Discussions</h2>
                        <Link to="/dashboard/discuss/new" className="text-xs font-semibold text-coral hover:text-burgundy">New discussion →</Link>
                    </div>
                    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                        {discussions.slice(0, 3).map((d) => <DiscussionCard key={d.id} {...d} />)}
                    </div>
                </div>
            )}
        </div>
    );
}

function EmptyChart({ label }) {
    return (
        <div className="flex h-44 items-center justify-center rounded-xl border border-dashed border-plum/15">
            <p className="text-xs text-plum/30">{label}</p>
        </div>
    );
}

function EmptyState({ icon, label, to, cta }) {
    return (
        <div className="rounded-xl border border-dashed border-plum/15 bg-white p-8 text-center">
            <p className="text-2xl">{icon}</p>
            <p className="mt-2 text-sm font-medium text-plum/40">{label}</p>
            <Link to={to} className="mt-2 inline-block text-xs font-semibold text-coral hover:text-burgundy">{cta} →</Link>
        </div>
    );
}
