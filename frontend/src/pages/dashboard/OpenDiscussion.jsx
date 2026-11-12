import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, CheckCircle2, Tag, X } from "lucide-react";

const KEY = "dh_discussions";
const load = () => { try { return JSON.parse(localStorage.getItem(KEY)) ?? []; } catch { return []; } };
const persist = (d) => localStorage.setItem(KEY, JSON.stringify(d));

const CATEGORIES  = ["Tech", "Social", "Work", "Lifestyle", "Education", "Finance", "Health", "Other"];
const TAG_SUGGESTIONS = ["advice", "opinion", "help", "career", "tools", "team", "productivity", "learning"];

export default function OpenDiscussion() {
    const navigate = useNavigate();
    const [submitted, setSubmitted] = useState(false);
    const [savedItem, setSavedItem] = useState(null);
    const [tagInput, setTagInput] = useState("");
    const [form, setForm] = useState({ title: "", body: "", category: "", tags: [] });
    const [errors, setErrors] = useState({});

    const set = (key, val) => setForm((p) => ({ ...p, [key]: val }));

    const addTag = (tag) => {
        const t = tag.trim().toLowerCase();
        if (t && !form.tags.includes(t) && form.tags.length < 5)
            setForm((p) => ({ ...p, tags: [...p.tags, t] }));
        setTagInput("");
    };

    const removeTag = (t) => setForm((p) => ({ ...p, tags: p.tags.filter((x) => x !== t) }));

    const handleTagKey = (e) => {
        if (e.key === "Enter" || e.key === ",") { e.preventDefault(); addTag(tagInput); }
    };

    const validate = () => {
        const e = {};
        if (!form.title.trim()) e.title = "Title is required";
        if (!form.body.trim())  e.body  = "Discussion body is required";
        if (!form.category)     e.category = "Pick a category";
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        const item = {
            id: Date.now(),
            title: form.title,
            body: form.body,
            category: form.category,
            tags: form.tags,
            replies: 0,
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
            <span className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-burgundy to-plum shadow-lg shadow-plum/30">
                <CheckCircle2 className="h-10 w-10 text-white" />
            </span>
            <h2 className="mt-6 text-2xl font-extrabold text-plum">Discussion Started! 💬</h2>
            <p className="mt-2 text-sm text-plum/60">
                <span className="font-semibold text-plum">"{savedItem.title}"</span> is open for replies.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
                <button
                    onClick={() => { setSubmitted(false); setForm({ title: "", body: "", category: "", tags: [] }); setErrors({}); setTagInput(""); }}
                    className="rounded-xl border border-plum/12 bg-white px-5 py-2.5 text-sm font-semibold text-plum transition hover:border-plum/25">
                    Start Another
                </button>
                <button onClick={() => navigate("/dashboard")}
                    className="rounded-xl bg-gradient-to-r from-burgundy to-plum px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-plum/25 transition hover:brightness-110">
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
                    <h1 className="text-xl font-extrabold text-plum">Open a Discussion</h1>
                    <p className="text-xs text-plum/50">Share a topic and get insights from your community</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} noValidate className="space-y-6">

                {/* Title + Body */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Discussion</h2>

                    <div className="mb-4">
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">Title <span className="text-coral">*</span></label>
                        <input type="text" value={form.title} onChange={(e) => set("title", e.target.value)}
                            placeholder="e.g. Is React still the best choice in 2025?"
                            maxLength={120}
                            className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                        <div className="mt-1 flex justify-between">
                            {errors.title ? <p className="text-xs text-coral">{errors.title}</p> : <span />}
                            <p className="text-xs text-plum/30">{form.title.length}/120</p>
                        </div>
                    </div>

                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">What's on your mind? <span className="text-coral">*</span></label>
                        <textarea value={form.body} onChange={(e) => set("body", e.target.value)}
                            placeholder="Share your thoughts, context, or question in detail…"
                            rows={5} maxLength={1000}
                            className="w-full resize-none rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20" />
                        <div className="mt-1 flex justify-between">
                            {errors.body ? <p className="text-xs text-coral">{errors.body}</p> : <span />}
                            <p className="text-xs text-plum/30">{form.body.length}/1000</p>
                        </div>
                    </div>
                </div>

                {/* Category */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-sm font-bold uppercase tracking-widest text-plum/40">Category <span className="text-coral">*</span></h2>
                    <div className="flex flex-wrap gap-2">
                        {CATEGORIES.map((cat) => (
                            <button key={cat} type="button" onClick={() => set("category", cat)}
                                className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition
                                    ${form.category === cat
                                        ? "border-sunset bg-sunset/10 text-sunset"
                                        : "border-plum/10 bg-white text-plum/60 hover:border-sunset/40 hover:text-sunset"}`}>
                                {cat}
                            </button>
                        ))}
                    </div>
                    {errors.category && <p className="mt-1.5 text-xs text-coral">{errors.category}</p>}
                </div>

                {/* Tags */}
                <div className="rounded-2xl border border-plum/8 bg-white p-6 shadow-sm">
                    <h2 className="mb-1 text-sm font-bold uppercase tracking-widest text-plum/40">Tags</h2>
                    <p className="mb-3 text-xs text-plum/40">Add up to 5 tags to help people find your discussion</p>

                    <div className="mb-3 flex flex-wrap gap-2">
                        {TAG_SUGGESTIONS.map((s) => (
                            <button key={s} type="button" onClick={() => addTag(s)}
                                disabled={form.tags.includes(s) || form.tags.length >= 5}
                                className={`rounded-full border px-3 py-1 text-xs font-medium transition
                                    ${form.tags.includes(s)
                                        ? "border-plum/20 bg-plum/8 text-plum/40 cursor-default"
                                        : "border-plum/10 text-plum/50 hover:border-sunset/40 hover:text-sunset disabled:opacity-30"}`}>
                                #{s}
                            </button>
                        ))}
                    </div>

                    {/* Selected tags */}
                    {form.tags.length > 0 && (
                        <div className="mb-3 flex flex-wrap gap-2">
                            {form.tags.map((t) => (
                                <span key={t} className="flex items-center gap-1 rounded-full bg-sunset/10 px-3 py-1 text-xs font-semibold text-sunset">
                                    #{t}
                                    <button type="button" onClick={() => removeTag(t)} className="ml-0.5 hover:text-coral">
                                        <X className="h-3 w-3" />
                                    </button>
                                </span>
                            ))}
                        </div>
                    )}

                    <div className="relative">
                        <Tag className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-plum/30" />
                        <input type="text" value={tagInput} onChange={(e) => setTagInput(e.target.value)} onKeyDown={handleTagKey}
                            placeholder="Type a tag and press Enter…"
                            disabled={form.tags.length >= 5}
                            className="w-full rounded-xl border border-plum/10 bg-white py-2.5 pl-9 pr-4 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20 disabled:opacity-40" />
                    </div>
                </div>

                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <button type="button" onClick={() => navigate("/dashboard")}
                        className="rounded-xl border border-plum/12 bg-white px-6 py-3 text-sm font-semibold text-plum transition hover:border-plum/25">
                        Cancel
                    </button>
                    <button type="submit"
                        className="rounded-xl bg-gradient-to-r from-burgundy to-plum px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-plum/20 transition hover:brightness-110 active:scale-[0.99]">
                        Post Discussion 💬
                    </button>
                </div>
            </form>
        </div>
    );
}
