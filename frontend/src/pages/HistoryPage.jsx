import { FileClock } from 'lucide-react'
import EmptyState from '../components/common/EmptyState'
import PageHeader from '../components/common/PageHeader'

const columns = ['Status', 'Provider', 'Model', 'Tokens', 'Latency', 'Created']

function HistoryPage() {
  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Monitoring"
        title="Request history"
        description="Review AI requests, provider outcomes and execution metrics."
      />
      <section className="card table-card">
        <div className="table-title-row">
          <div><h2>Conversations</h2><p>Stored gateway request history.</p></div>
          <span className="badge badge-neutral">Not loaded</span>
        </div>
        <div className="table-scroll">
          <table>
            <thead><tr>{columns.map((column) => <th key={column} scope="col">{column}</th>)}</tr></thead>
            <tbody />
          </table>
        </div>
        <EmptyState
          icon={FileClock}
          title="No history loaded"
          description="Conversation records will appear here after the backend connection is enabled."
        />
      </section>
    </div>
  )
}

export default HistoryPage
