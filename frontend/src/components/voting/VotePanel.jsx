import React, { useState } from 'react';
import { Star } from 'lucide-react';
import votesApi from '../../api/votes';
import Button from '../common/Button';
import Checkbox from '../common/Checkbox';
import Alert from '../common/Alert';
import { pollLabel } from '../../api/boards';

const VotePanel = ({ board, results, onVoted }) => {
  const [selected, setSelected] = useState([]);
  const [rating, setRating] = useState(0);
  const [anonymous, setAnonymous] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const isMultiple = board.pollType === 'MULTIPLE_CHOICE';
  const isRating = board.pollType === 'RATING';

  const toggleOption = (optionId) => {
    setSelected((current) => {
      if (isMultiple) return current.includes(optionId) ? current.filter((id) => id !== optionId) : [...current, optionId];
      if (current[0] !== optionId && isRating) setRating(0);
      return [optionId];
    });
  };

  const submitVote = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    if (!selected.length) {
      setError('Choose an option before voting.');
      return;
    }
    if (isRating && (!rating || rating < 1 || rating > 5)) {
      setError('Choose a rating from 1 to 5 stars.');
      return;
    }
    setSubmitting(true);
    try {
      for (const optionId of selected) {
        await votesApi.cast(board.id, { optionId, rating: isRating ? rating : undefined, anonymous });
      }
      setSelected([]);
      setRating(0);
      setSuccess('Your vote has been recorded.');
      await onVoted();
    } catch (voteError) {
      await onVoted();
      setError(voteError.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="rounded-xl border border-brand-900/10 bg-white p-5 sm:p-6">
      <h2 className="font-display text-xl font-bold">Cast your vote</h2>
      <p className="mt-1 text-sm text-ink/60">{pollLabel(board.pollType)}</p>
      <form className="mt-4 space-y-4" onSubmit={submitVote}>
        {error && <Alert type="error">{error}</Alert>}
        {success && <Alert type="success">{success}</Alert>}
        <fieldset className="space-y-3">
          <legend className="sr-only">Choose an option</legend>
          {board.options.map((option) => {
            const checked = selected.includes(option.id);
            const votes = Number(results?.votesByOption?.[option.id]) || 0;
            const totalVotes = Number(results?.totalVotes) || 0;
            const percentage = totalVotes > 0 ? Math.round((votes / totalVotes) * 100) : 0;
            return (
              <div key={option.id} className="rounded-lg border border-brand-900/10 p-3">
                <label className="flex cursor-pointer items-center gap-3 font-semibold">
                  <input
                    type={isMultiple ? 'checkbox' : 'radio'}
                    name="vote-option"
                    checked={checked}
                    onChange={() => toggleOption(option.id)}
                    className="h-4 w-4 accent-brand-700"
                  />
                  <span>{option.name}</span>
                </label>
                {isRating && checked && (
                  <div className="mt-3 flex items-center gap-1" role="group" aria-label={`Rate ${option.name} from 1 to 5 stars`}>
                    {[1, 2, 3, 4, 5].map((value) => (
                      <button key={value} type="button" onClick={() => setRating(value)}
                        aria-label={`${value} ${value === 1 ? 'star' : 'stars'}`}
                        aria-pressed={rating === value}
                        className="rounded p-1 text-marigold-500 focus-visible:ring-2 focus-visible:ring-brand-600">
                        <Star aria-hidden="true" className={`h-6 w-6 ${value <= rating ? 'fill-current' : ''}`} />
                      </button>
                    ))}
                    <span className="ml-2 text-sm text-ink/60">{rating ? `${rating} of 5` : 'Select a rating'}</span>
                  </div>
                )}
                <div className="mt-3">
                  <div className="mb-1 flex justify-between text-xs text-ink/60">
                    <span>{votes} {votes === 1 ? 'vote' : 'votes'}</span><span>{percentage}%</span>
                  </div>
                  <div role="progressbar" aria-label={`${option.name} vote share`}
                    aria-valuemin="0" aria-valuemax="100" aria-valuenow={percentage}
                    className="h-2 overflow-hidden rounded-full bg-brand-900/10">
                    <div className="h-full rounded-full bg-brand-500 transition-all" style={{ width: `${percentage}%` }} />
                  </div>
                </div>
              </div>
            );
          })}
        </fieldset>
        <Checkbox id={`anonymous-${board.id}`} label="Vote anonymously" checked={anonymous}
          onChange={(event) => setAnonymous(event.target.checked)} />
        <Button type="submit" loading={submitting}>Submit vote</Button>
      </form>
    </section>
  );
};

export default VotePanel;
