
import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Clock, Search, Flag, Pencil, Trash2 } from "lucide-react";
import { useBoards } from "../../hooks/useBoards";
import { flagContent } from "../../hooks/useFlagged";

const STATUS_STYLES = {
    active: "bg-sunset/10 text-sunset",
    closed: "bg-plum/10 text-plum/50",
};

function BoardCard({ board, onDelete }) {
    const {
        id,
        title,
        description,
        category,
        options,
        status,
        duration,
        createdAt,
        visibility = "public",
    } = board;

    const totalVotes = options?.reduce((sum, option) => sum + option.votes, 0) ?? 0;

    const [flagged, setFlagged] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [reason, setReason] = useState("");

    const handleFlag = () => {
        if (!reason.trim()) return;

        const ok = flagContent({
            targetType: "BOARD",
            targetId: id,
            title,
            reason: reason.trim(),
        });

        setFlagged(ok);
        setShowModal(false);
        setReason("");
    };

    const handleDelete = () => {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${title}"?`
        );

        if (confirmed) {
            onDelete(id);
        }
    };

    return (
        <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm transition-all hover:border-sunset/30 hover:shadow-md">

            {showModal && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-plum/40 backdrop-blur-sm"
                    onClick={() => setShowModal(false)}
                >
                    <div
                        className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h3 className="text-base font-bold text-plum">
                            Flag this Board
                        </h3>

                        <p className="mt-1 truncate text-xs text-plum/50">
                            {title}
                        </p>

                        <textarea
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            placeholder="Describe why you're flagging this..."
                            rows={3}
                            className="mt-4 w-full resize-none rounded-xl border border-plum/10 px-3 py-2.5 text-sm text-plum outline-none focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />

                        <div className="mt-4 flex gap-2">
                            <button
                                onClick={() => setShowModal(false)}
                                className="flex-1 rounded-xl border border-plum/10 py-2.5 text-sm font-medium text-plum/60 hover:bg-plum/5"
                            >
                                Cancel
                            </button>

                            <button
                                onClick={handleFlag}
                                disabled={!reason.trim()}
                                className="flex-1 rounded-xl bg-burgundy py-2.5 text-sm font-semibold text-white hover:brightness-110 disabled:opacity-40"
                            >
                                Submit Flag
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                    <p className="font-semibold leading-snug text-plum">
                        {title}
                    </p>

                    {description && (
                        <p className="mt-1 line-clamp-2 text-sm text-plum/50">
                            {description}
                        </p>
                    )}
                </div>

                <div className="flex shrink-0 items-center gap-2">
                    <span
                        className={`rounded-full px-2.5 py-1 text-[11px] font-semibold capitalize ${STATUS_STYLES[status] || STATUS_STYLES.active
                            }`}
                    >
                        {status}
                    </span>

                    <button
                        onClick={() => !flagged && setShowModal(true)}
                        title={flagged ? "Already flagged" : "Flag this board"}
                        className={`rounded-lg p-1.5 transition ${flagged
                                ? "cursor-default text-burgundy"
                                : "text-plum/20 hover:bg-burgundy/8 hover:text-burgundy"
                            }`}
                    >
                        <Flag className="h-3.5 w-3.5" />
                    </button>
                </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
                {options?.map((option, index) => (
                    <div
                        key={`${option.label}-${index}`}
                        className="flex items-center gap-1.5 rounded-lg border border-plum/8 bg-plum/3 px-3 py-1.5"
                    >
                        <span className="text-xs font-medium text-plum/70">
                            {option.label}
                        </span>

                        <span className="text-[11px] font-bold text-sunset">
                            {option.votes} votes
                        </span>
                    </div>
                ))}
            </div>

            <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-plum/5 pt-3 text-[11px] text-plum/40">
                <div className="flex flex-wrap items-center gap-3">
                    <span className="rounded-full bg-plum/6 px-2 py-0.5 font-medium text-plum/50">
                        {category}
                    </span>

                    <span className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        {duration}
                    </span>

                    <span className="rounded-full bg-plum/6 px-2 py-0.5 font-medium capitalize text-plum/60">
                        {visibility}
                    </span>
                </div>

                <span>
                    {createdAt
                        ? new Date(createdAt).toLocaleDateString()
                        : ""}
                </span>
            </div>

            <div className="mt-4 flex gap-2 border-t border-plum/5 pt-3">
                <Link
                    to={`/dashboard/boards/${id}/edit`}
                    className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-plum/10 py-2.5 text-sm font-semibold text-plum/70 transition hover:border-sunset/40 hover:bg-sunset/5 hover:text-sunset"
                >
                    <Pencil className="h-4 w-4" />
                    Edit
                </Link>

                <button
                    onClick={handleDelete}
                    className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-red-200 py-2.5 text-sm font-semibold text-red-600 transition hover:bg-red-50"
                >
                    <Trash2 className="h-4 w-4" />
                    Delete
                </button>
            </div>
        </div>
    );
}

