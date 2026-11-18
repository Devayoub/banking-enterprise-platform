import { useState, type FormEvent } from 'react'
import { submitLoanApplication } from '../../api/banking'
import type { LoanApplicationAccepted } from '../../api/types'
import { Card } from '../../components/Card'
import { ErrorState } from '../../components/ErrorState'

const inputClass =
  'mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-brand-600'

/** Starts the Camunda loan-approval process. The decision arrives asynchronously (underwriter review). */
export function LoansPage() {
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<LoanApplicationAccepted>()
  const [error, setError] = useState<Error>()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSubmitting(true)
    setError(undefined)
    try {
      setResult(
        await submitLoanApplication({
          amount: String(form.get('amount')),
          currency: 'EUR',
          termMonths: Number(form.get('termMonths')),
          monthlyIncome: String(form.get('monthlyIncome')),
        }),
      )
    } catch (err) {
      setError(err instanceof Error ? err : new Error('The application could not be submitted.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Apply for a personal loan</h1>

      <Card>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="amount" className="text-sm font-medium text-slate-800">
              Amount (EUR)
            </label>
            <input id="amount" name="amount" type="number" min="500" max="500000" step="0.01" required className={inputClass} />
          </div>
          <div>
            <label htmlFor="termMonths" className="text-sm font-medium text-slate-800">
              Duration (months)
            </label>
            <input id="termMonths" name="termMonths" type="number" min="6" max="360" step="1" required className={inputClass} />
          </div>
          <div>
            <label htmlFor="monthlyIncome" className="text-sm font-medium text-slate-800">
              Net monthly income (EUR)
            </label>
            <input id="monthlyIncome" name="monthlyIncome" type="number" min="0" step="0.01" required className={inputClass} />
          </div>
          <button
            type="submit"
            disabled={submitting}
            className="rounded-lg bg-brand-700 px-4 py-2 font-medium text-white hover:bg-brand-800 disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
          >
            {submitting ? 'Submitting…' : 'Submit application'}
          </button>
        </form>
      </Card>

      <div aria-live="polite">
        {error && <ErrorState error={error} />}
        {result && (
          <p role="status" className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900">
            Application received (reference {result.applicationId}). An underwriter will review it and you will be
            notified of the decision.
          </p>
        )}
      </div>
    </div>
  )
}
