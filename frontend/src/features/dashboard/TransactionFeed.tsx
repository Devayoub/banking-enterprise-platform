import { fetchTransactions } from '../../api/banking'
import type { Account } from '../../api/types'
import { Card } from '../../components/Card'
import { ErrorState } from '../../components/ErrorState'
import { Skeleton } from '../../components/Skeleton'
import { StatusBadge } from '../../components/StatusBadge'
import { useAsync } from '../../hooks/useAsync'
import { formatDate, formatMoney } from '../../lib/format'

/** Polls every 30 s for now. Replace with SSE / WebSocket fed by Kafka for true real-time updates. */
const REFRESH_INTERVAL_MS = 30_000

export function TransactionFeed({ account }: { account: Account }) {
  const { data, error, loading, reload } = useAsync(
    (signal) => fetchTransactions(account.id, 0, 20, signal),
    [account.id],
    REFRESH_INTERVAL_MS,
  )

  return (
    <Card
      title={`Recent transactions: ${account.displayName}`}
      titleId="transactions-heading"
      actions={
        <button
          type="button"
          onClick={reload}
          className="rounded-lg px-3 py-1.5 text-sm font-medium text-brand-800 ring-1 ring-slate-300 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
        >
          Refresh
        </button>
      }
    >
      <p className="sr-only" role="status" aria-live="polite">
        {loading ? 'Loading transactions' : `${data?.totalElements ?? 0} transactions loaded`}
      </p>

      {error && <ErrorState error={error} onRetry={reload} />}

      {!data && loading && (
        <ul className="space-y-3" aria-hidden="true">
          {Array.from({ length: 5 }, (_, i) => (
            <li key={i} className="flex items-center justify-between">
              <Skeleton className="h-4 w-48" />
              <Skeleton className="h-4 w-20" />
            </li>
          ))}
        </ul>
      )}

      {data && data.content.length === 0 && <p className="text-sm text-slate-600">No transactions yet.</p>}

      {data && data.content.length > 0 && (
        <table className="w-full text-sm">
          <caption className="sr-only">Most recent transactions, newest first</caption>
          <thead>
            <tr className="border-b border-slate-200 text-left text-slate-600">
              <th scope="col" className="py-2 font-medium">Date</th>
              <th scope="col" className="py-2 font-medium">Description</th>
              <th scope="col" className="py-2 font-medium">Type</th>
              <th scope="col" className="py-2 text-right font-medium">Amount</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((tx) => {
              const signed = tx.direction === 'DEBIT' ? '−' : '+'
              return (
                <tr key={tx.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2.5 whitespace-nowrap text-slate-600">{formatDate(tx.bookingDate)}</td>
                  <td className="py-2.5 text-slate-900">{tx.description ?? tx.reference}</td>
                  <td className="py-2.5">
                    <StatusBadge status={tx.direction} />
                  </td>
                  <td
                    className={`py-2.5 text-right font-medium whitespace-nowrap tabular-nums ${
                      tx.direction === 'CREDIT' ? 'text-emerald-800' : 'text-slate-900'
                    }`}
                  >
                    {signed}
                    {formatMoney(tx.amount, tx.currency)}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      )}
    </Card>
  )
}
