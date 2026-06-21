import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMeeting } from '../api/meetings';
import { getDecisionsByMeeting } from '../api/decisions';
import { getActionItemsByMeeting } from '../api/actionItems';
import { useTitle } from '../hooks/useTitle';

const badgeStyle = {
  PENDING: 'bg-gray-100 text-gray-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-green-100 text-green-700',
  OVERDUE: 'bg-red-100 text-red-700',
  PROPOSED: 'bg-gray-100 text-gray-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function MeetingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const { data: meeting, isLoading: loadingMeeting } = useQuery({
    queryKey: ['meeting', id],
    queryFn: () => getMeeting(id),
  });

  const { data: decisions = [] } = useQuery({
    queryKey: ['decisions', 'meeting', id],
    queryFn: () => getDecisionsByMeeting(id),
    enabled: !!id,
  });

  const { data: actionItems = [] } = useQuery({
    queryKey: ['actionItems', 'meeting', id],
    queryFn: () => getActionItemsByMeeting(id),
    enabled: !!id,
  });

  useTitle(meeting?.title || 'Meeting');

  if (loadingMeeting) {
    return <div className="space-y-4">{[...Array(3)].map((_, i) => <div key={i} className="h-20 bg-gray-100 rounded-lg animate-pulse" />)}</div>;
  }

  if (!meeting) {
    return <p className="text-gray-500">Meeting not found.</p>;
  }

  return (
    <div className="space-y-6">
      <button onClick={() => navigate('/history')} className="text-sm text-gray-500 hover:text-gray-700">← Back to history</button>

      <div>
        <h1 className="text-2xl font-bold text-gray-900">{meeting.title}</h1>
        <p className="text-sm text-gray-500 mt-1">
          {meeting.date} · {meeting.participants?.join(', ')}
        </p>
      </div>

      {/* Decisions */}
      <section>
        <h2 className="font-semibold text-gray-900 mb-3">Decisions ({decisions.length})</h2>
        {decisions.length === 0 ? (
          <p className="text-sm text-gray-400">No decisions extracted from this meeting.</p>
        ) : (
          <div className="space-y-2">
            {decisions.map((d) => (
              <div key={d.id} className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-center gap-2 mb-1">
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${badgeStyle[d.status] || ''}`}>{d.status}</span>
                  <span className="font-medium text-sm">{d.title}</span>
                </div>
                {d.reason && <p className="text-sm text-gray-500">{d.reason}</p>}
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Action Items */}
      <section>
        <h2 className="font-semibold text-gray-900 mb-3">Action Items ({actionItems.length})</h2>
        {actionItems.length === 0 ? (
          <p className="text-sm text-gray-400">No action items extracted from this meeting.</p>
        ) : (
          <div className="space-y-2">
            {actionItems.map((a) => (
              <div key={a.id} className={`bg-white border border-gray-200 rounded-lg p-4 ${a.status === 'COMPLETED' ? 'opacity-60' : ''}`}>
                <div className="flex items-center gap-2 mb-1">
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${badgeStyle[a.status] || ''}`}>{a.status}</span>
                  <span className="font-medium text-sm">{a.task}</span>
                </div>
                <p className="text-xs text-gray-500">👤 {a.assignedTo} · 📅 {a.dueDate}</p>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
