function Navbar() {
  const links = [
    { label: 'Home', href: '/' },
    { label: 'Explore', href: '/explore' },
    { label: 'Communities', href: '/communities' },
    { label: 'How it works', href: '/#how-it-works' },
  ]

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-slate-50">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-x-8 gap-y-4 px-5 py-4">
        <a
          href="/"
          className="flex items-center gap-2 font-bold text-slate-900 focus-visible:outline-2 focus-visible:outline-indigo-600"
        >
          <span className="grid size-9 place-items-center rounded-xl bg-indigo-600 text-white">
            D
          </span>
          <span className="text-lg">DecisionHub</span>
        </a>

        <nav aria-label="Main navigation" className="flex flex-wrap gap-x-6 gap-y-2">
          {links.map((link) => (
            <a
              key={link.label}
              href={link.href}
              className="text-sm font-medium text-slate-700 transition hover:text-indigo-700 focus-visible:outline-2 focus-visible:outline-indigo-600"
            >
              {link.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          <a
            href="/login"
            className="rounded-lg px-3 py-2 text-sm font-semibold text-slate-700 hover:text-indigo-700 focus-visible:outline-2 focus-visible:outline-indigo-600"
          >
            Login
          </a>
          <a
            href="/register"
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          >
            Get Started
          </a>
        </div>
      </div>
    </header>
  )
}

export default Navbar