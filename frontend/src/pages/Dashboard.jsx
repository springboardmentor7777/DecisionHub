
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Brand from '../components/Brand'

const dashboardConfig = {
  USER: {
    title: 'My Dashboard',
    subtitle: 'Welcome back! Here is your activity overview.',
    navigation: [
      { label: 'Overview', path: 'overview' },
      { label: 'My Decisions', path: 'decisions' },
      { label: 'My Votes', path: 'votes' },
      { label: 'Notifications', path: 'notifications' },
    ],
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
    navigation: [
      { label: 'Overview', path: 'overview' },
      { label: 'User Management', path: 'users' },
      { label: 'Communities', path: 'communities' },
      { label: 'Reports', path: 'reports' },
      { label: 'Settings', path: 'settings' },
    ],
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
    navigation: [
      { label: 'Overview', path: 'overview' },
      { label: 'My Communities', path: 'communities' },
      { label: 'Decisions', path: 'decisions' },
      { label: 'Discussions', path: 'discussions' },
      { label: 'Moderation', path: 'moderation' },
    ],
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

const sectionContent = {
  decisions: {
    title: 'My Decisions',
    description: 'View and manage your decisions.',
  },
  votes: {
    title: 'My Votes',
    description: 'Review the decisions you have voted on.',
  },
  notifications: {
    title: 'Notifications',
    description: 'Stay updated with your latest notifications.',
  },
  users: {
    title: 'User Management',
    description: 'Manage users across the DecisionHub platform.',
  },
  communities: {
    title: 'Communities',
    description: 'Explore and manage your communities.',
  },
  reports: {
    title: 'Reports',
    description: 'Review platform reports and activities.',
  },
  settings: {
    title: 'Settings',
    description: 'Manage your platform settings.',
  },
  discussions: {
    title: 'Discussions',
    description: 'View and manage community discussions.',
  },
  moderation: {
    title: 'Moderation',
    description: 'Review community content and moderation activities.',
  },
}

function Dashboard() {
  const [previewRole, setPreviewRole] = useState('USER')

  const navigate = useNavigate()
  const { section } = useParams()

  const currentConfig = dashboardConfig[previewRole]
  const activeSection = section || 'overview'

  const selectedNavigation = currentConfig.navigation.find(
    (item) => item.path === activeSection
  )

  const currentSection = sectionContent[activeSection]

  const handleNavigation = (path) => {
    if (path === 'overview') {
      navigate('/dashboard')
    } else {
      navigate(`/dashboard/${path}`)
    }
  }

  return (
    <div className="flex min-h-screen bg-slate-50">

      {/* Sidebar */}
      <aside className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white p-6 lg:flex">

        <Link
          to="/"
          aria-label="DecisionHub home"
          className="shrink-0 transition hover:opacity-90"
        >
          <Brand />
        </Link>

        <div className="mt-10">
          <p className="mb-4 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Workspace
          </p>

          <nav aria-label="Sidebar navigation" className="space-y-2">
            {currentConfig.navigation.map((item) => (
              <button
                key={item.path}
                type="button"
                onClick={() => handleNavigation(item.path)}
                aria-current={activeSection === item.path ? 'page' : undefined}
                className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                  activeSection === item.path
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-slate-600 hover:bg-slate-50'
                }`}
              >
                {item.label}
              </button>
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

          <Link
            to="/"
            className="mt-4 block text-sm font-medium text-indigo-600 hover:text-indigo-700"
          >
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
              DecisionHub / {selectedNavigation?.label || 'Overview'}
            </p>

            <h1 className="mt-1 text-xl font-bold text-slate-900">
              {currentConfig.title}
            </h1>
          </div>

          <div className="flex items-center gap-3">

            <label
              htmlFor="role-preview"
              className="text-sm font-medium text-slate-600"
            >
              Preview role
            </label>

            <select
              id="role-preview"
              value={previewRole}
              onChange={(event) => {
                setPreviewRole(event.target.value)
                navigate('/dashboard')
              }}
              className="max-w-52 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            >
              <option value="USER">User</option>
              <option value="ADMIN">Admin</option>
              <option value="COMMUNITY_MODERATOR">
                Community Moderator
              </option>
            </select>

          </div>
        </header>

        {/* Mobile and Tablet Navigation */}
        <nav
          aria-label="Dashboard navigation"
          className="flex gap-2 overflow-x-auto border-b border-slate-200 bg-white px-4 py-3 lg:hidden"
        >
          {currentConfig.navigation.map((item) => (
            <button
              key={item.path}
              type="button"
              onClick={() => handleNavigation(item.path)}
              aria-current={activeSection === item.path ? 'page' : undefined}
              className={`shrink-0 rounded-lg px-4 py-2 text-sm font-medium transition ${
                activeSection === item.path
                  ? 'bg-indigo-50 text-indigo-700'
                  : 'text-slate-600 hover:bg-slate-50'
              }`}
            >
              {item.label}
            </button>
          ))}
        </nav>

        {/* Dashboard Body */}
        <div className="mx-auto max-w-7xl p-5 sm:p-8">

          {activeSection === 'overview' ? (

            <>
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
                    <div
                      key={index}
                      className="flex items-start gap-4 py-4"
                    >
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
            </>

          ) : currentSection ? (

            <section className="rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
              <p className="text-sm font-medium text-indigo-600">
                DecisionHub Workspace
              </p>

              <h2 className="mt-3 text-2xl font-bold text-slate-900">
                {currentSection.title}
              </h2>

              <p className="mt-2 text-sm leading-6 text-slate-500">
                {currentSection.description}
              </p>

              <div className="mt-8 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-6 text-center">
                <p className="font-medium text-slate-700">
                  This section is under development.
                </p>

                <p className="mt-2 text-sm text-slate-500">
                  We'll implement this feature in the upcoming frontend milestones.
                </p>
              </div>

              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="mt-6 rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-700"
              >
                Back to Overview
              </button>
            </section>

          ) : (

            <section className="rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
              <h2 className="text-2xl font-bold text-slate-900">
                Page not found
              </h2>

              <p className="mt-2 text-sm text-slate-500">
                This Dashboard section does not exist for the selected role.
              </p>

              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="mt-6 rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-700"
              >
                Back to Overview
              </button>
            </section>

          )}

        </div>

      </main>
    </div>
  )
}

export default Dashboard
