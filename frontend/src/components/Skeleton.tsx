/** Loading placeholder. Hidden from screen readers; pair it with a visible or sr-only status message. */
export function Skeleton({ className = '' }: { className?: string }) {
  return <div aria-hidden="true" className={`animate-pulse rounded-md bg-slate-200 motion-reduce:animate-none ${className}`} />
}
