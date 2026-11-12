import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2, ChevronLeft, CheckCircle2 } from "lucide-react";

const KEY = "dh_comparisons";
const load = () => { try { return JSON.parse(localStorage.getItem(KEY)) ?? []; } catch { return []; } };
const persist = (d) => localStorage.setItem(KEY, JSON.stringify(d));

const CRITERIA_SUGGESTIONS = ["Cost", "Quality", "Ease of Use", "Performance", "Support", "Scalability", "Time to Learn", "Community"];

export default function CompareOptions() {
    const navigate = useNavigate();
    const [submitted, setSubmitted] = useState(false);
    const [savedItem, setSavedItem] = useState(null);
    const [form, setForm] = useState({
        title: "",
        description: "",
        options: ["", ""],
        criteria: [""],
    });
    const [errors, setErrors] = useState({});

    const set = (key, val) => setForm((p) => ({ ...p, [key]: val }));

    const setOption   = (i, val) => setForm((p) => { const a = [...p.options];   a[i] = val; return { ...p, options: a }; });
    const setCriteria = (i, val) => setForm((p) => { const a = [...p.criteria];  a[i] = val; return { ...p, criteria: a }; });

    const addOption   = () => { if (form.options.length < 5)   setForm((p) => ({ ...p, options:  [...p.options, ""] })); };
    const addCriteria = () => { if (form.criteria.length < 8)  setForm((p) => ({ ...p, criteria: [...p.criteria, ""] })); };

    const removeOption   = (i) => { if (form.options.length <= 2)  return; setForm((p) => ({ ...p, options:  p.options.filter((_,  idx) => idx !== i) })); };
    const removeCriteria = (i) => { if (form.criteria.length <= 1) return; setForm((p) => ({ ...p, criteria: p.criteria.filter((_, idx) => idx !== i) })); };

    const addSuggestion = (s) => {
        if (!form.criteria.includes(s) && form.criteria.length < 8)
            setForm((p) => ({ ...p, criteria: [...p.criteria.filter((c) => c.trim()), s] }));
    };

    const validate = () => {
        const e = {};
        if (!form.title.trim()) e.title = "Title is required";
        if (form.options.filter((o) => o.trim()).length < 2) e.options = "At least 2 options required";
        if (form.criteria.filter((c) => c.trim()).length < 1) e.criteria = "At least 1 criteria required";
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        const item = {
            id: Date.now(),
            title: form.title,
            description: form.description,
            options: form.options.filter((o) => o.trim()),
            criteria: form.criteria.filter((c) => c.trim()),
            createdAt: new Date().toISOString(),
            // TODO: replace with real userId from backend
        };
        const updated = [item, ...load()];
        persist(updated);
        setSavedItem(item);
        setSubmitted(true);
    };

    if (submitted) return (
        <div className="flex flex-col items-center justify-center py-20 text-center">
            <span className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-orange to-coral shadow-lg shadow-orange/30">
                <CheckCircle2 className="h-10 w-10 text-white" />
            </span>
            <h2 className="mt-6 text-2xl font-extrabold text-plum">Comparison Created! ⚖️</h2>
            <p className="mt-2 text-sm text-plum/60">
                <span className="font-semibold text-plum">"{savedItem.title}"</span> is ready for community input.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
                <button
                    onClick={() => { setSubmitted(false); setForm({ title: "", description: "", options: ["", ""], criteria: [""] }); setErrors({}); }}
                    className="rounded-xl border border-plum/12 bg-white px-5 py-2.5 text-sm font-semibold text-plum transition hover:border-plum/25">
                    Create Another
                </button>
                <button onClick={() => navigate("/dashboard")}
                    className="rounded-xl bg-gradient-to-r from-orange to-coral px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-orange/25 transition hover:brightness-105">
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
                    <h1 className="text-xl font-extrabold text-plum">Compare Options</h1>
                    <p className="text-xs text-plum/50">Set up a side-by-side comparison for your community</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} noValidate className="space-y-6">

                {/* Basic Info */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">What are you comparing?</h2>
                    <div className="mb-4">
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Title <span className="text-coral">*</span></label>
                        <input type="text" value={form.title} onChange={(e) => set("title", e.target.value)}
                            placeholder="e.g. Best Cloud Provider for Startups"
                            maxLength={100}
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                        <div className="mt-1 flex justify-between">
                            {errors.title ? <p className="text-xs text-coral">{errors.title}</p> : <span />}
                            <p className="text-xs text-plum/30">{form.title.length}/100</p>
                        </div>
                    </div>
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Description <span className="text-plum/30 text-xs font-normal">(optional)</span></label>
                        <textarea value={form.description} onChange={(e) => set("description", e.target.value)}
                            placeholder="Add context to help the community compare better…"
                            rows={2} maxLength={300}
                            className="w-full resize-none rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                    </div>
                </div>

                {/* Options */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-1 text-sm font-bold uppercase tracking-widest text-plum/40">Options to Compare</h2>
                    <p className="mb-4 text-xs text-plum/40">Add 2–5 items to compare side by side</p>
                    <div className="space-y-3">
                        {form.options.map((opt, i) => (
                            <div key={i} className="flex items-center gap-2">
                                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-orange/20 to-coral/20 text-xs font-bold text-orange">
                                    {i + 1}
                                </span>
                                <input type="text" value={opt} onChange={(e) => setOption(i, e.target.value)}
                                    placeholder={`Option ${i + 1} — e.g. AWS`}
                                    className="flex-1 rounded-xl border border-plum/10 bg-white px-4 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                                <button type="button" onClick={() => removeOption(i)} disabled={form.options.length <= 2}
                                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-plum/30 transition hover:bg-coral/10 hover:text-coral disabled:opacity-20 disabled:cursor-not-allowed">
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        ))}
                    </div>
                    {errors.options && <p className="mt-2 text-xs text-coral">{errors.options}</p>}
                    {form.options.length < 5 && (
                        <button type="button" onClick={addOption}
                            className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-plum/20 px-4 py-2.5 text-sm font-medium text-plum/50 transition hover:border-orange/40 hover:text-orange">
                            <Plus className="h-4 w-4" /> Add Option
                        </button>
                    )}
                </div>

                {/* Criteria */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-1 text-sm font-bold uppercase tracking-widest text-plum/40">Comparison Criteria</h2>
                    <p className="mb-3 text-xs text-plum/40">What factors should people judge by?</p>

                    {/* Suggestions */}
                    <div className="mb-4 flex flex-wrap gap-2">
                        {CRITERIA_SUGGESTIONS.map((s) => (
                            <button key={s} type="button" onClick={() => addSuggestion(s)}
                                disabled={form.criteria.includes(s) || form.criteria.length >= 8}
                                className={`rounded-lg border px-3 py-1 text-xs font-medium transition
                                    ${form.criteria.includes(s)
                                        ? "border-orange/30 bg-orange/10 text-orange cursor-default"
                                        : "border-plum/10 text-plum/50 hover:border-orange/40 hover:text-orange disabled:opacity-30"}`}>
                                + {s}
                            </button>
                        ))}
                    </div>

                    <div className="space-y-3">
                        {form.criteria.map((c, i) => (
                            <div key={i} className="flex items-center gap-2">
                                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-plum/10 to-burgundy/10 text-xs font-bold text-plum/50">
                                    {i + 1}
                                </span>
                                <input type="text" value={c} onChange={(e) => setCriteria(i, e.target.value)}
                                    placeholder="e.g. Cost"
                                    className="flex-1 rounded-xl border border-plum/10 bg-white px-4 py-2.5 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                                <button type="button" onClick={() => removeCriteria(i)} disabled={form.criteria.length <= 1}
                                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-plum/30 transition hover:bg-coral/10 hover:text-coral disabled:opacity-20 disabled:cursor-not-allowed">
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        ))}
                    </div>
                    {errors.criteria && <p className="mt-2 text-xs text-coral">{errors.criteria}</p>}
                    {form.criteria.length < 8 && (
                        <button type="button" onClick={addCriteria}
                            className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-plum/20 px-4 py-2.5 text-sm font-medium text-plum/50 transition hover:border-orange/40 hover:text-orange">
                            <Plus className="h-4 w-4" /> Add Criteria
                        </button>
                    )}
                </div>

                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <button type="button" onClick={() => navigate("/dashboard")}
                        className="rounded-xl border border-plum/12 bg-white px-6 py-3 text-sm font-semibold text-plum transition hover:border-plum/25">
                        Cancel
                    </button>
                    <button type="submit"
                        className="rounded-xl bg-gradient-to-r from-orange to-coral px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-orange/25 transition hover:brightness-105 active:scale-[0.99]">
                        Create Comparison ⚖️
                    </button>
                </div>
            </form>
        </div>
    );
}
