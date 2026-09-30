import {
  Braces,
  KeyRound,
  ShieldCheck,
  Sparkles,
  WalletCards,
} from 'lucide-react'
import PageHeader from '../components/common/PageHeader'
import RefreshButton from '../components/common/RefreshButton'
import UsageError from '../components/common/UsageError'
import UsageMetricGrid from '../components/common/UsageMetricGrid'
import useUsage from '../hooks/useUsage'

const capabilities = [
  { label: 'Authentication', value: 'JWT', icon: KeyRound },
  { label: 'AI Provider', value: 'Gemini', icon: Sparkles },
  { label: 'Rate Limiting', value: 'Enabled', icon: ShieldCheck },
  { label: 'Structured Output', value: 'Enabled', icon: Braces },
  { label: 'Cost Estimation', value: 'Supported', icon: WalletCards },
]

function DashboardPage() {
  const { usage, loading, refreshing, error, refresh } = useUsage()

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Overview"
        title="Dashboard"
        description="Monitor your AI Gateway usage and performance."
        actions={<RefreshButton onClick={refresh} loading={loading || refreshing} />}
      />

      <UsageError message={error} onRetry={refresh} disabled={loading || refreshing} />
      <UsageMetricGrid usage={usage} loading={loading && !usage} />

      <section className="card overview-card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Configuration</p>
            <h2>Gateway capabilities</h2>
            <p>Core capabilities available in the current backend.</p>
          </div>
        </div>
        <div className="capability-grid">
          {capabilities.map(({ label, value, icon: Icon }) => (
            <div className="capability-item" key={label}>
              <span className="capability-icon"><Icon size={18} /></span>
              <div><span>{label}</span><strong>{value}</strong></div>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default DashboardPage
