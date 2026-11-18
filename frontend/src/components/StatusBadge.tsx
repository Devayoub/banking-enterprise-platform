const STYLES: Record<string, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-800 ring-emerald-600/20',
  BLOCKED: 'bg-amber-50 text-amber-800 ring-amber-600/20',
  CLOSED: 'bg-slate-100 text-slate-700 ring-slate-500/20',
  CREDIT: 'bg-emerald-50 text-emerald-800 ring-emerald-600/20',
  DEBIT: 'bg-slate-100 text-slate-700 ring-slate-500/20',
}

/** Text label plus colour, so status is never conveyed by colour alone (WCAG 1.4.1). */
export function StatusBadge({ status }: { status: string }) {
  const style = STYLES[status] ?? STYLES.CLOSED
  const label = status.charAt(0) + status.slice(1).toLowerCase()
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset ${style}`}>
      {label}
    </span>
  )
}
