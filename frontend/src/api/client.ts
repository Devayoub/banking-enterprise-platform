import axios, { AxiosError, type AxiosResponse } from 'axios'
import { getValidToken, keycloak } from '../auth/keycloak'
import { config } from '../config'
import type { ApiResponse } from './types'

/** Shared HTTP client: every request goes to the gateway with a fresh bearer token and a correlation id. */
export const apiClient = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: 15_000,
  headers: { Accept: 'application/json' },
})

apiClient.interceptors.request.use(async (request) => {
  const token = await getValidToken(30)
  if (token) {
    request.headers.set('Authorization', `Bearer ${token}`)
  }
  request.headers.set('X-Correlation-Id', crypto.randomUUID())
  return request
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response?.status === 401) {
      await keycloak.login({ redirectUri: window.location.href })
    }
    return Promise.reject(toApiError(error))
  },
)

export class ApiError extends Error {
  readonly code: string
  readonly status?: number
  readonly correlationId?: string

  constructor(message: string, code: string, status?: number, correlationId?: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
    this.correlationId = correlationId
  }
}

function toApiError(error: AxiosError): ApiError {
  const body = error.response?.data as ApiResponse<unknown> | undefined
  const correlationId = error.response?.headers?.['x-correlation-id'] as string | undefined
  if (body?.error) {
    return new ApiError(body.error.message, body.error.code, error.response?.status, correlationId)
  }
  if (!error.response) {
    return new ApiError('We could not reach the bank. Check your connection and try again.', 'NETWORK_ERROR')
  }
  return new ApiError('Something went wrong. Please try again.', 'UNEXPECTED', error.response.status, correlationId)
}

/** Unwraps the platform's { success, data, error } envelope. */
export async function unwrap<T>(call: Promise<AxiosResponse<ApiResponse<T>>>): Promise<T> {
  const { data } = await call
  if (!data.success || data.data === undefined) {
    throw new ApiError(data.error?.message ?? 'Unexpected response', data.error?.code ?? 'UNEXPECTED')
  }
  return data.data
}