export default function AllBoards() {
    const { boards, deleteBoard } = useBoards();

    const [search, setSearch] = useState("");
    const [filter, setFilter] = useState("All");

    const categories = [
        "All",
        ...new Set(boards.map((board) => board.category).filter(Boolean)),
    ];

    const filtered = boards.filter((board) => {
        const matchCategory =
            filter === "All" || board.category === filter;

        const matchSearch = board.title
            .toLowerCase()
            .includes(search.toLowerCase());

        return matchCategory && matchSearch;
    });

    return (
        <div className="space-y-6 pb-10">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-extrabold text-plum">
                        All Boards
                    </h1>

                    <p className="mt-1 text-sm text-plum/50">
                        {boards.length} board{boards.length !== 1 ? "s" : ""} created
                    </p>
                </div>

                <Link
                    to="/dashboard/boards/new"
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sunset to-orange px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-sunset/25 transition hover:brightness-105"
                >
                    <Plus className="h-4 w-4" />
                    New Board
                </Link>
            </div>

            {boards.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-plum/15 bg-white py-20 text-center">
                    <p className="text-4xl">📊</p>

                    <p className="mt-3 text-base font-semibold text-plum/50">
                        No boards yet
                    </p>

                    <p className="mt-1 text-sm text-plum/30">
                        Create a board and let your community decide
                    </p>

                    <Link
                        to="/dashboard/boards/new"
                        className="mt-5 rounded-xl bg-gradient-to-r from-sunset to-orange px-5 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-105"
                    >
                        Create First Board
                    </Link>
                </div>
            ) : (
                <>
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                        <div className="relative max-w-sm flex-1">
                            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-plum/30" />

                            <input
                                type="search"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Search boards..."
                                className="w-full rounded-xl border border-plum/10 bg-white py-2.5 pl-9 pr-4 text-sm text-plum outline-none transition placeholder:text-plum/30 focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                            />
                        </div>

                        <div className="flex flex-wrap gap-2">
                            {categories.map((category) => (
                                <button
                                    key={category}
                                    onClick={() => setFilter(category)}
                                    className={`rounded-full border px-3 py-1.5 text-xs font-semibold transition ${filter === category
                                            ? "border-sunset bg-sunset/10 text-sunset"
                                            : "border-plum/10 bg-white text-plum/60 hover:border-sunset/40 hover:text-sunset"
                                        }`}
                                >
                                    {category}
                                </button>
                            ))}
                        </div>
                    </div>

                    {filtered.length === 0 ? (
                        <p className="py-10 text-center text-sm text-plum/40">
                            No boards match your search.
                        </p>
                    ) : (
                        <div className="grid gap-4 sm:grid-cols-2">
                            {filtered.map((board) => (
                                <BoardCard
                                    key={board.id}
                                    board={board}
                                    onDelete={deleteBoard}
                                />
                            ))}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
