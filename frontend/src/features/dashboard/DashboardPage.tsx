import { useState } from 'react'
import { fetchAccounts } from '../../api/banking'
import { useAuth } from '../../auth/useAuth'
import { ErrorBoundary } from '../../components/ErrorBoundary'
import { ErrorState } from '../../components/ErrorState'
import { Skeleton } from '../../components/Skeleton'
import { useAsync } from '../../hooks/useAsync'
import { AccountCard } from './AccountCard'
import { TransactionFeed } from './TransactionFeed'

export function DashboardPage() {
  const { profile } = useAuth()
  const { data: accounts, error, loading, reload } = useAsync((signal) => fetchAccounts(signal), [])
  const [selectedId, setSelectedId] = useState<string>()

  const selected = accounts?.find((a) => a.id === selectedId) ?? accounts?.[0]

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">
          Hello{profile?.firstName ? `, ${profile.firstName}` : ''}
        </h1>
        <p className="mt-1 text-slate-600">Here is an overview of your accounts.</p>
      </header>

      <section aria-labelledby="accounts-heading">
        <h2 id="accounts-heading" className="mb-3 text-base font-semibold">
          Your accounts
        </h2>

        <p className="sr-only" role="status" aria-live="polite">
          {loading && !accounts ? 'Loading accounts' : ''}
        </p>

        {error && <ErrorState error={error} onRetry={reload} />}

        {!accounts && loading && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" aria-hidden="true">
            {[0, 1].map((i) => (
              <div key={i} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="mt-4 h-7 w-40" />
                <Skeleton className="mt-2 h-3 w-48" />
              </div>
            ))}
          </div>
        )}

        {accounts && accounts.length === 0 && <p className="text-slate-600">You have no accounts yet.</p>}

        {accounts && accounts.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {accounts.map((account) => (
              <AccountCard
                key={account.id}
                account={account}
                selected={account.id === selected?.id}
                onSelect={setSelectedId}
              />
            ))}
          </div>
        )}
      </section>

      {selected && (
        <ErrorBoundary area="transaction-feed">
          <TransactionFeed account={selected} />
        </ErrorBoundary>
      )}
    </div>
  )
}
