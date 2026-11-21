
import React, { useState } from "react";
import { Search, Users, Check, Plus } from "lucide-react";
import { Link } from "react-router-dom";

const categories = [
    "All",
    "Career",
    "Education",
    "Technology",
    "Travel",
    "Finance",
    "Lifestyle",
];

const initialCommunities = [
    {
        id: 1,
        name: "Software Developers",
        category: "Technology",
        description: "Connect with developers and discuss programming, projects and new technologies.",
        members: 1250,
    },
    {
        id: 2,
        name: "Career Growth",
        category: "Career",
        description: "Share job opportunities, interview experiences and career advice.",
        members: 850,
    },
    {
        id: 3,
        name: "Students Hub",
        category: "Education",
        description: "A place for students to share knowledge, notes and learning resources.",
        members: 2100,
    },
    {
        id: 4,
        name: "Travel Explorers",
        category: "Travel",
        description: "Discover destinations and share your travel experiences.",
        members: 640,
    },
    {
        id: 5,
        name: "Personal Finance",
        category: "Finance",
        description: "Discuss saving, budgeting and personal financial planning.",
        members: 920,
    },
    {
        id: 6,
        name: "Healthy Lifestyle",
        category: "Lifestyle",
        description: "Share ideas about daily habits, hobbies and balanced living.",
        members: 570,
    },
];

export default function Communities() {
    const [communities] = useState(() => {
        try {
            const savedCommunities = JSON.parse(
                localStorage.getItem("dh_communities") || "[]"
            );

            return [...savedCommunities, ...initialCommunities];
        } catch {
            return initialCommunities;
        }
    });
    const [selectedCategory, setSelectedCategory] = useState("All");
    const [search, setSearch] = useState("");
    const [joinedIds, setJoinedIds] = useState(() => {
        try {
            return JSON.parse(
                localStorage.getItem("dh_joined_communities") || "[]"
            );
        } catch {
            return [];
        }
    });

    const filteredCommunities = communities.filter((community) => {
        const matchesCategory =
            selectedCategory === "All" ||
            community.category === selectedCategory;

        const matchesSearch =
            community.name.toLowerCase().includes(search.toLowerCase()) ||
            community.description.toLowerCase().includes(search.toLowerCase());

        return matchesCategory && matchesSearch;
    });

    const toggleJoin = (id) => {
        setJoinedIds((previous) => {
            const updated = previous.includes(id)
                ? previous.filter((communityId) => communityId !== id)
                : [...previous, id];

            localStorage.setItem(
                "dh_joined_communities",
                JSON.stringify(updated)
            );

            return updated;
        });
    };

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-plum sm:text-3xl">
                        Communities
                    </h1>
                    <p className="mt-1 text-sm text-plum/60">
                        Discover communities, connect with people and share ideas.
                    </p>
                </div>

                <Link
                    to="/dashboard/communities/new"
                    className="inline-flex items-center justify-center gap-2 rounded-xl bg-plum px-4 py-3 text-sm font-semibold text-white transition hover:bg-plum/90"
                >
                    <Plus size={18} />
                    Create Community
                </Link>
            </div>

            {/* Search */}
            <div className="relative">
                <Search
                    size={19}
                    className="absolute left-4 top-1/2 -translate-y-1/2 text-plum/40"
                />
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search communities..."
                    className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-4 text-sm text-plum outline-none transition focus:border-plum"
                />
            </div>

            {/* Categories */}
            <div>
                <h2 className="mb-3 text-sm font-semibold text-plum">
                    Explore Categories
                </h2>

                <div className="flex flex-wrap gap-2">
                    {categories.map((category) => (
                        <button
                            key={category}
                            onClick={() => setSelectedCategory(category)}
                            className={`rounded-full px-4 py-2 text-sm font-medium transition ${selectedCategory === category
                                ? "bg-plum text-white"
                                : "border border-plum/10 bg-white text-plum/70 hover:bg-plum/5"
                                }`}
                        >
                            {category}
                        </button>
                    ))}
                </div>
            </div>

            {/* Community Count */}
            <div className="flex items-center justify-between">
                <h2 className="text-lg font-bold text-plum">
                    Discover Communities
                </h2>
                <span className="text-sm text-plum/50">
                    {filteredCommunities.length} communities
                </span>
            </div>

            {/* Community Cards */}
            {filteredCommunities.length > 0 ? (
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                    {filteredCommunities.map((community) => {
                        const isJoined = joinedIds.includes(community.id);

                        return (
                            <div
                                key={community.id}
                                className="flex flex-col rounded-2xl border border-plum/10 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
                            >
                                <div className="mb-4 flex items-center justify-between">
                                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-sunset/15 text-plum">
                                        <Users size={23} />
                                    </div>

                                    <span className="rounded-full bg-plum/5 px-3 py-1 text-xs font-medium text-plum">
                                        {community.category}
                                    </span>
                                </div>

                                <h3 className="text-lg font-bold text-plum">
                                    {community.name}
                                </h3>

                                <p className="mt-2 min-h-12 text-sm leading-6 text-plum/60">
                                    {community.description}
                                </p>
                                <Link
                                    to={`/dashboard/communities/${community.id}`}
                                    className="mt-3 inline-block text-sm font-semibold text-plum hover:underline"
                                >
                                    View Details →
                                </Link>

                                <div className="mt-4 flex items-center gap-2 text-sm text-plum/60">
                                    <Users size={16} />
                                    {(
                                        community.members +
                                        (isJoined ? 1 : 0)
                                    ).toLocaleString()} members
                                </div>

                                <button
                                    onClick={() => toggleJoin(community.id)}
                                    className={`mt-5 flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition ${isJoined
                                        ? "border border-plum/20 bg-white text-plum hover:bg-red-50 hover:text-red-600"
                                        : "bg-plum text-white hover:bg-plum/90"
                                        }`}
                                >
                                    {isJoined ? (
                                        <>
                                            <Check size={17} />
                                            Joined
                                        </>
                                    ) : (
                                        "Join Community"
                                    )}
                                </button>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <div className="rounded-2xl border border-dashed border-plum/20 bg-white p-10 text-center">
                    <Users className="mx-auto mb-3 text-plum/30" size={35} />
                    <h3 className="font-semibold text-plum">
                        No communities found
                    </h3>
                    <p className="mt-1 text-sm text-plum/60">
                        Try another search or select a different category.
                    </p>
                </div>
            )}
        </div>
    );
}
