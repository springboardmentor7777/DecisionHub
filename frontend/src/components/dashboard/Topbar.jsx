import React from "react";
import { Menu, Search, Bell } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

export default function Topbar({ onMenuClick }) {
    const { user } = useAuth();

    return (
        <header className="sticky top-0 z-10 flex h-16 items-center gap-4 border-b border-plum/8 bg-ivory/90 px-4 backdrop-blur-md sm:px-6">
            <button
                onClick={onMenuClick}
                className="flex h-9 w-9 items-center justify-center rounded-xl text-plum/60 transition hover:bg-plum/8 hover:text-plum lg:hidden"
                aria-label="Open menu"
            >
                <Menu className="h-5 w-5" />
            </button>

            <div className="relative flex-1 max-w-sm">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-plum/30" />
                <input
                    type="search"
                    placeholder="Search boards, polls…"
                    className="w-full rounded-xl border border-plum/10 bg-white py-2 pl-9 pr-4 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                />
            </div>

            <div className="ml-auto flex items-center gap-2">
                <button className="relative flex h-9 w-9 items-center justify-center rounded-xl text-plum/60 transition hover:bg-plum/8 hover:text-plum">
                    <Bell className="h-5 w-5" />
                    <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-coral" />
                </button>

                <div className="flex items-center gap-2.5 rounded-xl border border-plum/10 bg-white px-3 py-1.5 shadow-sm">
                    <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-sunset to-orange text-xs font-bold text-white">
                        {user?.name.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()}
                    </span>
                    <span className="hidden text-sm font-medium text-plum sm:block">{user?.name.split(" ")[0]}</span>
                </div>
            </div>
        </header>
    );
}
