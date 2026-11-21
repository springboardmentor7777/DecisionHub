
import { useState } from "react";

const KEY = "dh_boards";

function load() {
    try {
        return JSON.parse(localStorage.getItem(KEY)) ?? [];
    } catch {
        return [];
    }
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
            visibility: form.visibility || "public",
            options: form.options
                .filter((option) => option.trim())
                .map((option) => ({
                    label: option,
                    votes: 0,
                })),
            status: "active",
            createdAt: new Date().toISOString(),
            createdBy: "me",
        };

        const updated = [newBoard, ...boards];

        persist(updated);
        setBoards(updated);

        return newBoard;
    };

    const updateBoard = (boardId, form) => {
        const updated = boards.map((board) => {
            if (board.id !== boardId) return board;

            return {
                ...board,
                title: form.title,
                description: form.description,
                category: form.category,
                duration: form.duration,
                visibility: form.visibility || board.visibility || "public",
                options: form.options
                    .filter((option) => option.trim())
                    .map((option, index) => ({
                        label: option,
                        votes: board.options[index]?.votes ?? 0,
                    })),
            };
        });

        persist(updated);
        setBoards(updated);
    };

    const deleteBoard = (boardId) => {
        const updated = boards.filter(
            (board) => board.id !== boardId
        );

        persist(updated);
        setBoards(updated);
    };

    const voteOption = (boardId, optionIndex) => {
        const updated = boards.map((board) => {
            if (board.id !== boardId) return board;

            const options = board.options.map((option, index) =>
                index === optionIndex
                    ? { ...option, votes: option.votes + 1 }
                    : option
            );

            return { ...board, options };
        });

        persist(updated);
        setBoards(updated);
    };

    return {
        boards,
        addBoard,
        updateBoard,
        deleteBoard,
        voteOption,
    };
}
