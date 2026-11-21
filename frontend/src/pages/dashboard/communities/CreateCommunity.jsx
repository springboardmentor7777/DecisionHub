
import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft, Users, Plus } from "lucide-react";

const categories = [
    "Career",
    "Education",
    "Technology",
    "Travel",
    "Finance",
    "Lifestyle",
];

export default function CreateCommunity() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        category: "",
        description: "",
    });

    const [error, setError] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();

        const name = form.name.trim();
        const description = form.description.trim();

        if (!name || !form.category || !description) {
            setError("Please fill in all fields.");
            return;
        }

        try {
            const savedCommunities = JSON.parse(
                localStorage.getItem("dh_communities") || "[]"
            );

            const newCommunity = {
                id: Date.now(),
                name,
                category: form.category,
                description,
                members: 0,
            };

            localStorage.setItem(
                "dh_communities",
                JSON.stringify([newCommunity, ...savedCommunities])
            );

            navigate("/dashboard/communities");
        } catch {
            setError("Unable to save community. Please try again.");
        }
    };

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <Link
                to="/dashboard/communities"
                className="inline-flex items-center gap-2 text-sm font-medium text-plum/60 hover:text-plum"
            >
                <ArrowLeft size={17} />
                Back to Communities
            </Link>

            <div>
                <h1 className="text-2xl font-bold text-plum sm:text-3xl">
                    Create Community
                </h1>
                <p className="mt-1 text-sm text-plum/60">
                    Build a community and connect with people who share your interests.
                </p>
            </div>

            <form
                onSubmit={handleSubmit}
                className="space-y-5 rounded-2xl border border-plum/10 bg-white p-6 shadow-sm sm:p-8"
            >
                <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-sunset/15 text-plum">
                    <Users size={27} />
                </div>

                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Community Name
                    </label>
                    <input
                        type="text"
                        value={form.name}
                        onChange={(e) => {
                            setForm({ ...form, name: e.target.value });
                            setError("");
                        }}
                        placeholder="Enter community name"
                        maxLength={60}
                        className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum outline-none transition focus:border-plum"
                    />
                </div>

                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Category
                    </label>
                    <select
                        value={form.category}
                        onChange={(e) => {
                            setForm({ ...form, category: e.target.value });
                            setError("");
                        }}
                        className="w-full rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum outline-none transition focus:border-plum"
                    >
                        <option value="">Select a category</option>
                        {categories.map((category) => (
                            <option key={category} value={category}>
                                {category}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label className="mb-2 block text-sm font-semibold text-plum">
                        Description
                    </label>
                    <textarea
                        value={form.description}
                        onChange={(e) => {
                            setForm({
                                ...form,
                                description: e.target.value,
                            });
                            setError("");
                        }}
                        placeholder="Describe what your community is about..."
                        rows={5}
                        maxLength={500}
                        className="w-full resize-none rounded-xl border border-plum/10 bg-white px-4 py-3 text-sm text-plum outline-none transition focus:border-plum"
                    />
                    <p className="mt-1 text-right text-xs text-plum/40">
                        {form.description.length}/500
                    </p>
                </div>

                {error && (
                    <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
                        {error}
                    </p>
                )}

                <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <Link
                        to="/dashboard/communities"
                        className="rounded-xl border border-plum/15 px-5 py-3 text-center text-sm font-semibold text-plum transition hover:bg-plum/5"
                    >
                        Cancel
                    </Link>

                    <button
                        type="submit"
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-plum px-5 py-3 text-sm font-semibold text-white transition hover:bg-plum/90"
                    >
                        <Plus size={17} />
                        Create Community
                    </button>
                </div>
            </form>
        </div>
    );
}

