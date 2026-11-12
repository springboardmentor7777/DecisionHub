import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Clock } from "lucide-react";

const loadComparisons = () => { try { return JSON.parse(localStorage.getItem("dh_comparisons")) ?? []; } catch { return []; } };

function CompareCard({ title, description, options, criteria, createdAt }) {
    return (
        <div className="rounded-2xl border border-plum/8 bg-white p-5 shadow-sm hover:shadow-md transition-all">
            <p className="font-semibold text-plum">{title}</p>
            {description && <p className="mt-1 text-xs text-plum/50 line-clamp-2">{description}</p>}

            {/* Options vs Criteria grid */}
            <div className="mt-4 overflow-x-auto">
                <table className="w-full text-xs">
                    <thead>
                        <tr>
                            <th className="pb-2 text-left font-bold text-plum/40 pr-3">Criteria</th>
                            {options.map((opt) => (
                                <th key={opt} className="pb-2 text-center font-semibold text-plum/70 px-2">
                                    <span className="rounded-lg bg-gradient-to-br from-sunset/10 to-orange/10 px-2 py-1 text-sunset">{opt}</span>
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {criteria.map((c) => (
                            <tr key={c} className="border-t border-plum/5">
                                <td className="py-2 pr-3 font-medium text-plum/60">{c}</td>
                                {options.map((opt) => (
                                    <td key={opt} className="py-2 px-2 text-center text-plum/20">—</td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="mt-3 flex items-center gap-3 text-[11px] text-plum/30">
                <span>{options.length} options</span>
                <span>·</span>
                <span>{criteria.length} criteria</span>
                <span>·</span>
                <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{new Date(createdAt).toLocaleDateString()}</span>
            </div>
        </div>
    );
}

export default function Compare() {
    const [comparisons] = useState(loadComparisons);

    return (
        <div className="space-y-8 pb-10">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-extrabold text-plum">Comparisons</h1>
                    <p className="mt-1 text-sm text-plum/50">{comparisons.length} comparison{comparisons.length !== 1 ? "s" : ""} created</p>
                </div>
                <Link to="/dashboard/compare/new"
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-orange to-coral px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-orange/25 transition hover:brightness-105">
                    <Plus className="h-4 w-4" /> New Comparison
                </Link>
            </div>

            {comparisons.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-plum/15 bg-white py-20 text-center">
                    <p className="text-4xl">⚖️</p>
                    <p className="mt-3 text-base font-semibold text-plum/50">No comparisons yet</p>
                    <p className="mt-1 text-sm text-plum/30">Compare options side by side with your community</p>
                    <Link to="/dashboard/compare/new"
                        className="mt-5 rounded-xl bg-gradient-to-r from-orange to-coral px-5 py-2.5 text-sm font-semibold text-white shadow-md transition hover:brightness-105">
                        Create Comparison
                    </Link>
                </div>
            ) : (
                <div className="grid gap-5 sm:grid-cols-2">
                    {comparisons.map((c) => <CompareCard key={c.id} {...c} />)}
                </div>
            )}
        </div>
    );
}
