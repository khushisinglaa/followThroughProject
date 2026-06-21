import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getAllDecisions, searchDecisions } from '../api/decisions';
import { useDebounce } from '../hooks/useDebounce';

const statusColors = {
  PROPOSED: 'bg-gray-100 text-gray-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  SUPERSEDED: 'bg-yellow-100 text-yellow-700',
};

function SkeletonCard() {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-5 space-y-3 animate-pulse">
      <div className="flex justify-between">
        <div className="h-5 w-20 bg-gray-200 rounded" />
        <div className="h-4 w-16 bg-gray-200 rounded" />
      </div>
      <div className="h-5 w-3/4 bg-gray-200 rounded" />
      <div className="h-4 w-1/2 bg-gray-200 rounded" />
      <div className="h-4 w-full bg-gray-200 rounded" />
    </div>
  );
}

function DecisionCard({ decision }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div
      className="bg-white border border-gray-200 rounded-lg p-5 cursor-pointer hover:border-indigo-300 transition-colors"
      onClick={() => setExpanded(!expanded)}
    >
      <div className="flex justify-between items-start mb-2">
        <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[decision.status] || statusColors.PROPOSED}`}>
          {decision.status}
        </span>
        <span className="text-xs text-gray-400">
          {decision.createdAt?.split('T')[0]}
        </span>
      </div>

      <h3 className="font-semibold text-gray-900">{decision.title}</h3>
      <p className="text-sm text-gray-500 mt-1">Owner: {decision.owner || 'Unassigned'}</p>

      {!expanded && decision.reason && (
        <p className="text-sm text-gray-600 mt-2 truncate">Because: {decision.reason}</p>
      )}

      {!expanded && (
        <p className="text-xs text-indigo-600 mt-3 font-medium">See why →</p>
      )}

      {expanded && (
        <div className="mt-4 space-y-3 border-t pt-3">
          {decision.reason && (
            <div>
              <p className="text-xs font-medium text-gray-400 uppercase">Reason</p>
              <p className="text-sm text-gray-700">{decision.reason}</p>
            </div>
          )}

          {decision.alternatives?.length > 0 && (
            <div>
              <p className="text-xs font-medium text-gray-400 uppercase">Alternatives considered</p>
              <div className="flex flex-wrap gap-1.5 mt-1">
                {decision.alternatives.map((alt, i) => (
                  <span key={i} className="text-xs px-2 py-0.5 bg-gray-100 text-gray-700 rounded-full">{alt}</span>
                ))}
              </div>
            </div>
          )}

          {decision.tradeoffs?.length > 0 && (
            <div>
              <p className="text-xs font-medium text-gray-400 uppercase">Tradeoffs</p>
              <div className="flex flex-wrap gap-1.5 mt-1">
                {decision.tradeoffs.map((t, i) => (
                  <span key={i} className="text-xs px-2 py-0.5 bg-amber-50 text-amber-700 rounded-full">{t}</span>
                ))}
              </div>
            </div>
          )}

          <p className="text-xs text-gray-400 mt-2 cursor-pointer hover:text-gray-600" onClick={(e) => { e.stopPropagation(); setExpanded(false); }}>
            Collapse ↑
          </p>
        </div>
      )}
    </div>
  );
}

export default function Decisions() {
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search);
  const navigate = useNavigate();

  const { data, isLoading, isFetching } = useQuery({
    queryKey: debouncedSearch ? ['decisions', 'search', debouncedSearch] : ['decisions'],
    queryFn: () => debouncedSearch ? searchDecisions(debouncedSearch) : getAllDecisions(),
  });

  // getAllDecisions returns paginated { content: [] }, searchDecisions returns []
  const decisions = Array.isArray(data) ? data : data?.content || [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Decision Memory</h1>
        <p className="text-gray-500 mt-1">Every decision your team has made, with the reasoning behind it.</p>
      </div>

      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search decisions... e.g. 'kafka' or 'redis'"
        className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />

      {isFetching && !isLoading && (
        <p className="text-sm text-gray-400">Searching...</p>
      )}

      {!isLoading && decisions.length > 0 && (
        <p className="text-sm text-gray-500">
          {debouncedSearch ? `${decisions.length} decisions found` : `Showing all ${decisions.length} decisions`}
        </p>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[...Array(4)].map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : decisions.length === 0 ? (
        <div className="text-center py-16">
          <svg className="w-12 h-12 mx-auto text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p className="text-gray-500 font-medium">No decisions recorded yet</p>
          <p className="text-sm text-gray-400 mt-1">Upload your first meeting to get started</p>
          <button onClick={() => navigate('/')} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
            Upload a meeting →
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {decisions.map((d) => <DecisionCard key={d.id} decision={d} />)}
        </div>
      )}
    </div>
  );
}
