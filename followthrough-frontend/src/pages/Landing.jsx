import { useNavigate } from 'react-router-dom';
import { useTitle } from '../hooks/useTitle';

export default function Landing() {
  useTitle('Home');
  const navigate = useNavigate();

  return (
    <div className="space-y-20 py-10">
      {/* Hero */}
      <section className="text-center max-w-2xl mx-auto">
        <h1 className="text-4xl font-bold text-gray-900 leading-tight">
          Your meetings decide things.<br />
          <span className="text-indigo-600">FollowThrough remembers why.</span>
        </h1>
        <p className="mt-4 text-lg text-gray-500">
          Paste your MOM or transcript. We extract every decision, every action item, every deadline — and track them so nothing slips.
        </p>
        <button
          onClick={() => navigate('/upload')}
          className="mt-6 px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 text-lg"
        >
          Try it free →
        </button>
      </section>

      {/* How it works */}
      <section className="max-w-3xl mx-auto">
        <h2 className="text-center text-sm font-semibold text-gray-400 uppercase tracking-wide mb-8">How it works</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Step number="1" title="Paste your transcript" desc="Copy your meeting notes, MOM, or full transcript and paste it in." />
          <Step number="2" title="AI extracts intelligence" desc="We identify every decision, action item, owner, and deadline automatically." />
          <Step number="3" title="Search & track forever" desc="Decisions are searchable. Action items have reminders. Nothing falls through." />
        </div>
      </section>

      {/* The Problem */}
      <section className="max-w-2xl mx-auto text-center">
        <blockquote className="text-xl text-gray-700 leading-relaxed">
          "Every team documents what was decided.<br />
          Almost no team remembers <em>why</em>.<br /><br />
          Six months later, nobody knows why you chose Kafka over RabbitMQ.<br />
          <strong className="text-gray-900">FollowThrough fixes that.</strong>"
        </blockquote>
      </section>
    </div>
  );
}

function Step({ number, title, desc }) {
  return (
    <div className="text-center">
      <div className="w-10 h-10 rounded-full bg-indigo-100 text-indigo-600 font-bold flex items-center justify-center mx-auto mb-3">
        {number}
      </div>
      <h3 className="font-semibold text-gray-900">{title}</h3>
      <p className="text-sm text-gray-500 mt-1">{desc}</p>
    </div>
  );
}
