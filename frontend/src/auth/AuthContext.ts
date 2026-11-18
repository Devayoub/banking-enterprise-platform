import { createContext } from 'react'
import type { Role } from './keycloak'

export interface UserProfile {
  subject: string
  username: string
  firstName?: string
  lastName?: string
  email?: string
}

export interface AuthState {
  initialized: boolean
  authenticated: boolean
  profile: UserProfile | null
  roles: Role[]
  login: (redirectPath?: string) => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: Role) => boolean
}

export const AuthContext = createContext<AuthState | null>(null)
