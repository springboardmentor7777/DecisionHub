import React from "react";

/* Simple, original line-art — a figure cresting a hill with a flag.
   Stands in for "decision-making journey" without pulling in image assets. */
export function JourneyIllustration() {
    return (
        <svg viewBox="0 0 320 160" className="w-full max-w-xs" fill="none">
            <path
                d="M0 140 Q60 90 120 130 T240 110 T320 140 V160 H0 Z"
                fill="currentColor"
                className="text-ivory/10"
            />
            <path
                d="M0 150 Q80 110 170 145 T320 130"
                stroke="currentColor"
                strokeWidth="2"
                className="text-sunset/60"
                fill="none"
                strokeDasharray="4 6"
            />
            {/* flag */}
            <line x1="150" y1="60" x2="150" y2="108" stroke="currentColor" strokeWidth="3" className="text-ivory/80" />
            <path d="M150 60 L182 70 L150 80 Z" className="fill-coral" />
            {/* figure */}
            <circle cx="120" cy="100" r="7" className="fill-ivory" />
            <path d="M120 107 L112 128 M120 107 L128 126 M120 112 L104 120 M120 112 L133 108" stroke="currentColor" strokeWidth="3" strokeLinecap="round" className="text-ivory" />
        </svg>
    );
}

/* Signpost with three directions — stands in for "community collaboration". */
export function SignpostIllustration() {
    return (
        <svg viewBox="0 0 320 160" className="w-full max-w-xs" fill="none">
            <path
                d="M0 140 Q90 100 170 135 T320 125 V160 H0 Z"
                fill="currentColor"
                className="text-ivory/10"
            />
            <line x1="140" y1="40" x2="140" y2="140" stroke="currentColor" strokeWidth="4" className="text-ivory/80" />
            <g className="fill-coral">
                <path d="M140 55 h70 l-10 14 10 14 h-70 Z" />
            </g>
            <g className="fill-sunset">
                <path d="M140 90 h-55 l10 14 -10 14 h55 Z" />
            </g>
            <g className="fill-orange">
                <path d="M140 72 h50 l-9 11 9 11 h-50 Z" opacity="0.9" />
            </g>
            <circle cx="140" cy="146" r="10" className="text-ivory/20 fill-current" />
        </svg>
    );
}