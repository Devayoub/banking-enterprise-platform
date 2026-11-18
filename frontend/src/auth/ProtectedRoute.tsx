import { useEffect, type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import type { Role } from './keycloak'
import { useAuth } from './useAuth'

interface ProtectedRouteProps {
  children: ReactNode
  /** The user needs at least one of these roles. Omit to only require a signed-in user. */
  roles?: Role[]
}

/**
 * Unauthenticated users are sent to the Keycloak login page and come back to the page they asked for.
 * Signed-in users without a required role see the 403 page.
 * This is UX only: the gateway and services enforce the same rules on every API call.
 */
export function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { authenticated, hasRole, login } = useAuth()
  const location = useLocation()

  useEffect(() => {
    if (!authenticated) {
      void login(location.pathname + location.search)
    }
  }, [authenticated, login, location.pathname, location.search])

  if (!authenticated) {
    return (
      <p role="status" aria-live="polite" className="p-8 text-slate-600">
        Redirecting to secure sign-in…
      </p>
    )
  }

  if (roles && !roles.some(hasRole)) {
    return <Navigate to="/forbidden" replace />
  }

  return <>{children}</>
}
