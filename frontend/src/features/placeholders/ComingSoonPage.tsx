import { Card } from '../../components/Card'

/** Placeholder for features planned in later sprints (transfers, KYC onboarding). */
export function ComingSoonPage({ title, description }: { title: string; description: string }) {
  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <Card>
        <p className="text-slate-700">{description}</p>
      </Card>
    </div>
  )
}
