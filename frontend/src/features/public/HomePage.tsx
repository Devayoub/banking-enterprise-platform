import { Navigate } from 'react-router'
import { useAuth } from '../../auth/useAuth'

export function HomePage() {
  const { authenticated, hasRole, login } = useAuth()

  if (authenticated && hasRole('CUSTOMER')) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <section className="mx-auto max-w-2xl py-16 text-center">
      <h1 className="text-3xl font-semibold tracking-tight text-slate-900">Your bank, secured end to end</h1>
      <p className="mt-4 text-slate-700">
        Sign in with your bank credentials to see your accounts, follow your transactions and apply for a loan.
      </p>
      {!authenticated && (
        <button
          type="button"
          onClick={() => void login('/dashboard')}
          className="mt-8 rounded-lg bg-brand-700 px-5 py-2.5 font-medium text-white hover:bg-brand-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
        >
          Sign in
        </button>
      )}
    </section>
  )
}
