import { NavLink, Outlet } from 'react-router'
import { useAuth } from '../auth/useAuth'
import type { Role } from '../auth/keycloak'

const NAV: { to: string; label: string; roles?: Role[] }[] = [
  { to: '/dashboard', label: 'Dashboard', roles: ['CUSTOMER'] },
  { to: '/transfers', label: 'Transfers', roles: ['CUSTOMER'] },
  { to: '/loans', label: 'Loans', roles: ['CUSTOMER'] },
  { to: '/kyc', label: 'Identity (KYC)', roles: ['CUSTOMER'] },
]

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 ${
    isActive ? 'bg-brand-50 text-brand-800' : 'text-slate-700 hover:bg-slate-100'
  }`

export function AppLayout() {
  const { authenticated, profile, roles, login, logout } = useAuth()
  const visibleNav = NAV.filter((item) => !item.roles || item.roles.some((r) => roles.includes(r)))

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-white focus:px-4 focus:py-2 focus:shadow"
      >
        Skip to main content
      </a>

      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-4 py-3">
          <NavLink to="/" className="text-lg font-semibold tracking-tight text-brand-800">
            Digital Banking
          </NavLink>

          {authenticated && (
            <nav aria-label="Main">
              <ul className="flex flex-wrap gap-1">
                {visibleNav.map((item) => (
                  <li key={item.to}>
                    <NavLink to={item.to} className={linkClass}>
                      {item.label}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </nav>
          )}

          <div className="flex items-center gap-3 text-sm">
            {authenticated ? (
              <>
                <span className="text-slate-600">
                  Signed in as <span className="font-medium text-slate-900">{profile?.firstName ?? profile?.username}</span>
                </span>
                <button
                  type="button"
                  onClick={() => void logout()}
                  className="rounded-lg px-3 py-2 font-medium text-slate-700 ring-1 ring-slate-300 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
                >
                  Sign out
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={() => void login('/dashboard')}
                className="rounded-lg bg-brand-700 px-4 py-2 font-medium text-white hover:bg-brand-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
              >
                Sign in
              </button>
            )}
          </div>
        </div>
      </header>

      <main id="main" tabIndex={-1} className="mx-auto max-w-6xl px-4 py-8 focus:outline-none">
        <Outlet />
      </main>
    </div>
  )
}
