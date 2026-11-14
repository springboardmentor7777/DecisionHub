import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Mail, Lock, Eye, EyeOff, LayoutDashboard, Vote, Scale, MessageSquare } from "lucide-react";
import api from "../api/axios.js";
import AuthLayout from "../components/AuthLayout";
import { JourneyIllustration } from "../components/AuthIllustrations";
import { GoogleIcon, GithubIcon } from "../components/SocialIcons";

export default function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [form, setForm] = useState({ email: "", password: "", remember: false });
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
    };

    const validate = () => {
        const next = {};
        if (!form.email.trim()) next.email = "Email is required";
        else if (!/^\S+@\S+\.\S+$/.test(form.email)) next.email = "Enter a valid email";
        if (!form.password) next.password = "Password is required";
        setErrors(next);
        return Object.keys(next).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;
        setSubmitting(true);
        setErrors({});
        try {
            const { data } = await api.post("/api/auth/login", {
                email: form.email.trim(),
                password: form.password,
            });
            localStorage.setItem("token", data.accessToken);
            login(
                {
                    id: data.userId,
                    name: data.fullName,
                    email: data.email,
                    role: String(data.role).toLowerCase(), // USER -> user
                },
                data.accessToken
            );
            navigate("/dashboard");
        } catch (err) {
            const status = err.response?.status;
            setErrors({
                form:
                    !err.response
                        ? "Cannot reach the server. Please try again."
                        : status === 400 || status === 401 || status === 403
                            ? "Invalid email or password"
                            : err.response.data?.message || "Something went wrong. Please try again.",
            });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <AuthLayout
            eyebrow="Your Community"
            heading="Decision"
            highlight="Hub."
            subtext="Create boards, compare options, vote, and make better decisions with the power of community."
            features={[
                { icon: <LayoutDashboard className="h-4 w-4" />, label: "Create Decision Boards" },
                { icon: <Vote className="h-4 w-4" />, label: "Vote & Run Polls" },
                { icon: <Scale className="h-4 w-4" />, label: "Compare Options" },
                { icon: <MessageSquare className="h-4 w-4" />, label: "Discuss & Get Insights" },
            ]}
            illustration={<JourneyIllustration />}
        >
            <h2 className="text-3xl font-extrabold text-plum">Welcome Back!</h2>
            <p className="mt-1.5 text-sm text-plum/60">Login to your DecisionHub account</p>

            <form className="mt-8 space-y-4" onSubmit={handleSubmit} noValidate>
                <div>
                    <label className="mb-1.5 block text-sm font-medium text-plum/80">Email</label>
                    <div className="relative">
                        <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-plum/40" />
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            placeholder="Enter your email"
                            className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-4 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                    </div>
                    {errors.email && <p className="mt-1 text-xs text-burgundy">{errors.email}</p>}
                </div>

                <div>
                    <label className="mb-1.5 block text-sm font-medium text-plum/80">Password</label>
                    <div className="relative">
                        <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-plum/40" />
                        <input
                            type={showPassword ? "text" : "password"}
                            name="password"
                            value={form.password}
                            onChange={handleChange}
                            placeholder="Enter your password"
                            className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-11 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword((s) => !s)}
                            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-plum/40 hover:text-plum/70"
                            aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                            {showPassword ? <EyeOff className="h-4.5 w-4.5" /> : <Eye className="h-4.5 w-4.5" />}
                        </button>
                    </div>
                    {errors.password && <p className="mt-1 text-xs text-burgundy">{errors.password}</p>}
                </div>

                <div className="flex items-center justify-between pt-1 text-sm">
                    <label className="flex items-center gap-2 text-plum/70">
                        <input
                            type="checkbox"
                            name="remember"
                            checked={form.remember}
                            onChange={handleChange}
                            className="h-4 w-4 rounded border-plum/30 text-sunset focus:ring-sunset/30"
                        />
                        Remember me
                    </label>
                    <Link to="/forgot-password" className="font-medium text-coral hover:text-burgundy">
                        Forgot password?
                    </Link>
                </div>

                {errors.form && (
                    <p className="rounded-lg bg-burgundy/10 px-3 py-2 text-sm text-burgundy">{errors.form}</p>
                )}

                <button
                    type="submit"
                    disabled={submitting}
                    className="mt-2 w-full rounded-xl bg-gradient-to-r from-sunset to-orange py-3.5 text-sm font-semibold text-white shadow-lg shadow-sunset/25 transition hover:brightness-105 active:scale-[0.99] disabled:opacity-60"
                >
                    {submitting ? "Logging in..." : "Log In"}
                </button>
            </form>

            <div className="my-6 flex items-center gap-3 text-xs text-plum/40">
                <span className="h-px flex-1 bg-plum/10" />
                Or continue with
                <span className="h-px flex-1 bg-plum/10" />
            </div>

            <div className="grid grid-cols-2 gap-3">
                <button className="flex items-center justify-center gap-2 rounded-xl border border-plum/10 bg-white py-2.5 text-sm font-medium text-plum/80 transition hover:border-plum/20 hover:bg-plum/5">
                    <GoogleIcon className="h-4.5 w-4.5" /> Google
                </button>
                <button className="flex items-center justify-center gap-2 rounded-xl border border-plum/10 bg-white py-2.5 text-sm font-medium text-plum/80 transition hover:border-plum/20 hover:bg-plum/5">
                    <GithubIcon className="h-4.5 w-4.5" /> GitHub
                </button>
            </div>

            <p className="mt-7 text-center text-sm text-plum/60">
                Don&apos;t have an account?{" "}
                <Link to="/register" className="font-semibold text-coral hover:text-burgundy">
                    Register Now
                </Link>
            </p>
        </AuthLayout>
    );
}