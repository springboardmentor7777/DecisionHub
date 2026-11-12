import { useState } from "react";

const KEY = "dh_boards";

function load() {
    try { return JSON.parse(localStorage.getItem(KEY)) ?? []; }
    catch { return []; }
}

function persist(boards) {
    localStorage.setItem(KEY, JSON.stringify(boards));
}

export function useBoards() {
    const [boards, setBoards] = useState(load);

    const addBoard = (form) => {
        const newBoard = {
            id: Date.now(),
            title: form.title,
            description: form.description,
            category: form.category,
            duration: form.duration,
            options: form.options.filter((o) => o.trim()).map((o) => ({ label: o, votes: 0 })),
            status: "active",
            createdAt: new Date().toISOString(),
            // TODO: replace with real userId from backend auth
            createdBy: "me",
        };
        const updated = [newBoard, ...boards];
        persist(updated);
        setBoards(updated);
        return newBoard;
    };

    const voteOption = (boardId, optionIndex) => {
        const updated = boards.map((b) => {
            if (b.id !== boardId) return b;
            const options = b.options.map((o, i) =>
                i === optionIndex ? { ...o, votes: o.votes + 1 } : o
            );
            return { ...b, options };
        });
        persist(updated);
        setBoards(updated);
    };

    return { boards, addBoard, voteOption };
}
