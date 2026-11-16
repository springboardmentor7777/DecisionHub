import React, { createContext, useContext, useState } from "react";
import api from "../api/axios.js";
const AuthContext = createContext(null);
const DEV_ROLE = "user"; 

const DEV_USERS = {
    user: { id: 1, name: "Alex Johnson",    email: "alex@example.com",         role: "user" },
    admin:     { id: 2, name: "Sarah Admin",     email: "sarah@decisionhub.com",    role: "admin" },
    moderator: { id: 3, name: "Mike Moderator",  email: "mike@decisionhub.com",     role: "moderator" },
};
function loadUser() {
    if (DEV_ROLE) return DEV_USERS[DEV_ROLE];         
    try { return JSON.parse(localStorage.getItem("dh_user")); }
    catch { return null; }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(loadUser);

    const login = async (email, password) => {
        if (DEV_ROLE) {                                 
            setUser(DEV_USERS[DEV_ROLE]);
            return DEV_USERS[DEV_ROLE];
        }
        const { data } = await api.post("/api/auth/login", { email, password });
        localStorage.setItem("token", data.accessToken);
        const userData = {
            id: data.userId,
            name: data.fullName,
            email: data.email,
            role: String(data.role).toLowerCase(),
        };
        localStorage.setItem("dh_user", JSON.stringify(userData));
        setUser(userData);
        return userData;
    };

    const logout = () => {
        if (!DEV_ROLE) {
            localStorage.removeItem("token");
            localStorage.removeItem("dh_user");
        }
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
