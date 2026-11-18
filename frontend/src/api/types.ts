/** Mirrors com.enterprise.banking.common.api.* and the core-service DTOs. */

export interface ApiErrorBody {
  code: string
  message: string
  details?: { field: string; message: string }[]
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: ApiErrorBody
  correlationId?: string
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AccountType = 'CURRENT' | 'SAVINGS' | 'CARD'
export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED'

export interface Account {
  id: string
  displayName: string
  maskedIban: string
  type: AccountType
  currency: string
  /** Serialized from BigDecimal; may arrive as a number or a string. Never do arithmetic on it in the UI. */
  balance: number | string
  status: AccountStatus
  lastUpdated: string
}

export interface Transaction {
  id: string
  reference: string
  direction: 'CREDIT' | 'DEBIT'
  amount: number | string
  currency: string
  description?: string
  bookingDate: string
  valueDate: string
}

export interface LoanApplicationRequest {
  amount: string
  currency: string
  termMonths: number
  monthlyIncome: string
}

export interface LoanApplicationAccepted {
  applicationId: string
  processInstanceId: string
  status: string
}
