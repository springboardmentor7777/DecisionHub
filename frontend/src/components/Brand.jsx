
function Brand({ showName = true, className = '' }) {
  return (
    <div className={`inline-flex items-center gap-3 ${className}`}>
      {/* DecisionHub symbol */}
      <svg
        viewBox="0 0 48 48"
        className="h-10 w-10 shrink-0 sm:h-11 sm:w-11"
        role="img"
        aria-label="DecisionHub logo"
        xmlns="http://www.w3.org/2000/svg"
      >
        <rect
          width="48"
          height="48"
          rx="13"
          fill="#4F46E5"
        />

        {/* Decision connections */}
        <path
          d="M24 11V19M24 19L14 29M24 19L34 29"
          stroke="white"
          strokeWidth="2.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Decision nodes */}
        <circle cx="24" cy="11" r="4" fill="white" />
        <circle cx="14" cy="31" r="4" fill="white" />
        <circle cx="34" cy="31" r="4" fill="white" />

        <path
          d="M14 38H34"
          stroke="#C7D2FE"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </svg>

      {/* Brand name */}
      {showName && (
        <span className="text-xl font-bold tracking-tight text-slate-900 sm:text-2xl">
          Decision<span className="text-indigo-600">Hub</span>
        </span>
      )}
    </div>
  )
}

export default Brand
