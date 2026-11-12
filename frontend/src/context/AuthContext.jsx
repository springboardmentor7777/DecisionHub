import React, { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

const MOCK_USERS = {
    user: {
        id: 1,
        name: "Alex Johnson",
        email: "alex@example.com",
        role: "user",
        avatar: null,
        joinedAt: "Jan 2025",
    },
    admin: {
        id: 2,
        name: "Sarah Admin",
        email: "sarah@decisionhub.com",
        role: "admin",
        avatar: null,
        joinedAt: "Oct 2024",
    },
    moderator: {
        id: 3,
        name: "Mike Moderator",
        email: "mike@decisionhub.com",
        role: "moderator",
        avatar: null,
        joinedAt: "Nov 2024",
    },
};

export function AuthProvider({ children }) {
    const [user, setUser] = useState(MOCK_USERS.user); 

    const login = (role = "user") => setUser(MOCK_USERS[role] ?? MOCK_USERS.user);
    const logout = () => setUser(null);

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
