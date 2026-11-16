import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, MessageSquare, Clock, Tag, Flag } from "lucide-react";
import { flagContent } from "../../hooks/useFlagged";

const loadDiscussions = () => { try { return JSON.parse(localStorage.getItem("dh_discussions")) ?? []; } catch { return []; } };

const CATEGORY_COLORS = {
    Tech: "bg-sunset/10 text-sunset",
    Social: "bg-coral/10 text-coral",
    Work: "bg-orange/10 text-orange",
    Lifestyle: "bg-burgundy/10 text-burgundy",
    Education: "bg-plum/10 text-plum",
    Finance: "bg-green-100 text-green-700",
    Health: "bg-blue-100 text-blue-600",
    Other: "bg-plum/6 text-plum/50",
};

function DiscussionCard({ id, title, body, category, tags, replies, createdAt }) {
    const [flagged, setFlagged] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [reason, setReason] = useState("");

    const handleFlag = () => {
        if (!reason.trim()) return;
        const ok = flagContent({ targetType: "DISCUSSION", targetId: id, title, reason: reason.trim() });
        setFlagged(ok);
        setShowModal(false);
        setReason("");
    };

    return (
        <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm hover:border-sunset/30 hover:shadow-md transition-all">
            {showModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-plum/40 backdrop-blur-sm" onClick={() => setShowModal(false)}>
                    <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
                        <h3 className="text-base font-bold text-plum">Flag this Discussion</h3>
                        <p className="mt-1 text-xs text-plum/50 truncate">{title}</p>
                        <textarea
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            placeholder="Describe why you're flagging this…"
                            rows={3}
                            className="mt-4 w-full rounded-xl border border-plum/10 px-3 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none focus:border-sunset focus:ring-2 focus:ring-sunset/20 resize-none"
                        />
                        <div className="mt-4 flex gap-2">
                            <button onClick={() => setShowModal(false)} className="flex-1 rounded-xl border border-plum/10 py-2.5 text-sm font-medium text-plum/60 hover:bg-plum/5">Cancel</button>
                            <button onClick={handleFlag} disabled={!reason.trim()} className="flex-1 rounded-xl bg-burgundy py-2.5 text-sm font-semibold text-white disabled:opacity-40 hover:brightness-110">Submit Flag</button>
                        </div>
                    </div>
                </div>
            )}
            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                    <p className="font-semibold text-plum leading-snug">{title}</p>
                    {body && <p className="mt-1.5 text-sm text-plum/50 line-clamp-2">{body}</p>}
                </div>
                <div className="flex shrink-0 items-center gap-2">
                    <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${CATEGORY_COLORS[category] ?? "bg-plum/6 text-plum/50"}`}>
                        {category}
                    </span>
                    <button
                        onClick={() => !flagged && setShowModal(true)}
                        title={flagged ? "Already flagged" : "Flag this discussion"}
                        className={`rounded-lg p-1.5 transition ${flagged ? "text-burgundy cursor-default" : "text-plum/20 hover:text-burgundy hover:bg-burgundy/8"}`}
                    >
                        <Flag className="h-3.5 w-3.5" />
                    </button>
                </div>
            </div>

            {tags?.length > 0 && (
                <div className="mt-3 flex flex-wrap gap-1.5">
                    {tags.map((t) => (
                        <span key={t} className="flex items-center gap-1 rounded-full bg-sunset/8 px-2.5 py-0.5 text-[11px] font-medium text-sunset">
                            <Tag className="h-2.5 w-2.5" />#{t}
                        </span>
                    ))}
                </div>
            )}

            <div className="mt-3 flex items-center gap-3 text-[11px] text-plum/30 border-t border-plum/5 pt-3">
                <span className="flex items-center gap-1"><MessageSquare className="h-3 w-3" />{replies ?? 0} replies</span>
                <span>·</span>
                <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{new Date(createdAt).toLocaleDateString()}</span>
            </div>
        </div>
    );
}

export default function Discuss() {
    const [discussions] = useState(loadDiscussions);
    const [filter, setFilter] = useState("All");

    const categories = ["All", ...new Set(discussions.map((d) => d.category))];
    const filtered = filter === "All" ? discussions : discussions.filter((d) => d.category === filter);

    return (
        <div className="space-y-6 pb-10">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-extrabold text-plum">Discussions</h1>
                    <p className="mt-1 text-sm text-plum/50">{discussions.length} discussion{discussions.length !== 1 ? "s" : ""} started</p>
                </div>
                <Link to="/dashboard/discuss/new"
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-burgundy to-plum px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-plum/20 transition hover:brightness-110">
                    <Plus className="h-4 w-4" /> New Discussion
                </Link>
            </div>

            {discussions.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-plum/15 bg-white py-20 text-center">
                    <p className="text-4xl">💬</p>
                    <p className="mt-3 text-base font-semibold text-plum/50">No discussions yet</p>
                    <p className="mt-1 text-sm text-plum/30">Start a topic and get insights from your community</p>
                    <Link to="/dashboard/discuss/new"
                        className="mt-5 rounded-xl bg-gradient-to-r from-burgundy to-plum px-5 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-110">
                        Start a Discussion
                    </Link>
                </div>
            ) : (
                <>
                    {categories.length > 2 && (
                        <div className="flex flex-wrap gap-2">
                            {categories.map((cat) => (
                                <button key={cat} onClick={() => setFilter(cat)}
                                    className={`rounded-full border px-3 py-1.5 text-xs font-semibold transition
                                        ${filter === cat
                                            ? "border-sunset bg-sunset/10 text-sunset"
                                            : "border-plum/10 bg-white text-plum/60 hover:border-sunset/40 hover:text-sunset"}`}>
                                    {cat}
                                </button>
                            ))}
                        </div>
                    )}

                    <div className="grid gap-4 sm:grid-cols-2">
                        {filtered.map((d) => <DiscussionCard key={d.id} {...d} />)}
                    </div>

                    {filtered.length === 0 && (
                        <p className="text-center text-sm text-plum/40 py-10">No discussions in this category.</p>
                    )}
                </>
            )}
        </div>
    );
}
