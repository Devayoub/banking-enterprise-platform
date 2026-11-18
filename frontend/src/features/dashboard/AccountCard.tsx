import type { Account } from '../../api/types'
import { StatusBadge } from '../../components/StatusBadge'
import { formatMoney } from '../../lib/format'

const TYPE_LABEL: Record<Account['type'], string> = {
  CURRENT: 'Current account',
  SAVINGS: 'Savings',
  CARD: 'Card',
}

interface AccountCardProps {
  account: Account
  selected: boolean
  onSelect: (accountId: string) => void
}

export function AccountCard({ account, selected, onSelect }: AccountCardProps) {
  return (
    <button
      type="button"
      onClick={() => onSelect(account.id)}
      aria-pressed={selected}
      className={`w-full rounded-2xl border bg-white p-5 text-left shadow-sm transition hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 motion-reduce:transition-none ${
        selected ? 'border-brand-600 ring-1 ring-brand-600' : 'border-slate-200'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-slate-600">{TYPE_LABEL[account.type]}</p>
          <p className="font-semibold text-slate-900">{account.displayName}</p>
        </div>
        <StatusBadge status={account.status} />
      </div>
      <p className="mt-4 text-2xl font-semibold tabular-nums text-slate-900">
        {formatMoney(account.balance, account.currency)}
      </p>
      <p className="mt-1 font-mono text-xs text-slate-600">
        <span className="sr-only">IBAN, partially hidden: </span>
        {account.maskedIban}
      </p>
    </button>
  )
}
