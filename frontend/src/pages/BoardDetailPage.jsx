import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import boardsApi from '../api/boards';
import votesApi from '../api/votes';
import useAuth from '../hooks/useAuth';
import VotePanel from '../components/voting/VotePanel';
import ComparisonTable from '../components/comparison/ComparisonTable';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Input from '../components/common/Input';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';
import CommentsSection from '../components/boards/CommentsSection';

const BoardDetailPage = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [board, setBoard] = useState(null);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [resultsError, setResultsError] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [adding, setAdding] = useState(false);
  const [optionError, setOptionError] = useState('');
  const [newOption, setNewOption] = useState({ name: '', pros: '', cons: '', score: '50' });
  const isOwner = board && String(board.ownerId) === String(user?.id);

  const loadResults = useCallback(async () => {
    try {
      setResultsError('');
      setResults(await votesApi.results(id));
    } catch (loadError) {
      setResultsError(loadError.message);
    }
  }, [id]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    Promise.allSettled([boardsApi.get(id), votesApi.results(id)]).then(([boardResult, votesResult]) => {
      if (!active) return;
      if (boardResult.status === 'fulfilled') setBoard(boardResult.value);
      else setError(boardResult.reason.message);
      if (votesResult.status === 'fulfilled') setResults(votesResult.value);
      else setResultsError(votesResult.reason.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [id]);

  const deleteBoard = async () => {
    if (!window.confirm('Delete this board? This cannot be undone.')) return;
    setDeleting(true);
    setError('');
    try {
      await boardsApi.remove(id);
      navigate('/boards', { replace: true });
    } catch (deleteError) {
      setError(deleteError.message);
    } finally {
      setDeleting(false);
    }
  };

  const addOption = async (event) => {
    event.preventDefault();
    setOptionError('');
    if (!newOption.name.trim() || newOption.score === '' || !Number.isInteger(Number(newOption.score))
      || Number(newOption.score) < 0 || Number(newOption.score) > 100) {
      setOptionError('Enter an option name and a whole-number score from 0 to 100.');
      return;
    }
    setAdding(true);
    try {
      const updatedBoard = await boardsApi.addOption(id, {
        ...newOption,
        name: newOption.name.trim(),
        pros: newOption.pros.trim(),
        cons: newOption.cons.trim(),
        score: Number(newOption.score),
      });
      setBoard(updatedBoard);
      setNewOption({ name: '', pros: '', cons: '', score: '50' });
    } catch (addError) {
      setOptionError(addError.message);
    } finally {
      setAdding(false);
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" className="text-brand-700" /></div>;
  if (error || !board) return <div className="space-y-4">
    <Link to="/boards" className="inline-flex items-center gap-2 text-sm font-semibold text-brand-800"><ArrowLeft size={16} /> Boards</Link>
    <Alert type="error">{error || 'Board could not be found.'}</Alert>
  </div>;

  return (
    <div className="space-y-7">
      <Link to="/boards" className="inline-flex items-center gap-2 text-sm font-semibold text-brand-800 hover:text-brand-950">
        <ArrowLeft size={16} /> All boards
      </Link>
      {resultsError && <Alert type="error">{resultsError}</Alert>}
      <Card>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-800">{board.category}</span>
              <span className="text-xs text-ink/55">{board.visibility === 'PRIVATE' ? 'Private board' : 'Public board'}</span>
            </div>
            <h1 className="mt-3 font-display text-3xl font-extrabold tracking-tight">{board.title}</h1>
            <p className="mt-2 text-sm text-ink/60">Created by {board.ownerName || 'a DecisionHub member'}</p>
          </div>
          {isOwner && <div className="flex gap-2">
            <Button variant="outline" onClick={() => navigate(`/boards/${id}/edit`)}>Edit</Button>
            <Button variant="danger" icon={Trash2} loading={deleting} onClick={deleteBoard}>Delete</Button>
          </div>}
        </div>
        {board.description && <p className="mt-5 max-w-3xl whitespace-pre-wrap leading-relaxed text-ink/75">{board.description}</p>}
      </Card>

      <div className="space-y-6">
        <VotePanel board={board} results={results} onVoted={loadResults} />
        <ComparisonTable options={board.options} votesByOption={results?.votesByOption} totalVotes={results?.totalVotes || 0} />
      </div>

      {isOwner && <Card>
        <h2 className="font-display text-xl font-bold">Add an option</h2>
        <form className="mt-4 grid gap-4 sm:grid-cols-2" onSubmit={addOption}>
          <Input id="new-option-name" label="Option name" value={newOption.name}
            onChange={(event) => setNewOption({ ...newOption, name: event.target.value })} maxLength={160} required />
          <Input id="new-option-score" label="Score (0-100)" type="number" min="0" max="100" step="1" value={newOption.score}
            onChange={(event) => setNewOption({ ...newOption, score: event.target.value })} required />
          <div>
            <label htmlFor="new-option-pros" className="mb-1.5 block text-sm font-semibold text-ink/85">Pros</label>
            <textarea id="new-option-pros" value={newOption.pros} onChange={(event) => setNewOption({ ...newOption, pros: event.target.value })}
              rows={2} maxLength={3000} className="w-full rounded-[10px] border border-brand-900/20 bg-white px-3 py-2 text-sm" />
          </div>
          <div>
            <label htmlFor="new-option-cons" className="mb-1.5 block text-sm font-semibold text-ink/85">Cons</label>
            <textarea id="new-option-cons" value={newOption.cons} onChange={(event) => setNewOption({ ...newOption, cons: event.target.value })}
              rows={2} maxLength={3000} className="w-full rounded-[10px] border border-brand-900/20 bg-white px-3 py-2 text-sm" />
          </div>
          {optionError && <div className="sm:col-span-2"><Alert type="error">{optionError}</Alert></div>}
          <div className="sm:col-span-2"><Button type="submit" loading={adding} icon={Plus}>Add option</Button></div>
        </form>
      </Card>}
      <CommentsSection boardId={id} />
    </div>
  );
};

export default BoardDetailPage;
