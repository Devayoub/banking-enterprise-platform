import { useCallback, useEffect, useRef, useState } from 'react'

export interface AsyncState<T> {
  data: T | undefined
  error: Error | undefined
  loading: boolean
  reload: () => void
}

/**
 * Runs an async loader, cancels it on unmount or when deps change, and optionally re-runs it on an interval.
 * Keeps the previous data while reloading so the UI does not flash back to skeletons on every refresh.
 */
export function useAsync<T>(
  loader: (signal: AbortSignal) => Promise<T>,
  deps: readonly unknown[],
  refreshIntervalMs?: number,
): AsyncState<T> {
  const [data, setData] = useState<T>()
  const [error, setError] = useState<Error>()
  const [loading, setLoading] = useState(true)
  const [tick, setTick] = useState(0)
  const loaderRef = useRef(loader)

  // Always call the latest loader without re-running the effect on every render.
  useEffect(() => {
    loaderRef.current = loader
  })

  const reload = useCallback(() => setTick((t) => t + 1), [])

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    loaderRef
      .current(controller.signal)
      .then((result) => {
        setData(result)
        setError(undefined)
      })
      .catch((err: unknown) => {
        if (!controller.signal.aborted) {
          setError(err instanceof Error ? err : new Error(String(err)))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })
    return () => controller.abort()
  }, [...deps, tick])

  useEffect(() => {
    if (!refreshIntervalMs) return
    const id = window.setInterval(reload, refreshIntervalMs)
    return () => window.clearInterval(id)
  }, [refreshIntervalMs, reload])

  return { data, error, loading, reload }
}
