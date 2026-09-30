import {
  Activity,
  Braces,
  Clock3,
  KeyRound,
  ShieldCheck,
  Sparkles,
  Timer,
  WalletCards,
  Workflow,
  Zap,
} from 'lucide-react'
import MetricCard from '../components/common/MetricCard'
import PageHeader from '../components/common/PageHeader'

const capabilities = [
  { label: 'Authentication', value: 'JWT', icon: KeyRound },
  { label: 'AI Provider', value: 'Gemini', icon: Sparkles },
  { label: 'Rate Limiting', value: 'Enabled', icon: ShieldCheck },
  { label: 'Structured Output', value: 'Enabled', icon: Braces },
  { label: 'Cost Estimation', value: 'Supported', icon: WalletCards },
]

function DashboardPage() {
  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Overview"
        title="Dashboard"
        description="Monitor your AI Gateway usage and performance."
      />

      <section className="metrics-grid" aria-label="Gateway metrics">
        <MetricCard icon={Workflow} label="Requests" hint="Connect /usage to load" />
        <MetricCard icon={Zap} label="Tokens" hint="Connect /usage to load" />
        <MetricCard icon={Clock3} label="Average Latency" hint="Connect /usage to load" />
        <MetricCard icon={Activity} label="Error Rate" hint="Connect /usage to load" />
      </section>

      <section className="card overview-card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Configuration</p>
            <h2>Gateway overview</h2>
            <p>Core capabilities available in the current backend.</p>
          </div>
          <span className="badge badge-neutral"><Timer size={14} /> Runtime status pending</span>
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
