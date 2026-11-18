import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";

const CATEGORIES = ["Career", "Education", "Technology", "Travel", "Finance", "Lifestyle"];
const STORE_KEY = "decisionhub-boards";

const readBoards = () => {
  try {
    return JSON.parse(localStorage.getItem(STORE_KEY)) || [];
  } catch {
    return [];
  }
};

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
    if (item === "Create Decision") {
      window.dispatchEvent(new Event("decisionhub:create"));
    }
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

function Modal({ title, onClose, children }) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 p-4"
      onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}
    >
      <section className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-xl font-bold">{title}</h2>
          <button type="button" onClick={onClose} className="text-2xl text-slate-400 hover:text-slate-700" aria-label="Close">×</button>
        </div>
        {children}
      </section>
    </div>
  );
}

export function UserDashboard() {
  const [boards, setBoards] = useState(readBoards);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [status, setStatus] = useState("All");
  const [showForm, setShowForm] = useState(false);
  const [selected, setSelected] = useState(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [newCategory, setNewCategory] = useState("Career");
  const [options, setOptions] = useState(["", ""]);
  const [pro, setPro] = useState("");
  const [con, setCon] = useState("");
  const [comment, setComment] = useState("");
  const [activePage, setActivePage] = useState("Overview");

  useEffect(() => { localStorage.setItem(STORE_KEY, JSON.stringify(boards)); }, [boards]);

  useEffect(() => {
    const openForm = () => setShowForm(true);
    window.addEventListener("decisionhub:create", openForm);
    return () => window.removeEventListener("decisionhub:create", openForm);
  }, []);

  const visibleBoards = useMemo(() => boards.filter((board) =>
    board.title.toLowerCase().includes(search.toLowerCase()) &&
    (category === "All" || board.category === category) &&
    (status === "All" || board.status === status)
  ), [boards, search, category, status]);

  const totalVotes = boards.reduce((sum, board) => sum + board.options.reduce((n, option) => n + option.votes, 0), 0);
  const totalComments = boards.reduce((sum, board) => sum + board.comments.length, 0);
  const selectedBoard = boards.find((board) => board.id === selected);

  function createBoard(event) {
    event.preventDefault();
    const names = options.map((value) => value.trim()).filter(Boolean);
    if (!title.trim() || names.length < 2 || names.length > 5) {
      window.alert("Enter a title and between 2 and 5 options.");
      return;
    }
    setBoards((current) => [{
      id: Date.now(), title: title.trim(), description: description.trim(),
      category: newCategory, status: "Active",
      options: names.map((name) => ({ name, votes: 0 })),
      pros: pro.trim() ? [pro.trim()] : [],
      cons: con.trim() ? [con.trim()] : [],
      comments: []
    }, ...current]);
    setTitle(""); setDescription(""); setOptions(["", ""]); setPro(""); setCon("");
    setShowForm(false); setActivePage("Overview");
  }

  function addComment(event) {
    event.preventDefault();
    if (!comment.trim() || !selectedBoard) return;
    setBoards((current) => current.map((board) => board.id === selectedBoard.id
      ? { ...board, comments: [...board.comments, { id: Date.now(), text: comment.trim() }] }
      : board
    ));
    setComment("");
  }

  const toggleStatus = (id) => setBoards((current) => current.map((board) =>
    board.id === id ? { ...board, status: board.status === "Active" ? "Completed" : "Active" } : board
  ));

  return (
    <DashboardShell role="user" activePage={activePage} setActivePage={setActivePage}>
      {activePage === "Profile" ? (
        <section className={panel}><Heading title="My Profile" subtitle="Your account profile." /><p className="mt-4 text-sm text-slate-500">Your profile details are managed by your connected account.</p></section>
      ) : activePage === "Settings" ? (
        <section className={panel}><Heading title="Settings" subtitle="Account preferences." /><p className="mt-4 text-sm text-slate-500">Settings can be connected to your backend.</p></section>
      ) : activePage === "Community" ? (
        <section className={panel}><Heading title="Community" subtitle="Browse and participate in community decisions." /></section>
      ) : (
        <>
          <Heading
            title="User Dashboard"
            subtitle="Create decisions, compare options and participate in community voting."
            action={<button onClick={() => setShowForm(true)} className={primaryButton}>+ Create decision</button>}
          />
          <StatCards items={[
            ["Decision boards", boards.length],
            ["Active decisions", boards.filter((board) => board.status === "Active").length],
            ["Total votes", totalVotes],
            ["Comments", totalComments]
          ]} />
          <section className={panel}>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
              <div><h2 className="text-xl font-bold">Decision boards</h2><p className="text-sm text-slate-500">Search, filter and participate in decisions.</p></div>
              <input className={`${field} max-w-xs`} placeholder="Search boards..." value={search} onChange={(event) => setSearch(event.target.value)} />
            </div>
            <div className="mb-5 flex flex-wrap gap-3">
              <select className={`${field} max-w-48`} value={category} onChange={(event) => setCategory(event.target.value)}>
                <option value="All">All categories</option>
                {CATEGORIES.map((item) => <option key={item}>{item}</option>)}
              </select>
              <select className={`${field} max-w-48`} value={status} onChange={(event) => setStatus(event.target.value)}>
                <option value="All">All statuses</option><option>Active</option><option>Completed</option>
              </select>
            </div>
            <div className="space-y-4">
              {visibleBoards.map((board) => {
                const votes = board.options.reduce((sum, option) => sum + option.votes, 0);
                return (
                  <article key={board.id} className="rounded-xl border border-slate-200 p-4">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="font-bold">{board.title}</h3>
                        <p className="mt-1 text-sm text-slate-500">{board.description}</p>
                        <p className="mt-2 text-xs text-indigo-600">{board.category} · {board.status} · {votes} votes</p>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <button className={secondaryButton} onClick={() => setSelected(board.id)}>Details</button>
                        <button className={secondaryButton} onClick={() => toggleStatus(board.id)}>{board.status === "Active" ? "Close" : "Reopen"}</button>
                        <button className="rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50" onClick={() => window.confirm("Delete this board?") && setBoards((current) => current.filter((item) => item.id !== board.id))}>Delete</button>
                      </div>
                    </div>
                    <div className="mt-4 space-y-3">
                      {board.options.map((option) => {
                        const percent = votes ? Math.round(option.votes / votes * 100) : 0;
                        return (
                          <div key={option.name}>
                            <div className="flex justify-between text-sm"><span>{option.name}</span><span>{option.votes} votes ({percent}%)</span></div>
                            <div className="mt-1 h-2 rounded-full bg-slate-100"><div className="h-2 rounded-full bg-indigo-500" style={{ width: `${percent}%` }} /></div>
                            {board.status === "Active" && <button className="mt-2 rounded-md bg-indigo-50 px-3 py-1.5 text-xs font-semibold text-indigo-700 hover:bg-indigo-100" onClick={() => setBoards((current) => current.map((item) => item.id !== board.id ? item : { ...item, options: item.options.map((choice) => choice.name === option.name ? { ...choice, votes: choice.votes + 1 } : choice) }))}>Vote for {option.name}</button>}
                          </div>
                        );
                      })}
                    </div>
                  </article>
                );
              })}
              {!visibleBoards.length && <Empty>No decision boards found. Create a board to get started.</Empty>}
            </div>
          </section>
        </>
      )}

      {showForm && (
        <Modal title="Create decision board" onClose={() => setShowForm(false)}>
          <form onSubmit={createBoard} className="space-y-4">
            <label className="block text-sm font-medium">Decision title<input required className={`${field} mt-1`} value={title} onChange={(event) => setTitle(event.target.value)} /></label>
            <label className="block text-sm font-medium">Description<textarea className={`${field} mt-1`} rows="2" value={description} onChange={(event) => setDescription(event.target.value)} /></label>
            <label className="block text-sm font-medium">Category<select className={`${field} mt-1`} value={newCategory} onChange={(event) => setNewCategory(event.target.value)}>{CATEGORIES.map((item) => <option key={item}>{item}</option>)}</select></label>
            <div>
              <p className="mb-2 text-sm font-medium">Voting options (2–5)</p>
              {options.map((value, index) => (
                <div key={index} className="mb-2 flex gap-2">
                  <input className={field} placeholder={`Option ${index + 1}`} value={value} onChange={(event) => setOptions((current) => current.map((item, i) => i === index ? event.target.value : item))} />
                  {options.length > 2 && <button type="button" className={secondaryButton} onClick={() => setOptions((current) => current.filter((_, i) => i !== index))}>Remove</button>}
                </div>
              ))}
              {options.length < 5 && <button type="button" className="text-sm font-semibold text-indigo-600" onClick={() => setOptions((current) => [...current, ""])}>+ Add option</button>}
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="text-sm">Key pro<input className={`${field} mt-1`} value={pro} onChange={(event) => setPro(event.target.value)} /></label>
              <label className="text-sm">Key con<input className={`${field} mt-1`} value={con} onChange={(event) => setCon(event.target.value)} /></label>
            </div>
            <div className="flex justify-end gap-2"><button type="button" className={secondaryButton} onClick={() => setShowForm(false)}>Cancel</button><button className={primaryButton}>Publish board</button></div>
          </form>
        </Modal>
      )}

      {selectedBoard && (
        <Modal title={selectedBoard.title} onClose={() => setSelected(null)}>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded-xl bg-emerald-50 p-4"><h3 className="font-semibold text-emerald-800">Pros</h3>{(selectedBoard.pros || []).map((item, index) => <p key={index} className="mt-2 text-sm">{item}</p>)}{!selectedBoard.pros?.length && <p className="mt-2 text-sm text-slate-500">No pros added.</p>}</div>
            <div className="rounded-xl bg-rose-50 p-4"><h3 className="font-semibold text-rose-800">Cons</h3>{(selectedBoard.cons || []).map((item, index) => <p key={index} className="mt-2 text-sm">{item}</p>)}{!selectedBoard.cons?.length && <p className="mt-2 text-sm text-slate-500">No cons added.</p>}</div>
          </div>
          <h3 className="mt-6 font-bold">Community comments</h3>
          <div className="my-3 space-y-2">{selectedBoard.comments.map((item) => <p key={item.id} className="rounded-lg bg-slate-50 p-3 text-sm">{item.text}</p>)}{!selectedBoard.comments.length && <Empty>No comments yet.</Empty>}</div>
          <form onSubmit={addComment} className="flex gap-2"><input className={field} placeholder="Write a comment..." value={comment} onChange={(event) => setComment(event.target.value)} /><button className={primaryButton}>Post</button></form>
        </Modal>
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
