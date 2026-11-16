import React, { useState, useEffect } from "react";
import {
    FileWarning, MessageSquare, ShieldCheck, CheckCircle2,
    XCircle, Clock, AlertTriangle, Flag,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import api from "../../api/axios.js";
import { getFlagged, updateFlagStatus } from "../../hooks/useFlagged";

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

const STATUS_STYLES = {
    OPEN:   "bg-sunset/10 text-sunset",
    CLOSED: "bg-plum/10 text-plum/50",
    DRAFT:  "bg-orange/10 text-orange",
};

const VISIBILITY_STYLES = {
    PUBLIC:  "bg-emerald-50 text-emerald-600",
    PRIVATE: "bg-plum/8 text-plum/50",
};

function useDecisions() {
    const [decisions, setDecisions] = useState([]);
    const [loading, setLoading]     = useState(true);
    const [error, setError]         = useState("");
    useEffect(() => {
        api.get("/api/moderator/decisions")
            .then(({ data }) => setDecisions(Array.isArray(data) ? data : []))
            .catch(() => setError("Could not load decisions from server."))
            .finally(() => setLoading(false));
    }, []);
    return { decisions, loading, error };
}

function DecisionsTable({ decisions, loading, error }) {
    if (loading) return <div className="flex items-center justify-center py-16 text-sm text-plum/40">Loading…</div>;
    if (error)   return <div className="flex items-center justify-center gap-2 py-16 text-sm text-burgundy"><AlertTriangle className="h-4 w-4" />{error}</div>;
    if (decisions.length === 0) return (
        <div className="flex flex-col items-center justify-center gap-2 py-16">
            <ShieldCheck className="h-8 w-8 text-plum/20" />
            <p className="text-sm text-plum/40">No decisions found.</p>
        </div>
    );
    return (
        <div className="overflow-x-auto">
            <table className="w-full text-sm">
                <thead>
                    <tr className="border-b border-plum/8 bg-plum/3">
                        <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Title</th>
                        <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Status</th>
                        <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Visibility</th>
                        <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Created</th>
                        <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Ends</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-plum/5">
                    {decisions.map((d) => (
                        <tr key={d.decisionId ?? d.id} className="hover:bg-plum/2 transition-colors">
                            <td className="px-5 py-3.5">
                                <p className="font-medium text-plum truncate max-w-[200px]">{d.title}</p>
                                {d.description && <p className="text-xs text-plum/40 truncate max-w-[200px]">{d.description}</p>}
                            </td>
                            <td className="px-5 py-3.5">
                                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase tracking-wide ${STATUS_STYLES[d.status] ?? "bg-plum/10 text-plum"}`}>
                                    {d.status}
                                </span>
                            </td>
                            <td className="px-5 py-3.5">
                                <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold ${VISIBILITY_STYLES[d.visibility] ?? "bg-plum/10 text-plum"}`}>
                                    {d.visibility}
                                </span>
                            </td>
                            <td className="px-5 py-3.5 text-xs text-plum/40">
                                <span className="flex items-center gap-1">
                                    <Clock className="h-3 w-3" />
                                    {d.createdAt ? new Date(d.createdAt).toLocaleDateString() : "—"}
                                </span>
                            </td>
                            <td className="px-5 py-3.5 text-xs text-plum/40">
                                {d.endsAt ? new Date(d.endsAt).toLocaleDateString() : <span className="text-plum/20">No end date</span>}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

// ── OVERVIEW ──────────────────────────────────────────────────────────────────
export default function ModeratorDashboard() {
    const { user } = useAuth();
    const { decisions, loading, error } = useDecisions();
    const hour = new Date().getHours();
    const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

    const open   = decisions.filter((d) => d.status === "OPEN").length;
    const closed = decisions.filter((d) => d.status === "CLOSED").length;
    const flagged = getFlagged();

    return (
        <div className="space-y-8 pb-10">
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-orange via-plum to-orange p-6 text-white shadow-lg sm:p-8">
                <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-sunset/20 blur-3xl" />
                <div className="relative z-10">
                    <p className="text-sm font-medium text-ivory/70">{greeting} 👋</p>
                    <h1 className="mt-1 text-2xl font-extrabold tracking-tight sm:text-3xl">
                        Moderator Panel, <span className="text-coral">{user?.name?.split(" ")[0]}!</span>
                    </h1>
                    <p className="mt-2 text-sm text-ivory/70">Review decisions, manage discussions and keep the community healthy.</p>
                </div>
            </div>

            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Content Overview</h2>
                <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                    <StatCard icon={ShieldCheck}  label="Total Decisions" value={decisions.length}                              color="from-sunset to-orange" />
                    <StatCard icon={CheckCircle2} label="Open"            value={open}                                          color="from-coral to-sunset" />
                    <StatCard icon={XCircle}      label="Closed"          value={closed}                                        color="from-burgundy to-plum" />
                    <StatCard icon={Flag}         label="Flagged"         value={flagged.filter(f => f.status === "PENDING").length} color="from-orange to-coral" />
                </div>
            </div>

            <div>
                <h2 className="mb-4 text-base font-bold text-plum">Recent Decisions</h2>
                <div className="rounded-2xl border border-plum/8 bg-white shadow-sm overflow-hidden">
                    <DecisionsTable decisions={decisions.slice(0, 5)} loading={loading} error={error} />
                </div>
            </div>
        </div>
    );
}

export function ModeratorFlagged() {
    const [flags, setFlags]   = useState(() => getFlagged());
    const [filter, setFilter] = useState("ALL");

    const filtered = filter === "ALL" ? flags : flags.filter((f) => f.status === filter);
    const pending  = flags.filter((f) => f.status === "PENDING").length;
    const resolved = flags.filter((f) => f.status === "RESOLVED").length;
    const rejected = flags.filter((f) => f.status === "REJECTED").length;

    const handleAction = (id, status) => {
        const updated = updateFlagStatus(id, status);
        setFlags(updated);
    };

    const STATUS_BADGE = {
        PENDING:  "bg-orange/10 text-orange",
        RESOLVED: "bg-emerald-50 text-emerald-600",
        REJECTED: "bg-plum/10 text-plum/50",
    };
    const TYPE_BADGE = {
        BOARD:      "bg-sunset/10 text-sunset",
        DISCUSSION: "bg-burgundy/10 text-burgundy",
    };

    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Flagged Content</h1>
                <p className="mt-1 text-sm text-plum/50">Content reported by users for review.</p>
            </div>

            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                <StatCard icon={Flag}          label="Total Flagged" value={flags.length} color="from-sunset to-orange" />
                <StatCard icon={AlertTriangle} label="Pending"       value={pending}      color="from-orange to-coral" />
                <StatCard icon={CheckCircle2}  label="Resolved"      value={resolved}     color="from-coral to-sunset" />
                <StatCard icon={XCircle}       label="Rejected"      value={rejected}     color="from-burgundy to-plum" />
            </div>

            <div className="flex flex-wrap gap-2">
                {["ALL", "PENDING", "RESOLVED", "REJECTED"].map((s) => (
                    <button key={s} onClick={() => setFilter(s)}
                        className={`rounded-xl px-4 py-2 text-xs font-bold uppercase tracking-wide transition
                            ${filter === s ? "bg-plum text-white" : "bg-white border border-plum/10 text-plum/60 hover:border-plum/30"}`}>
                        {s}
                    </button>
                ))}
            </div>

            <div className="rounded-2xl border border-plum/8 bg-white shadow-sm overflow-hidden">
                {filtered.length === 0 ? (
                    <div className="flex flex-col items-center justify-center gap-2 py-16">
                        <Flag className="h-8 w-8 text-plum/15" />
                        <p className="text-sm text-plum/40">
                            {flags.length === 0 ? "No content has been flagged yet." : "No items match this filter."}
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b border-plum/8 bg-plum/3">
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Content</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Type</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Reason</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Status</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Flagged</th>
                                    <th className="px-5 py-3 text-left text-xs font-bold uppercase tracking-widest text-plum/40">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-plum/5">
                                {filtered.map((f) => (
                                    <tr key={f.id} className="hover:bg-plum/2 transition-colors">
                                        <td className="px-5 py-3.5">
                                            <p className="font-medium text-plum truncate max-w-[160px]">{f.title}</p>
                                        </td>
                                        <td className="px-5 py-3.5">
                                            <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase ${TYPE_BADGE[f.targetType] ?? "bg-plum/10 text-plum"}`}>
                                                {f.targetType}
                                            </span>
                                        </td>
                                        <td className="px-5 py-3.5 text-xs text-plum/60 max-w-[180px]">
                                            <p className="truncate">{f.reason}</p>
                                        </td>
                                        <td className="px-5 py-3.5">
                                            <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase ${STATUS_BADGE[f.status] ?? "bg-plum/10 text-plum"}`}>
                                                {f.status}
                                            </span>
                                        </td>
                                        <td className="px-5 py-3.5 text-xs text-plum/40">
                                            <span className="flex items-center gap-1">
                                                <Clock className="h-3 w-3" />
                                                {new Date(f.flaggedAt).toLocaleDateString()}
                                            </span>
                                        </td>
                                        <td className="px-5 py-3.5">
                                            {f.status === "PENDING" ? (
                                                <div className="flex gap-2">
                                                    <button onClick={() => handleAction(f.id, "RESOLVED")}
                                                        className="rounded-lg bg-emerald-50 px-3 py-1.5 text-[11px] font-bold text-emerald-600 hover:bg-emerald-100 transition">
                                                        Resolve
                                                    </button>
                                                    <button onClick={() => handleAction(f.id, "REJECTED")}
                                                        className="rounded-lg bg-plum/8 px-3 py-1.5 text-[11px] font-bold text-plum/50 hover:bg-plum/15 transition">
                                                        Reject
                                                    </button>
                                                </div>
                                            ) : (
                                                <span className="text-xs text-plum/30">—</span>
                                            )}
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

// ── DISCUSSIONS ───────────────────────────────────────────────────────────────
export function ModeratorDiscussions() {
    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Discussions</h1>
                <p className="mt-1 text-sm text-plum/50">Monitor and moderate community discussions.</p>
            </div>
            <div className="rounded-2xl border border-dashed border-plum/15 bg-white p-16 text-center shadow-sm">
                <MessageSquare className="mx-auto h-10 w-10 text-plum/15" />
                <p className="mt-3 text-sm font-semibold text-plum/40">Discussions endpoint not yet available</p>
                <p className="mt-1 text-xs text-plum/25">Will populate once the backend adds <code className="font-mono">GET /api/moderator/discussions</code></p>
            </div>
        </div>
    );
}

// ── DECISIONS ─────────────────────────────────────────────────────────────────
export function ModeratorDecisions() {
    const { decisions, loading, error } = useDecisions();
    const [statusFilter, setStatusFilter] = useState("ALL");

    const filtered = statusFilter === "ALL" ? decisions : decisions.filter((d) => d.status === statusFilter);

    return (
        <div className="space-y-6 pb-10">
            <div>
                <h1 className="text-2xl font-extrabold text-plum">Decisions</h1>
                <p className="mt-1 text-sm text-plum/50">All platform decisions available for moderation.</p>
            </div>

            <div className="flex flex-wrap gap-2">
                {["ALL", "OPEN", "CLOSED", "DRAFT"].map((s) => (
                    <button key={s} onClick={() => setStatusFilter(s)}
                        className={`rounded-xl px-4 py-2 text-xs font-bold uppercase tracking-wide transition
                            ${statusFilter === s ? "bg-plum text-white" : "bg-white border border-plum/10 text-plum/60 hover:border-plum/30"}`}>
                        {s}
                    </button>
                ))}
            </div>

            <div className="rounded-2xl border border-plum/8 bg-white shadow-sm overflow-hidden">
                <DecisionsTable decisions={filtered} loading={loading} error={error} />
            </div>
        </div>
    );
}
