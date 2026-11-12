import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2, ChevronLeft, CheckCircle2 } from "lucide-react";

const KEY = "dh_polls";
const load = () => { try { return JSON.parse(localStorage.getItem(KEY)) ?? []; } catch { return []; } };
const persist = (d) => localStorage.setItem(KEY, JSON.stringify(d));

const POLL_TYPES = ["Single Choice", "Multiple Choice"];
const DURATIONS  = ["1 day", "3 days", "1 week", "2 weeks", "1 month"];

export default function CreatePoll() {
    const navigate = useNavigate();
    const [submitted, setSubmitted] = useState(false);
    const [savedPoll, setSavedPoll] = useState(null);
    const [form, setForm] = useState({ question: "", type: "Single Choice", duration: "", options: ["", ""] });
    const [errors, setErrors] = useState({});

    const set = (key, val) => setForm((p) => ({ ...p, [key]: val }));
    const setOption = (i, val) => setForm((p) => { const o = [...p.options]; o[i] = val; return { ...p, options: o }; });
    const addOption = () => { if (form.options.length < 6) setForm((p) => ({ ...p, options: [...p.options, ""] })); };
    const removeOption = (i) => { if (form.options.length <= 2) return; setForm((p) => ({ ...p, options: p.options.filter((_, idx) => idx !== i) })); };

    const validate = () => {
        const e = {};
        if (!form.question.trim()) e.question = "Question is required";
        if (!form.duration)        e.duration = "Pick a duration";
        if (form.options.filter((o) => o.trim()).length < 2) e.options = "At least 2 options are required";
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        const poll = {
            id: Date.now(),
            question: form.question,
            type: form.type,
            duration: form.duration,
            options: form.options.filter((o) => o.trim()).map((o) => ({ label: o, votes: 0 })),
            voted: false,
            createdAt: new Date().toISOString(),
            // TODO: replace with real userId from backend
        };
        const updated = [poll, ...load()];
        persist(updated);
        setSavedPoll(poll);
        setSubmitted(true);
    };

    if (submitted) return (
        <div className="flex flex-col items-center justify-center py-20 text-center">
            <span className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-coral to-sunset shadow-lg shadow-sunset/30">
                <CheckCircle2 className="h-10 w-10 text-white" />
            </span>
            <h2 className="mt-6 text-2xl font-extrabold text-plum">Poll Created! 🗳️</h2>
            <p className="mt-2 text-sm text-plum/60">
                <span className="font-semibold text-plum">"{savedPoll.question}"</span> is live.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
                <button
                    onClick={() => { setSubmitted(false); setForm({ question: "", type: "Single Choice", duration: "", options: ["", ""] }); setErrors({}); }}
                    className="rounded-xl border border-plum/12 bg-white px-5 py-2.5 text-sm font-semibold text-plum transition hover:border-plum/25"
                >
                    Create Another
                </button>
                <button
                    onClick={() => navigate("/dashboard")}
                    className="rounded-xl bg-gradient-to-r from-coral to-sunset px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-sunset/25 transition hover:brightness-105"
                >
                    Go to Dashboard →
                </button>
            </div>
        </div>
    );

    return (
        <div className="mx-auto max-w-2xl pb-12">
            <div className="mb-6 flex items-center gap-3">
                <button onClick={() => navigate("/dashboard")} className="flex h-9 w-9 items-center justify-center rounded-xl border border-plum/10 bg-white text-plum/50 transition hover:border-plum/20 hover:text-plum">
                    <ChevronLeft className="h-5 w-5" />
                </button>
                <div>
                    <h1 className="text-xl font-extrabold text-plum">Start a Poll</h1>
                    <p className="text-xs text-plum/50">Ask your community a question and collect votes</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} noValidate className="space-y-6">

                {/* Question */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Poll Question</h2>
                    <div className="mb-4">
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Question <span className="text-coral">*</span></label>
                        <input
                            type="text"
                            value={form.question}
                            onChange={(e) => set("question", e.target.value)}
                            placeholder="e.g. Which framework should we use for the new project?"
                            maxLength={150}
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                        <div className="mt-1 flex justify-between">
                            {errors.question ? <p className="text-xs text-coral">{errors.question}</p> : <span />}
                            <p className="text-xs text-plum/30">{form.question.length}/150</p>
                        </div>
                    </div>

                    {/* Poll Type */}
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Poll Type</label>
                        <div className="flex gap-2">
                            {POLL_TYPES.map((t) => (
                                <button key={t} type="button" onClick={() => set("type", t)}
                                    className={`rounded-lg border px-4 py-2 text-xs font-semibold transition
                                        ${form.type === t ? "border-sunset bg-sunset/10 text-sunset" : "border-plum/10 bg-white text-plum/60 hover:border-sunset/40 hover:text-sunset"}`}>
                                    {t}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Options */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-1 text-sm font-bold uppercase tracking-widest text-plum/40">Answer Options</h2>
                    <p className="mb-4 text-xs text-plum/40">Add 2–6 choices for people to pick from</p>
                    <div className="space-y-3">
                        {form.options.map((opt, i) => (
                            <div key={i} className="flex items-center gap-2">
                                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-coral/20 to-sunset/20 text-xs font-bold text-coral">
                                    {String.fromCharCode(65 + i)}
                                </span>
                                <input
                                    type="text" value={opt} onChange={(e) => setOption(i, e.target.value)}
                                    placeholder={`Choice ${String.fromCharCode(65 + i)}`}
                                    className="flex-1 rounded-xl border border-plum/10 bg-white px-4 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                                />
                                <button type="button" onClick={() => removeOption(i)} disabled={form.options.length <= 2}
                                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-plum/30 transition hover:bg-coral/10 hover:text-coral disabled:opacity-20 disabled:cursor-not-allowed">
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        ))}
                    </div>
                    {errors.options && <p className="mt-2 text-xs text-coral">{errors.options}</p>}
                    {form.options.length < 6 && (
                        <button type="button" onClick={addOption}
                            className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-plum/20 px-4 py-2.5 text-sm font-medium text-plum/50 transition hover:border-sunset/40 hover:text-sunset">
                            <Plus className="h-4 w-4" /> Add Choice
                        </button>
                    )}
                </div>

                {/* Duration */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Duration</h2>
                    <div className="flex flex-wrap gap-2">
                        {DURATIONS.map((d) => (
                            <button key={d} type="button" onClick={() => set("duration", d)}
                                className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition
                                    ${form.duration === d ? "border-orange bg-orange/10 text-orange" : "border-plum/10 bg-white text-plum/60 hover:border-orange/40 hover:text-orange"}`}>
                                {d}
                            </button>
                        ))}
                    </div>
                    {errors.duration && <p className="mt-1.5 text-xs text-coral">{errors.duration}</p>}
                </div>

                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <button type="button" onClick={() => navigate("/dashboard")}
                        className="rounded-xl border border-plum/12 bg-white px-6 py-3 text-sm font-semibold text-plum transition hover:border-plum/25">
                        Cancel
                    </button>
                    <button type="submit"
                        className="rounded-xl bg-gradient-to-r from-coral to-sunset px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-sunset/25 transition hover:brightness-105 active:scale-[0.99]">
                        Launch Poll 🗳️
                    </button>
                </div>
            </form>
        </div>
    );
}
