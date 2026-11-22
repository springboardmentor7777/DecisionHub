
import { useState } from "react";
import {
  Plus,
  Search,
  SlidersHorizontal,
  LayoutGrid,
  List,
  Lock,
  Globe,
  ArrowUpRight,
  Sparkles,
  FolderOpen,
  MoreHorizontal,
  GitCompareArrows,
  X,
} from "lucide-react";

function DecisionBoards() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [visibility, setVisibility] = useState("PRIVATE");

  const closeModal = () => {
    setIsModalOpen(false);
  };

  return (
    <section className="min-h-full space-y-8 pb-8">

      {/* Header */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-violet-700 via-violet-600 to-indigo-600 p-7 text-white shadow-xl shadow-violet-200/50 sm:p-9">
        <div className="absolute -right-12 -top-20 h-64 w-64 rounded-full bg-white/10 blur-2xl" />
        <div className="absolute bottom-[-70px] right-40 h-48 w-48 rounded-full bg-indigo-300/20 blur-3xl" />

        <div className="relative z-10 flex flex-wrap items-center justify-between gap-6">
          <div>
            <div className="mb-4 flex items-center gap-2 text-sm font-medium text-violet-100">
              <Sparkles size={15} />
              WORKSPACE / DECISIONS
            </div>

            <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Decision Boards
            </h1>

            <p className="mt-3 max-w-lg text-sm leading-6 text-violet-100 sm:text-base">
              Every great decision starts with clarity. Organize your thoughts,
              explore possibilities and choose with confidence.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="inline-flex items-center gap-2 rounded-xl bg-white px-5 py-3 font-semibold text-violet-700 shadow-lg transition hover:-translate-y-1 hover:bg-violet-50"
          >
            <Plus size={19} />
            Create decision
          </button>
        </div>
      </div>

      {/* Statistics */}
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard
          icon={FolderOpen}
          label="Total boards"
          value="0"
          color="violet"
          description="All your decisions"
        />
        <StatCard
          icon={Globe}
          label="Public boards"
          value="0"
          color="blue"
          description="Shared with everyone"
        />
        <StatCard
          icon={Lock}
          label="Private boards"
          value="0"
          color="amber"
          description="Only visible to you"
        />
      </div>

      {/* Search and filters */}
      <div className="rounded-2xl border border-slate-200/80 bg-white p-4 shadow-sm">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative min-w-[220px] flex-1">
            <Search
              size={19}
              className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              type="search"
              placeholder="Search your decision boards..."
              className="w-full rounded-xl border border-slate-200 bg-slate-50/70 py-3 pl-11 pr-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-violet-400 focus:bg-white focus:ring-4 focus:ring-violet-100"
            />
          </div>

          <button
            type="button"
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-600 transition hover:border-violet-300 hover:bg-violet-50 hover:text-violet-700"
          >
            <SlidersHorizontal size={17} />
            Filters
          </button>

          <div className="flex gap-1 rounded-xl bg-slate-100 p-1">
            <button
              type="button"
              aria-label="Grid view"
              className="rounded-lg bg-white p-2 text-violet-700 shadow-sm"
            >
              <LayoutGrid size={18} />
            </button>
            <button
              type="button"
              aria-label="List view"
              className="rounded-lg p-2 text-slate-500 transition hover:bg-white hover:text-violet-700"
            >
              <List size={18} />
            </button>
          </div>
        </div>
      </div>

      {/* Empty state */}
      <div className="relative overflow-hidden rounded-3xl border border-slate-200 bg-white px-6 py-16 text-center shadow-sm sm:py-20">
        <div className="absolute left-0 top-0 h-40 w-40 rounded-full bg-violet-100/50 blur-3xl" />
        <div className="absolute bottom-0 right-0 h-40 w-40 rounded-full bg-indigo-100/50 blur-3xl" />

        <div className="relative z-10 mx-auto flex max-w-lg flex-col items-center">
          <div className="relative mb-6">
            <div className="flex h-24 w-24 items-center justify-center rounded-[28px] bg-gradient-to-br from-violet-100 to-indigo-100 text-violet-600 shadow-inner">
              <GitCompareArrows size={42} strokeWidth={1.6} />
            </div>
            <div className="absolute -right-2 -top-2 flex h-9 w-9 items-center justify-center rounded-xl bg-white text-amber-500 shadow-md">
              <Sparkles size={18} />
            </div>
          </div>

          <span className="rounded-full bg-violet-50 px-4 py-1.5 text-xs font-semibold uppercase tracking-wider text-violet-700">
            Your workspace is ready
          </span>

          <h2 className="mt-5 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
            Start with your first decision
          </h2>

          <p className="mt-3 text-sm leading-7 text-slate-500">
            Big or small, every choice deserves a little clarity. Create a
            decision board, add your options and compare them side by side.
          </p>

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="mt-7 inline-flex items-center gap-2 rounded-xl bg-violet-600 px-6 py-3.5 font-semibold text-white shadow-lg shadow-violet-200 transition hover:-translate-y-1 hover:bg-violet-700 hover:shadow-xl"
          >
            <Plus size={19} />
            Create your first board
            <ArrowUpRight size={17} />
          </button>

          <p className="mt-5 text-xs text-slate-400">
            <Lock size={12} className="mr-1 inline" />
            Your decisions stay private unless you choose to share them.
          </p>
        </div>
      </div>

      {/* Create Decision Modal */}
      {isModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 p-4 backdrop-blur-sm"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeModal();
          }}
        >
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-decision-title"
            className="w-full max-w-lg overflow-hidden rounded-3xl bg-white shadow-2xl"
          >
            <div className="flex items-center justify-between bg-gradient-to-r from-violet-700 to-indigo-600 px-6 py-5 text-white">
              <div>
                <div className="flex items-center gap-2 text-xs font-medium text-violet-100">
                  <Sparkles size={14} />
                  NEW WORKSPACE
                </div>
                <h2
                  id="create-decision-title"
                  className="mt-2 text-xl font-bold"
                >
                  Create a decision
                </h2>
                <p className="mt-1 text-sm text-violet-100">
                  Give your next big decision a starting point.
                </p>
              </div>

              <button
                type="button"
                onClick={closeModal}
                aria-label="Close modal"
                className="rounded-xl p-2 transition hover:bg-white/15"
              >
                <X size={21} />
              </button>
            </div>

            <div className="space-y-5 p-6">
              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  Decision title <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Which laptop should I buy?"
                  maxLength={150}
                  className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-violet-400 focus:ring-4 focus:ring-violet-100"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  Description
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe what you are trying to decide..."
                  rows={3}
                  maxLength={1000}
                  className="w-full resize-none rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition focus:border-violet-400 focus:ring-4 focus:ring-violet-100"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  Category
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-violet-400 focus:ring-4 focus:ring-violet-100"
                >
                  <option value="">Select a category</option>
                  <option value="Education">Education</option>
                  <option value="Career">Career</option>
                  <option value="Technology">Technology</option>
                  <option value="Finance">Finance</option>
                  <option value="Lifestyle">Lifestyle</option>
                  <option value="Personal">Personal</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div>
                <label className="mb-3 block text-sm font-semibold text-slate-700">
                  Board visibility
                </label>

                <div className="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setVisibility("PRIVATE")}
                    className={`rounded-xl border p-4 text-left transition ${
                      visibility === "PRIVATE"
                        ? "border-violet-400 bg-violet-50 ring-2 ring-violet-100"
                        : "border-slate-200 hover:border-violet-200"
                    }`}
                  >
                    <Lock
                      size={20}
                      className="mb-2 text-violet-600"
                    />
                    <span className="block text-sm font-semibold text-slate-800">
                      Private
                    </span>
                    <span className="mt-1 block text-xs text-slate-500">
                      Only you can view
                    </span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setVisibility("PUBLIC")}
                    className={`rounded-xl border p-4 text-left transition ${
                      visibility === "PUBLIC"
                        ? "border-violet-400 bg-violet-50 ring-2 ring-violet-100"
                        : "border-slate-200 hover:border-violet-200"
                    }`}
                  >
                    <Globe
                      size={20}
                      className="mb-2 text-blue-600"
                    />
                    <span className="block text-sm font-semibold text-slate-800">
                      Public
                    </span>
                    <span className="mt-1 block text-xs text-slate-500">
                      Share with others
                    </span>
                  </button>
                </div>
              </div>

              <div className="flex justify-end gap-3 border-t border-slate-100 pt-5">
                <button
                  type="button"
                  onClick={closeModal}
                  className="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
                >
                  Cancel
                </button>

                <button
                  type="button"
                  disabled={!title.trim()}
                  className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white shadow-md transition hover:bg-violet-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <Plus size={17} />
                  Create board
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

function StatCard({ icon: Icon, label, value, color, description }) {
  const styles = {
    violet: "bg-violet-50 text-violet-600",
    blue: "bg-blue-50 text-blue-600",
    amber: "bg-amber-50 text-amber-600",
  };

  return (
    <div className="group rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition duration-300 hover:-translate-y-1 hover:border-violet-200 hover:shadow-lg hover:shadow-violet-100/50">
      <div className="flex items-center justify-between">
        <div className={`flex h-11 w-11 items-center justify-center rounded-xl ${styles[color]}`}>
          <Icon size={21} />
        </div>
        <MoreHorizontal size={20} className="text-slate-300" />
      </div>

      <p className="mt-5 text-sm font-medium text-slate-500">{label}</p>
      <div className="mt-1 flex items-end justify-between">
        <span className="text-3xl font-bold tracking-tight text-slate-900">
          {value}
        </span>
        <span className="pb-1 text-xs text-slate-400">{description}</span>
      </div>
    </div>
  );
}

export default DecisionBoards;
