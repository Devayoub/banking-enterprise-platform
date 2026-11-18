import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { AuthContext, type AuthState, type UserProfile } from './AuthContext'
import { initKeycloak, keycloak, type Role } from './keycloak'

const KNOWN_ROLES: readonly Role[] = ['CUSTOMER', 'TELLER', 'UNDERWRITER', 'ADMIN']

function readSession(): { profile: UserProfile | null; roles: Role[] } {
  const token = keycloak.tokenParsed
  if (!keycloak.authenticated || !token?.sub) {
    return { profile: null, roles: [] }
  }
  const claims = token as Record<string, unknown>
  const roles = (token.realm_access?.roles ?? []).filter((r): r is Role =>
    (KNOWN_ROLES as readonly string[]).includes(r),
  )
  return {
    profile: {
      subject: token.sub,
      username: String(claims.preferred_username ?? token.sub),
      firstName: claims.given_name as string | undefined,
      lastName: claims.family_name as string | undefined,
      email: claims.email as string | undefined,
    },
    roles,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [initialized, setInitialized] = useState(false)
  const [authenticated, setAuthenticated] = useState(false)
  const [session, setSession] = useState(readSession)
  const [initError, setInitError] = useState<string | null>(null)

  useEffect(() => {
    const sync = () => {
      setAuthenticated(Boolean(keycloak.authenticated))
      setSession(readSession())
    }

    keycloak.onAuthSuccess = sync
    keycloak.onAuthRefreshSuccess = sync
    keycloak.onAuthLogout = sync
    // Refresh proactively so API calls rarely wait on a token refresh.
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => keycloak.clearToken())
    }

    initKeycloak()
      .then(() => sync())
      .catch(() => setInitError('The sign-in service is unavailable. Please try again later.'))
      .finally(() => setInitialized(true))
  }, [])

  const login = useCallback(async (redirectPath?: string) => {
    const redirectUri = redirectPath ? `${window.location.origin}${redirectPath}` : window.location.href
    await keycloak.login({ redirectUri })
  }, [])

  const logout = useCallback(async () => {
    await keycloak.logout({ redirectUri: window.location.origin })
  }, [])

  const value = useMemo<AuthState>(
    () => ({
      initialized,
      authenticated,
      profile: session.profile,
      roles: session.roles,
      login,
      logout,
      hasRole: (role: Role) => session.roles.includes(role),
    }),
    [initialized, authenticated, session, login, logout],
  )

  if (!initialized) {
    return (
      <div role="status" aria-live="polite" className="grid min-h-screen place-items-center text-slate-600">
        Connecting securely…
      </div>
    )
  }

  if (initError) {
    return (
      <div role="alert" className="grid min-h-screen place-items-center p-6 text-center text-rose-700">
        {initError}
      </div>
    )
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
