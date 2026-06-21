import { NavLink, Outlet } from 'react-router-dom';

const links = [
  { to: '/', label: 'Upload' },
  { to: '/decisions', label: 'Decisions' },
  { to: '/action-items', label: 'Action Items' },
  { to: '/history', label: 'History' },
];

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center gap-8">
        <span className="text-lg font-bold text-indigo-600">FollowThrough</span>
        {links.map(({ to, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `text-sm font-medium ${isActive ? 'text-indigo-600' : 'text-gray-600 hover:text-gray-900'}`
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
      <main className="max-w-5xl mx-auto p-6">
        <Outlet />
      </main>
    </div>
  );
}
