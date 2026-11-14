
import { useState } from 'react'
import { Link } from 'react-router-dom'

const dashboardConfig = {
  USER: {
    title: 'My Dashboard',
    subtitle: 'Welcome back! Here is your activity overview.',
    navigation: ['Overview', 'My Decisions', 'My Votes', 'Notifications'],
    stats: [
      { label: 'Available Decisions', value: '12', color: 'bg-indigo-50 text-indigo-700' },
      { label: 'My Votes', value: '24', color: 'bg-purple-50 text-purple-700' },
      { label: 'Communities Joined', value: '5', color: 'bg-blue-50 text-blue-700' },
    ],
    activities: [
      'You voted on Choose project technology',
      'New discussion in Technology Community',
      'A new decision is available for voting',
    ],
  },

  ADMIN: {
    title: 'Admin Dashboard',
    subtitle: 'Monitor and manage the DecisionHub platform.',
    navigation: ['Overview', 'User Management', 'Communities', 'Reports', 'Settings'],
    stats: [
      { label: 'Total Users', value: '1,250', color: 'bg-indigo-50 text-indigo-700' },
      { label: 'Active Communities', value: '48', color: 'bg-purple-50 text-purple-700' },
      { label: 'Total Decisions', value: '320', color: 'bg-blue-50 text-blue-700' },
    ],
    activities: [
      'New user registered on the platform',
      'Community moderator activity updated',
      'Monthly platform report is available',
    ],
  },

  COMMUNITY_MODERATOR: {
    title: 'Moderator Dashboard',
    subtitle: 'Manage your communities and discussions.',
    navigation: ['Overview', 'My Communities', 'Decisions', 'Discussions', 'Moderation'],
    stats: [
      { label: 'Managed Communities', value: '8', color: 'bg-indigo-50 text-indigo-700' },
      { label: 'Active Decisions', value: '16', color: 'bg-purple-50 text-purple-700' },
      { label: 'Pending Discussions', value: '7', color: 'bg-blue-50 text-blue-700' },
    ],
    activities: [
      'A new discussion needs moderation',
      'A community decision received new votes',
      'New member joined your community',
    ],
  },
}

function Dashboard() {
  // Temporary role selector for frontend development.
  const [previewRole, setPreviewRole] = useState('USER')

  const currentConfig = dashboardConfig[previewRole]

  return (
    <div className="flex min-h-screen bg-slate-50">

      {/* Sidebar */}
      <aside className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white p-6 lg:flex">

        <Link to="/" className="flex items-center gap-2">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600 text-xl font-bold text-white">
            D
          </div>
          <span className="text-xl font-bold text-slate-900">
            DecisionHub
          </span>
        </Link>

        <div className="mt-10">
          <p className="mb-4 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Workspace
          </p>

          <nav className="space-y-2">
            {currentConfig.navigation.map((item, index) => (
              <div
                key={item}
                className={`rounded-lg px-4 py-3 text-sm font-medium ${
                  index === 0
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                {item}
              </div>
            ))}
          </nav>
        </div>

        <div className="mt-auto border-t border-slate-100 pt-5">
          <p className="text-sm font-semibold text-slate-800">
            Preview Account
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Frontend development mode
          </p>
          <Link to="/" className="mt-4 block text-sm font-medium text-indigo-600 hover:text-indigo-700">
            Back to home
          </Link>
        </div>

      </aside>

      {/* Main Content */}
      <main className="min-w-0 flex-1">

        {/* Header */}
        <header className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-200 bg-white px-5 py-5 sm:px-8">

          <div>
            <p className="text-sm text-slate-500">
              DecisionHub / Overview
            </p>
            <h1 className="mt-1 text-xl font-bold text-slate-900">
              {currentConfig.title}
            </h1>
          </div>

          <div className="flex items-center gap-3">

            <label htmlFor="role-preview" className="text-sm font-medium text-slate-600">
              Preview role
            </label>

            <select
              id="role-preview"
              value={previewRole}
              onChange={(event) => setPreviewRole(event.target.value)}
              className="max-w-52 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            >
              <option value="USER">User</option>
              <option value="ADMIN">Admin</option>
              <option value="COMMUNITY_MODERATOR">Community Moderator</option>
            </select>

          </div>
        </header>

{/* Mobile and Tablet Navigation */}
<nav
  aria-label="Dashboard navigation"
  className="flex gap-2 overflow-x-auto border-b border-slate-200 bg-white px-4 py-3 lg:hidden"
>
  {currentConfig.navigation.map((item, index) => (
    <button
      key={item}
      type="button"
      className={`shrink-0 rounded-lg px-4 py-2 text-sm font-medium ${
        index === 0
          ? 'bg-indigo-50 text-indigo-700'
          : 'text-slate-600 hover:bg-slate-50'
      }`}
    >
      {item}
    </button>
  ))}
</nav>

        {/* Dashboard Body */}
        <div className="mx-auto max-w-7xl p-5 sm:p-8">

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-slate-900">
              Overview
            </h2>
            <p className="mt-2 text-sm text-slate-500">
              {currentConfig.subtitle}
            </p>
          </div>

          {/* Statistics */}
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">

            {currentConfig.stats.map((stat) => (
              <div
                key={stat.label}
                className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
              >
                <p className="text-sm font-medium text-slate-500">
                  {stat.label}
                </p>

                <div className="mt-4 flex items-center justify-between">
                  <p className="text-3xl font-bold text-slate-900">
                    {stat.value}
                  </p>

                  <div className={`flex h-11 w-11 items-center justify-center rounded-xl text-lg font-bold ${stat.color}`}>
                    #
                  </div>
                </div>
              </div>
            ))}

          </div>

          {/* Recent Activity */}
          <section className="mt-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">

            <h3 className="text-lg font-bold text-slate-900">
              Recent Activity
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Recent updates based on your selected role.
            </p>

            <div className="mt-6 divide-y divide-slate-100">

              {currentConfig.activities.map((activity, index) => (
                <div key={index} className="flex items-start gap-4 py-4">

                  <div className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-indigo-500"></div>

                  <p className="text-sm leading-6 text-slate-700">
                    {activity}
                  </p>

                </div>
              ))}

            </div>

          </section>

          <p className="mt-6 text-xs text-slate-400">
            Demo dashboard — displayed statistics and activities are illustrative mock data.
          </p>

        </div>

      </main>
    </div>
  )
}

export default Dashboard
