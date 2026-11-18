import { BrowserRouter, Route, Routes } from 'react-router'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AppLayout } from './components/AppLayout'
import { ErrorBoundary } from './components/ErrorBoundary'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { LoansPage } from './features/loans/LoansPage'
import { ComingSoonPage } from './features/placeholders/ComingSoonPage'
import { HomePage } from './features/public/HomePage'
import { ForbiddenPage, NotFoundPage } from './features/public/StatusPages'

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route
            path="dashboard"
            element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <ErrorBoundary area="dashboard">
                  <DashboardPage />
                </ErrorBoundary>
              </ProtectedRoute>
            }
          />
          <Route
            path="loans"
            element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <LoansPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="transfers"
            element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <ComingSoonPage
                  title="Transfers"
                  description="SEPA and instant transfers with strong customer authentication (step-up via Keycloak) arrive in a later sprint."
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="kyc"
            element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <ComingSoonPage
                  title="Identity verification (KYC)"
                  description="Document upload and identity checks, orchestrated by the Camunda KYC onboarding process, arrive in a later sprint."
                />
              </ProtectedRoute>
            }
          />
          <Route path="forbidden" element={<ForbiddenPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
