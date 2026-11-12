import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2, ChevronLeft, CheckCircle2 } from "lucide-react";
import { useBoards } from "../../hooks/useBoards";

const CATEGORIES = ["Tech", "Social", "Work", "Lifestyle", "Education", "Finance", "Health", "Other"];
const DURATIONS  = ["1 day", "3 days", "1 week", "2 weeks", "1 month"];

export default function CreateBoard() {
    const navigate = useNavigate();
    const { addBoard } = useBoards();
    const [submitted, setSubmitted] = useState(false);
    const [form, setForm] = useState({
        title: "",
        description: "",
        category: "",
        duration: "",
        options: ["", ""],
    });
    const [errors, setErrors] = useState({});

    const set = (key, val) => setForm((p) => ({ ...p, [key]: val }));

    const setOption = (i, val) =>
        setForm((p) => {
            const options = [...p.options];
            options[i] = val;
            return { ...p, options };
        });

    const addOption = () => {
        if (form.options.length < 6) setForm((p) => ({ ...p, options: [...p.options, ""] }));
    };

    const removeOption = (i) => {
        if (form.options.length <= 2) return;
        setForm((p) => ({ ...p, options: p.options.filter((_, idx) => idx !== i) }));
    };

    const validate = () => {
        const e = {};
        if (!form.title.trim())    e.title    = "Title is required";
        if (!form.category)        e.category = "Pick a category";
        if (!form.duration)        e.duration = "Pick a duration";
        const filledOptions = form.options.filter((o) => o.trim());
        if (filledOptions.length < 2) e.options = "At least 2 options are required";
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        addBoard(form); // saves to localStorage — swap with API call when backend is ready
        setSubmitted(true);
    };

    if (submitted) {
        return (
            <div className="flex flex-col items-center justify-center py-20 text-center">
                <span className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-sunset to-orange shadow-lg shadow-sunset/30">
                    <CheckCircle2 className="h-10 w-10 text-white" />
                </span>
                <h2 className="mt-6 text-2xl font-extrabold text-plum">Board Created! 🎉</h2>
                <p className="mt-2 text-sm text-plum/60">
                    <span className="font-semibold text-plum">"{form.title}"</span> is live and ready for votes.
                </p>
                <div className="mt-8 flex flex-wrap justify-center gap-3">
                    <button
                        onClick={() => { setSubmitted(false); setForm({ title: "", description: "", category: "", duration: "", options: ["", ""] }); setErrors({}); }}
                        className="rounded-xl border border-plum/12 bg-white px-5 py-2.5 text-sm font-semibold text-plum transition hover:border-plum/25"
                    >
                        Create Another
                    </button>
                    <button
                        onClick={() => navigate("/dashboard")}
                        className="rounded-xl bg-gradient-to-r from-sunset to-orange px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-sunset/25 transition hover:brightness-105"
                    >
                        Go to Dashboard →
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl pb-12">
            {/* Header */}
            <div className="mb-6 flex items-center gap-3">
                <button
                    onClick={() => navigate("/dashboard")}
                    className="flex h-9 w-9 items-center justify-center rounded-xl border border-plum/10 bg-white text-plum/50 transition hover:border-plum/20 hover:text-plum"
                >
                    <ChevronLeft className="h-5 w-5" />
                </button>
                <div>
                    <h1 className="text-xl font-extrabold text-plum">Create New Board</h1>
                    <p className="text-xs text-plum/50">Set up a decision board for your community to vote on</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} noValidate className="space-y-6">

                {/* Card: Basic Info */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Basic Info</h2>

                    {/* Title */}
                    <div className="mb-4">
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Board Title <span className="text-coral">*</span></label>
                        <input
                            type="text"
                            value={form.title}
                            onChange={(e) => set("title", e.target.value)}
                            placeholder="e.g. Best JavaScript Framework 2025"
                            maxLength={100}
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                        <div className="mt-1 flex justify-between">
                            {errors.title
                                ? <p className="text-xs text-coral">{errors.title}</p>
                                : <span />
                            }
                            <p className="text-xs text-plum/30">{form.title.length}/100</p>
                        </div>
                    </div>

                    {/* Description */}
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Description <span className="text-plum/30 text-xs font-normal">(optional)</span></label>
                        <textarea
                            value={form.description}
                            onChange={(e) => set("description", e.target.value)}
                            placeholder="Give context to help people vote better…"
                            rows={3}
                            maxLength={300}
                            className="w-full resize-none rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                        <p className="mt-1 text-right text-xs text-plum/30">{form.description.length}/300</p>
                    </div>
                </div>

                {/* Card: Category & Duration */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Settings</h2>
                    <div className="grid gap-4 sm:grid-cols-2">

                        {/* Category */}
                        <div>
                            <label className="mb-1.5 block text-sm font-medium text-plum/80">Category <span className="text-coral">*</span></label>
                            <div className="flex flex-wrap gap-2">
                                {CATEGORIES.map((cat) => (
                                    <button
                                        key={cat}
                                        type="button"
                                        onClick={() => set("category", cat)}
                                        className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition
                                            ${form.category === cat
                                                ? "border-sunset bg-sunset/10 text-sunset"
                                                : "border-plum/10 bg-white text-plum/60 hover:border-sunset/40 hover:text-sunset"
                                            }`}
                                    >
                                        {cat}
                                    </button>
                                ))}
                            </div>
                            {errors.category && <p className="mt-1.5 text-xs text-coral">{errors.category}</p>}
                        </div>

                        {/* Duration */}
                        <div>
                            <label className="mb-1.5 block text-sm font-medium text-plum/80">Duration <span className="text-coral">*</span></label>
                            <div className="flex flex-wrap gap-2">
                                {DURATIONS.map((d) => (
                                    <button
                                        key={d}
                                        type="button"
                                        onClick={() => set("duration", d)}
                                        className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition
                                            ${form.duration === d
                                                ? "border-orange bg-orange/10 text-orange"
                                                : "border-plum/10 bg-white text-plum/60 hover:border-orange/40 hover:text-orange"
                                            }`}
                                    >
                                        {d}
                                    </button>
                                ))}
                            </div>
                            {errors.duration && <p className="mt-1.5 text-xs text-coral">{errors.duration}</p>}
                        </div>
                    </div>
                </div>

                {/* Card: Options */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-1 text-sm font-bold uppercase tracking-widest text-plum/40">Options to Vote On</h2>
                    <p className="mb-4 text-xs text-plum/40">Add 2 - 6 options for people to choose from</p>

                    <div className="space-y-3">
                        {form.options.map((opt, i) => (
                            <div key={i} className="flex items-center gap-2">
                                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-sunset/20 to-orange/20 text-xs font-bold text-sunset">
                                    {i + 1}
                                </span>
                                <input
                                    type="text"
                                    value={opt}
                                    onChange={(e) => setOption(i, e.target.value)}
                                    placeholder={`Option ${i + 1}`}
                                    className="flex-1 rounded-xl border border-plum/10 bg-white px-4 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                                />
                                <button
                                    type="button"
                                    onClick={() => removeOption(i)}
                                    disabled={form.options.length <= 2}
                                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-plum/30 transition hover:bg-coral/10 hover:text-coral disabled:opacity-20 disabled:cursor-not-allowed"
                                >
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        ))}
                    </div>

                    {errors.options && <p className="mt-2 text-xs text-coral">{errors.options}</p>}

                    {form.options.length < 6 && (
                        <button
                            type="button"
                            onClick={addOption}
                            className="mt-4 flex items-center gap-2 rounded-xl border border-dashed border-plum/20 px-4 py-2.5 text-sm font-medium text-plum/50 transition hover:border-sunset/40 hover:text-sunset w-full justify-center"
                        >
                            <Plus className="h-4 w-4" /> Add Option
                        </button>
                    )}
                </div>

                {/* Submit */}
                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <button
                        type="button"
                        onClick={() => navigate("/dashboard")}
                        className="rounded-xl border border-plum/12 bg-white px-6 py-3 text-sm font-semibold text-plum transition hover:border-plum/25"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="rounded-xl bg-gradient-to-r from-sunset to-orange px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-sunset/25 transition hover:brightness-105 active:scale-[0.99]"
                    >
                        Create Board 
                    </button>
                </div>
            </form>
        </div>
    );
}
