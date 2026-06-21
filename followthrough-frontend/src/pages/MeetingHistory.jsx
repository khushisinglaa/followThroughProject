import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getAllMeetings } from '../api/meetings';
import { useTitle } from '../hooks/useTitle';

export default function MeetingHistory() {
  useTitle('History');
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ['meetings'],
    queryFn: () => getAllMeetings(),
  });

  const meetings = Array.isArray(data) ? data : data?.content || [];

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[...Array(3)].map((_, i) => (
          <div key={i} className="h-28 bg-gray-100 rounded-lg animate-pulse" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Meeting History</h1>
        <p className="text-gray-500 mt-1">Every meeting you've uploaded, with its extracted intelligence.</p>
      </div>

      {meetings.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-4xl mb-3">📭</p>
          <p className="text-gray-500 font-medium">No meetings uploaded yet</p>
          <p className="text-sm text-gray-400 mt-1">Upload your first meeting to get started</p>
          <button onClick={() => navigate('/upload')} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
            Upload a meeting →
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {meetings.map((m) => (
            <MeetingCard key={m.id} meeting={m} onClick={() => navigate(`/meetings/${m.id}`)} />
          ))}
        </div>
      )}
    </div>
  );
}

function MeetingCard({ meeting, onClick }) {
  const participantCount = meeting.participants?.length || 0;
  const names = meeting.participants?.slice(0, 3).join(', ') || 'No participants';

  return (
    <div onClick={onClick} className="bg-white border border-gray-200 rounded-lg p-5 hover:border-indigo-300 cursor-pointer transition-colors">
      <div className="flex justify-between items-start">
        <h3 className="font-semibold text-gray-900">{meeting.title}</h3>
        <span className="text-sm text-gray-400 whitespace-nowrap ml-4">{meeting.date}</span>
      </div>
      <p className="text-sm text-gray-500 mt-1">
        👥 {names}{participantCount > 3 ? ` +${participantCount - 3} more` : ''} ({participantCount} participants)
      </p>
      <div className="flex justify-between items-center mt-3">
        <p className="text-xs text-gray-400">Uploaded {meeting.createdAt?.split('T')[0]}</p>
        <span className="text-xs text-indigo-600 font-medium">View details →</span>
      </div>
    </div>
  );
}
