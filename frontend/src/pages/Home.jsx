
import { Link } from 'react-router-dom'
import Brand from '../components/Brand'

function Home() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">

      {/* Navigation */}
      <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/95 backdrop-blur">
        <nav className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 sm:py-5">

          {/* DecisionHub Brand Logo */}
          <Link
            to="/"
            aria-label="DecisionHub home"
            className="shrink-0"
          >
            <Brand />
          </Link>

          {/* Navigation Links */}
          <div className="hidden items-center gap-8 text-sm font-medium text-slate-600 md:flex">
            <a href="#features" className="transition hover:text-indigo-600">
              Features
            </a>
            <a href="#about" className="transition hover:text-indigo-600">
              About
            </a>
          </div>

          {/* Authentication Links */}
          <div className="flex items-center gap-2 sm:gap-3">

            <Link
              to="/login"
              className="rounded-lg px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 sm:px-4"
            >
              Login
            </Link>

            <Link
              to="/register"
              className="rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 sm:px-5"
            >
              Get Started
            </Link>

          </div>
        </nav>
      </header>

      {/* Hero Section */}
      <main>
        <section className="mx-auto grid max-w-7xl items-center gap-14 px-4 py-16 sm:px-6 sm:py-20 md:grid-cols-2 md:py-28">

          <div>
            <div className="mb-6 inline-flex items-center gap-2 rounded-full bg-indigo-50 px-4 py-2 text-sm font-medium text-indigo-700">
              <span className="h-2 w-2 rounded-full bg-indigo-600"></span>
              Collaborative Decision Intelligence
            </div>

            <h1 className="max-w-xl text-4xl font-bold leading-tight tracking-tight text-slate-900 sm:text-5xl lg:text-6xl">
              Make better decisions,
              <span className="text-indigo-600"> together.</span>
            </h1>

            <p className="mt-6 max-w-lg text-base leading-8 text-slate-600 sm:text-lg">
              Bring people, ideas, and insights together.
              Compare options, collect community feedback,
              and make informed decisions with DecisionHub.
            </p>

            {/* Hero Buttons */}
            <div className="mt-9 flex flex-wrap gap-4">

              <Link
                to="/register"
                className="rounded-xl bg-indigo-600 px-6 py-3.5 font-semibold text-white shadow-lg shadow-indigo-200 transition hover:bg-indigo-700 sm:px-7"
              >
                Create an Account
              </Link>

              <a
                href="#features"
                className="rounded-xl border border-slate-300 bg-white px-6 py-3.5 font-semibold text-slate-700 transition hover:bg-slate-100 sm:px-7"
              >
                Explore Platform
              </a>

            </div>

            <div className="mt-10 flex flex-wrap gap-4 text-sm text-slate-500 sm:gap-6">
              <span>✓ Collaborative voting</span>
              <span>✓ Community insights</span>
              <span>✓ Better decisions</span>
            </div>
          </div>

          {/* Dashboard Preview */}
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xl shadow-slate-200/70 sm:p-7">

            <div className="flex items-center justify-between border-b border-slate-100 pb-5">
              <div>
                <p className="text-sm text-slate-500">
                  Decision Overview
                </p>
                <h2 className="mt-1 text-xl font-bold">
                  Community Decisions
                </h2>
              </div>

              <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-700">
                Active
              </span>
            </div>

            <div className="mt-6 grid grid-cols-2 gap-4">

              <div className="rounded-xl bg-indigo-50 p-4">
                <p className="text-sm text-slate-600">
                  Active Decisions
                </p>
                <p className="mt-2 text-3xl font-bold text-indigo-700">
                  12
                </p>
              </div>

              <div className="rounded-xl bg-purple-50 p-4">
                <p className="text-sm text-slate-600">
                  Total Votes
                </p>
                <p className="mt-2 text-3xl font-bold text-purple-700">
                  248
                </p>
              </div>

            </div>

            <div className="mt-7">
              <h3 className="mb-4 font-semibold">
                Recent Decisions
              </h3>

              <div className="space-y-4">

                <div className="rounded-xl border border-slate-100 p-4">
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-semibold">
                      Choose project technology
                    </p>
                    <span className="text-xs text-green-600">
                      Open
                    </span>
                  </div>
                  <p className="mt-2 text-sm text-slate-500">
                    32 community votes
                  </p>
                  <div className="mt-3 h-2 overflow-hidden rounded-full bg-slate-100">
                    <div className="h-full w-3/4 rounded-full bg-indigo-500"></div>
                  </div>
                </div>

                <div className="rounded-xl border border-slate-100 p-4">
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-semibold">
                      Select event location
                    </p>
                    <span className="text-xs text-green-600">
                      Open
                    </span>
                  </div>
                  <p className="mt-2 text-sm text-slate-500">
                    18 community votes
                  </p>
                  <div className="mt-3 h-2 overflow-hidden rounded-full bg-slate-100">
                    <div className="h-full w-1/2 rounded-full bg-purple-500"></div>
                  </div>
                </div>

              </div>
            </div>

            <div className="mt-6 rounded-xl bg-slate-900 p-4 text-white">
              <p className="text-sm text-slate-300">
                DecisionHub Insight
              </p>
              <p className="mt-1 font-medium">
                Better collaboration leads to informed decisions.
              </p>
            </div>

          </div>
        </section>

        {/* Features */}
        <section id="features" className="border-t border-slate-200 bg-white py-20">
          <div className="mx-auto max-w-7xl px-4 sm:px-6">

            <div className="mx-auto max-w-2xl text-center">
              <p className="font-semibold text-indigo-600">
                PLATFORM FEATURES
              </p>

              <h2 className="mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
                Everything you need to decide together
              </h2>

              <p className="mt-4 text-slate-600">
                A collaborative platform for organizing ideas,
                evaluating options, and engaging communities.
              </p>
            </div>

            <div className="mt-12 grid gap-6 md:grid-cols-3">

              {[
                {
                  icon: '◈',
                  title: 'Decision Management',
                  description: 'Create decision boards and organize multiple options in one place.'
                },
                {
                  icon: '◎',
                  title: 'Community Voting',
                  description: 'Collect votes and feedback to understand community preferences.'
                },
                {
                  icon: '▥',
                  title: 'Insights & Analytics',
                  description: 'Understand voting results and make informed decisions.'
                }
              ].map((feature) => (
                <div
                  key={feature.title}
                  className="rounded-2xl border border-slate-200 bg-white p-7 transition hover:-translate-y-1 hover:shadow-lg"
                >
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-50 text-2xl text-indigo-600">
                    {feature.icon}
                  </div>

                  <h3 className="mt-5 text-lg font-bold">
                    {feature.title}
                  </h3>

                  <p className="mt-3 leading-7 text-slate-600">
                    {feature.description}
                  </p>
                </div>
              ))}

            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer id="about" className="border-t border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-col items-start justify-between gap-5 px-4 py-8 sm:flex-row sm:items-center sm:px-6">

          <Link to="/" aria-label="DecisionHub home">
            <Brand />
          </Link>

          <p className="text-sm text-slate-500">
            Collaborative Decision Intelligence System
          </p>

        </div>
      </footer>

    </div>
  )
}

export default Home
