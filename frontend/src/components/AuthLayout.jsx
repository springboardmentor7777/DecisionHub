import React from "react";
import authLandscape from "../assets/auth-landscape.png";

export default function AuthLayout({
                                       eyebrow,
                                       heading,
                                       highlight,
                                       subtext,
                                       features,
                                       illustration,
                                       children,
                                   }) {
    // Login par image ka left hissa hiker dikhata hai.
    // Register par right hissa signpost dikhata hai.
    const imagePosition = eyebrow === "Start Your" ? "right center" : "left center";

    return (
        <main className="flex min-h-screen w-full overflow-hidden bg-ivory text-plum">
            {/* Left brand and artwork panel */}
            <section className="relative hidden min-h-screen w-[46%] flex-col justify-between overflow-hidden bg-plum px-10 py-10 text-ivory lg:flex xl:px-14 xl:py-12">
                <img
                    src={authLandscape}
                    alt=""
                    aria-hidden="true"
                    className="absolute inset-0 h-full w-full object-cover"
                    style={{ objectPosition: imagePosition }}
                />

                {/* Dark overlay keeps the white copy legible over the artwork */}
                <div className="absolute inset-0 bg-gradient-to-b from-plum/85 via-plum/45 to-burgundy/35" />

                {/* Soft brand glow */}
                <div className="pointer-events-none absolute -left-24 -top-24 h-80 w-80 rounded-full bg-sunset/20 blur-3xl" />
                <div className="pointer-events-none absolute -bottom-24 right-0 h-80 w-80 rounded-full bg-orange/20 blur-3xl" />

                <div className="relative z-10">
                    {/* Brand mark */}
                    <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-sunset to-orange text-white shadow-lg shadow-sunset/30">
              <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  className="h-6 w-6"
                  fill="currentColor"
              >
                <circle cx="8" cy="8" r="3" />
                <circle cx="16.5" cy="9" r="2.5" />
                <path d="M2.5 19c0-3.1 2.4-5.5 5.5-5.5s5.5 2.4 5.5 5.5v1h-11v-1Z" />
                <path d="M14 14c.7-.5 1.6-.8 2.5-.8 2.8 0 5 2.1 5 4.8v1h-5.2c-.1-2-.9-3.7-2.3-5Z" />
              </svg>
            </span>

                        <span className="text-xl font-extrabold tracking-tight text-white">
              Decision<span className="text-coral">Hub</span>
            </span>
                    </div>

                    <p className="mt-12 text-xs font-bold uppercase tracking-[0.24em] text-orange">
                        {eyebrow}
                    </p>

                    <h1 className="mt-3 max-w-lg text-4xl font-extrabold leading-[1.08] tracking-tight xl:text-5xl">
                        {heading}{" "}
                        <span className="bg-gradient-to-r from-sunset to-orange bg-clip-text text-transparent">
              {highlight}
            </span>
                    </h1>

                    <p className="mt-5 max-w-md text-sm leading-6 text-ivory/90 xl:text-base xl:leading-7">
                        {subtext}
                    </p>

                    <ul className="mt-8 space-y-3">
                        {features.map((feature) => (
                            <li
                                key={feature.label}
                                className="flex items-center gap-3 text-sm font-medium text-white/95"
                            >
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-white/15 bg-white/10 text-base shadow-inner">
                  {feature.icon}
                </span>
                                {feature.label}
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Existing illustration is kept above the background artwork */}
                {illustration && (
                    <div className="relative z-10 mt-4 max-w-xs opacity-90">
                        {illustration}
                    </div>
                )}
            </section>

            {/* Form panel */}
            <section className="relative flex min-h-screen flex-1 items-center justify-center overflow-hidden px-6 py-10 sm:px-10 lg:px-12">
                {/* Curved edge extending into the brand panel */}
                <svg
                    aria-hidden="true"
                    className="pointer-events-none absolute -left-[120px] top-0 hidden h-full w-[121px] lg:block"
                    viewBox="0 0 121 1000"
                    preserveAspectRatio="none"
                >
                    <path
                        d="M121 0 C35 90 45 210 82 330 C121 455 120 565 72 690 C30 800 44 910 121 1000 L121 0 Z"
                        fill="#fdf8f3"
                    />
                </svg>

                <div className="auth-form relative z-10 w-full max-w-md">
                    {/* Mobile brand */}
                    <div className="mb-8 flex items-center gap-3 lg:hidden">
            <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-sunset to-orange text-white shadow-md shadow-sunset/20">
              <span className="text-sm font-extrabold">DH</span>
            </span>
                        <span className="text-xl font-extrabold tracking-tight text-plum">
              Decision<span className="text-coral">Hub</span>
            </span>
                    </div>

                    {children}
                </div>
            </section>
        </main>
    );
}