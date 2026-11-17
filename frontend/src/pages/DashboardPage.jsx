import React, { useEffect, useMemo, useState } from "react";

const CATEGORIES = ["Career", "Education", "Technology", "Travel", "Finance", "Lifestyle"];

const readBoards = () => {
  try {
    return JSON.parse(localStorage.getItem("decisionhub-boards")) || [];
  } catch {
    return [];
  }
};

// ---------------- USER DASHBOARD ----------------
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

  useEffect(() => {
    localStorage.setItem("decisionhub-boards", JSON.stringify(boards));
  }, [boards]);

  const visibleBoards = useMemo(() => boards.filter((board) =>
    board.title.toLowerCase().includes(search.toLowerCase()) &&
    (category === "All" || board.category === category) &&
    (status === "All" || board.status === status)
  ), [boards, search, category, status]);

  const totalVotes = boards.reduce(
    (sum, board) => sum + board.options.reduce((n, option) => n + option.votes, 0), 0
  );
  const activeBoards = boards.filter((board) => board.status === "Active").length;
  const totalComments = boards.reduce((sum, board) => sum + board.comments.length, 0);

  function createBoard(event) {
    event.preventDefault();
    const names = options.map((option) => option.trim()).filter(Boolean);
    if (!title.trim() || names.length < 2 || names.length > 5) {
      window.alert("Enter a title and between 2 and 5 options.");
      return;
    }

    setBoards((current) => [{
      id: Date.now(),
      title: title.trim(),
      description: description.trim(),
      category: newCategory,
      status: "Active",
      options: names.map((name) => ({ name, votes: 0 })),
      pros: pro.trim() ? [pro.trim()] : [],
       cons: con.trim() ? [con.trim()] : [],
      comments: []
    }, ...current]);

    setTitle("");
    setDescription("");
    setOptions(["", ""]);
    setPro("");
    setCon("");
    setShowForm(false);
  }

  function vote(boardId, optionName) {
    setBoards((current) => current.map((board) =>
      board.id !== boardId || board.status !== "Active"
        ? board
        : {
            ...board,
            options: board.options.map((option) =>
              option.name === optionName
                ? { ...option, votes: option.votes + 1 }
                : option
            )
          }
    ));
  }

  function addComment(event) {
    event.preventDefault();
    if (!comment.trim() || !selected) return;

    setBoards((current) => current.map((board) =>
      board.id === selected.id
        ? { ...board, comments: [...board.comments, { id: Date.now(), text: comment.trim() }] }
        : board
    ));
    setComment("");
  }

  const selectedBoard = boards.find((board) => board.id === selected?.id);
  
  return (
    <DashboardShell>
      <DashboardHeading
        title="User Dashboard"
        subtitle="Create decisions, compare options and participate in community voting."
        action={<button onClick={() => setShowForm(true)} className={primaryButton}>+ Create decision</button>}
      />

      <StatsGrid items={[
        ["Decision boards", boards.length],
        ["Active decisions", activeBoards],
        ["Total votes", totalVotes],
        ["Comments", totalComments]
      ]} />

      <section className={panel}>
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold">Decision boards</h2>
            <p className="text-sm text-slate-500">Search, filter and participate in decisions.</p>
          </div>
          <input value={search} onChange={(event) => setSearch(event.target.value)}
            placeholder="Search boards..." className={inputClass} />
        </div>

        <div className="mb-5 flex flex-wrap gap-3">
          <select value={category} onChange={(event) => setCategory(event.target.value)} className={inputClass}>
            <option value="All">All categories</option>
            {CATEGORIES.map((item) => <option key={item}>{item}</option>)}
          </select>
          <select value={status} onChange={(event) => setStatus(event.target.value)} className={inputClass}>
            <option value="All">All statuses</option>
            <option>Active</option>
            <option>Completed</option>
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
                    <button onClick={() => setSelected(board)} className={secondaryButton}>Details</button>
                    <button onClick={() => setBoards((current) => current.map((item) =>
                      item.id === board.id ? { ...item, status: item.status === "Active" ? "Completed" : "Active" } : item
                    ))} className={secondaryButton}>
                      {board.status === "Active" ? "Close" : "Reopen"}
                    </button>
                    <button onClick={() => {
                      if (window.confirm("Delete this board?")) {
                        setBoards((current) => current.filter((item) => item.id !== board.id));
                      }
                    }} className="rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50">Delete</button>
                  </div>
                </div>

                <div className="mt-4 space-y-3">
                  {board.options.map((option) => {
                    const percent = votes ? Math.round(option.votes / votes * 100) : 0;
                    return (
                      <div key={option.name} className="space-y-2">
                        <div className="mb-1 flex justify-between text-sm">
                          <span>{option.name}</span>
                          <span>{option.votes} votes ({percent}%)</span>
                        </div>
                        <div className="h-2 rounded-full bg-slate-100">
                          <div className="h-2 rounded-full bg-indigo-500" style={{ width: `${percent}%` }} />
                        </div>
                        {board.status === "Active" && (
                          <button onClick={() => vote(board.id, option.name)}
                            className="mt-2 rounded-md bg-indigo-50 px-3 py-1.5 text-xs font-semibold text-indigo-700 hover:bg-indigo-100">
                            Vote for {option.name}
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              </article>
            );
          })}
          {!visibleBoards.length && (
            <p className="py-10 text-center text-slate-500">No decision boards found. Create a board to get started.</p>
          )}
        </div>
      </section>

      {showForm && (
        <Modal onClose={() => setShowForm(false)}>
          <form onSubmit={createBoard} className="space-y-4">
            <h2 className="text-xl font-bold">Create decision board</h2>
            <label className="block text-sm font-medium">Decision title
              <input required value={title} onChange={(event) => setTitle(event.target.value)} className={`${inputClass} mt-1 w-full`} />
              </label>
            <label className="block text-sm font-medium">Description
              <textarea value={description} onChange={(event) => setDescription(event.target.value)} className={`${inputClass} mt-1 w-full`} rows="2" />
            </label>
            <label className="block text-sm font-medium">Category
              <select value={newCategory} onChange={(event) => setNewCategory(event.target.value)} className={`${inputClass} mt-1 w-full`}>
                {CATEGORIES.map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
            <div>
              <p className="mb-2 text-sm font-medium">Voting options (2–5)</p>
              {options.map((option, index) => (
                <div key={index} className="mb-2 flex gap-2">
                  <input value={option} onChange={(event) => setOptions((current) =>
                    current.map((item, i) => i === index ? event.target.value : item)
                  )} placeholder={`Option ${index + 1}`} className={`${inputClass} min-w-0 flex-1`} />
                  {options.length > 2 && (
                    <button type="button" onClick={() => setOptions((current) => current.filter((_, i) => i !== index))}>Remove</button>
                  )}
                </div>
              ))}
              {options.length < 5 && (
                <button type="button" onClick={() => setOptions((current) => [...current, ""])}
                  className="text-sm font-semibold text-indigo-600">+ Add option</button>
              )}
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="text-sm">Key pro
                <input value={pro} onChange={(event) => setPro(event.target.value)} className={`${inputClass} mt-1 w-full`} />
              </label>
              <label className="text-sm">Key con
                <input value={con} onChange={(event) => setCon(event.target.value)} className={`${inputClass} mt-1 w-full`} />
              </label>
            </div>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className={secondaryButton}>Cancel</button>
              <button className={primaryButton}>Publish board</button>
            </div>
          </form>
        </Modal>
      )}
      
      {selectedBoard && (
        <Modal onClose={() => setSelected(null)} side>
          <h2 className="text-xl font-bold">{selectedBoard.title}</h2>
          <div className="mt-5 grid gap-3 sm:grid-cols-2">
            <div className="rounded-xl bg-emerald-50 p-4">
              <h3 className="font-semibold text-emerald-800">Pros</h3>
              {selectedBoard.pros.map((item, index) => <p key={index} className="mt-2 text-sm">{item}</p>)}
              {!selectedBoard.pros.length && <p className="mt-2 text-sm text-slate-500">No pros added.</p>}
            </div>
            <div className="rounded-xl bg-rose-50 p-4">
              <h3 className="font-semibold text-rose-800">Cons</h3>
              {selectedBoard.cons.map((item, index) => <p key={index} className="mt-2 text-sm">{item}</p>)}
              {!selectedBoard.cons.length && <p className="mt-2 text-sm text-slate-500">No cons added.</p>}
            </div>
          </div>
          <h3 className="mt-6 font-bold">Community comments</h3>
          <div className="mt-3 space-y-2">
            {selectedBoard.comments.map((item) => <p key={item.id} className="rounded-lg bg-slate-50 p-3 text-sm">{item.text}</p>)}
            {!selectedBoard.comments.length && <p className="text-sm text-slate-500">No comments yet.</p>}
          </div>
          <form onSubmit={addComment} className="mt-4 flex gap-2">
            <input value={comment} onChange={(event) => setComment(event.target.value)}
              placeholder="Write a comment..." className={`${inputClass} min-w-0 flex-1`} />
            <button className={primaryButton}>Post</button>
          </form>
        </Modal>
      )}
    </DashboardShell>
  );
}
// ---------------- ADMIN DASHBOARD ----------------
export function AdminDashboard() {
  return (
    <DashboardShell>
      <DashboardHeading title="Admin Dashboard"
        subtitle="Platform administration and system oversight." />
      <StatsGrid items={[
        ["User management", "Manage"],
        ["Decision boards", "Review"],
        ["Platform analytics", "Reports"],
        ["System settings", "Configure"]
      ]} />
      <section className={panel}>
        <h2 className="mb-4 text-xl font-bold">Administration</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <FeatureCard title="User Management" description="Manage user accounts and review account status." />
          <FeatureCard title="Role Management" description="Review and manage role assignments." />
          <FeatureCard title="Decision Management" description="Oversee decision boards across the platform." />
          <FeatureCard title="Reports & Analytics" description="Review platform reports and participation analytics." />
          <FeatureCard title="Content Oversight" description="Review platform content and community activity." />
          <FeatureCard title="System Settings" description="Manage platform-level configuration." />
        </div>
      </section>
    </DashboardShell>
  );
}

// ---------------- MODERATOR DASHBOARD ----------------
export function ModeratorDashboard() {
  return (
    <DashboardShell>
      <DashboardHeading title="Moderator Dashboard"
        subtitle="Review reports and help maintain a respectful community." />
      <StatsGrid items={[
        ["Reported content", "Review queue"],
        ["Discussions", "Moderate"],
        ["Comments", "Review"],
        ["Guidelines", "Enforce"]
      ]} />
      <section className={panel}>
        <h2 className="mb-4 text-xl font-bold">Community Moderation</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <FeatureCard title="Reported Content" description="Review content reported by community members." />
          <FeatureCard title="Discussion Moderation" description="Monitor discussions and address guideline violations." />
          <FeatureCard title="Comment Review" description="Review comments for inappropriate or harmful content." />
          <FeatureCard title="User Reports" description="Assess reports and take moderation action when authorized." />
          <FeatureCard title="Community Guidelines" description="Help maintain community standards." />
          <FeatureCard title="Moderation History" description="Review previously handled moderation cases." />
        </div>
      </section>
    </DashboardShell>
  );
  }

// ---------------- SHARED UI COMPONENTS ----------------
const panel = "rounded-2xl bg-white p-5 shadow-sm";
const inputClass = "rounded-lg border border-slate-300 bg-white px-3 py-2 outline-none focus:border-indigo-500";
const primaryButton = "rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700";
const secondaryButton = "rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50";

function DashboardShell({ children }) {
  return <div className="min-h-screen bg-slate-100 p-5 text-slate-800 md:p-8">
    <main className="mx-auto max-w-7xl space-y-6">{children}</main>
  </div>;
}

function DashboardHeading({ title, subtitle, action }) {
  return <header className="flex flex-wrap items-center justify-between gap-4">
    <div>
      <h1 className="text-3xl font-bold text-indigo-700">{title}</h1>
      <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
    </div>
    {action}
  </header>;
}

function StatsGrid({ items }) {
  return <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    {items.map(([label, value]) => <div key={label} className="rounded-xl bg-white p-5 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-2 text-lg font-bold text-slate-800">{value}</p>
    </div>)}
  </section>;
}

function FeatureCard({ title, description }) {
  return <article className="rounded-xl border border-slate-200 p-5">
    <h3 className="font-semibold">{title}</h3>
    <p className="mt-2 text-sm text-slate-500">{description}</p>
  </article>;
}

function Modal({ children, onClose, side = false }) {
  return <div onMouseDown={(event) => {
    if (event.target === event.currentTarget) onClose();
  }} className={`fixed inset-0 z-50 flex bg-black/40 ${side ? "justify-end" : "items-center justify-center p-4"}`}>
    <section className={`relative overflow-y-auto bg-white p-6 shadow-xl ${
      side ? "h-full w-full max-w-lg" : "max-h-[90vh] w-full max-w-xl rounded-2xl"
    }`}>
      <button type="button" onClick={onClose}
        className="absolute right-4 top-3 text-2xl text-slate-500 hover:text-slate-900" aria-label="Close">×</button>
      <div className="pr-6">{children}</div>
    </section>
  </div>;
}

// The active role should be supplied by the existing authenticated application.
// Do not let users choose or promote their own role here.
function DashboardPage({ role = "user" }) {
  if (role === "admin") return <AdminDashboard />;
  if (role === "moderator") return <ModeratorDashboard />;
  return <UserDashboard />;
}

export default DashboardPage;
