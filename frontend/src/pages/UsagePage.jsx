import { Calculator, Coins } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import PageHeader from '../components/common/PageHeader'
import RefreshButton from '../components/common/RefreshButton'
import UsageError from '../components/common/UsageError'
import UsageMetricGrid from '../components/common/UsageMetricGrid'
import useUsage from '../hooks/useUsage'
import { formatCurrency } from '../utils/formatters'

function UsagePage() {
  const { usage, loading, refreshing, error, refresh } = useUsage()
  const initialLoading = loading && !usage

  const renderCost = () => {
    if (initialLoading) {
      return <div className="cost-loading" aria-label="Loading cost estimation"><span className="cost-skeleton" /></div>
    }

    if (!usage) {
      return (
        <EmptyState
          icon={Calculator}
          title="Cost data unavailable"
          description="Usage data could not be loaded from the gateway."
        />
      )
    }

    if (!usage.costEstimationAvailable || usage.estimatedCostUsd == null) {
      return (
        <EmptyState
          icon={Calculator}
          title="Cost estimation unavailable"
          description="Pricing or token information is unavailable for one or more stored requests."
        />
      )
    }

    return (
      <div className="cost-value-panel">
        <span>Estimated provider cost</span>
        <strong>{formatCurrency(usage.estimatedCostUsd)}</strong>
        <small>USD · Based on stored token usage</small>
      </div>
    )
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Monitoring"
        title="Usage"
        description="Understand request volume, performance and estimated provider cost."
        actions={<RefreshButton onClick={refresh} loading={loading || refreshing} />}
      />
      <UsageError message={error} onRetry={refresh} disabled={loading || refreshing} />
      <UsageMetricGrid usage={usage} loading={initialLoading} />
      <section className="card cost-card">
        <div className="section-heading">
          <div><p className="eyebrow">Cost estimation</p><h2>Estimated provider cost</h2><p>Calculated from stored token usage and configured model pricing.</p></div>
          <span className="capability-icon"><Coins size={20} /></span>
        </div>
        {renderCost()}
      </section>
    </div>
  )
}

export default UsagePage
