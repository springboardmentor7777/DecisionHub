import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
    LayoutDashboard, Vote, GitCompare, MessageSquare,
    LogOut, X, ChevronRight,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";

const USER_NAV = [
    { icon: LayoutDashboard, label: "Dashboard", to: "/dashboard" },
    { icon: Vote,            label: "My Polls",  to: "/dashboard/votes" },
    { icon: GitCompare,      label: "Compare",   to: "/dashboard/compare" },
    { icon: MessageSquare,   label: "Discuss",   to: "/dashboard/discuss" },
];

function Avatar({ user, size = "md" }) {
    const sz = size === "md" ? "h-10 w-10 text-base" : "h-8 w-8 text-xs";
    const initials = user.name.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase();
    return (
        <span className={`${sz} flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-sunset to-orange font-bold text-white shadow`}>
            {initials}
        </span>
    );
}

export default function Sidebar({ open, onClose }) {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const roleLabel = {
        user: "Member",
        admin: "Administrator",
        moderator: "Moderator",
    }[user?.role] ?? "Member";

    const roleBadgeColor = {
        user: "bg-sunset/10 text-sunset",
        admin: "bg-burgundy/10 text-burgundy",
        moderator: "bg-orange/10 text-orange",
    }[user?.role] ?? "bg-sunset/10 text-sunset";

    return (
        <>
            {/* Mobile backdrop */}
            {open && (
                <div
                    className="fixed inset-0 z-20 bg-plum/40 backdrop-blur-sm lg:hidden"
                    onClick={onClose}
                />
            )}

            <aside
                className={`
                    fixed inset-y-0 left-0 z-30 flex w-64 flex-col bg-plum text-ivory shadow-2xl
                    transition-transform duration-300 ease-in-out
                    ${open ? "translate-x-0" : "-translate-x-full"}
                    lg:relative lg:translate-x-0 lg:shadow-none
                `}
            >
                {/* Brand */}
                <div className="flex items-center justify-between px-5 py-5 border-b border-white/10">
                    <div className="flex items-center gap-3">
                        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-sunset to-orange shadow-lg shadow-sunset/30">
                            <svg viewBox="0 0 24 24" className="h-5 w-5" fill="currentColor">
                                <circle cx="8" cy="8" r="3" />
                                <circle cx="16.5" cy="9" r="2.5" />
                                <path d="M2.5 19c0-3.1 2.4-5.5 5.5-5.5s5.5 2.4 5.5 5.5v1h-11v-1Z" />
                                <path d="M14 14c.7-.5 1.6-.8 2.5-.8 2.8 0 5 2.1 5 4.8v1h-5.2c-.1-2-.9-3.7-2.3-5Z" />
                            </svg>
                        </span>
                        <span className="text-lg font-extrabold tracking-tight">
                            Decision<span className="text-coral">Hub</span>
                        </span>
                    </div>
                    <button onClick={onClose} className="lg:hidden text-ivory/50 hover:text-ivory">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* User card */}
                <div className="mx-4 mt-5 rounded-2xl bg-white/8 p-4 border border-white/10">
                    <div className="flex items-center gap-3">
                        <Avatar user={user} />
                        <div className="min-w-0">
                            <p className="truncate text-sm font-semibold text-white">{user?.name}</p>
                            <span className={`mt-0.5 inline-block rounded-full px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide ${roleBadgeColor}`}>
                                {roleLabel}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Nav */}
                <nav className="mt-5 flex-1 space-y-0.5 px-3 overflow-y-auto">
                    <p className="mb-2 px-3 text-[10px] font-bold uppercase tracking-widest text-ivory/30">Menu</p>
                    {USER_NAV.map(({ icon: Icon, label, to }) => (
                        <NavLink
                            key={to}
                            to={to}
                            end={to === "/dashboard"}
                            onClick={onClose}
                            className={({ isActive }) =>
                                `group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all
                                ${isActive
                                    ? "bg-gradient-to-r from-sunset/90 to-orange/80 text-white shadow-md shadow-sunset/20"
                                    : "text-ivory/70 hover:bg-white/8 hover:text-white"
                                }`
                            }
                        >
                            <Icon className="h-4.5 w-4.5 shrink-0" />
                            <span className="flex-1">{label}</span>
                            <ChevronRight className="h-3.5 w-3.5 opacity-0 group-hover:opacity-50 transition-opacity" />
                        </NavLink>
                    ))}
                </nav>

                {/* Logout */}
                <div className="p-4 border-t border-white/10">
                    <button
                        onClick={handleLogout}
                        className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-ivory/60 transition hover:bg-white/8 hover:text-coral"
                    >
                        <LogOut className="h-4.5 w-4.5" />
                        Log Out
                    </button>
                </div>
            </aside>
        </>
    );
}
