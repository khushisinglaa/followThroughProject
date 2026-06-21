import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

const links = [
  { to: '/upload', label: 'Upload' },
  { to: '/decisions', label: 'Decisions' },
  { to: '/action-items', label: 'Action Items' },
  { to: '/history', label: 'History' },
];

export default function Layout() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <nav className="bg-white border-b border-gray-200 px-6 py-3">
        <div className="flex items-center justify-between">
          <NavLink to="/" className="text-lg font-bold text-indigo-600">FollowThrough</NavLink>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-6">
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
          </div>

          {/* Hamburger button */}
          <button onClick={() => setMenuOpen(!menuOpen)} className="md:hidden p-1 text-gray-600">
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              {menuOpen ? (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              )}
            </svg>
          </button>
        </div>

        {/* Mobile nav */}
        {menuOpen && (
          <div className="md:hidden mt-3 pb-2 space-y-2">
            {links.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `block text-sm font-medium py-1 ${isActive ? 'text-indigo-600' : 'text-gray-600'}`
                }
              >
                {label}
              </NavLink>
            ))}
          </div>
        )}
      </nav>
      <main className="max-w-5xl mx-auto p-6 flex-1 w-full">
        <Outlet />
      </main>
      <footer className="text-center text-sm text-gray-400 border-t mt-12 py-6">
        Built by Khushi Singla · <a href="https://github.com/khushisinglaa" className="underline hover:text-gray-600">GitHub</a>
      </footer>
    </div>
  );
}
