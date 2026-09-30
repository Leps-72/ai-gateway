import { RefreshCw } from 'lucide-react'

function RefreshButton({ onClick, loading }) {
  return (
    <button
      className="button button-secondary"
      type="button"
      onClick={onClick}
      disabled={loading}
      aria-label={loading ? 'Refreshing usage data' : 'Refresh usage data'}
    >
      <RefreshCw className={loading ? 'spin' : ''} size={16} aria-hidden="true" />
      {loading ? 'Refreshing…' : 'Refresh'}
    </button>
  )
}

export default RefreshButton
