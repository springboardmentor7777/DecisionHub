import React from 'react';
import Card from '../common/Card';

const ComparisonTable = ({ options = [], votesByOption = {}, totalVotes = 0 }) => {
  const rankedOptions = [...options].sort((a, b) => (Number(b.score) || 0) - (Number(a.score) || 0));

  return (
    <Card className="w-full" padding="p-5 sm:p-6">
      <section aria-labelledby="comparison-heading">
        <h2 id="comparison-heading" className="mb-4 font-display text-xl font-bold">Compare options</h2>
      <div className="overflow-x-auto rounded-xl border border-brand-900/10 bg-white">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead className="bg-brand-50 text-xs uppercase tracking-wide text-ink/60">
            <tr>
              <th scope="col" className="px-4 py-3">Rank</th>
              <th scope="col" className="px-4 py-3">Option</th>
              <th scope="col" className="px-4 py-3">Pros</th>
              <th scope="col" className="px-4 py-3">Cons</th>
              <th scope="col" className="min-w-[7rem] whitespace-nowrap px-4 py-3">Score</th>
              <th scope="col" className="px-4 py-3">Votes</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-brand-900/10">
            {rankedOptions.map((option, index) => {
              const votes = Number(votesByOption[option.id]) || 0;
              const percentage = totalVotes > 0 ? Math.round((votes / totalVotes) * 100) : 0;
              return (
                <tr key={option.id} className={index === 0 ? 'bg-marigold-50/60' : ''}>
                  <td className="px-4 py-4 font-bold">#{index + 1}</td>
                  <th scope="row" className="px-4 py-4 font-semibold text-ink">
                    <span>{option.name}</span>
                    {index === 0 && <span className="ml-2 inline-flex rounded-full bg-marigold-300 px-2 py-0.5 text-xs font-bold text-ink">Leading</span>}
                  </th>
                  <td className="max-w-56 whitespace-pre-wrap px-4 py-4 text-ink/70">{option.pros || '—'}</td>
                  <td className="max-w-56 whitespace-pre-wrap px-4 py-4 text-ink/70">{option.cons || '—'}</td>
                  <td className="min-w-[7rem] whitespace-nowrap px-4 py-4 font-semibold">{Number(option.score) || 0}/100</td>
                  <td className="px-4 py-4 tabular-nums">{votes} <span className="text-ink/55">({percentage}%)</span></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <p className="mt-2 text-xs text-ink/55">Vote percentages are based on {totalVotes} total votes.</p>
      </section>
    </Card>
  );
};

export default ComparisonTable;
