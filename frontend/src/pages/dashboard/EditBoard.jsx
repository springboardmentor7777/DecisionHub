
import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ChevronLeft, Plus, Trash2, Save } from "lucide-react";
import { useBoards } from "../../hooks/useBoards";

export default function EditBoard() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { boards, updateBoard } = useBoards();

    const board = boards.find((item) => String(item.id) === id);

    const [form, setForm] = useState({
        title: "",
        description: "",
        category: "",
        duration: "",
        visibility: "public",
        options: ["", ""],
    });

    useEffect(() => {
        if (board) {
            setForm({
                title: board.title || "",
                description: board.description || "",
                category: board.category || "",
                duration: board.duration || "",
                visibility: board.visibility || "public",
                options: board.options?.map((option) => option.label) || ["", ""],
            });
        }
    }, [board]);

    const set = (field, value) => {
        setForm((previous) => ({
            ...previous,
            [field]: value,
        }));
    };

    const setOption = (index, value) => {
        const updated = [...form.options];
        updated[index] = value;
        set("options", updated);
    };

    const addOption = () => {
        if (form.options.length < 6) {
            set("options", [...form.options, ""]);
        }
    };

    const removeOption = (index) => {
        if (form.options.length <= 2) return;

        set(
            "options",
            form.options.filter((_, i) => i !== index)
        );
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!form.title.trim() || !form.category || !form.duration) {
            alert("Please fill in all required fields.");
            return;
        }

        const validOptions = form.options.filter((option) => option.trim());

        if (validOptions.length < 2) {
            alert("Please provide at least two options.");
            return;
        }

        updateBoard(board.id, {
            ...form,
            options: validOptions,
        });

        alert("Board updated successfully!");
        navigate("/dashboard/boards");
    };

    if (!board) {
        return (
            <div className="rounded-2xl bg-white p-8 text-center shadow-sm">
                <h2 className="text-xl font-bold text-plum">
                    Board Not Found
                </h2>

                <p className="mt-2 text-sm text-plum/50">
                    This board may have been deleted or does not exist.
                </p>

                <Link
                    to="/dashboard/boards"
                    className="mt-5 inline-block rounded-xl bg-sunset px-5 py-2.5 text-sm font-semibold text-white"
                >
                    Back to Boards
                </Link>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-3xl space-y-6 pb-10">
            <Link
                to="/dashboard/boards"
                className="inline-flex items-center gap-2 text-sm font-medium text-plum/50 transition hover:text-sunset"
            >
                <ChevronLeft className="h-4 w-4" />
                Back to Boards
            </Link>

            <div>
                <h1 className="text-2xl font-extrabold text-plum">
                    Edit Decision Board
                </h1>

                <p className="mt-1 text-sm text-plum/50">
                    Update your decision details and options.
                </p>
            </div>

            <form
                onSubmit={handleSubmit}
                className="space-y-6 rounded-2xl border border-plum/8 bg-white p-6 shadow-sm"
            >
                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Board Title *
                    </label>

                    <input
                        value={form.title}
                        onChange={(e) => set("title", e.target.value)}
                        maxLength={100}
                        required
                        placeholder="Enter board title"
                        className="w-full rounded-xl border border-plum/10 px-4 py-3 text-sm outline-none focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                    />
                </div>

                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Description
                    </label>

                    <textarea
                        value={form.description}
                        onChange={(e) => set("description", e.target.value)}
                        rows={4}
                        maxLength={500}
                        placeholder="Describe your decision..."
                        className="w-full resize-none rounded-xl border border-plum/10 px-4 py-3 text-sm outline-none focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                    />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                        <label className="mb-2 block text-sm font-semibold text-plum">
                            Category *
                        </label>

                        <select
                            value={form.category}
                            onChange={(e) => set("category", e.target.value)}
                            required
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm outline-none focus:border-sunset"
                        >
                            <option value="">Select category</option>
                            {["Tech", "Social", "Work", "Lifestyle", "Education", "Finance", "Health", "Other"].map((category) => (
                                <option key={category} value={category}>
                                    {category}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="mb-2 block text-sm font-semibold text-plum">
                            Duration *
                        </label>

                        <select
                            value={form.duration}
                            onChange={(e) => set("duration", e.target.value)}
                            required
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm outline-none focus:border-sunset"
                        >
                            <option value="">Select duration</option>
                            {["1 day", "3 days", "1 week", "2 weeks", "1 month"].map((duration) => (
                                <option key={duration} value={duration}>
                                    {duration}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Board Visibility
                    </label>

                    <select
                        value={form.visibility}
                        onChange={(e) => set("visibility", e.target.value)}
                        className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm outline-none focus:border-sunset"
                    >
                        <option value="public">Public - Anyone can view and vote</option>
                        <option value="private">Private - Only invited members</option>
                    </select>
                </div>

                <div>
                    <div className="mb-3 flex items-center justify-between">
                        <label className="text-sm font-semibold text-plum">
                            Decision Options *
                        </label>

                        <span className="text-xs text-plum/40">
                            {form.options.length}/6 options
                        </span>
                    </div>

                    <div className="space-y-3">
                        {form.options.map((option, index) => (
                            <div key={index} className="flex gap-2">
                                <input
                                    value={option}
                                    onChange={(e) => setOption(index, e.target.value)}
                                    maxLength={100}
                                    placeholder={`Option ${index + 1}`}
                                    className="flex-1 rounded-xl border border-plum/10 px-4 py-3 text-sm outline-none focus:border-sunset"
                                />

                                <button
                                    type="button"
                                    onClick={() => removeOption(index)}
                                    disabled={form.options.length <= 2}
                                    className="rounded-xl border border-plum/10 px-3 text-plum/40 transition hover:bg-red-50 hover:text-red-500 disabled:opacity-30"
                                >
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        ))}
                    </div>

                    {form.options.length < 6 && (
                        <button
                            type="button"
                            onClick={addOption}
                            className="mt-3 inline-flex items-center gap-2 text-sm font-semibold text-sunset hover:underline"
                        >
                            <Plus className="h-4 w-4" />
                            Add Option
                        </button>
                    )}
                </div>

                <div className="flex flex-col-reverse gap-3 border-t border-plum/5 pt-5 sm:flex-row sm:justify-end">
                    <Link
                        to="/dashboard/boards"
                        className="rounded-xl border border-plum/10 px-5 py-3 text-center text-sm font-semibold text-plum/60 hover:bg-plum/5"
                    >
                        Cancel
                    </Link>

                    <button
                        type="submit"
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-sunset to-orange px-6 py-3 text-sm font-semibold text-white shadow-md transition hover:brightness-105"
                    >
                        <Save className="h-4 w-4" />
                        Save Changes
                    </button>
                </div>
            </form>
        </div>
    );
}
