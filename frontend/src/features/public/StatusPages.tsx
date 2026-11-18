import { Link } from 'react-router'

function StatusPage({ code, title, message }: { code: string; title: string; message: string }) {
  return (
    <section className="mx-auto max-w-xl py-16 text-center">
      <p className="text-sm font-semibold text-brand-700">{code}</p>
      <h1 className="mt-2 text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="mt-3 text-slate-700">{message}</p>
      <Link
        to="/"
        className="mt-6 inline-block rounded-lg px-4 py-2 font-medium text-brand-800 ring-1 ring-slate-300 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
      >
        Back to home
      </Link>
    </section>
  )
}

export function ForbiddenPage() {
  return <StatusPage code="403" title="Access denied" message="Your profile does not give access to this page." />
}

export function NotFoundPage() {
  return <StatusPage code="404" title="Page not found" message="The page you are looking for does not exist." />
}
