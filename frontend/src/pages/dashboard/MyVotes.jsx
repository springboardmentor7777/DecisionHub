import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, CheckCircle2, Clock } from "lucide-react";

const loadPolls = () => { try { return JSON.parse(localStorage.getItem("dh_polls")) ?? []; } catch { return []; } };
const savePolls = (d) => localStorage.setItem("dh_polls", JSON.stringify(d));

function PollCard({ poll, onVote }) {
    const totalVotes = poll.options.reduce((s, o) => s + o.votes, 0);
    return (
        <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm hover:shadow-md transition-all">
            <div className="flex items-start justify-between gap-3">
                <div>
                    <p className="font-semibold text-plum leading-snug">{poll.question}</p>
                    <div className="mt-1.5 flex items-center gap-2 text-xs text-plum/40">
                        <span className="rounded-full bg-plum/6 px-2 py-0.5 font-medium">{poll.type}</span>
                        <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{poll.duration}</span>
                        <span>{new Date(poll.createdAt).toLocaleDateString()}</span>
                    </div>
                </div>
                {poll.voted && (
                    <span className="flex shrink-0 items-center gap-1 rounded-full bg-sunset/10 px-2.5 py-1 text-xs font-semibold text-sunset">
                        <CheckCircle2 className="h-3.5 w-3.5" /> Voted
                    </span>
                )}
            </div>

            <div className="mt-4 space-y-2">
                {poll.options.map((opt, i) => {
                    const pct = totalVotes > 0 ? Math.round((opt.votes / totalVotes) * 100) : 0;
                    return (
                        <button key={i} onClick={() => !poll.voted && onVote(poll.id, i)}
                            disabled={poll.voted}
                            className={`relative w-full overflow-hidden rounded-xl border px-4 py-2.5 text-left text-sm font-medium transition
                                ${poll.voted ? "border-plum/8 cursor-default" : "border-plum/12 hover:border-sunset hover:bg-sunset/5 cursor-pointer"}`}>
                            {poll.voted && (
                                <span className="absolute inset-y-0 left-0 rounded-xl bg-sunset/10 transition-all" style={{ width: `${pct}%` }} />
                            )}
                            <span className="relative flex justify-between">
                                <span className="text-plum/80">{opt.label}</span>
                                {poll.voted && <span className="font-bold text-sunset">{pct}%</span>}
                            </span>
                        </button>
                    );
                })}
            </div>
            <p className="mt-3 text-xs text-plum/30">{totalVotes} vote{totalVotes !== 1 ? "s" : ""} total</p>
        </div>
    );
}

export default function MyVotes() {
    const [polls, setPolls] = useState(loadPolls);

    const handleVote = (pollId, optionIndex) => {
        const updated = polls.map((p) => {
            if (p.id !== pollId) return p;
            const options = p.options.map((o, i) => i === optionIndex ? { ...o, votes: o.votes + 1 } : o);
            return { ...p, options, voted: true };
        });
        savePolls(updated);
        setPolls(updated);
    };

    const voted    = polls.filter((p) => p.voted);
    const pending  = polls.filter((p) => !p.voted);

    return (
        <div className="space-y-8 pb-10">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-extrabold text-plum">My Polls</h1>
                    <p className="mt-1 text-sm text-plum/50">{polls.length} poll{polls.length !== 1 ? "s" : ""} created</p>
                </div>
                <Link to="/dashboard/polls/new"
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-coral to-sunset px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-sunset/25 transition hover:brightness-105">
                    <Plus className="h-4 w-4" /> New Poll
                </Link>
            </div>

            {polls.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-plum/15 bg-white py-20 text-center">
                    <p className="text-4xl">🗳️</p>
                    <p className="mt-3 text-base font-semibold text-plum/50">No polls yet</p>
                    <p className="mt-1 text-sm text-plum/30">Create a poll and let your community vote</p>
                    <Link to="/dashboard/polls/new"
                        className="mt-5 rounded-xl bg-gradient-to-r from-coral to-sunset px-5 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-105">
                        Start a Poll
                    </Link>
                </div>
            ) : (
                <>
                    {pending.length > 0 && (
                        <div>
                            <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Awaiting Your Vote ({pending.length})</h2>
                            <div className="grid gap-4 sm:grid-cols-2">
                                {pending.map((p) => <PollCard key={p.id} poll={p} onVote={handleVote} />)}
                            </div>
                        </div>
                    )}
                    {voted.length > 0 && (
                        <div>
                            <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Voted ({voted.length})</h2>
                            <div className="grid gap-4 sm:grid-cols-2">
                                {voted.map((p) => <PollCard key={p.id} poll={p} onVote={handleVote} />)}
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
