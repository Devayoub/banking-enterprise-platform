import { ApiError } from '../api/client'

interface ErrorStateProps {
  error: Error
  onRetry?: () => void
}

export function ErrorState({ error, onRetry }: ErrorStateProps) {
  const correlationId = error instanceof ApiError ? error.correlationId : undefined
  return (
    <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
      <p className="font-medium">{error.message}</p>
      {correlationId && <p className="mt-1 text-rose-800">Reference for support: {correlationId}</p>}
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-3 rounded-lg bg-white px-3 py-1.5 font-medium text-rose-900 ring-1 ring-rose-300 hover:bg-rose-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rose-700"
        >
          Try again
        </button>
      )}
    </div>
  )
}
