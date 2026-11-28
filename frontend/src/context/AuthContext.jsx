
import React, {
    createContext,
    useContext,
    useEffect,
    useState,
} from "react";

import api from "../api/axios.js";

const AuthContext = createContext(null);

// Convert backend user response into frontend user format
function formatUser(data) {
    return {
        id: data.userId,
        name: data.fullName,
        email: data.email,
        role: String(data.role).toLowerCase(),
    };
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Restore and validate an existing session
    useEffect(() => {
        const restoreSession = async () => {
            const token = localStorage.getItem("token");

            if (!token) {
                localStorage.removeItem("dh_user");
                setLoading(false);
                return;
            }

            try {
                const { data } = await api.get("/api/auth/me");

                const userData = formatUser(data);

                localStorage.setItem(
                    "dh_user",
                    JSON.stringify(userData)
                );

                setUser(userData);
            } catch (error) {
                localStorage.removeItem("token");
                localStorage.removeItem("dh_user");
                setUser(null);
            } finally {
                setLoading(false);
            }
        };

        restoreSession();
    }, []);

    // Login using Spring Boot API
    const login = async (email, password) => {
        const { data } = await api.post("/api/auth/login", {
            email,
            password,
        });

        if (!data.accessToken) {
            throw new Error("Authentication token was not received.");
        }

        localStorage.setItem("token", data.accessToken);

        const userData = formatUser(data);

        localStorage.setItem(
            "dh_user",
            JSON.stringify(userData)
        );

        setUser(userData);

        return userData;
    };

    // Logout and clear session
    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("dh_user");

        setUser(null);
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
