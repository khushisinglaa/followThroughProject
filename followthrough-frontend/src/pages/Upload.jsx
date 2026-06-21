import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createMeeting, extractMeeting } from '../api/meetings';

const loadingMessages = [
  'Reading your meeting...',
  'Finding decisions...',
  'Identifying action items...',
  'Almost done...',
];

export default function Upload() {
  const [title, setTitle] = useState('');
  const [participants, setParticipants] = useState('');
  const [transcript, setTranscript] = useState('');
  const [status, setStatus] = useState('idle'); // idle | loading | success | error
  const [result, setResult] = useState(null);
  const [loadingMsgIndex, setLoadingMsgIndex] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    if (status !== 'loading') return;
    const interval = setInterval(() => {
      setLoadingMsgIndex((i) => (i + 1) % loadingMessages.length);
    }, 3000);
    return () => clearInterval(interval);
  }, [status]);

  const handleSubmit = async () => {
    setStatus('loading');
    setLoadingMsgIndex(0);
    try {
      const meeting = await createMeeting({
        title: title || 'Untitled Meeting',
        date: new Date().toISOString().split('T')[0],
        participants: participants.split(',').map((p) => p.trim()).filter(Boolean),
        rawTranscript: transcript,
      });
      const extraction = await extractMeeting(meeting.id);
      setResult(extraction);
      setStatus('success');
    } catch {
      setStatus('error');
    }
  };

  const reset = () => {
    setTitle('');
    setParticipants('');
    setTranscript('');
    setStatus('idle');
    setResult(null);
  };

  if (status === 'success' && result) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-green-600">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <h1 className="text-2xl font-bold">Extraction complete</h1>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-5">
          <p className="text-sm text-gray-500 mb-3">Found in your meeting</p>
          <div className="flex gap-4">
            <span className="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-sm font-medium">
              {result.decisionsExtracted} decisions
            </span>
            <span className="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm font-medium">
              {result.actionItemsExtracted} action items
            </span>
          </div>
        </div>

        {result.decisions?.length > 0 && (
          <div className="bg-white border border-gray-200 rounded-lg p-5">
            <h2 className="font-semibold mb-3">Decisions</h2>
            <ul className="space-y-2">
              {result.decisions.map((d) => (
                <li key={d.id} className="flex justify-between text-sm">
                  <span>{d.title}</span>
                  <span className="text-gray-400">{d.owner}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {result.actionItems?.length > 0 && (
          <div className="bg-white border border-gray-200 rounded-lg p-5">
            <h2 className="font-semibold mb-3">Action Items</h2>
            <ul className="space-y-2">
              {result.actionItems.map((a) => (
                <li key={a.id} className="flex justify-between text-sm">
                  <span>{a.task}</span>
                  <span className="text-gray-400">{a.assignedTo} · {a.dueDate}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="flex gap-3">
          <button onClick={() => navigate('/decisions')} className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
            View all decisions →
          </button>
          <button onClick={() => navigate('/action-items')} className="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">
            View all action items →
          </button>
        </div>
        <button onClick={reset} className="text-sm text-gray-500 hover:text-gray-700 underline">
          Upload another meeting
        </button>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="space-y-4">
        <div className="bg-red-50 border border-red-200 rounded-lg p-5">
          <div className="flex items-center gap-2 text-red-600 mb-2">
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
            <h2 className="font-semibold">Extraction failed</h2>
          </div>
          <p className="text-sm text-red-700">
            Something went wrong while reading your meeting. Check that your transcript has enough content and try again.
          </p>
        </div>
        <button onClick={() => setStatus('idle')} className="px-4 py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-800">
          Try again
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">What happened in your meeting?</h1>
        <p className="text-gray-500 mt-1">Paste your transcript or MOM below. We'll extract the decisions and action items.</p>
      </div>

      <div className="space-y-4">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="e.g. Backend Architecture Review — June 2026"
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <input
          type="text"
          value={participants}
          onChange={(e) => setParticipants(e.target.value)}
          placeholder="Rahul, Khushi, Amit — comma separated"
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <textarea
          value={transcript}
          onChange={(e) => setTranscript(e.target.value)}
          rows={10}
          placeholder={`Paste your full meeting transcript or MOM here...\n\nExample:\nRahul: We need to decide on our caching strategy.\nKhushi: Redis gives us more than just key-value — pub/sub too.\nAmit: Let's go with Redis. I'll set it up by Friday.`}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-y"
        />
      </div>

      <button
        onClick={handleSubmit}
        disabled={!transcript.trim() || status === 'loading'}
        className="px-5 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {status === 'loading' ? (
          <span className="flex items-center gap-2">
            <svg className="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
            </svg>
            {loadingMessages[loadingMsgIndex]}
          </span>
        ) : (
          'Extract decisions & action items →'
        )}
      </button>
    </div>
  );
}
