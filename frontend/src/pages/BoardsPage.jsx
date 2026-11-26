import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import boardsApi, { CATEGORIES, pollLabel } from '../api/boards';
import useAuth from '../hooks/useAuth';
import BoardForm from '../components/boards/BoardForm';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Input from '../components/common/Input';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

const BoardsPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('public');
  const [boards, setBoards] = useState([]);
  const [category, setCategory] = useState('');
  const [search, setSearch] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    const loadBoards = tab === 'public' ? boardsApi.listPublic() : boardsApi.listMine();
    loadBoards.then((data) => {
      if (active) setBoards(data);
    }).catch((loadError) => {
      if (active) setError(loadError.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [tab]);

  useEffect(() => {
    const openForm = () => {
      setTab('mine');
      setFormOpen(true);
    };
    window.addEventListener('decisionhub:create', openForm);
    return () => window.removeEventListener('decisionhub:create', openForm);
  }, []);

  const filteredBoards = useMemo(() => boards.filter((board) => {
    const query = search.trim().toLowerCase();
    return (!category || board.category === category)
      && (!query || `${board.title} ${board.description || ''}`.toLowerCase().includes(query));
  }), [boards, category, search]);

  const createBoard = async (payload) => {
    const board = await boardsApi.create(payload);
    navigate(`/boards/${board.id}`);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-brand-700">Decision making, together</p>
          <h1 className="mt-1 font-display text-3xl font-extrabold tracking-tight">Decision boards</h1>
        </div>
        <Button icon={Plus} onClick={() => { setTab('mine'); setFormOpen((open) => !open); }}>
          New decision
        </Button>
      </div>

      {formOpen && (
        <Card>
          <div className="mb-5 flex items-center justify-between gap-3">
            <h2 className="font-display text-xl font-bold">Create a decision board</h2>
            <Button variant="ghost" size="sm" onClick={() => setFormOpen(false)}>Close</Button>
          </div>
          <BoardForm onSubmit={createBoard} />
        </Card>
      )}

      <div className="flex flex-col gap-4 border-b border-brand-900/10 sm:flex-row sm:items-center sm:justify-between">
        <div role="tablist" aria-label="Board lists" className="flex gap-2">
          {[['public', 'Public boards'], ['mine', 'My boards']].map(([value, label]) => (
            <button key={value} role="tab" aria-selected={tab === value} onClick={() => setTab(value)}
              className={`border-b-2 px-3 py-3 text-sm font-semibold ${tab === value ? 'border-brand-700 text-brand-900' : 'border-transparent text-ink/55'}`}>
              {label}
            </button>
          ))}
        </div>
        <div className="grid gap-3 pb-3 sm:grid-cols-2 sm:items-center">
          <Input id="board-search" label="Search boards" placeholder="Search title or description"
            value={search} onChange={(event) => setSearch(event.target.value)} />
          <div>
            <label htmlFor="board-filter-category" className="mb-1.5 block text-sm font-semibold text-ink/85">Category</label>
            <select id="board-filter-category" value={category} onChange={(event) => setCategory(event.target.value)}
              className="h-11 w-full rounded-[10px] border border-brand-900/20 bg-white px-3 text-sm">
              <option value="">All categories</option>
              {CATEGORIES.map((item) => <option key={item} value={item}>{item}</option>)}
            </select>
          </div>
        </div>
      </div>

      {error && <Alert type="error">{error}</Alert>}
      {loading ? <div className="flex justify-center py-12"><Spinner size="lg" className="text-brand-700" /></div>
        : filteredBoards.length ? (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {filteredBoards.map((board) => (
              <Link key={board.id} to={`/boards/${board.id}`} className="rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-600">
                <Card hover className="h-full">
                  <div className="flex items-start justify-between gap-3">
                    <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-800">{board.category}</span>
                    {tab === 'mine' && String(board.ownerId) === String(user?.id) &&
                      <span className="text-xs text-ink/55">{board.visibility === 'PRIVATE' ? 'Private' : 'Public'}</span>}
                  </div>
                  <h2 className="mt-4 font-display text-lg font-bold leading-snug">{board.title}</h2>
                  <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-ink/65">{board.description || 'No description provided.'}</p>
                  <div className="mt-5 flex items-center justify-between border-t border-brand-900/10 pt-4 text-xs text-ink/55">
                    <span>{pollLabel(board.pollType)}</span>
                    <span>{board.options?.length || 0} options</span>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        ) : <p className="rounded-xl border border-dashed border-brand-900/20 p-8 text-center text-sm text-ink/60">No boards found. Try another search or create a new decision.</p>}
    </div>
  );
};

export default BoardsPage;
