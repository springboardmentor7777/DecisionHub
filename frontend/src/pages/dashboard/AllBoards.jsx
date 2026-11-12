import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Clock, Search } from "lucide-react";
import { useBoards } from "../../hooks/useBoards";

const STATUS_STYLES = {
    active: "bg-sunset/10 text-sunset",
    closed: "bg-plum/10 text-plum/50",
};

function BoardCard({ title, description, category, options, status, duration, createdAt }) {
    const totalVotes = options?.reduce((sum, o) => sum + o.votes, 0) ?? 0;
    return (
        <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm hover:border-sunset/30 hover:shadow-md transition-all">
            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                    <p className="font-semibold text-plum leading-snug">{title}</p>
                    {description && <p className="mt-1 text-sm text-plum/50 line-clamp-2">{description}</p>}
                </div>
                <span className={`shrink-0 rounded-full px-2.5 py-1 text-[11px] font-semibold capitalize ${STATUS_STYLES[status]}`}>
                    {status}
                </span>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
                {options?.map((opt) => (
                    <div key={opt.label} className="flex items-center gap-1.5 rounded-lg border border-plum/8 bg-plum/3 px-3 py-1.5">
                        <span className="text-xs font-medium text-plum/70">{opt.label}</span>
                        <span className="text-[11px] font-bold text-sunset">{opt.votes} votes</span>
                    </div>
                ))}
            </div>

            <div className="mt-4 flex items-center justify-between border-t border-plum/5 pt-3 text-[11px] text-plum/30">
                <div className="flex items-center gap-3">
                    <span className="rounded-full bg-plum/6 px-2 py-0.5 font-medium text-plum/50">{category}</span>
                    <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{duration}</span>
                </div>
                <span>{new Date(createdAt).toLocaleDateString()}</span>
            </div>
        </div>
    );
}

export default function AllBoards() {
    const { boards } = useBoards();
    const [search, setSearch]   = useState("");
    const [filter, setFilter]   = useState("All");

    const categories = ["All", ...new Set(boards.map((b) => b.category))];

    const filtered = boards.filter((b) => {
        const matchCat    = filter === "All" || b.category === filter;
        const matchSearch = b.title.toLowerCase().includes(search.toLowerCase());
        return matchCat && matchSearch;
    });

    return (
        <div className="space-y-6 pb-10">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-extrabold text-plum">All Boards</h1>
                    <p className="mt-1 text-sm text-plum/50">{boards.length} board{boards.length !== 1 ? "s" : ""} created</p>
                </div>
                <Link to="/dashboard/boards/new"
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sunset to-orange px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-sunset/25 transition hover:brightness-105">
                    <Plus className="h-4 w-4" /> New Board
                </Link>
            </div>

            {boards.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-plum/15 bg-white py-20 text-center">
                    <p className="text-4xl">📊</p>
                    <p className="mt-3 text-base font-semibold text-plum/50">No boards yet</p>
                    <p className="mt-1 text-sm text-plum/30">Create a board and let your community decide</p>
                    <Link to="/dashboard/boards/new"
                        className="mt-5 rounded-xl bg-gradient-to-r from-sunset to-orange px-5 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-105">
                        Create First Board
                    </Link>
                </div>
            ) : (
                <>
                    {/* Search + Filter */}
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                        <div className="relative flex-1 max-w-sm">
                            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-plum/30" />
                            <input type="search" value={search} onChange={(e) => setSearch(e.target.value)}
                                placeholder="Search boards…"
                                className="w-full rounded-xl border border-plum/10 bg-white py-2.5 pl-9 pr-4 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                        </div>
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
                    </div>

                    {filtered.length === 0 ? (
                        <p className="py-10 text-center text-sm text-plum/40">No boards match your search.</p>
                    ) : (
                        <div className="grid gap-4 sm:grid-cols-2">
                            {filtered.map((b) => <BoardCard key={b.id} {...b} />)}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
