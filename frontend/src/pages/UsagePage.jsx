import { Activity, Calculator, Clock3, Coins, Workflow, Zap } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import MetricCard from '../components/common/MetricCard'
import PageHeader from '../components/common/PageHeader'

function UsagePage() {
  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Monitoring"
        title="Usage"
        description="Understand request volume, performance and estimated provider cost."
      />
      <section className="metrics-grid" aria-label="Usage metrics">
        <MetricCard icon={Workflow} label="Requests" hint="Usage data not loaded" />
        <MetricCard icon={Zap} label="Tokens" hint="Usage data not loaded" />
        <MetricCard icon={Clock3} label="Average Latency" hint="Usage data not loaded" />
        <MetricCard icon={Activity} label="Error Rate" hint="Usage data not loaded" />
      </section>
      <section className="card cost-card">
        <div className="section-heading">
          <div><p className="eyebrow">Cost estimation</p><h2>Estimated provider cost</h2><p>Calculated from stored token usage and configured model pricing.</p></div>
          <span className="capability-icon"><Coins size={20} /></span>
        </div>
        <EmptyState
          icon={Calculator}
          title="Cost data not loaded"
          description="The console has not requested usage metrics from the gateway."
        />
      </section>
    </div>
  )
}

export default UsagePage
