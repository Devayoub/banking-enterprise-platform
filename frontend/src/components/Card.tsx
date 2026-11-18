import type { ReactNode } from 'react'

interface CardProps {
  title?: string
  titleId?: string
  actions?: ReactNode
  children: ReactNode
  className?: string
}

export function Card({ title, titleId, actions, children, className = '' }: CardProps) {
  return (
    <section
      aria-labelledby={title ? titleId : undefined}
      className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-sm ${className}`}
    >
      {(title || actions) && (
        <header className="mb-4 flex items-center justify-between gap-4">
          {title && (
            <h2 id={titleId} className="text-base font-semibold text-slate-900">
              {title}
            </h2>
          )}
          {actions}
        </header>
      )}
      {children}
    </section>
  )
}
