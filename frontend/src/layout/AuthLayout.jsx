import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import Logo from '../components/common/Logo';

/**
 * Sample boards shown beside the form. They are illustrations, not live data.
 */
const SAMPLES = {
  login: {
    heading: 'Pick the option your people can back.',
    text: 'Compare choices side by side, ask friends or a community to vote, and see where everyone lands.',
    board: {
      title: 'Goa or Bali for the March trip?',
      options: [
        { name: 'Goa', pct: 58, votes: 74 },
        { name: 'Bali', pct: 42, votes: 54 },
      ],
      total: 128,
      note: 'Closes in 2 days',
    },
  },
  register: {
    heading: 'Put your next decision to a vote.',
    text: 'Create a board, list your options, and let friends or communities weigh in before you commit.',
    board: {
      title: 'Startup offer or corporate role?',
      options: [
        { name: 'Startup offer', pct: 47, votes: 101 },
        { name: 'Corporate role', pct: 39, votes: 83 },
        { name: 'Wait a year', pct: 14, votes: 30 },
      ],
      total: 214,
      note: '12 comments',
    },
  },
};

const AVATARS = [
  { initials: 'AM', cls: 'bg-brand-200 text-brand-900' },
  { initials: 'RK', cls: 'bg-marigold-100 text-marigold-600' },
  { initials: 'SP', cls: 'bg-brand-400 text-white' },
];

const SampleBoard = ({ board }) => (
  <figure
    aria-label="Example of a decision board"
    className="relative mt-8 max-w-md lg:mt-10"
  >
    {/* second sheet peeking out behind the board */}
    <div
      aria-hidden="true"
      className="absolute inset-x-4 -bottom-2.5 h-full rounded-2xl bg-white/20"
    />
    <div className="relative rounded-2xl bg-paper text-ink p-5 shadow-2xl shadow-black/25 lg:p-6">
      <p className="text-xs font-semibold text-brand-700">Sample board</p>
      <h3 className="mt-1 font-display text-xl font-bold leading-snug tracking-tight">
        {board.title}
      </h3>

      <ul className="mt-5 space-y-4">
        {board.options.map((opt, i) => {
          const leading = i === 0;
          return (
            <li key={opt.name}>
              <div className="flex items-baseline justify-between text-sm">
                <span className={leading ? 'font-bold' : 'font-medium text-ink/75'}>
                  {opt.name}
                </span>
                <span className="tabular-nums">
                  <span className={leading ? 'font-bold' : 'font-semibold text-ink/75'}>
                    {opt.pct}%
                  </span>
                  <span className="ml-2 hidden text-xs text-ink/50 lg:inline">{opt.votes} votes</span>
                </span>
              </div>
              <div className="mt-1.5 h-2.5 rounded-full bg-brand-900/10 overflow-hidden">
                <div
                  className={`h-full origin-left rounded-full animate-grow-x ${
                    leading ? 'bg-marigold-400' : 'bg-brand-400'
                  }`}
                  style={{ width: `${opt.pct}%`, animationDelay: `${350 + i * 150}ms` }}
                />
              </div>
            </li>
          );
        })}
      </ul>

      <figcaption className="mt-6 flex items-center justify-between border-t border-brand-900/10 pt-4">
        <div className="flex items-center">
          <div className="flex -space-x-1.5">
            {AVATARS.map((a) => (
              <span
                key={a.initials}
                className={`inline-flex h-7 w-7 items-center justify-center rounded-full border-2 border-paper text-[10px] font-bold ${a.cls}`}
              >
                {a.initials}
              </span>
            ))}
          </div>
          <span className="ml-3 text-xs text-ink/60">{board.total} voted</span>
        </div>
        <span className="hidden text-xs font-medium text-ink/60 lg:block">{board.note}</span>
      </figcaption>
    </div>
  </figure>
);

export const AuthLayout = ({ children }) => {
  const { pathname } = useLocation();
  const sample = pathname.startsWith('/register') ? SAMPLES.register : SAMPLES.login;

  return (
    <div className="min-h-screen bg-paper md:grid md:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      {/* Brand panel: tablet and up */}
      <aside className="relative hidden flex-col justify-between overflow-hidden bg-brand-900 px-8 py-10 text-white md:flex lg:px-12 xl:px-16">
        {/* faint concentric rings */}
        <svg
          aria-hidden="true"
          className="pointer-events-none absolute -bottom-40 -right-40 h-[34rem] w-[34rem] text-white/[0.06]"
          viewBox="0 0 100 100"
          fill="none"
          stroke="currentColor"
          strokeWidth="0.6"
        >
          <circle cx="50" cy="50" r="48" />
          <circle cx="50" cy="50" r="36" />
          <circle cx="50" cy="50" r="24" />
          <circle cx="50" cy="50" r="12" />
        </svg>

        <Link to="/" aria-label="DecisionHub home" className="relative w-fit">
          <Logo tone="light" />
        </Link>

        <div className="relative">
          <h2 className="max-w-md font-display text-3xl font-bold leading-[1.15] tracking-tight lg:text-4xl xl:text-[2.75rem] xl:leading-[1.1]">
            {sample.heading}
          </h2>
          <p className="mt-4 max-w-md text-base leading-relaxed text-brand-100/85">
            {sample.text}
          </p>
          <SampleBoard board={sample.board} />
        </div>

        <p className="relative text-xs text-brand-200/70">
          &copy; {new Date().getFullYear()} DecisionHub
        </p>
      </aside>

      {/* Form column: the only thing shown on phones */}
      <div className="flex min-h-screen flex-col px-5 py-8 min-[400px]:px-6 sm:px-10 md:px-8 lg:px-10">
        <header className="md:hidden">
          <Link to="/" aria-label="DecisionHub home" className="inline-block">
            <Logo />
          </Link>
        </header>

        <main className="flex flex-1 items-center justify-center py-10">
          <div className="w-full max-w-[26rem]">{children}</div>
        </main>
      </div>
    </div>
  );
};

export default AuthLayout;