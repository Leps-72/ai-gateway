import { Activity, Clock3, Workflow, Zap } from 'lucide-react'
import { formatLatency, formatNumber, formatPercentage } from '../../utils/formatters'
import MetricCard from './MetricCard'

function UsageMetricGrid({ usage, loading }) {
  return (
    <section className="metrics-grid" aria-label="Usage metrics">
      <MetricCard
        icon={Workflow}
        label="Requests"
        value={usage ? formatNumber(usage.requests) : null}
        hint="Total AI requests"
        loading={loading}
      />
      <MetricCard
        icon={Zap}
        label="Tokens"
        value={usage ? formatNumber(usage.tokens) : null}
        hint="Input and output tokens"
        loading={loading}
      />
      <MetricCard
        icon={Clock3}
        label="Average Latency"
        value={usage ? formatLatency(usage.averageLatencyMs) : null}
        hint="Mean request duration"
        loading={loading}
      />
      <MetricCard
        icon={Activity}
        label="Error Rate"
        value={usage ? formatPercentage(usage.errorRate) : null}
        hint="Failed requests / all requests"
        loading={loading}
      />
    </section>
  )
}

export default UsageMetricGrid
