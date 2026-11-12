
function Dashboard() {
  const menuItems = [
    "Overview",
    "My Decisions",
    "Compare Options",
    "Polls & Voting",
    "Discussions",
    "Communities",
    "Analytics",
    "Notifications",
    "Reports",
    "My Profile"
  ];

  return (
    <div className="dh-layout">

      <aside className="dh-sidebar">
        <div className="dh-brand">
          <div className="dh-logo">D</div>
          <div>
            <h2>DecisionHub</h2>
            <span>Think. Compare. Decide.</span>
          </div>
        </div>

        <p className="dh-label">WORKSPACE</p>

        <nav className="dh-menu">
          {menuItems.map((item, index) => (
            <button
              key={item}
              className={`dh-menu-item ${index === 0 ? "active" : ""}`}
              type="button"
            >
              <span className="dh-menu-icon">
                {["⌂", "▤", "⇄", "◉", "☷", "♧", "▥", "♢", "▧", "●"][index]}
              </span>
              {item}
            </button>
          ))}
        </nav>

        <div className="dh-user">
          <div className="dh-avatar">R</div>
          <div>
            <strong>...</strong>
            <span>Member Account</span>
          </div>
        </div>
      </aside>

      <main className="dh-main">

        <header className="dh-topbar">
          <div>
            <span className="dh-breadcrumb">Workspace / Overview</span>
            <h2>Dashboard</h2>
          </div>

          <div className="dh-top-right">
            <input placeholder="Search decisions..." />
            <button type="button" className="dh-bell">♧</button>
            <div className="dh-avatar">R</div>
          </div>
        </header>

        <section className="dh-content">
          <div className="dh-welcome">
            <div>
              <h1>Welcome back ! 👋</h1>
              <p>Here's what's happening with your decisions today.</p>
            </div>

            <button className="dh-create" type="button">
              + Create Decision
            </button>
          </div>

          <div className="dh-stats">
            <div className="dh-card">
              <span>Total Decisions</span>
              <h2>0</h2>
              <small>Across all categories</small>
            </div>

            <div className="dh-card">
              <span>Active Polls</span>
              <h2>0</h2>
              <small>Currently accepting votes</small>
            </div>

            <div className="dh-card">
              <span>Communities</span>
              <h2>0</h2>
              <small>Groups you're connected to</small>
            </div>

            <div className="dh-card">
              <span>Votes Received</span>
              <h2>0</h2>
              <small>Total participation</small>
            </div>
          </div>

          <div className="dh-section">
            <h2>Recent Decisions</h2>
            <p>Your latest decision boards will appear here.</p>
          </div>

        </section>
      </main>
    </div>
  );
}

export default Dashboard;

