import Keycloak from 'keycloak-js'
import { config } from '../config'

/** Single Keycloak instance for the whole app. Tokens live in memory only (never localStorage). */
export const keycloak = new Keycloak({
  url: config.keycloak.url,
  realm: config.keycloak.realm,
  clientId: config.keycloak.clientId,
})

export type Role = 'CUSTOMER' | 'TELLER' | 'UNDERWRITER' | 'ADMIN'

let initPromise: Promise<boolean> | null = null

/**
 * Initialises Keycloak once, even under React StrictMode (which mounts effects twice in development).
 * 'check-sso' restores an existing session silently without forcing a login on public pages.
 */
export function initKeycloak(): Promise<boolean> {
  initPromise ??= keycloak.init({
    onLoad: 'check-sso',
    pkceMethod: 'S256',
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    checkLoginIframe: false,
  })
  return initPromise
}

/** Returns a token valid for at least `minValiditySeconds`, refreshing it first if needed. */
export async function getValidToken(minValiditySeconds = 30): Promise<string | undefined> {
  if (!keycloak.authenticated) {
    return undefined
  }
  try {
    await keycloak.updateToken(minValiditySeconds)
    return keycloak.token
  } catch {
    // Refresh token expired or session ended in Keycloak: start a fresh login.
    await keycloak.login({ redirectUri: window.location.href })
    return undefined
  }
}
