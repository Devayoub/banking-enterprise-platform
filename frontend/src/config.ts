export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  keycloak: {
    url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8180',
    realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'banking-realm',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'banking-frontend',
  },
} as const
