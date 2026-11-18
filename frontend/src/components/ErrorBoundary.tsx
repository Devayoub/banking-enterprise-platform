import { Component, type ErrorInfo, type ReactNode } from 'react'

interface Props {
  children: ReactNode
  /** Short name of the area, shown to the user and in logs. */
  area?: string
}

interface State {
  hasError: boolean
}

/** Catches render errors so one broken widget never blanks the whole banking app. */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // Hook point for the front-end monitoring tool. Never log tokens or customer data.
    console.error(`[ErrorBoundary:${this.props.area ?? 'app'}]`, error.name, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
          <p className="font-medium">This section could not be displayed.</p>
          <button
            type="button"
            onClick={() => this.setState({ hasError: false })}
            className="mt-3 rounded-lg bg-white px-3 py-1.5 font-medium ring-1 ring-rose-300 hover:bg-rose-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rose-700"
          >
            Reload section
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
