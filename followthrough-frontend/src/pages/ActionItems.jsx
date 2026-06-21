import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getAllActionItems, getOverdueActionItems, updateStatus } from '../api/actionItems';
import { toast } from '../components/Toast';

const tabs = ['All', 'Pending', 'Overdue', 'Completed'];

const badgeStyle = {
  PENDING: 'bg-gray-100 text-gray-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-green-100 text-green-700',
  OVERDUE: 'bg-red-100 text-red-700',
};

function daysOverdue(dueDate) {
  const diff = Math.floor((Date.now() - new Date(dueDate).getTime()) / 86400000);
  return diff > 0 ? diff : 0;
}

function sortItems(items) {
  const order = { OVERDUE: 0, PENDING: 1, IN_PROGRESS: 1, COMPLETED: 2 };
  return [...items].sort((a, b) => (order[a.status] ?? 1) - (order[b.status] ?? 1));
}

export default function ActionItems() {
  const [activeTab, setActiveTab] = useState('All');
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: allData, isLoading } = useQuery({
    queryKey: ['actionItems'],
    queryFn: () => getAllActionItems(),
  });

  const { data: overdueData } = useQuery({
    queryKey: ['actionItems', 'overdue'],
    queryFn: getOverdueActionItems,
  });

  const mutation = useMutation({
    mutationFn: (id) => updateStatus(id, 'COMPLETED'),
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ['actionItems'] });
      const prev = queryClient.getQueryData(['actionItems']);
      queryClient.setQueryData(['actionItems'], (old) => {
        if (!old) return old;
        const content = old.content || old;
        const updated = (Array.isArray(content) ? content : []).map((item) =>
          item.id === id ? { ...item, status: 'COMPLETED' } : item
        );
        return old.content ? { ...old, content: updated } : updated;
      });
      return { prev };
    },
    onError: (_err, _id, context) => {
      queryClient.setQueryData(['actionItems'], context.prev);
      toast('Failed to update status. Try again.');
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['actionItems'] });
    },
  });

  const allItems = Array.isArray(allData) ? allData : allData?.content || [];
  const overdueItems = overdueData || [];

  const filtered = (() => {
    if (activeTab === 'Overdue') return overdueItems;
    if (activeTab === 'Pending') return allItems.filter((i) => i.status === 'PENDING' || i.status === 'IN_PROGRESS');
    if (activeTab === 'Completed') return allItems.filter((i) => i.status === 'COMPLETED');
    return allItems;
  })();

  const sorted = sortItems(filtered);

  const stats = {
    total: allItems.length,
    pending: allItems.filter((i) => i.status === 'PENDING' || i.status === 'IN_PROGRESS').length,
    overdue: allItems.filter((i) => i.status === 'OVERDUE').length,
    completed: allItems.filter((i) => i.status === 'COMPLETED').length,
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="h-24 bg-gray-100 rounded-lg animate-pulse" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Action Items</h1>
        <p className="text-gray-500 mt-1">Everything your team committed to, tracked.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatCard label="Total" value={stats.total} />
        <StatCard label="Pending" value={stats.pending} />
        <StatCard label="Overdue" value={stats.overdue} highlight={stats.overdue > 0} />
        <StatCard label="Completed" value={stats.completed} />
      </div>

      {/* Tabs */}
      <div className="flex gap-6 border-b border-gray-200">
        {tabs.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`pb-2 text-sm font-medium ${activeTab === tab ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500 hover:text-gray-700'}`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* List */}
      {sorted.length === 0 ? (
        <EmptyState tab={activeTab} onUpload={() => navigate('/')} />
      ) : (
        <div className="space-y-3">
          {sorted.map((item) => (
            <ActionItemRow key={item.id} item={item} onComplete={() => mutation.mutate(item.id)} />
          ))}
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, highlight }) {
  return (
    <div className={`rounded-lg p-4 text-center ${highlight ? 'bg-red-50 border border-red-200' : 'bg-white border border-gray-200'}`}>
      <p className={`text-2xl font-bold ${highlight ? 'text-red-600' : 'text-gray-900'}`}>{value}</p>
      <p className="text-xs text-gray-500">{label}</p>
    </div>
  );
}

function ActionItemRow({ item, onComplete }) {
  const isCompleted = item.status === 'COMPLETED';
  const isOverdue = item.status === 'OVERDUE';

  return (
    <div className={`bg-white border border-gray-200 rounded-lg p-4 ${isCompleted ? 'opacity-60' : ''}`}>
      <div className="flex justify-between items-start">
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${badgeStyle[item.status]}`}>
              {item.status.replace('_', ' ')}
            </span>
            {isOverdue && (
              <span className="text-xs text-red-500">{daysOverdue(item.dueDate)} days overdue</span>
            )}
          </div>
          <p className="font-medium text-gray-900">{item.task}</p>
          <p className="text-sm text-gray-500">
            👤 {item.assignedTo} &nbsp; 📅 {item.dueDate}
          </p>
        </div>
        {!isCompleted && (
          <button
            onClick={onComplete}
            className="text-xs px-3 py-1.5 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 font-medium whitespace-nowrap"
          >
            Mark complete ✓
          </button>
        )}
      </div>
    </div>
  );
}

function EmptyState({ tab, onUpload }) {
  const messages = {
    All: { text: 'No action items yet.', sub: 'Upload a meeting to get started.', showButton: true },
    Pending: { text: 'No pending items.', sub: 'Everything is either done or overdue.' },
    Overdue: { text: 'No overdue items.', sub: 'Your team is on track 🎉' },
    Completed: { text: 'Nothing completed yet.', sub: 'Get to work!' },
  };
  const m = messages[tab] || messages.All;

  return (
    <div className="text-center py-12">
      <p className="text-gray-500 font-medium">{m.text}</p>
      <p className="text-sm text-gray-400 mt-1">{m.sub}</p>
      {m.showButton && (
        <button onClick={onUpload} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
          Upload a meeting →
        </button>
      )}
    </div>
  );
}
