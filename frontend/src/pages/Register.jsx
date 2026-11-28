
import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { User, Mail, Lock, Eye, EyeOff, Vote, Scale, Users } from "lucide-react";
import api from "../api/axios.js";
import AuthLayout from "../components/AuthLayout";
import { SignpostIllustration } from "../components/AuthIllustrations";
import { GoogleIcon, GithubIcon } from "../components/SocialIcons";

export default function Register() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        fullName: "",
        email: "",
        password: "",
        confirmPassword: "",
    });

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const validate = () => {
        const next = {};

        if (!form.fullName.trim()) next.fullName = "Full name is required";

        if (!form.email.trim()) next.email = "Email is required";
        else if (!/^\S+@\S+\.\S+$/.test(form.email))
            next.email = "Enter a valid email";

        if (!form.password) next.password = "Password is required";
        else if (form.password.length < 8)
            next.password = "Minimum 8 characters";

        if (form.confirmPassword !== form.password)
            next.confirmPassword = "Passwords don't match";

        setErrors(next);
        return Object.keys(next).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validate()) return;

        setSubmitting(true);
        setErrors({});

        try {
            await api.post("/api/auth/register", {
                fullName: form.fullName.trim(),
                email: form.email.trim(),
                password: form.password,
            });

            navigate("/login");
        } catch (err) {
            const data = err.response?.data;

            const fieldMsg =
                data && typeof data === "object"
                    ? Object.values(data).find((v) => typeof v === "string")
                    : null;

            setErrors({
                form: !err.response
                    ? "Cannot reach the server. Please try again."
                    : data?.message ||
                    fieldMsg ||
                    "Registration failed. Please try again.",
            });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <AuthLayout
            eyebrow="Start Your"
            heading="Decision"
            highlight="Journey."
            subtext="Join a community of thinkers, share opinions, run polls and make smarter choices together."
            features={[
                { icon: <Vote className="h-4 w-4" />, label: "Vote" },
                { icon: <Scale className="h-4 w-4" />, label: "Compare" },
                { icon: <Users className="h-4 w-4" />, label: "Collaborate" },
            ]}
            illustration={<SignpostIllustration />}
        >
            <h2 className="text-3xl font-extrabold text-plum">
                Create Your Account
            </h2>

            <p className="mt-1.5 text-sm text-plum/60">
                Join DecisionHub and be part of a collaborative decision-making community.
            </p>

            <form className="mt-8 space-y-4" onSubmit={handleSubmit} noValidate>
                {/* Full Name */}
                <div>
                    <label className="mb-1.5 block text-sm font-medium text-plum/80">
                        Full Name
                    </label>

                    <div className="relative">
                        <User className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-plum/40" />

                        <input
                            type="text"
                            name="fullName"
                            value={form.fullName}
                            onChange={handleChange}
                            placeholder="Full name"
                            className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-4 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                        />
                    </div>

                    {errors.fullName && (
                        <p className="mt-1 text-xs text-burgundy">
                            {errors.fullName}
                        </p>
                    )}
                </div>

                {/* Email */}
                <div>
                    <label className="mb-1.5 block text-sm font-medium text-plum/80">
                        Email
                    </label>

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

                    {errors.email && (
                        <p className="mt-1 text-xs text-burgundy">
                            {errors.email}
                        </p>
                    )}
                </div>

                {/* Password fields */}
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    {/* Password */}
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">
                            Password
                        </label>

                        <div className="relative">
                            <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-plum/40" />

                            <input
                                type={showPassword ? "text" : "password"}
                                name="password"
                                value={form.password}
                                onChange={handleChange}
                                placeholder="Create password"
                                className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-11 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                            />

                            <button
                                type="button"
                                onClick={() => setShowPassword((s) => !s)}
                                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-plum/40 hover:text-plum/70"
                                aria-label={
                                    showPassword ? "Hide password" : "Show password"
                                }
                            >
                                {showPassword ? (
                                    <EyeOff className="h-4.5 w-4.5" />
                                ) : (
                                    <Eye className="h-4.5 w-4.5" />
                                )}
                            </button>
                        </div>

                        {errors.password && (
                            <p className="mt-1 text-xs text-burgundy">
                                {errors.password}
                            </p>
                        )}
                    </div>

                    {/* Confirm Password */}
                    <div>
                        <label className="mb-1.5 block text-sm font-medium text-plum/80">
                            Confirm Password
                        </label>

                        <div className="relative">
                            <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-plum/40" />

                            <input
                                type={showConfirmPassword ? "text" : "password"}
                                name="confirmPassword"
                                value={form.confirmPassword}
                                onChange={handleChange}
                                placeholder="Confirm password"
                                className="w-full rounded-xl border border-plum/10 bg-white py-3 pl-11 pr-11 text-sm text-plum placeholder:text-plum/30 outline-none transition focus:border-sunset focus:ring-2 focus:ring-sunset/20"
                            />

                            <button
                                type="button"
                                onClick={() =>
                                    setShowConfirmPassword((s) => !s)
                                }
                                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-plum/40 hover:text-plum/70"
                                aria-label={
                                    showConfirmPassword
                                        ? "Hide confirm password"
                                        : "Show confirm password"
                                }
                            >
                                {showConfirmPassword ? (
                                    <EyeOff className="h-4.5 w-4.5" />
                                ) : (
                                    <Eye className="h-4.5 w-4.5" />
                                )}
                            </button>
                        </div>

                        {errors.confirmPassword && (
                            <p className="mt-1 text-xs text-burgundy">
                                {errors.confirmPassword}
                            </p>
                        )}
                    </div>
                </div>

                {/* Error message */}
                {errors.form && (
                    <p className="rounded-lg bg-burgundy/10 px-3 py-2 text-sm text-burgundy">
                        {errors.form}
                    </p>
                )}

                {/* Submit */}
                <button
                    type="submit"
                    disabled={submitting}
                    className="mt-2 w-full rounded-xl bg-gradient-to-r from-sunset to-orange py-3.5 text-sm font-semibold text-white shadow-lg shadow-sunset/25 transition hover:brightness-105 active:scale-[0.99] disabled:opacity-60"
                >
                    {submitting ? "Creating account..." : "Create Account"}
                </button>
            </form>

            <div className="my-6 flex items-center gap-3 text-xs text-plum/40">
                <span className="h-px flex-1 bg-plum/10" />
                Or sign up with
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
                Already have an account?{" "}
                <Link
                    to="/login"
                    className="font-semibold text-coral hover:text-burgundy"
                >
                    Log In
                </Link>
            </p>
        </AuthLayout>
    );
}
