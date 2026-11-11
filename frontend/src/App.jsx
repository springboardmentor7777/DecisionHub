import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";

function AppContent() {
    const { pathname } = useLocation();
    const isAuthPage = pathname === "/login" || pathname === "/register";

    return (
        <>
            {!isAuthPage && <Navbar />}

            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
            </Routes>
        </>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
}