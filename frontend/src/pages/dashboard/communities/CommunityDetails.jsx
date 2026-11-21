
import React, { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Users, Check } from "lucide-react";

export default function CommunityDetails() {
    const { id } = useParams();

    const [joinedIds, setJoinedIds] = useState(() => {
        try {
            return JSON.parse(
                localStorage.getItem("dh_joined_communities") || "[]"
            );
        } catch {
            return [];
        }
    });

    const sampleCommunities = [
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

    let communities = [];

    try {
        communities = JSON.parse(
            localStorage.getItem("dh_communities") || "[]"
        );
    } catch {
        communities = [];
    }

    const allCommunities = [...communities, ...sampleCommunities];

    const community = allCommunities.find(
        (item) => String(item.id) === id
    );

    if (!community) {
        return (
            <div className="rounded-2xl bg-white p-8 text-center">
                <h2 className="text-xl font-bold text-plum">
                    Community not found
                </h2>
                <Link
                    to="/dashboard/communities"
                    className="mt-4 inline-block text-sm font-semibold text-plum underline"
                >
                    Back to Communities
                </Link>
            </div>
        );
    }

    const isJoined = joinedIds.includes(community.id);

    const toggleJoin = () => {
        setJoinedIds((previous) => {
            const updated = previous.includes(community.id)
                ? previous.filter((item) => item !== community.id)
                : [...previous, community.id];

            localStorage.setItem(
                "dh_joined_communities",
                JSON.stringify(updated)
            );

            return updated;
        });
    };

    return (
        <div className="mx-auto max-w-3xl space-y-6">
            <Link
                to="/dashboard/communities"
                className="inline-flex items-center gap-2 text-sm font-medium text-plum/60 hover:text-plum"
            >
                <ArrowLeft size={17} />
                Back to Communities
            </Link>

            <div className="rounded-2xl border border-plum/10 bg-white p-6 shadow-sm sm:p-8">
                <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-sunset/15 text-plum">
                    <Users size={30} />
                </div>

                <span className="mt-5 inline-block rounded-full bg-plum/5 px-3 py-1 text-xs font-semibold text-plum">
                    {community.category}
                </span>

                <h1 className="mt-3 text-2xl font-bold text-plum sm:text-3xl">
                    {community.name}
                </h1>

                <p className="mt-3 leading-7 text-plum/60">
                    {community.description}
                </p>

                <div className="mt-6 flex items-center gap-2 text-sm text-plum/60">
                    <Users size={19} />
                    {(community.members + (isJoined ? 1 : 0)).toLocaleString()} members
                </div>

                <button
                    onClick={toggleJoin}
                    className={`mt-6 flex w-full items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold transition sm:w-auto ${isJoined
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
        </div>
    );
}
