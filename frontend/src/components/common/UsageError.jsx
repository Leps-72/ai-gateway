import { AlertCircle, RotateCcw } from 'lucide-react'

function UsageError({ message, onRetry, disabled }) {
  if (!message) return null

  return (
    <div className="inline-error" role="alert">
      <div><AlertCircle size={18} aria-hidden="true" /><span>{message}</span></div>
      <button className="button button-secondary button-small" type="button" onClick={onRetry} disabled={disabled}>
        <RotateCcw size={15} aria-hidden="true" /> Retry
      </button>
    </div>
  )
}

export default UsageError
