import React, { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";
import { boardsApi, CATEGORIES, pollLabel } from "../api/boards";
import { votesApi } from "../api/votes";
import profileApi from "../api/profile";
import Alert from "../components/common/Alert";
import Spinner from "../components/common/Spinner";
import Button from "../components/common/Button";
import Input from "../components/common/Input";

const panel = "rounded-2xl border border-slate-200 bg-white p-5 shadow-sm";
const field = "w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100";
const primaryButton = "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700";
const secondaryButton = "rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50";

function DashboardShell({ role, children, activePage, setActivePage }) {
  const [profileOpen, setProfileOpen] = useState(false);
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const displayName = user?.displayName || "My Account";
  const initials =
    (user?.displayName || "U").trim().split(/\s+/).slice(0, 2).map((word) => word[0]).join("").toUpperCase() || "U";

  const roleName = role === "admin" ? "Administrator" : role === "moderator" ? "Moderator" : "User";
  const links = role === "admin"
    ? ["Overview", "Users", "Roles", "Decisions", "Analytics", "Settings"]
    : role === "moderator"
      ? ["Overview", "Reported Content", "Discussions", "Comments", "Guidelines", "History"]
      : ["Overview", "My Decisions", "Create Decision", "Community", "Settings"];

  async function handleLogout() {
    try {
      if (typeof logout === "function") await logout();
    } finally {
      navigate("/", { replace: true });
    }
  }

  function selectPage(item) {
    setActivePage(item);
    if (role === "user" && ["Create Decision", "My Decisions"].includes(item)) navigate("/boards");
    if (role === "user" && item === "Community") navigate("/communities");
  }

  return (
    <div className="min-h-screen bg-slate-100 text-slate-800">
      <div className="flex min-h-screen">
        <aside className="hidden w-64 shrink-0 flex-col bg-slate-950 text-slate-100 md:flex">
          <div className="border-b border-white/10 px-6 py-6">
            <div className="text-2xl font-extrabold tracking-tight">
              Decision<span className="text-indigo-400">Hub</span>
            </div>
            <p className="mt-1 text-xs text-slate-400">{roleName} workspace</p>
          </div>
          <nav className="flex-1 space-y-1 px-3 py-5" aria-label="Dashboard navigation">
            {links.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => selectPage(item)}
                className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                  activePage === item ? "bg-indigo-600 text-white" : "text-slate-300 hover:bg-white/10 hover:text-white"
                }`}
              >
                <span className="mr-2">{item === "Overview" ? "▦" : item === "Settings" ? "⚙" : "◈"}</span>
                {item}
              </button>
            ))}
          </nav>
          <div className="border-t border-white/10 p-4 text-xs text-slate-400">DecisionHub · Dashboard</div>
        </aside>

        <div className="min-w-0 flex-1">
          <header className="sticky top-0 z-20 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 shadow-sm sm:px-6">
            <div className="flex items-center gap-3">
              <div className="text-lg font-extrabold text-indigo-700 md:hidden">DecisionHub</div>
              <div className="hidden text-sm text-slate-500 md:block">{roleName} / {activePage}</div>
              <select
                className={`${field} max-w-48 md:hidden`}
                value={activePage}
                onChange={(event) => selectPage(event.target.value)}
                aria-label="Dashboard section"
              >
                {links.map((item) => <option key={item}>{item}</option>)}
              </select>
            </div>

            <div className="relative">
              <button
                type="button"
                onClick={() => setProfileOpen((value) => !value)}
                className="flex items-center gap-2 rounded-full border border-slate-200 p-1.5 pr-3 hover:bg-slate-50"
                aria-label="Open profile menu"
                aria-expanded={profileOpen}
              >
                <span className="grid h-9 w-9 place-items-center rounded-full bg-indigo-100 text-sm font-bold text-indigo-700">{initials}</span>
                <span className="hidden text-left sm:block">
                  <span className="block max-w-[10rem] truncate text-sm font-semibold">{displayName}</span>
                  <span className="block text-xs text-slate-500">{roleName}</span>
                </span>
                <span className="text-slate-500">⌄</span>
              </button>
              {profileOpen && (
                <div className="absolute right-0 mt-2 w-60 rounded-xl border border-slate-200 bg-white p-2 shadow-xl">
                  <div className="px-3 pb-2 pt-1">
                    <p className="truncate text-sm font-semibold text-slate-900">{displayName}</p>
                    {user?.email && <p className="truncate text-xs text-slate-500">{user.email}</p>}
                  </div>
                  <div className="mb-1 border-t border-slate-100" />
                  <button
                    className="w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-slate-50"
                    onClick={() => { setActivePage("Profile"); setProfileOpen(false); }}
                  >
                    My Profile
                  </button>
                  <button
                    className="w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-slate-50"
                    onClick={() => { setActivePage("Settings"); setProfileOpen(false); }}
                  >
                    Settings
                  </button>
                  <div className="my-1 border-t border-slate-100" />
                  <button
                    className="w-full rounded-lg px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50"
                    onClick={handleLogout}
                  >
                    Sign out
                  </button>
                </div>
              )}
            </div>
          </header>
          <main className="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">{children}</main>
        </div>
      </div>
    </div>
  );
}

function Heading({ title, subtitle, action }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">{title}</h1>
        <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
      </div>
      {action}
    </div>
  );
}

function StatCards({ items }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {items.map(([label, value]) => (
        <div key={label} className={panel}>
          <p className="text-sm text-slate-500">{label}</p>
          <p className="mt-2 text-2xl font-bold text-slate-900">{value}</p>
        </div>
      ))}
    </div>
  );
}

function Empty({ children }) {
  return <p className="py-10 text-center text-sm text-slate-500">{children}</p>;
}

function ProfileView() {
  const [profile, setProfile] = useState({ email: "", displayName: "", bio: "", interests: "" });
  const [password, setPassword] = useState({ currentPassword: "", newPassword: "" });
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [profileError, setProfileError] = useState("");
  const [profileSuccess, setProfileSuccess] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");

  useEffect(() => {
    let active = true;
    profileApi.get().then((data) => {
      if (active) {
        setProfile({
          email: data.email || "",
          displayName: data.displayName || "",
          bio: data.bio || "",
          interests: data.interests || "",
        });
      }
    }).catch((error) => {
      if (active) setLoadError(error.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, []);

  const saveProfile = async (event) => {
    event.preventDefault();
    setProfileError("");
    setProfileSuccess("");
    setSavingProfile(true);
    try {
      const updated = await profileApi.update({
        displayName: profile.displayName.trim(),
        bio: profile.bio,
        interests: profile.interests,
      });
      setProfile({
        email: updated.email || profile.email,
        displayName: updated.displayName || "",
        bio: updated.bio || "",
        interests: updated.interests || "",
      });
      setProfileSuccess("Profile updated.");
    } catch (error) {
      setProfileError(error.message);
    } finally {
      setSavingProfile(false);
    }
  };

  const changePassword = async (event) => {
    event.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");
    setChangingPassword(true);
    try {
      await profileApi.changePassword(password);
      setPassword({ currentPassword: "", newPassword: "" });
      setPasswordSuccess("Password changed.");
    } catch (error) {
      setPasswordError(error.message);
    } finally {
      setChangingPassword(false);
    }
  };

  if (loading) {
    return <section className={panel}><div className="flex justify-center py-10"><Spinner size="lg" className="text-indigo-600" /></div></section>;
  }

  if (loadError) {
    return <section className={panel}><Heading title="My Profile" subtitle="Manage your account profile." /><Alert type="error" className="mt-4">{loadError}</Alert></section>;
  }

  return (
    <div className="space-y-6">
      <section className={panel}>
        <Heading title="My Profile" subtitle="Manage your account profile." />
        <form className="mt-6 space-y-4" onSubmit={saveProfile}>
          {profileError && <Alert type="error">{profileError}</Alert>}
          {profileSuccess && <Alert type="success">{profileSuccess}</Alert>}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input id="profile-email" label="Email" type="email" value={profile.email} disabled />
            <Input id="profile-display-name" label="Display name" value={profile.displayName}
              onChange={(event) => setProfile({ ...profile, displayName: event.target.value })}
              maxLength={80} required />
          </div>
          <div>
            <label htmlFor="profile-bio" className="mb-1.5 block text-sm font-semibold text-slate-700">Bio</label>
            <textarea id="profile-bio" value={profile.bio} maxLength={1000}
              onChange={(event) => setProfile({ ...profile, bio: event.target.value })}
              rows={4} className={`${field} resize-y`} />
          </div>
          <div>
            <label htmlFor="profile-interests" className="mb-1.5 block text-sm font-semibold text-slate-700">Interests</label>
            <textarea id="profile-interests" value={profile.interests} maxLength={500}
              onChange={(event) => setProfile({ ...profile, interests: event.target.value })}
              rows={3} className={`${field} resize-y`} />
          </div>
          <Button type="submit" loading={savingProfile}>Save profile</Button>
        </form>
      </section>

      <section className={panel}>
        <h2 className="text-xl font-bold text-slate-900">Change password</h2>
        <form className="mt-5 max-w-xl space-y-4" onSubmit={changePassword}>
          {passwordError && <Alert type="error">{passwordError}</Alert>}
          {passwordSuccess && <Alert type="success">{passwordSuccess}</Alert>}
          <Input id="current-password" label="Current password" type="password"
            autoComplete="current-password" value={password.currentPassword}
            onChange={(event) => setPassword({ ...password, currentPassword: event.target.value })} required />
          <Input id="new-password" label="New password" type="password"
            autoComplete="new-password" value={password.newPassword}
            onChange={(event) => setPassword({ ...password, newPassword: event.target.value })}
            minLength={8} maxLength={72} required />
          <Button type="submit" loading={changingPassword}>Change password</Button>
        </form>
      </section>
    </div>
  );
}

export function UserDashboard() {
  const [boards, setBoards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [activePage, setActivePage] = useState("Overview");
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    const loadBoards = async () => {
      setLoading(true);
      setError("");
      try {
        const myBoards = await boardsApi.listMine();
        const results = await Promise.all(myBoards.map((board) => votesApi.results(board.id)));
        if (active) {
          setBoards(myBoards.map((board, index) => ({
            ...board,
            totalVotes: results[index].totalVotes,
          })));
        }
      } catch (loadError) {
        if (active) setError(loadError.message);
      } finally {
        if (active) setLoading(false);
      }
    };
    loadBoards();
    return () => { active = false; };
  }, []);

  const visibleBoards = useMemo(() => boards.filter((board) =>
    (board.title || "").toLowerCase().includes(search.toLowerCase()) &&
    (category === "All" || board.category === category)
  ), [boards, search, category]);

  const totalVotes = boards.reduce((sum, board) => sum + board.totalVotes, 0);
  const publicBoards = boards.filter((board) => board.visibility === "PUBLIC").length;

  return (
    <DashboardShell role="user" activePage={activePage} setActivePage={setActivePage}>
      {activePage === "Profile" ? (
        <ProfileView />
      ) : activePage === "Settings" ? (
        <section className={panel}><Heading title="Settings" subtitle="Account preferences." /><p className="mt-4 text-sm text-slate-500">Settings can be connected to your backend.</p></section>
      ) : (
        <>
          <Heading
            title="User Dashboard"
            subtitle="Create decisions, compare options and participate in community voting."
            action={<button onClick={() => navigate("/boards")} className={primaryButton}>+ Create decision</button>}
          />
          <StatCards items={[
            ["Decision boards", boards.length],
            ["Active decisions", boards.length],
            ["Total votes", totalVotes],
            ["Public boards", publicBoards]
          ]} />
          <section className={panel}>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
              <div><h2 className="text-xl font-bold">Decision boards</h2><p className="text-sm text-slate-500">Your decisions, votes and comparisons.</p></div>
              <input className={`${field} max-w-xs`} placeholder="Search boards..." value={search} onChange={(event) => setSearch(event.target.value)} />
            </div>
            <div className="mb-5 flex flex-wrap gap-3">
              <select className={`${field} max-w-48`} value={category} onChange={(event) => setCategory(event.target.value)}>
                <option value="All">All categories</option>
                {CATEGORIES.map((item) => <option key={item}>{item}</option>)}
              </select>
            </div>
            {loading ? (
              <div className="flex justify-center py-10"><Spinner size="lg" className="text-indigo-600" /></div>
            ) : error ? (
              <Alert type="error">{error}</Alert>
            ) : <div className="space-y-4">
              {visibleBoards.map((board) => {
                return (
                  <article key={board.id} className="rounded-xl border border-slate-200 p-4">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="font-bold">{board.title}</h3>
                        <p className="mt-1 text-sm text-slate-500">{board.description || "No description provided."}</p>
                        <p className="mt-2 text-xs text-indigo-600">
                          {board.category} · {board.visibility === "PRIVATE" ? "Private" : "Public"} · {pollLabel(board.pollType)} · {board.totalVotes} {board.totalVotes === 1 ? "vote" : "votes"}
                        </p>
                      </div>
                      <Link className={secondaryButton} to={`/boards/${board.id}`}>Details</Link>
                    </div>
                  </article>
                );
              })}
              {!visibleBoards.length && <Empty>No decision boards found. Create a board to get started.</Empty>}
            </div>}
          </section>
        </>
      )}
    </DashboardShell>
  );
}

function ManagementDashboard({ role }) {
  const isAdmin = role === "admin";
  const [activePage, setActivePage] = useState("Overview");
  const [users, setUsers] = useState([
    { id: 1, name: "Alex Morgan", role: "User", active: true },
    { id: 2, name: "Jamie Lee", role: "Moderator", active: true },
    { id: 3, name: "Taylor Kim", role: "User", active: false }
  ]);
  const [reports, setReports] = useState([
    { id: 101, subject: "Inappropriate comment", status: "Pending" },
    { id: 102, subject: "Spam decision board", status: "Pending" },
    { id: 103, subject: "Harassment report", status: "Pending" }
  ]);

  const content = activePage === "Overview" ? (
    <>
      <StatCards items={isAdmin
        ? [["Total users", users.length], ["Active users", users.filter((user) => user.active).length], ["Pending reports", reports.filter((report) => report.status === "Pending").length], ["Platform status", "Operational"]]
        : [["Open reports", reports.filter((report) => report.status === "Pending").length], ["Resolved reports", reports.filter((report) => report.status === "Resolved").length], ["Users monitored", users.length], ["Community status", "Active"]]} />
      <section className={panel}><h2 className="text-lg font-bold">{isAdmin ? "Administration overview" : "Moderation overview"}</h2><p className="mt-2 text-sm text-slate-500">{isAdmin ? "Use the sidebar to manage users and review platform areas." : "Use the sidebar to review reports and community discussions."}</p></section>
    </>
  ) : isAdmin && activePage === "Users" ? (
    <section className={panel}>
      <h2 className="mb-4 text-lg font-bold">User management</h2>
      <div className="space-y-3">{users.map((user) => (
        <div key={user.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 p-3">
          <div><p className="font-semibold">{user.name}</p><p className="text-xs text-slate-500">{user.role} · {user.active ? "Active" : "Disabled"}</p></div>
          <button className={secondaryButton} onClick={() => setUsers((current) => current.map((item) => item.id === user.id ? { ...item, active: !item.active } : item))}>{user.active ? "Disable user" : "Enable user"}</button>
        </div>
      ))}</div>
    </section>
  ) : !isAdmin && ["Reported Content", "Comments", "Discussions", "History"].includes(activePage) ? (
    <section className={panel}>
      <h2 className="mb-4 text-lg font-bold">{activePage}</h2>
      <div className="space-y-3">{reports.map((report) => (
        <div key={report.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 p-3">
          <div><p className="font-semibold">{report.subject}</p><p className="text-xs text-slate-500">Report #{report.id} · {report.status}</p></div>
          {report.status === "Pending" && <div className="flex gap-2">
            <button className={secondaryButton} onClick={() => setReports((current) => current.map((item) => item.id === report.id ? { ...item, status: "Dismissed" } : item))}>Dismiss</button>
            <button className={primaryButton} onClick={() => setReports((current) => current.map((item) => item.id === report.id ? { ...item, status: "Resolved" } : item))}>Resolve</button>
          </div>}
        </div>
      ))}</div>
    </section>
  ) : (
    <section className={panel}><h2 className="text-lg font-bold">{activePage}</h2><p className="mt-2 text-sm text-slate-500">{activePage === "Settings" ? "Platform settings will be connected to the backend." : activePage === "Analytics" ? "Analytics summaries will appear here when platform data is connected." : activePage === "Roles" ? "Role assignment is controlled by the authenticated backend." : activePage === "Guidelines" ? "Community rules and moderation guidance." : "This section is ready for its authorized backend workflow."}</p></section>
  );

  return (
    <DashboardShell role={role} activePage={activePage} setActivePage={setActivePage}>
      <Heading title={isAdmin ? "Admin Dashboard" : "Moderator Dashboard"} subtitle={isAdmin ? "Platform administration and system oversight." : "Review reports and help maintain a respectful community."} />
      {content}
    </DashboardShell>
  );
}

export function AdminDashboard() {
  return <ManagementDashboard role="admin" />;
}

export function ModeratorDashboard() {
  return <ManagementDashboard role="moderator" />;
}

export default function DashboardPage({ role = "user" }) {
  if (role === "admin") return <AdminDashboard />;
  if (role === "moderator") return <ModeratorDashboard />;
  return <UserDashboard />;
}
