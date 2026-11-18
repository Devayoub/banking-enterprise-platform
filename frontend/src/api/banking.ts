import { apiClient, unwrap } from './client'
import type {
  Account,
  ApiResponse,
  LoanApplicationAccepted,
  LoanApplicationRequest,
  PageResponse,
  Transaction,
} from './types'

export function fetchAccounts(signal?: AbortSignal): Promise<Account[]> {
  return unwrap(apiClient.get<ApiResponse<Account[]>>('/api/v1/accounts', { signal }))
}

export function fetchTransactions(
  accountId: string,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<PageResponse<Transaction>> {
  return unwrap(
    apiClient.get<ApiResponse<PageResponse<Transaction>>>(
      `/api/v1/accounts/${encodeURIComponent(accountId)}/transactions`,
      { params: { page, size }, signal },
    ),
  )
}

export function submitLoanApplication(request: LoanApplicationRequest): Promise<LoanApplicationAccepted> {
  return unwrap(apiClient.post<ApiResponse<LoanApplicationAccepted>>('/api/v1/loans', request))
}
