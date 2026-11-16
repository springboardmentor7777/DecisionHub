import React from "react";
import { Menu } from "lucide-react";
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

            <div className="ml-auto flex items-center gap-2">
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
